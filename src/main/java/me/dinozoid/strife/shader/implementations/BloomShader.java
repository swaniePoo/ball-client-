package me.dinozoid.strife.shader.implementations;

import lombok.Getter;
import lombok.Setter;
import me.dinozoid.strife.shader.ShaderProgram;
import me.dinozoid.strife.util.MinecraftUtil;
import me.dinozoid.strife.util.render.RenderUtil;
import me.dinozoid.strife.util.render.StencilUtil;
import me.dinozoid.strife.util.system.MathUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glUniform1;

public class BloomShader extends MinecraftUtil {

    private ShaderProgram bloomShader = new ShaderProgram("fragment/bloom.frag");

    public Framebuffer framebuffer = new Framebuffer(1, 1, false);
    private Framebuffer bloomFramebuffer = new Framebuffer(1, 1, false);
    private static List<RenderCallback> renders = new ArrayList<>();

    @Getter
    @Setter
    public int sourceTexture, radius, offset;


    public BloomShader(){
    }


    public static void drawAndBloom(RenderCallback render) {
        //render.render();
        renders.add(render);
    }

    public void renderBlur() {
        if(renders.isEmpty()) return;

        for (RenderCallback callback : renders) {
            callback.render();
        }

//        bloomFramebuffer = RenderUtil.createFramebuffer(bloomFramebuffer);
//        bloomFramebuffer.framebufferClear();
//        bloomFramebuffer.bindFramebuffer(true);

        renders.clear();


        bloomFramebuffer.unbindFramebuffer();

        framebuffer = RenderUtil.createFramebuffer(framebuffer);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.0f);
        GlStateManager.enableBlend();
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        final FloatBuffer weightBuffer = BufferUtils.createFloatBuffer(256);
        for (int i = 0; i <= radius; i++) {
            weightBuffer.put(MathUtil.calculateGaussianValue(i, radius));
        }
        weightBuffer.rewind();

        RenderUtil.setAlphaLimit(0.0F);

        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(true);
        bloomShader.init();
        setupUniforms(radius, offset, 0, weightBuffer);
        RenderUtil.bindTexture(sourceTexture);
        bloomShader.renderCanvas(new ScaledResolution(mc));
        bloomShader.uninit();
        framebuffer.unbindFramebuffer();


        mc.getFramebuffer().bindFramebuffer(true);

        bloomShader.init();
        setupUniforms(radius, 0, offset, weightBuffer);
        GL13.glActiveTexture(GL13.GL_TEXTURE16);
        RenderUtil.bindTexture(sourceTexture);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        RenderUtil.bindTexture(framebuffer.framebufferTexture);
        bloomShader.renderCanvas(new ScaledResolution(mc));
        bloomShader.uninit();

        GlStateManager.alphaFunc(516, 0.1f);
        GlStateManager.enableAlpha();

        GlStateManager.bindTexture(0);
    }

    public void setupUniforms(int radius, int directionX, int directionY, FloatBuffer weights) {
        bloomShader.setUniformi("inTexture", 0);
        bloomShader.setUniformi("textureToCheck", 16);
        bloomShader.setUniformf("radius", radius);
        bloomShader.setUniformf("texelSize", 1.0F / (float) mc.displayWidth, 1.0F / (float) mc.displayHeight);
        bloomShader.setUniformf("direction", directionX, directionY);
        glUniform1(bloomShader.getUniform("weights"), weights);
    }
}
