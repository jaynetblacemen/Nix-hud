package me.Azz_9.flex_hud.client.configurableModules.modules.notHud;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.RichPresence;
import me.Azz_9.flex_hud.client.configurableModules.ConfigRegistry;
import me.Azz_9.flex_hud.client.configurableModules.modules.AbstractModule;
import me.Azz_9.flex_hud.client.configurableModules.modules.TickableModule;
import me.Azz_9.flex_hud.client.screens.configurationScreen.AbstractConfigurationScreen;
import me.Azz_9.flex_hud.client.screens.configurationScreen.configEntries.StringFieldEntry;
import me.Azz_9.flex_hud.client.screens.configurationScreen.configEntries.ToggleButtonEntry;
import me.Azz_9.flex_hud.client.screens.configurationScreen.configVariables.ConfigString;
import me.Azz_9.flex_hud.client.screens.configurationScreen.crosshairConfigScreen.AbstractCrosshairConfigScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.time.OffsetDateTime;

public class DiscordRpc extends AbstractModule implements TickableModule {

    private final ConfigString firstLine;
    private final ConfigString secondLine;

    // Placeholder - user needs to replace this
    private static final long CLIENT_ID = 1459588408799137978L;
    private IPCClient client;

    private boolean isRpcInitialized = false;
    private long lastTimeUpdate = 0;

    public DiscordRpc() {
        this.enabled.setConfigTextTranslationKey("flex_hud.discord_rpc.config.enable");

        this.firstLine = new ConfigString("Playing Minecraft", "first_line");
        this.secondLine = new ConfigString("In Game", "second_line");

        ConfigRegistry.register(getID(), "first_line", firstLine);
        ConfigRegistry.register(getID(), "second_line", secondLine);
    }

    @Override
    public Text getName() {
        return Text.translatable("flex_hud.discord_rpc");
    }

    @Override
    public String getID() {
        return "discord_rpc";
    }

    @Override
    public void tick() {
        if (isEnabled()) {
            if (!isRpcInitialized) {
                initRpc();
            }

            // periodical updates if needed
            if (System.currentTimeMillis() - lastTimeUpdate > 2000) {
                updatePresence();
                lastTimeUpdate = System.currentTimeMillis();
            }
        } else {
            if (isRpcInitialized) {
                shutdownRpc();
            }
        }
    }

    private void initRpc() {
        try {
            client = new IPCClient(CLIENT_ID);
            client.setListener(new IPCListener() {
                @Override
                public void onReady(IPCClient client) {
                    // connected
                }
            });
            client.connect();
            isRpcInitialized = true;
        } catch (Exception e) {
            e.printStackTrace();
            isRpcInitialized = false; // Retry later
        }
    }

    private void updatePresence() {
        if (client == null)
            return;

        RichPresence.Builder builder = new RichPresence.Builder();
        builder.setDetails(firstLine.getValue())
                .setState(secondLine.getValue())
                .setStartTimestamp(OffsetDateTime.now());

        try {
            client.sendRichPresence(builder.build());
        } catch (IllegalStateException e) {
            isRpcInitialized = false; // Assume disconnected
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void shutdownRpc() {
        if (client != null) {
            client.close();
        }
        isRpcInitialized = false;
    }

    @Override
    public AbstractConfigurationScreen getConfigScreen(Screen parent) {
        return new AbstractCrosshairConfigScreen(getName(), parent) {
            @Override
            protected void init() {
                if (MinecraftClient.getInstance().getLanguageManager().getLanguage().equals("fr_fr")) {
                    buttonWidth = 200;
                }

                super.init();

                this.addAllEntries(
                        new ToggleButtonEntry.Builder()
                                .setToggleButtonWidth(buttonWidth)
                                .setVariable(enabled)
                                .build(),
                        new StringFieldEntry.Builder()
                                .setStringFieldWidth(buttonWidth)
                                .setVariable(firstLine)
                                .build(),
                        new StringFieldEntry.Builder()
                                .setStringFieldWidth(buttonWidth)
                                .setVariable(secondLine)
                                .build());
            }
        };
    }
}
