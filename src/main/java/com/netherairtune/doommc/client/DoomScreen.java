package com.netherairtune.doommc.client;

import com.mojang.blaze3d.systems.RenderSystem;
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

import java.io.File;

public class DoomScreen extends Screen {

    private NativeImage image;
    private NativeImageBackedTexture texture;
    private Identifier textureId;
    private final int doomWidth;
    private final int doomHeight;
    private final int scale = 2;
    private int waitTicks = 0;
    private boolean playerReady = false;
    private boolean initialized = false;
    private boolean wadMissing = false;

    public DoomScreen() {
        super(Text.literal("DoomMC"));
        this.doomWidth = 320;
        this.doomHeight = 200;
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
                System.out.println("[DoomMC] IWAD path: " + wadFile.getAbsolutePath()); // just for debug 
                System.out.println("[DoomMC] IWAD exists: " + wadFile.exists());
                System.out.println("[DoomMC] IWAD size: " + wadFile.length());
                
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
            textureId = this.client.getTextureManager().registerDynamicTexture("doom_screen", texture);
        }
        
        int boxWidth = doomWidth * scale;
        int boxHeight = doomHeight * scale;
        int x = (this.width - boxWidth) / 2;
        int y = (this.height - boxHeight) / 2;

        this.addDrawableChild(
            ButtonWidget.builder(Text.literal("X"), button -> {
                if (this.client != null) {
                    this.client.setScreen(null);
                }
            })
            .dimensions(x + boxWidth - 20, y + 5, 15, 15)
            .build()
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        
        if (wadMissing) {
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
        for (int y = 0; y < doomHeight; y++) {
            for (int x = 0; x < doomWidth; x++) {
                int idx = (y * doomWidth + x) * 4;
                int r = framebuffer[idx] & 0xFF;
                int g = framebuffer[idx + 1] & 0xFF;
                int b = framebuffer[idx + 2] & 0xFF;
                int a = framebuffer[idx + 3] & 0xFF;
                int argb = (a << 24) | (b << 16) | (g << 8) | r;
                image.setColor(x, y, argb);
            }
        }
        
        texture.upload();

        int boxWidth = doomWidth * scale;
        int boxHeight = doomHeight * scale;
        int x = (this.width - boxWidth) / 2;
        int y = (this.height - boxHeight) / 2;

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, textureId);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        context.drawTexture(textureId, x, y, 0, 0, boxWidth, boxHeight, doomWidth, doomHeight);
        
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
        
        DoomJNI.mouseButton(button + 1, true);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (wadMissing) {
            return super.mouseReleased(mouseX, mouseY, button);
        }
        
        DoomJNI.mouseButton(button + 1, false);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (wadMissing) {
            return;
        }
        
        DoomJNI.mouseMove((int)mouseX / 10, (int)mouseY / 10);
    }

    private int mapKey(int key) {
        return switch (key) {
            case GLFW.GLFW_KEY_W -> 'w';
            case GLFW.GLFW_KEY_A -> 'a';
            case GLFW.GLFW_KEY_S -> 's';
            case GLFW.GLFW_KEY_D -> 'd';
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