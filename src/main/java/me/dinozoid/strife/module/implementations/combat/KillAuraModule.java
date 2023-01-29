package me.dinozoid.strife.module.implementations.combat;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.alpine.event.EventState;
import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.network.PacketInboundEvent;
import me.dinozoid.strife.event.implementations.player.PlayerMotionEvent;
import me.dinozoid.strife.event.implementations.render.Render2DEvent;
import me.dinozoid.strife.event.implementations.render.Render3DEvent;
import me.dinozoid.strife.font.CustomFontRenderer;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.module.implementations.player.ScaffoldModule;
import me.dinozoid.strife.module.implementations.visuals.OverlayModule;
import me.dinozoid.strife.property.Property;
import me.dinozoid.strife.property.implementations.DoubleProperty;
import me.dinozoid.strife.property.implementations.EnumProperty;
import me.dinozoid.strife.property.implementations.MultiSelectEnumProperty;
import me.dinozoid.strife.util.network.PacketUtil;
import me.dinozoid.strife.util.player.PlayerUtil;
import me.dinozoid.strife.util.player.RotationUtil;
import me.dinozoid.strife.util.render.RenderUtil;
import me.dinozoid.strife.util.system.MathUtil;
import me.dinozoid.strife.util.system.TimerUtil;
import me.dinozoid.strife.util.world.WorldUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.server.S30PacketWindowItems;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

@ModuleInfo(name = "KillAura", renderName = "KillAura", description = "Automatically attack entities.", aliases = "Aura", category = Category.COMBAT)
public final class KillAuraModule extends Module {

    private final EnumProperty<KillAuraMode> modeProperty = new EnumProperty<>("Mode", KillAuraMode.SINGLE);

    private final DoubleProperty switchDelayProperty = new DoubleProperty("Switch Delay", 50, 1, 1000, 20, Property.Representation.INT, () -> modeProperty.getValue() == KillAuraMode.SWITCH);
    private final EnumProperty<SortMode> sortProperty = new EnumProperty<>("Sort by", SortMode.HEALTH, () -> modeProperty.getValue() == KillAuraMode.SINGLE);

    private final DoubleProperty minAPSProperty = new DoubleProperty("Min APS", 11, 1, 20, 1, Property.Representation.INT);
    private final DoubleProperty maxAPSProperty = new DoubleProperty("Max APS", 14, 1, 20, 1, Property.Representation.INT);

    private final EnumProperty<AttackMode> attackModeProperty = new EnumProperty<>("Attack In", AttackMode.PRE);
    private final EnumProperty<BlockMode> blockModeProperty = new EnumProperty<>("Block Mode", BlockMode.HYPIXEL);

    private final DoubleProperty rangeProperty = new DoubleProperty("Range", 4.2, 1, 7, 0.1);
    private final DoubleProperty wallRangeProperty = new DoubleProperty("Wall Range", 4.2, 1, 7, 0.1);
    private final DoubleProperty blockRangeProperty = new DoubleProperty("Block Range", 4.2, 1, 10, 0.1);
    private final DoubleProperty fovRangeProperty = new DoubleProperty("Fov Range", 180, 1, 180, 10, Property.Representation.INT);

    private final MultiSelectEnumProperty<PlayerUtil.Target> targetsProperty = new MultiSelectEnumProperty<>("Targets", PlayerUtil.Target.PLAYERS);

    private final Property<Boolean> rayTraceProperty = new Property<>("Ray Trace", false);
    private final Property<Boolean> rotationsProperty = new Property<>("Rotations", true);
    private final Property<Boolean> lockViewProperty = new Property<>("Lock View", false);
    private final Property<Boolean> autoBlockProperty = new Property<>("Autoblock", true);
    private final Property<Boolean> targetHUDProperty = new Property<>("Target HUD", true);
    private final Property<Boolean> noSwingProperty = new Property<>("No Swing", false);
    private final Property<Boolean> keepSprintProperty = new Property<>("Keep Sprint", true);
    private final EnumProperty<TargetHUDMode> targetHUDModeProperty = new EnumProperty<>("Target HUD Mode", TargetHUDMode.STRIFE, targetHUDProperty::getValue);
    private final TimerUtil switchTimer = new TimerUtil();
    private final TimerUtil attackTimer = new TimerUtil();
    private final TimerUtil blockTimer = new TimerUtil();

    private boolean block;
    private int targetIndex, ticks;
    private EntityLivingBase target, hudTarget;
    private double animatedHealth, animatedOpacity, animatedScale;
    private final List<ItemStack> items = new ArrayList<>();
    private CustomFontRenderer font18, font15, font12;
    private float width, height, x = 400, y = 300, dragX, dragY;
    private float health, maxHealth;
    private boolean hovered, dragging;
    private NetworkPlayerInfo info;
    private boolean visible;
    private String name;
    private long start;

    public void draw(int mouseX, int mouseY, float partialTicks) {
        if(!toggled()) return;
        if (dragging) {
            x = (mouseX + dragX);
            y = (mouseY + dragY);
        }
        if(mc.currentScreen instanceof GuiChat)
            hovered = RenderUtil.inBounds(x, y, x + width, y + height, mouseX, mouseY);
    }

    public void onMouseClick(int mouseX, int mouseY, int mouseButton) {
        if(!toggled()) return;
        if (hovered) {
            if (mouseButton == 0) {
                dragging = true;
                dragX = (x - mouseX);
                dragY = (y - mouseY);
            }
        }
    }

    public void onMouseRelease(int mouseX, int mouseY, int state) {
        if(!toggled()) return;
        dragging = false;
        hovered = false;
    }

    @EventHandler
    private final Listener<PacketInboundEvent> packetOutboundListener = new Listener<>(event -> {
        if(mc.thePlayer.ticksExisted <= 5) return;
        switch (blockModeProperty.getValue()) {
            case HYPIXEL: {
                if (event.getPacket() instanceof S30PacketWindowItems && PlayerUtil.isHoldingSword() && block && blockModeProperty.getValue() == BlockMode.HYPIXEL) {
                    event.cancel();
                   block = false;
                }
                break;
            }
        }
    });

    @EventHandler
    private final Listener<Render3DEvent> render3DEventListener = new Listener<>(event -> {
        if(mc.thePlayer.ticksExisted <= 5) return;
        if (target != null && mc.theWorld != null && mc.thePlayer != null) {
            drawCircle(target, 0.66, true);
        }
    });

    @EventHandler
    private final Listener<Render2DEvent> render2DListener = new Listener<>(event -> {
        if(mc.thePlayer.ticksExisted <= 5) return;
        if (font15 == null)
            font15 = Client.INSTANCE.getFontRepository().fontBy("ProductSans").size(15);
        if (font12 == null)
            font12 = Client.INSTANCE.getFontRepository().fontBy("ProductSans").size(12);
        if (font18 == null)
            font18 = Client.INSTANCE.getFontRepository().fontBy("ProductSans").size(18);
        if (targetHUDProperty.getValue()) {
            if (mc.currentScreen instanceof GuiChat && mc.theWorld != null)
                hudTarget = mc.thePlayer;
            else hudTarget = target;

            if (hudTarget != null) {
                if (hudTarget instanceof EntityPlayer) {
                    NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(hudTarget.getUniqueID());
                    if (info != null)
                        this.info = info;
                } else info = null;
                health = hudTarget.getHealth();
                maxHealth = hudTarget.getMaxHealth();
                name = hudTarget.getName();
                items.clear();
                items.add(hudTarget.getCurrentArmor(3));
                items.add(hudTarget.getCurrentArmor(2));
                items.add(hudTarget.getCurrentArmor(1));
                items.add(hudTarget.getCurrentArmor(0));
                items.add(hudTarget.getHeldItem());
            }

            boolean visible = hudTarget != null;
            boolean open = visible && !this.visible;
            boolean closed = !visible && this.visible;

            if(open) {
                start = System.currentTimeMillis();
                this.visible = true;
            } else if(closed) {
                start = System.currentTimeMillis();
                this.visible = false;
                hudTarget = null;
            }

            float opacityAnim = Math.min(1, (System.currentTimeMillis() - start) / 500f);
            animatedOpacity = -1 * Math.pow(opacityAnim -1, 6) + 1;
            if(!visible)
                animatedOpacity = 1 - animatedOpacity;

            float scaleAnim = Math.min(1, (System.currentTimeMillis() - start) / 500f);
            animatedScale = -1 * Math.pow(scaleAnim -1, 8) + 1;
            if(!visible)
                animatedScale = 1 - animatedScale;

            glPushMatrix();
            glTranslatef(x, y, 0);
            glTranslatef(width / 2, height / 2, 0);
            glScaled(animatedScale, animatedScale, 1);
            glTranslatef(-x, -y, 0);
            glTranslatef(-width / 2, -height / 2, 0);
            if(mc.currentScreen instanceof GuiChat) {
                if(hovered)
                    RenderUtil.drawOutlinedRect(x, y, x + width, y + height, 2, new Color( 0, 0, 0, 150).getRGB(), new Color( 0, 0, 0, 160).getRGB());
                else RenderUtil.drawRect(x, y, x + width, y + height, new Color( 0, 0, 0, 100).getRGB());
            }
            if (animatedOpacity > 0) {
                width = Math.max(130, font15.getWidth(name));
                height = 37;
                float x = this.x;
                float y = this.y;
                switch (targetHUDModeProperty.getValue()) {
                    case STRIFE: {
                        RenderUtil.drawRect(x, y, x + width, y + height, new Color(0, 0, 0, MathHelper.clamp_int((int) (animatedOpacity * 255), 0, 180)).getRGB());
                        if (info != null && info.getLocationSkin() != null) {
                            glPushMatrix();
                            glEnable(GL_BLEND);
                            glDisable(GL_LIGHTING);
                            mc.getTextureManager().bindTexture(info.getLocationSkin());
                            glColor4f(1, 1, 1, (float) animatedOpacity);
                            Gui.drawScaledCustomSizeModalRect((int) x + 2, (int) y + 2, 8, 8, 8, 8, 33, 33, 64, 64);
                            glColor4f(1, 1, 1, 1);
                            glDisable(GL_BLEND);
                            glPopMatrix();
                            x += 32;
                        }
                        Color healthColor = Color.GREEN;
                        if (health < maxHealth / 2) healthColor = new Color(240, 255, 0);
                        if (health < maxHealth / 3) healthColor = Color.ORANGE;
                        if (health < maxHealth / 4) healthColor = Color.RED;
                        healthColor = new Color(healthColor.getRed(), healthColor.getGreen(), healthColor.getBlue(), (int)(animatedOpacity * 255));
                        float healthWidth = width - 32 - 5;
                        animatedHealth = RenderUtil.animate(hudTarget != null ? healthWidth * (health / maxHealth) : 0, animatedHealth, 0.05f);
                        glColor4f(1, 1, 1, 1);
                        glPushMatrix();
                        font18.drawString(name, x + 6, y + 4, new Color(255, 255, 255, (int)(animatedOpacity * 255)).getRGB());
                        glPopMatrix();
                        RenderUtil.drawRect(x + 5, y + height - 2 - 2, (float) (x + 3 + animatedHealth), y + height - 2, healthColor.getRGB());
                        int i = 0;
                        for(ItemStack item : items) {
                            if(item != null) {
                                glColor4f(1, 1, 1, (float) animatedOpacity);
                                glPushMatrix();
                                glDisable(GL_LIGHTING);
                                mc.getRenderItem().renderItemAndEffectIntoGUI(item, (!(item.getItem() instanceof ItemArmor) ? x + 5f : x + 2.5f) + (i * 16), y + 4 + font18.getHeight(name) + 2, (int)(animatedOpacity * 255));
                                glColor4f(1, 1, 1, 1);
                                glPopMatrix();
                                i++;
                            }
                        }
                        break;
                    }
                    case REMIX: {
                        break;
                    }
                    case EXHIBITION: {
                        break;
                    }
                    case NOVOLINE: {
                        break;
                    }
                }
            }
            glPopMatrix();
        }
    });

    public static KillAuraModule instance() {
        return Client.INSTANCE.getModuleRepository().moduleBy(KillAuraModule.class);
    }

    @Override
    public void init() {
        super.init();
        addValueChangeListener(modeProperty);
    }

    @Override
    public void onEnable() {
        switchTimer.reset();
        attackTimer.reset();
        blockTimer.reset();
        animatedScale = 0;
        animatedOpacity = 0;
        animatedHealth = 0;
        ticks = 0;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        targetIndex = 0;
        target = null;
    }

    public void directUpdate(PlayerMotionEvent event) {
        if(mc.thePlayer.ticksExisted <= 5){
         if(this.toggled()){
             this.toggle();
         }
         return;
        }
        if (!toggled()) return;
        if (ScaffoldModule.getInstance().toggled()) return;


//
//        if(target == null && PlayerUtil.isHoldingSword() && block && blockModeProperty.getValue() == BlockMode.HYPIXEL){
//            PacketUtil.sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
//            block = false;
//        }

        if (event.getState() == EventState.PRE) {

            EntityLivingBase optimalTarget = null;
            target = null;
            List<EntityLivingBase> livingEntities = WorldUtil.getLivingEntities(entity -> PlayerUtil.isValid(entity, targetsProperty) && distanceCheck(entity, true));
            if (livingEntities.size() > 1) {
                livingEntities.sort(sortProperty.getValue().sorter());
                if (livingEntities.size() > 0) {
                    if (modeProperty.getValue() == KillAuraMode.SWITCH) {
                        if (switchTimer.hasElapsed(switchDelayProperty.getValue().longValue())) {
                            targetIndex++;
                            switchTimer.reset();
                        }
                        if (targetIndex >= livingEntities.size()) {
                            targetIndex = 0;
                        }
                    }
                    optimalTarget = livingEntities.get(targetIndex);
                }
            } else if (livingEntities.size() == 1) optimalTarget = livingEntities.get(0);
            target = optimalTarget;
            if (target == null) return;
            if (autoBlockProperty.getValue() && block)
                unblock();
            if (distanceCheck(optimalTarget, false)) {
                final float[] rotations = RotationUtil.getRotationFromEntity(optimalTarget);
                if (Math.abs(RotationUtil.getYawDifference(rotations[0], mc.thePlayer.rotationYaw)) > fovRangeProperty.getValue() /*|| RotationUtil.getYawDifference(mc.thePlayer.rotationYaw, rotations[0]) > fovRangeProperty.getValue()*/) {
                    return;
                }
                if (!wallRangeProperty.getValue().equals(rangeProperty.getValue()) || rayTraceProperty.getValue()) {
                    EntityLivingBase raycastedEntity = WorldUtil.raycast(rangeProperty.getValue(), rotations, true);
                    if (raycastedEntity != null && !(raycastedEntity == optimalTarget) && rayTraceProperty.getValue()) {
                        return;
                    }
                }
                if (rotationsProperty.getValue()) {
                    event.setYaw(rotations[0]);
                    event.setPitch(rotations[1]);
                    if (lockViewProperty.getValue()) {
                        mc.thePlayer.rotationYaw = rotations[0];
                        mc.thePlayer.rotationPitch = rotations[1];
                    }
                }
                if (wallRangeProperty.getValue().equals(rangeProperty.getValue()) || WorldUtil.lastRaycastRange() <= wallRangeProperty.getValue()) {
                    if (attackModeProperty.getValue() == AttackMode.PRE || (attackModeProperty.getValue() == AttackMode.HVH && mc.thePlayer.ticksExisted % 10 != 0)) {
                        attack(optimalTarget);
                    }
                }
            }
        } else {
            boolean canBlock = autoBlockProperty.getValue() && distanceCheck(target, true) && PlayerUtil.isHoldingSword();
            if (PlayerUtil.isValid(target, targetsProperty) && target.getDistanceToEntity(mc.thePlayer) <= rangeProperty.getValue()) {
                if (WorldUtil.lastRaycastRange() <= wallRangeProperty.getValue() && distanceCheck(target, false)) {
                    if (attackModeProperty.getValue() == AttackMode.POST || (attackModeProperty.getValue() == AttackMode.HVH && mc.thePlayer.ticksExisted % 10 == 0)) {
                        attack(target);
                    }
                }
            }
            if (canBlock && !block && blockModeProperty.getValue() == BlockMode.HYPIXEL) block();
        }
    }

    private void attack(EntityLivingBase entity) {
        int min = minAPSProperty.getValue().intValue();
        int max = maxAPSProperty.getValue().intValue();
        int cps;

        if (min == max) cps = min;
        else cps = MathUtil.randomInt(max + 1, min);

        if (attackTimer.hasElapsed(1000 / cps)) {
            if (noSwingProperty.getValue()) {
                PacketUtil.sendPacketNoEvent(new C0APacketAnimation());
            } else mc.thePlayer.swingItem();
            if (keepSprintProperty.getValue()) {
                PacketUtil.sendPacketNoEvent(new C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK));
            } else {
                mc.playerController.attackEntity(mc.thePlayer, entity);
            }
            attackTimer.reset();
           // blockTimer.reset();
            ticks++;
        }
    }

    private void unblock() {
        if (ticks == 0 && PlayerUtil.isHoldingSword() && blockTimer.hasElapsed(200) && blockModeProperty.getValue() == BlockMode.VERUS) {
            PacketUtil.sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
            block = false;
        }
    }

    private void block() {
        switch (blockModeProperty.getValue()) {
            case HYPIXEL: {
                mc.playerController.syncCurrentPlayItem();
               PacketUtil.sendPacketNoEvent(new C02PacketUseEntity(C02PacketUseEntity.Action.INTERACT));
                PacketUtil.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.getCurrentEquippedItem()));
                break;
            }
            case VERUS: {
                if (ticks >= 1) {
                    mc.playerController.syncCurrentPlayItem();
                    PacketUtil.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.getCurrentEquippedItem()));
                }
                break;
            }
        }
        block = true;
    }

    private boolean distanceCheck(EntityLivingBase entityLivingBase, boolean block) {
        return mc.thePlayer != null && entityLivingBase != null && mc.thePlayer.getDistanceToEntity(entityLivingBase) < (block ? Math.max(rangeProperty.getValue(), blockRangeProperty.getValue()) : rangeProperty.getValue());
    }

    private void drawCircle(Entity entity, double rad, boolean shade) {
        glPushMatrix();
        glDisable(GL_TEXTURE_2D);
        glEnable(GL_LINE_SMOOTH);
        glEnable(GL_POINT_SMOOTH);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST);
        glHint(GL_POINT_SMOOTH_HINT, GL_NICEST);
        glDepthMask(false);
        GlStateManager.alphaFunc(GL_GREATER, 0);
        if (shade) glShadeModel(GL_SMOOTH);
        GlStateManager.disableCull();
        glBegin(GL_TRIANGLE_STRIP);

        final double x = RenderUtil.interpolate(entity.posX, entity.lastTickPosX, mc.timer.renderPartialTicks) - mc.getRenderManager().renderPosX;
        final double y = (RenderUtil.interpolate(entity.posY, entity.lastTickPosY, mc.timer.renderPartialTicks) - mc.getRenderManager().renderPosY) + Math.sin(System.currentTimeMillis() / 4E+2) + 1;
        final double z = RenderUtil.interpolate(entity.posZ, entity.lastTickPosZ, mc.timer.renderPartialTicks) - mc.getRenderManager().renderPosZ;

        Color c;
        for (float i = 0; i < Math.PI * 2; i += Math.PI * 2 / 64) {
            final double vecX = x + rad * Math.cos(i);
            final double vecZ = z + rad * Math.sin(i);
            c = new Color(OverlayModule.getColor(Math.round(i * 100)));

            if (shade) {
                glColor4f(c.getRed() / 255F,
                        c.getGreen() / 255F,
                        c.getBlue() / 255F,
                        0
                );
                glVertex3d(vecX, y - Math.cos(System.currentTimeMillis() / 4E+2) / 2, vecZ);
                glColor4f(c.getRed() / 255F,
                        c.getGreen() / 255F,
                        c.getBlue() / 255F,
                        0.85F
                );
            }
            glVertex3d(vecX, y, vecZ);
        }
        glEnd();
        if (shade) glShadeModel(GL_FLAT);
        glDepthMask(true);
        glEnable(GL_DEPTH_TEST);
        GlStateManager.alphaFunc(GL_GREATER, 0.1F);
        GlStateManager.enableCull();
        glDisable(GL_LINE_SMOOTH);
        glDisable(GL_POINT_SMOOTH);
        glEnable(GL_BLEND);
        glEnable(GL_TEXTURE_2D);
        glPopMatrix();
        glColor3f(255, 255, 255);
    }

    public EntityLivingBase target() {
        return target;
    }

    private enum SortMode {
        HEALTH(new HealthSorter()), ARMOR(new ArmorSorter()), FOV(new FovSorter()), HURTTIME(new HurtTimeSorter()), DISTANCE(new DistanceSorter());

        private final Comparator<EntityLivingBase> sorter;

        SortMode(Comparator<EntityLivingBase> sorter) {
            this.sorter = sorter;
        }

        public Comparator<EntityLivingBase> sorter() {
            return sorter;
        }
    }

    private enum KillAuraMode {
        SINGLE, SWITCH
    }

    private enum AttackMode {
        PRE, POST, HVH
    }

    private enum TargetHUDMode {
        STRIFE, EXHIBITION, REMIX, NOVOLINE
    }

    private enum BlockMode {
        HYPIXEL, VERUS
    }

    private final static class HealthSorter implements Comparator<EntityLivingBase> {
        public int compare(EntityLivingBase o1, EntityLivingBase o2) {
            return Double.compare(PlayerUtil.getEffectiveHealth(o1), PlayerUtil.getEffectiveHealth(o2));
        }
    }

    private final static class ArmorSorter implements Comparator<EntityLivingBase> {
        public int compare(EntityLivingBase o1, EntityLivingBase o2) {
            return Double.compare(o1.getTotalArmorValue(), o2.getTotalArmorValue());
        }
    }

    private final static class FovSorter implements Comparator<EntityLivingBase> {
        public int compare(EntityLivingBase o1, EntityLivingBase o2) {
            return Double.compare(o1.getPositionVector().subtract(mc.thePlayer.getPositionEyes(mc.timer.renderPartialTicks)).angle(mc.thePlayer.getLookVec()), o2.getPositionVector().subtract(mc.thePlayer.getPositionEyes(mc.timer.renderPartialTicks)).angle(mc.thePlayer.getLookVec()));
        }
    }

    private final static class HurtTimeSorter implements Comparator<EntityLivingBase> {
        public int compare(EntityLivingBase o1, EntityLivingBase o2) {
            return Double.compare(PlayerUtil.MAX_HURT_RESISTANT_TIME - o1.hurtResistantTime, PlayerUtil.MAX_HURT_RESISTANT_TIME - o2.hurtResistantTime);
        }
    }

    private final static class DistanceSorter implements Comparator<EntityLivingBase> {
        public int compare(EntityLivingBase o1, EntityLivingBase o2) {
            return -Double.compare(mc.thePlayer.getDistanceToEntity(o1), mc.thePlayer.getDistanceToEntity(o2));
        }
    }

    public Property<Boolean> autoBlockProperty() {
        return autoBlockProperty;
    }
    public MultiSelectEnumProperty<PlayerUtil.Target> targetsProperty() {
        return targetsProperty;
    }
}
