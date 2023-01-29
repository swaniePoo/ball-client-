package me.dinozoid.strife.module.implementations.visuals;

import com.google.common.base.Predicates;
import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.player.RenderNametagEvent;
import me.dinozoid.strife.event.implementations.render.Render2DEvent;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.property.implementations.MultiSelectEnumProperty;
import me.dinozoid.strife.util.player.PlayerUtil;
import me.dinozoid.strife.util.render.RenderUtil;
import me.dinozoid.strife.util.world.WorldUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;

@ModuleInfo(name = "Nametags", renderName = "Nametags", category = Category.VISUALS)
public class NametagsModule extends Module {

    private final MultiSelectEnumProperty<PlayerUtil.Target> targetsProperty = new MultiSelectEnumProperty<>("Targets", PlayerUtil.Target.PLAYERS);

    @EventHandler
    private final Listener<RenderNametagEvent> renderNametagListener = new Listener<>(event -> {
        if (event.getEntity() instanceof EntityPlayer)
            event.cancel();
    });

    @EventHandler
    private final Listener<Render2DEvent> render2DListener = new Listener<>(event -> {
        final List<EntityLivingBase> livingEntities = WorldUtil.getLivingEntities(Predicates.and(entity -> PlayerUtil.isValid(entity, targetsProperty), Predicates.instanceOf(EntityPlayer.class)));
        for (EntityLivingBase entity : livingEntities) {
            double x = RenderUtil.interpolate(entity.isEntityAlive() ? entity.posX : entity.lastTickPosX, entity.lastTickPosX, event.getPartialTicks()) - mc.getRenderManager().viewerPosX;
            double y = RenderUtil.interpolate(entity.isEntityAlive() ? entity.posY : entity.lastTickPosY, entity.lastTickPosY, event.getPartialTicks()) - mc.getRenderManager().viewerPosY;
            double z = RenderUtil.interpolate(entity.isEntityAlive() ? entity.posZ : entity.lastTickPosZ, entity.lastTickPosZ, event.getPartialTicks()) - mc.getRenderManager().viewerPosZ;
        }
    });

}
