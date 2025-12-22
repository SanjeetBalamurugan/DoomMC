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

import java.io.File;

public class DoomScreen extends Screen {

    private NativeImage image;
    private NativeImageBackedTexture texture;
    private Identifier textureId;

    private final int doomWidth = 320;
    private final int doomHeight = 200;

    private boolean initialized = false;
    private boolean playerReady = false;
    private boolean wadMissing = false;
    private int waitTicks = 0;

    private int lastMouseX;
    private boolean firstMouse = true;

    public DoomScreen() {
        super(Text.literal("DoomMC"));
    }

    @Override
    protected void init() {
        super.init();

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
                "-warp", "1", "1",
                "-skill", "3"
            });
            initialized = true;
        }

        image = new NativeImage(doomWidth, doomHeight, false);
        texture = new NativeImageBackedTexture(image);
        textureId = client.getTextureManager().registerDynamicTexture("doom", texture);

        texture.bindTexture();
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        if (!DoomConfig.get().isAndroid) {
            GLFW.glfwSetInputMode(
                client.getWindow().getHandle(),
                GLFW.GLFW_CURSOR,
                GLFW.GLFW_CURSOR_DISABLED
            );
        }
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        if (wadMissing || !initialized) return;

        DoomJNI.doomStep();

        byte[] fb = DoomJNI.getFramebuffer();
        for (int y = 0; y < doomHeight; y++) {
            for (int x = 0; x < doomWidth; x++) {
                int i = (y * doomWidth + x) * 4;
                int argb =
                    (fb[i + 3] & 0xFF) << 24 |
                    (fb[i + 2] & 0xFF) << 16 |
                    (fb[i + 1] & 0xFF) << 8 |
                    (fb[i] & 0xFF);
                image.setColor(x, y, argb);
            }
        }

        texture.upload();

        int h = (int)(height * 0.65f);
        int w = h * 16 / 9;
        int x = (width - w) / 2;
        int y = (height - h) / 2;

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, textureId);
        ctx.drawTexture(textureId, x, y, 0, 0, w, h, doomWidth, doomHeight);
    }

    @Override
    public boolean keyPressed(int key, int sc, int mods) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        DoomJNI.keyDown(mapKey(key));
        return true;
    }

    @Override
    public boolean keyReleased(int key, int sc, int mods) {
        DoomJNI.keyUp(mapKey(key));
        return true;
    }

    @Override
    public void mouseMoved(double x, double y) {
        if (firstMouse) {
            lastMouseX = (int)x;
            firstMouse = false;
            return;
        }

        int dx = (int)x - lastMouseX;
        lastMouseX = (int)x;

        DoomJNI.mouseMove(dx, 0);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (button == 0) DoomJNI.mouseButton(DoomJNI.MOUSE_LEFT, true);
        return true;
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        if (button == 0) DoomJNI.mouseButton(DoomJNI.MOUSE_LEFT, false);
        return true;
    }

    private int mapKey(int k) {
        return switch (k) {
            case GLFW.GLFW_KEY_W -> DoomJNI.KEY_UPARROW;
            case GLFW.GLFW_KEY_S -> DoomJNI.KEY_DOWNARROW;
            case GLFW.GLFW_KEY_A -> DoomJNI.KEY_LEFTARROW;
            case GLFW.GLFW_KEY_D -> DoomJNI.KEY_RIGHTARROW;
            case GLFW.GLFW_KEY_SPACE -> ' ';
            case GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL -> DoomJNI.KEY_RCTRL;
            case GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT -> DoomJNI.KEY_RALT;
            case GLFW.GLFW_KEY_ENTER -> DoomJNI.KEY_ENTER;
            default -> k;
        };
    }

    @Override
    public void close() {
        if (!DoomConfig.get().isAndroid) {
            GLFW.glfwSetInputMode(
                client.getWindow().getHandle(),
                GLFW.GLFW_CURSOR,
                GLFW.GLFW_CURSOR_NORMAL
            );
        }

        if (texture != null) texture.close();
        if (image != null) image.close();

        super.close();
    }
}
