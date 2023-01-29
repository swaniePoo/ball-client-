package me.dinozoid.strife.module.implementations.player.hackerdetector;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.ui.notification.Notification;
import me.dinozoid.strife.util.MinecraftUtil;
import net.minecraft.entity.EntityLivingBase;

public abstract class Check extends MinecraftUtil {
    public double buffer;
    private final String name;
    private final String type;
    private final boolean experimental;

    public Check(String name, String type, boolean experimental) {
        this.name = name;
        this.type = type;
        this.experimental = experimental;
    }

    public void fail(EntityLivingBase player, String info) {
        //PlayerUtil.sendMessage("&c" + player.getName() + " &7failed &c" + this.name + " (" + this.type + ")" + ((experimental) ? " [Experimental]" : "") + ((info != "") ? ". &7[" + info + "]" : "."));
        if (player.getName().equals(mc.thePlayer.getName())) return;
//        Client.INSTANCE.notificationRepository().display(new Notification(Notification.NotificationType.INFO, "Check failed",
//                "�c" + player.getName() + " �7failed �c" + this.name + "(" + this.type + ")" + ((experimental) ? " [Experimental]" : "") + ((info != "") ? ". �7[" + info + "]" : ""), 2000));
    }

    public abstract void handleCheck(EntityLivingBase player);
}
