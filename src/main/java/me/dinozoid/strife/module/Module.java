package me.dinozoid.strife.module;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.alpine.listener.Listenable;
import me.dinozoid.strife.module.implementations.misc.SpotifyModule;
import me.dinozoid.strife.module.implementations.visuals.GlowESPModule;
import me.dinozoid.strife.property.Property;
import me.dinozoid.strife.property.PropertyRepository;
import me.dinozoid.strife.util.Animation;
import me.dinozoid.strife.util.DecelerateAnimation;
import me.dinozoid.strife.util.Direction;
import me.dinozoid.strife.util.MinecraftUtil;
import me.dinozoid.strife.util.system.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Supplier;

public abstract class Module extends MinecraftUtil implements Listenable {

    protected static final PropertyRepository propertyRepository = new PropertyRepository();

    private final ModuleInfo annotation = getClass().getAnnotation(ModuleInfo.class);
    private final String name = annotation.name();
    private final Category category = annotation.category();
    private final String renderName = annotation.renderName();
    private final String description = annotation.description();
    private final String[] aliases = annotation.aliases();
    private int keybind = annotation.keybind();

    private boolean toggled, hidden, hasSuffix;
    private String suffix;
    public final Animation animation = new DecelerateAnimation(250, 1);

    public static PropertyRepository propertyRepository() {
        return propertyRepository;
    }

    public void init() {
        propertyRepository.register(this);
    }

    public void addValueChangeListener(Property<?> mode, Supplier<Boolean> pascal) {
        setSuffix(pascal.get() ? StringUtil.upperSnakeCaseToPascal(mode.getValue().toString()) : mode.getValue().toString());
        mode.addValueChange((oldValue, value) -> setSuffix(pascal.get() ? StringUtil.upperSnakeCaseToPascal(mode.getValue().toString()) : mode.getValue().toString()));
    }

    public void addValueChangeListener(Property<?> mode) {
        addValueChangeListener(mode, () -> true);
    }

    public void setSuffix(String suffix) {
        hasSuffix = true;
        String[] split = suffix.split("_");
        if (split.length > 1) {
            StringBuilder stringBuilder = new StringBuilder();
            int index = 0;
            for (String text : split) {
                if (index > 0) stringBuilder.append(" ");
                stringBuilder.append(StringUtils.capitalize(text));
                index++;
            }
            this.suffix = "\2478\2477" + stringBuilder + "\2478";
        } else this.suffix = "\2478\2477" + suffix + "\2478";
    }

    public int key() {
        return keybind;
    }

    public void key(int key) {
        this.keybind = key;
    }

    public void pressed() {
        toggle();
    }

    public void toggle() {
        toggled(!toggled);
    }

    public void toggled(boolean toggled) {
        if (toggled) {
            if (!this.toggled) onEnable();
        } else {
            if (this.toggled) onDisable();
        }
        this.toggled = toggled;
    }

    public void onEnable() {
        Client.INSTANCE.getEventBus().subscribe(this);
        if(!getClass().equals(SpotifyModule.class)) animation.setDirection(Direction.FORWARDS);
    }

    public void onDisable() {
        if(this instanceof GlowESPModule){
            GlowESPModule.fadeIn.setDirection(Direction.BACKWARDS);
        }
        Client.INSTANCE.getEventBus().unsubscribe(this);
        if(!getClass().equals(SpotifyModule.class)) animation.setDirection(Direction.BACKWARDS);
    }

    public void hidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String name() {
        return name;
    }

    public Category category() {
        return category;
    }

    public String renderName() {
        return hasSuffix ? renderName + "\2477 " + suffix : renderName;
    }

    public String description() {
        return description;
    }

    public String[] aliases() {
        return aliases;
    }

    public boolean toggled() {
        return toggled;
    }

    public boolean hasSuffix() {
        return hasSuffix;
    }

    public boolean hidden() {
        return hidden;
    }
}
