package com.netherairtune.doommc.client;

import com.netherairtune.doommc.DoomJNI;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class DoomScreen extends Screen {

    private final int scale = 3;
    private final int doomWidth = DoomJNI.getWidth();
    private final int doomHeight = DoomJNI.getHeight();
    private final int[] framebuffer = new int[doomWidth * doomHeight];

    public DoomScreen() {
        super(Text.literal("DoomMC"));
    }

    @Override
    protected void init() {
        int boxWidth = (int) (this.width * 0.75);
        int boxHeight = boxWidth * 9 / 16;
        int x = (this.width - boxWidth) / 2;
        int y = (this.height - boxHeight) / 2;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("X"), b -> this.client.setScreen(null))
                .dimensions(x + boxWidth - 20, y + 5, 15, 15)
                .build());
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        DoomJNI.doomStep();
        int[] fb = DoomJNI.getFramebuffer();

        System.arraycopy(fb, 0, framebuffer, 0, framebuffer.length);

        int boxWidth = doomWidth * scale;
        int boxHeight = doomHeight * scale;
        int x = (this.width - boxWidth) / 2;
        int y = (this.height - boxHeight) / 2;

        context.fill(x, y, x + boxWidth, y + boxHeight, 0xFF000000);

        for (int dy = 0; dy < doomHeight; dy++) {
            for (int dx = 0; dx < doomWidth; dx++) {
                int color = framebuffer[dy * doomWidth + dx];
                for (int sy = 0; sy < scale; sy++) {
                    for (int sx = 0; sx < scale; sx++) {
                        context.drawPixel(x + dx * scale + sx, y + dy * scale + sy, color);
                    }
                }
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        DoomJNI.doomKeyDown(mapKey(keyCode));
        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        DoomJNI.doomKeyUp(mapKey(keyCode));
        return true;
    }

    @Override
    public boolean mouseMoved(double mouseX, double mouseY) {
        DoomJNI.doomMouseMove((int) mouseX, (int) mouseY);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        DoomJNI.doomMouseButton(button + 1, true);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        DoomJNI.doomMouseButton(button + 1, false);
        return true;
    }

    private int mapKey(int keyCode) {
        return switch (keyCode) {
            case GLFW.GLFW_KEY_W -> DoomJNI.DOOM_KEY_w;
            case GLFW.GLFW_KEY_A -> DoomJNI.DOOM_KEY_a;
            case GLFW.GLFW_KEY_S -> DoomJNI.DOOM_KEY_s;
            case GLFW.GLFW_KEY_D -> DoomJNI.DOOM_KEY_d;
            case GLFW.GLFW_KEY_SPACE -> DoomJNI.DOOM_KEY_SPACE;
            case GLFW.GLFW_KEY_UP -> DoomJNI.DOOM_KEY_UPARROW;
            case GLFW.GLFW_KEY_DOWN -> DoomJNI.DOOM_KEY_DOWNARROW;
            case GLFW.GLFW_KEY_LEFT -> DoomJNI.DOOM_KEY_LEFTARROW;
            case GLFW.GLFW_KEY_RIGHT -> DoomJNI.DOOM_KEY_RIGHTARROW;
            default -> keyCode;
        };
    }
}
