package me.dinozoid.strife.shader.implementations;

import lombok.Getter;
import lombok.Setter;
import me.dinozoid.strife.shader.ShaderProgram;
import me.dinozoid.strife.ui.callback.RenderCallback;
import me.dinozoid.strife.util.MinecraftUtil;
import me.dinozoid.strife.util.render.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.shader.Framebuffer;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class GlowShader extends MinecraftUtil {

    private static final ShaderProgram outlineShader = new ShaderProgram("fragment/glow.frag");
    private static Framebuffer glowBuffer = new Framebuffer(1, 1, false);

    @Getter @Setter private int radius, fade;
    @Getter @Setter private Color color;

    public GlowShader(Color color, int radius, int fade) {
        this.color = color;
        this.radius = radius;
        this.fade = fade;
    }

    public void glow(RenderCallback renderCallback) {
        glPushMatrix();
        final ScaledResolution scaledResolution = new ScaledResolution(mc);
        glowBuffer = RenderUtil.createFramebuffer(glowBuffer);

        glowBuffer.framebufferClear();
        glowBuffer.bindFramebuffer(false);
        renderCallback.onRender();
        glowBuffer.unbindFramebuffer();

        glEnable(GL_BLEND);
        outlineShader.init();
        setupUniforms();
        mc.getFramebuffer().bindFramebuffer(false);
        glBindTexture(GL_TEXTURE_2D, glowBuffer.framebufferTexture);
        outlineShader.renderCanvas(scaledResolution);
        outlineShader.uninit();
        glDisable(GL_BLEND);
        glPopMatrix();
    }

    public void setupUniforms() {
        outlineShader.setUniformi("originalTexture", 0);
        outlineShader.setUniformf("texelSize", 1f / mc.displayWidth, 1f / mc.displayHeight);
        outlineShader.setUniformf("outlineColor", color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
        outlineShader.setUniformi("radius", radius);
        outlineShader.setUniformi("fade", fade);
    }
}
