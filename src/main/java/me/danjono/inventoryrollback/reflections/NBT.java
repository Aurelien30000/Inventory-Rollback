package me.danjono.inventoryrollback.reflections;

import me.danjono.inventoryrollback.InventoryRollback;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class NBT {

    private static final Method BUKKIT_AS_NMS_ITEM;
    private static final Method NMS_AS_BUKKIT_ITEM;
    private static final Constructor<?> NBT_TAG_CONSTRUCTOR;

    // 1.20.4-
    private static String GET_TAG_METHOD_NAME;
    private static String SET_TAG_METHOD_NAME;

    // 1.20.5+
    private static Method GET_DATA_COMPONENT_MAP;
    private static Object CUSTOM_DATA_COMPONENT_MAP_KEY; // custom_data key
    private static Method GET_DATA_COMPONENT_VALUE;
    private static Method GET_CUSTOM_DATA_NBT_COPY;
    private static Method UPDATE_CUSTOM_DATA_NBT;

    private static final Map<Class<?>, String> GET_TAG_ELEMENT_METHOD_NAMES = new HashMap<>();
    private static final Map<Class<?>, String> SET_TAG_ELEMENT_METHOD_NAMES = new HashMap<>();

    static {
        try {
            // Init and cache the commonly used reflection accessor objects
            final Class<?> nmsItemStackClass;
            final Class<?> nbtClass;

            // 1.17.1+
            nmsItemStackClass = Packets.getNMSClass("world.item.ItemStack");
            nbtClass = Packets.getNMSClass("nbt.NBTTagCompound");

            final Class<?> craftItemStackClass = Packets.getCraftBukkitClass("inventory.CraftItemStack");

            BUKKIT_AS_NMS_ITEM = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
            NMS_AS_BUKKIT_ITEM = craftItemStackClass.getMethod("asBukkitCopy", nmsItemStackClass);
            NBT_TAG_CONSTRUCTOR = nbtClass.getConstructor();

            final InventoryRollback.VersionName version = InventoryRollback.getVersion();
            if (version.isAtLeast(InventoryRollback.VersionName.v1_20_5)) {
                resolve1_20_5OrHigherReflectionNames(version);
            } else {
                resolvePre1_20_5ReflectionNames(version);
            }

            resolveNbtTagCompoundReflectionNames(version);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void resolve1_20_5OrHigherReflectionNames(InventoryRollback.VersionName version) {
        try {
            // 1.20.5 or higher (1.20.5 now places custom NBT in a custom_data component)
            String getDataComponentMapMethodName = "a";

            Class<?> dataComponentsClass = Class.forName("net.minecraft.core.component.DataComponents");
            CUSTOM_DATA_COMPONENT_MAP_KEY = dataComponentsClass.getField("b").get(null);

            Class<?> nmsItemStackClass = Class.forName("net.minecraft.world.item.ItemStack");
            GET_DATA_COMPONENT_MAP = nmsItemStackClass.getMethod(getDataComponentMapMethodName);

            Class<?> dataComponentMapClass = Class.forName("net.minecraft.core.component.DataComponentMap");
            Class<?> dataComponentTypeClass = Class.forName("net.minecraft.core.component.DataComponentType");
            GET_DATA_COMPONENT_VALUE = dataComponentMapClass.getMethod("a", dataComponentTypeClass);

            Class<?> customDataClass = Class.forName("net.minecraft.world.item.component.CustomData");
            Class<?> itemStackClass = Class.forName("net.minecraft.world.item.ItemStack");
            GET_CUSTOM_DATA_NBT_COPY = customDataClass.getMethod("c");
            UPDATE_CUSTOM_DATA_NBT = customDataClass.getMethod("a", dataComponentTypeClass, itemStackClass, Consumer.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void resolvePre1_20_5ReflectionNames(InventoryRollback.VersionName version) {
        if (version.isAtLeast(InventoryRollback.VersionName.v1_18_0)) {
            if (version.isAtLeast(InventoryRollback.VersionName.v1_20_0)) {
                GET_TAG_METHOD_NAME = "v";
            } else if (version.isAtLeast(InventoryRollback.VersionName.v1_19_0)) {
                GET_TAG_METHOD_NAME = "u";
            } else if (version.isAtLeast(InventoryRollback.VersionName.v1_18_2)) {
                GET_TAG_METHOD_NAME = "t";
            } else {
                GET_TAG_METHOD_NAME = "s";
            }

            SET_TAG_METHOD_NAME = "c";
        } else {
            GET_TAG_METHOD_NAME = "getTag";
            SET_TAG_METHOD_NAME = "setTag";
        }
    }

    private static void resolveNbtTagCompoundReflectionNames(InventoryRollback.VersionName version) {
        if (version.isAtLeast(InventoryRollback.VersionName.v1_18_0)) {
            GET_TAG_ELEMENT_METHOD_NAMES.put(Integer.class, "h");
            GET_TAG_ELEMENT_METHOD_NAMES.put(Long.class, "i");
            GET_TAG_ELEMENT_METHOD_NAMES.put(Float.class, "j");
            GET_TAG_ELEMENT_METHOD_NAMES.put(Double.class, "k");
            GET_TAG_ELEMENT_METHOD_NAMES.put(String.class, "l");

            SET_TAG_ELEMENT_METHOD_NAMES.put(Integer.class, "a");
            SET_TAG_ELEMENT_METHOD_NAMES.put(Long.class, "a");
            SET_TAG_ELEMENT_METHOD_NAMES.put(Float.class, "a");
            SET_TAG_ELEMENT_METHOD_NAMES.put(Double.class, "a");
            SET_TAG_ELEMENT_METHOD_NAMES.put(String.class, "a");
        } else {
            GET_TAG_ELEMENT_METHOD_NAMES.put(Integer.class, "getInt");
            GET_TAG_ELEMENT_METHOD_NAMES.put(Long.class, "getLong");
            GET_TAG_ELEMENT_METHOD_NAMES.put(Float.class, "getFloat");
            GET_TAG_ELEMENT_METHOD_NAMES.put(Double.class, "getDouble");
            GET_TAG_ELEMENT_METHOD_NAMES.put(String.class, "getString");

            SET_TAG_ELEMENT_METHOD_NAMES.put(Integer.class, "setInt");
            SET_TAG_ELEMENT_METHOD_NAMES.put(Long.class, "setLong");
            SET_TAG_ELEMENT_METHOD_NAMES.put(Float.class, "setFloat");
            SET_TAG_ELEMENT_METHOD_NAMES.put(Double.class, "setDouble");
            SET_TAG_ELEMENT_METHOD_NAMES.put(String.class, "setString");
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
        final String uuid = getString("uuid");
        return uuid != null && !uuid.isEmpty();
    }

    public ItemStack setString(String key, String data) {
        return writeDataToBukkitItem(key, String.class, data);
    }

    public ItemStack setInt(String key, Integer data) {
        return writeDataToBukkitItem(key, int.class, data);
    }

    public ItemStack setLong(String key, Long data) {
        return writeDataToBukkitItem(key, long.class, data);
    }

    public ItemStack setDouble(String key, Double data) {
        return writeDataToBukkitItem(key, double.class, data);
    }

    public ItemStack setFloat(String key, Float data) {
        return writeDataToBukkitItem(key, float.class, data);
    }

    public String getString(String key) {
        return readDataFromBukkitItem(key, String.class, String.class);
    }

    public int getInt(String key) {
        return readDataFromBukkitItem(key, int.class, Integer.class);
    }

    public Long getLong(String key) {
        return readDataFromBukkitItem(key, long.class, Long.class);
    }

    public double getDouble(String key) {
        return readDataFromBukkitItem(key, double.class, Double.class);
    }

    public Float getFloat(String key) {
        return readDataFromBukkitItem(key, float.class, Float.class);
    }

    private @Nullable <T> T readDataFromBukkitItem(String key, Class<T> dataType, Class<?> mapType) {
        T result = null;

        try {
            Object itemstack = BUKKIT_AS_NMS_ITEM.invoke(null, item);

            if (InventoryRollback.getVersion().isAtLeast(InventoryRollback.VersionName.v1_20_5)) {
                result = readCustomDataFromNmsItem(itemstack, key, dataType, mapType);
            } else {
                result = readNbtFromNmsItem(itemstack, key, dataType, mapType);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // noinspection ReassignedVariable
        return result;
    }

    private <T> T readCustomDataFromNmsItem(Object nmsItem, String key, Class<T> dataType, Class<?> mapType)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Object compMap = GET_DATA_COMPONENT_MAP.invoke(nmsItem);
        Object customData = GET_DATA_COMPONENT_VALUE.invoke(compMap, CUSTOM_DATA_COMPONENT_MAP_KEY);
        if (customData == null) {
            return null;
        }

        Object nbtComp = GET_CUSTOM_DATA_NBT_COPY.invoke(customData);
        return this.readNbtValue(key, mapType, nbtComp);
    }

    private <T> T readNbtFromNmsItem(Object nmsItem, String key, Class<T> dataType, Class<?> mapType)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Object comp = null;
        try {
            comp = nmsItem.getClass().getMethod(GET_TAG_METHOD_NAME).invoke(nmsItem);
        } catch (NullPointerException e) {
            return null;
        }

        return readNbtValue(key, mapType, comp);
    }

    private <T> @Nullable T readNbtValue(String key, Class<?> mapType, Object nbtComponent)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        try {
            // noinspection unchecked
            return (T) nbtComponent.getClass().getMethod(GET_TAG_ELEMENT_METHOD_NAMES.get(mapType), String.class)
                    .invoke(nbtComponent, key);
        } catch (NullPointerException e) {
            return null;
        }
    }

    private ItemStack writeDataToBukkitItem(String key, Class<?> dataType, Object data) {
        try {
            Object nmsItem = BUKKIT_AS_NMS_ITEM
                    .invoke(null, item);

            if (InventoryRollback.getVersion().isAtLeast(InventoryRollback.VersionName.v1_20_5)) {
                writeCustomDataToNmsItem(nmsItem, key, dataType, data);
            } else {
                writeNbtToNmsItem(nmsItem, key, dataType, data);
            }

            item = (ItemStack) NMS_AS_BUKKIT_ITEM.invoke(null, nmsItem);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return item;
    }

    private void writeCustomDataToNmsItem(Object nmsItem, String key, Class<?> dataType, Object data)
            throws InvocationTargetException, IllegalAccessException {
        UPDATE_CUSTOM_DATA_NBT.invoke(null, CUSTOM_DATA_COMPONENT_MAP_KEY, nmsItem, (Consumer<Object>) (comp) -> {
            try {
                writeNbtValue(comp, key, dataType, data);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                ex.printStackTrace();
            }
        });
    }

    private void writeNbtToNmsItem(Object nmsItem, String key, Class<?> dataType, Object data)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {
        Object comp = nmsItem.getClass().getMethod(GET_TAG_METHOD_NAME).invoke(nmsItem);

        if (comp == null) {
            comp = NBT_TAG_CONSTRUCTOR.newInstance();
        }

        writeNbtValue(comp, key, dataType, data);

        nmsItem.getClass().getMethod(SET_TAG_METHOD_NAME, comp.getClass()).invoke(nmsItem, comp);
    }

    private static void writeNbtValue(Object nbtComponent, String key, Class<?> dataType, Object data) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        nbtComponent.getClass().getMethod(SET_TAG_ELEMENT_METHOD_NAMES.get(data.getClass()), String.class, dataType)
                .invoke(nbtComponent, key, data);
    }

}
