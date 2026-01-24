package me.Azz_9.flex_hud.client.mixin;

import me.Azz_9.flex_hud.client.utils.DrawingUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClickableWidget.class)
public abstract class ButtonWidgetMixin {

    @Shadow
    public int x;
    @Shadow
    public int y;
    @Shadow
    protected int width;
    @Shadow
    protected int height;
    @Shadow
    protected boolean hovered;
    @Shadow
    public boolean active;
    @Shadow
    public boolean visible;
    @Shadow
    protected float alpha;

    @Shadow
    public abstract Text getMessage();

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!this.visible)
            return;

        // ONLY apply to ButtonWidget to avoid breaking text fields (like server
        // address)
        if (!((Object) this instanceof ButtonWidget))
            return;

        // Update hovered state (manual calculation since we're at HEAD)
        this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width
                && mouseY < this.y + this.height;

        int renderHeight = this.height;
        int renderY = this.y;

        // If it's a standard button (height 20), make it thinner (16)
        if (this.height == 20) {
            renderHeight = 16;
            renderY = this.y + 2;
        }

        if (!this.active) {
            // Render disabled state
            DrawingUtils.drawRoundedRect(context, this.x, renderY, this.width, renderHeight, 4, 0x80303030);
        } else {
            // Render matte black with hover effect
            int color = this.hovered ? 0xFF202020 : 0xFF101010;
            DrawingUtils.drawRoundedRect(context, this.x, renderY, this.width, renderHeight, 4, color);

            // Add a subtle border if hovered
            if (this.hovered) {
                DrawingUtils.drawBorder(context, this.x, renderY, this.width, renderHeight, 1, 0xFF404040);
            }
        }

        // Render text
        TextRenderer textRenderer = net.minecraft.client.MinecraftClient.getInstance().textRenderer;
        int i = this.active ? 0xFFFFFF : 0xA0A0A0;
        context.drawCenteredTextWithShadow(textRenderer, this.getMessage(), this.x + this.width / 2,
                renderY + (renderHeight - 8) / 2, i | MathHelper.ceil(this.alpha * 255.0F) << 24);

        ci.cancel();
    }
}
