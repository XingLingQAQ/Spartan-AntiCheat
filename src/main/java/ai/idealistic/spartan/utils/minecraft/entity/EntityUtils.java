package ai.idealistic.spartan.utils.minecraft.entity;

import ai.idealistic.spartan.utils.java.ReflectionUtils;

public class EntityUtils {

    public static final boolean abstractHorseClass = ReflectionUtils.classExists(
            "org.bukkit.entity.AbstractHorse"
    );

}
