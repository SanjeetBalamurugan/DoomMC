package com.netherairtune.doommc;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class DoomJNI {
    private static boolean loaded = false;

    static {
        try {
        	//added android, fuk
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
            Class.forName("android.os.Build");
            return true;
        } catch (ClassNotFoundException e) {
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
    public static native int getWidth();
    public static native int getHeight();
    public static native boolean isPlayerReady();
    public static native void keyDown(int key);
    public static native void keyUp(int key);
    public static native void mouseMove(int x, int y);
    public static native void mouseButton(int button, boolean pressed);

    public static boolean isLoaded() {
        return loaded;
    }
}
