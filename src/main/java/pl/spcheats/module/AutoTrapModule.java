package pl.spcheats.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import pl.spcheats.util.BlockPlacer;
import pl.spcheats.util.PlayerTargets;

import java.util.ArrayList;
import java.util.List;

public class AutoTrapModule extends Module {
    public static final AutoTrapModule INSTANCE = new AutoTrapModule();

    private static final Direction[] SIDES = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    private final NumberSetting range = new NumberSetting("Zasięg", 2, 6, 5, 1, " m");
    private final NumberSetting perTick = new NumberSetting("Bloków na tick", 1, 4, 2, 1, "");

    private AutoTrapModule() {
        super("Auto Trap", "Combat", "Obsydian wokół najbliższego gracza", false);
        settings.add(range);
        settings.add(perTick);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.world == null || mc.player == null) {
            return;
        }
        PlayerEntity target = PlayerTargets.closest(mc, range.value);
        if (target == null) {
            return;
        }
        int slot = BlockPlacer.findHotbar(mc, Items.OBSIDIAN);
        if (slot < 0) {
            return;
        }

        BlockPos base = target.getBlockPos();
        List<BlockPos> positions = new ArrayList<>();
        for (Direction side : SIDES) {
            positions.add(base.offset(side));
        }
        for (Direction side : SIDES) {
            positions.add(base.offset(side).up());
        }
        positions.add(base.up(2));

        int placed = 0;
        for (BlockPos pos : positions) {
            if (placed >= perTick.value) {
                break;
            }
            if (BlockPlacer.placeAt(mc, pos, slot, 4.5)) {
                placed++;
            }
        }
    }
}
