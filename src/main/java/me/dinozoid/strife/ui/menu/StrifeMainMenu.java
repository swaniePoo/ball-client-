package me.dinozoid.strife.ui.menu;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.font.CustomFontRenderer;
import me.dinozoid.strife.shader.implementations.GlowShader;
import me.dinozoid.strife.shader.implementations.MenuShader;
import me.dinozoid.strife.util.render.RenderUtil;
import me.dinozoid.strife.util.ui.ZoomUtil;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;

public class StrifeMainMenu extends GuiScreen {

    private final ResourceLocation strifeLogo;
    private final ResourceLocation singleplayerButton;
    private final ResourceLocation multiplayerButton;
    private final ResourceLocation altsButton;
    private final ResourceLocation settingsButton;
    private final ResourceLocation quitButton;
    private final MenuShader backgroundShader;
    private GlowShader glowShader;
    private final float zoomValue = 0.298f;
    private ZoomUtil strifeLogoZoom, singleplayerZoom, multiplayerZoom, altsZoom, settingsZoom, quitZoom;
    private StrifeAccountManager strifeAccountManager;

    public StrifeMainMenu() {
        this(0);
    }

    public StrifeMainMenu(int pass) {
        strifeLogo = new ResourceLocation("strife/gui/Strife-128x.png");
        singleplayerButton = new ResourceLocation("strife/gui/mainmenu/singleplayer.png");
        multiplayerButton = new ResourceLocation("strife/gui/mainmenu/multiplayer.png");
        altsButton = new ResourceLocation("strife/gui/mainmenu/alts.png");
        settingsButton = new ResourceLocation("strife/gui/mainmenu/settings.png");
        quitButton = new ResourceLocation("strife/gui/mainmenu/quit.png");
        backgroundShader = new MenuShader(pass);
    }

    private CustomFontRenderer font31;

    @Override
    public void initGui() {
        ScaledResolution sc = new ScaledResolution(mc);
        strifeLogoZoom = new ZoomUtil(sc.getScaledWidth() / 2f - 32, sc.getScaledHeight() / 2f - 75, 64, 64, 12, zoomValue, 6);
        singleplayerZoom = new ZoomUtil(sc.getScaledWidth() / 2f - 12 - 64, sc.getScaledHeight() / 2f, 24, 24, 12, zoomValue, 6);
        multiplayerZoom = new ZoomUtil(sc.getScaledWidth() / 2f - 12 - 32, sc.getScaledHeight() / 2f, 24, 24, 12, zoomValue, 6);
        altsZoom = new ZoomUtil(sc.getScaledWidth() / 2f - 12, sc.getScaledHeight() / 2f, 24, 24, 12, zoomValue, 6);
        settingsZoom = new ZoomUtil(sc.getScaledWidth() / 2f - 12 + 32, sc.getScaledHeight() / 2f, 24, 24, 12, zoomValue, 6);
        quitZoom = new ZoomUtil(sc.getScaledWidth() / 2f - 12 + 64, sc.getScaledHeight() / 2f, 24, 24, 12, zoomValue, 6);
        font31 = Client.INSTANCE.getFontRepository().currentFont().size(31);
        glowShader = new GlowShader(new Color(50, 219, 10), 8, 300);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        final ScaledResolution scaledResolution = new ScaledResolution(mc);
        backgroundShader.render(scaledResolution);
        strifeLogoZoom.update(mouseX, mouseY);
        singleplayerZoom.update(mouseX, mouseY);
        multiplayerZoom.update(mouseX, mouseY);
        altsZoom.update(mouseX, mouseY);
        settingsZoom.update(mouseX, mouseY);
        quitZoom.update(mouseX, mouseY);
        RenderUtil.drawImage(strifeLogo, strifeLogoZoom.getX(), strifeLogoZoom.getY(), strifeLogoZoom.getWidth(), strifeLogoZoom.getHeight(), 255);
        RenderUtil.drawImage(singleplayerButton, singleplayerZoom.getX(), singleplayerZoom.getY(), singleplayerZoom.getWidth(), singleplayerZoom.getHeight(), 255);
        RenderUtil.drawImage(multiplayerButton, multiplayerZoom.getX(), multiplayerZoom.getY(), multiplayerZoom.getWidth(), multiplayerZoom.getHeight(), 255);
        RenderUtil.drawImage(altsButton, altsZoom.getX(), altsZoom.getY(), altsZoom.getWidth(), altsZoom.getHeight(), 255);
        RenderUtil.drawImage(settingsButton, settingsZoom.getX(), settingsZoom.getY(), settingsZoom.getWidth(), settingsZoom.getHeight(), 255);
        RenderUtil.drawImage(quitButton, quitZoom.getX(), quitZoom.getY(), quitZoom.getWidth(), quitZoom.getHeight(), 255);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (RenderUtil.isHovered(strifeLogoZoom.getX(), strifeLogoZoom.getY(), strifeLogoZoom.getWidth(), strifeLogoZoom.getHeight(), mouseX, mouseY)) {
            mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 2));
            try {
                Desktop.getDesktop().browse(new URI("https://strifeclient.club"));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        if (RenderUtil.isHovered(singleplayerZoom.getX(), singleplayerZoom.getY(), singleplayerZoom.getWidth(), singleplayerZoom.getHeight(), mouseX, mouseY)) {
            mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            mc.displayGuiScreen(new GuiSelectWorld(this));
        }
        if (RenderUtil.isHovered(multiplayerZoom.getX(), multiplayerZoom.getY(), multiplayerZoom.getWidth(), multiplayerZoom.getHeight(), mouseX, mouseY)) {
            mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            mc.displayGuiScreen(new GuiMultiplayer(this));
        }
        if (RenderUtil.isHovered(altsZoom.getX(), altsZoom.getY(), altsZoom.getWidth(), altsZoom.getHeight(), mouseX, mouseY)) {
            mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            if (strifeAccountManager == null) strifeAccountManager = new StrifeAccountManager(this, backgroundShader.getPass());
            mc.displayGuiScreen(strifeAccountManager);
        }
        if (RenderUtil.isHovered(settingsZoom.getX(), settingsZoom.getY(), settingsZoom.getWidth(), settingsZoom.getHeight(), mouseX, mouseY)) {
            mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
        }
        if (RenderUtil.isHovered(quitZoom.getX(), quitZoom.getY(), quitZoom.getWidth(), quitZoom.getHeight(), mouseX, mouseY)) {
            mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            mc.shutdown();
        }
    }

    public void setPass(int pass) {
        backgroundShader.setPass(pass);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
    }

    @Override
    public void updateScreen() {
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
