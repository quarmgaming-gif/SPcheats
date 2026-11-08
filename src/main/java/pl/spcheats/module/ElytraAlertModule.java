package pl.spcheats.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ElytraAlertModule extends Module {
    public static final ElytraAlertModule INSTANCE = new ElytraAlertModule();

    private static final int MAX_ALERTS = 4;

    private final NumberSetting range = new NumberSetting("Zasięg", 16, 300, 200, 8, " m");
    private final NumberSetting duration = new NumberSetting("Czas wiadomości", 1, 10, 4, 1, " s");

    private final Set<UUID> gliding = new HashSet<>();
    private final List<Alert> alerts = new ArrayList<>();

    private record Alert(String text, long expiresAt) {
    }

    private ElytraAlertModule() {
        super("Elytra Alert", "Wiadomość na górze ekranu, gdy ktoś leci na elytrze", true);
        settings.add(range);
        settings.add(duration);
    }

    @Override
    public void onDisable() {
        gliding.clear();
        alerts.clear();
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.world == null || mc.player == null) {
            gliding.clear();
            alerts.clear();
            return;
        }

        Set<UUID> now = new HashSet<>();
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player || !player.isGliding()) {
                continue;
            }
            float distance = mc.player.distanceTo(player);
            if (distance > range.value) {
                continue;
            }
            UUID id = player.getUuid();
            now.add(id);
            if (!gliding.contains(id)) {
                String text = player.getName().getString() + " leci na elytrze (" + Math.round(distance) + " m)";
                alerts.add(new Alert(text, System.currentTimeMillis() + (long) (duration.value * 1000)));
                while (alerts.size() > MAX_ALERTS) {
                    alerts.remove(0);
                }
                mc.getSoundManager().play(
                        PositionedSoundInstance.master(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.6f));
            }
        }
        gliding.clear();
        gliding.addAll(now);
    }

    public void renderHud(DrawContext context) {
        if (!isEnabled() || alerts.isEmpty()) {
            return;
        }
        long time = System.currentTimeMillis();
        alerts.removeIf(alert -> alert.expiresAt() < time);

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int y = 8;
        for (Alert alert : alerts) {
            int width = textRenderer.getWidth(alert.text()) + 24;
            int x = (context.getScaledWindowWidth() - width) / 2;
            context.fill(x - 1, y - 1, x + width + 1, y + 21, 0xFFE5E5EA);
            context.fill(x, y, x + width, y + 20, 0xFFFFFFFF);
            context.fill(x, y, x + 3, y + 20, 0xFFFF9500);
            context.drawText(textRenderer, alert.text(), x + 12, y + 6, 0xFF1C1C1E, false);
            y += 26;
        }
    }
}
