package ai.idealistic.spartan.functionality.connection;

import ai.idealistic.spartan.utils.math.AlgebraUtils;

import java.util.Objects;

public class IDs {

    public static final String
            user = "%%__USER__%%",
            file = "%%__NONCE__%%",
            resource = "%%__RESOURCE__%%";
    public static final boolean
            enabled = AlgebraUtils.validInteger(resource),
            builtByBit = "%%__BUILTBYBIT__%%".length() != 18,
            polymart = "%%__POLYMART__%%".length() == 1,
            spigot = !builtByBit && !polymart && enabled;
    private static String
            userModified = null,
            fileModified = null;

    static void set(int user, int nonce) {
        IDs.userModified = Integer.toString(user);
        IDs.fileModified = Integer.toString(nonce);
    }

    public static String user() {
        return userModified != null ? userModified : user;
    }

    public static String file() {
        if (IDs.enabled) {
            String f = fileModified != null ? fileModified : file;

            if (!f.startsWith("%%__")
                    && !AlgebraUtils.validInteger(f)) {
                f = String.valueOf(Objects.hash(f));
            }
            return f;
        } else {
            return user();
        }
    }

    static String platform() {
        return IDs.builtByBit ? "BuiltByBit" : IDs.polymart ? "Polymart" : IDs.spigot ? "SpigotMC" : "Free";
    }

    public static boolean canAdvertise() {
        return !IDs.enabled || IDs.builtByBit || IDs.polymart;
    }

}
