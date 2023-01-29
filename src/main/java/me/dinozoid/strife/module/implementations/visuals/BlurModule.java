package me.dinozoid.strife.module.implementations.visuals;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.DoAFuckingBloomEvent;
import me.dinozoid.strife.event.implementations.render.Render2DEvent;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.newshader.blur.BloomUtil;
import me.dinozoid.strife.property.Property;
import me.dinozoid.strife.property.implementations.DoubleProperty;
import me.dinozoid.strife.property.implementations.MultiSelectEnumProperty;
import me.dinozoid.strife.shader.implementations.BlurShader;
import me.dinozoid.strife.ui.callback.ShaderCallback;
import me.dinozoid.strife.util.render.RenderUtil;
import me.dinozoid.strife.util.render.StencilUtil;
import net.minecraft.client.shader.Framebuffer;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;

@ModuleInfo(name = "Blur", renderName = "Blur", category = Category.MOVEMENT)
public class BlurModule extends Module {

    private final DoubleProperty blurRadiusProperty = new DoubleProperty("Radius", 25, 1, 100, 1);
    private final MultiSelectEnumProperty<BlurElement> elementsProperty = new MultiSelectEnumProperty<>("Elements", BlurElement.CHAT);
    private final Property<Boolean> shadow = new Property<Boolean>("Shadow", true);
    private final DoubleProperty shadowRadius = new DoubleProperty("Shadow Radius", 6, 1, 20,1, shadow::getValue);
    private final DoubleProperty shadowOffset = new DoubleProperty("Shadow Offset", 2, 1, 15,1, shadow::getValue);

    private final List<ShaderCallback> renderCallbacks = new ArrayList<>();
    private Framebuffer bloomFramebuffer = new Framebuffer(1, 1, false);

    public void blurShit(){
        if(shadow.getValue()){
            bloomFramebuffer = RenderUtil.createFramebuffer(bloomFramebuffer, true);

            bloomFramebuffer.framebufferClear();
            bloomFramebuffer.bindFramebuffer(true);
            Client.INSTANCE.getEventBus().post(new DoAFuckingBloomEvent());
            bloomFramebuffer.unbindFramebuffer();
            BloomUtil.renderBlur(bloomFramebuffer.framebufferTexture, shadowRadius.getValue().intValue(), shadowOffset.getValue().intValue());
        }
    }

    @EventHandler
    private final Listener<Render2DEvent> render2DListener = new Listener<>(event -> {
//        if (!elementsProperty.getValue().isEmpty()) {
//            glPushMatrix();
//            StencilUtil.initStencilToWrite();
//            blurShader.setRadius(blurRadiusProperty.getValue().floatValue());
//            renderCallbacks.forEach(ShaderCallback::preRender);
//            renderCallbacks.clear();
//            StencilUtil.readStencilBuffer(1);
//            blurShader.blur();
//            StencilUtil.uninitStencilBuffer();
//            glPopMatrix();
//        }

    });

    public List<ShaderCallback> renderCallbacks() {
        return renderCallbacks;
    }

    public static BlurModule instance() {
        return Client.INSTANCE.getModuleRepository().moduleBy(BlurModule.class);
    }

    public MultiSelectEnumProperty<BlurElement> elementsProperty() {
        return elementsProperty;
    }

    public enum BlurElement {
        CHAT, SCOREBOARD, TABLIST, HOTBAR, SESSION_INFO, NOTIFICATIONS
    }

}
