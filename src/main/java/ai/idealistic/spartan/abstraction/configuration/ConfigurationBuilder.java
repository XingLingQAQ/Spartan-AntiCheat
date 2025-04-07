package ai.idealistic.spartan.abstraction.configuration;

import ai.idealistic.spartan.Register;
import ai.idealistic.spartan.utils.minecraft.server.ConfigUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ConfigurationBuilder {

    public static String getDirectory(String fileName) {
        return Register.plugin.getDataFolder() + "/" + fileName + ".yml";
    }

    protected static final String prefix = "{prefix}";

    // Separator

    protected final File file;
    private final Map<String, Boolean>
            bool = new ConcurrentHashMap<>(),
            exists = new ConcurrentHashMap<>();
    private final Map<String, Integer> ints = new ConcurrentHashMap<>();
    private final Map<String, Double> dbls = new ConcurrentHashMap<>();
    private final Map<String, String> str = new ConcurrentHashMap<>();

    public ConfigurationBuilder(String fileName) {
        this.file = new File(getDirectory(fileName));
    }

    protected final YamlConfiguration getPath() {
        if (!file.exists()) {
            create();
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    protected final void internalClear() {
        bool.clear();
        exists.clear();
        ints.clear();
        str.clear();
    }

    public final File getFile() {
        return file;
    }

    public final boolean exists(String path) {
        Boolean data = exists.get(path);

        if (data != null) {
            return data;
        }
        boolean result = getPath().contains(path);
        exists.put(path, result);
        return result;
    }

    public final boolean getBoolean(String path) {
        Boolean data = bool.get(path);

        if (data != null) {
            return data;
        }
        boolean value = getPath().getBoolean(path);
        bool.put(path, value);
        return value;
    }

    public final int getInteger(String path) {
        Integer data = ints.get(path);

        if (data != null) {
            return data;
        }
        int value = getPath().getInt(path);
        ints.put(path, value);
        return value;
    }

    public final double getDouble(String path) {
        Double data = dbls.get(path);

        if (data != null) {
            return data;
        }
        double value = getPath().getDouble(path);
        dbls.put(path, value);
        return value;
    }

    public final String getString(String path) {
        String data = str.get(path);

        if (data != null) {
            return data;
        }
        String value = getPath().getString(path);

        if (value == null) {
            return path;
        }
        str.put(path, value);
        return value;
    }

    public final String getColorfulString(String path) {
        String data = str.get(path);

        if (data != null) {
            return data;
        }
        if (!file.exists()) {
            create();
        }
        String value = getPath().getString(path);

        if (value == null) {
            return path;
        } else {
            value = ChatColor.translateAlternateColorCodes('&', value);
            value = value.replace(prefix, Register.pluginName);
        }
        str.put(path, value);
        return value;
    }

    public final void clearOption(String name) {
        bool.remove(name);
        exists.remove(name);
        ints.remove(name);
        dbls.remove(name);
        str.remove(name);
    }

    public final void setOption(String name, Object value) {
        ConfigUtils.set(file, name, value);
        clearOption(name);
    }

    protected final void addOption(String name, Object value) {
        ConfigUtils.add(file, name, value);
        clearOption(name);
    }

    public final String getOldOption(String old, String current) {
        return exists(old) ? old : current;
    }

    public void clear() {
        internalClear();
    }

    abstract public void create();
}
