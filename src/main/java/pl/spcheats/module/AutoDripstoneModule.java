package pl.spcheats.module;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import pl.spcheats.util.BlockPlacer;
import pl.spcheats.util.PlayerTargets;

public class AutoDripstoneModule extends Module {
    public static final AutoDripstoneModule INSTANCE = new AutoDripstoneModule();

    private final NumberSetting range = new NumberSetting("Zasięg", 2, 6, 5, 1, " m");
    private final NumberSetting height = new NumberSetting("Max wysokość", 3, 8, 6, 1, " m");
    private final NumberSetting breakCeiling = NumberSetting.bool("Zrzut (łam blok)", true);

    private AutoDripstoneModule() {
        super("Auto Dripstone", "Combat", "Zrzuca stalaktyt na gracza", false);
        settings.add(range);
        settings.add(height);
        settings.add(breakCeiling);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.world == null || mc.player == null || mc.interactionManager == null) {
            return;
        }
        PlayerEntity target = PlayerTargets.closest(mc, range.value);
        if (target == null) {
            return;
        }

        BlockPos start = target.getBlockPos().up(2);
        BlockPos ceiling = null;
        for (int i = 0; i <= height.value; i++) {
            BlockPos pos = start.up(i);
            BlockState state = mc.world.getBlockState(pos);
            if (state.isReplaceable()) {
                continue;
            }
            if (i > 0 && !state.getCollisionShape(mc.world, pos).isEmpty()) {
                ceiling = pos;
            }
            break;
        }
        if (ceiling == null) {
            return;
        }

        BlockPos hanging = ceiling.down();
        Vec3d eyes = mc.player.getEyePos();
        double distance = eyes.distanceTo(Vec3d.ofCenter(ceiling));

        if (mc.world.getBlockState(hanging).isOf(Blocks.POINTED_DRIPSTONE)) {
            if (breakCeiling.isOn() && distance <= 4.5) {
                mc.interactionManager.updateBlockBreakingProgress(ceiling, Direction.DOWN);
                mc.player.swingHand(Hand.MAIN_HAND);
            }
            return;
        }

        int slot = BlockPlacer.findHotbar(mc, Items.POINTED_DRIPSTONE);
        if (slot < 0 || !mc.world.getBlockState(hanging).isReplaceable()) {
            return;
        }
        BlockPlacer.placeAgainst(mc, ceiling, Direction.DOWN, slot, 4.5);
    }
}
