package pl.spcheats.config;

import net.fabricmc.loader.api.FabricLoader;
import pl.spcheats.module.Module;
import pl.spcheats.module.ModuleManager;
import pl.spcheats.module.NumberSetting;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ConfigManager {
    private static final String EXTENSION = ".properties";

    private ConfigManager() {
    }

    private static Path dir() {
        return FabricLoader.getInstance().getConfigDir().resolve("spcheats");
    }

    public static List<String> list() {
        try {
            Files.createDirectories(dir());
            try (Stream<Path> stream = Files.list(dir())) {
                return stream
                        .map(path -> path.getFileName().toString())
                        .filter(name -> name.endsWith(EXTENSION))
                        .map(name -> name.substring(0, name.length() - EXTENSION.length()))
                        .sorted()
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public static String createNew() {
        List<String> existing = list();
        int index = 1;
        while (existing.contains("config" + index)) {
            index++;
        }
        String name = "config" + index;
        save(name);
        return name;
    }

    public static boolean save(String name) {
        Properties properties = new Properties();
        for (Module module : ModuleManager.MODULES) {
            properties.setProperty(module.name + ".enabled", String.valueOf(module.isEnabled()));
            for (NumberSetting setting : module.settings) {
                properties.setProperty(module.name + "." + setting.name, String.valueOf(setting.value));
            }
        }
        try {
            Files.createDirectories(dir());
            try (Writer writer = Files.newBufferedWriter(dir().resolve(name + EXTENSION), StandardCharsets.UTF_8)) {
                properties.store(writer, "SPcheats config");
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean load(String name) {
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(dir().resolve(name + EXTENSION), StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException e) {
            return false;
        }
        for (Module module : ModuleManager.MODULES) {
            for (NumberSetting setting : module.settings) {
                String value = properties.getProperty(module.name + "." + setting.name);
                if (value != null) {
                    try {
                        setting.setClamped(Double.parseDouble(value));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            String enabled = properties.getProperty(module.name + ".enabled");
            if (enabled != null) {
                module.setEnabled(Boolean.parseBoolean(enabled));
            }
        }
        return true;
    }

    public static boolean delete(String name) {
        try {
            return Files.deleteIfExists(dir().resolve(name + EXTENSION));
        } catch (IOException e) {
            return false;
        }
    }
}
