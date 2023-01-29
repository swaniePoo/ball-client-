package me.dinozoid.strife.module.implementations.visuals;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.render.RenderModelEvent;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.property.implementations.EnumProperty;
import me.dinozoid.strife.util.player.ModelUtil;
import net.minecraft.util.ResourceLocation;
import net.optifine.player.PlayerItemModel;

import java.awt.image.BufferedImage;

@ModuleInfo(name = "Hat", renderName = "Hat", category = Category.VISUALS)
public class HatModule extends Module {

    private final EnumProperty<HatMode> hatModeProperty = new EnumProperty<>("Mode", HatMode.WITCH_HAT);

    private PlayerItemModel model;

    @EventHandler
    private final Listener<RenderModelEvent> renderModelListener = new Listener<>(event -> {
        if (model != null && event.getPlayer() == mc.thePlayer) {
            event.setPlayerItemModel(model);
        }
    });

    public static HatModule instance() {
        return Client.INSTANCE.getModuleRepository().moduleBy(HatModule.class);
    }

    @Override
    public void init() {
        super.init();
        hatModeProperty.addValueChange((oldValue, value) -> {
            PlayerItemModel playerItemModel = ModelUtil.downloadModel(value.name().toLowerCase());
            if (playerItemModel != null) {
                if (!playerItemModel.isUsePlayerTexture()) {
                    BufferedImage bufferedimage = ModelUtil.downloadTextureImage(value.name().toLowerCase());
                    if (bufferedimage != null) {
                        playerItemModel.setTextureImage(bufferedimage);
                        ResourceLocation resourcelocation = new ResourceLocation("optifine.net", value.name().toLowerCase());
                        playerItemModel.setTextureLocation(resourcelocation);
                    }
                }
                this.model = playerItemModel;
            }
        });
        hatModeProperty.setValue(HatMode.SANTA_HAT);
    }

    public enum HatMode {
        SANTA_HAT, WITCH_HAT
    }

}
