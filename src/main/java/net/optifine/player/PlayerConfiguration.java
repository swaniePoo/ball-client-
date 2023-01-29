package net.optifine.player;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.event.implementations.render.RenderModelEvent;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.src.Config;

public class PlayerConfiguration
{
    private PlayerItemModel[] playerItemModels = new PlayerItemModel[0];
    private boolean initialized = false;

    public void renderPlayerItems(ModelBiped modelBiped, AbstractClientPlayer player, float scale, float partialTicks)
    {
        PlayerItemModel playerItemModel = null;
        for (PlayerItemModel itemModel : this.playerItemModels) {
            playerItemModel = itemModel;
        }
        final RenderModelEvent renderModelEvent = new RenderModelEvent(modelBiped, player, scale, playerItemModel);
        Client.INSTANCE.getEventBus().post(renderModelEvent);
        if (renderModelEvent.getPlayerItemModel() != null) {
            renderModelEvent.getPlayerItemModel().render(renderModelEvent.getModel(), renderModelEvent.getPlayer(), renderModelEvent.getScale(), partialTicks);
        }
        if (this.initialized) {
            for (PlayerItemModel playeritemmodel : this.playerItemModels) {
                playeritemmodel.render(renderModelEvent.getModel(), renderModelEvent.getPlayer(), renderModelEvent.getScale(), partialTicks);
            }
        }
    }

    public boolean isInitialized()
    {
        return this.initialized;
    }

    public void setInitialized(boolean initialized)
    {
        this.initialized = initialized;
    }

    public PlayerItemModel[] getPlayerItemModels()
    {
        return this.playerItemModels;
    }

    public void addPlayerItemModel(PlayerItemModel playerItemModel)
    {
        this.playerItemModels = (PlayerItemModel[]) Config.addObjectToArray(this.playerItemModels, playerItemModel);
    }
}
