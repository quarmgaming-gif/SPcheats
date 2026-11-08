package pl.spcheats.util;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class BlockPlacer {
    private BlockPlacer() {
    }

    public static int findHotbar(MinecraftClient mc, Item item) {
        if (mc.player == null) {
            return -1;
        }
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(item)) {
                return i;
            }
        }
        return -1;
    }

    public static boolean placeAt(MinecraftClient mc, BlockPos pos, int slot, double reach) {
        if (mc.world == null || mc.player == null || mc.interactionManager == null || slot < 0) {
            return false;
        }
        BlockState state = mc.world.getBlockState(pos);
        if (!state.isReplaceable()) {
            return false;
        }
        Vec3d eyes = mc.player.getEyePos();
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = pos.offset(direction);
            BlockState neighborState = mc.world.getBlockState(neighbor);
            if (neighborState.isAir() || neighborState.isReplaceable()) {
                continue;
            }
            Direction face = direction.getOpposite();
            if (clickFace(mc, neighbor, face, slot, reach, eyes)) {
                return true;
            }
        }
        return false;
    }

    public static boolean placeAgainst(MinecraftClient mc, BlockPos support, Direction face, int slot, double reach) {
        if (mc.world == null || mc.player == null || mc.interactionManager == null || slot < 0) {
            return false;
        }
        return clickFace(mc, support, face, slot, reach, mc.player.getEyePos());
    }

    private static boolean clickFace(MinecraftClient mc, BlockPos support, Direction face, int slot, double reach, Vec3d eyes) {
        Vec3d hit = Vec3d.ofCenter(support).add(face.getOffsetX() * 0.5, face.getOffsetY() * 0.5, face.getOffsetZ() * 0.5);
        if (eyes.distanceTo(hit) > reach) {
            return false;
        }
        int previous = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = slot;
        BlockHitResult result = new BlockHitResult(hit, face, support, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, result);
        mc.player.swingHand(Hand.MAIN_HAND);
        mc.player.getInventory().selectedSlot = previous;
        return true;
    }
}
