package com.netherairtune.doommc.client;

import com.netherairtune.doommc.DoomJNI;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class DoomScreen extends Screen {

    private final int doomWidth = DoomJNI.getWidth();
    private final int doomHeight = DoomJNI.getHeight();
    private final int[] framebuffer = new int[doomWidth * doomHeight];
    private NativeImageBackedTexture texture;

    public DoomScreen() {
        super(Text.literal("DoomMC"));
        texture = new NativeImageBackedTexture(doomWidth, doomHeight, false);
    }

    @Override
    protected void init() {
        int boxWidth = (int) (this.width * 0.75);
        int boxHeight = boxWidth * 9 / 16;
        int x = (this.width - boxWidth) / 2;
        int y = (this.height - boxHeight) / 2;

        addDrawableChild(ButtonWidget.builder(Text.literal("X"), b -> this.client.setScreen(null))
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
        System.arraycopy(DoomJNI.getFramebuffer(), 0, framebuffer, 0, framebuffer.length);

        NativeImage img = texture.getImage();
        for (int y = 0; y < doomHeight; y++) {
            for (int x = 0; x < doomWidth; x++) {
                img.setPixelRGBA(x, y, framebuffer[y * doomWidth + x]);
            }
        }
        texture.upload();

        int boxWidth = (int) (this.width * 0.75);
        int boxHeight = boxWidth * 9 / 16;
        int x = (this.width - boxWidth) / 2;
        int y = (this.height - boxHeight) / 2;

        context.drawTexture(texture.getTextureId(), x, y, boxWidth, boxHeight, 0, 0, doomWidth, doomHeight, doomWidth, doomHeight);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        DoomJNI.keyDown(mapMinecraftKeyToDoom(keyCode));
        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        DoomJNI.keyUp(mapMinecraftKeyToDoom(keyCode));
        return true;
    }

    private int mapMinecraftKeyToDoom(int keyCode) {
        switch (keyCode) {
            case GLFW.GLFW_KEY_W: return DoomJNI.DOOM_KEY_w;
            case GLFW.GLFW_KEY_A: return DoomJNI.DOOM_KEY_a;
            case GLFW.GLFW_KEY_S: return DoomJNI.DOOM_KEY_s;
            case GLFW.GLFW_KEY_D: return DoomJNI.DOOM_KEY_d;
            case GLFW.GLFW_KEY_SPACE: return DoomJNI.DOOM_KEY_SPACE;
            case GLFW.GLFW_KEY_UP: return DoomJNI.DOOM_KEY_UPARROW;
            case GLFW.GLFW_KEY_DOWN: return DoomJNI.DOOM_KEY_DOWNARROW;
            case GLFW.GLFW_KEY_LEFT: return DoomJNI.DOOM_KEY_LEFTARROW;
            case GLFW.GLFW_KEY_RIGHT: return DoomJNI.DOOM_KEY_RIGHTARROW;
            default: return keyCode;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        DoomJNI.mouseButton(button + 1, true);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        DoomJNI.mouseButton(button + 1, false);
        return true;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        DoomJNI.mouseMove((int) mouseX, (int) mouseY);
    }
}
