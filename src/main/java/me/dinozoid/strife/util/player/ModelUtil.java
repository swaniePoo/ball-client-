package me.dinozoid.strife.util.player;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.src.Config;
import net.optifine.player.PlayerItemModel;
import net.optifine.player.PlayerItemParser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStreamReader;

public class ModelUtil {

    public static BufferedImage downloadTextureImage(String texture) {
        String location = "assets/minecraft/strife/models/textures/" + texture + ".png";
        try {
            BufferedImage bufferedImage = ImageIO.read(ModelUtil.class.getClassLoader().getResourceAsStream(location));
            return bufferedImage;
        } catch (IOException ioexception) {
            Config.warn("Error loading item texture " + texture + ": " + ioexception.getClass().getName() + ": " + ioexception.getMessage());
            return null;
        }
    }

    public static PlayerItemModel downloadModel(String model) {
        String location = "assets/minecraft/strife/models/" + model + ".cfg";
        try {
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = (JsonObject) jsonParser.parse(new InputStreamReader(ModelUtil.class.getClassLoader().getResourceAsStream(location)));
            PlayerItemModel playerItemModel = PlayerItemParser.parseItemModel(jsonObject);
            return playerItemModel;
        } catch (Exception exception) {
            Config.warn("Error loading item model " + model + ": " + exception.getClass().getName() + ": " + exception.getMessage());
            return null;
        }
    }

}
