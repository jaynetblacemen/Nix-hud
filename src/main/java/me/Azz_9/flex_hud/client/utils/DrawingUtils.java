package me.Azz_9.flex_hud.client.utils;

import net.minecraft.client.gui.DrawContext;

public class DrawingUtils {

	public static void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
		drawBorder(context, x, y, width, height, 1, color);
	}

	public static void drawBorder(DrawContext context, int x, int y, int width, int height, int thick, int color) {
		context.fill(x, y, x + width, y + thick, color);
		context.fill(x, y + height - thick, x + width, y + height, color);
		context.fill(x, y + thick, x + thick, y + height - thick, color);
		context.fill(x + width - thick, y + thick, x + width, y + height - thick, color);
	}

	public static void drawModernBackground(DrawContext context, int width, int height) {
		// Modern dark gradient background
		// Top color: Very dark grey (almost black)
		// Bottom color: Pure black
		// High alpha to keep focus on UI but allow slight perceived depth
		int topColor = 0xF0101010;
		int bottomColor = 0xF0000000;
		context.fillGradient(0, 0, width, height, topColor, bottomColor);
	}

	public static void drawCustomBackground(DrawContext context, int width, int height) {
		net.minecraft.util.Identifier background = net.minecraft.util.Identifier.of("flex_hud",
				"textures/background/mainmenu_background.png");
		context.drawTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED, background, 0, 0, 0, 0, width, height,
				width, height);
	}

	public static void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int radius,
			int color) {
		// Simple rounded rectangle approximation using multiple fills
		// Corners
		context.fill(x + radius, y, x + width - radius, y + height, color); // Main body horizontal
		context.fill(x, y + radius, x + radius, y + height - radius, color); // Left body
		context.fill(x + width - radius, y + radius, x + width, y + height - radius, color); // Right body

		// Diagonal approximation for corners (better than nothing)
		// Top-left
		context.fill(x + 1, y + 1, x + radius, y + radius, color);
		// Top-right
		context.fill(x + width - radius, y + 1, x + width - 1, y + radius, color);
		// Bottom-left
		context.fill(x + 1, y + height - radius, x + radius, y + height - 1, color);
		// Bottom-right
		context.fill(x + width - radius, y + height - radius, x + width - 1, y + height - 1, color);
	}
}
