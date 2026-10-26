package br.com.caosarcana.common.equipment;

import net.minecraft.network.chat.Component;

public class RequirementResult {
    private static final RequirementResult ALLOWED = new RequirementResult(true, null);

    private final boolean allowed;
    private final Component denialMessage;

    private RequirementResult(boolean allowed, Component denialMessage) {
        this.allowed = allowed;
        this.denialMessage = denialMessage;
    }

    public static RequirementResult allow() {
        return ALLOWED;
    }

    public static RequirementResult deny(Component message) {
        return new RequirementResult(false, message);
    }

    public static RequirementResult deny(String plainMessage) {
        return new RequirementResult(false, Component.literal(plainMessage));
    }

    public boolean isAllowed() {
        return allowed;
    }

    public Component getDenialMessage() {
        return denialMessage != null ? denialMessage : Component.literal("Requisito não atendido.");
    }
}
