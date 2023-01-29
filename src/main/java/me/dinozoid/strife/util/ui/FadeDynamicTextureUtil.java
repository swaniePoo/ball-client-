package me.dinozoid.strife.util.ui;

import me.dinozoid.strife.ui.element.Position;
import me.dinozoid.strife.util.render.RenderUtil;
import net.minecraft.client.renderer.texture.DynamicTexture;

public class FadeDynamicTextureUtil {

    private final DynamicTexture originalImageOne;
    private final DynamicTexture originalImageTwo;
    private Position position;
    private DynamicTexture imageOne;
    private DynamicTexture imageTwo;
    private float oneOpacity = 255, twoOpacity = 0;

    public FadeDynamicTextureUtil(DynamicTexture imageOne, DynamicTexture imageTwo) {
        this(new Position(), imageOne, imageTwo);
    }

    public FadeDynamicTextureUtil(float x, float y, float width, float height, DynamicTexture imageOne, DynamicTexture imageTwo) {
        this(new Position(x, y, width, height), imageOne, imageTwo);
    }

    public FadeDynamicTextureUtil(Position position, DynamicTexture imageOne, DynamicTexture imageTwo) {
        this.position = position;
        this.originalImageOne = imageOne;
        this.originalImageTwo = imageTwo;
        this.imageOne = imageOne;
        this.imageTwo = imageTwo;
    }

    public void drawScreen() {
        if (imageOne != originalImageOne || imageTwo != originalImageTwo || imageOne != imageTwo) reset();
        if (oneOpacity > 0) oneOpacity -= 10;
        else if (twoOpacity < 255) twoOpacity += 10;
        if (oneOpacity > 0)
            RenderUtil.drawDynamicTexture(imageOne, position.x(), position.y(), position.width(), position.height(), oneOpacity);
        else
            RenderUtil.drawDynamicTexture(imageTwo, position.x(), position.y(), position.width(), position.height(), twoOpacity);
    }

    public void reset() {
        oneOpacity = 255;
        twoOpacity = 0;
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

    public DynamicTexture imageOne() {
        return imageOne;
    }

    public void imageOne(DynamicTexture imageOne) {
        this.imageOne = imageOne;
        reset();
    }

    public DynamicTexture imageTwo() {
        return imageTwo;
    }

    public void imageTwo(DynamicTexture imageTwo) {
        this.imageTwo = imageTwo;
        reset();
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
