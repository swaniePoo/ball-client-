package me.dinozoid.strife.module.implementations.visuals;

import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.player.PlayerMotionEvent;
import me.dinozoid.strife.event.implementations.render.Render3DEvent;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.util.render.RenderUtil;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.awt.*;
import java.util.*;

import static org.lwjgl.opengl.GL11.*;

@ModuleInfo(name = "JumpCircles", renderName = "JumpCircles", category = Category.VISUALS)
public class JumpCirclesModule extends Module {

    private final Map<Vec3, Long> jumps = new HashMap<>();

    private boolean onGround = false;

    @Override
    public void onEnable() {
        super.onEnable();
        jumps.clear();
        onGround = true;
    }

    @EventHandler
    private final Listener<PlayerMotionEvent> updatePlayerListener = new Listener<>(event -> {
        if (mc.thePlayer.onGround && !onGround) {
            jumps.put(mc.thePlayer.getPositionVector(), System.currentTimeMillis());
            onGround = true;
        }
        if (mc.thePlayer.motionY >= 0.01 || mc.thePlayer.fallDistance > 1)
            onGround = false;
    });

    @EventHandler
    private final Listener<Render3DEvent> render3DListener = new Listener<>(event -> {
        int vertices = 45;
        float increment = (float) (2 * Math.PI / vertices);
        RenderUtil.pre3D();
        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);
        glDepthMask(false);
        glFrontFace(GL_CW);
        Iterator<Map.Entry<Vec3, Long>> it = jumps.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Vec3, Long> object = it.next();
            long time = (System.currentTimeMillis() - object.getValue());
            float radius = MathHelper.clamp_float(time / 400f, 0, 1);
            float x = (float) (object.getKey().xCoord - mc.getRenderManager().viewerPosX);
            float y = (float) (object.getKey().yCoord - mc.getRenderManager().viewerPosY);
            float z = (float) (object.getKey().zCoord - mc.getRenderManager().viewerPosZ);
            glBegin(GL_TRIANGLE_FAN);
            glColor4f(0, 0, 0, 0);
            glVertex3f(x, y, z);
            for(int i = 0; i <= vertices; i++) {
                RenderUtil.color(new Color(OverlayModule.getColor(i * 2)), (1 - radius) * 255);
                float sin = MathHelper.sin(increment * i) * radius;
                float cos = -MathHelper.cos(increment * i) * radius;
                glVertex3f(x + sin, y, z + cos);
            }
            glEnd();
            if(radius == 1)
                it.remove();
        }
        glFrontFace(GL_CCW);
        glDepthMask(true);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        RenderUtil.post3D();
    });

}
