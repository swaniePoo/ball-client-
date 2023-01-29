package me.dinozoid.strife.module.implementations.movement;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.player.MovePlayerEvent;
import me.dinozoid.strife.event.implementations.player.PlayerJumpEvent;
import me.dinozoid.strife.event.implementations.player.PlayerMotionEvent;
import me.dinozoid.strife.event.implementations.player.PlayerStrafeEvent;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.module.implementations.combat.TargetStrafeModule;
import me.dinozoid.strife.module.implementations.player.ScaffoldModule;
import me.dinozoid.strife.property.implementations.DoubleProperty;
import me.dinozoid.strife.property.implementations.EnumProperty;
import me.dinozoid.strife.util.network.PacketUtil;
import me.dinozoid.strife.util.player.MovementUtil;
import me.dinozoid.strife.util.player.PlayerUtil;
import me.dinozoid.strife.util.player.RotationUtil;
import me.dinozoid.strife.util.system.MathUtil;
import net.minecraft.network.play.client.C03PacketPlayer;

@ModuleInfo(name = "Speed", renderName = "Speed", description = "Move faster.", category = Category.MOVEMENT)
public class SpeedModule extends Module {

    private final EnumProperty<SpeedMode> modeProperty = new EnumProperty<>("Mode", SpeedMode.HYPIXEL);
    private final DoubleProperty frictionProperty = new DoubleProperty("Vanilla Speed", 2, 0.1, 10, 0.1, () -> modeProperty.getValue() == SpeedMode.VANILLA);
    private final DoubleProperty timerSpeedProperty = new DoubleProperty("Timer Speed", 1, 0.1, 1.6, 0.01, () -> modeProperty.getValue() == SpeedMode.HYPIXEL_STRAFE);
    private int stage, abuseStage;
    private double moveSpeed;
    private float yaw;

    @EventHandler
    private final Listener<PlayerStrafeEvent> strafePlayerListener = new Listener<>(event -> {
        switch (modeProperty.getValue()) {
            case HYPIXEL_STRAFE: {
                double baseSpeed = MovementUtil.getBaseMoveSpeed();
                if (MovementUtil.isMoving()) {
                    if(!MovementUtil.isOnGround(1 / 64) && mc.thePlayer.ticksExisted > 5)
                        mc.timer.timerSpeed = timerSpeedProperty.getValue().floatValue() + MovementUtil.getRandomHypixelValuesFloat();

                    if (MovementUtil.isOnGround()) {
                        mc.thePlayer.motionY = MovementUtil.getJumpHeight(0.42F);
                        stage = 0;
                    }
                    switch (stage) {
                        case 0: {
                            moveSpeed = baseSpeed * 2.15;
                            break;
                        }
                        case 1: {
                            moveSpeed *= 0.58;
                            break;
                        }
                        case 4: {
                            moveSpeed = baseSpeed * 1.2;
                            break;
                        }
                        default: {
                            moveSpeed = moveSpeed / 100 * 98.5f;
                            break;
                        }
                    }
                    stage++;
                    if(Client.INSTANCE.getModuleRepository().moduleBy(TargetStrafeModule.class).shouldStrafe()){
                        event.setYaw(PlayerUtil.calculateYawFromSrcToDst(mc.thePlayer.rotationYaw, mc.thePlayer.posX, mc.thePlayer.posZ, Client.INSTANCE.getModuleRepository().moduleBy(TargetStrafeModule.class).currentPoint.point.xCoord, Client.INSTANCE.getModuleRepository().moduleBy(TargetStrafeModule.class).currentPoint.point.zCoord));
                    }

                    event.setMotionPartialStrafe((float)Math.max(baseSpeed, moveSpeed), 0.235F + (float)MovementUtil.getRandomHypixelValues());
                } else {
                    event.setMotion(0);
                }
                break;
            }
        }
    });

    @EventHandler
    private final Listener<MovePlayerEvent> movePlayerListener = new Listener<>(event -> {
        switch (modeProperty.getValue()) {
            case VANILLA: {
                if (MovementUtil.isMoving()) {
                    if(mc.thePlayer.onGround){
                        double x = mc.thePlayer.posX - mc.thePlayer.lastTickPosX;
                        double z = mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ;
                        PacketUtil.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + x, mc.thePlayer.posX, mc.thePlayer.posZ + z, true));
                        //mc.thePlayer.setPosition(mc.thePlayer.posX + xz[0], mc.thePlayer.posY, mc.thePlayer.posZ + xz[1]);

                        //MovementUtil.setSpeed(event, MovementUtil.getBaseMoveSpeed() * 1.7);
                    }
                }
                break;
            }
            case HYPIXEL: {
                double baseSpeed = MovementUtil.getBaseMoveSpeed();
                if (MovementUtil.isMoving()) {
                    if (MovementUtil.isOnGround()) {
                        event.setY(mc.thePlayer.motionY = MovementUtil.getJumpHeight(0.42F));
                        stage = 0;
                    }
                    switch (stage) {
                        case 0: {
                            moveSpeed = baseSpeed * 2.15;
                            break;
                        }
                        case 1: {
                            moveSpeed *= 0.58;
                            break;
                        }
                        case 4: {
                            moveSpeed = baseSpeed * 1.2;
                            break;
                        }
                        default: {
                            moveSpeed = moveSpeed / 100 * 98.5f;
                            break;
                        }
                    }
                    this.stage++;
                    double[] move = MovementUtil.yawPos(mc.thePlayer.getDirection(), Math.max(baseSpeed, moveSpeed));
                    event.setX(move[0]);
                    event.setZ(move[1]);
                } else {
                    event.setX(mc.thePlayer.motionX = 0);
                    event.setZ(mc.thePlayer.motionZ = 0);
                }
                break;
            }
            case AGC: {
                if (mc.thePlayer.hurtTime == 1) return;
                event.setY(mc.thePlayer.motionY = .0);
                MovementUtil.setSpeed(event, (mc.thePlayer.ticksExisted % 4 == 0) ? MovementUtil.getBaseMoveSpeed() * 1.5f : MovementUtil.getBaseMoveSpeed());
                break;
            }
        }
    });

    @EventHandler
    private final Listener<PlayerJumpEvent> playerJumpEventListener = new Listener<>(event -> {
        event.setCancelled(modeProperty.getValue() == SpeedMode.HYPIXEL || modeProperty.getValue() == SpeedMode.HYPIXEL_STRAFE);
    });

    public static SpeedModule instance() {
        return Client.INSTANCE.getModuleRepository().moduleBy(SpeedModule.class);
    }

    @Override
    public void init() {
        super.init();
        addValueChangeListener(modeProperty);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        moveSpeed = 0;
        abuseStage = 0;
        stage = 0;
        yaw = mc.thePlayer.rotationYaw;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if(mc.thePlayer == null) return;
        MovementUtil.setSpeed(0, 0, 0, mc.thePlayer.rotationYaw);
        mc.timer.timerSpeed = 1.0f;
    }

    private enum SpeedMode {
        HYPIXEL, HYPIXEL_STRAFE, VANILLA, AGC
    }

}