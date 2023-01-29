package me.dinozoid.strife.command;

import me.dinozoid.strife.command.implementations.*;
import me.dinozoid.strife.module.Module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandRepository {

    private final List<Command> COMMANDS = new ArrayList<>();

    public void init() {
        COMMANDS.clear();
        COMMANDS.addAll(Arrays.asList(new HelpCommand(),
                new ToggleCommand(),
                new BindCommand(),
                new FriendCommand(),
                new TargetCommand(),
                new IRCCommand(),
                new ConfigCommand(),
                new UsernameCommand(),
                new PanicCommand(),
                new HideCommand(),
                new NameCommand(),
                new FontCommand(),
                new ReloadCommand()));
    }

    public <T extends Command> T commandBy(Class<T> tClass) {
        return (T) COMMANDS.stream().filter(command -> command.getClass().equals(tClass)).findFirst().orElse(null);
    }

    public <T extends Command> T commandBy(String name) {
        return (T) COMMANDS.stream().filter(command -> command.name().equals(name)).findFirst().orElse(null);
    }

    public List<Command> commands() {
        return COMMANDS;
    }
}
