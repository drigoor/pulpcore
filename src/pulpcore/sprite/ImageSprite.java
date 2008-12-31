/*
    Copyright (c) 2008, Interactive Pulp, LLC
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

package pulpcore.sprite;

import pulpcore.animation.Bool;
import pulpcore.animation.Property;
import pulpcore.image.CoreGraphics;
import pulpcore.image.CoreImage;
import pulpcore.math.CoreMath;

/**
    An image-based sprite. The image can be an {@link pulpcore.image.AnimatedImage}.
    To ignore the CoreImage's hotspot, call {@link #setAnchor(int) } with an 
    anchor other than Sprite.DEFAULT, like Sprite.NORTH_WEST.
    <p>
    By default, ImageSprites use pixel-level checking for intersection tests. Use 
    {@link #setPixelLevelChecks(boolean) } to disable this feature.
*/
public class ImageSprite extends Sprite {
    
    private CoreImage image;
    private boolean pixelLevel = true;
    
    /**
        Flag indicating whether the edges of this ImageSprite are anti-aliased when rotating or
        drawing at fractional locations. The default value is {@code true}.
    */
    public final Bool antiAlias = new Bool(this, true);
    
    /**
        Creates an ImageSprite that has the same dimensions as the image.
    */
    public ImageSprite(String imageAsset, int x, int y) {
        this(imageAsset, x, y, -1, -1);
    }
    
    
    /**
        Creates an ImageSprite that has the same dimensions as the image.
    */
    public ImageSprite(CoreImage image, int x, int y) {
        this(image, x, y, -1, -1);
    }
    
    /**
        Creates an ImageSprite that draws the image scaled to the specified dimensions.
    */
    public ImageSprite(String imageAsset, int x, int y, int w, int h) {
        this(CoreImage.load(imageAsset), x, y, w, h);
    }
    
    /**
        Creates an ImageSprite that draws the image scaled to the specified dimensions.
    */
    public ImageSprite(CoreImage image, int x, int y, int w, int h) {
        super(x, y, w, h);
        if (image == null) {
            image = CoreImage.getBrokenImage();
        }
        setImage(image);
        if (w < 0) {
            width.set(image.getWidth());
        }
        if (h < 0) {
            height.set(image.getHeight());
        }
    }
    
    /**
        Creates an ImageSprite that has the same dimensions as the image.
    */
    public ImageSprite(String imageAsset, double x, double y) {
        this(imageAsset, x, y, -1, -1);
    }
    
    /**
        Creates an ImageSprite that has the same dimensions as the image.
    */
    public ImageSprite(CoreImage image, double x, double y) {
        this(image, x, y, -1, -1);
    }
    
    /**
        Creates an ImageSprite that draws the image scaled to the specified dimensions.
    */
    public ImageSprite(String imageAsset, double x, double y, double w, double h) {
        this(CoreImage.load(imageAsset), x, y, w, h);
    }
    
    /**
        Creates an ImageSprite that draws the image scaled to the specified dimensions.
    */
    public ImageSprite(CoreImage image, double x, double y, double w, double h) {
        super(x, y, w, h);
        setImage(image);
        if (w < 0) {
            width.set(image.getWidth());
        }
        if (h < 0) {
            height.set(image.getHeight());
        }
    }
    
    /**
        Gets this ImageSprite's internal image.
    */
    public CoreImage getImage() {
        return image;
    }
    
    /**
        Sets this ImageSprite's internal image. The width and height of this ImageSprite are
        not changed.
    */
    public void setImage(String imageAsset) {
        setImage(CoreImage.load(imageAsset));
    }
    
    /**
        Sets this ImageSprite's internal image. The width and height of this ImageSprite are
        not changed.
    */
    public void setImage(CoreImage image) {
        if (this.image != image) {
            this.image = image;
            setDirty(true);
        }
    }
    
    public void update(int elapsedTime) {
        super.update(elapsedTime);
        
        antiAlias.update(elapsedTime);
        
        if (image != null) {
            boolean changed = image.update(elapsedTime);
            if (changed) {
                setDirty(true);
            }
        }
    }

    public void propertyChange(Property p) {
        super.propertyChange(p);
        if (p == antiAlias) {
            setDirty(true);
        }
    }
    
    /**
        Sets whether this sprite should use pixel-level checking for intersections and picking.
    */
    public final void setPixelLevelChecks(boolean pixelLevel) {
        this.pixelLevel = pixelLevel;
    }
    
    /**
        Returns true if this sprite should use pixel-level checks for intersections and picking.
        @see #setPixelLevelChecks(boolean)
    */
    public final boolean getPixelLevelChecks() {
        return pixelLevel;
    }
    
    protected int getNaturalWidth() {
        if (image != null) {
            return CoreMath.toFixed(image.getWidth());
        }
        else {
            return super.getNaturalWidth();
        }
    }
    
    protected int getNaturalHeight() {
        if (image != null) {
            return CoreMath.toFixed(image.getHeight());
        }
        else {
            return super.getNaturalHeight();
        }
    }
    
    protected int getAnchorX() {
        if (image != null && getAnchor() == DEFAULT) {
            return CoreMath.toFixed(image.getHotspotX());
        }
        else {
            return super.getAnchorX();
        }
    }
    
    protected int getAnchorY() {
        if (image != null && getAnchor() == DEFAULT) {
            return CoreMath.toFixed(image.getHotspotY());
        }
        else {
            return super.getAnchorY();
        }
    }
    
    protected boolean isTransparent(int localX, int localY) {
        if (getPixelLevelChecks()) {
            return (image == null || image.isTransparent(localX, localY));
        }
        else {
            return super.isTransparent(localX, localY);
        }
    }
    
    protected void drawSprite(CoreGraphics g) {
        if (image != null) {
            int oldEdgeClamp = g.getEdgeClamp();
            int newEdgeClamp = antiAlias.get() ? CoreGraphics.EDGE_CLAMP_NONE :
                CoreGraphics.EDGE_CLAMP_ALL;
            g.setEdgeClamp(newEdgeClamp);
            g.drawImage(image);
            g.setEdgeClamp(oldEdgeClamp);
        }
    }
}