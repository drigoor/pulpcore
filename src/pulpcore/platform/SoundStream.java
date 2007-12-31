/*
    Copyright (c) 2007, Interactive Pulp, LLC
    All rights reserved.
    
    Redistribution and use in source and binary forms, with or without 
    modification, are permitted provided that the following conditions are met:

        * Redistributions of source code must retain the above copyright 
          notice, this list of conditions and the following disclaimer.
        * Redistributions in binary form must reproduce the above copyright 
          notice, this list of conditions and the following disclaimer in the 
          documentation and/or other materials provided with the distribution.
        * Neither the name of Interactive Pulp, LLC nor the names of its 
          contributors may be used to endorse or promote products derived from 
          this software without specific prior written permission.
    
    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
    POSSIBILITY OF SUCH DAMAGE.
*/

package pulpcore.platform;

import pulpcore.animation.Fixed;
import pulpcore.math.CoreMath;
import pulpcore.sound.Sound;


public class SoundStream {
    
    // The number of milliseconds to fade from a mute/unmute
    public static final int MUTE_TIME = 5;
    
    // Number of frames to render while animating. Should represent at least 1 ms at 44100Hz
    // (that is, greater than 44 frames)
    private static final int MAX_FRAMES_TO_RENDER_WHILE_ANIMATING = 64;
    
    private final AppContext context;
    private final Sound sound;
    private final int loopFrame;
    private final int numLoopFrames;
    
    private final Fixed level;
    private final Fixed pan;
    private final Fixed outputLevel = new Fixed();
    
    private int frame;
    private int animationFrame;
    private boolean lastMute;
    private double lastMasterVolume;
    private boolean loop;
    
    
    public SoundStream(AppContext context, Sound sound, Fixed level, Fixed pan, 
        int loopFrame, int numLoopFrames, int animationFrameDelay)
    {
        this.context = context;
        this.sound = sound;
        this.level = level;
        this.pan = pan;
        this.loopFrame = loopFrame;
        this.numLoopFrames = numLoopFrames;
        
        this.loop = (numLoopFrames > 0);
        this.frame = 0;
        this.animationFrame = -animationFrameDelay;
        
        this.lastMute = isMute();
        this.lastMasterVolume = getMasterVolume();
        this.outputLevel.set(lastMute ? 0 : lastMasterVolume);
    }
    
    public boolean isMute() {
        return context != null && context.isMute();
    }
    
    private double getMasterVolume() {
        if (context == null) {
            return 0;
        }
        else {
            return context.getSoundVolume();
        }
    }
    
    public boolean isFinished() {
        return (frame >= sound.getNumFrames());
    }
    
    public void skip(int numFrames) {
        int oldAnimationTime = getAnimationTime();
        int oldFrame = frame;
        int newFrame = frame + numFrames;
        animationFrame += numFrames;
        
        if (inLoop()) {
            frame = loopFrame + ((newFrame - loopFrame) % numLoopFrames);
        }
        else if (newFrame > sound.getNumFrames()) {
            frame = sound.getNumFrames();
        }
        else {
            frame = newFrame;
        }
        
        int elapsedTime = getAnimationTime() - oldAnimationTime;
        level.update(elapsedTime);
        pan.update(elapsedTime);
        outputLevel.update(elapsedTime);
    }
    
    
    private int getAnimationTime() {
        if (animationFrame < loopFrame) {
            return 0;
        }
        else {
            return 1000 * (animationFrame - loopFrame) / sound.getSampleRate();
        }
    }
    
    
    private boolean inLoop() {
        return (loop && frame >= loopFrame && frame < loopFrame + numLoopFrames);
    }
    
    
    public void render(byte[] dest, int destOffset, int destChannels, int numFrames) {
        boolean mute = isMute();
        double masterVolume = getMasterVolume();
        if (context != null && context.getStage() == null) {
            // Destroyed!
            mute = true;
            loop = false;
        }
        
        // Gradually change sound volume over time to reduce popping
        if (lastMute != mute || lastMasterVolume != masterVolume) {
            double currLevel = outputLevel.get();
            double goalLevel = mute ? 0 : masterVolume;
            outputLevel.animateTo(goalLevel, MUTE_TIME);
            lastMute = mute;
            lastMasterVolume = masterVolume;
        }
        
        int destFrameSize = destChannels * 2;
        
        while (numFrames > 0) {
            
            boolean isAnimating = level.isAnimating() || outputLevel.isAnimating() || 
                pan.isAnimating();
            int currLevel = getCurrLevel();
            int currPan = getCurrPan();
            
            int framesToRender = numFrames;
            if (isAnimating) {
                // Only render a few frames, then recalcuate animation parameters
                framesToRender = Math.min(MAX_FRAMES_TO_RENDER_WHILE_ANIMATING, framesToRender);
            }
            if (inLoop()) {
                // Don't render past loop boundary
                framesToRender = Math.min(framesToRender, loopFrame + numLoopFrames - frame); 
            }
            
            // Figure out the next level and pan (for interpolation)
            int startFrame = frame;
            skip(framesToRender);
            int nextLevel = getCurrLevel();
            int nextPan = getCurrPan();
            
            // Render
            if (currLevel > 0 || nextLevel > 0) {
                sound.getSamples(dest, destOffset, destChannels, startFrame, framesToRender);
            }
            render(dest, destOffset, destChannels, framesToRender, 
                currLevel, nextLevel, currPan, nextPan);
            
            // Inc offsets
            numFrames -= framesToRender;
            destOffset += framesToRender * destFrameSize;
        }
    }
    
    
    private int getCurrLevel() {
        int currLevel = level.getAsFixed();
        if (frame >= sound.getNumFrames()) {
            currLevel = 0;
        }
        if (currLevel <= 0) {
            currLevel = 0;
            loop = false;
        }
        else {
            currLevel = CoreMath.mul(currLevel, outputLevel.getAsFixed());
        }
        return currLevel;
    }
    
    
    private int getCurrPan() {
        int currPan = pan.getAsFixed();
        if (currPan < -CoreMath.ONE) {
            currPan = -CoreMath.ONE;
        }
        else if (currPan > CoreMath.ONE) {
            currPan = CoreMath.ONE;
        }
        return currPan;
    }
    
    
    private static void render(byte[] data, int offset, int channels,
        int numFrames, int startLevel, int endLevel, int startPan, int endPan)
    {
        int frameSize = channels * 2;
        
        if (startLevel <= 0 && endLevel <= 0) {
            // Mute
            int length = numFrames * frameSize;
            for (int i = 0; i < length; i++) {
                data[offset++] = 0;
            }
        }
        else if (channels == 1 || (startPan == 0 && endPan == 0)) {
            // No panning (both stereo and mono rendering)
            if (startLevel != CoreMath.ONE || endLevel != CoreMath.ONE) {
                int numSamples = numFrames*channels;
                int level = startLevel;
                int levelInc = (endLevel - startLevel) / numSamples;
                for (int i = 0; i < numSamples; i++) {
                    int input = getSample(data, offset); 
                    int output = (input * level) >> CoreMath.FRACTION_BITS;
                    setSample(data, offset, output);
                    
                    offset += 2;
                    level += levelInc;
                }
            }
        }
        else {
            // Stereo sound with panning
            int startLeftLevel4LeftInput;
            int startLeftLevel4RightInput;
            int startRightLevel4LeftInput;
            int startRightLevel4RightInput;
            int endLeftLevel4LeftInput;
            int endLeftLevel4RightInput;
            int endRightLevel4LeftInput;
            int endRightLevel4RightInput;
            if (startPan < 0) {
                startLeftLevel4LeftInput = CoreMath.ONE + startPan / 2;
                startLeftLevel4RightInput = -startPan / 2;
                startRightLevel4LeftInput = 0;
                startRightLevel4RightInput = CoreMath.ONE + startPan;
            }
            else {
                startLeftLevel4LeftInput = CoreMath.ONE - startPan;
                startLeftLevel4RightInput = 0;
                startRightLevel4LeftInput = startPan / 2;
                startRightLevel4RightInput = CoreMath.ONE - startPan / 2;
            }
            if (endPan < 0) {
                endLeftLevel4LeftInput = CoreMath.ONE + endPan / 2;
                endLeftLevel4RightInput = -endPan / 2;
                endRightLevel4LeftInput = 0;
                endRightLevel4RightInput = CoreMath.ONE + endPan;
            }
            else {
                endLeftLevel4LeftInput = CoreMath.ONE - endPan;
                endLeftLevel4RightInput = 0;
                endRightLevel4LeftInput = endPan / 2;
                endRightLevel4RightInput = CoreMath.ONE - endPan / 2;
            }
            if (startLevel != CoreMath.ONE) {
                startLeftLevel4LeftInput = CoreMath.mul(startLevel, startLeftLevel4LeftInput);
                startLeftLevel4RightInput = CoreMath.mul(startLevel, startLeftLevel4RightInput);
                startRightLevel4LeftInput = CoreMath.mul(startLevel, startRightLevel4LeftInput);
                startRightLevel4RightInput = CoreMath.mul(startLevel, startRightLevel4RightInput);
            }
            if (endLevel != CoreMath.ONE) {
                endLeftLevel4LeftInput = CoreMath.mul(endLevel, endLeftLevel4LeftInput);
                endLeftLevel4RightInput = CoreMath.mul(endLevel, endLeftLevel4RightInput);
                endRightLevel4LeftInput = CoreMath.mul(endLevel, endRightLevel4LeftInput);
                endRightLevel4RightInput = CoreMath.mul(endLevel, endRightLevel4RightInput);
            }
            
            int leftLevel4LeftInput = startLeftLevel4LeftInput;
            int leftLevel4RightInput = startLeftLevel4RightInput;
            int rightLevel4LeftInput = startRightLevel4LeftInput;
            int rightLevel4RightInput = startRightLevel4RightInput;
            int leftLevel4LeftInputInc = 
                (endLeftLevel4LeftInput - startLeftLevel4LeftInput) / numFrames;
            int leftLevel4RightInputInc = 
                (endLeftLevel4RightInput - startLeftLevel4RightInput) / numFrames;
            int rightLevel4LeftInputInc = 
                (endRightLevel4LeftInput - startRightLevel4LeftInput) / numFrames;
            int rightLevel4RightInputInc = 
                (endRightLevel4RightInput - startRightLevel4RightInput) / numFrames;
            for (int i = 0; i < numFrames; i++) {
                int leftInput = getSample(data, offset);
                int rightInput = getSample(data, offset + 2);
                int leftOutput = 
                    (leftInput * leftLevel4LeftInput + rightInput * leftLevel4RightInput) >>
                    CoreMath.FRACTION_BITS;
                int rightOutput = 
                    (leftInput * rightLevel4LeftInput + rightInput * rightLevel4RightInput) >>
                    CoreMath.FRACTION_BITS;                        
                setSample(data, offset, leftOutput);
                setSample(data, offset + 2, rightOutput);
                
                offset += 4;
                leftLevel4LeftInput += leftLevel4LeftInputInc;
                leftLevel4RightInput += leftLevel4RightInputInc;
                rightLevel4LeftInput += rightLevel4LeftInputInc;
                rightLevel4RightInput += rightLevel4RightInputInc;
            }
        }
    }
    
    
    public static int getSample(byte[] data, int offset) {
        // Signed little endian
        return (data[offset + 1] << 8) | (data[offset] & 0xff);
    }
    
    
    public static void setSample(byte[] data, int offset, int sample) {
        // Signed little endian
        data[offset] = (byte)sample;
        data[offset + 1] = (byte)(sample >> 8);
    }    
}


