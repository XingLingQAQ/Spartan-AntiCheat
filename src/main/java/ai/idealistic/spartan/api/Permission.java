package ai.idealistic.spartan.api;

import ai.idealistic.spartan.Register;

public enum Permission {
    CONDITION, WAVE, ADMIN, RELOAD,
    KICK, BYPASS, MANAGE, INFO, WARN, USE_BYPASS,
    NOTIFICATIONS, BEDROCK;

    private final String key;

    Permission() {
        key = Register.command + "." + this.name().toLowerCase();
    }

    public String getKey() {
        return key;
    }
}
