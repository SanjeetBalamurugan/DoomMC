package com.netherairtune.doommc;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class DoomJNI {

    static {
        try {
            String libName = System.mapLibraryName("doomjni");
            InputStream libStream = DoomJNI.class.getResourceAsStream("/native/" + libName);
            if (libStream == null) throw new RuntimeException("JNI library not found in resources");
            Path temp = Files.createTempFile("doomjni", libName);
            Files.copy(libStream, temp, StandardCopyOption.REPLACE_EXISTING);
            System.load(temp.toAbsolutePath().toString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Doom JNI library", e);
        }
    }

    public static native void doomInit(String[] args);
    public static native void doomStep();
    public static native int[] getFramebuffer();
    public static native int getWidth();
    public static native int getHeight();
    public static native void doomKeyDown(int key);
    public static native void doomKeyUp(int key);
    public static native void doomMouseMove(int x, int y);
    public static native void doomMouseButton(int button, boolean pressed);

    public static final int DOOM_KEY_RIGHTARROW = 0;
    public static final int DOOM_KEY_LEFTARROW = 1;
    public static final int DOOM_KEY_UPARROW = 2;
    public static final int DOOM_KEY_DOWNARROW = 3;
    public static final int DOOM_KEY_SPACE = ' ';
    public static final int DOOM_KEY_w = 'w';
    public static final int DOOM_KEY_a = 'a';
    public static final int DOOM_KEY_s = 's';
    public static final int DOOM_KEY_d = 'd';
}
