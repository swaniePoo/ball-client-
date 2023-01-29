package me.dinozoid.strife.module.implementations.visuals;

import lombok.Getter;
import lombok.Setter;
import me.dinozoid.strife.Client;
import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.DoAFuckingBloomEvent;
import me.dinozoid.strife.event.implementations.render.Render2DEvent;
import me.dinozoid.strife.event.implementations.render.RenderGUIEvent;
import me.dinozoid.strife.font.CustomFontRenderer;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.newshader.blur.BloomUtil;
import me.dinozoid.strife.newshader.blur.GaussianBlur;
import me.dinozoid.strife.newshader.blur.KawaseBlur;
import me.dinozoid.strife.property.Property;
import me.dinozoid.strife.property.implementations.ColorProperty;
import me.dinozoid.strife.property.implementations.DoubleProperty;
import me.dinozoid.strife.property.implementations.EnumProperty;
import me.dinozoid.strife.property.implementations.MultiSelectEnumProperty;
import me.dinozoid.strife.shader.implementations.BloomShader;
import me.dinozoid.strife.shader.implementations.BlurShader;
import me.dinozoid.strife.util.Animation;
import me.dinozoid.strife.util.Direction;
import me.dinozoid.strife.util.Dragging;
import me.dinozoid.strife.util.network.ServerUtil;
import me.dinozoid.strife.util.player.PlayerUtil;
import me.dinozoid.strife.util.render.RenderUtil;
import me.dinozoid.strife.util.render.RoundedUtil;
import me.dinozoid.strife.util.render.StencilUtil;
import me.dinozoid.strife.util.system.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL11.*;

@ModuleInfo(name = "HUD", renderName = "HUD", description = "The Heads-Up-Display module.", category = Category.VISUALS)
public class OverlayModule extends Module {

    private static final EnumProperty<ColorMode> colorModeProperty = new EnumProperty<>("Color Mode", ColorMode.PULSE);
    private static final ColorProperty colorProperty = new ColorProperty("Color", new Color(209, 50, 50),
            () -> colorModeProperty.getValue() == ColorMode.STATIC || colorModeProperty.getValue() == ColorMode.PULSE || colorModeProperty.getValue() == ColorMode.SWITCH);
    private static final ColorProperty secondColorProperty = new ColorProperty("Second Color", new Color(29, 205, 200), () -> colorModeProperty.getValue() == ColorMode.SWITCH);
    private static final EnumProperty<Font> fontProperty = new EnumProperty<>("Font", Font.VANILLA);
    private final EnumProperty<Background> backgroundProperty = new EnumProperty<>("Background", Background.OUTLINE);
    private final EnumProperty<ToggleAnimation> animationProperty = new EnumProperty<>("Animation", ToggleAnimation.MOVE_IN);
    private final MultiSelectEnumProperty<Element> elementsProperty = new MultiSelectEnumProperty<>("Elements", Element.ARRAYLIST, Element.BOSSBAR, Element.FPS, Element.NAME, Element.NOTIFICATIONS);
    private static final DoubleProperty fontSizeProperty = new DoubleProperty("Font Size", 19, 14, 36, 1, Property.Representation.INT, () -> fontProperty.getValue() != Font.VANILLA);
    private final DoubleProperty backgroundAlphaProperty = new DoubleProperty("Background Alpha", 120, 0, 255, 1, Property.Representation.INT);
    private final EnumProperty<LogoMode> logoType = new EnumProperty<>("Watermark Type", LogoMode.CSGO);

    public Dragging arraylistDrag = Client.INSTANCE.createDrag(this, "arraylist", 2, 3);

    public String longest = "";
    double longestWidth = 0;
    @Getter @Setter
    private float bossbarX = -1, bossbarY = -1, bossbarDragX, bossbarDragY, bossbarMaxX, bossbarMaxY, bossbarOriginX, bossbarOriginY;
    @Getter @Setter
    private boolean bossbarDragging, bossbarHovered, bossbarSet;
    private float potionX, potionY, potionDragX, potionDragY, potionMaxX, potionMaxY, potionOriginX, potionOriginY;
    private boolean potionDragging, potionHovered;
    private final float potionWidth = 190;
    private final float potionHeight = 100;
    public final List<AnimatedModule> animatedModules = new ArrayList<>();
    private ResourceLocation strifeLogo;
    Framebuffer bloomFramebuffer;
    private int ping = -1;
    public List<Module> modules;

    private SessionInfoModule sessionInfoModule;

    @EventHandler
    private final Listener<RenderGUIEvent> renderGUIEventListener = new Listener<>(event -> {
        for (Element element : elementsProperty.values()) {
            if (elementsProperty.selected(element)) {
                if (element == Element.NOTIFICATIONS) {
                    if (mc.currentScreen instanceof GuiIngameMenu) return;
                    Client.INSTANCE.getNotificationRepository().drawNotifications();
                }
            }
        }
    });

    private final Comparator<Object> SORT_METHOD = Comparator.comparingDouble(m -> {
        Module module = (Module) m;
        String name = module.renderName();
        return getWidth(name);
    }).reversed();

    public void getModules() {
        if (modules == null) {
            List<Module> moduleList = Client.INSTANCE.getModuleRepository().getToggledModules(Client.INSTANCE.getModuleRepository().modules());
            modules = moduleList;
        }
    }

    public void draw(int mouseX, int mouseY, float partialTicks) {
        if(!toggled()) return;
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        if(bossbarMaxX != scaledResolution.getScaledWidth())
            bossbarMaxX =scaledResolution.getScaledWidth();
        if(bossbarMaxY != scaledResolution.getScaledHeight())
            bossbarMaxY = scaledResolution.getScaledHeight();
        if(potionMaxX != scaledResolution.getScaledWidth())
            potionMaxX =scaledResolution.getScaledWidth();
        if(potionMaxY != scaledResolution.getScaledHeight())
            potionMaxY = scaledResolution.getScaledHeight();
        if (bossbarDragging) {
            bossbarX = MathHelper.clamp_float(mouseX + bossbarDragX, 0, bossbarMaxX);
            bossbarY = MathHelper.clamp_float(mouseY + bossbarDragY, 0, bossbarMaxY);
            bossbarOriginX = bossbarX;
            bossbarOriginY = bossbarY;
        }
        if (potionDragging) {
            potionX = MathHelper.clamp_float(mouseX + potionDragX, 0, potionMaxX);
            potionY = MathHelper.clamp_float(mouseY + potionDragY, 0, potionMaxY);
            potionOriginX = potionX;
            potionOriginY = potionY;
        }
        if(mc.currentScreen instanceof GuiChat) {
            float x = bossbarX;
            float y = bossbarY;
            bossbarHovered = RenderUtil.inBounds(x, y - 10, x + 182, y + 5, mouseX, mouseY);
            potionHovered = RenderUtil.isHovered(potionX, potionY, potionWidth, potionHeight, mouseX, mouseY);
        }
    }

    public void onMouseClick(int mouseX, int mouseY, int mouseButton) {
        if(!toggled()) return;
        float x = bossbarX;
        float y = bossbarY;
        if (RenderUtil.inBounds(x, y - 10, x + 182, y + 5, mouseX, mouseY)) {
            if (mouseButton == 0) {
                bossbarDragging = true;
                potionDragging = false;
                bossbarDragX = (x - mouseX);
                bossbarDragY = (y - mouseY);
            }
        }
        x = potionX;
        y = potionY;
        if(RenderUtil.isHovered(x, y, potionWidth, potionHeight, mouseX, mouseY)) {
            potionDragging = true;
            bossbarDragging = false;
            potionDragX = (x - mouseX);
            potionDragY = (y - mouseY);
        }
    }

    public void onMouseRelease(int mouseX, int mouseY, int state) {
        if(!toggled()) return;
        bossbarDragging = false;
        bossbarHovered = false;
        potionDragging = false;
        potionHovered = false;
    }

    @Override
    public void init() {
        super.init();
    }

    @EventHandler
    private final Listener<DoAFuckingBloomEvent> doAFuckingBloomEventListener = new Listener<>(event -> {
        if(modules == null) return;
        double yOffset = 0;
        ScaledResolution sr = new ScaledResolution(mc);
        for (Module module : modules) {
            final Animation moduleAnimation = module.animation;
            if (!module.toggled() && moduleAnimation.finished(Direction.BACKWARDS)) continue;

            String displayText = module.renderName();
            double textWidth = getWidth(displayText);

            double xValue = sr.getScaledWidth() - (arraylistDrag.getX());

            boolean flip = xValue <= sr.getScaledWidth() / 2f;
            double x = flip ? xValue : sr.getScaledWidth() - (textWidth + arraylistDrag.getX());

            double y = yOffset + arraylistDrag.getY();

            double heightVal = 11;
            switch (animationProperty.getValue()) {
                case MOVE_IN:
                    if(flip){
                        x -= Math.abs((moduleAnimation.getOutput() - 1) * (sr.getScaledWidth() - (arraylistDrag.getX() + textWidth)));
                    }else {
                        x += Math.abs((moduleAnimation.getOutput() - 1) * (arraylistDrag.getX() + textWidth));
                    }
                    break;
                case SCALE_IN:
                    RenderUtil.scaleStart((float) (x + getWidth(displayText) / 2f), (float) (y + heightVal / 2 - getHeight(displayText) / 2f), (float) moduleAnimation.getOutput());
                    break;
            }

            if(backgroundProperty.getValue() == Background.NONE) {
                Gui.drawRect((float) (x - 2), (float) (y - 3), (float) ((x - 2) + getWidth(displayText) + 5), (float) ((float) (y - 3) + heightVal), Color.WHITE.getRGB());
            }
            if (animationProperty.getValue() == ToggleAnimation.SCALE_IN) {
                RenderUtil.scaleEnd();
            }

            yOffset += moduleAnimation.getOutput() * heightVal;
        }
    });

    @EventHandler
    private final Listener<Render2DEvent> render2DListener = new Listener<>(event -> {
        if(sessionInfoModule == null)
            sessionInfoModule = SessionInfoModule.instance();
        Gui.drawRect(0, 0, 0, 0, 0);

        //RoundedUtil.drawRound(30, 30, 50, 50, 25, Color.WHITE);

//        bloomFramebuffer = RenderUtil.createFramebuffer(bloomFramebuffer, true);
//
//        bloomFramebuffer.framebufferClear();
//        bloomFramebuffer.bindFramebuffer(true);
//        RenderUtil.drawCircle(30, 30, 8, 360, new Color(255, 255, 255, 255));
//        bloomFramebuffer.unbindFramebuffer();

       // BloomUtil.renderBlur(bloomFramebuffer.framebufferTexture, 8, 2);

        BloomShader.drawAndBloom(() -> Gui.drawRect(30, 30, 50, 50, -1));
        double x12 = arraylistDrag.getX();
        double y12 = arraylistDrag.getY();
       // arraylistDrag.setw
        if (strifeLogo == null) strifeLogo = new ResourceLocation("strife/gui/Strife-128x.png");
        if (!mc.gameSettings.showDebugInfo) {
            if (logoType.getValue() == LogoMode.PICTURE) RenderUtil.drawImage(strifeLogo, 15, 15, 50, 50);
//            if (animatedModules.isEmpty()) {
//                for (Module module : Client.INSTANCE.getModuleRepository().modules()) {
//                    animatedModules.add(new AnimatedModule(module, (float) 0, (float) 0, getWidth(module.renderName()), 0));
//                }
//            }
           // ScaledResolution sc = new ScaledResolution(mc);
            int rightYOff = 0;
            int leftYOff = 0;
            if(mc.currentScreen instanceof GuiChat)
                rightYOff = 14;

            int i = 0;

//            arraylistDrag.setWidth(animatedModules.get(0).x);
//            arraylistDrag.setHeight(i);

//            StencilUtil.initStencilToWrite();
//            for(AnimatedModule animatedModule1 : activeModules){
//                if (elementsProperty().selected(Element.ARRAYLIST)) {
//                    Gui.drawRect((float) (getWidth(animatedModule1.module.renderName()) - x12 - 5), (float) y12, (float) getWidth(animatedModule1.module.renderName()), (float) (y12 + animatedModule1.height), -1);
//                }
//            }
//            StencilUtil.readStencilBuffer(1);
//            GaussianBlur.renderBlur(10);
//            StencilUtil.uninitStencilBuffer();

            for (Element element : elementsProperty.values()) {
                if (elementsProperty.selected(element)) {
                    switch (element) {
                        case NAME: {
                            if (logoType.getValue() == LogoMode.TEXT)
                                drawStringWithShadow(Client.CUSTOMNAME.charAt(0) + "\u00A77" + Client.CUSTOMNAME.substring(1), 2, 2, getColor(1));
                            break;
                        }
                        case FPS: {
                            String text = "FPS:\u00A7f " + Minecraft.getDebugFPS();
                            leftYOff += getHeight(text) - 2;
                            drawStringWithShadow(text, 2, event.getScaledResolution().getScaledHeight() - leftYOff, getColor(0));
                            break;
                        }
                        case SPEED: {
                            double bps = Math.hypot(mc.thePlayer.posX - mc.thePlayer.prevPosX, mc.thePlayer.posZ - mc.thePlayer.prevPosZ) * mc.timer.timerSpeed * 20;
                            String text = "Speed: \u00A7f" + Math.round(bps * 100.0) / 100.0 + " b/s";
                            leftYOff += getHeight(text) - 2;
                            drawStringWithShadow(text, 2, event.getScaledResolution().getScaledHeight() - leftYOff, getColor(1));
                            break;
                        }
                        case COORDS: {
                            final String text = "XYZ:\u00A7f "
                                    + String.format("%.0f", mc.thePlayer.posX).replace(",", ".")
                                    + " " + String.format("%.0f", mc.thePlayer.posY).replace(",", ".")
                                    + " " + String.format("%.0f", mc.thePlayer.posZ).replace(",", ".");
                            rightYOff += getHeight(text) - 2;
                            drawStringWithShadow(text, mc.displayWidth / 2F - getWidth(text) - 2, mc.displayHeight / 2f -  rightYOff - 11, getColor(0));
                            break;
                        }
                        case ARRAYLIST: {
                            //getModules();
                            modules = Client.INSTANCE.getModuleRepository().getToggledModules(Client.INSTANCE.getModuleRepository().modules());
                            modules.sort(SORT_METHOD);

                            if(!Client.INSTANCE.getModuleRepository().getToggledModules(modules).isEmpty()) {
                                Module firstMod = Client.INSTANCE.getModuleRepository().getToggledModules(modules).get(0);
                                longest = firstMod.renderName();
                                longestWidth = getWidth(longest);
                            }
                            double yOffset = 0;
                            ScaledResolution sr = new ScaledResolution(mc);
                            int count = 0;
                            //StencilUtil.initStencilToWrite();
                            for (Module module : modules) {
                                final Animation moduleAnimation = module.animation;

                                moduleAnimation.setEndPoint(1);
                                moduleAnimation.setDuration(500);
                                moduleAnimation.setDirection(module.toggled() ? Direction.FORWARDS : Direction.BACKWARDS);

                                if (!module.toggled() && moduleAnimation.finished(Direction.BACKWARDS)) continue;

                                String displayText = module.renderName();
                                double textWidth = getWidth(displayText);

                                double xValue = sr.getScaledWidth() - (arraylistDrag.getX());


                                boolean flip = xValue <= sr.getScaledWidth() / 2f;
                                double x = flip ? xValue : sr.getScaledWidth() - (textWidth + arraylistDrag.getX());


                                float alphaAnimation = 1;

                                double y = yOffset + arraylistDrag.getY();

                                double heightVal = 11;

                                switch (animationProperty.getValue()) {
                                    case MOVE_IN:
                                        if(flip){
                                            x -= Math.abs((moduleAnimation.getOutput() - 1) * (sr.getScaledWidth() - (arraylistDrag.getX() - textWidth)));
                                        }else {
                                            x += Math.abs((moduleAnimation.getOutput() - 1) * (arraylistDrag.getX() + textWidth));
                                        }
                                        break;
                                    case SCALE_IN:
                                        RenderUtil.scaleStart((float) (x + getWidth(displayText) / 2f), (float) (y + heightVal / 2 - getHeight(displayText) / 2f), (float) moduleAnimation.getOutput());
                                        alphaAnimation = (float) moduleAnimation.getOutput();
                                        break;
                                }

                                if (backgroundProperty.getValue() == Background.NONE) {
                                    Gui.drawRect((float) (x - 2), (float) (y - 3), (float) ((x - 2) + getWidth(displayText) + 5), (float) ((float) (y - 3) + heightVal),
                                            RenderUtil.applyOpacity(new Color(20, 20, 20), backgroundAlphaProperty.getValue().floatValue() / 255).getRGB());
                                }


                                int index = (int) (count * 200);
                                int textcolor = getColor(index);

                                drawStringWithShadow(displayText, (float) x, (float) ((y - (fontProperty.getValue() == Font.CIRCULAR ? 0.2 : 2)) + (heightVal / 2 - getHeight(displayText) / 2)), RenderUtil.applyOpacity(textcolor, alphaAnimation));

                                if (animationProperty.getValue() == ToggleAnimation.SCALE_IN) {
                                    RenderUtil.scaleEnd();
                                }

                                yOffset += moduleAnimation.getOutput() * heightVal;
                                count++;
                            }
                            break;
                        }
                        case POTIONSTATUS: {
                            float x = potionX;
                            float y = potionY + event.getScaledResolution().getScaledHeight() / 2f;
                            float yOff = 0;
                            if(potionHovered) {
                                RenderUtil.drawRect(potionX, potionY, potionX + potionWidth, potionY + potionHeight, new Color(0, 0, 0, 120).getRGB());
                            }
                            for(PotionEffect potionEffect : mc.thePlayer.getActivePotionEffects()) {
                                Potion potion = Potion.potionTypes[potionEffect.getPotionID()];
                                if (potion.hasStatusIcon()) {
                                    int i2 = potion.getStatusIconIndex();
                                    mc.getTextureManager().bindTexture(GuiContainer.inventoryBackground);
                                    glPushMatrix();
                                    glTranslatef(x, y, 0);
                                    glScalef(0.4f, 0.4f, 0);
                                    glTranslatef(-x, -y, 0);
                                    Gui.drawTexturedModalRect(potionX, y + yOff, i2 % 8 * 18, 198 + i2 / 8 * 18, 18, 18);
                                    glScalef(1, 1, 0);
                                    glPopMatrix();
                                    GlStateManager.bindTexture(0);
                                }
                                Client.INSTANCE.getFontRepository().fontBy("ProductSans").size(12).drawStringWithShadow(PlayerUtil.getFormattedPotionName(potionEffect) + "\u00A77 " + Potion.getDurationString(potionEffect), x + 8, y + yOff / 2.5f + 1, PlayerUtil.getPotionColor(potionEffect));
                                glColor4f(1, 1, 1, 1);
//                                RenderUtil.drawImage(location, x, y + yOff, 16, 16);
                                yOff += 20;
                            }
                            break;
                        }
                        case NOTIFICATIONS: {
                            Client.INSTANCE.getNotificationRepository().drawNotifications();
                            break;
                        }
                    }
                }

            }
            if (logoType.getValue() == LogoMode.CSGO) {
                if (ServerUtil.getServerData() != null) {
                    if (ServerUtil.onServer() && ServerUtil.getServerData().pingToServer != -1) {
                        ping = (int) ServerUtil.getServerData().pingToServer;
                    }
                }
                final String clock = (new SimpleDateFormat("h:mm a").format(Calendar.getInstance().getTime()));
                CustomFontRenderer font = Client.INSTANCE.getFontRepository().fontBy("ProductSans").size(19);
                final String msg = "\u00A7c" + Client.CUSTOMNAME.toLowerCase() + "\u00A77sense | " + Client.INSTANCE.getStrifeUser().clientUsername() + " | " + ping + "ms | " + ServerUtil.getCurrentIP() + " | " + clock;
                float width = font.getWidth(msg);
                float height = 11;
                float x = 3;
                float y = 4;
                float boxWidth = width + 6;
                float boxHeight = height + 5;
                RenderUtil.drawRect(x, y, x + boxWidth, y + boxHeight, new Color(Color.DARK_GRAY.getRed(), Color.DARK_GRAY.getBlue(), Color.DARK_GRAY.getGreen(), 180).darker().darker().getRGB());
                //RenderUtil.drawRect(x, y, x + boxWidth, y + 1, new Color(209, 50, 50).getRGB());
                final Color c = new Color(209, 50, 50);

                RenderUtil.drawGlowingRect(x, y, x + boxWidth, y + 1, c.getRGB());
//                glEnable(GL_LINE_SMOOTH);
//                for (int i = 0; i <= 7; ++i) {
//                    RenderUtil.drawRect(x - i, y - i, (x + boxWidth) + i * 2, (y + 1) + i, new Color(c.getRed(), c.getGreen(), c.getBlue(), 4).getRGB());
//                }
//                glDisable(GL_LINE_SMOOTH);

                font.drawStringWithShadow(msg, x + 3, y + 3, -1);
            }
        }
    });

    public void stuffToBlur(ScaledResolution sc, List<AnimatedModule> activeModules) {
        if (backgroundProperty.getValue() == Background.BLUR) {
            for (AnimatedModule animatedModule : activeModules) {
                switch (backgroundProperty.getValue()) {
                    case BARLEFT:
                        RenderUtil.drawRect(getWidth(animatedModule.module.renderName()) - animatedModule.x - 3, animatedModule.y, getWidth(animatedModule.module.renderName()), animatedModule.y + animatedModule.height, -1);
                        break;
                    case OUTLINE:
                        RenderUtil.drawRect(getWidth(animatedModule.module.renderName()) - animatedModule.x - 5, animatedModule.y, getWidth(animatedModule.module.renderName()), animatedModule.y + animatedModule.height, -1);
                        break;
                    default:
                        RenderUtil.drawRect(getWidth(animatedModule.module.renderName()) - animatedModule.x - 4, animatedModule.y, getWidth(animatedModule.module.renderName()), animatedModule.y + animatedModule.height, -1);
                        break;
                }
            }
        }
        if (sessionInfoModule.blurProperty().getValue() && sessionInfoModule.toggled()) {
            float x = sessionInfoModule.xPositionProperty().getValue().floatValue();
            float y = sessionInfoModule.yPositionProperty().getValue().floatValue();
            float width = sessionInfoModule.width();
            float height = sessionInfoModule.height();
            RenderUtil.drawRect(x, y, x + width, y + height, -1);
        }
    }

    public static int getColor(int index) {
        switch (colorModeProperty.getValue()) {
            case PULSE:
                return RenderUtil.fade(colorProperty.getValue(), -8000, index * -5);
            case ASTOLFO:
                return RenderUtil.astolfo(4, 0.5f, 1f, index);
            case RAINBOW:
                return RenderUtil.rainbow(4, 0.4f, 0.8f, index);
            case SWITCH:
                return RenderUtil.colorSwitch(secondColorProperty.getValue(), colorProperty.getValue(), 2000, -index / 40, 75, 2);
        }
        return colorProperty.getValue().getRGB();
    }

    public static void drawStringWithShadow(String text, float x, float y, int color) {
        drawStringWithShadow(text, x, y, color, 1);
    }

    public static float getWidth(String text) {
        return getWidth(text, 1);
    }

    public static float getHeight(String text) {
        return getHeight(text, 1);
    }


    public static void drawStringWithShadow(String text, float x, float y, int color, float scale) {
        switch (fontProperty.getValue()) {
            case VANILLA: {
                glPushMatrix();
                glTranslatef(x, y, 0);
                glScalef(scale, scale, 0);
                glTranslatef(-x, -y, 0);
                mc.fontRendererObj.drawStringWithShadow(text, x, y, color);
                glScalef(1, 1, 0);
                glPopMatrix();
                break;
            }
            case CIRCULAR: {
                Client.INSTANCE.getFontRepository().fontBy("Circular").size(fontSizeProperty.getValue().intValue()).drawStringWithShadow(text, x, y, color);
                break;
            }
            default: {
                Client.INSTANCE.getFontRepository().currentFont().size(fontSizeProperty.getValue().intValue()).drawStringWithShadow(text, x, y, color);
                break;
            }
        }
    }

    public static float getWidth(String text, float scale) {
        switch (fontProperty.getValue()) {
            case VANILLA: {
                return mc.fontRendererObj.getStringWidth(text) * scale;
            }
            case CIRCULAR: {
                return Client.INSTANCE.getFontRepository().fontBy("Circular").size(fontSizeProperty.getValue().intValue()).getWidth(text);
            }
            default: {
                return Client.INSTANCE.getFontRepository().currentFont().size(fontSizeProperty.getValue().intValue()).getWidth(text);
            }
        }
    }

    public static float getHeight(String text, float scale) {
        switch (fontProperty.getValue()) {
            case VANILLA: {
                return mc.fontRendererObj.FONT_HEIGHT + 2 * scale;
            }
            case CIRCULAR: {
                return Client.INSTANCE.getFontRepository().fontBy("Circular").size(fontSizeProperty.getValue().intValue()).getHeight(text) + 3;
            }
            default: {
                return Client.INSTANCE.getFontRepository().currentFont().size(fontSizeProperty.getValue().intValue()).getHeight(text) + 3;
            }
        }
    }

    public static final class AnimatedModule {

        private final Module module;
        private final float origWidth, origHeight;
        private float origX, origY, x, y, width, height;
        private final TimerUtil timer = new TimerUtil();

        public AnimatedModule(Module module, float origX, float origY, float width, float height) {
            this.module = module;
            this.origX = origX;
            this.origY = origY;
            this.x = -1;
            this.y = -1;
            this.origWidth = width;
            this.origHeight = height;
        }

        public boolean hidden() {
            return module.hidden();
        }
        public boolean toggled() {
            return module.toggled();
        }
        public boolean visible() {
            return (module.toggled() || x > 0) && !module.hidden();
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }
    }

    public enum Font {
        CLIENT, CIRCULAR, VANILLA
    }

    public enum Background {
        OUTLINE, BARLEFT, BARRIGHT, BLUR, NONE
    }

    public enum ToggleAnimation {
        MOVE_IN, SCALE_IN
    }

    public enum Element {
        NAME, ARRAYLIST, TIME, NOTIFICATIONS, FPS, SPEED, COORDS, POTIONSTATUS, BOSSBAR, ARMORHUD
    }

    public enum ColorMode {
        ASTOLFO, PULSE, RAINBOW, STATIC, SWITCH
    }

    public enum LogoMode {
        PICTURE, TEXT, CSGO
    }

    public MultiSelectEnumProperty<Element> elementsProperty() {
        return elementsProperty;
    }

    public static OverlayModule instance() {
        return Client.INSTANCE.getModuleRepository().moduleBy(OverlayModule.class);
    }

}
