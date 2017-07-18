package net.lliira.illyriad.map.model;

import java.util.Queue;

/**
 * The type of all the resources
 */
public enum ResourceType {
    Wood,
    Clay,
    Iron,
    Stone,
    Food,
    Gold,
    Herb,
    Mineral,
    Skin,
    Grape,
    RareHerb("rare_herb"),
    RareMineral("rare_mineral"),
    AnimalPart("animal_Part"),
    ElementalSalt("elemental_salt"),
    Equipment;

    public static final ResourceType[] BASICS = {Wood, Clay, Iron, Stone, Food, Gold};
    public static final ResourceType[] DEPOSITS = {
            Herb,
            Mineral,
            Skin,
            Grape,
            RareHerb,
            RareMineral,
            AnimalPart,
            ElementalSalt,
            Equipment};

    public static ResourceType toBasicResource(String typeIndex) {
        if (typeIndex.equals("1")) {
            return Wood;
        } else if (typeIndex.equals("2")) {
            return Clay;
        } else if (typeIndex.equals("3")) {
            return Iron;
        } else if (typeIndex.equals("4")) {
            return Stone;
        } else if (typeIndex.equals("5")) {
            return Food;
        } else if (typeIndex.equals("6")) {
            return Gold;
        } else {
            throw new RuntimeException(String.format("Unknown basic resource type: %s", typeIndex));
        }
    }

    public final String mName;

    ResourceType() {
        mName = name().toLowerCase();
    }

    ResourceType(String name) {
        mName = name;
    }
}
