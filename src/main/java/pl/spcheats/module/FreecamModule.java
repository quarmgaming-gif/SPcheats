package pl.spcheats.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.input.Input;

public class FreecamModule extends Module {
    public static final FreecamModule INSTANCE = new FreecamModule();

    private final NumberSetting speed = new NumberSetting("Prędkość", 1, 10, 3, 1, "");

    private OtherClientPlayerEntity camera;
    private Input savedInput;

    private FreecamModule() {
        super("Freecam", "Wizualne", "Wolna kamera bez ruszania postaci", false);
        settings.add(speed);
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.setCameraEntity(mc.player);
            if (savedInput != null) {
                mc.player.input = savedInput;
            }
        }
        camera = null;
        savedInput = null;
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.world == null || mc.player == null) {
            camera = null;
            savedInput = null;
            return;
        }

        if (camera == null) {
            camera = new OtherClientPlayerEntity(mc.world, mc.player.getGameProfile());
            camera.refreshPositionAndAngles(mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                    mc.player.getYaw(), mc.player.getPitch());
            savedInput = mc.player.input;
            mc.player.input = new Input();
            mc.setCameraEntity(camera);
        }

        double rad = Math.toRadians(mc.player.getYaw());
        double forwardX = -Math.sin(rad);
        double forwardZ = Math.cos(rad);

        double moveX = 0;
        double moveZ = 0;
        double moveY = 0;
        if (mc.options.forwardKey.isPressed()) {
            moveX += forwardX;
            moveZ += forwardZ;
        }
        if (mc.options.backKey.isPressed()) {
            moveX -= forwardX;
            moveZ -= forwardZ;
        }
        if (mc.options.leftKey.isPressed()) {
            moveX += forwardZ;
            moveZ -= forwardX;
        }
        if (mc.options.rightKey.isPressed()) {
            moveX -= forwardZ;
            moveZ += forwardX;
        }
        if (mc.options.jumpKey.isPressed()) {
            moveY += 1;
        }
        if (mc.options.sneakKey.isPressed()) {
            moveY -= 1;
        }

        double length = Math.sqrt(moveX * moveX + moveZ * moveZ);
        if (length > 0) {
            moveX /= length;
            moveZ /= length;
        }
        double step = speed.value * 0.1;
        camera.refreshPositionAndAngles(
                camera.getX() + moveX * step,
                camera.getY() + moveY * step,
                camera.getZ() + moveZ * step,
                mc.player.getYaw(),
                mc.player.getPitch());
    }
}
