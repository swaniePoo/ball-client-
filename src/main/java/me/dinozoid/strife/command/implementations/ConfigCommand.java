package me.dinozoid.strife.command.implementations;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.command.Command;
import me.dinozoid.strife.command.CommandInfo;
import me.dinozoid.strife.command.argument.Argument;
import me.dinozoid.strife.command.argument.implementations.MultiChoiceArgument;
import me.dinozoid.strife.config.Config;
import me.dinozoid.strife.config.ConfigRepository;
import me.dinozoid.strife.util.player.PlayerUtil;
import me.dinozoid.strife.util.system.FolderUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@CommandInfo(name = "Config", aliases = {"configs", "cfg"}, description = "Handle configs.")
public final class ConfigCommand extends Command {

    private boolean confirmSave;

    @Override
    public boolean execute(String[] args, String label) {
        if (label.equalsIgnoreCase("configs")) args[0] = "list";
        switch (args[0].toLowerCase()) {
            case "create":
                if (Client.INSTANCE.getConfigRepository().add(new Config(args[1])))
                    PlayerUtil.sendMessageWithPrefix("&c" + args[1] + " &7has been created.");
                else PlayerUtil.sendMessageWithPrefix("&c" + args[1] + " &7 could not be created.");
                break;
            case "delete":
            case "remove":
                Config config = Client.INSTANCE.getConfigRepository().configBy(args[1]);
                Client.INSTANCE.getConfigRepository().remove(config);
                break;
            case "load":
                Client.INSTANCE.getConfigRepository().init();
                config = Client.INSTANCE.getConfigRepository().configBy(args[1]);
                if (config != null) {
                    if (Client.INSTANCE.getConfigRepository().load(config))
                        PlayerUtil.sendMessageWithPrefix("&c" + config.name() + " &7has been loaded.");
                    else PlayerUtil.sendMessageWithPrefix("&c" + config.name() + " &7failed to load.");
                } else PlayerUtil.sendMessageWithPrefix("&7Config not found.");
                break;
            case "save":
                config = Client.INSTANCE.getConfigRepository().configBy(args[1]);
                if (config != null) {
                    if (confirmSave) {
                        if (Client.INSTANCE.getConfigRepository().save(config))
                            PlayerUtil.sendMessageWithPrefix("&c" + config.name() + " &7has been saved.");
                        else PlayerUtil.sendMessageWithPrefix("&c" + config.name() + " &7failed to save.");
                        confirmSave = false;
                    } else {
                        PlayerUtil.sendMessageWithPrefix("&7This config already exists, are you sure you want to save?");
                        confirmSave = true;
                    }
                } else PlayerUtil.sendMessageWithPrefix("&7Config not found.");
                break;
            case "list":
                Client.INSTANCE.getConfigRepository().init();
                PlayerUtil.sendMessage(" ");
                StringBuilder stringBuilder = new StringBuilder();
                Set<Config> configs = Client.INSTANCE.getConfigRepository().configs().keySet();
                if (!configs.isEmpty()) {
                    PlayerUtil.sendMessage("&7All available configs.");
                    for (Config conf : configs)
                        stringBuilder.append("&c" + conf.name()).append(Client.INSTANCE.getConfigRepository().currentConfig() == conf ? " (Loaded)" : "").append("&7, ");
                    PlayerUtil.sendMessage(stringBuilder.substring(0, stringBuilder.length() - 2));
                } else PlayerUtil.sendMessage("&7No available configs.");
                PlayerUtil.sendMessage(" ");
                break;
            case "folder":
                FolderUtil.openFolder(ConfigRepository.CONFIG_DIRECTORY.toFile());
                break;
        }
        return true;
    }

    @Override
    public List<Argument> arguments(String[] args) {
        return Arrays.asList(new MultiChoiceArgument(String.class, "Operation", "Create", "Remove", "Delete", "Save", "Load", "List", "Folder"), new Argument(String.class, "Name", () -> !(args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("folder"))));
    }
}
