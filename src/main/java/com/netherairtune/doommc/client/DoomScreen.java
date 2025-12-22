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
    private int waitTicks = 0;
    private boolean playerReady = false;
    private boolean initialized = false;
    private boolean wadMissing = false;
    private int lastMouseX = 0;
    private int lastMouseY = 0;
    private boolean firstMouse = true;

    public DoomScreen() {
        super(Text.literal("DoomMC"));
    }

    @Override
    protected void init() {
        super.init();
        
        if (!WadHelper.wadExists()) {
            wadMissing = true;
            
            int buttonWidth = 200;
            int buttonHeight = 20;
            int centerX = this.width / 2 - buttonWidth / 2;
            int centerY = this.height / 2;
            
            this.addDrawableChild(
                ButtonWidget.builder(Text.literal("Open DoomMC Folder"), button -> {
                    WadHelper.openDoomFolder();
                })
                .dimensions(centerX, centerY, buttonWidth, buttonHeight)
                .build()
            );
            
            this.addDrawableChild(
                ButtonWidget.builder(Text.literal("Close"), button -> {
                    if (this.client != null) {
                        this.client.setScreen(null);
                    }
                })
                .dimensions(centerX, centerY + 25, buttonWidth, buttonHeight)
                .build()
            );
            
            return;
        }
        
        if (!initialized) {
            try {
                File wadFile = WadHelper.getWadFile();
                String[] args = {
                    "doomjni",
                    "-iwad", wadFile.getAbsolutePath(),
                    "-warp", "1", "1",
                    "-skill", "3"
                };
                DoomJNI.doomInit(args);
                initialized = true;
            } catch (Exception e) {
                e.printStackTrace();
                if (this.client != null) {
                    this.client.setScreen(null);
                }
                return;
            }
        }
        
        if (image == null) {
            image = new NativeImage(doomWidth, doomHeight, false);
            texture = new NativeImageBackedTexture(image);
            texture.bindTexture();
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            textureId = this.client.getTextureManager().registerDynamicTexture("doom_screen", texture);
        }

        int screenHeight = (int)(this.height * 0.65);
        int displayWidth = (int)(screenHeight * 16.0 / 9.0);
        int displayHeight = screenHeight;
        int x = (this.width - displayWidth) / 2;
        int y = (this.height - displayHeight) / 2;

        this.addDrawableChild(
            ButtonWidget.builder(Text.literal("Close"), button -> {
                if (this.client != null) {
                    this.client.setScreen(null);
                }
            })
            .dimensions(x + displayWidth - 60, y + 5, 55, 20)
            .build()
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (wadMissing) {
            this.renderBackground(context, mouseX, mouseY, delta);
            
            context.drawTextWithShadow(
                this.textRenderer,
                Text.literal("DOOM WAD file not found!"),
                this.width / 2 - this.textRenderer.getWidth("DOOM WAD file not found!") / 2,
                this.height / 2 - 40,
                0xFF5555
            );
            
            context.drawTextWithShadow(
                this.textRenderer,
                Text.literal("Place doom.wad in the DoomMC folder"),
                this.width / 2 - this.textRenderer.getWidth("Place doom.wad in the DoomMC folder") / 2,
                this.height / 2 - 25,
                0xAAAAAA
            );
            
            super.render(context, mouseX, mouseY, delta);
            return;
        }
        
        if (!initialized) {
            return;
        }
        
        if (!playerReady) {
            if (waitTicks < 35) {
                DoomJNI.doomStep();
                waitTicks++;
                playerReady = DoomJNI.isPlayerReady();
            } else {
                playerReady = true;
            }
        } else {
            DoomJNI.doomStep();
        }
        
        byte[] framebuffer = DoomJNI.getFramebuffer();
        for (int py = 0; py < doomHeight; py++) {
            for (int px = 0; px < doomWidth; px++) {
                int idx = (py * doomWidth + px) * 4;
                int r = framebuffer[idx] & 0xFF;
                int g = framebuffer[idx + 1] & 0xFF;
                int b = framebuffer[idx + 2] & 0xFF;
                int a = framebuffer[idx + 3] & 0xFF;
                int argb = (a << 24) | (b << 16) | (g << 8) | r;
                image.setColor(px, py, argb);
            }
        }
        
        texture.upload();

        int screenHeight = (int)(this.height * 0.65);
        int displayWidth = (int)(screenHeight * 16.0 / 9.0);
        int displayHeight = screenHeight;
        int x = (this.width - displayWidth) / 2;
        int y = (this.height - displayHeight) / 2;

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, textureId);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        texture.bindTexture();
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        
        context.drawTexture(textureId, x, y, 0, 0, displayWidth, displayHeight, doomWidth, doomHeight);
        
        RenderSystem.disableBlend();

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (wadMissing) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (this.client != null) {
                this.client.setScreen(null);
            }
            return true;
        }
        DoomJNI.keyDown(mapKey(keyCode));
        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (wadMissing) {
            return super.keyReleased(keyCode, scanCode, modifiers);
        }
        
        DoomJNI.keyUp(mapKey(keyCode));
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (wadMissing) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        
        if (button == 0) {
            DoomJNI.mouseButton(DoomJNI.MOUSE_LEFT, true);
        } else if (button == 1) {
            DoomJNI.keyDown(' ');
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (wadMissing) {
            return super.mouseReleased(mouseX, mouseY, button);
        }
        
        if (button == 0) {
            DoomJNI.mouseButton(DoomJNI.MOUSE_LEFT, false);
        } else if (button == 1) {
            DoomJNI.keyUp(' ');
        }
        
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (wadMissing || !initialized) {
            return;
        }
        
        if (firstMouse) {
            lastMouseX = (int)mouseX;
            lastMouseY = (int)mouseY;
            firstMouse = false;
            return;
        }
        
        int deltaX = (int)mouseX - lastMouseX;
        int deltaY = (int)mouseY - lastMouseY;
        lastMouseX = (int)mouseX;
        lastMouseY = (int)mouseY;
        
        try {
            float sensitivity = DoomConfig.get().mouseSensitivity;
            deltaX = (int)(deltaX * sensitivity);
            deltaY = (int)(deltaY * sensitivity);
        } catch (Exception e) {
        }
        
        DoomJNI.mouseMove(deltaX, deltaY);
    }

    private int mapKey(int key) {
        return switch (key) {
            case GLFW.GLFW_KEY_W -> DoomJNI.KEY_UPARROW;
            case GLFW.GLFW_KEY_S -> DoomJNI.KEY_DOWNARROW;
            case GLFW.GLFW_KEY_A -> DoomJNI.KEY_LEFTARROW;
            case GLFW.GLFW_KEY_D -> DoomJNI.KEY_RIGHTARROW;
            case GLFW.GLFW_KEY_E -> 'e';
            case GLFW.GLFW_KEY_SPACE -> ' ';
            case GLFW.GLFW_KEY_UP -> DoomJNI.KEY_UPARROW;
            case GLFW.GLFW_KEY_DOWN -> DoomJNI.KEY_DOWNARROW;
            case GLFW.GLFW_KEY_LEFT -> DoomJNI.KEY_LEFTARROW;
            case GLFW.GLFW_KEY_RIGHT -> DoomJNI.KEY_RIGHTARROW;
            case GLFW.GLFW_KEY_ENTER -> DoomJNI.KEY_ENTER;
            case GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL -> DoomJNI.KEY_RCTRL;
            case GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT -> DoomJNI.KEY_RALT;
            case GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT -> DoomJNI.KEY_RSHIFT;
            default -> key;
        };
    }

    @Override
    public void close() {
        if (textureId != null && this.client != null) {
            this.client.getTextureManager().destroyTexture(textureId);
        }
        if (texture != null) {
            texture.close();
        }
        if (image != null) {
            image.close();
        }
        super.close();
    }
}