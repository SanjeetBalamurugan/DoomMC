package com.netherairtune.doommc.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class DoomScreen extends Screen {

    public DoomScreen() {
        super(Text.literal("DoomMC"));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        int boxWidth = 220;
        int boxHeight = 120;
        int x = (this.width - boxWidth) / 2;
        int y = (this.height - boxHeight) / 2;

        context.fill(x, y, x + boxWidth, y + boxHeight, 0xFF000000);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("DOOM will run here"), this.width / 2, y + 50, 0xFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }
}
