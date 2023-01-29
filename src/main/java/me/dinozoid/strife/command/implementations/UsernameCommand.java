package me.dinozoid.strife.command.implementations;

import me.dinozoid.strife.command.Command;
import me.dinozoid.strife.command.CommandInfo;
import me.dinozoid.strife.command.argument.Argument;
import me.dinozoid.strife.util.player.PlayerUtil;
import me.dinozoid.strife.util.system.StringUtil;

import java.util.List;

@CommandInfo(name = "Username", description = "Copies your current username to the clipboard.", aliases = "user")
public class UsernameCommand extends Command {

    @Override
    public boolean execute(String[] args, String label) {
        StringUtil.clipboardContents(mc.session.getUsername());
        PlayerUtil.sendMessageWithPrefix("&7Your username has been copied to the clipboard.");
        return true;
    }

    @Override
    public List<Argument> arguments(String[] args) {
        return null;
    }
}
