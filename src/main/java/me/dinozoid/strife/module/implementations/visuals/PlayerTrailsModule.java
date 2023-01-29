package me.dinozoid.strife.module.implementations.visuals;

import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.player.PlayerMotionEvent;
import me.dinozoid.strife.event.implementations.render.Render3DEvent;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.property.Property;
import me.dinozoid.strife.util.render.RenderUtil;
import me.dinozoid.strife.util.system.TimerUtil;
import net.minecraft.util.MathHelper;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

@ModuleInfo(name = "PlayerTrails", renderName = "PlayerTrails", category = Category.VISUALS)
public class PlayerTrailsModule extends Module {

    private final Property<Boolean> timeoutProperty = new Property<>("Timeout", true);
    private final List<Trail> trails = new ArrayList<>();

    public double getDistanceToTrail(Trail trail) {
        double xDiff = Math.abs(trail.x - mc.thePlayer.posX);
        double zDiff = Math.abs(trail.z - mc.thePlayer.posZ);
        return MathHelper.sqrt_double(xDiff * xDiff + zDiff * zDiff);
    }

    public void clearTrails() {
        trails.removeIf(breadcrumb -> !breadcrumb.visible && breadcrumb.opacity == 0 || getDistanceToTrail(breadcrumb) > 30);
    }

    public void updateTrails() {
        for (Trail trail : trails) {
            if (System.currentTimeMillis() - trail.time > 150 && timeoutProperty.getValue()) {
                trail.visible = false;
            }
        }
        if (mc.thePlayer.motionX != 0 || mc.thePlayer.motionY != 0 || mc.thePlayer.motionZ != 0) {
            trails.add(new Trail(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY, mc.thePlayer.getEntityBoundingBox().maxY, mc.thePlayer.posZ));
        }
    }

    @EventHandler
    private final Listener<PlayerMotionEvent> updatePlayerListener = new Listener<>(event -> {
        clearTrails();
        updateTrails();
    });

    @EventHandler
    private final Listener<Render3DEvent> render3DListener = new Listener<>(event -> {
        glPushMatrix();
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_CULL_FACE);
        glFrontFace(GL_CW);
        mc.entityRenderer.setupCameraTransform(event.getPartialTicks(), 0);
        glBegin(GL_QUADS);
        int i = 0;
        double x = -mc.getRenderManager().viewerPosX;
        double y = -mc.getRenderManager().viewerPosY;
        double z = -mc.getRenderManager().viewerPosZ;
        for (Trail trail : trails) {
            if (!trail.visible) {
                if (trail.opacity > 0) trail.opacity -= 1;
                trail.timer.reset();
            }
            if (i > 1) {
                if(mc.gameSettings.thirdPersonView == 0 && i >= trails.size() - 4) continue;
                Trail previousTrail = trails.get(i - 1);
                double diffX = trail.x - previousTrail.x;
                double diffY = (trail.maxY - trail.minY) - (previousTrail.maxY - previousTrail.minY);
                double diffZ = trail.z - previousTrail.z;
                double dist = Math.sqrt(diffX * diffX + diffY * diffY + diffZ * diffZ);
                int color = OverlayModule.getColor(i * 2);
                RenderUtil.color(color, (int)trail.opacity);
                glVertex3d(x + trail.x, y + trail.maxY, z + trail.z);
                    RenderUtil.color(color, (int) (trail.opacity - (dist / 50)));
                glVertex3d(x + previousTrail.x, y + previousTrail.maxY, z + previousTrail.z);
                    RenderUtil.color(color, (int) (trail.opacity - (dist / 50)));
                glVertex3d(x + previousTrail.x, y + previousTrail.minY, z + previousTrail.z);
                RenderUtil.color(color, (int)trail.opacity);
                glVertex3d(x + trail.x, y + trail.minY, z + trail.z);
            }
            i++;
        }
        glEnd();
        glEnable(GL_LINE_SMOOTH);
        glBegin(GL_LINE_STRIP);
        for (Trail trail : trails) {
            if(mc.gameSettings.thirdPersonView == 0 && i >= trails.size() - 4) continue;
            int color = OverlayModule.getColor(i * 2);
            RenderUtil.color(color, (int)trail.opacity);
            glVertex3d(x + trail.x, y + trail.maxY, z + trail.z);
        }
        glEnd();
        glBegin(GL_LINE_STRIP);
        for (Trail trail : trails) {
            if(mc.gameSettings.thirdPersonView == 0 && i >= trails.size() - 4) continue;
            int color = OverlayModule.getColor(i * 2);
            RenderUtil.color(color, (int)trail.opacity);
            glVertex3d(x + trail.x, y + trail.minY, z + trail.z);
        }
        glEnd();
        glColor4f(1, 1, 1, 1);
        glDisable(GL_LINE_SMOOTH);
        glFrontFace(GL_CCW);
        glEnable(GL_CULL_FACE);
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glPopMatrix();
    });

    @Override
    public void onDisable() {
        super.onDisable();
        trails.clear();
    }

    private class Trail {

        private final TimerUtil timer;
        private final double x, minY, maxY, z;
        private boolean visible;
        private float opacity;
        private final long time;

        public Trail(double x, double minY, double maxY, double z) {
            this.x = x;
            this.minY = minY;
            this.maxY = maxY;
            this.z = z;
            opacity = 100;
            visible = true;
            timer = new TimerUtil();
            this.time = System.currentTimeMillis();
        }
    }

}
