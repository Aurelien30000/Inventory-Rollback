package me.danjono.inventoryrollback.reflections;

import me.danjono.inventoryrollback.InventoryRollback;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class NBT {

    private static final Method BUKKIT_AS_NMS_ITEM;
    private static final Method NMS_AS_BUKKIT_ITEM;
    private static final Constructor<?> NBT_TAG_CONSTRUCTOR;

    private static final Method GET_TAG_METHOD;
    private static final Method SET_TAG_METHOD;

    private static final Map<Class<?>, Method> GET_TAG_ELEMENT_METHOD = new HashMap<>();
    private static final Map<Class<?>, Method> SET_TAG_ELEMENT_METHOD = new HashMap<>();

    static {
        try {
            // Init and cache the commonly used reflection accessor objects
            final Class<?> nmsItemStackClass;
            final Class<?> nbtClass;

            if (InventoryRollback.getVersion().greaterThanOrEqualTo(InventoryRollback.VersionName.v1_18_PLUS)) {
                nmsItemStackClass = Packets.getNMSClass("world.item.ItemStack");
                nbtClass = Packets.getNMSClass("nbt.NBTTagCompound");
            } else {
                nmsItemStackClass = Packets.getNMSClass("ItemStack");
                nbtClass = Packets.getNMSClass("NBTTagCompound");
            }

            final Class<?> craftItemStackClass = Packets.getCraftBukkitClass("inventory.CraftItemStack");

            BUKKIT_AS_NMS_ITEM = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
            NMS_AS_BUKKIT_ITEM = craftItemStackClass.getMethod("asBukkitCopy", nmsItemStackClass);
            NBT_TAG_CONSTRUCTOR = nbtClass.getConstructor();

            final String getTagMethodName, setTagMethodName,
                    getTagString, getTagInteger, getTagLong, getTagFloat, getTagDouble,
                    setTagString, setTagInteger, setTagLong, setTagFloat, setTagDouble;
            if (InventoryRollback.getVersion().greaterThanOrEqualTo(InventoryRollback.VersionName.v1_18_PLUS)) {
                getTagMethodName = "s";
                setTagMethodName = "c";

                getTagString = "l";
                getTagInteger = "h";
                getTagLong = "i";
                getTagFloat = "j";
                getTagDouble = "k";

                setTagString = "a";
                setTagInteger = "a";
                setTagLong = "a";
                setTagFloat = "a";
                setTagDouble = "a";
            } else {
                getTagMethodName = "getTag";
                setTagMethodName = "setTag";

                getTagString = "getString";
                getTagInteger = "getInt";
                getTagLong = "getLong";
                getTagFloat = "getFloat";
                getTagDouble = "getDouble";

                setTagString = "setString";
                setTagInteger = "setInt";
                setTagLong = "setLong";
                setTagFloat = "setFloat";
                setTagDouble = "setDouble";
            }

            GET_TAG_METHOD = nmsItemStackClass.getMethod(getTagMethodName);
            SET_TAG_METHOD = nmsItemStackClass.getMethod(setTagMethodName, nbtClass);

            GET_TAG_ELEMENT_METHOD.put(String.class, nbtClass.getMethod(getTagString, String.class));
            GET_TAG_ELEMENT_METHOD.put(int.class, nbtClass.getMethod(getTagInteger, String.class));
            GET_TAG_ELEMENT_METHOD.put(long.class, nbtClass.getMethod(getTagLong, String.class));
            GET_TAG_ELEMENT_METHOD.put(float.class, nbtClass.getMethod(getTagFloat, String.class));
            GET_TAG_ELEMENT_METHOD.put(double.class, nbtClass.getMethod(getTagDouble, String.class));

            SET_TAG_ELEMENT_METHOD.put(String.class, nbtClass.getMethod(setTagString, String.class, String.class));
            SET_TAG_ELEMENT_METHOD.put(int.class, nbtClass.getMethod(setTagInteger, String.class, int.class));
            SET_TAG_ELEMENT_METHOD.put(long.class, nbtClass.getMethod(setTagLong, String.class, long.class));
            SET_TAG_ELEMENT_METHOD.put(float.class, nbtClass.getMethod(setTagFloat, String.class, float.class));
            SET_TAG_ELEMENT_METHOD.put(double.class, nbtClass.getMethod(setTagDouble, String.class, double.class));
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private ItemStack item;

    public NBT(ItemStack item) {
        this.item = item;
    }

    public ItemStack setItemData() {
        return item;
    }

    public boolean hasUUID() {
        String uuid = getString("uuid");

        return uuid != null && !uuid.isEmpty();
    }

    public String getString(String key) {
        if (item == null || key == null) {
            return null;
        }

        try {
            final Object comp = getNBTCompound();

            if (comp == null) {
                return null;
            }
            return (String) GET_TAG_ELEMENT_METHOD.get(String.class).invoke(comp, key);

        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }

        return null;
    }

    public int getInt(String key) {
        if (item == null || key == null) {
            return 0;
        }

        try {
            final Object comp = getNBTCompound();
            if (comp == null) {
                return 0;
            }
            return (int) GET_TAG_ELEMENT_METHOD.get(int.class).invoke(comp, key);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public long getLong(String key) {
        if (item == null || key == null) {
            return 0;
        }

        try {
            final Object comp = getNBTCompound();
            if (comp == null) {
                return 0;
            }
            return (long) GET_TAG_ELEMENT_METHOD.get(long.class).invoke(comp, key);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }

        return 0;
    }


    public float getFloat(String key) {
        if (item == null || key == null) {
            return 0;
        }

        try {
            final Object comp = getNBTCompound();
            if (comp == null) {
                return 0;
            }
            return (float) GET_TAG_ELEMENT_METHOD.get(float.class).invoke(comp, key);
        } catch (ReflectiveOperationException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    public double getDouble(String key) {
        if (item == null || key == null) {
            return 0;
        }

        try {
            final Object comp = getNBTCompound();
            if (comp == null) {
                return 0;
            }

            return (double) GET_TAG_ELEMENT_METHOD.get(double.class).invoke(comp, key);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private Object getNBTCompound() throws ReflectiveOperationException {
        final Object itemstack = BUKKIT_AS_NMS_ITEM.invoke(null, item);
        return GET_TAG_METHOD.invoke(itemstack);
    }

    public ItemStack setString(String key, String data) {
        try {
            final Object itemstack = BUKKIT_AS_NMS_ITEM.invoke(null, item);
            Object comp = GET_TAG_METHOD.invoke(itemstack);

            if (comp == null) {
                comp = NBT_TAG_CONSTRUCTOR.newInstance();
            }

            SET_TAG_ELEMENT_METHOD.get(String.class).invoke(comp, key, data);

            SET_TAG_METHOD.invoke(itemstack, comp);
            item = (ItemStack) NMS_AS_BUKKIT_ITEM.invoke(null, itemstack);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return item;
    }

    public ItemStack setInt(String key, int data) {
        try {
            final Object itemstack = BUKKIT_AS_NMS_ITEM.invoke(null, item);
            Object comp = GET_TAG_METHOD.invoke(itemstack);

            if (comp == null) {
                comp = NBT_TAG_CONSTRUCTOR.newInstance();
            }

            SET_TAG_ELEMENT_METHOD.get(int.class).invoke(comp, key, data);

            SET_TAG_METHOD.invoke(itemstack, comp);
            item = (ItemStack) NMS_AS_BUKKIT_ITEM.invoke(null, itemstack);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return item;
    }

    public ItemStack setLong(String key, long data) {
        try {
            final Object itemstack = BUKKIT_AS_NMS_ITEM.invoke(null, item);
            Object comp = GET_TAG_METHOD.invoke(itemstack);

            if (comp == null) {
                comp = NBT_TAG_CONSTRUCTOR.newInstance();
            }

            SET_TAG_ELEMENT_METHOD.get(long.class).invoke(comp, key, data);

            SET_TAG_METHOD.invoke(itemstack, comp);
            item = (ItemStack) NMS_AS_BUKKIT_ITEM.invoke(null, itemstack);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return item;
    }

    public ItemStack setFloat(String key, float data) {
        try {
            final Object itemstack = BUKKIT_AS_NMS_ITEM.invoke(null, item);
            Object comp = GET_TAG_METHOD.invoke(itemstack);

            if (comp == null) {
                comp = NBT_TAG_CONSTRUCTOR.newInstance();
            }

            SET_TAG_ELEMENT_METHOD.get(float.class).invoke(comp, key, data);

            SET_TAG_METHOD.invoke(itemstack, comp);
            item = (ItemStack) NMS_AS_BUKKIT_ITEM.invoke(null, itemstack);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return item;
    }

    public ItemStack setDouble(String key, double data) {
        try {
            final Object itemstack = BUKKIT_AS_NMS_ITEM.invoke(null, item);
            Object comp = GET_TAG_METHOD.invoke(itemstack);

            if (comp == null) {
                comp = NBT_TAG_CONSTRUCTOR.newInstance();
            }

            SET_TAG_ELEMENT_METHOD.get(double.class).invoke(comp, key, data);

            SET_TAG_METHOD.invoke(itemstack, comp);
            item = (ItemStack) NMS_AS_BUKKIT_ITEM.invoke(null, itemstack);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return item;
    }

}
