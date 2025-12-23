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
    private boolean wadMissing = false;

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

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Close"),
                b -> close()
        ).dimensions(
                this.width - 90,
                10,
                80,
                20
        ).build());
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        if (wadMissing || !initialized) {
            super.render(ctx, mx, my, delta);
            return;
        }

        DoomJNI.doomStep();

        byte[] fb = DoomJNI.getFramebuffer();
        for (int y = 0; y < doomHeight; y++) {
            for (int x = 0; x < doomWidth; x++) {
                int i = (y * doomWidth + x) * 4;
                int b = fb[i] & 0xFF;
                int g = fb[i + 1] & 0xFF;
                int r = fb[i + 2] & 0xFF;
                int a = fb[i + 3] & 0xFF;
                int abgr = (a << 24) | (b << 16) | (g << 8) | r;
                image.setColor(x, y, abgr);
            }
        }

        texture.upload();

        float aspect = (float) doomWidth / doomHeight;

        int h = (int) (height * 0.75f);
        int w = (int) (h * aspect);

        if (w > width) {
            w = width;
            h = (int) (w / aspect);
        }

        int x = (width - w) / 2;
        int y = (height - h) / 2;

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, textureId);
        ctx.drawTexture(textureId, x, y, 0, 0, w, h, doomWidth, doomHeight);
        
        super.render(ctx, mx, my, delta);
    }

    @Override
    public boolean keyPressed(int key, int sc, int mods) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }

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
        if (DoomConfig.get().isAndroid) {
            return;
        }
        
        if (firstMouse) {
            lastMouseX = (int) x;
            firstMouse = false;
            return;
        }

        int dx = (int) (x - lastMouseX);
        lastMouseX = (int) x;

        float sens = DoomConfig.get().mouseSensitivity;
        DoomJNI.mouseMove((int) (dx * sens), 0);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (DoomConfig.get().isAndroid) {
            return super.mouseClicked(x, y, button);
        }
        
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            DoomJNI.keyDown(DoomJNI.KEY_RCTRL);
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            DoomJNI.keyDown(' ');
            return true;
        }
        return super.mouseClicked(x, y, button);
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        if (DoomConfig.get().isAndroid) {
            return super.mouseReleased(x, y, button);
        }
        
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
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
            case GLFW.GLFW_KEY_W -> DoomJNI.KEY_UPARROW;
            case GLFW.GLFW_KEY_S -> DoomJNI.KEY_DOWNARROW;
            case GLFW.GLFW_KEY_A -> DoomJNI.KEY_LEFTARROW;
            case GLFW.GLFW_KEY_D -> DoomJNI.KEY_RIGHTARROW;
            
            case GLFW.GLFW_KEY_UP -> DoomJNI.KEY_UPARROW;
            case GLFW.GLFW_KEY_DOWN -> DoomJNI.KEY_DOWNARROW;
            case GLFW.GLFW_KEY_LEFT -> DoomJNI.KEY_LEFTARROW;
            case GLFW.GLFW_KEY_RIGHT -> DoomJNI.KEY_RIGHTARROW;
            
            case GLFW.GLFW_KEY_Q -> ',';
            case GLFW.GLFW_KEY_E -> '.';

            case GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT -> DoomJNI.KEY_RSHIFT;
            case GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL -> DoomJNI.KEY_RCTRL;
            case GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT -> DoomJNI.KEY_RALT;

            case GLFW.GLFW_KEY_SPACE -> ' ';
            case GLFW.GLFW_KEY_ENTER -> DoomJNI.KEY_ENTER;
            case GLFW.GLFW_KEY_TAB -> DoomJNI.KEY_TAB;

            case GLFW.GLFW_KEY_1 -> '1';
            case GLFW.GLFW_KEY_2 -> '2';
            case GLFW.GLFW_KEY_3 -> '3';
            case GLFW.GLFW_KEY_4 -> '4';
            case GLFW.GLFW_KEY_5 -> '5';
            case GLFW.GLFW_KEY_6 -> '6';
            case GLFW.GLFW_KEY_7 -> '7';

            default -> -1;
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

        firstMouse = true;

        if (texture != null) texture.close();
        if (image != null) image.close();

        super.close();
    }
}