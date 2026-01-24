package me.Azz_9.flex_hud.client.mixin;

import me.Azz_9.flex_hud.client.utils.DrawingUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screen.option.LanguageOptionsScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Shadow
    private net.minecraft.client.gui.screen.SplashTextRenderer splashText;

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        this.splashText = null; // Hide splash text
        this.clearChildren();
        // ... (existing button setup)

        int centerY = this.height / 2;
        int centerX = this.width / 2;
        int btnWidth = 200;
        int btnHeight = 16;

        // Singleplayer
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.singleplayer"), button -> {
            this.client.setScreen(new SelectWorldScreen(this));
        }).dimensions(centerX - btnWidth / 2, centerY - 20, btnWidth, btnHeight).build());

        // Multiplayer
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.multiplayer"), button -> {
            this.client.setScreen(new MultiplayerScreen(this));
        }).dimensions(centerX - btnWidth / 2, centerY + 2, btnWidth, btnHeight).build());

        // Options
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.options"), button -> {
            this.client.setScreen(new OptionsScreen(this, this.client.options));
        }).dimensions(centerX - btnWidth / 2, centerY + 24, btnWidth, btnHeight).build());

        // Bottom left square buttons
        int sqSize = 20;
        int bottomY = this.height - 30;

        // Quit Button (X icon or literal)
        this.addDrawableChild(ButtonWidget.builder(Text.literal("X"), button -> {
            this.client.scheduleStop();
        }).dimensions(10, bottomY, sqSize, sqSize).build());

        // Language
        this.addDrawableChild(ButtonWidget.builder(Text.literal("L"), button -> {
            this.client
                    .setScreen(new LanguageOptionsScreen(this, this.client.options, this.client.getLanguageManager()));
        }).dimensions(35, bottomY, sqSize, sqSize).build());

        // Accessibility
        this.addDrawableChild(ButtonWidget.builder(Text.literal("A"), button -> {
            this.client.setScreen(new AccessibilityOptionsScreen(this, this.client.options));
        }).dimensions(60, bottomY, sqSize, sqSize).build());
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // Draw the background first
        DrawingUtils.drawCustomBackground(context, this.width, this.height);

        // Render our Nix-client logo
        Identifier logo = Identifier.of("flex_hud", "textures/background/nix_client.png");
        int logoWidth = 128; // Adjust based on actual image size
        int logoHeight = 32; // Adjust based on actual image size
        context.drawTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED, logo, this.width / 2 - logoWidth / 2,
                40, 0.0f, 0.0f, logoWidth, logoHeight, logoWidth, logoHeight);

        // Draw buttons and other elements by calling super.render
        super.render(context, mouseX, mouseY, delta);

        // Cancel the original render to skip logo and splash text
        ci.cancel();
    }
}
