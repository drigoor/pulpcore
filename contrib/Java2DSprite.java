import java.awt.AlphaComposite;
import java.awt.color.ColorSpace;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.Hashtable;
import pulpcore.image.CoreImage;
import pulpcore.sprite.ImageSprite;

/**
    Sprite that allows drawing with Java2D and works with PulpCore's dirty rectangles and 
    blend modes. Subclasses overwrite the needsRedraw() and draw() methods.
    
    Example:
    <pre>
    arc = new Java2DSprite(0, 0, 100, 100) {
            
        private int lastX = 0;
        private int strokeWidth = 5;
        
        public boolean needsRedraw() {
            return Input.getMouseX() != lastX;
        }

        public void draw(Graphics2D g) {
            lastX = Input.getMouseX();
            g.setColor(Color.RED);
            g.setStroke(new BasicStroke(strokeWidth)); 
            g.drawArc(strokeWidth, strokeWidth, 
                100-strokeWidth*2,
                100-strokeWidth*2,
                0, lastX * 360 / Stage.getWidth());
        }
    };
    </pre>
*/
public abstract class Java2DSprite extends ImageSprite {
    
    private BufferedImage bufferedImage;
    private boolean firstUpdate;
    private boolean clearBeforeDraw;
    
    public Java2DSprite(int x, int y, int w, int h) {
        this(x, y, w, h, true);
    }
    
    public Java2DSprite(int x, int y, int w, int h, boolean clearBeforeDraw) {
        super(new CoreImage(w, h, false), x, y);
        this.clearBeforeDraw = clearBeforeDraw;
        this.bufferedImage = getBufferedImageView(getImage());
        this.firstUpdate = true;
    }
    
    private static BufferedImage getBufferedImageView(CoreImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        
        DirectColorModel colorModel;
        SinglePixelPackedSampleModel sampleModel;
        
        if (image.isOpaque()) {
            colorModel = new DirectColorModel(24, 0xff0000, 0x00ff00, 0x0000ff);
            sampleModel = new SinglePixelPackedSampleModel(
                DataBuffer.TYPE_INT, w, h, new int[] { 0xff0000, 0x00ff00, 0x0000ff }); 
        }
        else {
            colorModel = new DirectColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
                32, 0x00ff0000, 0x0000ff00, 0x000000ff, 0xff000000,
                true, DataBuffer.TYPE_INT); 
            sampleModel = new SinglePixelPackedSampleModel(
                DataBuffer.TYPE_INT, w, h, new int[] { 
                0x00ff0000, 0x0000ff00, 0x000000ff, 0xff000000 }); 
        }
        
        DataBuffer dataBuffer = new DataBufferInt(image.getData(), w * h);
        WritableRaster raster = Raster.createWritableRaster(
            sampleModel, dataBuffer, new Point(0,0));
        
        return new BufferedImage(colorModel, raster, true, new Hashtable());        
    }
    
    public void update(int elapsedTime) {
        super.update(elapsedTime);
        if (needsRedraw() || firstUpdate) {
            Graphics2D g = bufferedImage.createGraphics();
            if (clearBeforeDraw) {
                g.setComposite(AlphaComposite.Clear);
                g.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
                g.setComposite(AlphaComposite.SrcOver);
            }
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            draw(g);
            g.dispose();
            setDirty(true);
            firstUpdate = false;
        }
    }
    
    protected abstract boolean needsRedraw();
    
    protected abstract void draw(Graphics2D g);
}