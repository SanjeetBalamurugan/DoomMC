package com.netherairtune.doommc;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

@Environment(EnvType.CLIENT)
public class DoomMC implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            Text message = Text.literal("Thanks for installing ")
                    .append(Text.literal("DoomMC").styled(style -> style.withColor(TextColor.fromRgb(0xFF0000)))) // Red
                    .append(Text.literal("! Have fun!").styled(style -> style.withColor(TextColor.fromRgb(0x00FF00)))); // Green

            client.player.sendMessage(message, false);
        }
    }
}
