package pl.spcheats.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import pl.spcheats.util.AlertHud;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ElytraAlertModule extends Module {
    public static final ElytraAlertModule INSTANCE = new ElytraAlertModule();

    private final NumberSetting range = new NumberSetting("Zasięg", 16, 300, 200, 8, " m");
    private final NumberSetting duration = new NumberSetting("Czas wiadomości", 1, 10, 4, 1, " s");

    private final Set<UUID> gliding = new HashSet<>();

    private ElytraAlertModule() {
        super("Elytra Alert", "Wizualne", "Wiadomość, gdy ktoś leci na elytrze", true);
        settings.add(range);
        settings.add(duration);
    }

    @Override
    public void onDisable() {
        gliding.clear();
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.world == null || mc.player == null) {
            gliding.clear();
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
                AlertHud.push(text, (long) (duration.value * 1000), 0xFFFF9500);
                mc.getSoundManager().play(
                        PositionedSoundInstance.master(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.6f));
            }
        }
        gliding.clear();
        gliding.addAll(now);
    }
}
