package me.Azz_9.flex_hud.client.configurableModules.modules.notHud;

import me.Azz_9.flex_hud.client.configurableModules.ConfigRegistry;
import me.Azz_9.flex_hud.client.configurableModules.modules.AbstractModule;
import me.Azz_9.flex_hud.client.configurableModules.modules.TickableModule;
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

public class MotionBlur extends AbstractModule implements TickableModule {

    private final ConfigInteger intensity;
    private final ConfigBoolean smartBlur;
    private final ConfigInteger fpsThreshold;

    private boolean isShaderLoaded = false;
    private static final Identifier SHADER_ID = Identifier.of("flex_hud", "motion_blur");

    private double lastX, lastY, lastZ;
    private float lastYaw, lastPitch;

    public MotionBlur() {
        this.enabled.setConfigTextTranslationKey("flex_hud.motion_blur.config.enable");

        this.intensity = new ConfigInteger(50, "intensity", 0, 100);
        this.smartBlur = new ConfigBoolean(false, "smart_blur");
        this.fpsThreshold = new ConfigInteger(60, "fps_threshold", 10, 260);

        ConfigRegistry.register(getID(), "intensity", intensity);
        ConfigRegistry.register(getID(), "smart_blur", smartBlur);
        ConfigRegistry.register(getID(), "fps_threshold", fpsThreshold);
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

    private net.minecraft.client.gl.PostEffectProcessor processor;
    private float currentBlurFactor = 0.0f;

    @Override
    public void tick() {
        // No longer using GameRenderer.setPostProcessor, so tick() is just for state
    }

    public void render(float tickDelta) {
        if (!isEnabled()) {
            if (processor != null) {
                processor.close();
                processor = null;
            }
            isShaderLoaded = false;
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null)
            return;

        if (processor == null) {
            loadShaderManual(client);
        }

        if (processor != null) {
            float blurValue = calculateBlurFactor(client, tickDelta);

            // Set the uniform on our private processor
            me.Azz_9.flex_hud.client.utils.MotionBlurUniforms.setUniformOnProcessor(processor,
                    "flex_hud:post/motion_blur", "Phosphor", blurValue);

            // Manually render
            net.minecraft.client.gl.Framebuffer mainBuffer = client.getFramebuffer();
            // Use reflection or accessor to get the ObjectAllocator (pool field) from
            // GameRenderer if needed,
            // or just use client.getFramebuffer() if that works.
            // In 1.21.1, render takes (Framebuffer, ObjectAllocator)
            try {
                java.lang.reflect.Field poolField = net.minecraft.client.render.GameRenderer.class
                        .getDeclaredField("pool");
                poolField.setAccessible(true);
                net.minecraft.client.util.ObjectAllocator allocator = (net.minecraft.client.util.ObjectAllocator) poolField
                        .get(client.gameRenderer);
                processor.render(mainBuffer, allocator);
            } catch (Exception e) {
                // Fallback or log
            }
        }
    }

    private void loadShaderManual(MinecraftClient client) {
        try {
            net.minecraft.client.gl.ShaderLoader loader = client.getShaderLoader();
            processor = loader.loadPostEffect(SHADER_ID, net.minecraft.client.render.DefaultFramebufferSet.MAIN_ONLY);
            isShaderLoaded = (processor != null);

            // Initialize camera state
            net.minecraft.client.render.Camera camera = client.gameRenderer.getCamera();
            lastX = camera.getCameraPos().x;
            lastY = camera.getCameraPos().y;
            lastZ = camera.getCameraPos().z;
            lastYaw = camera.getYaw();
            lastPitch = camera.getPitch();

            System.out.println("[FlexHUD] Manually loaded motion blur processor: " + SHADER_ID);
        } catch (Exception e) {
            System.err.println("[FlexHUD] Failed to manual load motion blur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private float calculateBlurFactor(MinecraftClient client, float tickDelta) {
        if (client.isPaused())
            return 0.0f;

        boolean active = true;
        if (smartBlur.getValue()) {
            if (client.getCurrentFps() < fpsThreshold.getValue()) {
                active = false;
            }
        }

        if (!active) {
            currentBlurFactor = lerp(currentBlurFactor, 0.0f, 0.1f);
            return currentBlurFactor;
        }

        net.minecraft.client.render.Camera camera = client.gameRenderer.getCamera();
        double x = camera.getCameraPos().x;
        double y = camera.getCameraPos().y;
        double z = camera.getCameraPos().z;
        float yaw = camera.getYaw();
        float pitch = camera.getPitch();

        if (lastX == 0 && lastY == 0 && lastZ == 0) {
            lastX = x;
            lastY = y;
            lastZ = z;
            lastYaw = yaw;
            lastPitch = pitch;
            return currentBlurFactor;
        }

        double dx = x - lastX;
        double dy = y - lastY;
        double dz = z - lastZ;
        float dyaw = yaw - lastYaw;
        float dpitch = pitch - lastPitch;

        while (dyaw >= 180)
            dyaw -= 360;
        while (dyaw < -180)
            dyaw += 360;
        while (dpitch >= 180)
            dpitch -= 360;
        while (dpitch < -180)
            dpitch += 360;

        double velocity = Math.sqrt(dx * dx + dy * dy + dz * dz) * 2.0
                + Math.sqrt(dyaw * dyaw + dpitch * dpitch) * 0.15;

        lastX = x;
        lastY = y;
        lastZ = z;
        lastYaw = yaw;
        lastPitch = pitch;

        float sensitivity = intensity.getValue() / 100.0f;
        float targetBlur = (float) (velocity * sensitivity * 3.0);

        // Hybrid: Minimum base blur for temporal smoothness
        float floor = 0.02f;
        targetBlur = Math.max(targetBlur, floor);
        targetBlur = Math.min(targetBlur, 0.96f);

        // Smooth transitions
        currentBlurFactor = lerp(currentBlurFactor, targetBlur, 0.2f);

        return currentBlurFactor;
    }

    private float lerp(float a, float b, float f) {
        return a + f * (b - a);
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
