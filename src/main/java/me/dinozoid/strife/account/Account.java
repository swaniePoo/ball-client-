package me.dinozoid.strife.account;

import com.thealtening.auth.service.AlteningServiceType;
import me.dinozoid.strife.Client;
import me.dinozoid.strife.util.MinecraftUtil;
import me.dinozoid.strife.util.player.AltService;
import me.dinozoid.strife.util.render.DynamicTextureUtil;
import me.dinozoid.strife.util.ui.FadeDynamicTextureUtil;
import net.minecraft.client.renderer.texture.DynamicTexture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class Account extends MinecraftUtil {

    private AccountInfo info;
    private String email, password;
    private DynamicTexture avatar, body;
    private BufferedImage image, bodyImage;

    private FadeDynamicTextureUtil avatarAnimate, bodyAnimate;

    private float y, width;

    public Account(AlteningServiceType type, String combo) {
        this(type, combo.split(":")[0], combo.split(":")[1]);
    }

    public Account(AlteningServiceType type, String email, String password) {
        this.email = email;
        this.password = password;
        info = new AccountInfo(type);
        try {
            avatar = new DynamicTexture(ImageIO.read(getClass().getClassLoader().getResourceAsStream("assets/minecraft/strife/gui/accountmanager/steve.png")));
            body = new DynamicTexture(ImageIO.read(getClass().getClassLoader().getResourceAsStream("assets/minecraft/strife/gui/accountmanager/steve-body.png")));
            avatarAnimate = new FadeDynamicTextureUtil(avatar, avatar);
            bodyAnimate = new FadeDynamicTextureUtil(body, body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CompletableFuture<Boolean> login(boolean direct) {
        CompletableFuture<Boolean> successful = AltService.login(email, password);
        successful.whenCompleteAsync((success, throwable) -> {
            if (success) {
                info.parse().whenCompleteAsync((parseSuccess, throwable1) -> {
                    if (!direct) {
                        loadAvatar();
                        loadBody();
                    }
                    Client.INSTANCE.getAccountRepository().currentAccount(this);
                });
            }
        });
        return successful;
    }

    private void loadAvatar() {
        if (info.uuid() != null) {
            try {
                CompletableFuture<BufferedImage> toComplete = new CompletableFuture<>();
                Client.INSTANCE.getExecutorService().submit(() -> {
                    try {
                        toComplete.complete(ImageIO.read(new URL("https://crafatar.com/avatars/" + info.uuid() + "?size=64&overlay")));
                    } catch (IOException e) {
                        toComplete.complete(null);
                    }
                });
                toComplete.whenCompleteAsync((image, throwable) -> {
                    if (image != null) {
                        this.image = image;
                        avatar = DynamicTextureUtil.addTexture(info.username(), image);
                        avatarAnimate.imageOne(avatar);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadBody() {
        if (info.uuid() != null) {
            try {
                CompletableFuture<BufferedImage> toComplete = new CompletableFuture<>();
                Client.INSTANCE.getExecutorService().submit(() -> {
                    try {
                        toComplete.complete(ImageIO.read(new URL("https://crafatar.com/renders/body/" + info.uuid() + "?size=64&overlay")));
                    } catch (IOException e) {
                        toComplete.complete(null);
                    }
                });
                toComplete.whenCompleteAsync((image, throwable) -> {
                    if (image != null) {
                        this.bodyImage = image;
                        body = DynamicTextureUtil.addTexture(info.username() + "-body", bodyImage);
                        bodyAnimate.imageTwo(body);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public FadeDynamicTextureUtil avatarAnimate() {
        return avatarAnimate;
    }

    public void avatarAnimate(FadeDynamicTextureUtil avatarAnimate) {
        this.avatarAnimate = avatarAnimate;
    }

    public FadeDynamicTextureUtil bodyAnimate() {
        return bodyAnimate;
    }

    public void bodyAnimate(FadeDynamicTextureUtil bodyAnimate) {
        this.bodyAnimate = bodyAnimate;
    }

    public float width() {
        return width;
    }

    public void width(float width) {
        this.width = width;
    }

    public float y() {
        return y;
    }

    public void y(float y) {
        this.y = y;
    }

    public String email() {
        return this.email;
    }

    public void email(String email) {
        this.email = email;
    }

    public String password() {
        return this.password;
    }

    public void password(String password) {
        this.password = password;
    }

    public AccountInfo info() {
        return this.info;
    }

    public void info(AccountInfo info) {
        this.info = info;
    }

    public DynamicTexture avatar() {
        if (image != null) {
            avatar = DynamicTextureUtil.addTexture(info.username(), image);
            avatarAnimate.imageOne(avatar);
        }
        return avatar;
    }

    public DynamicTexture body() {
        if (bodyImage != null) {
            body = DynamicTextureUtil.addTexture(info.username() + "-body", bodyImage);
            bodyAnimate.imageTwo(body);
        }
        return body;
    }

    public void avatar(DynamicTexture avatar) {
        this.avatar = avatar;
    }
}
