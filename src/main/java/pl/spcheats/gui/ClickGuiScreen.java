package pl.spcheats.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;
import pl.spcheats.config.ConfigManager;
import pl.spcheats.module.Module;
import pl.spcheats.module.ModuleManager;
import pl.spcheats.module.NumberSetting;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClickGuiScreen extends Screen {
    private static final int PANEL_WIDTH = 340;
    private static final int TOP = 64;
    private static final int CATEGORY_ROW = 22;
    private static final int MODULE_ROW = 36;
    private static final int SETTING_ROW = 34;
    private static final int CONFIG_ROW = 40;
    private static final int NEW_CONFIG_ROW = 40;

    private static final int WHITE = 0xFFFFFFFF;
    private static final int LINE = 0xFFE5E5EA;
    private static final int HOVER = 0xFFF2F2F7;
    private static final int TEXT = 0xFF1C1C1E;
    private static final int TEXT_MUTED = 0xFF8E8E93;
    private static final int ON = 0xFF34C759;
    private static final int OFF = 0xFFD1D1D6;
    private static final int ACCENT = 0xFFFF9500;
    private static final int DANGER = 0xFFE5342B;

    private static int tab = 0;

    private record Hit(int x1, int y1, int x2, int y2, boolean clipped,
                       Runnable left, Runnable right, NumberSetting slider) {
        boolean contains(double x, double y) {
            return x >= x1 && x < x2 && y >= y1 && y < y2;
        }
    }

    private final List<Hit> hits = new ArrayList<>();
    private final Set<Module> expanded = new HashSet<>();
    private List<String> configs = new ArrayList<>();
    private String status = "Prawy Shift zamyka menu, prawy klik rozwija ustawienia";

    private NumberSetting dragging;
    private int dragX1;
    private int dragX2;
    private int scroll;
    private int maxScroll;
    private int clipTop;
    private int clipBottom;

    public ClickGuiScreen() {
        super(Text.literal("SPcheats"));
    }

    @Override
    protected void init() {
        configs = ConfigManager.list();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private int modulesContentHeight() {
        int total = 0;
        String lastCategory = null;
        for (Module module : ModuleManager.MODULES) {
            if (!module.category.equals(lastCategory)) {
                total += CATEGORY_ROW;
                lastCategory = module.category;
            }
            total += MODULE_ROW;
            if (expanded.contains(module)) {
                total += module.settings.size() * SETTING_ROW;
            }
        }
        return total;
    }

    private int configsContentHeight() {
        int total = NEW_CONFIG_ROW + configs.size() * CONFIG_ROW;
        if (configs.isEmpty()) {
            total += 30;
        }
        return total;
    }

    private void button(DrawContext context, int x, int y, int w, int h, String label, int background, int foreground) {
        context.fill(x, y, x + w, y + h, background);
        int textX = x + (w - textRenderer.getWidth(label)) / 2;
        context.drawText(textRenderer, label, textX, y + (h - 8) / 2, foreground, false);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        hits.clear();
        context.fill(0, 0, width, height, 0x33000000);

        int panelHeight = Math.min(420, height - 48);
        int px = (width - PANEL_WIDTH) / 2;
        int py = 24;

        context.fill(px - 1, py - 1, px + PANEL_WIDTH + 1, py + panelHeight + 1, LINE);
        context.fill(px, py, px + PANEL_WIDTH, py + panelHeight, WHITE);

        context.drawText(textRenderer, Text.literal("SPcheats").formatted(Formatting.BOLD), px + 16, py + 10, TEXT, false);
        context.drawText(textRenderer, status, px + 16, py + 24, TEXT_MUTED, false);

        String[] tabNames = {"Modules", "Configs"};
        for (int i = 0; i < tabNames.length; i++) {
            int tabX = px + 16 + i * 86;
            int tabY = py + 38;
            boolean selected = tab == i;
            button(context, tabX, tabY, 80, 20, tabNames[i], selected ? TEXT : HOVER, selected ? WHITE : TEXT);
            final int index = i;
            hits.add(new Hit(tabX, tabY, tabX + 80, tabY + 20, false, () -> {
                tab = index;
                scroll = 0;
                configs = ConfigManager.list();
            }, null, null));
        }
        context.fill(px, py + TOP - 1, px + PANEL_WIDTH, py + TOP, LINE);

        clipTop = py + TOP;
        clipBottom = py + panelHeight;
        int visible = clipBottom - clipTop;
        int content = tab == 0 ? modulesContentHeight() : configsContentHeight();
        maxScroll = Math.max(0, content - visible + 8);
        scroll = Math.max(0, Math.min(scroll, maxScroll));

        context.enableScissor(px, clipTop, px + PANEL_WIDTH, clipBottom);
        if (tab == 0) {
            renderModules(context, px, mouseX, mouseY);
        } else {
            renderConfigs(context, px);
        }
        context.disableScissor();
    }

    private void renderModules(DrawContext context, int px, int mouseX, int mouseY) {
        int y = clipTop - scroll;
        String lastCategory = null;
        for (Module module : ModuleManager.MODULES) {
            if (!module.category.equals(lastCategory)) {
                context.drawText(textRenderer, module.category.toUpperCase(), px + 16, y + 8, TEXT_MUTED, false);
                lastCategory = module.category;
                y += CATEGORY_ROW;
            }

            boolean hovered = mouseY >= clipTop && mouseY < clipBottom
                    && mouseX >= px && mouseX < px + PANEL_WIDTH
                    && mouseY >= y && mouseY < y + MODULE_ROW;
            if (hovered) {
                context.fill(px, y, px + PANEL_WIDTH, y + MODULE_ROW, HOVER);
            }
            context.drawText(textRenderer, module.name, px + 16, y + 8, TEXT, false);
            context.drawText(textRenderer, module.description, px + 16, y + 21, TEXT_MUTED, false);
            drawSwitch(context, px + PANEL_WIDTH - 16 - 30, y + 11, module.isEnabled());

            hits.add(new Hit(px, y, px + PANEL_WIDTH, y + MODULE_ROW, true,
                    module::toggle,
                    () -> {
                        if (!expanded.remove(module)) {
                            expanded.add(module);
                        }
                    }, null));
            y += MODULE_ROW;

            if (expanded.contains(module)) {
                for (NumberSetting setting : module.settings) {
                    int trackX1 = px + 16;
                    int trackX2 = px + PANEL_WIDTH - 16;
                    context.drawText(textRenderer, setting.name, trackX1, y + 4, TEXT, false);

                    if (setting.bool) {
                        drawSwitch(context, trackX2 - 30, y + 4, setting.isOn());
                        hits.add(new Hit(px, y, px + PANEL_WIDTH, y + SETTING_ROW, true,
                                setting::toggle, null, null));
                    } else {
                        String value = setting.display();
                        context.drawText(textRenderer, value, trackX2 - textRenderer.getWidth(value), y + 4, TEXT_MUTED, false);
                        context.fill(trackX1, y + 20, trackX2, y + 24, OFF);
                        int fillX = trackX1 + (int) ((trackX2 - trackX1) * setting.ratio());
                        context.fill(trackX1, y + 20, fillX, y + 24, ACCENT);
                        context.fill(fillX - 4, y + 17, fillX + 4, y + 27, WHITE);
                        context.fill(fillX - 4, y + 17, fillX + 4, y + 18, LINE);
                        context.fill(fillX - 4, y + 26, fillX + 4, y + 27, LINE);
                        context.fill(fillX - 4, y + 17, fillX - 3, y + 27, LINE);
                        context.fill(fillX + 3, y + 17, fillX + 4, y + 27, LINE);
                        hits.add(new Hit(trackX1, y, trackX2, y + SETTING_ROW, true, null, null, setting));
                    }
                    y += SETTING_ROW;
                }
            }
            context.fill(px, y - 1, px + PANEL_WIDTH, y, LINE);
        }
    }

    private void drawSwitch(DrawContext context, int x, int y, boolean on) {
        context.fill(x, y, x + 30, y + 14, on ? ON : OFF);
        int knobX = on ? x + 30 - 12 : x + 2;
        context.fill(knobX, y + 2, knobX + 10, y + 12, WHITE);
    }

    private void renderConfigs(DrawContext context, int px) {
        int y = clipTop - scroll;

        int newX = px + 16;
        int newW = PANEL_WIDTH - 32;
        button(context, newX, y + 8, newW, 24, "+ Nowy config (zapisuje obecne ustawienia)", TEXT, WHITE);
        hits.add(new Hit(newX, y + 8, newX + newW, y + 32, true, () -> {
            String name = ConfigManager.createNew();
            configs = ConfigManager.list();
            status = "Zapisano " + name;
        }, null, null));
        y += NEW_CONFIG_ROW;

        if (configs.isEmpty()) {
            context.drawText(textRenderer, "Brak configów.", px + 16, y + 8, TEXT_MUTED, false);
            return;
        }

        for (String name : configs) {
            context.drawText(textRenderer, name, px + 16, y + 15, TEXT, false);

            int deleteX = px + PANEL_WIDTH - 16 - 54;
            int saveX = deleteX - 58;
            int loadX = saveX - 58;
            int buttonY = y + 10;

            button(context, loadX, buttonY, 54, 20, "Wczytaj", HOVER, TEXT);
            button(context, saveX, buttonY, 54, 20, "Zapisz", HOVER, TEXT);
            button(context, deleteX, buttonY, 54, 20, "Usuń", DANGER, WHITE);

            hits.add(new Hit(loadX, buttonY, loadX + 54, buttonY + 20, true, () -> {
                status = ConfigManager.load(name) ? "Wczytano " + name : "Nie udało się wczytać " + name;
            }, null, null));
            hits.add(new Hit(saveX, buttonY, saveX + 54, buttonY + 20, true, () -> {
                status = ConfigManager.save(name) ? "Zapisano " + name : "Nie udało się zapisać " + name;
            }, null, null));
            hits.add(new Hit(deleteX, buttonY, deleteX + 54, buttonY + 20, true, () -> {
                ConfigManager.delete(name);
                configs = ConfigManager.list();
                status = "Usunięto " + name;
            }, null, null));

            y += CONFIG_ROW;
            context.fill(px, y - 1, px + PANEL_WIDTH, y, LINE);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Hit hit : hits) {
            if (!hit.contains(mouseX, mouseY)) {
                continue;
            }
            if (hit.clipped() && (mouseY < clipTop || mouseY >= clipBottom)) {
                continue;
            }
            if (hit.slider() != null) {
                if (button == 0) {
                    dragging = hit.slider();
                    dragX1 = hit.x1();
                    dragX2 = hit.x2();
                    updateSlider(mouseX);
                }
                return true;
            }
            if (button == 0 && hit.left() != null) {
                hit.left().run();
                return true;
            }
            if (button == 1 && hit.right() != null) {
                hit.right().run();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging != null) {
            updateSlider(mouseX);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scroll = Math.max(0, Math.min(maxScroll, scroll - (int) (verticalAmount * 20)));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void updateSlider(double mouseX) {
        if (dragging != null) {
            dragging.setFromRatio((mouseX - dragX1) / (double) (dragX2 - dragX1));
        }
    }
}
