package birinchi.avaworld.model.enums;

public enum PowerType {
    WATER("МАГИЯ ВОДЫ"),
    EARTH("МАГИЯ ЗЕМЛИ"),
    FIRE("МАГИЯ ОГНЯ"),
    AIR("МАГИЯ ВОЗДУХА"),
    METAL("МАГИЯ МЕТАЛЛА"),
    MAGMA("МАГИЯ ЛАВЫ"),
    SAND("МАГИЯ ПЕСКА"),
    NONE("ОТСУТСТВУЕТ");


    private final String displayName;

    PowerType(String displayName) {
        this.displayName = displayName;
    }
}