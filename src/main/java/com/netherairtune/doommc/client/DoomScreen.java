package com.netherairtune.doommc.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class DoomScreen extends Screen {

    public DoomScreen() {
        super(Text.literal("DoomMC"));
    }

    @Override
protected void init() {
    int boxWidth = (int) (this.width * 0.75);
    int boxHeight = boxWidth * 9 / 16;
    int x = (this.width - boxWidth) / 2;
    int y = (this.height - boxHeight) / 2;

    this.addDrawableChild(ButtonWidget.builder(Text.literal("X"), button -> this.client.setScreen(null))
            .dimensions(x + boxWidth - 20, y + 5, 15, 15)
            .build());
}


    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int boxWidth = (int) (this.width * 0.75);
        int boxHeight = boxWidth * 9 / 16;
        int x = (this.width - boxWidth) / 2;
        int y = (this.height - boxHeight) / 2;

        context.fill(x, y, x + boxWidth, y + boxHeight, 0xFF000000);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("DOOM will run here"), this.width / 2, y + boxHeight / 2 - 4, 0xFFFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }
}
