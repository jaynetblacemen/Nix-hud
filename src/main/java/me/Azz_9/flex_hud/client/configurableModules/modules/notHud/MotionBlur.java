package me.Azz_9.flex_hud.client.configurableModules.modules.notHud;

import me.Azz_9.flex_hud.client.configurableModules.ConfigRegistry;
import me.Azz_9.flex_hud.client.configurableModules.modules.AbstractModule;
import me.Azz_9.flex_hud.client.configurableModules.modules.TickableModule;
import me.Azz_9.flex_hud.client.mixin.GameRendererAccessor;
import me.Azz_9.flex_hud.client.screens.configurationScreen.AbstractConfigurationScreen;
import me.Azz_9.flex_hud.client.screens.configurationScreen.configEntries.IntSliderEntry;
import me.Azz_9.flex_hud.client.screens.configurationScreen.configEntries.ToggleButtonEntry;
import me.Azz_9.flex_hud.client.screens.configurationScreen.configVariables.ConfigBoolean;
import me.Azz_9.flex_hud.client.screens.configurationScreen.configVariables.ConfigInteger;
import me.Azz_9.flex_hud.client.screens.configurationScreen.crosshairConfigScreen.AbstractCrosshairConfigScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.lang.reflect.Method;

public class MotionBlur extends AbstractModule implements TickableModule {

    private final ConfigInteger intensity;
    private final ConfigBoolean smartBlur;
    private final ConfigInteger fpsThreshold;

    private boolean isShaderLoaded = false;
    private static final Identifier SHADER_ID = Identifier.of("flex_hud", "motion_blur");

    private double lastX, lastY, lastZ;
    private float lastYaw, lastPitch;
    private static MotionBlur INSTANCE;

    public MotionBlur() {
        this.enabled.setConfigTextTranslationKey("flex_hud.motion_blur.config.enable");

        this.intensity = new ConfigInteger(50, "intensity", 0, 100);
        this.smartBlur = new ConfigBoolean(false, "smart_blur");
        this.fpsThreshold = new ConfigInteger(60, "fps_threshold", 10, 260);

        ConfigRegistry.register(getID(), "intensity", intensity);
        ConfigRegistry.register(getID(), "smart_blur", smartBlur);
        ConfigRegistry.register(getID(), "fps_threshold", fpsThreshold);

        INSTANCE = this;
    }

    @Override
    public Text getName() {
        return Text.translatable("flex_hud.motion_blur");
    }

    @Override
    public String getID() {
        return "motion_blur";
    }

    @Override
    public void init() {
        // Use Mixin instead of Fabric API event
    }

    public static void onWorldRenderEnd(float tickDelta) {
        if (INSTANCE != null && INSTANCE.isEnabled()) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null || client.isPaused())
                return;
            INSTANCE.updateBlur(client, tickDelta);
        }
    }

    @Override
    public void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null)
            return;

        boolean shouldEnable = isEnabled();

        if (shouldEnable && smartBlur.getValue()) {
            if (client.getCurrentFps() < fpsThreshold.getValue()) {
                shouldEnable = false;
            }
        }

        boolean isEnabled = shouldEnable;
        if (isEnabled) {
            if (!isShaderLoaded) {
                loadShader(client);
            }
        } else {
            if (isShaderLoaded) {
                unloadShader(client);
            }
        }
    }

    private void updateBlur(MinecraftClient client, float tickDelta) {
        if (!isShaderLoaded)
            return;

        net.minecraft.client.render.Camera camera = client.gameRenderer.getCamera();
        double x = camera.getCameraPos().x;
        double y = camera.getCameraPos().y;
        double z = camera.getCameraPos().z;
        float yaw = camera.getYaw();
        float pitch = camera.getPitch();

        // Calculate deltas
        double dx = x - lastX;
        double dy = y - lastY;
        double dz = z - lastZ;
        float dyaw = yaw - lastYaw;
        float dpitch = pitch - lastPitch;

        // Normalize rotation deltas to -180...180
        while (dyaw >= 180)
            dyaw -= 360;
        while (dyaw < -180)
            dyaw += 360;
        while (dpitch >= 180)
            dpitch -= 360;
        while (dpitch < -180)
            dpitch += 360;

        // Approximate "motion magnitude"
        double velocity = Math.sqrt(dx * dx + dy * dy + dz * dz) * 1.0 + Math.sqrt(dyaw * dyaw + dpitch * dpitch) * 0.1;

        // Update last values
        lastX = x;
        lastY = y;
        lastZ = z;
        lastYaw = yaw;
        lastPitch = pitch;

        float sensitivity = intensity.getValue() / 100.0f;
        float blurFactor = (float) (velocity * sensitivity * 2.5);

        float finalValue = Math.min(Math.max(blurFactor, 0.0f), 0.96f);
        if (finalValue < 0.02f)
            finalValue = 0.0f;

        me.Azz_9.flex_hud.client.utils.MotionBlurUniforms.setUniform(client, "motion_blur", "Phosphor", finalValue);
    }

    private void loadShader(MinecraftClient client) {
        client.execute(() -> {
            try {
                if (client.gameRenderer instanceof GameRendererAccessor accessor) {
                    accessor.invokeSetPostProcessor(SHADER_ID);
                    isShaderLoaded = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                isShaderLoaded = false;
            }
        });
    }

    private void unloadShader(MinecraftClient client) {
        client.execute(() -> {
            try {
                // Use reflection to find a clear/disable method if accessor fails or isn't
                // enough
                // But first try simple accessor with null if possible, or standard clear
                Method m = null;
                try {
                    m = net.minecraft.client.render.GameRenderer.class.getMethod("clearPostProcessor");
                    m.invoke(client.gameRenderer);
                } catch (NoSuchMethodException e) {
                    try {
                        m = net.minecraft.client.render.GameRenderer.class.getMethod("disablePostProcessor");
                        m.invoke(client.gameRenderer);
                    } catch (NoSuchMethodException ex) {
                        // Fallback: set to null via accessor
                        if (client.gameRenderer instanceof GameRendererAccessor accessor) {
                            accessor.invokeSetPostProcessor(null);
                        }
                    }
                }
                isShaderLoaded = false;

                // Reset history
                lastX = 0;
                lastY = 0;
                lastZ = 0;
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public AbstractConfigurationScreen getConfigScreen(Screen parent) {
        return new AbstractCrosshairConfigScreen(getName(), parent) {
            @Override
            protected void init() {
                super.init();

                ToggleButtonEntry smartBlurEntry = new ToggleButtonEntry.Builder()
                        .setToggleButtonWidth(buttonWidth)
                        .setVariable(smartBlur)
                        .build();

                this.addAllEntries(
                        new ToggleButtonEntry.Builder()
                                .setToggleButtonWidth(buttonWidth)
                                .setVariable(enabled)
                                .build(),
                        new IntSliderEntry.Builder()
                                .setIntSliderWidth(buttonWidth)
                                .setVariable(intensity)
                                .setStep(1)
                                .build(),
                        smartBlurEntry,
                        new IntSliderEntry.Builder()
                                .setIntSliderWidth(buttonWidth)
                                .setVariable(fpsThreshold)
                                .setStep(1)
                                .addDependency(smartBlurEntry, false)
                                .build());
            }
        };
    }
}
