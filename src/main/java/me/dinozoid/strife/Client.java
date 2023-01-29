package me.dinozoid.strife;

import lombok.Getter;
import lombok.Setter;
import me.dinozoid.strife.account.AccountRepository;
import me.dinozoid.strife.alpine.bus.EventBus;
import me.dinozoid.strife.alpine.bus.EventManager;
import me.dinozoid.strife.alpine.listener.Listenable;
import me.dinozoid.strife.command.CommandRepository;
import me.dinozoid.strife.config.ConfigRepository;
import me.dinozoid.strife.config.settings.SettingsLoader;
import me.dinozoid.strife.font.FontRepository;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleRepository;
import me.dinozoid.strife.shader.implementations.BloomShader;
import me.dinozoid.strife.target.TargetRepository;
import me.dinozoid.strife.ui.notification.NotificationRepository;
import me.dinozoid.strife.util.Dragging;
import me.dinozoid.strife.util.SpotifyAPI;
import me.dinozoid.strife.util.network.ServerUtil;
import me.dinozoid.strife.util.player.PlayerUtil;
import me.dinozoid.strife.websocket.SocketClient;
import me.dinozoid.strife.websocket.user.User;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.OldServerPinger;
import org.lwjgl.opengl.Display;
import viamcp.ViaMCP;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Getter
public enum Client implements Listenable {
    INSTANCE;


    //private SpotifyAPI spotifyAPI = new SpotifyAPI();;
    public static final String NAME = "Strife";
    public static String CUSTOMNAME = NAME;
    public static int BUILD = 211030;
    public static final String RELEASE_BRANCH = "Development";
    public static final String RELEASE_DATE = "211030";
    public static final String CHAT_PREFIX = ".";
    public static final String COMMAND_PREFIX = "\u00A7cStrife \u00A77»\u00A7r ";
    public static final Path DIRECTORY = Paths.get(Minecraft.getMinecraft().mcDataDir.getAbsolutePath(), "Strife");

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10);
    private final EventBus eventBus = new EventManager();
    private final SettingsLoader settingsLoader = new SettingsLoader();
    private final ConfigRepository configRepository = new ConfigRepository();
    private final ModuleRepository moduleRepository = new ModuleRepository();
    private final NotificationRepository notificationRepository = new NotificationRepository();
    private final AccountRepository accountRepository = new AccountRepository();
    private final CommandRepository commandRepository = new CommandRepository();
    private final TargetRepository targetRepository = new TargetRepository();
    private final FontRepository fontRepository = new FontRepository();
    private OldServerPinger oldServerPinger;
    private BloomShader bloomShader = new BloomShader();
    private SocketClient socketClient;
    @Setter
    private List<User> onlineUsers;
    @Setter
    private User strifeUser = new User("Jinthium", "0000", "Developer");

    private final Runnable pingRunnable = () -> {
        try {
            if (ServerUtil.onServer() && ServerUtil.getServerData().pingToServer != -1) {
                oldServerPinger.ping(ServerUtil.getServerData());
                if (Minecraft.getMinecraft() != null) PlayerUtil.sendMessage("Pinging...");
            }
        } catch (Throwable ignored) {
        }
    };

    /**
     * Called when the client starts up.
     **/
    public void startup(boolean reload) {
        Display.setTitle(NAME + " " + BUILD + " - " + RELEASE_BRANCH);
        if(!reload) {
            try {
                ViaMCP.getInstance().start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        onlineUsers = new ArrayList<>();
        oldServerPinger = new OldServerPinger();
        settingsLoader.load();
        configRepository.init();
        fontRepository.init();
        moduleRepository.init();
        commandRepository.init();
        accountRepository.init();
        notificationRepository.init();
        if(!reload) connectSocket(true);
        //if(!reload)
          //  moduleRepository.moduleBy(DiscordRPCModule.class).startTimestamp(System.currentTimeMillis());
        // scheduledExecutorService.scheduleAtFixedRate(pingRunnable, 0, 5, TimeUnit.SECONDS);
    }

    public Dragging createDrag(Module module, String name, float x, float y) {
        DragManager.draggables.put(name, new Dragging(module, name, x, y));
        return DragManager.draggables.get(name);
    }

    public void connectSocket(boolean blocking) {
//        try {
//            socketClient = new SocketClient(new URI("ws://127.0.0.1:29155/?uid=0000&hwid=" + LicenceUtil.licence()));
//            if(blocking) socketClient.connectBlocking();
//            else socketClient.connect();
//            socketClient.packetHandler().sendPacket(new CUserUpdatePacket(new SUserUpdatePacket.Value(SUserUpdatePacket.UpdateType.ACCOUNT_USERNAME, Minecraft.getMinecraft().session.getUsername())));
//            socketClient.packetHandler().sendPacket(new CServerCommandPacket(CServerCommandPacket.CommandOperation.LIST_USERS, "", "capes"));
//        } catch (URISyntaxException | InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * Called when the client shuts down.
     **/
    public void shutdown(boolean shutdown) {
        settingsLoader.save();
       // moduleRepository.moduleBy(DiscordRPCModule.class).startTimestamp(System.currentTimeMillis());
//        if(shutdown) socketClient.close(2000);
    }
}
