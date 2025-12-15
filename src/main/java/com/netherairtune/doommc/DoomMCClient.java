package com.netherairtune.doommc;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DoomMCClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("DoomMC");

    @Override
    public void onInitializeClient() {
        LOGGER.info("DoomMC client initializing");

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            client.execute(() -> {
                if (client.player != null) {
                    Text message = Text.literal("Thanks for installing ").append(Text.literal("DoomMC").styled(style -> style.withColor(TextColor.fromRgb(0xFF0000)))).append(Text.literal("! Have fun!").styled(style -> style.withColor(TextColor.fromRgb(0x00FF00))));
                    client.player.sendMessage(message, false);
                }
            });
        });
    }
}
