package pl.spcheats.module;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import pl.spcheats.util.AlertHud;

public class TrapAlertModule extends Module {
    public static final TrapAlertModule INSTANCE = new TrapAlertModule();

    private static final Direction[] SIDES = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    private final NumberSetting threshold = new NumberSetting("Min. bloków wokół", 2, 9, 5, 1, "");

    private BlockPos lastPos;
    private int lastCount;
    private long lastAlert;

    private TrapAlertModule() {
        super("Trap Alert", "Wizualne", "Wiadomość, gdy ktoś Cię zamyka", true);
        settings.add(threshold);
    }

    @Override
    public void onDisable() {
        lastPos = null;
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.world == null || mc.player == null) {
            lastPos = null;
            return;
        }

        BlockPos base = mc.player.getBlockPos();
        int count = 0;
        for (Direction side : SIDES) {
            BlockPos pos = base.offset(side);
            if (blocked(mc, pos)) {
                count++;
            }
            if (blocked(mc, pos.up())) {
                count++;
            }
        }
        if (blocked(mc, base.up(2))) {
            count++;
        }

        if (lastPos == null || !lastPos.equals(base)) {
            lastPos = base;
            lastCount = count;
            return;
        }

        long now = System.currentTimeMillis();
        if (count >= threshold.value && count > lastCount && now - lastAlert > 3000) {
            AlertHud.push("Ktoś Cię zamyka! (" + count + "/9 bloków)", 5000, 0xFFE5342B);
            mc.getSoundManager().play(
                    PositionedSoundInstance.master(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 0.8f));
            lastAlert = now;
        }
        lastCount = count;
    }

    private boolean blocked(MinecraftClient mc, BlockPos pos) {
        BlockState state = mc.world.getBlockState(pos);
        return state.isOf(Blocks.COBWEB) || !state.getCollisionShape(mc.world, pos).isEmpty();
    }
}
