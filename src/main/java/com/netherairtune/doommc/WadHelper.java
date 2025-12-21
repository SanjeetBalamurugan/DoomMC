package com.netherairtune.doommc;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;

import java.io.File;

public class WadHelper {
    
    private static final String WAD_FILENAME = "doom.wad";
    
    public static File getWadFile() {
        MinecraftClient client = MinecraftClient.getInstance();
        File gameDir = client.runDirectory;
        File doomDir = new File(gameDir, "doommc");
        
        if (!doomDir.exists()) {
            doomDir.mkdirs();
        }
        
        return new File(doomDir, WAD_FILENAME);
    }
    
    public static File getDoomDir() {
        MinecraftClient client = MinecraftClient.getInstance();
        File gameDir = client.runDirectory;
        File doomDir = new File(gameDir, "doommc");
        
        if (!doomDir.exists()) {
            doomDir.mkdirs();
        }
        
        return doomDir;
    }
    
    public static boolean wadExists() {
        File wadFile = getWadFile();
        return wadFile.exists() && wadFile.length() > 0;
    }
    
    public static void openDoomFolder() {
        File doomDir = getDoomDir();
        Util.getOperatingSystem().open(doomDir);
    }
}