package me.dinozoid.strife.command.implementations;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.command.Command;
import me.dinozoid.strife.command.CommandInfo;
import me.dinozoid.strife.command.argument.Argument;
import me.dinozoid.strife.ui.notification.Notification;

import java.util.List;

@CommandInfo(name = "Reload", aliases = "rl", description = "Add a target.")
public class ReloadCommand extends Command {
    @Override
    public boolean execute(String[] args, String label) {
        Client.INSTANCE.shutdown(false);
        Client.INSTANCE.startup(true);
        Client.INSTANCE.getNotificationRepository().display(new Notification(Notification.NotificationType.SUCCESS, "Reload", "Reloaded the client", 1000));
        return true;
    }

    @Override
    public List<Argument> arguments(String[] args) {
        return null;
    }
}
