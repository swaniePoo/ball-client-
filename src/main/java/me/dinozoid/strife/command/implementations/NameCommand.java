package me.dinozoid.strife.command.implementations;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.command.Command;
import me.dinozoid.strife.command.CommandInfo;
import me.dinozoid.strife.command.argument.Argument;
import me.dinozoid.strife.util.player.PlayerUtil;

import java.util.Arrays;
import java.util.List;

@CommandInfo(name = "Name", aliases = "ClientName")
public class NameCommand extends Command {
    @Override
    public boolean execute(String[] args, String label) {
        Client.CUSTOMNAME = args[0];
        PlayerUtil.sendMessageWithPrefix("&7You renamed the client to &c" + args[0] + ".");
        return true;
    }

    @Override
    public List<Argument> arguments(String[] args) {
        return Arrays.asList(new Argument(String.class, "Name"));
    }
}
