package me.danjono.inventoryrollback.inventory;

import com.lishid.openinv.IOpenInv;
import me.danjono.inventoryrollback.InventoryRollback;
import me.danjono.inventoryrollback.config.MessageData;
import me.danjono.inventoryrollback.config.SoundData;
import me.danjono.inventoryrollback.data.LogType;
import me.danjono.inventoryrollback.gui.BackupMenu;
import me.danjono.inventoryrollback.util.InventoryUtils;
import me.danjono.inventoryrollback.util.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

public class RestoreInventory {

    private final FileConfiguration playerData;
    private final long timestamp;

    public RestoreInventory(FileConfiguration playerData, long timestamp) {
        this.playerData = playerData;
        this.timestamp = timestamp;
    }

    //Credits to Dev_Richard (https://www.spigotmc.org/members/dev_richard.38792/)
    //https://gist.github.com/RichardB122/8958201b54d90afbc6f0
    public static void setTotalExperience(Player player, float xpFloat) {
        final int xp = (int) xpFloat;

        int level;
        float experience;
        //Levels 0 through 15
        if (xp >= 0 && xp < 351) {
            //Calculate Everything
            final int a = 1;
            final int b = 6;
            final int c = -xp;
            final double[] roots = MathUtils.roots(a, b, c);
            level = (int) roots[0];
            final int xpForLevel = level * level + (6 * level);
            final int remainder = xp - xpForLevel;
            final int experienceNeeded = (2 * level) + 7;
            experience = (float) remainder / (float) experienceNeeded;
            experience = MathUtils.round(experience, 2);

            //Levels 16 through 30
        } else if (xp >= 352 && xp < 1507) {
            //Calculate Everything
            final double a = 2.5;
            final double b = -40.5;
            final int c = -xp + 360;
            final double[] roots = MathUtils.roots(a, b, c);
            final double dLevel = roots[0];
            level = (int) Math.floor(dLevel);
            final int xpForLevel = (int) (2.5 * level * level - (40.5 * level) + 360);
            final int remainder = xp - xpForLevel;
            final int experienceNeeded = (5 * level) - 38;
            experience = (float) remainder / (float) experienceNeeded;
            experience = MathUtils.round(experience, 2);

            //Level 31 and greater
        } else {
            //Calculate Everything
            final double a = 4.5;
            final double b = -162.5;
            final int c = -xp + 2220;
            final double[] roots = MathUtils.roots(a, b, c);
            final double dLevel = roots[0];
            level = (int) Math.floor(dLevel);
            final int xpForLevel = (int) (4.5 * level * level - (162.5 * level) + 2220);
            final int remainder = xp - xpForLevel;
            final int experienceNeeded = (9 * level) - 158;
            experience = (float) remainder / (float) experienceNeeded;
            experience = MathUtils.round(experience, 2);
        }
        //Set Everything
        player.setLevel(level);
        player.setExp(experience);
    }

    public static int getLevel(float floatXP) {
        final int xp = (int) floatXP;

        //Levels 0 through 15
        if (xp >= 0 && xp < 351) {
            //Calculate Everything
            final int a = 1;
            final int b = 6;
            final int c = -xp;
            return (int) Math.floor(MathUtils.roots(a, b, c)[0]);
            //Levels 16 through 30
        } else if (xp >= 352 && xp < 1507) {
            //Calculate Everything
            final double a = 2.5;
            final double b = -40.5;
            final int c = -xp + 360;
            return (int) Math.floor(MathUtils.roots(a, b, c)[0]);
            //Level 31 and greater
        } else {
            //Calculate Everything
            final double a = 4.5;
            final double b = -162.5;
            final int c = -xp + 2220;
            return (int) Math.floor(MathUtils.roots(a, b, c)[0]);
        }
    }

    public ItemStack[] retrieveArmour() {
        ItemStack[] inv = retrieveMainInventory();
        if (inv == null || inv.length == 0) {
            return null;
        }

        ItemStack[] armour = new ItemStack[4];
        System.arraycopy(inv, 36, armour, 0, 4);

        return armour;
    }

    public ItemStack[] retrieveMainInventory() {
        try {
            return stacksFromBase64(playerData.getString("data." + timestamp + ".inventory"));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ItemStack[] retrieveEnderChestInventory() {
        ItemStack[] inv = null;

        try {
            inv = stacksFromBase64(playerData.getString("data." + timestamp + ".enderchest"));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        return inv;
    }

    private void logDeserializationError() {
        String packageVersion = playerData.getString("data." + timestamp + ".version");

        //Backup generated before InventoryRollback 1.3
        if (packageVersion == null) {
            InventoryRollback.logger.log(Level.SEVERE, ChatColor.stripColor(MessageData.pluginName)
                    + "There was an error deserializing the material data. This is likely caused by a now incompatible material ID if the backup was originally generated on a different Minecraft server version.");
        }
        //Backup was not generated on the same server version
        else if (!packageVersion.equalsIgnoreCase(InventoryRollback.getPackageVersion())) {
            InventoryRollback.logger.log(Level.SEVERE, ChatColor.stripColor(MessageData.pluginName)
                    + "There was an error deserializing the material data. The backup was generated on a "
                    + packageVersion + " version server whereas you are now running a " + InventoryRollback
                    .getPackageVersion()
                    + " version server. It is likely a material ID inside the backup is no longer valid on this Minecraft server version and cannot be convereted.");
        }
        //Unknown error
        else {
            InventoryRollback.logger.log(Level.SEVERE, ChatColor.stripColor(MessageData.pluginName)
                    + "There was an error deserializing the material data. Please upload the affected players backup file to Pastebin and send a link to it in the discussion page on Spigot for InventoryRollback detailing the problem as accurately as you can.");
        }
    }

    public int getHunger() {
        return playerData.getInt("data." + timestamp + ".hunger");
    }

    public float getSaturation() {
        return (float) playerData.getDouble("data." + timestamp + ".saturation");
    }

    public float getXP() {
        return (float) playerData.getDouble("data." + timestamp + ".xp");
    }

    public double getHealth() {
        return playerData.getDouble("data." + timestamp + ".health");
    }

    public String getLocationString() {
        String world = playerData.getString("data." + timestamp + ".location.world");
        String x = playerData.getString("data." + timestamp + ".location.x");
        String y = playerData.getString("data." + timestamp + ".location.y");
        String z = playerData.getString("data." + timestamp + ".location.z");
        return world + "," + x + "," + y + "," + z;
    }

    private ItemStack[] stacksFromBase64(String data) {
        if (data == null) {
            return new ItemStack[0];
        }

        ItemStack[] stacks;

        try (BukkitObjectInputStream dataInput = new BukkitObjectInputStream(new ByteArrayInputStream(Base64Coder
                .decodeLines(data)))) {
            stacks = new ItemStack[dataInput.readInt()];

            for (int i = 0; i < stacks.length; i++) {
                try {
                    stacks[i] = (ItemStack) dataInput.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    logDeserializationError();
                    return new ItemStack[0];
                }
            }
            return stacks;
        } catch (IOException ex) {
            ex.printStackTrace();
            return new ItemStack[0];
        }
    }

    public BackupMenu getMenu(Player viewer, UUID target, LogType logType) {
        // Deserialize contents
        final ItemStack[] inventory = retrieveMainInventory();
        final ItemStack[] armour = retrieveArmour();
        final ItemStack[] enderchest = retrieveEnderChestInventory();

        final boolean hasEnderChest = enderchest == null || enderchest.length > 0;

        // Deserialize stats
        final float xp = getXP();
        final double health = getHealth();
        final int hunger = getHunger();
        final float saturation = getSaturation();

        // This fine because nothing is accessing bukkit api here
        return new BackupMenu(viewer, target, logType, timestamp, inventory, armour, getLocationString(), hasEnderChest, health, hunger, saturation, xp);
    }

    public void restoreInventory(CommandSender staff, OfflinePlayer offlinePlayer) {
        final IOpenInv iOpenInv = InventoryRollback.getInstance().getOpenInvAPI();

        if (offlinePlayer.isOnline() || iOpenInv != null) {
            final Player player;
            final Inventory inventory;
            final boolean useOpenInv;

            if (offlinePlayer.isOnline()) {
                player = (Player) offlinePlayer;
                inventory = player.getInventory();
                useOpenInv = false;
            } else {
                if (iOpenInv != null) {
                    player = iOpenInv.loadPlayer(offlinePlayer);
                    if (player == null) {
                        staff.sendMessage(MessageData.pluginName + MessageData.mainInventoryNotOnline(offlinePlayer.getName()));
                        return;
                    }

                    iOpenInv.retainPlayer(player, InventoryRollback.getInstance());
                    try {
                        inventory = iOpenInv.getSpecialInventory(player, false /* player is offline */).getBukkitInventory();
                        useOpenInv = true;
                    } catch (InstantiationException e) {
                        staff.sendMessage(MessageData.pluginName + MessageData.mainInventoryNotOnline(offlinePlayer.getName()));
                        return;
                    }
                } else {
                    staff.sendMessage(MessageData.pluginName + MessageData.mainInventoryNotOnline(offlinePlayer.getName()));
                    return;
                }
            }

            if (InventoryUtils.isEmpty(inventory)) {
                Bukkit.getScheduler().runTask(InventoryRollback.getInstance(), () -> {
                    if (useOpenInv) {
                        InventoryUtils.setSortedItems(inventory, retrieveMainInventory(), retrieveArmour());
                    } else {
                        inventory.setContents(retrieveMainInventory());
                        ((PlayerInventory) inventory).setArmorContents(retrieveArmour());
                    }

                    if (SoundData.mainInventoryEnabled) {
                        player.playSound(player.getLocation(), SoundData.mainInventory, SoundData.mainInventoryVolume, 1);
                    }
                });
            } else {
                staff.sendMessage(MessageData.pluginName + MessageData.mainInventoryNotEmpty(offlinePlayer.getName()));
                return;
            }

            if (useOpenInv) {
                iOpenInv.releasePlayer(player, InventoryRollback.getInstance());
                player.saveData();
            }

            staff.sendMessage(MessageData.pluginName + MessageData.mainInventoryRestored(player.getName()));
            if (staff != offlinePlayer.getPlayer())
                player.sendMessage(MessageData.pluginName + MessageData.mainInventoryRestoredPlayer(staff.getName()));
        } else {
            staff.sendMessage(MessageData.pluginName + MessageData.mainInventoryNotOnline(offlinePlayer.getName()));
        }
    }

    public void restoreEnderChest(CommandSender staff, OfflinePlayer offlinePlayer) {
        final IOpenInv iOpenInv = InventoryRollback.getInstance().getOpenInvAPI();

        if (offlinePlayer.isOnline() || iOpenInv != null) {
            final Player player;
            final Inventory inventory;
            final boolean useOpenInv;

            if (offlinePlayer.isOnline()) {
                player = (Player) offlinePlayer;
                inventory = player.getEnderChest();
                useOpenInv = false;
            } else {
                if (iOpenInv != null) {
                    player = iOpenInv.loadPlayer(offlinePlayer);
                    if (player == null) {
                        staff.sendMessage(MessageData.pluginName + MessageData.enderChestNotOnline(offlinePlayer.getName()));
                        return;
                    }

                    iOpenInv.retainPlayer(player, InventoryRollback.getInstance());
                    try {
                        inventory = iOpenInv.getSpecialEnderChest(player, false /* player is offline */).getBukkitInventory();
                        useOpenInv = true;
                    } catch (InstantiationException e) {
                        staff.sendMessage(MessageData.pluginName + MessageData.enderChestNotOnline(offlinePlayer.getName()));
                        return;
                    }
                } else {
                    staff.sendMessage(MessageData.pluginName + MessageData.enderChestNotOnline(offlinePlayer.getName()));
                    return;
                }
            }

            if (InventoryUtils.isEmpty(inventory)) {
                Bukkit.getScheduler().runTask(InventoryRollback.getInstance(), () -> {
                    inventory.setContents(retrieveEnderChestInventory());

                    if (SoundData.enderChestEnabled) {
                        player.playSound(player.getLocation(), SoundData.enderChest, SoundData.enderChestVolume, 1);
                    }
                });
            } else {
                staff.sendMessage(MessageData.pluginName + MessageData.enderChestNotEmpty(offlinePlayer.getName()));
                return;
            }

            if (useOpenInv) {
                iOpenInv.releasePlayer(player, InventoryRollback.getInstance());
            }

            staff.sendMessage(MessageData.pluginName + MessageData.enderChestRestored(player.getName()));
            if (staff != offlinePlayer.getPlayer())
                player.sendMessage(MessageData.pluginName + MessageData.enderChestRestoredPlayer(staff.getName()));
        } else {
            staff.sendMessage(MessageData.pluginName + MessageData.enderChestNotOnline(offlinePlayer.getName()));
        }
    }

    public void restoreHealth(CommandSender staff, OfflinePlayer offlinePlayer) {
        if (offlinePlayer.isOnline()) {
            final Player player = (Player) offlinePlayer;
            final double health = getHealth();

            Bukkit.getScheduler().runTask(InventoryRollback.getInstance(), () -> {
                player.setHealth(health);

                if (SoundData.foodEnabled) {
                    player.playSound(player.getLocation(), SoundData.food, SoundData.foodVolume, 1);
                }

                staff.sendMessage(MessageData.pluginName + MessageData.healthRestored(player.getName()));
                if (staff != player) {
                    player.sendMessage(
                            MessageData.pluginName + MessageData.healthRestoredPlayer(staff.getName()));
                }
            });
        } else {
            staff.sendMessage(MessageData.pluginName + MessageData.healthNotOnline(offlinePlayer.getName()));
        }
    }

    public void restoreFood(CommandSender staff, OfflinePlayer offlinePlayer) {
        if (offlinePlayer.isOnline()) {
            final Player player = (Player) offlinePlayer;
            final int hunger = getHunger();
            final float saturation = getSaturation();

            Bukkit.getScheduler().runTask(InventoryRollback.getInstance(), () -> {
                player.setFoodLevel(hunger);
                player.setSaturation(saturation);

                if (SoundData.hungerEnabled) {
                    player.playSound(player.getLocation(), SoundData.hunger, SoundData.hungerVolume, 1);
                }

                staff.sendMessage(MessageData.pluginName + MessageData.hungerRestored(player.getName()));
                if (staff != player) {
                    player.sendMessage(
                            MessageData.pluginName + MessageData.hungerRestoredPlayer(staff.getName()));
                }
            });
        } else {
            staff.sendMessage(
                    MessageData.pluginName + MessageData.hungerNotOnline(offlinePlayer.getName()));
        }
    }

    public void restoreExperience(CommandSender staff, OfflinePlayer offlinePlayer) {
        if (offlinePlayer.isOnline()) {
            final Player player = (Player) offlinePlayer;
            final float xp = getXP();

            Bukkit.getScheduler().runTask(InventoryRollback.getInstance(), () -> {
                RestoreInventory.setTotalExperience(player, xp);

                if (SoundData.experienceEnabled) {
                    player.playSound(
                            player.getLocation(), SoundData.experience, SoundData.experienceVolume, 1);
                }

                staff.sendMessage(MessageData.pluginName + MessageData
                        .experienceRestored(player.getName(), RestoreInventory.getLevel(xp)));
                if (staff != player)
                    player.sendMessage(MessageData.pluginName + MessageData
                            .experienceRestoredPlayer(staff.getName(), (int) xp));
            });
        } else {
            staff.sendMessage(
                    MessageData.pluginName + MessageData.experienceNotOnline(offlinePlayer.getName()));
        }
    }

}
