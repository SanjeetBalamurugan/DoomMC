package com.netherairtune.doommc;

import com.netherairtune.doommc.registry.ModBlocks;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DoomMC implements ModInitializer {

    public static final String MOD_ID = "doommc";
    public static final Logger LOGGER = LoggerFactory.getLogger("DoomMC");

    @Override
    public void onInitialize() {
        LOGGER.info("DoomMC initializing");
        ModBlocks.init();
    }
}
