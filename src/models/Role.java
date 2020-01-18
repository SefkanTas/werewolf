package models;

public enum Role {
    WEREWOLF("Loup-Garou"),
    VILLAGER("Villageois"),
    SEER("Voyante"),
    WITCH("Sorciere");

    private String name;

    Role(String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
