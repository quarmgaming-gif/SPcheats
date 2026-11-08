package pl.spcheats.module;

import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    public static final List<Module> MODULES = new ArrayList<>();

    public static void init() {
        MODULES.add(ElytraAlertModule.INSTANCE);
    }

    public static void tick(MinecraftClient client) {
        for (Module module : MODULES) {
            if (module.isEnabled()) {
                module.onTick(client);
            }
        }
    }
}
