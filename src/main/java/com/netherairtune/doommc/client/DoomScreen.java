package com.netherairtune.doommc.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.netherairtune.doommc.DoomJNI;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class DoomScreen extends Screen {

    private final NativeImage image;
    private final NativeImageBackedTexture texture;
    private final int doomWidth = DoomJNI.getWidth();
    private final int doomHeight = DoomJNI.getHeight();
    private final int scale = 2;

    public DoomScreen() {
        super(Text.literal("DoomMC"));
        image = new NativeImage(doomWidth, doomHeight, false);
        texture = new NativeImageBackedTexture(image);
    }

    @Override
    protected void init() {
        int boxWidth = doomWidth * scale;
        int boxHeight = doomHeight * scale;
        int x = (this.width - boxWidth) / 2;
        int y = (this.height - boxHeight) / 2;

        addDrawableChild(ButtonWidget.builder(Text.literal("X"), button -> this.client.setScreen(null))
                .dimensions(x + boxWidth - 20, y + 5, 15, 15)
                .build());
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        DoomJNI.doomStep();
        int[] framebuffer = DoomJNI.getFramebuffer();
        for (int y = 0; y < doomHeight; y++)
            for (int x = 0; x < doomWidth; x++)
                image.setPixelColor(x, y, framebuffer[y * doomWidth + x]);

        texture.upload();

        int boxWidth = doomWidth * scale;
        int boxHeight = doomHeight * scale;
        int x = (this.width - boxWidth) / 2;
        int y = (this.height - boxHeight) / 2;

        RenderSystem.enableTexture();
        drawTexture(matrices, texture.getTexture(), x, y, 0, 0, boxWidth, boxHeight, doomWidth, doomHeight);

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        DoomJNI.DOOM_KeyDown(mapKey(keyCode));
        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        DoomJNI.DOOM_KeyUp(mapKey(keyCode));
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        DoomJNI.DOOM_MouseButton(button + 1, true);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        DoomJNI.DOOM_MouseButton(button + 1, false);
        return true;
    }

    @Override
    public void onMouseMove(double mouseX, double mouseY) {
        DoomJNI.DOOM_MouseMove((int) mouseX, (int) mouseY);
    }

    private int mapKey(int key) {
        return switch (key) {
            case GLFW.GLFW_KEY_W -> DoomJNI.DOOM_KEY_w;
            case GLFW.GLFW_KEY_A -> DoomJNI.DOOM_KEY_a;
            case GLFW.GLFW_KEY_S -> DoomJNI.DOOM_KEY_s;
            case GLFW.GLFW_KEY_D -> DoomJNI.DOOM_KEY_d;
            case GLFW.GLFW_KEY_SPACE -> DoomJNI.DOOM_KEY_SPACE;
            case GLFW.GLFW_KEY_UP -> DoomJNI.DOOM_KEY_UPARROW;
            case GLFW.GLFW_KEY_DOWN -> DoomJNI.DOOM_KEY_DOWNARROW;
            case GLFW.GLFW_KEY_LEFT -> DoomJNI.DOOM_KEY_LEFTARROW;
            case GLFW.GLFW_KEY_RIGHT -> DoomJNI.DOOM_KEY_RIGHTARROW;
            default -> key;
        };
    }
}
