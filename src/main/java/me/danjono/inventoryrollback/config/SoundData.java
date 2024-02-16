package me.danjono.inventoryrollback.config;

import org.bukkit.Sound;

public class SoundData extends ConfigFile {

    public static Sound enderPearl;
    public static boolean enderPearlEnabled;
    public static float enderPearlVolume;

    public static Sound mainInventory;
    public static boolean mainInventoryEnabled;
    public static float mainInventoryVolume;

    public static Sound enderChest;
    public static boolean enderChestEnabled;
    public static float enderChestVolume;

    public static Sound food;
    public static boolean foodEnabled;
    public static float foodVolume;

    public static Sound hunger;
    public static boolean hungerEnabled;
    public static float hungerVolume;

    public static Sound experience;
    public static boolean experienceEnabled;
    public static float experienceVolume;

    public void setSounds() {

        //If sounds are invalid they will be disabled.
        try {
            enderPearl = Sound.valueOf((String) getDefaultValue("sounds.enderPearl.sound", "ENTITY_ENDERMAN_TELEPORT"));
        } catch (IllegalArgumentException e) {
            enderPearl = Sound.ENTITY_ENDERMAN_TELEPORT;
        }
        enderPearlEnabled = (boolean) getDefaultValue("sounds.enderPearl.enabled", true);
        enderPearlVolume = ((Double) getDefaultValue("sounds.enderPearl.volume", 0.5)).floatValue();

        try {
            mainInventory = Sound.valueOf((String) getDefaultValue("sounds.mainInventory.sound", "ENTITY_HORSE_ARMOR"));
        } catch (IllegalArgumentException e) {
            mainInventory = Sound.ENTITY_HORSE_ARMOR;
        }
        mainInventoryEnabled = (boolean) getDefaultValue("sounds.mainInventory.enabled", true);
        mainInventoryVolume = ((Double) getDefaultValue("sounds.mainInventory.volume", 0.5)).floatValue();

        try {
            enderChest = Sound.valueOf((String) getDefaultValue("sounds.enderChest.sound", "ENTITY_ENDER_DRAGON_FLAP"));
        } catch (IllegalArgumentException e) {
            enderChest = Sound.ENTITY_ENDER_DRAGON_FLAP;
        }
        enderChestEnabled = (boolean) getDefaultValue("sounds.enderChest.enabled", true);
        enderChestVolume = ((Double) getDefaultValue("sounds.enderChest.volume", 0.5)).floatValue();

        try {
            food = Sound.valueOf((String) getDefaultValue("sounds.food.sound", "ENTITY_GENERIC_EAT"));
        } catch (IllegalArgumentException e) {
            food = Sound.ENTITY_GENERIC_EAT;
        }
        foodEnabled = (boolean) getDefaultValue("sounds.food.enabled", true);
        foodVolume = ((Double) getDefaultValue("sounds.food.volume", 0.5)).floatValue();

        try {
            hunger = Sound.valueOf((String) getDefaultValue("sounds.hunger.sound", "ENTITY_HORSE_EAT"));
        } catch (IllegalArgumentException e) {
            hunger = Sound.ENTITY_HORSE_EAT;
        }
        hungerEnabled = (boolean) getDefaultValue("sounds.hunger.enabled", true);
        hungerVolume = ((Double) getDefaultValue("sounds.hunger.volume", 0.5)).floatValue();

        try {
            experience = Sound.valueOf((String) getDefaultValue("sounds.xp.sound", "ENTITY_PLAYER_LEVELUP"));
        } catch (IllegalArgumentException e) {
            experience = Sound.ENTITY_PLAYER_LEVELUP;
        }
        experienceEnabled = (boolean) getDefaultValue("sounds.xp.enabled", true);
        experienceVolume = ((Double) getDefaultValue("sounds.xp.volume", 0.5)).floatValue();
    }

}
