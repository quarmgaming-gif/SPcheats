package pl.spcheats.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import pl.spcheats.util.BlockPlacer;
import pl.spcheats.util.PlayerTargets;

public class AutoWebModule extends Module {
    public static final AutoWebModule INSTANCE = new AutoWebModule();

    private final NumberSetting range = new NumberSetting("Zasięg", 2, 6, 5, 1, " m");

    private AutoWebModule() {
        super("Auto Web", "Combat", "Pajęczyny na nogach i głowie gracza", false);
        settings.add(range);
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
        int slot = BlockPlacer.findHotbar(mc, Items.COBWEB);
        if (slot < 0) {
            return;
        }
        BlockPos feet = target.getBlockPos();
        if (!BlockPlacer.placeAt(mc, feet, slot, 4.5)) {
            BlockPlacer.placeAt(mc, feet.up(), slot, 4.5);
        }
    }
}
