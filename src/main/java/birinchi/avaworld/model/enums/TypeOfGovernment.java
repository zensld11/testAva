package birinchi.avaworld.model.enums;

public enum TypeOfGovernment {
    CHIEFTAIN("Вождь Племени"),
    COUNCIL_OF_ELDERS("Совет Старейшин"),
    THE_EARTH_KING("Царь Земли");

    private final String displayName;

    TypeOfGovernment(String displayName) {
        this.displayName = displayName;
    }
}