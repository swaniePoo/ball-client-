package me.dinozoid.strife.command.implementations;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.command.Command;
import me.dinozoid.strife.command.CommandInfo;
import me.dinozoid.strife.command.argument.Argument;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.ui.notification.Notification;

import java.util.Arrays;
import java.util.List;

@CommandInfo(name = "Hide", description = "Hides a module on the arraylist.")
public class HideCommand extends Command {


    @Override
    public boolean execute(String[] args, String label) {
        Module module = Client.INSTANCE.getModuleRepository().moduleBy(args[0]);
        if(module != null) {
            module.hidden(!module.hidden());
            Client.INSTANCE.getNotificationRepository().display(new Notification(Notification.NotificationType.SUCCESS, "Hide", module.name() + " is now " + (module.hidden() ? "hidden." : "visible.")));
        }
        return true;
    }

    @Override
    public List<Argument> arguments(String[] args) {
        return Arrays.asList(new Argument(String.class, "Module"));
    }
}
