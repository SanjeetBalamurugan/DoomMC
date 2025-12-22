package com.netherairtune.doommc;

import com.google.gson.Gson;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;

public final class DoomConfig {

    private static DoomConfigData cached;

    public static DoomConfigData get() {
        if (cached != null) return cached;

        try {
            MinecraftClient client = MinecraftClient.getInstance();
            File gameDir = client.runDirectory;
            File doomDir = new File(gameDir, "doommc");
            File configFile = new File(doomDir, "doomconfig.json");

            if (!configFile.exists()) throw new RuntimeException(configFile.getAbsolutePath());

            try (Reader reader = new FileReader(configFile)) {
                Gson gson = new Gson();
                cached = gson.fromJson(reader, DoomConfigData.class);
                if (cached.mouseSensitivity <= 0) {
                    cached.mouseSensitivity = 1.0f;
                }
                return cached;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static final class DoomConfigData {
        public boolean isAndroid;
        public float mouseSensitivity = 1.0f;
    }
}