package me.dinozoid.strife.shader.implementations;

import lombok.Getter;
import lombok.Setter;
import me.dinozoid.strife.shader.ShaderProgram;
import me.dinozoid.strife.util.MinecraftUtil;
import net.minecraft.client.gui.ScaledResolution;

import static org.lwjgl.opengl.GL11.*;

public class MenuShader extends MinecraftUtil {

    private ShaderProgram menuShader = new ShaderProgram("fragment/menu.frag");

    @Getter
    @Setter
    private int pass;

    public MenuShader(int pass) {
        this.pass = pass;
    }

    public void render(final ScaledResolution scaledResolution) {
        menuShader.init();
        setupUniforms();
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        menuShader.renderCanvas(scaledResolution);
        menuShader.uninit();
        pass++;
    }

    public void setupUniforms() {
        menuShader.setUniformf("time", pass / 100f);
        menuShader.setUniformf("resolution", mc.displayWidth, mc.displayHeight);
    }

}
