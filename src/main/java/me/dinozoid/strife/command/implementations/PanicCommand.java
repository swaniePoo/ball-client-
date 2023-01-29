package me.dinozoid.strife.command.implementations;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.command.Command;
import me.dinozoid.strife.command.CommandInfo;
import me.dinozoid.strife.command.argument.Argument;
import me.dinozoid.strife.module.implementations.visuals.OverlayModule;
import me.dinozoid.strife.ui.notification.Notification;

import java.util.List;

@CommandInfo(name = "Panic", description = "Disables all modules.", aliases = "p")
public class PanicCommand extends Command {


    @Override
    public boolean execute(String[] args, String label) {
        Client.INSTANCE.getModuleRepository().modules().forEach(module -> {
            if (!module.getClass().equals(OverlayModule.class))
                module.toggled(false);
        });
        Client.INSTANCE.getNotificationRepository().display(new Notification(Notification.NotificationType.ERROR, "Panic", "All modules have been disabled."));
        return true;
    }

    @Override
    public List<Argument> arguments(String[] args) {
        return null;
    }
}
