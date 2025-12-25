package com.netherairtune.doommc.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.netherairtune.doommc.DoomConfig;
import com.netherairtune.doommc.DoomJNI;
import com.netherairtune.doommc.WadHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.sound.sampled.*;
import java.io.File;

public class DoomScreen extends Screen {

    private NativeImage image;
    private NativeImageBackedTexture texture;
    private Identifier textureId;

    private int doomWidth = 320;
    private int doomHeight = 200;

    private boolean initialized = false;
    private boolean wadMissing = false;

    private int lastMouseX;
    private boolean firstMouse = true;

    private int windowWidth;
    private int windowHeight;
    private int windowX;
    private int windowY;
    private boolean dragging = false;
    private int dragOffsetX;
    private int dragOffsetY;

    private static final int TITLE_BAR_HEIGHT = 24;
    private static final int WINDOW_BORDER = 2;

    private SourceDataLine audioLine;

    public DoomScreen() {
        super(Text.literal("Doom"));
    }

    @Override
    protected void init() {
        if (!WadHelper.wadExists()) {
            wadMissing = true;

            int w = 200;
            int x = this.width / 2 - w / 2;
            int y = this.height / 2;

            addDrawableChild(ButtonWidget.builder(
                    Text.literal("Open DoomMC Folder"),
                    b -> WadHelper.openDoomFolder()
            ).dimensions(x, y, w, 20).build());

            addDrawableChild(ButtonWidget.builder(
                    Text.literal("Close"),
                    b -> this.client.setScreen(null)
            ).dimensions(x, y + 25, w, 20).build());

            return;
        }

        if (!initialized) {
            File wad = WadHelper.getWadFile();
            DoomJNI.doomInit(new String[]{
                    "doomjni",
                    "-iwad", wad.getAbsolutePath(),
                    "-skill", "3"
            });
            initialized = true;
        }

        windowWidth = this.width - 40;
        windowHeight = this.height - 40;
        
        windowX = (this.width - windowWidth) / 2;
        windowY = (this.height - windowHeight) / 2;

        image = new NativeImage(doomWidth, doomHeight, false);
        texture = new NativeImageBackedTexture(image);
        textureId = client.getTextureManager().registerDynamicTexture("doom", texture);

        texture.bindTexture();
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        GLFW.glfwSetInputMode(
                client.getWindow().getHandle(),
                GLFW.GLFW_CURSOR,
                GLFW.GLFW_CURSOR_DISABLED
        );

        try {
            int sampleRate = DoomJNI.getSampleRate();
            AudioFormat format = new AudioFormat(sampleRate, 16, 2, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            audioLine = (SourceDataLine) AudioSystem.getLine(info);
            audioLine.open(format, 4096);
            audioLine.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        renderBackground(ctx, mx, my, delta);

        if (wadMissing || !initialized) {
            super.render(ctx, mx, my, delta);
            return;
        }

        if (DoomJNI.shouldExit()) {
            close();
            return;
        }

        DoomJNI.doomStep();

        short[] audioData = DoomJNI.getAudioBuffer();
        if (audioData != null && audioLine != null) {
            byte[] audioBytes = new byte[audioData.length * 2];
            for (int i = 0; i < audioData.length; i++) {
                audioBytes[i * 2] = (byte) (audioData[i] & 0xFF);
                audioBytes[i * 2 + 1] = (byte) ((audioData[i] >> 8) & 0xFF);
            }
            audioLine.write(audioBytes, 0, audioBytes.length);
        }

        byte[] fb = DoomJNI.getFramebuffer();
        if (fb.length >= doomWidth * doomHeight * 4) {
            for (int y = 0; y < doomHeight; y++) {
                for (int x = 0; x < doomWidth; x++) {
                    int i = (y * doomWidth + x) * 4;
                    int b = fb[i] & 0xFF;
                    int g = fb[i + 1] & 0xFF;
                    int r = fb[i + 2] & 0xFF;
                    int a = fb[i + 3] & 0xFF;
                    image.setColor(x, y, (a << 24) | (b << 16) | (g << 8) | r);
                }
            }
        }

        texture.upload();

        ctx.fill(windowX - WINDOW_BORDER, windowY - TITLE_BAR_HEIGHT - WINDOW_BORDER, 
                 windowX + windowWidth + WINDOW_BORDER, windowY + windowHeight + WINDOW_BORDER, 
                 0xFF3C3C3C);

        ctx.fill(windowX, windowY - TITLE_BAR_HEIGHT, 
                 windowX + windowWidth, windowY, 
                 0xFF1E1E1E);
        
        String title = "Doom (Shareware) - " + doomWidth + "x" + doomHeight;
        ctx.drawText(client.textRenderer, title, 
                     windowX + 8, windowY - TITLE_BAR_HEIGHT + 8, 
                     0xFFFFFF, true);

        int buttonSize = 16;
        int buttonY = windowY - TITLE_BAR_HEIGHT + 4;
        int closeX = windowX + windowWidth - buttonSize - 4;
        int maxX = closeX - buttonSize - 4;
        int minX = maxX - buttonSize - 4;

        ctx.fill(closeX, buttonY, closeX + buttonSize, buttonY + buttonSize, 0xFFE81123);
        ctx.drawText(client.textRenderer, "×", closeX + 4, buttonY + 4, 0xFFFFFF, false);

        ctx.fill(maxX, buttonY, maxX + buttonSize, buttonY + buttonSize, 0xFF666666);
        ctx.drawText(client.textRenderer, "□", maxX + 4, buttonY + 4, 0xFFFFFF, false);

        ctx.fill(minX, buttonY, minX + buttonSize, buttonY + buttonSize, 0xFF666666);
        ctx.drawText(client.textRenderer, "—", minX + 4, buttonY + 4, 0xFFFFFF, false);

        ctx.fill(windowX, windowY, windowX + windowWidth, windowY + windowHeight, 0xFF000000);

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, textureId);
        ctx.drawTexture(textureId, windowX, windowY, 0, 0, 
                       windowWidth, windowHeight, doomWidth, doomHeight);

        super.render(ctx, mx, my, delta);
    }

    @Override
    public boolean keyPressed(int key, int sc, int mods) {
        int doomKey = mapKey(key);
        if (doomKey != -1) {
            DoomJNI.keyDown(doomKey);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyReleased(int key, int sc, int mods) {
        int doomKey = mapKey(key);
        if (doomKey != -1) {
            DoomJNI.keyUp(doomKey);
            return true;
        }
        return false;
    }

    @Override
    public void mouseMoved(double x, double y) {
        if (dragging) {
            windowX = (int) x - dragOffsetX;
            windowY = (int) y - dragOffsetY + TITLE_BAR_HEIGHT;
            return;
        }

        if (firstMouse) {
            lastMouseX = (int) x;
            firstMouse = false;
            return;
        }

        int dx = (int) (x - lastMouseX);
        lastMouseX = (int) x;

        DoomJNI.mouseMove((int) (dx * DoomConfig.get().mouseSensitivity), 0);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT &&
            x >= windowX && x <= windowX + windowWidth &&
            y >= windowY - TITLE_BAR_HEIGHT && y < windowY) {
            
            int buttonSize = 16;
            int buttonY = windowY - TITLE_BAR_HEIGHT + 4;
            int closeX = windowX + windowWidth - buttonSize - 4;
            
            if (x >= closeX && x <= closeX + buttonSize &&
                y >= buttonY && y <= buttonY + buttonSize) {
                close();
                return true;
            }
            
            dragging = true;
            dragOffsetX = (int) x - windowX;
            dragOffsetY = (int) y - windowY + TITLE_BAR_HEIGHT;
            return true;
        }

        if (x >= windowX && x <= windowX + windowWidth &&
            y >= windowY && y <= windowY + windowHeight) {
            
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                DoomJNI.keyDown(DoomJNI.KEY_RCTRL);
                return true;
            }
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                DoomJNI.keyDown(' ');
                return true;
            }
        }
        
        return super.mouseClicked(x, y, button);
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            dragging = false;
            DoomJNI.keyUp(DoomJNI.KEY_RCTRL);
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            DoomJNI.keyUp(' ');
            return true;
        }
        return super.mouseReleased(x, y, button);
    }

    private int mapKey(int k) {
        return switch (k) {
            case GLFW.GLFW_KEY_ESCAPE -> DoomJNI.KEY_ESCAPE;
            case GLFW.GLFW_KEY_W, GLFW.GLFW_KEY_UP -> DoomJNI.KEY_UPARROW;
            case GLFW.GLFW_KEY_S, GLFW.GLFW_KEY_DOWN -> DoomJNI.KEY_DOWNARROW;
            case GLFW.GLFW_KEY_A, GLFW.GLFW_KEY_LEFT -> DoomJNI.KEY_LEFTARROW;
            case GLFW.GLFW_KEY_D, GLFW.GLFW_KEY_RIGHT -> DoomJNI.KEY_RIGHTARROW;
            case GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT -> DoomJNI.KEY_RSHIFT;
            case GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL -> DoomJNI.KEY_RCTRL;
            case GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT -> DoomJNI.KEY_RALT;
            case GLFW.GLFW_KEY_SPACE -> ' ';
            case GLFW.GLFW_KEY_ENTER -> DoomJNI.KEY_ENTER;
            case GLFW.GLFW_KEY_TAB -> DoomJNI.KEY_TAB;
            case GLFW.GLFW_KEY_BACKSPACE -> DoomJNI.KEY_BACKSPACE;
            case GLFW.GLFW_KEY_PAUSE -> DoomJNI.KEY_PAUSE;
            case GLFW.GLFW_KEY_EQUAL -> DoomJNI.KEY_EQUALS;
            case GLFW.GLFW_KEY_MINUS -> DoomJNI.KEY_MINUS;
            case GLFW.GLFW_KEY_Q -> ',';
            case GLFW.GLFW_KEY_E -> '.';
            case GLFW.GLFW_KEY_F1 -> DoomJNI.KEY_F1;
            case GLFW.GLFW_KEY_F2 -> DoomJNI.KEY_F2;
            case GLFW.GLFW_KEY_F3 -> DoomJNI.KEY_F3;
            case GLFW.GLFW_KEY_F4 -> DoomJNI.KEY_F4;
            case GLFW.GLFW_KEY_F5 -> DoomJNI.KEY_F5;
            case GLFW.GLFW_KEY_F6 -> DoomJNI.KEY_F6;
            case GLFW.GLFW_KEY_F7 -> DoomJNI.KEY_F7;
            case GLFW.GLFW_KEY_F8 -> DoomJNI.KEY_F8;
            case GLFW.GLFW_KEY_F9 -> DoomJNI.KEY_F9;
            case GLFW.GLFW_KEY_F10 -> DoomJNI.KEY_F10;
            case GLFW.GLFW_KEY_F11 -> DoomJNI.KEY_F11;
            case GLFW.GLFW_KEY_F12 -> DoomJNI.KEY_F12;
            case GLFW.GLFW_KEY_1 -> '1';
            case GLFW.GLFW_KEY_2 -> '2';
            case GLFW.GLFW_KEY_3 -> '3';
            case GLFW.GLFW_KEY_4 -> '4';
            case GLFW.GLFW_KEY_5 -> '5';
            case GLFW.GLFW_KEY_6 -> '6';
            case GLFW.GLFW_KEY_7 -> '7';
            case GLFW.GLFW_KEY_8 -> '8';
            case GLFW.GLFW_KEY_9 -> '9';
            case GLFW.GLFW_KEY_0 -> '0';
            case GLFW.GLFW_KEY_R -> 'r';
            case GLFW.GLFW_KEY_T -> 't';
            case GLFW.GLFW_KEY_Y -> 'y';
            case GLFW.GLFW_KEY_U -> 'u';
            case GLFW.GLFW_KEY_I -> 'i';
            case GLFW.GLFW_KEY_O -> 'o';
            case GLFW.GLFW_KEY_P -> 'p';
            case GLFW.GLFW_KEY_F -> 'f';
            case GLFW.GLFW_KEY_G -> 'g';
            case GLFW.GLFW_KEY_H -> 'h';
            case GLFW.GLFW_KEY_J -> 'j';
            case GLFW.GLFW_KEY_K -> 'k';
            case GLFW.GLFW_KEY_L -> 'l';
            case GLFW.GLFW_KEY_Z -> 'z';
            case GLFW.GLFW_KEY_X -> 'x';
            case GLFW.GLFW_KEY_C -> 'c';
            case GLFW.GLFW_KEY_V -> 'v';
            case GLFW.GLFW_KEY_B -> 'b';
            case GLFW.GLFW_KEY_N -> 'n';
            case GLFW.GLFW_KEY_M -> 'm';
            default -> -1;
        };
    }

    @Override
    public void close() {
        GLFW.glfwSetInputMode(
                client.getWindow().getHandle(),
                GLFW.GLFW_CURSOR,
                GLFW.GLFW_CURSOR_NORMAL
        );

        firstMouse = true;

        if (audioLine != null) {
            audioLine.stop();
            audioLine.close();
        }

        if (texture != null) texture.close();
        if (image != null) image.close();

        super.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.client != null && this.client.world != null) {
            context.fillGradient(0, 0, this.width, this.height, 0x00000000, 0x00000000);
        } else {
            super.renderBackground(context, mouseX, mouseY, delta);
        }
    }
}