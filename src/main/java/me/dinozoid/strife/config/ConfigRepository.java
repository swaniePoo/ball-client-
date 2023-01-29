package me.dinozoid.strife.config;

import me.dinozoid.strife.Client;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public final class ConfigRepository {

    public static Path CONFIG_DIRECTORY = null;
    private final Map<Config, File> CONFIGS = new HashMap<>();

    private Config currentConfig;

    public void init() {
        CONFIG_DIRECTORY = Paths.get(String.valueOf(Client.DIRECTORY), "configs");
        CONFIGS.clear();
        try {
            CONFIG_DIRECTORY.toFile().mkdirs();
            Files.list(CONFIG_DIRECTORY).map(Path::toFile).forEach(file -> CONFIGS.put(new Config(file.getName().replace(".json", "")), file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean load(Config config) {
        if (config != null)
            if (config.load()) {
                currentConfig = config;
                return true;
            }
        return false;
    }

    public boolean add(Config config) {
        CONFIGS.put(config, Paths.get(String.valueOf(Client.DIRECTORY), "configs", config.name()).toFile());
        return config.save();
    }

    public boolean remove(Config config) {
        CONFIGS.remove(config);
        return config.delete();
    }

    public boolean save(Config config) {
        if (config != null)
            return config.save();
        return false;
    }

    public boolean load(String name) {
        return load(configBy(name));
    }

    public boolean save(String name) {
        return save(configBy(name));
    }

    public Config configBy(String name) {
        return CONFIGS.keySet().stream().filter(config -> config.name().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Map<Config, File> configs() {
        return CONFIGS;
    }

    public Config currentConfig() {
        return currentConfig;
    }

}
