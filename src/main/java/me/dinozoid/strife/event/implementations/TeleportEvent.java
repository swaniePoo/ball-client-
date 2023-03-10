package me.dinozoid.strife.event.implementations;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.dinozoid.strife.event.Event;
import net.minecraft.network.play.client.C03PacketPlayer;

@Getter
@Setter
@AllArgsConstructor
public final class TeleportEvent extends Event {
    private C03PacketPlayer.C06PacketPlayerPosLook response;
    private double posX;
    private double posY;
    private double posZ;
    private float yaw;
    private float pitch;
}
