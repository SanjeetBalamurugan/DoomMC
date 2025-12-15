package com.netherairtune.doommc.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class DoomScreen extends Screen {

    private int panelWidth;
    private int panelHeight;
    private int panelX;
    private int panelY;

    public DoomScreen() {
        super(Text.literal("DoomMC"));
    }

    @Override
    protected void init() {
        panelWidth = (int)(this.width * 0.7);
        panelHeight = (int)(this.height * 0.7);

        panelX = (this.width - panelWidth) / 2;
        panelY = (this.height - panelHeight) / 2;

        this.addDrawableChild(
            ButtonWidget.builder(Text.literal("X"), button -> this.close())
                .dimensions(panelX + panelWidth - 22, panelY + 6, 16, 16)
                .build()
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);

        context.fill(
            panelX,
            panelY,
            panelX + panelWidth,
            panelY + panelHeight,
            0xFF000000
        );

        context.drawCenteredTextWithShadow(
            this.textRenderer,
            "Doom will run here",
            this.width / 2,
            this.height / 2,
            0xAAFF0000
        );

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
