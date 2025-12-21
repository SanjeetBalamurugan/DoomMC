package com.netherairtune.doommc;

import com.google.gson.Gson;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;

public final class DoomConfig {

    private static DoomConfigData cached;

    // ok fuck it lemme just make the user to use that config to specify is he running mc in Android 
    public static DoomConfigData get(boolean isAndroid) {
        if (cached != null) {
            return cached;
        }

        try {
            MinecraftClient client = MinecraftClient.getInstance();
            File gameDir = client.runDirectory;
            File doomDir = new File(gameDir, "doommc");
            File configFile = new File(doomDir, "doomconfig.json");

            if (!configFile.exists()) {
                throw new RuntimeException(configFile.getAbsolutePath());
            }

            try (Reader reader = new FileReader(configFile)) {
                Gson gson = new Gson();
                cached = gson.fromJson(reader, DoomConfigData.class);
                cached.isAndroid = isAndroid;
                return cached;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static final class DoomConfigData {
        public boolean isAndroid;
    }
}
