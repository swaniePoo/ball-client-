package me.dinozoid.strife.module.implementations.visuals;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.player.PlayerMotionEvent;
import me.dinozoid.strife.event.implementations.render.Render2DEvent;
import me.dinozoid.strife.event.implementations.render.Render3DEvent;
import me.dinozoid.strife.event.implementations.render.RenderPlayerShaderEvent;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.newshader.ShaderUtil;
import me.dinozoid.strife.property.Property;
import me.dinozoid.strife.property.implementations.ColorProperty;
import me.dinozoid.strife.property.implementations.DoubleProperty;
import me.dinozoid.strife.property.implementations.EnumProperty;
import me.dinozoid.strife.property.implementations.MultiSelectEnumProperty;
import me.dinozoid.strife.shader.ShaderProgram;
import me.dinozoid.strife.util.Animation;
import me.dinozoid.strife.util.DecelerateAnimation;
import me.dinozoid.strife.util.player.PlayerUtil;
import me.dinozoid.strife.util.render.RenderUtil;
import me.dinozoid.strife.util.system.MathUtil;
import me.dinozoid.strife.util.world.WorldUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.awt.*;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glUniform1;

@ModuleInfo(name = "GlowESP", renderName = "GlowESP", category = Category.VISUALS)
public class GlowESPModule extends Module {

    private final MultiSelectEnumProperty<PlayerUtil.Target> targetsProperty = new MultiSelectEnumProperty<>("Targets", PlayerUtil.Target.PLAYERS);
    private final EnumProperty<RenderUtil.ColorMode> colorModeProperty = new EnumProperty<>("Color Mode", RenderUtil.ColorMode.ASTOLFO);
    private final ColorProperty colorProperty = new ColorProperty("EColor", new Color(209, 50, 50), () -> colorModeProperty.getValue() == RenderUtil.ColorMode.STATIC || colorModeProperty.getValue() == RenderUtil.ColorMode.PULSE || colorModeProperty.getValue() == RenderUtil.ColorMode.SWITCH);
    private final ColorProperty secondColorProperty = new ColorProperty("FColor", new Color(29, 205, 200), () -> colorModeProperty.getValue() == RenderUtil.ColorMode.SWITCH);
    private final DoubleProperty radius = new DoubleProperty("Radius", 4, 2, 30, 1);
    private final DoubleProperty exposure = new DoubleProperty("Exposure", 2.2, .5, 3.5, .1);
    private final Property<Boolean> seperate = new Property<>("Seperate Texure", false);

    public static boolean renderNameTags = true;
    private final ShaderUtil outlineShader = new ShaderUtil("fragment/outline.frag");
    private final ShaderUtil glowShader = new ShaderUtil("fragment/glow.frag");

    public Framebuffer framebuffer;
    public Framebuffer outlineFrameBuffer;
    public Framebuffer glowFrameBuffer;
    private final Frustum frustum = new Frustum();

    private List<EntityLivingBase> livingEntities = new ArrayList<>();

    public static Animation fadeIn;

    @Override
    public void onEnable() {
        super.onEnable();
        fadeIn = new DecelerateAnimation(250, 1);
    }

    public void createFrameBuffers() {
        framebuffer = RenderUtil.createFramebuffer(framebuffer, true);
        outlineFrameBuffer = RenderUtil.createFramebuffer(outlineFrameBuffer, true);
        glowFrameBuffer = RenderUtil.createFramebuffer(glowFrameBuffer, true);
    }

    @EventHandler
    private final Listener<Render3DEvent> render3DListener = new Listener<>(event -> {
        createFrameBuffers();
        collectEntities();
        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(true);
        renderEntities(event.getPartialTicks());
        framebuffer.unbindFramebuffer();
        mc.getFramebuffer().bindFramebuffer(true);
        GlStateManager.disableLighting();
    });

    @EventHandler
    private final Listener<Render2DEvent> render2DListener = new Listener<>(event -> {


        ScaledResolution sr = new ScaledResolution(mc);
        if (framebuffer != null && outlineFrameBuffer != null && livingEntities.size() > 0) {
            GlStateManager.enableAlpha();
            GlStateManager.alphaFunc(516, 0.0f);
            GlStateManager.enableBlend();
            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

            outlineFrameBuffer.framebufferClear();
            outlineFrameBuffer.bindFramebuffer(true);
            outlineShader.init();
            setupOutlineUniforms(0, 1);
            RenderUtil.bindTexture(framebuffer.framebufferTexture);
            ShaderUtil.drawQuads();
            outlineShader.init();
            setupOutlineUniforms(1, 0);
            RenderUtil.bindTexture(framebuffer.framebufferTexture);
            ShaderUtil.drawQuads();
            outlineShader.unload();
            outlineFrameBuffer.unbindFramebuffer();

            GlStateManager.color(1, 1, 1, 1);
            glowFrameBuffer.framebufferClear();
            glowFrameBuffer.bindFramebuffer(true);
            glowShader.init();
            setupGlowUniforms(1, 0);
            RenderUtil.bindTexture(outlineFrameBuffer.framebufferTexture);
            ShaderUtil.drawQuads();
            glowShader.unload();
            glowFrameBuffer.unbindFramebuffer();

            mc.getFramebuffer().bindFramebuffer(true);
            glowShader.init();
            setupGlowUniforms(0, 1);
            if (seperate.getValue()) {
                GL13.glActiveTexture(GL13.GL_TEXTURE16);
                RenderUtil.bindTexture(framebuffer.framebufferTexture);
            }
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            RenderUtil.bindTexture(glowFrameBuffer.framebufferTexture);
            ShaderUtil.drawQuads();
            glowShader.unload();

        }
    });

    public void setupGlowUniforms(float dir1, float dir2) {
        Color color = getColor();
        glowShader.setUniformi("texture", 0);
        if (seperate.getValue()) {
            glowShader.setUniformi("textureToCheck", 16);
        }
        glowShader.setUniformf("radius", radius.getValue().floatValue());
        glowShader.setUniformf("texelSize", 1.0f / mc.displayWidth, 1.0f / mc.displayHeight);
        glowShader.setUniformf("direction", dir1, dir2);
        glowShader.setUniformf("color", color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
        glowShader.setUniformf("exposure", (float) (exposure.getValue().floatValue() * fadeIn.getOutput()));
        glowShader.setUniformi("avoidTexture", seperate.getValue() ? 1 : 0);

        final FloatBuffer buffer = BufferUtils.createFloatBuffer(256);
        for (int i = 1; i <= radius.getValue().floatValue(); i++) {
            buffer.put(MathUtil.calculateGaussianValue(i, radius.getValue().floatValue() / 2));
        }
        buffer.rewind();

        glUniform1(glowShader.getUniform("weights"), buffer);
    }


    public void setupOutlineUniforms(float dir1, float dir2) {
        Color color = getColor();
        outlineShader.setUniformi("texture", 0);
        outlineShader.setUniformf("radius", radius.getValue().floatValue() / 1.5f);
        outlineShader.setUniformf("texelSize", 1.0f / mc.displayWidth, 1.0f / mc.displayHeight);
        outlineShader.setUniformf("direction", dir1, dir2);
        outlineShader.setUniformf("color", color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
    }

    public void renderEntities(float ticks) {
        livingEntities.forEach(entity -> {
            renderNameTags = false;
            mc.getRenderManager().renderEntityStaticNoShadow(entity, ticks, false);
            renderNameTags = true;
        });
    }

    private Color getColor() {
        return new Color(RenderUtil.getColor(colorModeProperty, 8, colorProperty.getValue(), secondColorProperty.getValue()));
    }

    public void collectEntities() {
        livingEntities.clear();
        livingEntities = WorldUtil.getLivingEntities(entity -> RenderUtil.isInView(entity) && PlayerUtil.isValid(entity, targetsProperty) || entity == mc.thePlayer && mc.gameSettings.thirdPersonView != 0);
    }
}
