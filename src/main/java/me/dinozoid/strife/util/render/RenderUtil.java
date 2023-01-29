package me.dinozoid.strife.util.render;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.module.implementations.misc.StreamerModeModule;
import me.dinozoid.strife.module.implementations.visuals.OverlayModule;
import me.dinozoid.strife.property.implementations.EnumProperty;
import me.dinozoid.strife.util.MinecraftUtil;
import me.dinozoid.strife.util.player.PlayerUtil;
import me.dinozoid.strife.websocket.user.User;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL14;
import org.lwjgl.util.glu.GLU;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.util.ConcurrentModificationException;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.glBlendFuncSeparate;

public class RenderUtil extends MinecraftUtil {

    private static StreamerModeModule streamerMode;

    private static final Frustum frustum = new Frustum();

    public static void resetColor() {
        GlStateManager.color(1, 1, 1, 1);
    }

    public static void glRestoreBlend(final boolean wasEnabled) {
        if (!wasEnabled) {
            glDisable(GL_BLEND);
        }
    }

    public static void scaleStart(float x, float y, float scale) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(scale, scale, 1);
        GlStateManager.translate(-x, -y, 0);
    }

    public static void scaleEnd() {
        GlStateManager.popMatrix();
    }

    public static int applyOpacity(int color, float opacity) {
        Color old = new Color(color);
        return applyOpacity(old, opacity).getRGB();
    }

    //Opacity value ranges from 0-1
    public static Color applyOpacity(Color color, float opacity) {
        opacity = Math.min(1, Math.max(0, opacity));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * opacity));
    }

    public static Color getAverageColor(BufferedImage bi, int width, int height, int pixelStep) {

        int[] color = new int[3];
        for (int x = 0; x < width; x += pixelStep) {
            for (int y = 0; y < height; y += pixelStep) {
                Color pixel = new Color(bi.getRGB(x, y));
                color[0] += pixel.getRed();
                color[1] += pixel.getGreen();
                color[2] += pixel.getBlue();
            }
        }
        int num = width * height;
        return new Color(color[0] / num, color[1] / num, color[2] / num);
    }

    public static void renderBreadCrumbs(final List<Vec3> vec3s) {

        GlStateManager.disableDepth();
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glEnable(GL_LINE_SMOOTH);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        int i = 0;
        try {
            for (final Vec3 v : vec3s) {

                i++;

                boolean draw = true;

                final double x = v.xCoord - (mc.getRenderManager()).renderPosX;
                final double y = v.yCoord - (mc.getRenderManager()).renderPosY;
                final double z = v.zCoord - (mc.getRenderManager()).renderPosZ;

                final double distanceFromPlayer = mc.thePlayer.getDistance(v.xCoord, v.yCoord - 1, v.zCoord);
                int quality = (int) (distanceFromPlayer * 4 + 10);

                if (quality > 350)
                    quality = 350;

                if (i % 10 != 0 && distanceFromPlayer > 25) {
                    draw = false;
                }

                if (i % 3 == 0 && distanceFromPlayer > 15) {
                    draw = false;
                }

                if (draw) {

                    glPushMatrix();
                    glTranslated(x, y, z);

                    final float scale = 0.04f;
                    glScalef(-scale, -scale, -scale);

                    glRotated(-(mc.getRenderManager()).playerViewY, 0.0D, 1.0D, 0.0D);
                    glRotated((mc.getRenderManager()).playerViewX, 1.0D, 0.0D, 0.0D);

                    final Color c = new Color(OverlayModule.getColor(0));

                    RenderUtil.drawFilledCircleNoGL(0, 0, 0.7, c.hashCode(), quality);

                    if (distanceFromPlayer < 4)
                        RenderUtil.drawFilledCircleNoGL(0, 0, 1.4, new Color(c.getRed(), c.getGreen(), c.getBlue(), 50).hashCode(), quality);

                    if (distanceFromPlayer < 20)
                        RenderUtil.drawFilledCircleNoGL(0, 0, 2.3, new Color(c.getRed(), c.getGreen(), c.getBlue(), 30).hashCode(), quality);

                    glScalef(0.8f, 0.8f, 0.8f);

                    glPopMatrix();

                }

            }
        } catch (final ConcurrentModificationException ignored) {
        }

        glDisable(GL_LINE_SMOOTH);
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        GlStateManager.enableDepth();

        glColor3d(255, 255, 255);
    }

    public static void renderBreadCrumb(final Vec3 vec3) {

        GlStateManager.disableDepth();
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glEnable(GL_LINE_SMOOTH);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        try {

            final double x = vec3.xCoord - (mc.getRenderManager()).renderPosX;
            final double y = vec3.yCoord - (mc.getRenderManager()).renderPosY;
            final double z = vec3.zCoord - (mc.getRenderManager()).renderPosZ;

            final double distanceFromPlayer = mc.thePlayer.getDistance(vec3.xCoord, vec3.yCoord - 1, vec3.zCoord);
            int quality = (int) (distanceFromPlayer * 4 + 10);

            if (quality > 350)
                quality = 350;

            glPushMatrix();
            glTranslated(x, y, z);

            final float scale = 0.04f;
            glScalef(-scale, -scale, -scale);

            glRotated(-(mc.getRenderManager()).playerViewY, 0.0D, 1.0D, 0.0D);
            glRotated((mc.getRenderManager()).playerViewX, 1.0D, 0.0D, 0.0D);

            final Color c = new Color(OverlayModule.getColor(0));

            drawFilledCircleNoGL(0, 0, 0.7, c.hashCode(), quality);

            if (distanceFromPlayer < 4)
                drawFilledCircleNoGL(0, 0, 1.4, new Color(c.getRed(), c.getGreen(), c.getBlue(), 50).hashCode(), quality);

            if (distanceFromPlayer < 20)
                drawFilledCircleNoGL(0, 0, 2.3, new Color(c.getRed(), c.getGreen(), c.getBlue(), 30).hashCode(), quality);


            glScalef(0.8f, 0.8f, 0.8f);

            glPopMatrix();


        } catch (final ConcurrentModificationException ignored) {
        }

        glDisable(GL_LINE_SMOOTH);
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        GlStateManager.enableDepth();

        glColor3d(255, 255, 255);
    }



    public static void drawFilledCircleNoGL(final int x, final int y, final double r, final int c, final int quality) {
        final float f = ((c >> 24) & 0xff) / 255F;
        final float f1 = ((c >> 16) & 0xff) / 255F;
        final float f2 = ((c >> 8) & 0xff) / 255F;
        final float f3 = (c & 0xff) / 255F;

        glColor4f(f1, f2, f3, f);
        glBegin(GL_TRIANGLE_FAN);

        for (int i = 0; i <= 360 / quality; i++) {
            final double x2 = Math.sin(((i * quality * Math.PI) / 180)) * r;
            final double y2 = Math.cos(((i * quality * Math.PI) / 180)) * r;
            glVertex2d(x + x2, y + y2);
        }

        glEnd();
    }

    public static void glDrawBoundingBox(final AxisAlignedBB bb,
                                         final float lineWidth,
                                         final boolean filled) {
        if (filled) {
            // 4 sides
            glBegin(GL_QUAD_STRIP);
            {
                glVertex3d(bb.minX, bb.minY, bb.minZ);
                glVertex3d(bb.minX, bb.maxY, bb.minZ);

                glVertex3d(bb.maxX, bb.minY, bb.minZ);
                glVertex3d(bb.maxX, bb.maxY, bb.minZ);

                glVertex3d(bb.maxX, bb.minY, bb.maxZ);
                glVertex3d(bb.maxX, bb.maxY, bb.maxZ);

                glVertex3d(bb.minX, bb.minY, bb.maxZ);
                glVertex3d(bb.minX, bb.maxY, bb.maxZ);

                glVertex3d(bb.minX, bb.minY, bb.minZ);
                glVertex3d(bb.minX, bb.maxY, bb.minZ);
            }
            glEnd();

            // Bottom
            glBegin(GL_QUADS);
            {
                glVertex3d(bb.minX, bb.minY, bb.minZ);
                glVertex3d(bb.maxX, bb.minY, bb.minZ);
                glVertex3d(bb.maxX, bb.minY, bb.maxZ);
                glVertex3d(bb.minX, bb.minY, bb.maxZ);
            }
            glEnd();

            glCullFace(GL_FRONT);

            // Top
            glBegin(GL_QUADS);
            {
                glVertex3d(bb.minX, bb.maxY, bb.minZ);
                glVertex3d(bb.maxX, bb.maxY, bb.minZ);
                glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
                glVertex3d(bb.minX, bb.maxY, bb.maxZ);
            }
            glEnd();

            glCullFace(GL_BACK);
        }


        if (lineWidth > 0) {
            glLineWidth(lineWidth);

            glEnable(GL_LINE_SMOOTH);
            glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);

            glBegin(GL_LINE_STRIP);
            {
                // Bottom
                glVertex3d(bb.minX, bb.minY, bb.minZ);
                glVertex3d(bb.maxX, bb.minY, bb.minZ);
                glVertex3d(bb.maxX, bb.minY, bb.maxZ);
                glVertex3d(bb.minX, bb.minY, bb.maxZ);
                glVertex3d(bb.minX, bb.minY, bb.minZ);

                // Top
                glVertex3d(bb.minX, bb.maxY, bb.minZ);
                glVertex3d(bb.maxX, bb.maxY, bb.minZ);
                glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
                glVertex3d(bb.minX, bb.maxY, bb.maxZ);
                glVertex3d(bb.minX, bb.maxY, bb.minZ);
            }
            glEnd();

            glBegin(GL_LINES);
            {
                glVertex3d(bb.maxX, bb.minY, bb.minZ);
                glVertex3d(bb.maxX, bb.maxY, bb.minZ);

                glVertex3d(bb.maxX, bb.minY, bb.maxZ);
                glVertex3d(bb.maxX, bb.maxY, bb.maxZ);

                glVertex3d(bb.minX, bb.minY, bb.maxZ);
                glVertex3d(bb.minX, bb.maxY, bb.maxZ);
            }
            glEnd();

            glDisable(GL_LINE_SMOOTH);
            glHint(GL_LINE_SMOOTH_HINT, GL_DONT_CARE);
        }
    }

    public static boolean glEnableBlend() {
        final boolean wasEnabled = glIsEnabled(GL_BLEND);

        if (!wasEnabled) {
            glEnable(GL_BLEND);
            glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        }

        return wasEnabled;
    }


    public static Color interpolateColorC(Color color1, Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));
        return new Color(interpolateInt(color1.getRed(), color2.getRed(), amount),
                interpolateInt(color1.getGreen(), color2.getGreen(), amount),
                interpolateInt(color1.getBlue(), color2.getBlue(), amount),
                interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
    }

    public static int interpolateInt(int oldValue, int newValue, double interpolationValue){
        return interpolate2(oldValue, newValue, (float) interpolationValue).intValue();
    }

    public static Double interpolate2(double oldValue, double newValue, double interpolationValue){
        return (oldValue + (newValue - oldValue) * interpolationValue);
    }


    public enum ColorMode {
        ASTOLFO, PULSE, RAINBOW, STATIC, SWITCH, SYNC
    }
    public static void setAlphaLimit(float limit) {
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL_GREATER, (float) (limit * .01));
    }


    public static void bindTexture(int texture) {
        glBindTexture(GL_TEXTURE_2D, texture);
    }

    public static boolean isEntityInFrustum(final Entity entity) {
        return (isBoxInFrustrum(entity.getEntityBoundingBox()) || entity.ignoreFrustumCheck);
    }

    public static boolean isInView(Entity ent) {
        frustum.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);
        return frustum.isBoundingBoxInFrustum(ent.getEntityBoundingBox()) || ent.ignoreFrustumCheck;
    }

    private static boolean isBoxInFrustrum(final AxisAlignedBB bb) {
        final Entity current = mc.getRenderViewEntity();
        frustum.setPosition(current.posX, current.posY, current.posZ);
        return frustum.isBoundingBoxInFrustum(bb);
    }

    public static boolean isVecInFrustrum(Vec3 vec) {
        Entity current = mc.getRenderViewEntity();
        frustum.setPosition(current.posX, current.posY, current.posZ);
        return frustum.isBoxInFrustum(vec.xCoord, vec.yCoord, vec.zCoord, vec.xCoord, vec.yCoord, vec.zCoord);
    }

    public static <Type extends EnumProperty<ColorMode>> int getColor(Type colorModeProperty, int index, Color color, Color color2) {
        switch (colorModeProperty.getValue()) {
            case PULSE:
                return fade(color, -8000, index * -5);
            case ASTOLFO:
                return astolfo(4, 0.5f, 1f, index);
            case RAINBOW:
                return rainbow(4, 0.4f, 0.8f, index);
            case SWITCH:
                return colorSwitch(color2, color, 2000, -index / 40, 75, 2);
            case SYNC:
                return OverlayModule.getColor(index);
        }
        return color.getRGB();
    }

    public static void drawImage(float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight) {
        float f = 1.0F / textureWidth;
        float f1 = 1.0F / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0.0F).tex(u * f, (v + height) * f1).endVertex();
        worldrenderer.pos((x + width), y + height, 0.0F).tex((u + width) * f, (v + height) * f1).endVertex();
        worldrenderer.pos((x + width), y, 0.0F).tex((u + width) * f, v * f1).endVertex();
        worldrenderer.pos(x, y, 0.0F).tex(u * f, v * f1).endVertex();
        tessellator.draw();
    }

    public static void drawRoundedRect(float left, float top, float right, float bottom, float radius, int points, int color) {
        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;

        if (left < right) left = left + right - (right = left);
        if (top < bottom) top = top + bottom - (bottom = top);

        float[][] corners = {
                {right + radius, top - radius, 270},
                {left - radius, top - radius, 360},
                {left - radius, bottom + radius, 90},
                {right + radius, bottom + radius, 180}};

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.alphaFunc(516, 0.003921569F);
        GlStateManager.color(f, f1, f2, f3);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        renderer.begin(GL_POLYGON, DefaultVertexFormats.POSITION);
        for (float[] c : corners) {
            for (int i = 0; i <= points; i++) {
                double anglerad = (Math.PI * (c[2] + i * 90.0F / points) / 180.0f);
                renderer.pos(c[0] + (Math.sin(anglerad) * radius), c[1] + (Math.cos(anglerad) * radius), 0).endVertex();
            }
        }

        tessellator.draw();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }

    public static void drawShadedRoundedRect(float left, float top, float right, float bottom, float radius, int points, int color) {
        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;

        if (left < right) {
            float i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            float j = top;
            top = bottom;
            bottom = j;
        }

        float[][] corners = {
                {left - radius, top - radius},
                {left - radius, bottom + radius},
                {right + radius, bottom + radius},
                {right + radius, top - radius}};

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        GlStateManager.shadeModel(7425);

        renderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        renderer.pos((double) left - radius, (double) bottom + radius, 0.0D).color(f, f1, f2, f3).endVertex();
        renderer.pos((double) right + radius, (double) bottom + radius, 0.0D).color(f, f1, f2, f3).endVertex();
        renderer.pos((double) right + radius, (double) top - radius, 0.0D).color(f, f1, f2, f3).endVertex();
        renderer.pos((double) left - radius, (double) top - radius, 0.0D).color(f, f1, f2, f3).endVertex();
        tessellator.draw();

        renderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        renderer.pos((double) left - radius, bottom, 0.0D).color(f, f1, f2, 0f).endVertex();
        renderer.pos((double) right + radius, bottom, 0.0D).color(f, f1, f2, 0f).endVertex();
        renderer.pos((double) right + radius, (double) bottom + radius, 0.0D).color(f, f1, f2, f3).endVertex();
        renderer.pos((double) left - radius, (double) bottom + radius, 0.0D).color(f, f1, f2, f3).endVertex();
        tessellator.draw();

        renderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        renderer.pos((double) left - radius, (double) top - radius, 0.0D).color(f, f1, f2, f3).endVertex();
        renderer.pos((double) right + radius, (double) top - radius, 0.0D).color(f, f1, f2, f3).endVertex();
        renderer.pos((double) right + radius, top, 0.0D).color(f, f1, f2, 0f).endVertex();
        renderer.pos((double) left - radius, top, 0.0D).color(f, f1, f2, 0f).endVertex();
        tessellator.draw();


        //left right side
        renderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        renderer.pos(left, (double) bottom + radius, 0.0D).color(f, f1, f2, 0f).endVertex();
        renderer.pos((double) left - radius, (double) bottom + radius, 0.0D).color(f, f1, f2, f3).endVertex();
        renderer.pos((double) left - radius, (double) top - radius, 0.0D).color(f, f1, f2, f3).endVertex();
        renderer.pos(left, (double) top - radius, 0.0D).color(f, f1, f2, 0f).endVertex();
        tessellator.draw();

        renderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        renderer.pos((double) right + radius, (double) bottom + radius, 0.0D).color(f, f1, f2, f3).endVertex();
        renderer.pos(right, (double) bottom + radius, 0.0D).color(f, f1, f2, 0f).endVertex();
        renderer.pos(right, (double) top - radius, 0.0D).color(f, f1, f2, 0f).endVertex();
        renderer.pos((double) right + radius, (double) top - radius, 0.0D).color(f, f1, f2, f3).endVertex();
        tessellator.draw();

        float angle = 0f;
        for (float[] c : corners) {
            renderer.begin(GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
            renderer.pos(c[0], c[1], 0).color(f, f1, f2, f3).endVertex();
            for (int i = 0; i <= points; i++) {
                double anglerad = (Math.PI * angle / 180.0f);
                double pointX = (Math.sin(anglerad) * radius);
                double pointY = (Math.cos(anglerad) * radius);
                renderer.pos(c[0] + pointX, c[1] + pointY, 0).color(f, f1, f2, 0f).endVertex();
                angle += 90f / points;
            }
            //renderer.pos(c[0], c[1], 0).endVertex();
            tessellator.draw();
            angle -= 90f / points;
        }
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawHorizontalLine(float startX, float endX, float y, float thickness, int color) {
        if (endX < startX) {
            float i = startX;
            startX = endX;
            endX = i;
        }
        drawRect(startX, y - thickness / 2, endX, y + thickness / 2, color);
    }

    public static void drawVerticalLine(float x, float startY, float endY, float thickness, int color) {
        if (endY < startY) {
            float i = startY;
            startY = endY;
            endY = i;
        }

        drawRect(x - thickness / 2, startY, x + thickness / 2, endY, color);
    }

    public static void circleStrokeAesthetic(float x, float y, float radius, float start, float end, float thicc, Color color, int factors) {
        //Dont use tesselator doesnt support float.
        for (int ie = 0; ie < thicc; ie += factors) {
            GlStateManager.disableTexture2D();
            glEnable(GL_BLEND);
            GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
            glEnable(GL_LINE_SMOOTH);
            glLineWidth(factors * 2f);
            glBegin(GL_LINE_STRIP);
            //Dont question the retarded code, its to get rid of stroke unevenness
            glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
            for (float i = start; i <= end; i += (360 / 180f)) {
                glVertex2f((float) (x + (Math.cos(i * Math.PI / 180) * (radius))), (float) (y + (Math.sin(i * Math.PI / 180) * (radius))));
            }
            radius++;
            glEnd();
            glDisable(GL_LINE_SMOOTH);
            GlStateManager.enableTexture2D();
        }
    }

    public static void drawCircleWithPoints(float x, float y, float radius, float start, float end, Color color) {
        //Dont use tesselator doesnt support float.
        GlStateManager.disableTexture2D();
        glEnable(GL_BLEND);
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        glEnable(GL_POINT_SMOOTH);
        glPointSize(5f);
        glBegin(GL_POINTS);
        //Dont question the retarded code, its to get rid of stroke uneveness
        glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        for (float i = start; i <= end; i += (360 / 30f)) {
            glVertex2f((float) (x + (Math.cos(i * Math.PI / 180) * (radius))), (float) (y + (Math.sin(i * Math.PI / 180) * (radius))));
        }
        radius++;
        glEnd();
        glDisable(GL_POINT_SMOOTH);
        GlStateManager.enableTexture2D();

    }

    public static void quadsCircleStroke(float x, float y, float radius, float start, float end, float thicc, Color colore) {
        int color = colore.getRGB();
        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();

        GlStateManager.disableTexture2D();
        glEnable(GL_BLEND);
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        GlStateManager.color(f, f1, f2, f3);
        glEnable(GL_POLYGON_SMOOTH);
        double posX = 0, posY = 0;
        renderer.begin(GL_QUAD_STRIP, DefaultVertexFormats.POSITION);
        for (float i = start; i <= end; i += (360 / 180f)) {
            double anglerad = (i - 90) * Math.PI / 180d;
            posX = x + (Math.sin(anglerad) * (radius));
            posY = y + (Math.cos(anglerad) * (radius));

            renderer.pos(posX, posY, 0).endVertex();

            posX = x + (Math.sin(anglerad) * (radius + thicc));
            posY = y + (Math.cos(anglerad) * (radius + thicc));
            renderer.pos(posX, posY, 0).endVertex();
        }
        tessellator.draw();
    }

    public static void drawLine(double x, double y, double x1, double y1, float lineWidth, int color) {
        glPushMatrix();
        glEnable(GL_BLEND);
        glEnable(GL_LINE_SMOOTH);
        glDisable(GL_TEXTURE_2D);
        color(color);
        glLineWidth(lineWidth);
        glBegin(GL_LINE_LOOP);
        glVertex2d(x, y);
        glVertex2d(x1, y1);
        glEnd();
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
        glEnable(GL_TEXTURE_2D);
        glPopMatrix();
    }

    public static void pre3D() {
        glPushMatrix();
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glShadeModel(GL_SMOOTH);
        glDisable(GL_TEXTURE_2D);
        glEnable(GL_LINE_SMOOTH);
        glDisable(GL_LIGHTING);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
    }

    public static void post3D() {
        glDisable(GL_LINE_SMOOTH);
        glEnable(GL_TEXTURE_2D);
        glShadeModel(GL_FLAT);
        glDisable(GL_BLEND);
        glPopMatrix();
        glColor4f(1, 1, 1, 1);
    }

    public static void cropBox(float x, float y, float width, float height) {
        final ScaledResolution scale = new ScaledResolution(mc);
        int factor = scale.getScaleFactor();
        glScissor((int) (x * factor), (int) ((scale.getScaledHeight() - height) * factor), (int) ((width - x) * factor), (int) ((height - y) * factor));
    }

    public static void makeCropBox(float left, float top, float right, float bottom) {
        glPushMatrix();
        glEnable(GL_SCISSOR_TEST);
        cropBox(left, top, right, bottom);
    }

    public static void cropBoxIgnoreFactor(float x, float y, float x2, float y2) {
        final ScaledResolution scale = new ScaledResolution(mc);
        glScissor((int) (x), (int) ((scale.getScaledHeight() - y2)), (int) ((x2 - x)), (int) ((y2 - y)));
    }

    public static void makeCropBoxIgnoreFactor(int left, int top, int right, int bottom) {
        glPushMatrix();
        glEnable(GL_SCISSOR_TEST);
        cropBoxIgnoreFactor(left, top, right, bottom);
    }

    public static void destroyCropBox() {
        glDisable(GL_SCISSOR_TEST);
        glPopMatrix();
    }

    public static void drawCircleProgress(float x, float y, float radius, float progress, float thicc, Color color, Color backgroundColor, int mode, int factors) {
        switch (mode) {
            case 0:
                circleStrokeAesthetic(x, y, radius, 105, 435, thicc, backgroundColor, factors);
                circleStrokeAesthetic(x, y, radius, 105, 105 + (330 * progress), thicc, color, factors);
                break;
            case 1:
                quadsCircleStroke(x, y, radius, 105, 435, thicc, backgroundColor);
                quadsCircleStroke(x, y, radius, 105, 105 + (330 * progress), thicc, color);
                break;
            case 2:
                drawCircleWithPoints(x, y, radius, 105, 435, backgroundColor);
                drawCircleWithPoints(x, y, radius, 105, 105 + (330 * progress), color);
                break;
        }
    }

    public static int rainbow(float seconds, float saturation, float brightness, long index) {
        float hue = ((System.currentTimeMillis() + index) % (int) (seconds * 1000)) / (seconds * 1000);
        return Color.HSBtoRGB(hue, saturation, brightness);
    }

    public static int astolfo(float seconds, float saturation, float brightness, float index) {
        float speed = 3000f;
        float hue = (System.currentTimeMillis() % (int) (seconds * 1000)) + index;
        while (hue > speed)
            hue -= speed;
        hue /= speed;
        if (hue > 0.5)
            hue = 0.5F - (hue - 0.5f);
        hue += 0.5F;
        return Color.HSBtoRGB(hue, saturation, brightness);
    }

    public static int fade(Color color, int count, int index) {
        float[] hsb = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        float brightness = Math.abs((System.currentTimeMillis() % 2000L / 1000.0f + index / (float) count * 2.0f) % 2.0f - 1.0f);
        brightness = 0.5f + 0.5f * brightness;
        return Color.HSBtoRGB(hsb[0], hsb[1], brightness % 2.0f);
    }

    public static int colorSwitch(Color firstColor, Color secondColor, float time, int index, long timePerIndex, double speed) {
        return colorSwitch(firstColor, secondColor, time, index, timePerIndex, speed, 255);
    }

    public static int colorSwitch(Color firstColor, Color secondColor, float time, int index, long timePerIndex, double speed, double alpha) {
        long now = (long) (speed * System.currentTimeMillis() + index * timePerIndex);

        float redDiff = (firstColor.getRed() - secondColor.getRed()) / time;
        float greenDiff = (firstColor.getGreen() - secondColor.getGreen()) / time;
        float blueDiff = (firstColor.getBlue() - secondColor.getBlue()) / time;
        int red = Math.round(secondColor.getRed() + redDiff * (now % (long) time));
        int green = Math.round(secondColor.getGreen() + greenDiff * (now % (long) time));
        int blue = Math.round(secondColor.getBlue() + blueDiff * (now % (long) time));

        float redInverseDiff = (secondColor.getRed() - firstColor.getRed()) / time;
        float greenInverseDiff = (secondColor.getGreen() - firstColor.getGreen()) / time;
        float blueInverseDiff = (secondColor.getBlue() - firstColor.getBlue()) / time;
        int inverseRed = Math.round(firstColor.getRed() + redInverseDiff * (now % (long) time));
        int inverseGreen = Math.round(firstColor.getGreen() + greenInverseDiff * (now % (long) time));
        int inverseBlue = Math.round(firstColor.getBlue() + blueInverseDiff * (now % (long) time));

        if (now % ((long) time * 2) < (long) time)
            return new Color(inverseRed, inverseGreen, inverseBlue, (int) alpha).getRGB();
        else return new Color(red, green, blue, (int) alpha).getRGB();
    }

    public static void drawWaveString(String str, float x, float y) {
        float posX = x;
        for (int i = 0; i < str.length(); i++) {
            String ch = str.charAt(i) + "";
            mc.fontRendererObj.drawStringWithShadow(ch, (int) posX, (int) y, OverlayModule.getColor(i * 200));
            posX += mc.fontRendererObj.getStringWidth(ch);
        }
    }

    public static float animate(double target, double current, double speed) {
        boolean larger = (target > current);
        if (speed < 0.0F) speed = 0.0F;
        else if (speed > 1.0F) speed = 1.0F;
        double dif = Math.abs(current - target);
        double factor = dif * speed;
//        if (factor < 0.1f) factor = 0.1F;
        if (larger) current += factor;
        else current -= factor;
        return (float) current;
    }

    public static float animate(float target, float current, float speed) {
        boolean larger = (target > current);
        if (speed < 0.0f) speed = 0.0f;
        else if (speed > 1.0f) speed = 1.0f;
        float dif = Math.abs(current - target);
        float factor = dif * speed;
//        if (factor < 0.1f) factor = 0.1f;
        if (larger) current += factor;
        else current -= factor;
        return current;
    }

    public static double interpolate(double current, double old, double scale) {
        return old + (current - old) * scale;
    }

    public static float interpolateFloat(float current, float old, float scale) {
        return old + (current - old) * scale;
    }

    public static double[] project2D(final double x, final double y, final double z) {
        FloatBuffer objectPosition = ActiveRenderInfo.objectCoords();
        ScaledResolution sc = new ScaledResolution(mc);
        if (GLU.gluProject((float)x, (float)y, (float)z, ActiveRenderInfo.modelview(), ActiveRenderInfo.projection(), ActiveRenderInfo.viewport(), objectPosition))
            return new double[]{ objectPosition.get(0) / sc.getScaleFactor(), objectPosition.get(1) / sc.getScaleFactor(),
                    objectPosition.get(2) };
        return null;
    }

    public static boolean isHovered(float x, float y, float w, float h, int mouseX, int mouseY) {
        return (mouseX >= x && mouseX <= (x + w) && mouseY >= y && mouseY <= (y + h));
    }

    public static boolean inBounds(float x, float y, float w, float h, int mouseX, int mouseY) {
        return (mouseX >= x && mouseX <= w && mouseY >= y && mouseY <= h);
    }

    public static void color(Color color) {
        GlStateManager.color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
    }

    public static void color(Color color, float alpha) {
        GlStateManager.color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, alpha / 255f);
    }

    public static float[] convertRGB(int rgb) {
        float a = (rgb >> 24 & 0xFF) / 255.0f;
        float r = (rgb >> 16 & 0xFF) / 255.0f;
        float g = (rgb >> 8 & 0xFF) / 255.0f;
        float b = (rgb & 0xFF) / 255.0f;
        return new float[]{r, g, b, a};
    }

    public static float[] convertHSB(int hsb) {
        float r = (hsb >> 16) & 0xFF;
        float g = (hsb >> 8) & 0xFF;
        float b = hsb & 0xFF;
        return new float[]{r, g, b};
    }

    public static Color toColorRGB(int rgb, float alpha) {
        float[] rgba = convertRGB(rgb);
        return new Color(rgba[0], rgba[1], rgba[2], alpha / 255f);
    }

    public static void color(int color) {
        float[] rgba = convertRGB(color);
        glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    public static void color(int color, int alpha) {
        float[] rgba = convertRGB(color);
        glColor4f(rgba[0], rgba[1], rgba[2], alpha / 255f);
    }

    public static Color brighter(Color color, float factor) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int alpha = color.getAlpha();
        int i = (int) (1.0 / (1.0 - factor));
        if (r == 0 && g == 0 && b == 0) return new Color(i, i, i, alpha);
        if (r > 0 && r < i) r = i;
        if (g > 0 && g < i) g = i;
        if (b > 0 && b < i) b = i;
        return new Color(Math.min((int) (r / factor), 255), Math.min((int) (g / factor), 255), Math.min((int) (b / factor), 255), alpha);
    }

    public static Color darker(Color color, float factor) {
        return new Color(Math.max((int) (color.getRed() * factor), 0), Math.max((int) (color.getGreen() * factor), 0), Math.max((int) (color.getBlue() * factor), 0), color.getAlpha());
    }

    public static int darker(final int color, final float factor) {
        final int r = (int) ((color >> 16 & 0xFF) * factor);
        final int g = (int) ((color >> 8 & 0xFF) * factor);
        final int b = (int) ((color & 0xFF) * factor);
        final int a = color >> 24 & 0xFF;
        return (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF) | (a & 0xFF) << 24;
    }

    public static void drawImage(ResourceLocation image, float x, float y, float width, float height) {
        drawImage(image, x, y, width, height, 255);
    }

    public static void drawImage(ResourceLocation image, float x, float y, float width, float height, float opacity) {
        glPushMatrix();
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glDepthMask(false);
        glColor4f(1, 1, 1, opacity / 255);
        mc.getTextureManager().bindTexture(image);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0f, 0.0f, width, height, width, height);
        GlStateManager.color(1, 1, 1, 1);
        glDepthMask(true);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
        glPopMatrix();
    }

    public static Framebuffer createFramebuffer(Framebuffer framebuffer) {
        return createFramebuffer(framebuffer, false);
    }

    public static Framebuffer createFramebuffer(Framebuffer framebuffer, boolean depth) {
        if (framebuffer == null || framebuffer.framebufferWidth != mc.displayWidth || framebuffer.framebufferHeight != mc.displayHeight) {
            if (framebuffer != null) {
                framebuffer.deleteFramebuffer();
            }
            return new Framebuffer(mc.displayWidth, mc.displayHeight, depth);
        }
        return framebuffer;
    }

    public static void drawImageWithTint(ResourceLocation image, float x, float y, float width, float height, Color color) {
        glPushMatrix();
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glDepthMask(false);
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        mc.getTextureManager().bindTexture(image);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0f, 0.0f, width, height, width, height);
        GlStateManager.color(1, 1, 1, 1);
        glDepthMask(true);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
        glPopMatrix();
    }

    public static void drawDynamicTexture(DynamicTexture texture, float x, float y, float width, float height) {
        drawDynamicTexture(texture, x, y, width, height, 255);
    }

    public static void drawDynamicTexture(DynamicTexture texture, float x, float y, float width, float height, float opacity) {
        glPushMatrix();
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glDepthMask(false);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        glColor4f(1, 1, 1, opacity / 255);
        DynamicTextureUtil.bindTexture(texture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0f, 0.0f, width, height, width, height);
        glDepthMask(true);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
        glPopMatrix();
    }

    public static void drawRect(float x, float y, float width, float height, int color) {
        glPushMatrix();
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);
        glPushMatrix();
        color(color);
        glBegin(GL_QUADS);
        glVertex2d(width, y);
        glVertex2d(x, y);
        glVertex2d(x, height);
        glVertex2d(width, height);
        glEnd();
        glPopMatrix();
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
        glPopMatrix();
    }

    public static void drawGlowingRect(float x, float y, float width, float height, int color) {
        glPushMatrix();
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);
        glPushMatrix();
        color(color);
        glShadeModel(GL_SMOOTH);
        glBegin(GL_QUADS);
        glVertex2d(width, y);
        glVertex2d(x, y);
        glColor4f(0, 0, 0, 0);
        glVertex2d(x, height * 2);
        glVertex2d(width, height * 2);
        glEnd();
        glShadeModel(GL_FLAT);
        glPopMatrix();
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
        glPopMatrix();
    }

    public static void drawGradientRect(float x, float y, float width, float height, int firstColor, int secondColor, boolean perpendicular) {
        glPushMatrix();
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);
        glPushMatrix();
        glShadeModel(GL_SMOOTH);
        glBegin(GL_QUADS);

        color(firstColor);
        glVertex2d(width, y);
        if(perpendicular)
            color(secondColor);
        glVertex2d(x, y);
        color(secondColor);
        glVertex2d(x, height);
        if(perpendicular)
            color(firstColor);
        glVertex2d(width, height);
        glEnd();
        glShadeModel(GL_FLAT);
        glPopMatrix();
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
        glPopMatrix();
    }

    public static void drawOutlinedRect(float x, float y, float width, float height, int outlineThickness, int color, int outlineColor) {
        glPushMatrix();
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);
        color(color);
        glBegin(GL_QUADS);
        glVertex2d(width, y);
        glVertex2d(x, y);
        glVertex2d(x, height);
        glVertex2d(width, height);
        glEnd();
        color(outlineColor);
        glLineWidth(outlineThickness);
        glBegin(GL_LINE_LOOP);
        glVertex2d(width + outlineThickness / 4f, y - outlineThickness / 4f);
        glVertex2d(x - outlineThickness / 4f, y - outlineThickness / 4f);
        glVertex2d(x - outlineThickness / 4f, height + outlineThickness / 4f);
        glVertex2d(width + outlineThickness / 4f, height + outlineThickness / 4f);
        glEnd();
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
        glPopMatrix();
    }

    public static void drawBorderedRect(float x, float y, float width, float height, float lineWidth, int outerColor, int innerColor) {
        drawRect(x, y, width, height, innerColor);
        drawBorder(x, y, width, height, lineWidth, outerColor);
    }

    public static void drawOptimizedBorderedRect(float x, float y, float width, float height, float lineWidth, int outerColor, int innerColor) {
        drawRect(x - lineWidth, y - lineWidth, width + lineWidth, height + lineWidth, outerColor);
        drawRect(x, y, width, height, innerColor);
    }

    public static void drawBorder(float x, float y, float width, float height, float lineWidth, int color) {
        Gui.drawRect(x, y, x + width, y + lineWidth, color);
        Gui.drawRect(x, y, x + lineWidth, y + height, color);
        Gui.drawRect(x, y + height - lineWidth, x + width, y + height, color);
        Gui.drawRect(x + width - lineWidth, y, x + width, y + height, color);
    }

    public static void drawPing(float x, float y, NetworkPlayerInfo playerInfo) {
        mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/icons.png"));
        int offset = 0;
        if (playerInfo != null) {
            if (playerInfo.getResponseTime() < 0) offset = 5;
            if (playerInfo.getResponseTime() < 150) offset = 0;
            if (playerInfo.getResponseTime() < 300) offset = 1;
            if (playerInfo.getResponseTime() < 600) offset = 2;
            if (playerInfo.getResponseTime() < 1000) offset = 3;
            else offset = 4;
        } else offset = 0;
        drawTexturedModalRect((int) (x - 11), (int) y, 0, 176 + offset * 8, 10, 8);
    }

    public static void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height) {
        float f = 0.00390625F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0).tex((float) textureX * f, (float) (textureY + height) * f).endVertex();
        worldrenderer.pos(x + width, y + height, 0).tex((float) (textureX + width) * f, (float) (textureY + height) * f).endVertex();
        worldrenderer.pos(x + width, y, 0).tex((float) (textureX + width) * f, (float) textureY * f).endVertex();
        worldrenderer.pos(x, y, 0).tex((float) textureX * f, (float) textureY * f).endVertex();
        tessellator.draw();
    }

    public static void drawFilledCircle(float cx, float cy, float radius, float num_segments, Color color) {
        double theta = 2 * Math.PI / num_segments;
        double c = Math.cos(theta); // precalculate the sine and cosine
        double s = Math.sin(theta);
        double t;
        double x = radius; //we start at angle = 0
        double y = 0;
        glBegin(GL_LINE_LOOP);
        for (int ii = 0; ii < num_segments; ii++) {
            color(color);
            glVertex2d(x + cx, y + cy); //output vertex
            //apply the rotation matrix
            t = x;
            x = c * x - s * y;
            y = s * t + c * y;
        }
        glEnd();
    }

    public static void drawCircle(float x, float y, float radius, float lineWidth, Color color) {
        // if any skids are reading this, MathHelper uses FastMath dumbasses :)
        glPushMatrix();
        glDisable(GL_TEXTURE_2D);
        glEnable(GL_LINE_SMOOTH);
        glLineWidth(lineWidth);
        glBegin(GL_LINE_STRIP);
        color(color);
        int vertices = 90;
        float increment = 2 * MathHelper.PI / vertices;
        for(int i = 0; i <= vertices; i++) {
            float sin = MathHelper.sin(increment * i) * radius;
            float cos = -MathHelper.cos(increment * i) * radius;
            glVertex2f(x + cos, y + sin);
        }
        glEnd();
        glDisable(GL_LINE_SMOOTH);
        glEnable(GL_TEXTURE_2D);
        glPopMatrix();
    }

    public static String getText(String text, boolean vanilla) {
        if (streamerMode == null)
            streamerMode = Client.INSTANCE.getModuleRepository().moduleBy(StreamerModeModule.class);
        if (streamerMode.toggled()) {
            Minecraft mc = Minecraft.getMinecraft();
            text = text.replaceAll(mc.session.getUsername(), "You");
        }
        if (vanilla)
            for (User user : Client.INSTANCE.getOnlineUsers()) {
                text = text.replaceAll(user.accountUsername(), user.accountUsername() + " \u00A77(" + PlayerUtil.ircChatColor(user) + user.clientUsername() + "\u00A77)\u00A7f");
            }
        return text;
    }

}
