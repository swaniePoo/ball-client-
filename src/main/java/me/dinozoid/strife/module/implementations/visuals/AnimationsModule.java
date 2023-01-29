package me.dinozoid.strife.module.implementations.visuals;

import me.dinozoid.strife.Client;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.property.Property;
import me.dinozoid.strife.property.implementations.DoubleProperty;
import me.dinozoid.strife.property.implementations.EnumProperty;

@ModuleInfo(name = "Animations", renderName = "Animations", description = "Sword/hit animations.", category = Category.VISUALS)
public class AnimationsModule extends Module {

    private final EnumProperty<AnimationsMode> animationsModeProperty = new EnumProperty<>("Mode", AnimationsMode.EXHIBITION);
    private final EnumProperty<HitMode> hitModeProperty = new EnumProperty<>("Hit Mode", HitMode.NORMAL);
    private final DoubleProperty scaleProperty = new DoubleProperty("Scale", 1.0, 0.5, 2, 0.05);
    private final Property<Boolean> hitSlowdownProperty = new Property<>("Hit Slowdown", false);
    private final DoubleProperty hitSlowdownAmount = new DoubleProperty("Slowdown Amount", 9, 6, 90, 1, hitSlowdownProperty::getValue);
    private final DoubleProperty itemXProperty = new DoubleProperty("Item X", 0, -1, 1, 0.0001);
    private final DoubleProperty itemYProperty = new DoubleProperty("Item Y", 0, -1, 1, 0.0001);
    private final DoubleProperty itemZProperty = new DoubleProperty("Item Z", 0, -1, 1, 0.0001);
    private final Property<Boolean> alwaysScale = new Property<>("Always scale", false, () -> scaleProperty.getValue() != 1.0);


    public static AnimationsModule instance() {
        return Client.INSTANCE.getModuleRepository().moduleBy(AnimationsModule.class);
    }

    @Override
    public void init() {
        super.init();
        addValueChangeListener(animationsModeProperty);
    }

    public EnumProperty<AnimationsMode> animationsModeProperty() {
        return animationsModeProperty;
    }

    public EnumProperty<HitMode> hitModeProperty() {
        return hitModeProperty;
    }

    public DoubleProperty scaleProperty() {
        return scaleProperty;
    }

    public Property<Boolean> hitSlowdownProperty() {
        return hitSlowdownProperty;
    }

    public DoubleProperty hitSlowdownAmount() {
        return hitSlowdownAmount;
    }

    public DoubleProperty itemXProperty() {
        return itemXProperty;
    }

    public DoubleProperty itemYProperty() {
        return itemYProperty;
    }

    public DoubleProperty itemZProperty() {
        return itemZProperty;
    }

    public Property<Boolean> getAlwaysScale() {
        return alwaysScale;
    }

    public enum AnimationsMode {
        VANILLA, OLD, SWANK, EXHIBITION, WAVE, STELLA, KEKCLIENT, ETHEREAL, DORTWARE, LUNAR, AVATAR, BOOP
    }

    public enum HitMode {
        NORMAL, SMOOTH
    }
}
