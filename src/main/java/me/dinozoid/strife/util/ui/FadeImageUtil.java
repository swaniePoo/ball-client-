package me.dinozoid.strife.util.ui;

import me.dinozoid.strife.ui.element.Position;
import me.dinozoid.strife.util.render.RenderUtil;
import net.minecraft.util.ResourceLocation;

public class FadeImageUtil {

    private final ResourceLocation originalImageOne;
    private final ResourceLocation orginalImageTwo;
    private Position position;
    private ResourceLocation imageOne;
    private ResourceLocation imageTwo;
    private float oneOpacity = 255, twoOpacity = 0;

    public FadeImageUtil(ResourceLocation imageOne, ResourceLocation imageTwo) {
        this(new Position(), imageOne, imageTwo);
    }

    public FadeImageUtil(Position position, ResourceLocation imageOne, ResourceLocation imageTwo) {
        this.position = position;
        this.originalImageOne = imageOne;
        this.orginalImageTwo = imageTwo;
        this.imageOne = imageOne;
        this.imageTwo = imageTwo;
    }

    public FadeImageUtil(float x, float y, float width, float height, ResourceLocation imageOne, ResourceLocation imageTwo) {
        this(new Position(x, y, width, height), imageOne, imageTwo);
    }

    public void drawScreen(float mouseX, float mouseY) {
        if (oneOpacity > 0) oneOpacity -= 10;
        else if (twoOpacity < 255) twoOpacity += 10;
        if (oneOpacity > 0)
            RenderUtil.drawImage(imageOne, position.x(), position.y(), position.width(), position.height(), oneOpacity);
        else
            RenderUtil.drawImage(imageTwo, position.x(), position.y(), position.width(), position.height(), twoOpacity);
    }

    public void position(float x, float y, float width, float height) {
        position.position(x, y, width, height);
    }

    public Position position() {
        return position;
    }

    public void position(Position position) {
        this.position = position;
    }

    public ResourceLocation imageOne() {
        return imageOne;
    }

    public void imageOne(ResourceLocation imageOne) {
        this.imageOne = imageOne;
    }

    public ResourceLocation imageTwo() {
        return imageTwo;
    }

    public void imageTwo(ResourceLocation imageTwo) {
        this.imageTwo = imageTwo;
    }

    public float oneOpacity() {
        return oneOpacity;
    }

    public void oneOpacity(float oneOpacity) {
        this.oneOpacity = oneOpacity;
    }

    public float twoOpacity() {
        return twoOpacity;
    }

    public void twoOpacity(float twoOpacity) {
        this.twoOpacity = twoOpacity;
    }
}
