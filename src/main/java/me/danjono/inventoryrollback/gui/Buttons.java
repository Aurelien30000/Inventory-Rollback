package me.danjono.inventoryrollback.gui;

import me.danjono.inventoryrollback.config.MessageData;
import me.danjono.inventoryrollback.data.LogType;
import me.danjono.inventoryrollback.inventory.RestoreInventory;
import me.danjono.inventoryrollback.reflections.NBT;
import me.danjono.inventoryrollback.util.MathUtils;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Buttons {

    private static final Material pageSelector = Material.WHITE_BANNER;
    private static final Material enderPearl = Material.ENDER_PEARL;
    private static final Material inventory = Material.CHEST;
    private static final Material enderChest = Material.ENDER_CHEST;
    private static final Material health = Material.MELON_SLICE;
    private static final Material hunger = Material.ROTTEN_FLESH;
    private static final Material experience = Material.EXPERIENCE_BOTTLE;

    public static ItemStack getPageSelectorIcon() {
        return new ItemStack(pageSelector);
    }

    public static ItemStack getEnderPearlIcon() {
        return new ItemStack(enderPearl);
    }

    public static ItemStack getInventoryIcon() {
        return new ItemStack(inventory);
    }

    public static ItemStack getEnderChestIcon() {
        return new ItemStack(enderChest);
    }

    public static ItemStack getHealthIcon() {
        return new ItemStack(health);
    }

    public static ItemStack getHungerIcon() {
        return new ItemStack(hunger);
    }

    public static ItemStack getExperienceIcon() {
        return new ItemStack(experience);
    }

    private final MessageData messages;

    public Buttons() {
        this.messages = new MessageData();
    }

    public ItemStack nextButton(String displayName, UUID uuid, LogType logType, int page, List<String> lore) {
        ItemStack button = getPageSelectorIcon();
        final BannerMeta meta = (BannerMeta) button.getItemMeta();

        final List<Pattern> patterns = new ArrayList<>();
        patterns.add(new Pattern(DyeColor.BLACK, PatternType.BASE));
        patterns.add(new Pattern(DyeColor.WHITE, PatternType.RHOMBUS_MIDDLE));
        patterns.add(new Pattern(DyeColor.BLACK, PatternType.HALF_VERTICAL));
        patterns.add(new Pattern(DyeColor.GRAY, PatternType.BORDER));

        meta.setPatterns(patterns);

        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);

        if (displayName != null) {
            meta.setDisplayName(displayName);
        }

        meta.setLore(lore);

        button.setItemMeta(meta);

        final NBT nbt = new NBT(button);

        nbt.setString("uuid", uuid.toString());
        nbt.setString("logType", logType.name());
        nbt.setInt("page", page);
        button = nbt.setItemData();

        return button;
    }

    public ItemStack backButton(String displayName, UUID uuid, LogType logType, int page, List<String> lore) {
        ItemStack button = getPageSelectorIcon();
        final BannerMeta meta = (BannerMeta) button.getItemMeta();

        final List<Pattern> patterns = new ArrayList<>();
        patterns.add(new Pattern(DyeColor.BLACK, PatternType.BASE));
        patterns.add(new Pattern(DyeColor.WHITE, PatternType.RHOMBUS_MIDDLE));
        patterns.add(new Pattern(DyeColor.BLACK, PatternType.HALF_VERTICAL_MIRROR));
        patterns.add(new Pattern(DyeColor.GRAY, PatternType.BORDER));

        meta.setPatterns(patterns);

        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);

        if (displayName != null) {
            meta.setDisplayName(displayName);
        }

        if (lore != null) {
            meta.setLore(lore);
        }

        button.setItemMeta(meta);

        final NBT nbt = new NBT(button);

        nbt.setString("uuid", uuid.toString());
        nbt.setString("logType", logType.name());
        nbt.setInt("page", page);
        button = nbt.setItemData();

        return button;
    }

    public ItemStack mainMenuBackButton(String displayName, UUID uuid) {
        ItemStack button = getPageSelectorIcon();
        final BannerMeta meta = (BannerMeta) button.getItemMeta();

        final List<Pattern> patterns = new ArrayList<>();
        patterns.add(new Pattern(DyeColor.BLACK, PatternType.BASE));
        patterns.add(new Pattern(DyeColor.WHITE, PatternType.RHOMBUS_MIDDLE));
        patterns.add(new Pattern(DyeColor.BLACK, PatternType.HALF_VERTICAL_MIRROR));
        patterns.add(new Pattern(DyeColor.GRAY, PatternType.BORDER));

        meta.setPatterns(patterns);

        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);

        if (displayName != null) {
            meta.setDisplayName(displayName);
        }

        button.setItemMeta(meta);

        final NBT nbt = new NBT(button);

        nbt.setString("uuid", uuid.toString());
        button = nbt.setItemData();

        return button;
    }

    public ItemStack inventoryMenuBackButton(String displayName, UUID uuid, LogType logType) {
        ItemStack button = getPageSelectorIcon();
        final BannerMeta meta = (BannerMeta) button.getItemMeta();

        final List<Pattern> patterns = new ArrayList<>();
        patterns.add(new Pattern(DyeColor.BLACK, PatternType.BASE));
        patterns.add(new Pattern(DyeColor.WHITE, PatternType.RHOMBUS_MIDDLE));
        patterns.add(new Pattern(DyeColor.BLACK, PatternType.HALF_VERTICAL_MIRROR));
        patterns.add(new Pattern(DyeColor.GRAY, PatternType.BORDER));

        meta.setPatterns(patterns);

        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);

        if (displayName != null) {
            meta.setDisplayName(displayName);
        }

        button.setItemMeta(meta);

        final NBT nbt = new NBT(button);

        nbt.setString("uuid", uuid.toString());
        nbt.setString("logType", logType.name());
        button = nbt.setItemData();

        return button;
    }

    public ItemStack createInventoryButton(ItemStack item, UUID uuid, LogType logType, String location, Long timestamp, String displayName, List<String> lore) {
        final ItemMeta meta = item.getItemMeta();
        //meta.setDisplayName(name);

        if (lore != null) {
            meta.setLore(lore);
        }

        meta.setDisplayName(displayName);

        item.setItemMeta(meta);

        final NBT nbt = new NBT(item);

        nbt.setString("uuid", uuid.toString());
        nbt.setString("logType", logType.name());
        nbt.setLong("timestamp", timestamp);
        nbt.setString("location", location);
        item = nbt.setItemData();

        return item;
    }

    public ItemStack createLogTypeButton(ItemStack item, UUID uuid, String name, LogType logType, List<String> lore) {
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);

        if (lore != null) {
            meta.setLore(lore);
        }

        item.setItemMeta(meta);

        final NBT nbt = new NBT(item);

        nbt.setString("uuid", uuid.toString());
        nbt.setString("logType", logType.name());
        item = nbt.setItemData();

        return item;
    }

    public ItemStack playerHead(OfflinePlayer player, List<String> lore) {
        final ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        final SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setOwningPlayer(player);
        skullMeta.setDisplayName(ChatColor.RESET + player.getName());

        if (lore != null) {
            skullMeta.setLore(lore);
        }

        skull.setItemMeta(skullMeta);

        return skull;
    }

    public ItemStack enderPearlButton(UUID uuid, LogType logType, Long timestamp, String location) {
        ItemStack item = new ItemStack(getEnderPearlIcon());

        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageData.deathLocationMessage);

        item.setItemMeta(meta);

        final NBT nbt = new NBT(item);

        nbt.setString("uuid", uuid.toString());
        nbt.setString("logType", logType.name());
        nbt.setLong("timestamp", timestamp);
        nbt.setString("location", location);
        item = nbt.setItemData();

        return item;
    }

    public ItemStack inventoryButton(UUID uuid, LogType logType, Long timestamp) {
        ItemStack item = new ItemStack(getInventoryIcon());

        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageData.restoreInventory);

        item.setItemMeta(meta);

        final NBT nbt = new NBT(item);

        nbt.setString("uuid", uuid.toString());
        nbt.setString("logType", logType.name());
        nbt.setLong("timestamp", timestamp);
        item = nbt.setItemData();

        return item;
    }

    public ItemStack enderChestButton(UUID uuid, LogType logType, Long timestamp) {
        ItemStack item = new ItemStack(getEnderChestIcon());

        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageData.restoreEnderChest);

        item.setItemMeta(meta);

        final NBT nbt = new NBT(item);

        nbt.setString("uuid", uuid.toString());
        nbt.setString("logType", logType.name());
        nbt.setLong("timestamp", timestamp);
        item = nbt.setItemData();

        return item;
    }

    public ItemStack healthButton(UUID uuid, LogType logType, Long timestamp, double health) {
        ItemStack item = new ItemStack(getHealthIcon());

        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageData.restoreHealth);

        final List<String> lore = new ArrayList<>();
        lore.add(messages.restoreHealthLevel(MathUtils.round(health, 2) + ""));
        meta.setLore(lore);

        item.setItemMeta(meta);

        final NBT nbt = new NBT(item);

        nbt.setString("uuid", uuid.toString());
        nbt.setString("logType", logType.name());
        nbt.setLong("timestamp", timestamp);
        nbt.setDouble("health", health);
        item = nbt.setItemData();

        return item;
    }

    public ItemStack hungerButton(UUID uuid, LogType logType, Long timestamp, int hunger, float saturation) {
        ItemStack item = new ItemStack(getHungerIcon());

        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageData.restoreHunger);

        final List<String> lore = new ArrayList<>();
        lore.add(messages.restoreHungerLevel(hunger + ""));
        meta.setLore(lore);

        item.setItemMeta(meta);

        final NBT nbt = new NBT(item);

        nbt.setString("uuid", uuid.toString());
        nbt.setString("logType", logType.name());
        nbt.setLong("timestamp", timestamp);
        nbt.setInt("hunger", hunger);
        nbt.setFloat("saturation", saturation);
        item = nbt.setItemData();

        return item;
    }

    public ItemStack experiencePotion(UUID uuid, LogType logType, Long timestamp, float xp) {
        ItemStack item = new ItemStack(getExperienceIcon());

        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageData.restoreExperience);

        final List<String> lore = new ArrayList<>();
        lore.add(messages.restoreExperienceLevel(RestoreInventory.getLevel(xp) + ""));
        meta.setLore(lore);

        item.setItemMeta(meta);

        final NBT nbt = new NBT(item);

        nbt.setString("uuid", uuid.toString());
        nbt.setString("logType", logType.name());
        nbt.setLong("timestamp", timestamp);
        nbt.setFloat("xp", xp);
        item = nbt.setItemData();

        return item;
    }

}
