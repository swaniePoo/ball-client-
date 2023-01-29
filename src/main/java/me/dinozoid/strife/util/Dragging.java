package me.dinozoid.strife.util;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import me.dinozoid.strife.Client;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.implementations.visuals.OverlayModule;
import me.dinozoid.strife.util.render.RenderUtil;
import me.dinozoid.strife.util.render.RoundedUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.util.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class Dragging extends MinecraftUtil {
    @Expose
    @SerializedName("x")
    private float xPos;
    @Expose
    @SerializedName("y")
    private float yPos;

    public float initialXVal;
    public float initialYVal;

    private float startX, startY;
    private boolean dragging;

    private float width, height;

    @Expose
    @SerializedName("name")
    private String name;

    private final Module module;

    public Animation hoverAnimation = new DecelerateAnimation(250, 1, Direction.BACKWARDS);

    public Dragging(Module module, String name, float initialXVal, float initialYVal) {
        this.module = module;
        this.name = name;
        this.xPos = initialXVal;
        this.yPos = initialYVal;
        this.initialXVal = initialXVal;
        this.initialYVal = initialYVal;
    }

    public Module getModule() {
        return module;
    }

    public String getName() {
        return name;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getX() {
        return xPos;
    }

    public void setX(float x) {
        this.xPos = x;
    }

    public float getY() {
        return yPos;
    }

    public void setY(float y) {
        this.yPos = y;
    }


    private String longestModule;

    public final void onDraw(int mouseX, int mouseY) {
        boolean hovering = HoveringUtils.isHovering(xPos, yPos, width, height, mouseX, mouseY);
        if (dragging) {
            xPos = (mouseX - startX);
            yPos = (mouseY - startY);
        }
        hoverAnimation.setDirection(hovering ? Direction.FORWARDS : Direction.BACKWARDS);
        if (!hoverAnimation.isDone() || hoverAnimation.finished(Direction.FORWARDS)) {
            RoundedUtil.drawRoundOutline(xPos, yPos, width, height, 4, 0.5f,
                    RenderUtil.applyOpacity(Color.WHITE, 0), RenderUtil.applyOpacity(Color.WHITE, (float) hoverAnimation.getOutput()));
        }
    }

    public final void onDrawArraylist(OverlayModule arraylistMod, int mouseX, int mouseY) {
        ScaledResolution sr = new ScaledResolution(mc);

        List<Module> modules = Client.INSTANCE.getModuleRepository().getToggledModules(arraylistMod.modules);

        String longest = getLongestModule(arraylistMod);

        width = (float) (arraylistMod.getWidth(longest) + 5);
        height = (float) ((11) * modules.size());

        float textVal = (float) arraylistMod.getWidth(longest);
        float xVal = sr.getScaledWidth() - (textVal + 8 + xPos);

        if (sr.getScaledWidth() - xPos <= sr.getScaledWidth() / 2f) {
            xVal += textVal - 2;
        }

        boolean hovering = HoveringUtils.isHovering(xVal, yPos, width, height, mouseX, mouseY);

        if (dragging) {
            xPos = -(mouseX - startX);
            yPos = (mouseY - startY);
        }
        hoverAnimation.setDirection(hovering ? Direction.FORWARDS : Direction.BACKWARDS);

        if (!hoverAnimation.isDone() || hoverAnimation.finished(Direction.FORWARDS)) {
            RoundedUtil.drawRoundOutline(xVal, yPos - 8, width + 20, height + 16, 10, 0.5f,
                    RenderUtil.applyOpacity(Color.BLACK, (float) (0f * hoverAnimation.getOutput())), RenderUtil.applyOpacity(Color.WHITE, (float) hoverAnimation.getOutput()));
        }
    }

    public final void onClickArraylist(OverlayModule arraylistMod, int mouseX, int mouseY, int button) {
        ScaledResolution sr = new ScaledResolution(mc);

        String longest = getLongestModule(arraylistMod);

        float textVal = (float) arraylistMod.getWidth(longest);
        float xVal = sr.getScaledWidth() - (textVal + 8 + xPos);

        if (sr.getScaledWidth() - xPos <= sr.getScaledWidth() / 2f) {
            xVal += textVal - 16;
        }

        boolean canDrag = HoveringUtils.isHovering(xVal, yPos, width, height, mouseX, mouseY);

        if (button == 0 && canDrag) {
            dragging = true;
            startX = (int) (mouseX + xPos);
            startY = (int) (mouseY - yPos);
        }
    }

    public final void onClick(int mouseX, int mouseY, int button) {
        boolean canDrag = HoveringUtils.isHovering(xPos, yPos, width, height, mouseX, mouseY);
        if (button == 0 && canDrag) {
            dragging = true;
            startX = (int) (mouseX - xPos);
            startY = (int) (mouseY - yPos);
        }
    }

    public final void onRelease(int button) {
        if (button == 0) dragging = false;
    }

    private String getLongestModule(OverlayModule arraylistMod) {
        return arraylistMod.longest;
    }


}