package me.Azz_9.flex_hud.client.screens.modulesList;

import com.google.common.collect.ImmutableList;
import me.Azz_9.flex_hud.client.screens.configurationScreen.AbstractConfigurationScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

import static me.Azz_9.flex_hud.client.Flex_hudClient.MOD_ID;

public class Module {
	public String name;
	public String id;
	public Identifier icon;
	public AbstractConfigurationScreen configScreen;
	public ClickableWidget button;
	public ImmutableList<String> keywords;

	public Module(String name, String id, AbstractConfigurationScreen configScreen, int buttonWidth, int buttonHeight,
			ModulesListScreen parent, Supplier<Tooltip> getTooltip, ImmutableList<String> keywords) {
		if (name == null) {
			this.setAllNull();
		} else {
			this.name = name;
			this.id = id;
			this.icon = Identifier.of(MOD_ID, "modules_icons/" + id + ".png");
			this.configScreen = configScreen;
			this.button = new ClickableWidget(0, 0, buttonWidth, buttonHeight, Text.literal(name)) {
				@Override
				protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
					if (!this.visible)
						return;

					int bgColor = this.isHovered() ? 0xFF333333 : 0xFF181818; // Modern dark grey
					// Draw solid flat background
					context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), bgColor);

					// Draw very subtle modern border
					int borderCol = 0xFF222222;
					context.fill(getX(), getY(), getX() + getWidth(), getY() + 1, borderCol);
					context.fill(getX(), getY() + getHeight() - 1, getX() + getWidth(), getY() + getHeight(),
							borderCol);
					context.fill(getX(), getY(), getX() + 1, getY() + getHeight(), borderCol);
					context.fill(getX() + getWidth() - 1, getY(), getX() + getWidth(), getY() + getHeight(), borderCol);

					// Draw centered text
					TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
					int textX = getX() + (getWidth() - textRenderer.getWidth(getMessage())) / 2;
					int textY = getY() + (getHeight() - 8) / 2;
					context.drawTextWithShadow(textRenderer, getMessage(), textX, textY, 0xFFFFFFFF);
				}

				@Override
				public void onClick(Click click, boolean bl) {
					configScreen.setParentScrollAmount(parent.getModulesListWidget().getScrollY());
					MinecraftClient.getInstance().setScreen(configScreen);
				}

				@Override
				protected void appendClickableNarrations(NarrationMessageBuilder builder) {
					this.appendDefaultNarrations(builder);
				}

				@Override
				public void playDownSound(SoundManager soundManager) {
					ClickableWidget.playClickSound(soundManager);
				}
			};

			if (getTooltip != null) {
				this.button.setTooltip(getTooltip.get());
			}
			this.keywords = keywords;
		}
	}

	private void setAllNull() {
		this.name = null;
		this.id = null;
		this.icon = null;
		this.configScreen = null;
		this.button = null;
		this.keywords = null;
	}

	public boolean exists() {
		return this.name != null;
	}
}
