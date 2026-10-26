package br.com.caosarcana.common.equipment;

public enum RequirementType {
    EQUIP("Equipar / Usar"),
    CRAFT("Fabricar"),
    TRADE("Comercializar");

    private final String displayName;

    RequirementType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
