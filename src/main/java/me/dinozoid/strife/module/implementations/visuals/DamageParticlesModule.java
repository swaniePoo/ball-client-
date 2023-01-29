package me.dinozoid.strife.module.implementations.visuals;

import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.player.PlayerMotionEvent;
import me.dinozoid.strife.event.implementations.render.Render3DEvent;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.module.implementations.combat.KillAuraModule;
import me.dinozoid.strife.util.render.RenderUtil;
import me.dinozoid.strife.util.system.MathUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

@ModuleInfo(name = "DamageParticles", renderName = "DamageParticles", category = Category.VISUALS)
public class DamageParticlesModule extends Module {

    private final List<DamageParticle> damageParticles = new ArrayList<>();
    @EventHandler
    private final Listener<Render3DEvent> render3DListener = new Listener<>(event -> {
        damageParticles.forEach(damageParticle -> damageParticle.render(event));
        damageParticles.removeIf(damageParticle -> System.currentTimeMillis() - damageParticle.startTime > damageParticle.displayTime);
    });
    private float lastHealth;
    private EntityLivingBase lastTarget;
    @EventHandler
    private final Listener<PlayerMotionEvent> updatePlayerListener = new Listener<>(event -> {
        EntityLivingBase entity = KillAuraModule.instance().target() != null ? KillAuraModule.instance().target() : mc.objectMouseOver != null && mc.objectMouseOver.entityHit != null ? mc.objectMouseOver.entityHit instanceof EntityLivingBase ? (EntityLivingBase) mc.objectMouseOver.entityHit : lastTarget : lastTarget;
        if (entity != null) {
            if (lastTarget == entity && entity.getHealth() != lastHealth || entity.hurtTime > 10) {
                float healthDifference = entity.getHealth() - lastHealth;
                damageParticles.add(new DamageParticle(entity, healthDifference));
                lastHealth = entity.getHealth();
            }
            lastHealth = entity.getHealth();
            lastTarget = entity;
        } else {
            lastHealth = 20;
            lastTarget = null;
        }
    });

    @Override
    public void onDisable() {
        super.onDisable();
        damageParticles.clear();
    }

    class DamageParticle {

        private float damage;
        private final float xOff;
        private final float yOff;
        private final float zOff;
        private final EntityLivingBase entity;
        private final long startTime;
        private final long displayTime;

        public DamageParticle(final EntityLivingBase entity, final float damage) {
            startTime = System.currentTimeMillis();
            displayTime = 1500;
            this.entity = entity;
            this.damage = damage;
            xOff = MathUtil.randomFloat(-0.5f, 0.5f);
            yOff = MathUtil.randomFloat(0, 1.5f);
            zOff = MathUtil.randomFloat(-0.5f, 0.5f);
        }

        public void render(Render3DEvent event) {
            glPushMatrix();
            glEnable(GL_BLEND);
            glDisable(GL_DEPTH_TEST);
            glDepthMask(false);
            mc.entityRenderer.setupCameraTransform(event.getPartialTicks(), 0);
            double x = RenderUtil.interpolate(entity.isEntityAlive() ? entity.posX : entity.lastTickPosX, entity.lastTickPosX, event.getPartialTicks()) - mc.getRenderManager().viewerPosX;
            double y = RenderUtil.interpolate(entity.isEntityAlive() ? entity.posY : entity.lastTickPosY, entity.lastTickPosY, event.getPartialTicks()) - mc.getRenderManager().viewerPosY;
            double z = RenderUtil.interpolate(entity.isEntityAlive() ? entity.posZ : entity.lastTickPosZ, entity.lastTickPosZ, event.getPartialTicks()) - mc.getRenderManager().viewerPosZ;
            long timeDifference = System.currentTimeMillis() - startTime;
            double size = MathHelper.clamp_double(timeDifference / (float) displayTime * 2, 0, 1);
            glTranslated(x + xOff, y + yOff, z + zOff);
            glRotated(-mc.getRenderManager().playerViewY, 0, 1, 0);
            glRotated(mc.getRenderManager().playerViewX, (mc.gameSettings.thirdPersonView == 2) ? -1.0f : 1.0f, 0, 0);
            glScaled(-0.018 * size, -0.018 * size, 0.018 * size);
            damage = MathUtil.round(damage, 2);
            String damageString = (damage > 0 ? "+" : "") + damage;
            Color color = Color.GREEN;
            if (damage < 0) {
                if (damage < -1) color = Color.YELLOW;
                if (damage < -1.5) color = Color.ORANGE;
                if (damage < -2.5) color = Color.RED;
            }
            mc.fontRendererObj.drawStringWithShadow(damageString, -mc.fontRendererObj.getStringWidth(damageString) / 2f, 0, color.getRGB());
            glScaled(1, 1, 1);
            glDepthMask(true);
            glEnable(GL_DEPTH_TEST);
            glDisable(GL_BLEND);
            glPopMatrix();
        }

        public float damage() {
            return damage;
        }

        public float displayTime() {
            return displayTime;
        }

        public float currentTime() {
            return startTime;
        }
    }

}