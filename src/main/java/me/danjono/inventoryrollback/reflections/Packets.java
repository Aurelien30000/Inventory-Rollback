package me.danjono.inventoryrollback.reflections;

import org.bukkit.Bukkit;

public class Packets {

    private static final String CRAFTBUKKIT_PACKAGE = Bukkit.getServer().getClass().getPackage().getName();

    public static Class<?> getNMSClass(String name) throws ClassNotFoundException {
        return Class.forName("net.minecraft." + name);
    }

    public static Class<?> getCraftBukkitClass(String name) throws ClassNotFoundException {
        return Class.forName(CRAFTBUKKIT_PACKAGE + "." + name);
    }

}