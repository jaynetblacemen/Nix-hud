package me.Azz_9.flex_hud.client.configurableModules.modules.notHud;

//import club.minnced.discord.rpc.DiscordRPC;
//import club.minnced.discord.rpc.DiscordRichPresence;
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

public class DiscordRpc extends AbstractModule implements TickableModule {

    private final ConfigString firstLine;
    private final ConfigString secondLine;
    // private final DiscordRPC lib = DiscordRPC.INSTANCE;
    // private final DiscordRichPresence presence = new DiscordRichPresence();
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
        /*
         * if (isEnabled()) {
         * if (!isRpcInitialized) {
         * initRpc();
         * }
         * 
         * // Update presence periodically (every 2 seconds or so to avoid rate
         * // limits/spam)
         * if (System.currentTimeMillis() - lastTimeUpdate > 2000) {
         * updatePresence();
         * lastTimeUpdate = System.currentTimeMillis();
         * }
         * 
         * lib.Discord_RunCallbacks();
         * } else {
         * if (isRpcInitialized) {
         * shutdownRpc();
         * }
         * }
         */
    }

    private void initRpc() {
        /*
         * // Placeholder App ID - User must replace this!
         * String applicationId = "1459588408799137978";
         * lib.Discord_Initialize(applicationId, null, true, "");
         * presence.startTimestamp = System.currentTimeMillis() / 1000;
         * isRpcInitialized = true;
         */
    }

    private void updatePresence() {
        /*
         * presence.details = firstLine.getValue();
         * presence.state = secondLine.getValue();
         * lib.Discord_UpdatePresence(presence);
         */
    }

    private void shutdownRpc() {
        /*
         * lib.Discord_ClearPresence();
         * lib.Discord_Shutdown();
         * isRpcInitialized = false;
         */
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
