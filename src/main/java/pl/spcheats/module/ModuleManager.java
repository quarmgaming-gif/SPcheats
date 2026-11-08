package pl.spcheats.module;

import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    public static final List<Module> MODULES = new ArrayList<>();

    public static void init() {
        MODULES.add(KillAuraModule.INSTANCE);
        MODULES.add(AutoTrapModule.INSTANCE);
        MODULES.add(AutoWebModule.INSTANCE);
        MODULES.add(AutoDripstoneModule.INSTANCE);
        MODULES.add(FreecamModule.INSTANCE);
        MODULES.add(ElytraAlertModule.INSTANCE);
        MODULES.add(TrapAlertModule.INSTANCE);
    }

    public static void tick(MinecraftClient client) {
        for (Module module : MODULES) {
            if (module.isEnabled()) {
                module.onTick(client);
            }
        }
    }
}
