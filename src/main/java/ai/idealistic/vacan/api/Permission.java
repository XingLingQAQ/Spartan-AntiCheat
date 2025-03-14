package ai.idealistic.vacan.api;

import ai.idealistic.vacan.Register;

public enum Permission {
    CONDITION, WAVE, RECONNECT, ADMIN, RELOAD,
    KICK, BYPASS, MANAGE, INFO, CHAT_PROTECTION, WARN, USE_BYPASS,
    NOTIFICATIONS, BEDROCK;

    private final String key;

    Permission() {
        key = Register.command + "." + this.name().toLowerCase();
    }

    public String getKey() {
        return key;
    }
}
