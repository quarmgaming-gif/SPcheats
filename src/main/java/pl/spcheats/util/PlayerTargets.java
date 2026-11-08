package pl.spcheats.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

public final class PlayerTargets {
    private PlayerTargets() {
    }

    public static PlayerEntity closest(MinecraftClient mc, double range) {
        if (mc.world == null || mc.player == null) {
            return null;
        }
        PlayerEntity best = null;
        double bestDistance = range * range;
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player || !player.isAlive() || player.isSpectator()) {
                continue;
            }
            double distance = mc.player.squaredDistanceTo(player);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = player;
            }
        }
        return best;
    }
}
