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

package pulpcore.platform.applet;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import javax.imageio.ImageIO;
import pulpcore.Build;
import pulpcore.CoreSystem;
import pulpcore.image.CoreImage;
import pulpcore.Input;
import pulpcore.math.CoreMath;
import pulpcore.platform.AppContext;
import pulpcore.platform.Platform;
import pulpcore.platform.PolledInput;
import pulpcore.platform.Surface;
import pulpcore.scene.Scene;
import pulpcore.Stage;
import pulpcore.util.Base64;
import pulpcore.util.ByteArray;

public final class AppletAppContext extends AppContext {
    
    private CoreApplet applet;
    private Surface surface;
    private AppletInput inputSystem;
    private Stage stage;
    private Object jsObject;
    private boolean firstFrameDrawn = false;
    private boolean enableLiveConnect;
    
    public AppletAppContext(CoreApplet app, SystemTimer timer) {
        this.applet = app;
        this.enableLiveConnect = true;
        
        if (CoreSystem.isWindows() && isMozillaFamily() && isVirtualHost()) {
            enableLiveConnect = false;
        }
        
        // Create the JSObject for JavaScript functionality
        if (enableLiveConnect) {
            try {
                // JSObject jsObject = netscape.javascript.JSObject.getWindow(this);
                Class c = Class.forName("netscape.javascript.JSObject");
                Method getWindow = c.getMethod("getWindow", 
                    new Class[] { Class.forName("java.applet.Applet") } );
                jsObject = getWindow.invoke(null, new Object[] { app });
            }
            catch (Throwable t) {
                // Ignore
            }
        }
        
        setTalkBackField("pulpcore.platform", "Applet");
        setTalkBackField("pulpcore.platform.timer", timer.getName());
        setTalkBackField("pulpcore.platform.javascript", "" + (jsObject != null));  
        setTalkBackField("pulpcore.url", getBaseURL().toString());
        createSurface(app);
        stage = new Stage(surface, this);
    }
    
    private boolean isMozillaFamily() {
        String browserName = getAppProperty("browsername");
        if (browserName == null) {
            return false;
        }
        return (browserName.equals("Firefox") || 
            browserName.equals("Mozilla") || 
            browserName.equals("Netscape"));
    }
    
    private boolean isVirtualHost() {
        String host = getBaseURL().getHost();
        if (host == null) {
            return false;
        }
        try {
            byte[] ip = InetAddress.getByName(host).getAddress();
            String realHost = InetAddress.getByAddress(ip).getHostName();
            return !host.endsWith(realHost);
        }
        catch (Exception ex) {
            if (Build.DEBUG) CoreSystem.print("Couldn't determine host", ex);
        }
        return false;
    }
    
    /* package-private */ CoreApplet getApplet() {
        return applet;
    }
    
    public String getAppProperty(String name) {
        return applet.getParameter(name);
    }
    
    public Scene createFirstScene() {
        return applet.createFirstScene();
    }
    
    public void start() {
        if (stage != null) {
            stage.start();
        }
        
        if (Build.DEBUG) printMemory("App: start");
    }
    
    public void stop() {
        if (stage != null) {
            stage.stop();
        }
        if (Build.DEBUG) printMemory("App: stop");
    }
    
    public void destroy() {
        if (stage != null) {
            stage.destroy();
            stage = null;
        }
        surface = null;
        jsObject = null;
        inputSystem = null;
        setMute(true);
        if (Build.DEBUG) printMemory("App: destroy");
    }
    
    /**
        Returns true if calling JavaScript via LiveConnect is enabled.
    */
    public boolean isJavaScriptEnabled() {
        if (jsObject == null) {
            return false;
        }
        String name = "foo" + CoreMath.rand(0, 9999);
        String value = "bar" + CoreMath.rand(0, 9999);
        callJavaScript("pulpcore_setCookie", new Object[] { name, value });
        boolean enabled = value.equals(callJavaScript("pulpcore_getCookie", name));
        callJavaScript("pulpcore_deleteCookie", name);
        return enabled;
    }

    /**
        Calls a JavaScript method with no arguments.
    */
    public Object callJavaScript(String method) {
        return callJavaScript(method, (Object[])null);
    }
    
    /**
        Calls a JavaScript method with one argument.
    */
    public Object callJavaScript(String method, Object arg) {
        return callJavaScript(method, new Object[] { arg });
    }
    
    /**
        Calls a JavaScript method with a list of arguments.
    */
    public Object callJavaScript(String method, Object[] args) {
        
        if (jsObject == null) {
            return null;
        }
        
        try {
            Class c = Class.forName("netscape.javascript.JSObject");
            Method call = c.getMethod("call", 
                new Class[] { method.getClass(), new Object[0].getClass() });
            return call.invoke(jsObject, new Object[] { method, args });
        }
        catch (Throwable t) {
            if (Build.DEBUG) print("Couldn't call JavaScript method " + method, t);
        }   
        return null;
    }
    
    public void notifyFrameComplete() {
        if (!firstFrameDrawn) {
            firstFrameDrawn = true;
            if (enableLiveConnect) {
                callJavaScript("pulpcore_appletLoaded");
            }
            else {
                // This works fine in Firefox
                try {
                    applet.getAppletContext().showDocument(
                        new URL("javascript: pulpcore_appletLoaded();"));
                }
                catch (Exception ex) {
                    if (Build.DEBUG) CoreSystem.print("pulpcore_appletLoaded", ex);
                }
            }
        }
    }
    
    private void createSurface(CoreApplet app) {
        Component inputComponent = app;
        surface = null;
        boolean useBufferStrategy = false;
        
        /*
            On Java 6, use BufferStrategy on all platforms.
            
            On Java 5, use BufferedImageSurface on Windows and Linux
            
            BufferStrategy has a problem on:
            * Mac OS X 10.5 (Leopard) - uses lots of CPU. Cannot reach 60fps (55fps max).
            
            Repainting (BufferedImageSurface) has a problem on:
            * Mac OS X + Firefox (using the "apple.awt.MyCPanel" peer) - repaint events are lost 
              when moving the mouse over the applet.
            * Mac OS X (all) - cannot reach 60fps (55fps max).
            
            TODO: Test again when 32-bit Java 6 on Mac is available. BufferStrategy still seems to
            use more processor in some cases, but it's hard to judge because of the different
            arch.
        */
        if (CoreSystem.isJava16orNewer()) {
            useBufferStrategy = true;
        }
        else if (CoreSystem.isMacOSX() && CoreSystem.isJava15orNewer()) {
            if (CoreSystem.isMacOSXLeopardOrNewer()) {
                // For Mac OS X 10.5:
                // Only use BufferStrategy on Firefox (the "apple.awt.MyCPanel" peer)
                Object peer = applet.getPeer();
                if (peer != null && "apple.awt.MyCPanel".equals(peer.getClass().getName())) {
                    useBufferStrategy = true;
                }
                else {
                    useBufferStrategy = false;
                }
            }
            else {
                // Before Mac OS X 10.5, BufferStrategy was perfect.
                useBufferStrategy = true;
            }
        }
        else {
            useBufferStrategy = false;
        }
        
        if (surface == null && useBufferStrategy) {
            try {
                Class.forName("java.awt.image.BufferStrategy");
                surface = new BufferStrategySurface(app);
                inputComponent = ((BufferStrategySurface)surface).getCanvas();
                setTalkBackField("pulpcore.platform.surface", surface.toString());
            }
            catch (Exception ex) {
                // ignore
            }
        }
        
        // Try to use BufferedImage. It's faster than using ImageProducer, and,
        // on some VMs, the ImageProducerSurface creates a lot of 
        // garbage for the GC to cleanup.
        if (surface == null) {
            try {
                surface = new BufferedImageSurface(app);
                setTalkBackField("pulpcore.platform.surface", surface.toString());
            }
            catch (Exception ex) {
                // ignore
            }
        }
        
        // BufferedImage is not available - should not happen
        if (surface == null) {
            setTalkBackField("pulpcore.platform.surface", "none");
        }
        
        inputSystem = new AppletInput(inputComponent);
    }
    
    public Stage getStage() {
        return stage;
    }
    
    public Surface getSurface() {
        return surface;
    }
    
    public void pollInput() {
        inputSystem.pollInput();
    }
           
    public PolledInput getPolledInput() {
        return inputSystem.getPolledInput();
    }
           
    public void requestKeyboardFocus() {
        inputSystem.requestKeyboardFocus();
    }
           
    public int getCursor() {
        return inputSystem.getCursor();
    }
           
    public void setCursor(int cursor) {
        inputSystem.setCursor(cursor);
    }
    
    public URL getBaseURL() {
        return applet.getCodeBase();
    }
    
    public void showDocument(String url, String target) {
        URL parsedURL;
        
        try {
            parsedURL = new URL(url);
        }
        catch (MalformedURLException ex) {
            if (Build.DEBUG) print("Invalid URL: " + url);
            return;
        }
        
        applet.getAppletContext().showDocument(parsedURL, target);
    }
    
    public String getLocaleLanguage() {
        try {
            return applet.getLocale().getLanguage();
        }
        catch (Throwable t) {
            return "";
        }
        
    }
    
    public String getLocaleCountry() {
        try {
            return applet.getLocale().getCountry();
        }
        catch (Throwable t) {
            return "";
        }
    }
    
    public void putUserData(String key, byte[] data) {
        String name = "pulpcore_" + key;
        String value = Base64.encodeURLSafe(data);
        
        callJavaScript("pulpcore_setCookie", new Object[] { name, value });
    }
        
    public byte[] getUserData(String key) {
        String name = "pulpcore_" + key;
        
        Object result = callJavaScript("pulpcore_getCookie", name);
        if (result == null) {
            return null;
        }
        else {
            String value = result.toString();
            
            // Reset the expiration date to another 90 days
            callJavaScript("pulpcore_setCookie", new Object[] { name, value });
            
            return Base64.decodeURLSafe(value);
        }
    }
    
    public void removeUserData(String key) {
        String name = "pulpcore_" + key;
        
        callJavaScript("pulpcore_deleteCookie", name);
    }
    
    public CoreImage loadImage(ByteArray in) {
        if (in == null) {
            return null;
        }
        
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(in.getData()));

            boolean isOpaque = true;
            ColorModel model = image.getColorModel();
            if (model instanceof DirectColorModel) {
                isOpaque = ((DirectColorModel)model).getAlphaMask() == 0;
            }
            
            // Convert to TYPE_INT_RGB or TYPE_INT_ARGB_PRE 
            if (image.getType() != BufferedImage.TYPE_INT_RGB &&
                image.getType() != BufferedImage.TYPE_INT_ARGB_PRE)
            {
                int type = isOpaque ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB_PRE;
                BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(),
                    type);
                Graphics g = newImage.getGraphics();
                g.drawImage(image, 0, 0, null);
                image = newImage;
            }
            
            int[] data = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
            
            if (image.getType() == BufferedImage.TYPE_INT_RGB) {
                // Add the alpha component to the data
                for (int i = 0; i < data.length; i++) {
                    data[i] = 0xff000000 | data[i];
                }
            }
            
            // Convert to CoreImage
            return new CoreImage(image.getWidth(), image.getHeight(), isOpaque, data);
        }
        catch (Exception ex) {
            if (Build.DEBUG) CoreSystem.print("ImageIO", ex);
            return null;
        }
    }
}
