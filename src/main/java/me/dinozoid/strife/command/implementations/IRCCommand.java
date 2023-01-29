package me.dinozoid.strife.command.implementations;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.command.Command;
import me.dinozoid.strife.command.CommandInfo;
import me.dinozoid.strife.command.argument.Argument;
import me.dinozoid.strife.module.implementations.misc.IRCModule;
import me.dinozoid.strife.util.player.PlayerUtil;
import me.dinozoid.strife.websocket.packet.implementations.CChatPacket;
import me.dinozoid.strife.websocket.packet.implementations.CServerCommandPacket;

import java.util.Arrays;
import java.util.List;

@CommandInfo(name = "IRC", description = "Send messages in irc.", aliases = "c")
public class IRCCommand extends Command {

    @Override
    public boolean execute(String[] args, String label) {
        if (Client.INSTANCE.getModuleRepository().moduleBy(IRCModule.class).toggled()) {
            switch (args[0].toLowerCase()) {
                case "users": {
                    Client.INSTANCE.getSocketClient().packetHandler().sendPacket(new CServerCommandPacket(CServerCommandPacket.CommandOperation.LIST_USERS, "", "irc"));
                    break;
                }
                default: {
                    StringBuilder stringBuilder = new StringBuilder();
                    Arrays.stream(args).forEach(arg -> stringBuilder.append(arg).append(" "));
                    Client.INSTANCE.getSocketClient().packetHandler().sendPacket(new CChatPacket(stringBuilder.toString()));
                    break;
                }
            }
        } else {
            PlayerUtil.sendMessageWithPrefix("&7You don't have IRC toggled, please toggle it to use irc.");
        }
        return true;
    }
    @Override
    public List<Argument> arguments(String[] args) {
        return Arrays.asList(new Argument(String.class, "Message"));
    }
}
