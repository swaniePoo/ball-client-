package me.dinozoid.strife.module.implementations.visuals;

import me.dinozoid.strife.alpine.event.EventState;
import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.player.PlayerMotionEvent;
import me.dinozoid.strife.event.implementations.player.WorldLoadEvent;
import me.dinozoid.strife.event.implementations.render.Render3DEvent;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.property.Property;
import me.dinozoid.strife.property.implementations.DoubleProperty;
import me.dinozoid.strife.property.implementations.EnumProperty;
import me.dinozoid.strife.util.render.RenderUtil;
import me.dinozoid.strife.util.system.TimerUtil;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;


@ModuleInfo(name = "Breadcrumbs", renderName = "Breadcrumbs", category = Category.VISUALS)
public class BreadcrumbsModule extends Module {

    private final DoubleProperty pointSizeProperty = new DoubleProperty("Point Size", 20, 10, 60, 1, Property.Representation.INT);
    private final DoubleProperty timeProperty = new DoubleProperty("Time", 2, 1, 10, 1, Property.Representation.INT);
    private final Property<Boolean> timeoutProperty = new Property<>("Timeout", true);
    private final Property<Boolean> moreBreadcrumbsProperty = new Property<>("More Breadcrumbs", false);
    private final EnumProperty<BreadcrumbsMode> modeProperty = new EnumProperty<>("Mode", BreadcrumbsMode.DOTS);
    private final List<Breadcrumb> breadcrumbs = new ArrayList<>();
    @EventHandler
    private final Listener<Render3DEvent> render3DListener = new Listener<>(event -> {
        if (breadcrumbs.size() > 0) {
            drawBreadcrumbs(event);
        }
    });
    @EventHandler
    private final Listener<WorldLoadEvent> worldLoadListener = new Listener<>(event -> {
        breadcrumbs.clear();
    });
    private final TimerUtil timer = new TimerUtil();
    @EventHandler
    private final Listener<PlayerMotionEvent> updatePlayerListener = new Listener<>(event -> {
        if (event.getState() == EventState.PRE) {
            clearBreadcrumbs();
            updateBreadcrumbs();
        }
    });

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        breadcrumbs.clear();
    }

    public void drawBreadcrumbs(Render3DEvent event) {
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_DEPTH_TEST);
        glDepthMask(false);
        glEnable(GL_BLEND);
        if (modeProperty.getValue() == BreadcrumbsMode.DOTS)
            glEnable(GL_POINT_SMOOTH);
        else glEnable(GL_LINE_SMOOTH);
        glPushMatrix();
        int index = 0;
        if (modeProperty.getValue() == BreadcrumbsMode.LINES) {
            glLineWidth(6);
            glBegin(GL_LINES);
        }
        for (Breadcrumb breadcrumb : breadcrumbs) {
            if (!breadcrumb.visible) {
                if (breadcrumb.opacity > 0) breadcrumb.opacity -= 1;
                if (breadcrumb.scale >= 0.1) breadcrumb.scale -= 0.1f;
            }
            if(!RenderUtil.isVecInFrustrum(new Vec3(breadcrumb.x, breadcrumb.y, breadcrumb.z))) continue;
            double x = breadcrumb.x - mc.getRenderManager().viewerPosX;
            double y = breadcrumb.y - mc.getRenderManager().viewerPosY;
            double z = breadcrumb.z - mc.getRenderManager().viewerPosZ;
            switch (modeProperty.getValue()) {
                case DOTS: {
                    mc.entityRenderer.setupCameraTransform(event.getPartialTicks(), 0);
                    glPointSize(pointSizeProperty.getValue().floatValue());
                    glBegin(GL_POINTS);
                    RenderUtil.color(OverlayModule.getColor(index * 15), (int) breadcrumb.opacity);
                    glVertex3d(x, y, z);
                    glEnd();
                    glPointSize(pointSizeProperty.getValue().floatValue() * 1.75f);
                    glBegin(GL_POINTS);
                    RenderUtil.color(OverlayModule.getColor(index * 15), (int) breadcrumb.opacity / 3);
                    glVertex3d(x, y, z);
                    glEnd();
                    glPointSize(pointSizeProperty.getValue().floatValue() * 2.5f);
                    glBegin(GL_POINTS);
                    RenderUtil.color(OverlayModule.getColor(index * 15), (int) breadcrumb.opacity / 4);
                    glVertex3d(x, y, z);
                    glEnd();
                    break;
                }
                case LINES: {
                    RenderUtil.color(OverlayModule.getColor(index * 100), (int) breadcrumb.opacity);
                    glVertex3d(x, y, z);
                    break;
                }
            }
            index++;
        }
        if (modeProperty.getValue() == BreadcrumbsMode.LINES) {
            glEnd();
        }
        glColor4f(1, 1, 1, 1);
        if (modeProperty.getValue() == BreadcrumbsMode.DOTS)
            glDisable(GL_POINT_SMOOTH);
        else glDisable(GL_LINE_SMOOTH);
        glDisable(GL_BLEND);
        glDepthMask(true);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);
        glPopMatrix();
    }

    public void clearBreadcrumbs() {
        breadcrumbs.removeIf(breadcrumb -> !breadcrumb.visible && breadcrumb.opacity == 0 && breadcrumb.scale == 0 || getDistanceToBreadcrumb(breadcrumb) > 30);
    }

    public void updateBreadcrumbs() {
        for (Breadcrumb breadcrumb : breadcrumbs) {
            if (System.currentTimeMillis() - breadcrumb.time > timeProperty.getValue() * 2000 && timeoutProperty.getValue()) {
                breadcrumb.visible = false;
            }
        }
        if (timer.hasElapsed(moreBreadcrumbsProperty.getValue() ? 25 : 50) && mc.thePlayer.motionX != 0 || mc.thePlayer.motionY != 0 || mc.thePlayer.motionZ != 0) {
            breadcrumbs.add(new Breadcrumb(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ));
            timer.reset();
        }
    }

    public double getDistanceToBreadcrumb(Breadcrumb breadcrumb) {
        double xDiff = Math.abs(breadcrumb.x - mc.thePlayer.posX);
        double yDiff = Math.abs(breadcrumb.y - mc.thePlayer.posY);
        double zDiff = Math.abs(breadcrumb.z - mc.thePlayer.posZ);
        return MathHelper.sqrt_double(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff);
    }

    public enum BreadcrumbsMode {
        DOTS, LINES
    }

    private class Breadcrumb {

        private final double x;
        private final double y;
        private final double z;
        private float opacity = 255, scale = 1;
        private boolean visible = true;
        private final long time;

        public Breadcrumb(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
            time = System.currentTimeMillis();
        }
    }
}
