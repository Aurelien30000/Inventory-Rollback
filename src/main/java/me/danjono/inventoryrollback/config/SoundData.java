package me.danjono.inventoryrollback.config;

import me.danjono.inventoryrollback.InventoryRollback;
import me.danjono.inventoryrollback.InventoryRollback.VersionName;
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
            enderPearl = Sound.valueOf((String) getDefaultValue("sounds.enderPearl.sound", "ENTITY_ENDERMEN_TELEPORT"));
        } catch (IllegalArgumentException e) {
            if (InventoryRollback.getVersion().between(VersionName.v1_8_R1, VersionName.v1_8_R3)) {
                enderPearl = Sound.valueOf("ENDERMAN_TELEPORT");
            } else if (InventoryRollback.getVersion().between(VersionName.v1_9_R1, VersionName.v1_12_R1)) {
                enderPearl = Sound.valueOf("ENTITY_ENDERMEN_TELEPORT");
            } else {
                enderPearl = Sound.valueOf("ENTITY_ENDERMAN_TELEPORT");
            }
        }
        enderPearlEnabled = (boolean) getDefaultValue("sounds.enderPearl.enabled", true);
        enderPearlVolume = ((Double) getDefaultValue("sounds.enderPearl.volume", 0.5)).floatValue();

        try {
            mainInventory = Sound.valueOf((String) getDefaultValue("sounds.mainInventory.sound", "ENTITY_HORSE_ARMOR"));
        } catch (IllegalArgumentException e) {
            if (InventoryRollback.getVersion().between(VersionName.v1_8_R1, VersionName.v1_8_R3)) {
                mainInventory = Sound.valueOf("HORSE_ARMOR");
            } else if (InventoryRollback.getVersion().between(VersionName.v1_9_R1, VersionName.v1_12_R1)) {
                mainInventory = Sound.valueOf("ENTITY_HORSE_ARMOR");
            } else {
                mainInventory = Sound.valueOf("ENTITY_HORSE_ARMOR");
            }
        }
        mainInventoryEnabled = (boolean) getDefaultValue("sounds.mainInventory.enabled", true);
        mainInventoryVolume = ((Double) getDefaultValue("sounds.mainInventory.volume", 0.5)).floatValue();

        try {
            enderChest = Sound.valueOf((String) getDefaultValue("sounds.enderChest.sound", "ENTITY_ENDERDRAGON_FLAP"));
        } catch (IllegalArgumentException e) {
            if (InventoryRollback.getVersion().between(VersionName.v1_8_R1, VersionName.v1_8_R3)) {
                enderChest = Sound.valueOf("ENDERDRAGON_WINGS");
            } else if (InventoryRollback.getVersion().between(VersionName.v1_9_R1, VersionName.v1_12_R1)) {
                enderChest = Sound.valueOf("ENTITY_ENDERDRAGON_FLAP");
            } else {
                enderChest = Sound.valueOf("ENTITY_ENDER_DRAGON_FLAP");
            }
        }
        enderChestEnabled = (boolean) getDefaultValue("sounds.enderChest.enabled", true);
        enderChestVolume = ((Double) getDefaultValue("sounds.enderChest.volume", 0.5)).floatValue();

        try {
            food = Sound.valueOf((String) getDefaultValue("sounds.food.sound", "ENTITY_GENERIC_EAT"));
        } catch (IllegalArgumentException e) {
            if (InventoryRollback.getVersion().between(VersionName.v1_8_R1, VersionName.v1_8_R3)) {
                food = Sound.valueOf("EAT");
            } else if (InventoryRollback.getVersion().between(VersionName.v1_9_R1, VersionName.v1_12_R1)) {
                food = Sound.valueOf("ENTITY_GENERIC_EAT");
            } else {
                food = Sound.valueOf("ENTITY_GENERIC_EAT");
            }
        }
        foodEnabled = (boolean) getDefaultValue("sounds.food.enabled", true);
        foodVolume = ((Double) getDefaultValue("sounds.food.volume", 0.5)).floatValue();

        try {
            hunger = Sound.valueOf((String) getDefaultValue("sounds.hunger.sound", "ENTITY_HORSE_EAT"));
        } catch (IllegalArgumentException e) {
            if (InventoryRollback.getVersion().between(VersionName.v1_8_R1, VersionName.v1_8_R3)) {
                hunger = Sound.valueOf("HORSE_IDLE");
            } else if (InventoryRollback.getVersion().between(VersionName.v1_9_R1, VersionName.v1_12_R1)) {
                hunger = Sound.valueOf("ENTITY_HORSE_EAT");
            } else {
                hunger = Sound.valueOf("ENTITY_HORSE_EAT");
            }
        }
        hungerEnabled = (boolean) getDefaultValue("sounds.hunger.enabled", true);
        hungerVolume = ((Double) getDefaultValue("sounds.hunger.volume", 0.5)).floatValue();

        try {
            experience = Sound.valueOf((String) getDefaultValue("sounds.xp.sound", "ENTITY_PLAYER_LEVELUP"));
        } catch (IllegalArgumentException e) {
            if (InventoryRollback.getVersion().between(VersionName.v1_8_R1, VersionName.v1_8_R3)) {
                experience = Sound.valueOf("LEVEL_UP");
            } else if (InventoryRollback.getVersion().between(VersionName.v1_9_R1, VersionName.v1_12_R1)) {
                experience = Sound.valueOf("ENTITY_PLAYER_LEVELUP");
            } else {
                experience = Sound.valueOf("ENTITY_PLAYER_LEVELUP");
            }
        }
        experienceEnabled = (boolean) getDefaultValue("sounds.xp.enabled", true);
        experienceVolume = ((Double) getDefaultValue("sounds.xp.volume", 0.5)).floatValue();
    }

}
