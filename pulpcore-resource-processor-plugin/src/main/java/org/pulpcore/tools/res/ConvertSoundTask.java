/*
    Copyright (c) 2007-2010, Interactive Pulp, LLC
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

package org.pulpcore.tools.res;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

public class ConvertSoundTask extends AbstractMojo {
    
    private File srcFile;
    private File destFile;
    
    public void setSrcFile(File srcFile) {
        this.srcFile = srcFile;
    }
    
    
    public void setDestFile(File destFile) {
        this.destFile = destFile;
    }
    
    public void execute() throws MojoExecutionException {
        if (srcFile == null) {
            throw new MojoExecutionException("The srcFile is not specified.");
        }
        if (destFile == null) {
            throw new MojoExecutionException("The destFile is not specified.");
        }
                
        try {
            convert();
        }
        catch (UnsupportedAudioFileException ex) {
            throw new MojoExecutionException("Not a valid sound file: " + srcFile);
        }
        catch (IOException ex) {
            throw new MojoExecutionException("Error creating sound " + srcFile, ex);
        }
    }
    
    private void convert() throws UnsupportedAudioFileException, IOException, MojoExecutionException {
        AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(srcFile);
        AudioFormat format = fileFormat.getFormat();
        if (isValid(format)) {
            FileUtils.copyFile(srcFile, destFile, true);
            getLog().info("Copied " + srcFile);
        }
        else {
            // Try to convert
            int channels = (format.getChannels() >= 2) ? 2 : 1;
            AudioFormat goalFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                format.getSampleRate(), 16, channels, 2*channels, format.getSampleRate(), false);
                
            AudioInputStream sourceStream = AudioSystem.getAudioInputStream(srcFile);
            AudioInputStream convertedStream = null;
            try {
                convertedStream = AudioSystem.getAudioInputStream(goalFormat, sourceStream);
                AudioSystem.write(convertedStream, AudioFileFormat.Type.WAVE, destFile);
                getLog().info("Converted " + srcFile);
            }
            catch (IllegalArgumentException ex) {
                throw new MojoExecutionException("Could not convert: " + srcFile + " (" + format + ")");
            }
            finally {
                sourceStream.close();
                if (convertedStream != null) {
                    convertedStream.close();
                }
            }
        }
    }
    
    private boolean isValid(AudioFormat format) {
        float sampleRate = format.getSampleRate();
        if (sampleRate >= 8000 && sampleRate <= 8100) {
            sampleRate = 8000;
        }
        
        if (format.getChannels() < 1 || format.getChannels() > 2) {
            return false;
        }
        else if (sampleRate != 8000 && sampleRate != 11025 && 
            sampleRate != 22050 && sampleRate != 44100) 
        {
            return false;
        }        
        else if (format.getEncoding() == AudioFormat.Encoding.ULAW) {
            return (format.getSampleSizeInBits() == 8);
        }
        else if (format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED) {
            return (format.isBigEndian() == false && format.getSampleSizeInBits() == 16);
        }
        else {
            return false;
        }
    }
}
