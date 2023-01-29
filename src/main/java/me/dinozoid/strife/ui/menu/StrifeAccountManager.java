package me.dinozoid.strife.ui.menu;

import com.thealtening.auth.service.AlteningServiceType;
import me.dinozoid.strife.Client;
import me.dinozoid.strife.account.Account;
import me.dinozoid.strife.account.AccountInfo;
import me.dinozoid.strife.font.CustomFont;
import me.dinozoid.strife.font.CustomFontRenderer;
import me.dinozoid.strife.shader.implementations.MenuShader;
import me.dinozoid.strife.ui.element.Position;
import me.dinozoid.strife.ui.element.StrifeButton;
import me.dinozoid.strife.ui.element.StrifeTextField;
import me.dinozoid.strife.util.player.AltService;
import me.dinozoid.strife.util.render.RenderUtil;
import me.dinozoid.strife.util.system.StringUtil;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StrifeAccountManager extends GuiScreen {

    private final CustomFontRenderer font30;
    private final CustomFontRenderer font24;
    private final CustomFontRenderer font20;
    private final CustomFontRenderer font19;
    private final CustomFontRenderer font15;
    private final ResourceLocation hypixel;
    private final ResourceLocation mineplex;
    private final ResourceLocation delete;
    private final ResourceLocation login;
    private final GuiScreen parent;
    private final MenuShader shader;
    private float hypixelOpacity, mineplexOpacity;
    private Position menuPos, altsPos, deleteAccountPos, loginAccountPos;
    private float hypixelY, mineplexY;
    private StrifeTextField emailField, passwordField;
    private boolean addingAlt;
    private Account selectedAccount;
    private StrifeButton addButton;

    private StrifeButton clipboardButton, directLoginButton, addAccountButton, importListButton, accountLoginButton, accountRemoveButton;

    private float scroll;
    private float yOffset;

    public StrifeAccountManager(GuiScreen parent, int pass) {
        shader = new MenuShader(pass);
        this.parent = parent;
        hypixel = new ResourceLocation("strife/gui/accountmanager/hypixel.png");
        mineplex = new ResourceLocation("strife/gui/accountmanager/mineplex.png");
        delete = new ResourceLocation("strife/gui/accountmanager/delete.png");
        login = new ResourceLocation("strife/gui/accountmanager/login.png");
        CustomFont font = Client.INSTANCE.getFontRepository().defaultFont();
        font15 = font.size(15);
        font19 = font.size(19);
        font20 = font.size(20);
        font24 = font.size(24);
        font30 = font.size(30);
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        ScaledResolution sc = new ScaledResolution(mc);
        menuPos = new Position(60, 60, sc.getScaledWidth() - 60, sc.getScaledHeight() - 60);
        altsPos = new Position(menuPos.x() + 3, menuPos.y() + 25, menuPos.width() / 2 - 2, menuPos.height() - 28);
        deleteAccountPos = new Position(0, 0, 0, 0);
        loginAccountPos = new Position(0, 0, 0, 0);
        addButton = new StrifeButton(altsPos.x(), altsPos.height() + 2, 100f, menuPos.height() - altsPos.height() - 4, "Add/Import", 19, 0xff4f5052);
        emailField = new StrifeTextField(19, menuPos.x() + menuPos.width() / 2 - 120 / 2f, menuPos.y() + menuPos.height() / 2 - 100, 120, 15, 0xff242424);
        passwordField = new StrifeTextField(19, menuPos.x() + menuPos.width() / 2 - 120 / 2f, menuPos.y() + menuPos.height() / 2 - 100 + 25, 120, 15, 0xff242424);

        clipboardButton = new StrifeButton(menuPos.x() + menuPos.width() / 2 - 120 / 2f, emailField.y() + 50, 120, 12, "Clipboard", 19, 0xff4f5052);
        directLoginButton = new StrifeButton(menuPos.x() + menuPos.width() / 2 - 120 / 2f, emailField.y() + 64, 120, 12, "Direct Login", 19, 0xff4f5052);
        addAccountButton = new StrifeButton(menuPos.x() + menuPos.width() / 2 - 120 / 2f, emailField.y() + 76, 120, 12, "Add Account", 19, 0xff4f5052);
        importListButton = new StrifeButton(menuPos.x() + menuPos.width() / 2 - 120 / 2f, emailField.y() + 88, 120, 12, "Import List", 19, 0xff4f5052);
        accountLoginButton = new StrifeButton(207f / 2 + menuPos.width() - altsPos.width() / 2 - 100, menuPos.y() + 28, 200, 20, "Login", 19, 0xff4f5052);
        accountRemoveButton = new StrifeButton(207f / 2 + menuPos.width() - altsPos.width() / 2 - 100, menuPos.y() + 30 + 20, 200, 20, "Remove", 19, 0xff4f5052);
    }

    public String getCurrentTime(long millis) {
        Date date = new Date(millis);
        DateFormat dateFormat = new SimpleDateFormat("dd'd' HH'h' mm'm' s's'");
        return dateFormat.format(date);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Gui.drawRect(0, 0, 0, 0, 0);
        final ScaledResolution scaledResolution = new ScaledResolution(mc);
        shader.render(scaledResolution);
        if (!addingAlt) {
            menuPos.width(RenderUtil.animate(scaledResolution.getScaledWidth() - 60, menuPos.width(), 0.3f) - 0.1F);
            menuPos.x(RenderUtil.animate(menuPos.originalX(), menuPos.x(), 0.3f) - 0.1F);
            altsPos.x(menuPos.x() + 3);
            altsPos.y(menuPos.y() + 25);
            altsPos.width(menuPos.width() / 2 - 2);
            altsPos.height(menuPos.height() - 28);
            emailField.setPosition(menuPos.x() + menuPos.width() / 2 - 200f / 2, menuPos.y() + menuPos.height() / 2 - 100, 200, 15);
            RenderUtil.makeCropBox(menuPos.x(), menuPos.y(), menuPos.x() + menuPos.width(), menuPos.y() + menuPos.height());
            RenderUtil.drawRoundedRect(menuPos.x(), menuPos.y(), menuPos.width(), menuPos.height(), 10, 15, 0xff2b2b2b);
            addButton.drawScreen(mouseX, mouseY);
            RenderUtil.destroyCropBox();

            int offset = 3;
            int accountHeight = 40;

            yOffset = -43;
            RenderUtil.makeCropBox(altsPos.x(), altsPos.y(), altsPos.width(), altsPos.height());
            for (int i = 0; i < Client.INSTANCE.getAccountRepository().accounts().size(); i++) {
                Account account = Client.INSTANCE.getAccountRepository().accounts().get(i);
                account.y(RenderUtil.animate(((accountHeight + offset) * i) + offset, account.y(), 0.15f) - 0.1F);
                account.width(-RenderUtil.animate(altsPos.width() - offset, account.width(), 0.1f) - 0.1F);
                deleteAccountPos.width(RenderUtil.animate(40, deleteAccountPos.width(), 0.2F) - 0.1F);
                loginAccountPos.width(RenderUtil.animate(80, loginAccountPos.width(), 0.2F) - 0.1F);
                AccountInfo info = account.info();
                float altsY = altsPos.y() + account.y();
                String name = info.username() == null ? "<Unknown>" : info.hypixelRank().representation() + info.username();
                if (RenderUtil.inBounds(altsPos.x() + account.width() + offset, altsY, altsPos.width() - offset, altsY + accountHeight, mouseX, mouseY) || selectedAccount == account) {
                    RenderUtil.drawRoundedRect(altsPos.x() + account.width() + offset, altsY, altsPos.width() - offset, altsY + accountHeight, 10, 15, new Color(0xff242424).brighter().getRGB());
                } else {
                    deleteAccountPos.width(RenderUtil.animate(-40, deleteAccountPos.width(), 0.2F));
                    loginAccountPos.width(RenderUtil.animate(-80, loginAccountPos.width(), 0.2F));
                    RenderUtil.drawRoundedRect(altsPos.x() + account.width(), altsY, altsPos.width() - offset, altsY + accountHeight, 10, 15, 0xff242424);
                }
                RenderUtil.drawRoundedRect(altsPos.width() - offset - loginAccountPos.width(), altsY, altsPos.width() - offset - deleteAccountPos.width(), altsY + accountHeight, 10, 15, new Color(50, 209, 66).getRGB());
                RenderUtil.drawImage(login, altsPos.width() - loginAccountPos.width() + 16 / 2f, altsY + accountHeight / 2f - 16 / 2f, 16, 16);
                RenderUtil.drawRoundedRect(altsPos.width() - offset - deleteAccountPos.width(), altsY, altsPos.width() - offset, altsY + accountHeight, 10, 15, new Color(209, 50, 50).getRGB());
                RenderUtil.drawImage(delete, altsPos.width() - deleteAccountPos.width() + 16 / 2f, altsY + accountHeight / 2f - 16 / 2f, 16, 16);
                RenderUtil.drawDynamicTexture(account.avatar(), altsPos.x() + offset * 2, altsY + offset + 1, 32, 32);
                font19.drawStringWithShadow(name, altsPos.x() + 32 + offset * 2 + 2, altsY + offset + 1, -1);
                font15.drawStringWithShadow("Email: " + account.email(), altsPos.x() + 32 + offset * 2 + 2, altsY + offset + 1 + font19.getHeight(name) + 2, 0xff787878);
                font15.drawStringWithShadow("Capes: " + info.capes(), altsPos.x() + 32 + offset * 2 + 1.8f, altsY + offset + 1 + font19.getHeight(name) + font15.getHeight(info.capes().toString()) + 3, 0xff787878);
                yOffset += account.y();
            }
            RenderUtil.destroyCropBox();

            RenderUtil.makeCropBox(menuPos.x(), menuPos.y(), menuPos.width(), menuPos.height());
            if (selectedAccount != null) {
                AccountInfo info = selectedAccount.info();

                hypixelOpacity = RenderUtil.animate(255, hypixelOpacity, 0.1F) + 0.1F;
                mineplexOpacity = RenderUtil.animate(255, mineplexOpacity, 0.1F) + 0.1F;
                if (hypixelOpacity > 255) hypixelOpacity = 255;
                if (mineplexOpacity > 255) mineplexOpacity = 255;

                hypixelY = RenderUtil.animate(menuPos.y() + 28, hypixelY, 0.2F) - 0.1F;
                mineplexY = RenderUtil.animate(menuPos.y() + hypixelY + 28 * 2 + 5, mineplexY, 0.2F) - 0.1F;

                RenderUtil.drawImage(hypixel, altsPos.width() + 5, hypixelY, 207, 116, hypixelOpacity);
                font20.drawStringWithShadow("Hypixel", altsPos.width() + 10, hypixelY + 5, -1);
                font19.drawStringWithShadow("Rank: " + info.hypixelRank(), altsPos.width() + 10, hypixelY + 8 + 10, -1);
                font19.drawStringWithShadow("Level: " + info.hypixelLevel(), altsPos.width() + 10, hypixelY + 8 + 20, -1);
                DateFormat dateFormat = new SimpleDateFormat("D'd' H'h' m'm' s's'");
                Date date = new Date(info.hypixelBanMillis() - System.currentTimeMillis());
                font19.drawStringWithShadow("Banned For: " + dateFormat.format(date), altsPos.width() + 10, hypixelY + 116 - 25, -1);
                font19.drawStringWithShadow("Status: " + info.hypixelBanned(), altsPos.width() + 10, hypixelY + 116 - 15, -1);
                RenderUtil.drawImage(mineplex, altsPos.width() + 5, mineplexY, 207, 116, mineplexOpacity);
                font20.drawStringWithShadow("Mineplex", altsPos.width() + 10, mineplexY + 5, -1);
                font19.drawStringWithShadow("Rank: " + info.mineplexRank(), altsPos.width() + 10, mineplexY + 8 + 10, -1);
                font19.drawStringWithShadow("Level: " + info.mineplexLevel(), altsPos.width() + 10, mineplexY + 8 + 20, -1);
                font19.drawStringWithShadow("Status: " + info.mineplexBanned(), altsPos.width() + 10, mineplexY + 116 - 15, -1);
                RenderUtil.drawDynamicTexture(selectedAccount.body(), 207f / 2 + menuPos.width() - altsPos.width() / 2 - 40, hypixelY, 80, 180);

                accountLoginButton.setPosition(207f / 2 + menuPos.width() - altsPos.width() / 2 - accountLoginButton.position().width() / 2, hypixelY + 200, 200, 20);
                accountLoginButton.drawScreen(mouseX, mouseY);
                accountRemoveButton.drawScreen(mouseX, mouseY);
                accountRemoveButton.setPosition(207f / 2 + menuPos.width() - altsPos.width() / 2 - accountRemoveButton.position().width() / 2, hypixelY + 200 + accountLoginButton.position().height() + 10, 200, 20);
            }
            RenderUtil.destroyCropBox();

        } else {
            float menuWidth = 300;
            menuPos.height(RenderUtil.animate(scaledResolution.getScaledHeight() - 60, menuPos.height(), 0.3f) - 0.1F);
            menuPos.y(RenderUtil.animate(menuPos.originalY(), menuPos.y(), 0.3f) - 0.1F);
            menuPos.width(RenderUtil.animate((scaledResolution.getScaledWidth() - 60) / 2f + menuWidth / 2, menuPos.width(), 0.3f) - 0.1F);
            menuPos.x(RenderUtil.animate((scaledResolution.getScaledWidth() - 60) / 2f - menuWidth / 2, menuPos.x(), 0.3f) - 0.1F);
            emailField.setPosition(menuPos.x() + menuWidth / 2 - 120 / 2f, menuPos.y() + menuPos.height() / 2 - (150f / scaledResolution.getScaleFactor()), 120, 15);
            passwordField.setPosition(emailField.x(), emailField.y() + 25, 120, 15);
            clipboardButton.setPosition(emailField.x(), emailField.y() + 50, 120, 12);
            directLoginButton.setPosition(emailField.x(), emailField.y() + 65, 120, 12);
            addAccountButton.setPosition(emailField.x(), emailField.y() + 80, 120, 12);
            importListButton.setPosition(emailField.x(), emailField.y() + 95, 120, 12);
            RenderUtil.makeCropBox(menuPos.x(), menuPos.y(), menuPos.x() + menuPos.width(), menuPos.y() + menuPos.height());
            Gui.drawRect(menuPos.x(), menuPos.y(), menuPos.width(), menuPos.height(), 0xff2b2b2b);
            emailField.drawField(mouseX, mouseY);
            passwordField.drawField(mouseX, mouseY);
            font20.drawStringWithShadow(AltService.loginStatus(), emailField.x(), emailField.y() - 20, -1);
            clipboardButton.drawScreen(mouseX, mouseY);
            directLoginButton.drawScreen(mouseX, mouseY);
            addAccountButton.drawScreen(mouseX, mouseY);
            importListButton.drawScreen(mouseX, mouseY);
            RenderUtil.destroyCropBox();
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (addingAlt) {
            emailField.mouseClicked(mouseX, mouseY, mouseButton);
            passwordField.mouseClicked(mouseX, mouseY, mouseButton);
            clipboardButton.mouseClicked(mouseX, mouseY, mouseButton, button -> {
                if (button == 0) {
                    mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                    String clipboardContents = StringUtil.trimmedClipboardContents();
                    String[] split = clipboardContents.split(":");
                    if (split[0] != null) emailField.text(split[0]);
                    if (split.length > 1) passwordField.text(split[1]);
                }
            });
            directLoginButton.mouseClicked(mouseX, mouseY, mouseButton, button -> {
                if (button == 0) {
                    mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                    String email = emailField.text();
                    String password = passwordField.text();
                    if (email != null) {
                        Client.INSTANCE.getAccountRepository().currentAccount(new Account(AlteningServiceType.MOJANG, email, password));
                        Client.INSTANCE.getAccountRepository().currentAccount().login(true);
                    }
                }
            });
            addAccountButton.mouseClicked(mouseX, mouseY, mouseButton, button -> {
                if (button == 0) {
                    mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                    String email = emailField.text();
                    String password = passwordField.text();
                    if (email != null && !email.isEmpty()) {
                        Client.INSTANCE.getAccountRepository().addAccount(email.contains("@alt.com") ? AlteningServiceType.THEALTENING : AlteningServiceType.MOJANG, email, password);
                        addingAlt = false;
                    }
                }
            });
        } else {
            if (selectedAccount != null) {
                accountLoginButton.mouseClicked(mouseX, mouseY, mouseButton, button -> {
                    if (button == 0) {
                        mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                        selectedAccount.login(false);
                    }
                });
                accountRemoveButton.mouseClicked(mouseX, mouseY, mouseButton, button -> {
                    if (button == 0) {
                        mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                        Client.INSTANCE.getAccountRepository().removeAccount(selectedAccount);
                    }
                });
            }
            addButton.mouseClicked(mouseX, mouseY, mouseButton, button -> {
                if (button == 0) {
                    mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                    addingAlt = true;
                }
            });
            int accountHeight = 40;
            int offset = 3;

            selectedAccount = null;
            for (int i = 0; i < Client.INSTANCE.getAccountRepository().accounts().size(); i++) {
                Account account = Client.INSTANCE.getAccountRepository().accounts().get(i);
                if (RenderUtil.inBounds(altsPos.x(), altsPos.y() + account.y(), altsPos.width(), altsPos.y() + account.y() + accountHeight, mouseX, mouseY)) {
                    float altsY = altsPos.y() + account.y();
//                    if(selectedAccount != null && RenderUtil.inBounds(altsPos.width() - offset - deleteAccountPos.width(), altsY, altsPos.width() - offset, altsY + accountHeight, mouseX, mouseY)) {
//                        if(mouseButton == 0) {
//                            mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
//                            StrifeClient.INSTANCE.accountRepository().removeAccount(selectedAccount);
//                        }
//                        return;
//                    }
                    if (mouseButton == 0) {
                        mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                        hypixelY = menuPos.y();
                        hypixelOpacity = 100;
                        mineplexY = hypixelY + 50;
                        mineplexOpacity = 100;
                        selectedAccount = account;
                    } else {
                        account.login(false);
                    }
                }
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (addingAlt) {
            emailField.keyTyped(typedChar, keyCode);
            passwordField.keyTyped(typedChar, keyCode);
            if (keyCode == 1)
                addingAlt = false;
        } else {
            if (keyCode == 1) {
                if (parent != null) {
                    if (parent instanceof StrifeMainMenu) ((StrifeMainMenu) parent).setPass(shader.getPass());
                    mc.displayGuiScreen(parent);
                } else mc.displayGuiScreen(new StrifeMainMenu(shader.getPass()));
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (Mouse.hasWheel()) {
            int pixels = 40;
            int direction = Mouse.getEventDWheel();
            if (direction > 0) {
                direction = 1;
            } else {
                direction = 0;
            }
            scroll = scroll - direction / pixels;
            scroll = MathHelper.clamp_float(scroll, 0.0F, 1.0F);
        }
    }

    @Override
    public void updateScreen() {
        emailField.updateScreen();
        passwordField.updateScreen();
    }


    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public void setPass(int pass) {
        shader.setPass(pass);
    }

}
