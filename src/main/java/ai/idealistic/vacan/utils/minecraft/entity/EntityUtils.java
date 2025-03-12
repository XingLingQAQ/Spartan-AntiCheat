package ai.idealistic.vacan.utils.minecraft.entity;

import ai.idealistic.vacan.utils.java.ReflectionUtils;

public class EntityUtils {

    public static final boolean abstractHorseClass = ReflectionUtils.classExists(
            "org.bukkit.entity.AbstractHorse"
    );

}
