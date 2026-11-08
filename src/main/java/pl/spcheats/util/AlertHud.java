package pl.spcheats.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public final class AlertHud {
    private static final int MAX_ALERTS = 4;
    private static final List<Alert> ALERTS = new ArrayList<>();

    private record Alert(String text, long expiresAt, int accent) {
    }

    private AlertHud() {
    }

    public static void push(String text, long durationMs, int accent) {
        ALERTS.add(new Alert(text, System.currentTimeMillis() + durationMs, accent));
        while (ALERTS.size() > MAX_ALERTS) {
            ALERTS.remove(0);
        }
    }

    public static void clear() {
        ALERTS.clear();
    }

    public static void render(DrawContext context) {
        if (ALERTS.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        ALERTS.removeIf(alert -> alert.expiresAt() < now);

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int y = 8;
        for (Alert alert : ALERTS) {
            int width = textRenderer.getWidth(alert.text()) + 24;
            int x = (context.getScaledWindowWidth() - width) / 2;
            context.fill(x - 1, y - 1, x + width + 1, y + 21, 0xFFE5E5EA);
            context.fill(x, y, x + width, y + 20, 0xFFFFFFFF);
            context.fill(x, y, x + 3, y + 20, alert.accent());
            context.drawText(textRenderer, alert.text(), x + 12, y + 6, 0xFF1C1C1E, false);
            y += 26;
        }
    }
}
