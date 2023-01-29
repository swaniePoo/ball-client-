package me.dinozoid.strife.module.implementations.visuals;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.DoAFuckingBloomEvent;
import me.dinozoid.strife.event.implementations.network.PacketInboundEvent;
import me.dinozoid.strife.event.implementations.render.Render2DEvent;
import me.dinozoid.strife.font.CustomFontRenderer;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.newshader.blur.GaussianBlur;
import me.dinozoid.strife.newshader.blur.KawaseBlur;
import me.dinozoid.strife.property.Property;
import me.dinozoid.strife.property.implementations.DoubleProperty;
import me.dinozoid.strife.util.network.ServerUtil;
import me.dinozoid.strife.util.player.ChatFormatting;
import me.dinozoid.strife.util.render.RenderUtil;
import me.dinozoid.strife.util.render.RoundedUtil;
import me.dinozoid.strife.util.render.StencilUtil;
import me.dinozoid.strife.util.system.DateUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S40PacketDisconnect;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.Display;

import java.awt.*;

@ModuleInfo(name = "SessionInfo", renderName = "SessionInfo", category = Category.VISUALS)
public class SessionInfoModule extends Module {

    private final float width = 190;
    private final float height = 100;
    private final DoubleProperty xPositionProperty = new DoubleProperty("X", 4, 0, Display.getWidth() + 10, 1, Property.Representation.INT);
    private final DoubleProperty yPositionProperty = new DoubleProperty("Y", 30, 0, Display.getHeight() - 52, 1, Property.Representation.INT);
    private final Property<Boolean> blurProperty = new Property<>("Blur", false);
    private float dragX;
    private boolean dragging, hovered;

    private CustomFontRenderer font19, font21;
    private ResourceLocation playtimeImage, gamesImage, killsImage, bansImage, winsImage;
    private float dragY;
    private float origX;
    private float origY;

    private long startPlayTime;
    private int kills, bans, games, wins;

    public static SessionInfoModule instance() {
        return Client.INSTANCE.getModuleRepository().moduleBy(SessionInfoModule.class);
    }

    @EventHandler
    private final Listener<DoAFuckingBloomEvent> doAFuckingBloomEventListener = new Listener<>(event -> {
        float x = xPositionProperty.getValue().floatValue();
        float y = yPositionProperty.getValue().floatValue();
        Gui.drawRect(x, y, x + width,  y + height, -1);
       // RoundedUtil.drawRoundOutline(x, y, width, this.height, 4, 0.5f, new Color(-1), new Color(-1));
    });

    @EventHandler
    private final Listener<PacketInboundEvent> packetInboundListener = new Listener<>(event -> {
        if (event.getPacket() instanceof S02PacketChat) {
            S02PacketChat success = event.getPacket();
            String message = ChatFormatting.stripFormatting(success.getChatComponent().getFormattedText());
            if (message.contains("by " + mc.session.getUsername())) {
                kills++;
            } else if (message.contains("You won!") || message.contains("coins! (Win)")) {
                wins++;
            } else if (message.contains("The game starts in 1 second!")) {
                games++;
            }
        }
        if(event.getPacket() instanceof S40PacketDisconnect) {
            S40PacketDisconnect packet = event.getPacket();
            packet.getReason().appendText("\n" + ChatFormatting.RESET + "You played for: " + DateUtil.getFormattedTime(System.currentTimeMillis() - startPlayTime));
        }
    });

    @EventHandler
    private final Listener<Render2DEvent> render2DListener = new Listener<>(event -> {
        float x = xPositionProperty.getValue().floatValue();
        float y = yPositionProperty.getValue().floatValue();
        StencilUtil.initStencilToWrite();
        RoundedUtil.drawRound(x, y, width, this.height, 4, new Color(-1));
        StencilUtil.readStencilBuffer(1);
        GaussianBlur.renderBlur(10);
        //KawaseBlur.renderBlur(8, 2);
        StencilUtil.uninitStencilBuffer();
        if (mc.currentScreen instanceof GuiChat) {
            if (hovered)
                RoundedUtil.drawRoundOutline(x, y, width, this.height, 4, 0.1f, new Color(0, 0, 0, 150), new Color(0, 0, 0, 150));
            else RoundedUtil.drawRoundOutline(x, y, width, this.height, 4, 0.1f, new Color(0, 0, 0, 100), new Color(0, 0, 0, 150));
        }
        float height = font21.getHeight("session\u00A77info");
        Color color = new Color(209, 50, 50);
        if (!blurProperty.getValue()) {
            RoundedUtil.drawRoundOutline(x, y, width, this.height, 4, 0.1f, new Color(Color.DARK_GRAY.getRed(), Color.DARK_GRAY.getBlue(), Color.DARK_GRAY.getGreen(), 180).darker().darker(), color);
            //RoundedUtil.drawRoundOutline(x - 1, y + 15, width,  2, 0, 0.1f, new Color(Color.DARK_GRAY.getRed(), Color.DARK_GRAY.getBlue(), Color.DARK_GRAY.getGreen(), 180).darker().darker(), color);
            //RoundedUtil.drawRound(x, y, x + width, y + this.height - 20, 8, new Color(Color.DARK_GRAY.getRed(), Color.DARK_GRAY.getBlue(), Color.DARK_GRAY.getGreen(), 180).darker().darker());
           // RenderUtil.drawRect(x, y, x + width, y + this.height, new Color(Color.DARK_GRAY.getRed(), Color.DARK_GRAY.getBlue(), Color.DARK_GRAY.getGreen(), 180).darker().darker().getRGB());
//            RenderUtil.drawRect(x, y, x + width, y + 1, color.getRGB());
//            RenderUtil.drawRect(x, y, x + 1, y + this.height, color.getRGB());
        }
        //RenderUtil.drawRect(x, y, x + width, y + 1, color.getRGB());
        //RenderUtil.drawRect(x, y, x + 1, y + this.height, color.getRGB());
        font21.drawStringWithShadow("session\u00A77info", x + 4, y + 4, RenderUtil.brighter(color, 0.83F).getRGB());
        RenderUtil.drawImageWithTint(playtimeImage, x + 4, y + height + 8, 12, 12, color);
        long millis = ServerUtil.onServer() ? System.currentTimeMillis() - startPlayTime : 0;
        font19.drawStringWithShadow(DateUtil.getFormattedTime(millis), x + 4 + 15, y + height + 9, -1);
        RenderUtil.drawImageWithTint(gamesImage, x + 4, y + height + 8 + 15, 12, 12, color);
        font19.drawStringWithShadow(games + " Games played", x + 4 + 15, y + height + 9 + 15, -1);
        RenderUtil.drawImageWithTint(winsImage, x + 4, y + height + 8 + 15 + 15, 12, 12, color);
        font19.drawStringWithShadow(wins + " Wins", x + 4 + 15, y + height + 8 + 15 + 16, -1);
        RenderUtil.drawImageWithTint(killsImage, x + 4, y + height + 8 + 15 + 15 + 16, 12, 12, color);
        font19.drawStringWithShadow(kills + " Kills", x + 4 + 15, y + height + 8 + 15 + 15 + 17, -1);
        RenderUtil.drawImageWithTint(bansImage, x + 4, y + height + 8 + 15 + 15 + 15 + 17, 12, 12, color);
        font19.drawStringWithShadow(bans + " Bans", x + 4 + 15, y + height + 8 + 15 + 15 + 15 + 18, -1);
    });

    public void draw(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        if(xPositionProperty.max() != scaledResolution.getScaledWidth())
            xPositionProperty.max(scaledResolution.getScaledWidth());
        if(yPositionProperty.max() != scaledResolution.getScaledHeight())
            yPositionProperty.max(scaledResolution.getScaledHeight());
        if(!toggled()) return;
        if (dragging) {
            xPositionProperty.setValue((double) (mouseX + dragX));
            yPositionProperty.setValue((double) (mouseY + dragY));
            origX = xPositionProperty.getValue().floatValue();
            origY = yPositionProperty.getValue().floatValue();
        }
        if(mc.currentScreen instanceof GuiChat) {
            float x = xPositionProperty.getValue().floatValue();
            float y = yPositionProperty.getValue().floatValue();
            hovered = RenderUtil.inBounds(x, y, x + width, y + height, mouseX, mouseY);
        }
    }

    public void onMouseClick(int mouseX, int mouseY, int mouseButton) {
        if (!toggled()) return;
        if (RenderUtil.inBounds(xPositionProperty.getValue().floatValue(), yPositionProperty.getValue().floatValue(), width + xPositionProperty.getValue().floatValue(), yPositionProperty.getValue().floatValue() + height, mouseX, mouseY)) {
            if (mouseButton == 0) {
                dragging = true;
                dragX = (xPositionProperty.getValue().floatValue() - mouseX);
                dragY = (yPositionProperty.getValue().floatValue() - mouseY);
            }
        }
    }

    public void onMouseRelease(int mouseX, int mouseY, int state) {
        if (!toggled()) return;
        dragging = false;
        hovered = false;
    }

    @Override
    public void init() {
        super.init();
        font19 = Client.INSTANCE.getFontRepository().fontBy("ProductSans").size(19);
        font21 = Client.INSTANCE.getFontRepository().fontBy("ProductSans").size(21);
        playtimeImage = new ResourceLocation("strife/gui/sessioninfo/playtime.png");
        gamesImage = new ResourceLocation("strife/gui/sessioninfo/games.png");
        killsImage = new ResourceLocation("strife/gui/sessioninfo/kills.png");
        bansImage = new ResourceLocation("strife/gui/sessioninfo/bans.png");
        winsImage = new ResourceLocation("strife/gui/sessioninfo/wins.png");
    }

    public Property<Boolean> blurProperty() {
        return blurProperty;
    }

    public DoubleProperty xPositionProperty() {
        return xPositionProperty;
    }

    public DoubleProperty yPositionProperty() {
        return yPositionProperty;
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    public void addBan() {
        this.bans++;
    }
    public long startPlayTime() {
        return startPlayTime;
    }
    public void startPlayTime(long startPlayTime) {
        this.startPlayTime = startPlayTime;
    }
    public int kills() {
        return kills;
    }
    public void kills(int kills) {
        this.kills = kills;
    }
    public int bans() {
        return bans;
    }
    public void bans(int bans) {
        this.bans = bans;
    }
    public int games() {
        return games;
    }
    public void games(int games) {
        this.games = games;
    }
    public int wins() {
        return wins;
    }
    public void wins(int wins) {
        this.wins = wins;
    }
}
