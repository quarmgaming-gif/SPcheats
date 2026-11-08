package pl.spcheats.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import pl.spcheats.util.PlayerTargets;

public class KillAuraModule extends Module {
    public static final KillAuraModule INSTANCE = new KillAuraModule();

    private final NumberSetting range = new NumberSetting("Zasięg", 3, 6, 4, 0.5, " m");
    private final NumberSetting fullCharge = NumberSetting.bool("Tylko pełny atak", true);

    private KillAuraModule() {
        super("Kill Aura", "Combat", "Auto-atak najbliższego gracza", false);
        settings.add(range);
        settings.add(fullCharge);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.world == null || mc.player == null || mc.interactionManager == null || mc.currentScreen != null) {
            return;
        }
        PlayerEntity target = PlayerTargets.closest(mc, range.value);
        if (target == null) {
            return;
        }

        Vec3d eyes = mc.player.getEyePos();
        Vec3d aim = target.getBoundingBox().getCenter();
        double dx = aim.x - eyes.x;
        double dy = aim.y - eyes.y;
        double dz = aim.z - eyes.z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, horizontal));
        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);

        if (fullCharge.isOn() && mc.player.getAttackCooldownProgress(0.5f) < 0.95f) {
            return;
        }
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}
