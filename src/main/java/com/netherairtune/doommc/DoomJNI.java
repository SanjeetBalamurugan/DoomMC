package com.netherairtune.doommc;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import com.netherairtune.doommc.DoomConfig;

public class DoomJNI {
    private static boolean loaded = false;

    static {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            String arch = System.getProperty("os.arch").toLowerCase();

            String platformDir = getPlatformDirectory(os, arch);
            String libName = System.mapLibraryName("doomjni");

            File externalLib = new File(new File("."), "doommc/" + platformDir + "/" + libName);

            if (externalLib.exists()) {
                System.load(externalLib.getAbsolutePath());
                loaded = true;
            } else {
                String resourcePath = "/native/" + platformDir + "/" + libName;
                InputStream libStream = DoomJNI.class.getResourceAsStream(resourcePath);

                if (libStream == null) {
                    throw new RuntimeException("Native library not found: " + resourcePath);
                }

                Path temp = Files.createTempFile("doomjni_" + platformDir + "_", libName);
                temp.toFile().deleteOnExit();
                Files.copy(libStream, temp, StandardCopyOption.REPLACE_EXISTING);
                libStream.close();

                System.load(temp.toAbsolutePath().toString());
                loaded = true;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Doom JNI library", e);
        }
    }

    private static boolean isAndroid() {
        try {
            DoomConfig.DoomConfigData cfg = DoomConfig.get();
            return cfg.isAndroid;
        } catch (Exception e) {
            return false;
        }
    }

    private static String getPlatformDirectory(String os, String arch) {
        if (isAndroid()) {
            if (arch.contains("aarch64") || arch.contains("arm64")) {
                return "android-arm64";
            }
            throw new UnsupportedOperationException("Unsupported Android arch: " + arch);
        }

        if (arch.contains("aarch64") || arch.contains("arm64")) {
        arch = "aarch64";
    } else if (arch.contains("amd64") || arch.contains("x86_64")) {
        arch = "x86_64";
    } else if (arch.contains("x86") || arch.contains("i386") || arch.contains("i686")) {
        arch = "x86";
    }

        if (os.contains("linux")) {
            return "linux-" + arch;
        }
        if (os.contains("mac")) {
            return "macos-" + arch;
        }
        if (os.contains("win")) {
            return "windows-" + arch;
        }

        throw new UnsupportedOperationException(os + " " + arch);
    }

    public static native void doomInit(String[] args);
    public static native void doomStep();
    public static native byte[] getFramebuffer();
    public static native short[] getAudioBuffer();
    public static native int getSampleRate();
    public static native int getWidth();
    public static native int getHeight();
    public static native boolean isPlayerReady();
    public static native boolean shouldExit();
    public static native void keyDown(int key);
    public static native void keyUp(int key);
    public static native void mouseMove(int x, int y);
    public static native void mouseButton(int button, boolean pressed);

    public static final int KEY_RIGHTARROW = 0xae;
    public static final int KEY_LEFTARROW = 0xac;
    public static final int KEY_UPARROW = 0xad;
    public static final int KEY_DOWNARROW = 0xaf;
    public static final int KEY_ESCAPE = 27;
    public static final int KEY_ENTER = 13;
    public static final int KEY_TAB = 9;
    public static final int KEY_F1 = 0x80 + 0x3b;
    public static final int KEY_F2 = 0x80 + 0x3c;
    public static final int KEY_F3 = 0x80 + 0x3d;
    public static final int KEY_F4 = 0x80 + 0x3e;
    public static final int KEY_F5 = 0x80 + 0x3f;
    public static final int KEY_F6 = 0x80 + 0x40;
    public static final int KEY_F7 = 0x80 + 0x41;
    public static final int KEY_F8 = 0x80 + 0x42;
    public static final int KEY_F9 = 0x80 + 0x43;
    public static final int KEY_F10 = 0x80 + 0x44;
    public static final int KEY_F11 = 0x80 + 0x57;
    public static final int KEY_F12 = 0x80 + 0x58;
    public static final int KEY_BACKSPACE = 127;
    public static final int KEY_PAUSE = 0xff;
    public static final int KEY_EQUALS = 0x3d;
    public static final int KEY_MINUS = 0x2d;
    public static final int KEY_RSHIFT = 0x80 + 0x36;
    public static final int KEY_RCTRL = 0x80 + 0x1d;
    public static final int KEY_RALT = 0x80 + 0x38;
    
    public static final int MOUSE_LEFT = 1;
    public static final int MOUSE_MIDDLE = 2;
    public static final int MOUSE_RIGHT = 4;

    public static boolean isLoaded() {
        return loaded;
    }
}