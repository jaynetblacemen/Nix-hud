package me.Azz_9.flex_hud.client.mixin;

import me.Azz_9.flex_hud.client.utils.DrawingUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void onRenderBackground(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Screen screen = (Screen) (Object) this;
        // Apply custom background to almost all screens for consistency, as requested
        // But skip screens where a custom background would be intrusive (like HUD
        // overlays if they use Screen)
        if (!(screen instanceof net.minecraft.client.gui.screen.ChatScreen)) {
            DrawingUtils.drawCustomBackground(context, screen.width, screen.height);
            ci.cancel();
        }
    }
}
