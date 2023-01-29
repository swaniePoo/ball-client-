package me.dinozoid.strife.module.implementations.visuals;

import com.google.common.base.Predicates;
import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.player.RenderNametagEvent;
import me.dinozoid.strife.event.implementations.render.Render2DEvent;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.property.Property;
import me.dinozoid.strife.property.implementations.ColorProperty;
import me.dinozoid.strife.property.implementations.DoubleProperty;
import me.dinozoid.strife.property.implementations.EnumProperty;
import me.dinozoid.strife.property.implementations.MultiSelectEnumProperty;
import me.dinozoid.strife.util.player.PlayerUtil;
import me.dinozoid.strife.util.render.RenderUtil;
import me.dinozoid.strife.util.world.WorldUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;

import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

@ModuleInfo(name = "ESP", renderName = "ESP", description = "See things through walls.", category = Category.VISUALS)
public class ESPModule extends Module {

    private final MultiSelectEnumProperty<PlayerUtil.Target> targetsProperty = new MultiSelectEnumProperty<>("Targets", PlayerUtil.Target.PLAYERS);
    private final MultiSelectEnumProperty<Element> elementsProperty = new MultiSelectEnumProperty<>("Elements", Element.BOX, Element.NAMETAGS, Element.HEALTH, Element.ARMOR, Element.HAND);
    private final DoubleProperty boxThicknessProperty = new DoubleProperty("Box Thickness", 1, 1, 10, 1, () -> elementsProperty.selected(Element.BOX));
    private final EnumProperty<BoxMode> boxModeProperty = new EnumProperty<>("Box Mode", BoxMode.BOX, () -> elementsProperty.selected(Element.BOX));
    private final Property<Boolean> oppositeCornersProperty = new Property<>("Opposite", false, () -> elementsProperty.selected(Element.BOX) && boxModeProperty.getValue() == BoxMode.HALF_CORNERS);
    private final EnumProperty<RenderUtil.ColorMode> boxColorModeProperty = new EnumProperty<>("Color Mode", RenderUtil.ColorMode.SYNC, () -> elementsProperty.selected(Element.BOX));
    private final ColorProperty boxColorProperty = new ColorProperty("Color", new Color(209, 50, 50), () -> boxColorModeProperty.getValue() == RenderUtil.ColorMode.STATIC || boxColorModeProperty.getValue() == RenderUtil.ColorMode.PULSE || boxColorModeProperty.getValue() == RenderUtil.ColorMode.SWITCH);
    private final ColorProperty secondColorProperty = new ColorProperty("Second Color", new Color(29, 205, 200), () -> elementsProperty.selected(Element.BOX) && boxColorModeProperty.getValue() == RenderUtil.ColorMode.SWITCH);
    private final Property<Boolean> boxFadeProperty = new Property<>("Box Fade", true, () -> elementsProperty.selected(Element.BOX));

    private BlurModule blurModule;

    public int getBoxColor(int index) {
        return RenderUtil.getColor(boxColorModeProperty, index, boxColorProperty.getValue(), secondColorProperty.getValue());
    }

    @EventHandler
    private final Listener<RenderNametagEvent> renderNametagListener = new Listener<>(event -> {
        if (event.getEntity() instanceof EntityPlayer && elementsProperty.selected(Element.NAMETAGS))
            event.cancel();
    });

    @EventHandler
    private final Listener<Render2DEvent> render2DListener = new Listener<>(event -> {
        if(blurModule == null)
            blurModule = BlurModule.instance();
        final List<EntityLivingBase> livingEntities = WorldUtil.getLivingEntities(Predicates.and(entity -> PlayerUtil.isValid(entity, targetsProperty)));
        for (EntityLivingBase entity : livingEntities) {
            if(!RenderUtil.isEntityInFrustum(entity)) continue;
            final double diffX = entity.posX - entity.lastTickPosX;
            final double diffY = entity.posY - entity.lastTickPosY;
            final double diffZ = entity.posZ - entity.lastTickPosZ;
            final double deltaX = mc.thePlayer.posX - entity.posX;
            final double deltaY = mc.thePlayer.posY - entity.posY;
            final double deltaZ = mc.thePlayer.posZ - entity.posZ;
            final float partialTicks = event.getPartialTicks();
            final AxisAlignedBB interpolatedBB = new AxisAlignedBB(
                    entity.lastTickPosX - entity.width / 2 + diffX * partialTicks,
                    entity.lastTickPosY + diffY * partialTicks,
                    entity.lastTickPosZ - entity.width / 2 + diffZ * partialTicks,
                    entity.lastTickPosX + entity.width / 2 + diffX * partialTicks,
                    entity.lastTickPosY + entity.height + diffY * partialTicks,
                    entity.lastTickPosZ + entity.width / 2 + diffZ * partialTicks);
            final double[][] vectors = new double[8][2];
            final float[] coords = new float[4];
            convertTo2D(interpolatedBB, vectors, coords);
            float minX = coords[0], minY = coords[1], maxX = coords[2], maxY = coords[3];
            float opacity = 255 - MathHelper.clamp_float(MathHelper.sqrt_double(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) * 4, 0, 255);
            Color color = boxFadeProperty.getValue() ? RenderUtil.toColorRGB(getBoxColor(0), opacity) : new Color(getBoxColor(0));
            for (Element element : elementsProperty.values()) {
                if (elementsProperty.selected(element)) {
                    switch (element) {
                        case BOX: {
                            switch (boxModeProperty.getValue()) {
                                case BOX: {
                                    RenderUtil.pre3D();
                                    glLineWidth(boxThicknessProperty.getValue().floatValue() * 4f);
                                    glBegin(GL_LINE_LOOP);
                                    glColor4f(0, 0, 0, opacity / 255f);
                                    glVertex2f(minX, minY);
                                    glVertex2f(maxX, minY);
                                    glVertex2f(maxX, maxY);
                                    glVertex2f(minX, maxY);
                                    glEnd();

                                    glLineWidth(boxThicknessProperty.getValue().floatValue());
                                    RenderUtil.color(color);
                                    glBegin(GL_LINE_LOOP);
                                    glVertex2f(minX, minY);
                                    glVertex2f(maxX, minY);
                                    glVertex2f(maxX, maxY);
                                    glVertex2f(minX, maxY);
                                    glEnd();
                                    RenderUtil.post3D();
                                    break;
                                }
                                case FILL: {
                                    RenderUtil.drawRect(minX, minY, maxX, maxY, color.getRGB());
                                    break;
                                }
                                case BLUR_FILL: {
                                    if(!blurModule.toggled()) return;
                                    float finalMinX = minX;
                                    float finalMinY = minY;
                                    float finalMaxX = maxX;
                                    float finalMaxY = maxY;
                                    blurModule.renderCallbacks().add(() -> {
                                        RenderUtil.drawRect(finalMinX, finalMinY, finalMaxX, finalMaxY, color.getRGB());
                                    });
                                    break;
                                }
                                case HORIZ_SIDES: {
                                    RenderUtil.pre3D();
                                    float lineLength = (maxX - minX) / 3;
                                    glLineWidth(boxThicknessProperty.getValue().floatValue() * 4f);
                                    glColor4f(0, 0, 0, opacity / 255f);
                                    glBegin(GL_LINES);
                                    glVertex2f(minX, minY);
                                    glVertex2f(minX + lineLength, minY);
                                    glVertex2f(minX, minY);
                                    glVertex2f(minX, maxY);
                                    glVertex2f(minX, maxY);
                                    glVertex2f(minX + lineLength, maxY);
                                    glVertex2f(maxX, minY);
                                    glVertex2f(maxX - lineLength, minY);
                                    glVertex2f(maxX, minY);
                                    glVertex2f(maxX, maxY);
                                    glVertex2f(maxX, maxY);
                                    glVertex2f(maxX - lineLength, maxY);
                                    glEnd();

                                    glLineWidth(boxThicknessProperty.getValue().floatValue());
                                    glBegin(GL_LINES);
                                    RenderUtil.color(color);
                                    glVertex2f(minX, minY);
                                    glVertex2f(minX + lineLength, minY);
                                    glVertex2f(minX, minY);
                                    glVertex2f(minX, maxY);
                                    glVertex2f(minX, maxY);
                                    glVertex2f(minX + lineLength, maxY);
                                    glVertex2f(maxX, minY);
                                    glVertex2f(maxX - lineLength, minY);
                                    glVertex2f(maxX, minY);
                                    glVertex2f(maxX, maxY);
                                    glVertex2f(maxX, maxY);
                                    glVertex2f(maxX - lineLength, maxY);
                                    glEnd();
                                    RenderUtil.post3D();
                                    break;
                                }
                                case VERT_SIDES: {
                                    RenderUtil.pre3D();
                                    float lineLength = (maxX - minX) / 3;
                                    glLineWidth(boxThicknessProperty.getValue().floatValue() * 4f);
                                    glColor4f(0, 0, 0, opacity / 255f);
                                    glBegin(GL_LINES);
                                    glVertex2f(minX, minY);
                                    glVertex2f(minX, minY + lineLength);
                                    glVertex2f(minX, minY);
                                    glVertex2f(maxX, minY);
                                    glVertex2f(maxX, minY);
                                    glVertex2f(maxX, minY + lineLength);
                                    glVertex2f(minX, maxY);
                                    glVertex2f(minX, maxY - lineLength);
                                    glVertex2f(minX, maxY);
                                    glVertex2f(maxX, maxY);
                                    glVertex2f(maxX, maxY);
                                    glVertex2f(maxX, maxY - lineLength);
                                    glEnd();
                                    glLineWidth(boxThicknessProperty.getValue().floatValue());
                                    RenderUtil.color(color);
                                    glBegin(GL_LINES);
                                    glVertex2f(minX, minY);
                                    glVertex2f(minX, minY + lineLength);
                                    glVertex2f(minX, minY);
                                    glVertex2f(maxX, minY);
                                    glVertex2f(maxX, minY);
                                    glVertex2f(maxX, minY + lineLength);
                                    glVertex2f(minX, maxY);
                                    glVertex2f(minX, maxY - lineLength);
                                    glVertex2f(minX, maxY);
                                    glVertex2f(maxX, maxY);
                                    glVertex2f(maxX, maxY);
                                    glVertex2f(maxX, maxY - lineLength);
                                    glEnd();
                                    RenderUtil.post3D();
                                    break;
                                }
                                case CORNERS: {
                                    RenderUtil.pre3D();
                                    glLineWidth(boxThicknessProperty.getValue().floatValue() * 4f);
                                    glBegin(GL_LINES);
                                    glColor4f(0, 0, 0, opacity / 255f);
                                    float lineLength = (maxX - minX) / 3;
                                    glVertex2f(minX, minY);
                                    glVertex2f(minX + lineLength, minY);
                                    glVertex2f(minX, minY);
                                    glVertex2f(minX, minY + lineLength);

                                    glVertex2f(maxX, minY);
                                    glVertex2f(maxX - lineLength, minY);
                                    glVertex2f(maxX, minY);
                                    glVertex2f(maxX, minY + lineLength);

                                    glVertex2f(minX, maxY);
                                    glVertex2f(minX + lineLength, maxY);
                                    glVertex2f(minX, maxY);
                                    glVertex2f(minX, maxY - lineLength);

                                    glVertex2f(maxX, maxY);
                                    glVertex2f(maxX - lineLength, maxY);
                                    glVertex2f(maxX, maxY);
                                    glVertex2f(maxX, maxY - lineLength);
                                    glEnd();

                                    glLineWidth(boxThicknessProperty.getValue().floatValue());
                                    glBegin(GL_LINES);
                                    RenderUtil.color(color);
                                    glVertex2f(minX, minY);
                                    glVertex2f(minX + lineLength, minY);
                                    glVertex2f(minX, minY);
                                    glVertex2f(minX, minY + lineLength);
                                    glVertex2f(maxX, minY);
                                    glVertex2f(maxX - lineLength, minY);
                                    glVertex2f(maxX, minY);
                                    glVertex2f(maxX, minY + lineLength);
                                    glVertex2f(minX, maxY);
                                    glVertex2f(minX + lineLength, maxY);
                                    glVertex2f(minX, maxY);
                                    glVertex2f(minX, maxY - lineLength);
                                    glVertex2f(maxX, maxY);
                                    glVertex2f(maxX - lineLength, maxY);
                                    glVertex2f(maxX, maxY);
                                    glVertex2f(maxX, maxY - lineLength);
                                    glEnd();
                                    RenderUtil.post3D();
                                    break;
                                }
                                case HALF_CORNERS: {
                                    RenderUtil.pre3D();
                                    glLineWidth(boxThicknessProperty.getValue().floatValue() * 4f);
                                    glBegin(GL_LINES);
                                    glColor4f(0, 0, 0, opacity / 255f);
                                    float lineLength = (maxX - minX) / 3;
                                    if (oppositeCornersProperty.getValue()) {
                                        glVertex2f(maxX, minY);
                                        glVertex2f(maxX - lineLength, minY);
                                        glVertex2f(maxX, minY);
                                        glVertex2f(maxX, minY + lineLength);

                                        glVertex2f(minX, maxY);
                                        glVertex2f(minX + lineLength, maxY);
                                        glVertex2f(minX, maxY);
                                        glVertex2f(minX, maxY - lineLength);
                                    } else {
                                        glVertex2f(minX, minY);
                                        glVertex2f(minX + lineLength, minY);
                                        glVertex2f(minX, minY);
                                        glVertex2f(minX, minY + lineLength);

                                        glVertex2f(maxX, maxY);
                                        glVertex2f(maxX - lineLength, maxY);
                                        glVertex2f(maxX, maxY);
                                        glVertex2f(maxX, maxY - lineLength);
                                    }
                                    glEnd();
                                    glLineWidth(boxThicknessProperty.getValue().floatValue());
                                    glBegin(GL_LINES);
                                    RenderUtil.color(color);
                                    if (oppositeCornersProperty.getValue()) {
                                        glVertex2f(maxX, minY);
                                        glVertex2f(maxX - lineLength, minY);
                                        glVertex2f(maxX, minY);
                                        glVertex2f(maxX, minY + lineLength);

                                        glVertex2f(minX, maxY);
                                        glVertex2f(minX + lineLength, maxY);
                                        glVertex2f(minX, maxY);
                                        glVertex2f(minX, maxY - lineLength);
                                    } else {
                                        glVertex2f(minX, minY);
                                        glVertex2f(minX + lineLength, minY);
                                        glVertex2f(minX, minY);
                                        glVertex2f(minX, minY + lineLength);

                                        glVertex2f(maxX, maxY);
                                        glVertex2f(maxX - lineLength, maxY);
                                        glVertex2f(maxX, maxY);
                                        glVertex2f(maxX, maxY - lineLength);
                                    }
                                    glEnd();
                                    RenderUtil.post3D();
                                    break;
                                }
                            }
                            break;
                        }
                        case NAMETAGS: {
                            float scale = 0.55f;
                            float leftoverScale = 1 / scale;
                            minX *= leftoverScale;
                            minY *= leftoverScale;
                            maxX *= leftoverScale;
                            maxY *= leftoverScale;
                            glScalef(scale, scale, 1);
                            mc.fontRendererObj.drawStringWithShadow(entity.getDisplayName().getFormattedText(), minX + (maxX - minX) / 2 - mc.fontRendererObj.getStringWidth(entity.getDisplayName().getFormattedText()) / 2f, boxModeProperty.getValue() == BoxMode.BOX || boxModeProperty.getValue() == BoxMode.FILL ? minY - mc.fontRendererObj.FONT_HEIGHT - 3 : minY - mc.fontRendererObj.FONT_HEIGHT / 2f, new Color(255, 255, 255, MathHelper.floor_float(opacity)).getRGB());
                            glScalef(leftoverScale, leftoverScale, 1);
                            minX *= scale;
                            minY *= scale;
                            maxX *= scale;
                            maxY *= scale;
                            break;
                        }
                        case HAND: {
                            if(entity.getHeldItem() != null) {
                                float scale = 0.5f;
                                float leftoverScale = 1 / scale;
                                minX *= leftoverScale;
                                minY *= leftoverScale;
                                maxX *= leftoverScale;
                                maxY *= leftoverScale;
                                glScalef(scale, scale, 1);
                                String text = entity.getHeldItem().getDisplayName();
                                mc.fontRendererObj.drawStringWithShadow(text, minX + (maxX - minX) / 2 - mc.fontRendererObj.getStringWidth(text) / 2f, boxModeProperty.getValue() == BoxMode.BOX || boxModeProperty.getValue() == BoxMode.FILL ? maxY + mc.fontRendererObj.FONT_HEIGHT - 3 : maxY - mc.fontRendererObj.FONT_HEIGHT / 2f, new Color(255, 255, 255, MathHelper.floor_float(opacity)).getRGB());
                                glScalef(leftoverScale, leftoverScale, 1);
                                minX *= scale;
                                minY *= scale;
                                maxX *= scale;
                                maxY *= scale;
                            }
                            break;
                        }
                        case HEALTH: {
                            minX -= 3;
                            maxX -= 3;
                            RenderUtil.pre3D();
                            glLineWidth(boxThicknessProperty.getValue().floatValue() * 4f);
                            glBegin(GL_LINES);
                            glColor4f(0, 0, 0, opacity / 255f);
                            glVertex2f(minX, minY);
                            glVertex2f(minX, maxY);
                            glEnd();
                            glLineWidth(boxThicknessProperty.getValue().floatValue());
                            glBegin(GL_LINES);
                            Color healthColor = Color.GREEN;
                            if (entity.getHealth() < entity.getMaxHealth() / 2) healthColor = Color.YELLOW;
                            if (entity.getHealth() < entity.getMaxHealth() / 3) healthColor = Color.ORANGE;
                            if (entity.getHealth() < entity.getMaxHealth() / 4) healthColor = Color.RED;
                            RenderUtil.color(healthColor, MathHelper.floor_float(opacity));
                            glVertex2f(minX, minY + (maxY - minY));
                            glVertex2f(minX, maxY - (maxY - minY) * (entity.getHealth() / entity.getMaxHealth()));
                            glEnd();
                            RenderUtil.post3D();
                            minX += 3;
                            maxX += 3;
                            break;
                        }
                    }
                }
            }
        }
    });

//    @EventHandler
//    private final Listener<Render2DEvent> render2DListener = new Listener<>(event -> {
//        for (EntityLivingBase entity : entityPositionMap.keySet()) {
//            float[] positions = entityPositionMap.get(entity);
//            float x = positions[0];
//            float z = positions[1];
//            float y = positions[2];
//            float w = positions[3];
//            mc.entityRenderer.setupOverlayRendering();
//            glPushMatrix();
//            glDisable(GL_TEXTURE_2D);
//            glEnable(GL_BLEND);
//            for (Element element : elementsProperty.values()) {
//                if (elementsProperty.selected(element)) {
//                    switch (element) {
//                        case BOX: {
//                            if (boxModeProperty.value() == BoxMode.SOLID) {
//                                glLineWidth(boxThicknessProperty.value().floatValue() * 4.5f);
//                                glBegin(GL_LINE_LOOP);
//                                RenderUtil.color(Color.BLACK);
//                                glVertex2f(x, y);
//                                glVertex2f(x, w);
//                                glVertex2f(z, w);
//                                glVertex2f(z, y);
//                                glEnd();
//                                glLineWidth(boxThicknessProperty.value().floatValue());
//                                glBegin(GL_LINE_LOOP);
//                                RenderUtil.color(entity instanceof EntityPlayer && PlayerUtil.isTeammate((EntityPlayer) entity) ? Color.GREEN.getRGB() : Color.RED.getRGB());
//                                glVertex2f(x, y);
//                                glVertex2f(x, w);
//                                glVertex2f(z, w);
//                                glVertex2f(z, y);
//                                glEnd();
//                            } else {
//
//                            }
//                            break;
//                        }
//                        case ARMOR: {
//
//                            break;
//                        }
//                        case HEALTH: {
////                            glLineWidth(boxThicknessProperty.value().floatValue() * 5.6f);
////                            glBegin(GL_LINES);
////                            RenderUtil.color(Color.BLACK);
////                            glVertex2f(x - 3, y);
////                            glVertex2f(x - 3, w);
////                            glEnd();
//                            glLineWidth(boxThicknessProperty.value().floatValue() / 2);
//                            glBegin(GL_LINES);
//                            Color color = Color.GREEN;
//                            if (entity.getHealth() < entity.getMaxHealth() / 2) color = Color.YELLOW;
//                            if (entity.getHealth() < entity.getMaxHealth() / 3) color = Color.ORANGE;
//                            if (entity.getHealth() < entity.getMaxHealth() / 4) color = Color.RED;
//                            RenderUtil.color(color);
//                            glVertex2f(x - 3, y);
//                            glVertex2f(x - 3, w - 1);
//                            glEnd();
//                            break;
//                        }
//                        case NAMETAGS: {
//
//                            break;
//                        }
//                    }
//                }
//            }
//            glDisable(GL_BLEND);
//            glEnable(GL_TEXTURE_2D);
//            glPopMatrix();
//        }
//    });

    private void convertTo2D(AxisAlignedBB interpolatedBB, double[][] vectors, float[] coords) {
        if(coords == null || vectors == null || interpolatedBB == null) return;
        double x = mc.getRenderManager().viewerPosX;
        double y = mc.getRenderManager().viewerPosY;
        double z = mc.getRenderManager().viewerPosZ;

        vectors[0] = RenderUtil.project2D(interpolatedBB.minX - x, interpolatedBB.minY - y,
                interpolatedBB.minZ - z);
        vectors[1] = RenderUtil.project2D(interpolatedBB.minX - x, interpolatedBB.minY - y,
                interpolatedBB.maxZ - z);
        vectors[2] = RenderUtil.project2D(interpolatedBB.minX - x, interpolatedBB.maxY - y,
                interpolatedBB.minZ - z);
        vectors[3] = RenderUtil.project2D(interpolatedBB.maxX - x, interpolatedBB.minY - y,
                interpolatedBB.minZ - z);
        vectors[4] = RenderUtil.project2D(interpolatedBB.maxX - x, interpolatedBB.maxY - y,
                interpolatedBB.minZ - z);
        vectors[5] = RenderUtil.project2D(interpolatedBB.maxX - x, interpolatedBB.minY - y,
                interpolatedBB.maxZ - z);
        vectors[6] = RenderUtil.project2D(interpolatedBB.minX - x, interpolatedBB.maxY - y,
                interpolatedBB.maxZ - z);
        vectors[7] = RenderUtil.project2D(interpolatedBB.maxX - x, interpolatedBB.maxY - y,
                interpolatedBB.maxZ - z);

        float minW = (float) Arrays.stream(vectors).min(Comparator.comparingDouble(pos -> pos[2])).orElse(new double[]{0.5})[2];
        float maxW = (float) Arrays.stream(vectors).max(Comparator.comparingDouble(pos -> pos[2])).orElse(new double[]{0.5})[2];
        if (maxW > 1 || minW < 0) return;
        float minX = (float) Arrays.stream(vectors).min(Comparator.comparingDouble(pos -> pos[0])).orElse(new double[]{0})[0];
        float maxX = (float) Arrays.stream(vectors).max(Comparator.comparingDouble(pos -> pos[0])).orElse(new double[]{0})[0];
        final float top = (mc.displayHeight / (float) new ScaledResolution(mc).getScaleFactor());
        float minY = (float) (top - Arrays.stream(vectors).min(Comparator.comparingDouble(pos -> top - pos[1])).orElse(new double[]{0})[1]);
        float maxY = (float) (top - Arrays.stream(vectors).max(Comparator.comparingDouble(pos -> top - pos[1])).orElse(new double[]{0})[1]);
        coords[0] = minX;
        coords[1] = minY;
        coords[2] = maxX;
        coords[3] = maxY;
    }

    public enum Element {
        BOX, NAMETAGS, HEALTH, ARMOR, HAND
    }

    public enum BoxMode {
        BOX, FILL, BLUR_FILL, HORIZ_SIDES, VERT_SIDES, CORNERS, HALF_CORNERS
    }

    public enum Target {
        PLAYERS, MOBS, ANIMALS, INVISIBLES
    }
}
