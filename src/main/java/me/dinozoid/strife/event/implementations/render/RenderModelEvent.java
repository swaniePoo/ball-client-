package me.dinozoid.strife.event.implementations.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.dinozoid.strife.event.Event;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.optifine.player.PlayerItemModel;

@Getter
@Setter
@AllArgsConstructor
public class RenderModelEvent extends Event {

    private ModelBiped model;
    private AbstractClientPlayer player;
    private float scale;
    private PlayerItemModel playerItemModel;
}
