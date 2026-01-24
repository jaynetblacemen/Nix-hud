package me.Azz_9.flex_hud.client.screens.modulesList;

import me.Azz_9.flex_hud.client.configurableModules.ModulesHelper;
import me.Azz_9.flex_hud.client.screens.AbstractSmoothScrollableList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ScrollableModulesList extends AbstractSmoothScrollableList<ScrollableModulesList.Entry> {

	private final List<Entry> entries = new ArrayList<>();
	private int buttonWidth;
	private int buttonHeight;
	private int iconWidthHeight;
	private int padding;
	private int columns;

	public ScrollableModulesList(MinecraftClient client, int width, int height, int top, int itemHeight,
			int buttonWidth, int buttonHeight, int iconWidthHeight, int padding, int columns) {
		super(client, width, height, top, itemHeight);
		this.buttonWidth = buttonWidth;
		this.buttonHeight = buttonHeight;
		this.iconWidthHeight = iconWidthHeight;
		this.padding = padding;
		this.columns = columns;
	}

	/*
	 * modules' size needs to be equal or lower than number of columns
	 */
	public void addModule(List<Module> modules) {
		assert modules.size() <= columns;
		if (modules.size() < columns) {
			for (int i = 0; i < columns - modules.size(); i++) {
				modules.add(null);
			}
		}
		Entry entry = new Entry(modules, this);
		this.entries.add(entry);
		this.addEntry(entry);
	}

	public void filterModules(String query) {
		String finalQuery = query.toLowerCase().strip();
		super.clearEntries();

		List<Entry> newEntries = new ArrayList<>();
		List<Module> modulesInEntry = new ArrayList<>(); // modules in current entry

		Consumer<Module> addModuleToEntry = module -> {
			modulesInEntry.add(module);

			if (modulesInEntry.size() == columns) {
				newEntries.add(new Entry(new ArrayList<>(modulesInEntry), this));
				modulesInEntry.clear();
			}
		};

		for (Entry entry : this.entries) {
			// Vérification pour les colonnes
			for (Module module : entry.rowModules) {
				if (module != null && module.keywords.stream().anyMatch(keyword -> keyword.contains(finalQuery))) {
					addModuleToEntry.accept(module);
				}
			}
		}

		// Ajouter une entrée incomplète si nécessaire
		if (!modulesInEntry.isEmpty()) {
			newEntries.add(new Entry(modulesInEntry, this));
		}

		for (Entry entry : newEntries) {
			this.addEntry(entry);
		}

		updateScroll();

	}

	public void updateScroll() {
		if (this.getScrollY() > this.getMaxScrollY()) {
			this.setScrollY(this.getMaxScrollY());
		}
	}

	@Override
	public int getRowWidth() {
		return buttonWidth * columns + padding * (columns - 1);
	}

	public int getButtonWidth() {
		return buttonWidth;
	}

	public int getButtonHeight() {
		return buttonHeight;
	}

	public void setColumns(int columns) {
		this.columns = columns;
		ModulesHelper.getInstance().numberOfColumns.setValue(columns);
	}

	public List<Entry> getEntries() {
		return entries;
	}

	@Override
	protected void clearEntries() {
		super.clearEntries();
		this.entries.clear();
	}

	public static class Entry extends ElementListWidget.Entry<Entry> {
		private final List<Module> rowModules;
		private final ScrollableModulesList scrollableModulesList;

		public Entry(List<Module> rowModules, ScrollableModulesList scrollableModulesList) {
			this.rowModules = rowModules;
			this.scrollableModulesList = scrollableModulesList;
		}

		@Override
		public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			int rowWidth = scrollableModulesList.getRowWidth();
			int startX = getX() + (getWidth() - rowWidth) / 2;

			for (int i = 0; i < rowModules.size(); i++) {
				Module module = rowModules.get(i);
				if (module == null) {
					break;
				}

				int x = startX + i * (scrollableModulesList.buttonWidth + scrollableModulesList.padding);
				int y = getY();

				int cardX = x - 2;
				int cardY = y + 4;
				int cardWidth = scrollableModulesList.buttonWidth + 4;
				int cardHeight = scrollableModulesList.itemHeight - 8;

				// Solid Pit Black Background (0xFF000000)
				// High Curvature (Radius ~6-8px)
				// 1. Central body
				context.fill(cardX + 6, cardY, cardX + cardWidth - 6, cardY + cardHeight, 0xFF000000);
				// 2. Inner side blocks
				context.fill(cardX + 2, cardY + 2, cardX + 6, cardY + cardHeight - 2, 0xFF000000);
				context.fill(cardX + cardWidth - 6, cardY + 2, cardX + cardWidth - 2, cardY + cardHeight - 2,
						0xFF000000);
				// 3. Outer side blocks
				context.fill(cardX, cardY + 6, cardX + 2, cardY + cardHeight - 6, 0xFF000000);
				context.fill(cardX + cardWidth - 2, cardY + 6, cardX + cardWidth, cardY + cardHeight - 6, 0xFF000000);

				// Optional: Extra curvature steps for smoother arc
				context.fill(cardX + 1, cardY + 4, cardX + 2, cardY + 6, 0xFF000000);
				context.fill(cardX + 1, cardY + cardHeight - 6, cardX + 2, cardY + cardHeight - 4, 0xFF000000);
				context.fill(cardX + cardWidth - 2, cardY + 4, cardX + cardWidth - 1, cardY + 6, 0xFF000000);
				context.fill(cardX + cardWidth - 2, cardY + cardHeight - 6, cardX + cardWidth - 1,
						cardY + cardHeight - 4, 0xFF000000);

				// Draw manual border (following high curvature)
				int borderCol = 0xFF111111; // Darker for "Pit" look
				// Horizontal lines
				context.fill(cardX + 6, cardY, cardX + cardWidth - 6, cardY + 1, borderCol); // Top
				context.fill(cardX + 6, cardY + cardHeight - 1, cardX + cardWidth - 6, cardY + cardHeight, borderCol); // Bottom
				// Vertical lines
				context.fill(cardX, cardY + 6, cardX + 1, cardY + cardHeight - 6, borderCol); // Left
				context.fill(cardX + cardWidth - 1, cardY + 6, cardX + cardWidth, cardY + cardHeight - 6, borderCol); // Right

				// Corner arcs (manual pixels)
				// Top-Left
				context.fill(cardX + 1, cardY + 4, cardX + 2, cardY + 6, borderCol);
				context.fill(cardX + 2, cardY + 2, cardX + 4, cardY + 4, borderCol);
				context.fill(cardX + 4, cardY + 1, cardX + 6, cardY + 2, borderCol);
				// Top-Right
				context.fill(cardX + cardWidth - 2, cardY + 4, cardX + cardWidth - 1, cardY + 6, borderCol);
				context.fill(cardX + cardWidth - 4, cardY + 2, cardX + cardWidth - 2, cardY + 4, borderCol);
				context.fill(cardX + cardWidth - 6, cardY + 1, cardX + cardWidth - 4, cardY + 2, borderCol);
				// Bottom-Left
				context.fill(cardX + 1, cardY + cardHeight - 6, cardX + 2, cardY + cardHeight - 4, borderCol);
				context.fill(cardX + 2, cardY + cardHeight - 4, cardX + 4, cardY + cardHeight - 2, borderCol);
				context.fill(cardX + 4, cardY + cardHeight - 2, cardX + 6, cardY + cardHeight - 1, borderCol);
				// Bottom-Right
				context.fill(cardX + cardWidth - 2, cardY + cardHeight - 6, cardX + cardWidth - 1,
						cardY + cardHeight - 4, borderCol);
				context.fill(cardX + cardWidth - 4, cardY + cardHeight - 4, cardX + cardWidth - 2,
						cardY + cardHeight - 2, borderCol);
				context.fill(cardX + cardWidth - 6, cardY + cardHeight - 2, cardX + cardWidth - 4,
						cardY + cardHeight - 1, borderCol);

				// Render Icon
				int iconX = x + (scrollableModulesList.buttonWidth - scrollableModulesList.iconWidthHeight) / 2;
				context.drawTexture(RenderPipelines.GUI_TEXTURED, module.icon, iconX, y + 2, 0, 0,
						scrollableModulesList.iconWidthHeight, scrollableModulesList.iconWidthHeight,
						scrollableModulesList.iconWidthHeight, scrollableModulesList.iconWidthHeight);

				// Render Button
				module.button.setX(x);
				module.button.setY(y + scrollableModulesList.iconWidthHeight + 6);
				module.button.render(context, mouseX, mouseY, deltaTicks);
			}
		}

		@Override
		public List<ClickableWidget> children() {
			List<ClickableWidget> clickableWidgets = new ArrayList<>();
			for (Module module : rowModules) {
				if (module != null) {
					clickableWidgets.add(module.button);
				}
			}
			return clickableWidgets;
		}

		@Override
		public List<ClickableWidget> selectableChildren() {
			return this.children();
		}
	}
}
