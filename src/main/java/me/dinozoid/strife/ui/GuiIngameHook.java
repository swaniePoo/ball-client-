package me.dinozoid.strife.ui;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.event.implementations.render.Render2DEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;

public class GuiIngameHook extends GuiIngame {

    public GuiIngameHook(Minecraft mcIn) {
        super(mcIn);
    }

    @Override
    public void renderGameOverlay(float partialTicks) {
        super.renderGameOverlay(partialTicks);
//        Client.INSTANCE.eventBus().post(new Render2DEvent(partialTicks));
    }
}
