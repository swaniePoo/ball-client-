package me.dinozoid.strife.module;

import com.sun.org.apache.xerces.internal.impl.PropertyManager;
import me.dinozoid.strife.Client;
import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listenable;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.system.KeyEvent;
import me.dinozoid.strife.module.implementations.combat.*;
import me.dinozoid.strife.module.implementations.exploit.*;
import me.dinozoid.strife.module.implementations.misc.*;
import me.dinozoid.strife.module.implementations.movement.*;
import me.dinozoid.strife.module.implementations.player.*;
import me.dinozoid.strife.module.implementations.visuals.*;
import me.dinozoid.strife.ui.clickgui.StrifeClickGUI;
import me.dinozoid.strife.ui.skeetui.StrifeSkeetUI;
import me.dinozoid.strife.util.MinecraftUtil;
import org.lwjgl.input.Keyboard;

import java.util.*;
import java.util.stream.Collectors;

public class ModuleRepository extends MinecraftUtil implements Listenable {

    private final List<Module> MODULES = new ArrayList<>();

    private ClickGUIModule clickGUIModule;
    private StrifeClickGUI clickGUI;
    private StrifeSkeetUI skeetUI;

    @EventHandler
    private final Listener<KeyEvent> keyEvent = new Listener<>(event -> {
        if (event.getKey() == Keyboard.KEY_RSHIFT) {
            if (clickGUIModule.modeProperty().getValue() == ClickGUIModule.ClickGUIMode.DROPDOWN)
                mc.displayGuiScreen(clickGUI);
            else mc.displayGuiScreen(skeetUI);
        }
        MODULES.forEach(module -> {
            if (module.key() == event.getKey()) module.pressed();
        });
    });

    public void init() {
        Client.INSTANCE.getEventBus().unsubscribe(this);
        Client.INSTANCE.getEventBus().subscribe(this);
        MODULES.clear();
        add(
                // COMBAT
                new WTapModule(),
                new PingSpoofModule(),
                new AutoClickerModule(),
                new AutoHeadModule(),
                new ReachModule(),
                new CriticalsModule(),
                new KillAuraModule(),
                new AutoPotionModule(),
                new HitBoxModule(),
                // MOVEMENT
                new SprintModule(),
                new FlightModule(),
                new AntiVoidModule(),
                new LongJumpModule(),
                new SpeedModule(),
                new AutoJumpModule(),
                new NoFallModule(),
                new SafeWalkModule(),
                new StepModule(),
                new FunnyPacketModule(),
                // PLAYER
                new NoSlowdownModule(),
                new InventoryMoveModule(),
                new InventoryManagerModule(),
                //  new InventoryManager2Module(),
                new ChestStealerModule(),
                new ScaffoldModule(),
                new VelocityModule(),
                new TimerModule(),
//                new HackerDetectorModule(),
                // EXPLOIT
                new PhaseModule(),
                new BlinkModule(),
                new DisablerModule(),
                new NoRotateModule(),
                new EntityDesyncModule(),
                new StaffAnalyzerModule(),
                new ChatBypassModule(),
//                new ParkourGameModule(),
//                new TestModule(),
                // VISUALS
                new ESPModule(),
                new CapeModule(),
                new OverlayModule(),
                new FullbrightModule(),
                new CameraClipModule(),
                new SpeedMineModule(),
                new FastPlaceModule(),
                new BlockOverlayModule(),
                new MotionBlurModule(),
                new AmbienceModule(),
                new AnimationsModule(),
                new HatModule(),
                new ChamsModule(),
                new OutlineESPModule(),
                new GlowESPModule(),
                clickGUIModule = new ClickGUIModule(),
                new GlintColorizeModule(),
                new CustomCrosshairModule(),
                new SessionInfoModule(),
                new PlayerTrailsModule(),
                new ScoreboardModule(),
                new DamageParticlesModule(),
                new BreadcrumbsModule(),
                new JumpCirclesModule(),
                new NoHurtCamModule(),
                new LowFireModule(),
                new NoBobModule(),
                new NoFovModule(),
                new BlurModule(),
                // MISC
                new StreamerModeModule(),
                new AutoHypixelModule(),
                new IRCModule(),
                new ResetVLModule(),
                new MCFModule(),
                new TargetStrafeModule(),
                new SpotifyModule()
        );
        clickGUI = new StrifeClickGUI();
        skeetUI = new StrifeSkeetUI();
    }

    private void add(Module... modules) {
        Arrays.stream(modules).forEach(module -> {
            module.init();
            MODULES.add(module);
        });
    }

    public Module moduleBy(String name) {
        for (Module module : MODULES) {
            if (module.name().equalsIgnoreCase(name) || Arrays.stream(module.aliases()).anyMatch(name::equalsIgnoreCase))
                return module;
        }
        return null;
    }

    public List<Module> modulesIn(Category category) {
        return MODULES.stream().filter(module -> module.category().equals(category)).collect(Collectors.toList());
    }

    public List<Module> getToggledModules(List<Module> modules) {
        return modules.stream().filter(Module::toggled).collect(Collectors.toList());
    }

    public <T extends Module> T moduleBy(Class<T> tClass) {
        return (T) MODULES.stream().filter(mod -> mod.getClass().equals(tClass)).findFirst().orElse(null);
    }

    public List<Module> modules() {
        return MODULES;
    }
}