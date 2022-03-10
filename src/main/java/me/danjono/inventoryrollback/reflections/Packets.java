package me.danjono.inventoryrollback.reflections;

import me.danjono.inventoryrollback.InventoryRollback;

public class Packets {

    public static Class<?> getNMSClass(String name) throws ClassNotFoundException {
        return InventoryRollback.getVersion().greaterThanOrEqualTo(InventoryRollback.VersionName.v1_17_R1)
                ? Class.forName("net.minecraft." + name)
                : Class.forName("net.minecraft.server." + InventoryRollback.getPackageVersion() + "." + name);
    }

    public static Class<?> getCraftBukkitClass(String name) throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit." + InventoryRollback.getPackageVersion() + "." + name);
    }

}