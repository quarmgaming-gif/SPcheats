package pl.spcheats.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;
import pl.spcheats.module.Module;
import pl.spcheats.module.ModuleManager;
import pl.spcheats.module.NumberSetting;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClickGuiScreen extends Screen {
    private static final int PANEL_WIDTH = 320;
    private static final int HEADER = 40;
    private static final int MODULE_ROW = 36;
    private static final int SETTING_ROW = 34;

    private static final int WHITE = 0xFFFFFFFF;
    private static final int LINE = 0xFFE5E5EA;
    private static final int HOVER = 0xFFF2F2F7;
    private static final int TEXT = 0xFF1C1C1E;
    private static final int TEXT_MUTED = 0xFF8E8E93;
    private static final int ON = 0xFF34C759;
    private static final int OFF = 0xFFD1D1D6;
    private static final int ACCENT = 0xFFFF9500;

    private record Hit(int x1, int y1, int x2, int y2, Module module, NumberSetting setting) {
        boolean contains(double x, double y) {
            return x >= x1 && x < x2 && y >= y1 && y < y2;
        }
    }

    private final List<Hit> hits = new ArrayList<>();
    private final Set<Module> expanded = new HashSet<>();

    private NumberSetting dragging;
    private int dragX1;
    private int dragX2;
    private int scroll;
    private int clipTop;
    private int clipBottom;

    public ClickGuiScreen() {
        super(Text.literal("SPcheats"));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        hits.clear();

        context.fill(0, 0, width, height, 0x33000000);

        int content = 0;
        for (Module module : ModuleManager.MODULES) {
            content += MODULE_ROW;
            if (expanded.contains(module)) {
                content += module.settings.size() * SETTING_ROW;
            }
        }

        int panelHeight = Math.min(HEADER + content + 8, height - 60);
        int px = (width - PANEL_WIDTH) / 2;
        int py = 30;

        int visible = panelHeight - HEADER;
        int maxScroll = Math.max(0, content - visible + 8);
        scroll = Math.max(0, Math.min(scroll, maxScroll));

        context.fill(px - 1, py - 1, px + PANEL_WIDTH + 1, py + panelHeight + 1, LINE);
        context.fill(px, py, px + PANEL_WIDTH, py + panelHeight, WHITE);

        context.drawText(textRenderer, Text.literal("SPcheats").formatted(Formatting.BOLD), px + 16, py + 10, TEXT, false);
        context.drawText(textRenderer, "Prawy Shift zamyka menu, prawy klik rozwija ustawienia", px + 16, py + 24, TEXT_MUTED, false);
        context.fill(px, py + HEADER - 1, px + PANEL_WIDTH, py + HEADER, LINE);

        clipTop = py + HEADER;
        clipBottom = py + panelHeight;
        context.enableScissor(px, clipTop, px + PANEL_WIDTH, clipBottom);

        int y = clipTop - scroll;
        for (Module module : ModuleManager.MODULES) {
            boolean hovered = mouseY >= clipTop && mouseY < clipBottom
                    && mouseX >= px && mouseX < px + PANEL_WIDTH
                    && mouseY >= y && mouseY < y + MODULE_ROW;
            if (hovered) {
                context.fill(px, y, px + PANEL_WIDTH, y + MODULE_ROW, HOVER);
            }
            context.drawText(textRenderer, module.name, px + 16, y + 8, TEXT, false);
            context.drawText(textRenderer, module.description, px + 16, y + 21, TEXT_MUTED, false);

            int switchX = px + PANEL_WIDTH - 16 - 30;
            context.fill(switchX, y + 11, switchX + 30, y + 25, module.isEnabled() ? ON : OFF);
            int knobX = module.isEnabled() ? switchX + 30 - 12 : switchX + 2;
            context.fill(knobX, y + 13, knobX + 10, y + 23, WHITE);

            hits.add(new Hit(px, y, px + PANEL_WIDTH, y + MODULE_ROW, module, null));
            y += MODULE_ROW;

            if (expanded.contains(module)) {
                for (NumberSetting setting : module.settings) {
                    int trackX1 = px + 16;
                    int trackX2 = px + PANEL_WIDTH - 16;

                    context.drawText(textRenderer, setting.name, px + 16, y + 4, TEXT, false);
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

                    hits.add(new Hit(trackX1, y, trackX2, y + SETTING_ROW, module, setting));
                    y += SETTING_ROW;
                }
            }
            context.fill(px, y - 1, px + PANEL_WIDTH, y, LINE);
        }

        context.disableScissor();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseY >= clipTop && mouseY < clipBottom) {
            for (Hit hit : hits) {
                if (!hit.contains(mouseX, mouseY)) {
                    continue;
                }
                if (hit.setting() != null) {
                    if (button == 0) {
                        dragging = hit.setting();
                        dragX1 = hit.x1();
                        dragX2 = hit.x2();
                        updateSlider(mouseX);
                    }
                    return true;
                }
                if (button == 0) {
                    hit.module().toggle();
                } else if (button == 1) {
                    if (!expanded.remove(hit.module())) {
                        expanded.add(hit.module());
                    }
                }
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
        scroll -= (int) (verticalAmount * 20);
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
