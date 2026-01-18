package me.Azz_9.flex_hud.client.screens;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public abstract class AbstractBackNavigableScreen extends Screen {
	protected final Screen PARENT;

	protected AbstractBackNavigableScreen(Text title, Screen parent) {
		super(title);
		this.PARENT = parent;
	}

	public void close() {
		if (client != null && PARENT != null) {
			client.setScreen(PARENT);
		} else {
			super.close();
		}
	}

	@Override
	public void renderBackground(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
		me.Azz_9.flex_hud.client.utils.DrawingUtils.drawModernBackground(context, this.width, this.height);
	}
}
