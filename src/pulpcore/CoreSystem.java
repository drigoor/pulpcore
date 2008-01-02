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

package pulpcore;

import java.net.URL;
import pulpcore.net.Upload;
import pulpcore.platform.AppContext;
import pulpcore.platform.Platform;
import pulpcore.platform.SoundEngine;

/**
    The CoreSystem class contains useful platform-specific methods.
    The class cannot be instantiated.
*/
public class CoreSystem {

    static {
        String osName = getJavaProperty("os.name");
        String osVersion = getJavaProperty("os.version");
        String javaVersion = getJavaProperty("java.version");
        if (osName == null) {
            osName = "";
        }
        if (osVersion == null) {
            osVersion = "";
        }
        if (javaVersion == null) {
            javaVersion = "1.0";
        }
        
        IS_MAC_OS_X = osName.equals("Mac OS X");
        IS_MAC_OS_X_LEOPARD = osName.equals("Mac OS X") && (osVersion.compareTo("10.5") >= 0);
        
        IS_WINDOWS = osName.startsWith("Windows");
        IS_WINDOWS_XP = osName.startsWith("Windows") && (osVersion.compareTo("5.1") >= 0);

        IS_JAVA_1_3 = (javaVersion.compareTo("1.3") >= 0);
        IS_JAVA_1_4 = (javaVersion.compareTo("1.4") >= 0);
        IS_JAVA_1_5 = (javaVersion.compareTo("1.5") >= 0);
        IS_JAVA_1_6 = (javaVersion.compareTo("1.6") >= 0);
        IS_JAVA_1_7 = (javaVersion.compareTo("1.7") >= 0);
    }

    private static final boolean IS_MAC_OS_X;
    private static final boolean IS_MAC_OS_X_LEOPARD;
    private static final boolean IS_WINDOWS;
    private static final boolean IS_WINDOWS_XP;
    private static final boolean IS_JAVA_1_3;
    private static final boolean IS_JAVA_1_4;
    private static final boolean IS_JAVA_1_5;
    private static final boolean IS_JAVA_1_6;
    private static final boolean IS_JAVA_1_7;

    private static Platform platform;
    
    
    // Prevent instantiation
    private CoreSystem() { }


    public static void init(Platform platform) {
        CoreSystem.platform = platform;
    }
    
    
    /**
        Gets a Java system property. Returns null if the property does not exist or there is a 
        security excepion.
    */
    public static String getJavaProperty(String name) {
        try {
            return System.getProperty(name);
        }
        catch (SecurityException ex) {
            return null;
        }
    }


    /**
        Used internally by PulpCore - most apps will not need to access
        the Platform instance.
    */
    public static Platform getPlatform() {
        return platform;
    }
    
    
    /**
        Used internally by PulpCore - most apps will not need to access
        the AppContext instance.
    */
    public static AppContext getThisAppContext() {
        return platform.getThisAppContext();
    }


    /**
        Returns true if Java 1.3 or newer is in use.
    */
    public static final boolean isJava13orNewer() {
        return IS_JAVA_1_3;
    }


    /**
        Returns true if Java 1.4 or newer is in use.
    */
    public static final boolean isJava14orNewer() {
        return IS_JAVA_1_4;
    }


    /**
        Returns true if Java 1.5 or newer is in use.
    */
    public static final boolean isJava15orNewer() {
        return IS_JAVA_1_5;
    }


    /**
        Returns true if Java 1.6 or newer is in use.
    */
    public static final boolean isJava16orNewer() {
        return IS_JAVA_1_6;
    }
    
    
    /**
        Returns true if Java 1.7 or newer is in use.
    */
    public static final boolean isJava17orNewer() {
        return IS_JAVA_1_7;
    }
    
    
    /**
        Returns true if the current operating system is any version of Mac OS X.
    */
    public static final boolean isMacOSX() {
        return IS_MAC_OS_X;
    }
    
    /**
        Returns true if the current operating system is Mac OS X Leopard (10.5) or newer.
    */
    public static final boolean isMacOSXLeopardOrNewer() {
        return IS_MAC_OS_X_LEOPARD;
    }
    
    
    /**
        Returns true if the current operating system is any version of Windows.
    */
    public static final boolean isWindows() {
        return IS_WINDOWS;
    }
    
    
    /**
        Returns true if the current operating system is Windows XP (5.1) or newer.
    */
    public static final boolean isWindowsXPorNewer() {
        return IS_WINDOWS_XP;
    }


    //
    // Shorcut methods to AppContext
    //
    
    
    /**
        Gets a named property for this application instance. Returns null if the named parameter
        does not exist.
        <p>
        For Applets, the named properties are the applet parameters.
    */
    public static String getAppProperty(String name) {
        return getThisAppContext().getAppProperty(name);
    }
    

    /**
        Sets a new TalkBack field. If the named field already exists, it is replaced.
    */
    public static void setTalkBackField(String name, String value) {
        getThisAppContext().setTalkBackField(name, value);
    }


    public static void setTalkBackField(String name, Throwable t) {
        getThisAppContext().setTalkBackField(name, t);
    }


    public static void clearTalkBackFields() {
        getThisAppContext().clearTalkBackFields();
    }


    /**
        <code>
        Upload upload = Stage.sendTalkBackData();

        ...

        boolean sent = upload.isCompleted();
        </code>
        @return null if talkback is not enabled or there are no fields to send.
    */
    public static Upload sendTalkBackData() {
        return getThisAppContext().sendTalkBackData();
    }


    /**
        Determines if this app is running from one of the specified
        hosts.
    */
    public static boolean isValidHost(String[] validHosts) {
        return getThisAppContext().isValidHost(validHosts);
    }


    public static void setConsoleOutputEnabled(boolean consoleOut) {
        getThisAppContext().setConsoleOutputEnabled(consoleOut);
    }


    public static boolean isConsoleOutputEnabled() {
        return getThisAppContext().isConsoleOutputEnabled();
    }


    public static String getLogText() {
        return getThisAppContext().getLogText();
    }
    
    
    public static void clearLog() {
        getThisAppContext().clearLog();
    }
    
    
    /**
        Prints a the string representation of an object to the log.
    */
    public static void print(Object object) {
        if (object == null) {
            print("null");
        }
        else {
            print(object.toString());
        }
    }
    
    
    /**
        Prints the string representation of a boolean to the log.
    */
    public static void print(boolean b) {
        print(Boolean.toString(b));
    }
    
    
    /**
        Prints the string representation of an integer to the log.
    */
    public static void print(int n) {
        print(Integer.toString(n));
    }
    
    
    /**
        Prints the string representation of a float to the log.
    */
    public static void print(float n) {
        print(Float.toString(n));
    }
    
    
    /**
        Prints the string representation of a double to the log.
    */
    public static void print(double n) {
        print(Double.toString(n));
    }


    /**
        Prints a line of text to the log.
    */
    public static void print(String statement) {
        AppContext context = getThisAppContext();
        if (context != null) {
            context.print(statement);
        }
        else if (Build.DEBUG) {
            System.out.println(statement);
        }
    }


    /**
        Prints a line of text and a Throwable's stack trace to the log.
    */
    public static void print(String statement, Throwable t) {
        AppContext context = getThisAppContext();
        if (context != null) {
            context.print(statement, t);
        }
        else if (Build.DEBUG) {
            System.out.println(statement);
            t.printStackTrace();
        }
    }


    /**
        Prints the amount of current memory usage and the change in memory
        usage since the last call to this method. System.gc() is called
        before querying the amount of free memory.
    */
    public static void printMemory(String statement) {
        AppContext context = getThisAppContext();
        if (context != null) {
            context.printMemory(statement);
        }
        else if (Build.DEBUG) {
            System.out.println(statement);
        }
    }


    /**
        Attempts to store persistant user data to the local machine.
        <p>
        For applets, each key is stored in a Base64-encoded cookie. 
        The user's web browser must have 
        LiveConnect, JavaScript, and cookies enabled.
        Web browsers may have the following limitations for cookies 
        (according to RFC 2109 section 6.3):
        <ul>
        <li>300 cookies total</li>
        <li>20 cookies per domain (per site, not per page)</li>
        <li>4,096 bytes per cookie (name and value combined)</li>
        </ul>
        Additionally, Internet Explorer allows only 4,096 bytes per domain. 
        <p>
        Cookies may "expire" (become unaccessable)
        after an amount of time or may be deleted at any time by the browser.
        <p>
        Base64 encoding increases the data size by 33%.
        In summary, for Applets, try to use as few keys as possible, and keep
        the data length to a minimum.
        <p>
        Example: CoreSystem.putUserData("MyGame", data);
        @see #getUserData(String)
        @see #removeUserData(String)
    */
    public static void putUserData(String key, byte[] data) {
        getThisAppContext().putUserData(key, data);
    }
    
    
    /**
        Attempts to get persistant user data from the local machine.
        @see #putUserData(String, byte[])
        @see #removeUserData(String)
    */
    public static byte[] getUserData(String key) {
        return getThisAppContext().getUserData(key);
    }


    /**
        Attempts to remove persistant user data from the local machine.
        @see #putUserData(String, byte[])
        @see #getUserData(String)
    */
    public static void removeUserData(String key) {
        getThisAppContext().removeUserData(key);
    }


    public static URL getBaseURL() {
        return getThisAppContext().getBaseURL();
    }


    public static String getLocaleLanguage() {
        return getThisAppContext().getLocaleLanguage();
    }


    public static String getLocaleCountry() {
        return getThisAppContext().getLocaleCountry();
    }

    
    public static void showDocument(String url) {
        showDocument(url, "_top");
    }
    
    
    public static void showDocument(String url, String target) {
        getThisAppContext().showDocument(url, target);
    }
    
    
    //
    // Shorcut methods to Platform
    //


    /**
        Returns the current value of the system timer in milliseconds.
    */
    public static long getTimeMillis() {
        return platform.getTimeMillis();
    }


    /**
        Returns the current value of the system timer in microseconds.
    */
    public static long getTimeMicros() {
        return platform.getTimeMicros();
    }


    /**
        Checks if the platform has access to the native operating system
        clipboard. If not, an internal clipboard is used.
    */
    public static boolean isNativeClipboard() {
        return platform.isNativeClipboard();
    }


    /**
        Returns the text currently in the clipboard. Returns an empty string
        if there is no text in the clipboard. If the native clipboard is not accessible (typical
        in Applet environments), an application clipboard is used.
    */
    public static String getClipboardText() {
        return platform.getClipboardText();
    }


    /**
        Sets the text in the clipboard. If the native clipboard is not accessible (typical
        in Applet environments), an application clipboard is used.
    */
    public static void setClipboardText(String text) {
        platform.setClipboardText(text);
    }
    
    
    /**
        Returns true if this platform is hosted in a browser (Applets).
    */
    public static boolean isBrowserHosted() {
        return platform.isBrowserHosted();
    }
    
    
    /**
        Returns the name of the web browser, or null if the browser name could not be determined.
    */
    public static String getBrowserName() {
        return platform.getBrowserName();
    }

    
    /**
        Returns the version of the web browser, or null if the browser version
        could not be determined.
    */
    public static String getBrowserVersion() {
        return platform.getBrowserVersion();
    }
    
    
    //
    // Sound methods
    //
    
    
    /**
        Gets the audio mute setting for this application.
        @return true if the application is muted (silent), false otherwise
    */
    public static boolean isMute() {
        return getThisAppContext().isMute();
    }
    
    /**
        Sets the audio mute setting for this application. This setting takes effect immediately 
        for all calls to {@link pulpcore.sound.Sound#play()}.
        <p>
        Due to buffering, if any sounds are playing when this setting is changed, there may be a 
        slight delay before the new setting takes affect on those playing sounds. 
        @param mute true to mute the application, false otherwise
    */
    public static void setMute(boolean mute) {
        getThisAppContext().setMute(mute);
    }
    
    /**
        Gets the master sound volume mute setting for this application.
        @return the master sound volume, from 0 (silent) to 1 (full).
    */
    public static double getSoundVolume() {
        return getThisAppContext().getSoundVolume();
    }
    
    /**
        Sets the master sound volume mute setting for this application. This setting takes effect 
        immediately for all calls to {@link pulpcore.sound.Sound#play()}.
        <p>
        Due to buffering, if any sounds are playing when this setting is changed, there may be a 
        slight delay before the new setting takes affect on those playing sounds. 
        @param volume the master sound volume, from 0 (silent) to 1 (full).
    */
    public static void setSoundVolume(double volume) {
        getThisAppContext().setSoundVolume(volume);
    }
    
    
    /**
        Returns true if the user's system can play sound.
    */
    public static boolean isSoundEngineAvailable() {
        return (getPlatform().getSoundEngine() != null);
    }
    
    
    /**
        Gets the number of sounds currently playing in the sound engine.
    */
    public static int getNumSoundsPlaying() {
        // Don't create the sound engine if it's not already created
        if (!getPlatform().isSoundEngineCreated()) {
            return 0;
        }
        
        SoundEngine soundEngine = getPlatform().getSoundEngine();
        if (soundEngine == null) {
            return 0;
        }
        else {
            return soundEngine.getNumSoundsPlaying();
        }
    }
    
    
    /**
        Gets the maximum number of sounds that can be played simultaneously.
    */
    public static int getMaxSimultaneousSounds() {
        SoundEngine soundEngine = getPlatform().getSoundEngine();
        if (soundEngine == null) {
            return 0;
        }
        else {
            return soundEngine.getMaxSimultaneousSounds();
        }
    }
}