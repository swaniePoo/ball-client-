package me.dinozoid.strife.config;

import com.google.gson.*;
import me.dinozoid.strife.Client;
import me.dinozoid.strife.DragManager;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.property.Property;
import me.dinozoid.strife.property.implementations.ColorProperty;
import me.dinozoid.strife.property.implementations.DoubleProperty;
import me.dinozoid.strife.property.implementations.EnumProperty;
import me.dinozoid.strife.property.implementations.MultiSelectEnumProperty;
import me.dinozoid.strife.util.player.PlayerUtil;

import java.awt.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public final class Config {

    private String name;

    public Config(String name) {
        this.name = name;
    }

    public boolean save() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject jsonObject = new JsonObject();
        DragManager.saveDragData();
        Client.INSTANCE.getModuleRepository().modules().forEach(module -> {
            JsonObject moduleObject = new JsonObject();
            moduleObject.addProperty("Keybind", module.key());
            moduleObject.addProperty("Enabled", module.toggled());
            moduleObject.addProperty("Hidden", module.hidden());
            JsonObject settingsObject = new JsonObject();
            Module.propertyRepository().propertiesBy(module.getClass()).forEach(property -> {
                if (property instanceof DoubleProperty)
                    settingsObject.addProperty(property.getLabel(), ((DoubleProperty) property).getValue());
//                if (property instanceof ColorProperty) {
//                    Color color = (Color) property.getValue();
//                    JsonObject colorObject = new JsonObject();
//                    colorObject.addProperty("Red", color.getRed());
//                    colorObject.addProperty("Green", color.getGreen());
//                    colorObject.addProperty("Blue", color.getBlue());
//                    colorObject.addProperty("Alpha", color.getAlpha());
//                    settingsObject.add(property.getLabel(), colorObject);
//                }
                if (property.getValue() instanceof Enum)
                    settingsObject.addProperty(property.getLabel(), String.valueOf(property.getValue()));
                if (property.getValue() instanceof Boolean)
                    settingsObject.addProperty(property.getLabel(), ((Property<Boolean>) property).getValue());
                if (property instanceof MultiSelectEnumProperty) {
                    MultiSelectEnumProperty castedProperty = (MultiSelectEnumProperty) property;
                    settingsObject.add(property.getLabel(), gson.toJsonTree(Arrays.stream(castedProperty.values()).filter(castedProperty::selected).collect(Collectors.toList()), property.type()));
                }
            });
            moduleObject.add("Settings", settingsObject);
            jsonObject.add(module.name(), moduleObject);
        });
        try {
            FileWriter fileWriter = new FileWriter(ConfigRepository.CONFIG_DIRECTORY + "/" + name + ".json");
            gson.toJson(jsonObject, fileWriter);
            fileWriter.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean load() {
        try {
            DragManager.loadDragData();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileReader reader = new FileReader(ConfigRepository.CONFIG_DIRECTORY + "/" + name + ".json");
            JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();
            reader.close();
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                Module module = Client.INSTANCE.getModuleRepository().moduleBy(entry.getKey());
                if (module != null) {
                    final JsonObject jsonModule = (JsonObject) entry.getValue();
                    boolean toggled = jsonModule.get("Enabled").getAsBoolean();
                    if (module.toggled() != toggled)
                        module.toggled(toggled);
                    module.key(jsonModule.get("Keybind").getAsInt());
                    module.hidden(jsonModule.get("Hidden").getAsBoolean());
                    for (Map.Entry<String, JsonElement> setting : jsonModule.get("Settings").getAsJsonObject().entrySet()) {
                        Property property = Module.propertyRepository().propertyBy(module.getClass(), setting.getKey());
                        if (property != null) {
                            if (property instanceof DoubleProperty)
                                property.setValue(setting.getValue().getAsDouble());
                            if (property instanceof EnumProperty) {
                                property.setValue(Enum.valueOf(property.type(), setting.getValue().getAsString()));
                            }
                            if (property.getValue() instanceof Boolean)
                                property.setValue(setting.getValue().getAsBoolean());
//                            if (property instanceof ColorProperty) {
//                                JsonObject colorObject = setting.getValue().getAsJsonObject();
//                                if(colorObject.has("Red") && colorObject.has("Green") && colorObject.has("Blue") && colorObject.has("Alpha")) {
//                                    property.setValue(new Color(colorObject.get("Red").getAsInt(), colorObject.get("Green").getAsInt(),
//                                            colorObject.get("Blue").getAsInt(), colorObject.get("Alpha").getAsInt()));
//
//                                    PlayerUtil.sendMessageWithPrefix("RED: " + colorObject.get("Red").getAsString());
//                                }
//                            }
                            if (property instanceof MultiSelectEnumProperty) {
                                MultiSelectEnumProperty castedProperty = (MultiSelectEnumProperty) property;
                                JsonElement jsonElement = setting.getValue();
                                if (jsonElement.isJsonArray()) {
                                    for (JsonElement element : jsonElement.getAsJsonArray()) {
                                        PlayerUtil.sendMessage(element.getAsString());
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete() {
        try {
            Files.delete(Paths.get(ConfigRepository.CONFIG_DIRECTORY + "/" + name + ".json"));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String name() {
        return name;
    }

    public void name(String name) {
        this.name = name;
    }
}
