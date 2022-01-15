package me.danjono.inventoryrollback.commands;

import com.google.common.collect.Maps;
import me.danjono.inventoryrollback.InventoryRollback;
import me.danjono.inventoryrollback.config.ConfigFile;
import me.danjono.inventoryrollback.config.MessageData;
import me.danjono.inventoryrollback.data.LogType;
import me.danjono.inventoryrollback.data.PlayerData;
import me.danjono.inventoryrollback.gui.InventoryName;
import me.danjono.inventoryrollback.gui.MainMenu;
import me.danjono.inventoryrollback.gui.RollbackListMenu;
import me.danjono.inventoryrollback.inventory.RestoreInventory;
import me.danjono.inventoryrollback.inventory.SaveInventory;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Commands extends ConfigFile implements TabExecutor {

    private static final Map<String, String> subcommands = new HashMap<>();

    static {
        subcommands.put("restore", "inventoryrollback.restore");
        subcommands.put("backups", "inventoryrollback.restore");
        subcommands.put("forcebackup", "inventoryrollback.forcebackup");
        subcommands.put("enable", "inventoryrollback.enable");
        subcommands.put("disable", "inventoryrollback.disable");
        subcommands.put("reload", "inventoryrollback.reload");
    }

    @SuppressWarnings("deprecation")
    private static OfflinePlayer getPlayer(String input) {
        try {
            UUID uuid = UUID.fromString(input);
            return Bukkit.getOfflinePlayer(uuid);
        } catch (IllegalArgumentException ignored) {
            return Bukkit.getOfflinePlayer(input);
        }
    }

    private static Map.Entry<Long, ConfigurationSection> getBackupByIndex(OfflinePlayer player, LogType logType, int index) {
        PlayerData data = new PlayerData(player, logType);
        if (!data.loadData() || data.getData().getInt("saves") < index) {
            return null;
        }
        FileConfiguration config = data.getData();
        List<Long> saves = config.getConfigurationSection("data").getKeys(false).stream()
                .map(Long::valueOf).sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        if (saves.size() < index)
            return null;
        long key = saves.get(index - 1);
        ConfigurationSection save = config.getConfigurationSection("data." + key);
        return Maps.immutableEntry(key, save);
    }

    // Usage: /ir backups <player> [type] [index]
    private static void viewBackup(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(MessageData.pluginName + MessageData.error);
            return;
        }
        OfflinePlayer player = getPlayer(args[0]);
        switch (args.length) {
            case 1: {
                // list number of saves of each LogType
                boolean hasData = false;
                for (LogType type : LogType.values()) {
                    PlayerData data = new PlayerData(player, type);
                    if (data.loadData()) {
                        hasData = true;
                        sender.sendMessage(MessageData.pluginName + type.name() + ": " + data.getData().getInt("saves"));
                    }
                }
                if (!hasData) {
                    sender.sendMessage(MessageData.pluginName + MessageData.noBackup(player.getName()));
                }
                break;
            }
            case 2: {
                // list saves of the specified LogType
                LogType logType;
                try {
                    logType = LogType.valueOf(args[1].toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException ignored) {
                    sender.sendMessage(MessageData.pluginName + MessageData.error);
                    return;
                }
                PlayerData data = new PlayerData(player, logType);
                if (!data.loadData() || data.getData().getInt("saves") == 0) {
                    sender.sendMessage(MessageData.pluginName + MessageData.noBackup(player.getName()));
                    return;
                }
                FileConfiguration config = data.getData();
                List<Long> saves = config.getConfigurationSection("data").getKeys(false).stream()
                        .map(Long::valueOf).sorted(Comparator.reverseOrder()).collect(Collectors.toList());
                int index = 1;
                for (Long key : saves) {
                    ConfigurationSection save = config.getConfigurationSection("data." + key);
                    sender.sendMessage(MessageData.pluginName + index++ + ". " +
                            (save.contains("deathReason") ? MessageData.deathReason(save.getString("deathReason")) + " " : "") +
                            MessageData.deathTime(RollbackListMenu.getTime(key)));
                }
                break;
            }
            case 3: {
                // show details
                LogType logType;
                int index;
                try {
                    logType = LogType.valueOf(args[1].toUpperCase(Locale.ROOT));
                    index = Integer.parseInt(args[2]);
                } catch (IllegalArgumentException ignored) {
                    sender.sendMessage(MessageData.pluginName + MessageData.error);
                    return;
                }

                Map.Entry<Long, ConfigurationSection> entry = getBackupByIndex(player, logType, index);
                if (entry == null) {
                    sender.sendMessage(MessageData.pluginName + MessageData.noBackup(player.getName()));
                    return;
                }
                long key = entry.getKey();
                ConfigurationSection save = entry.getValue();

                sender.sendMessage(MessageData.pluginName + InventoryName.BACKUP.getName());
                sender.sendMessage(MessageData.pluginName + MessageData.deathTime(RollbackListMenu.getTime(key)));
                if (save.contains("deathReason"))
                    sender.sendMessage(MessageData.pluginName + MessageData.deathReason(save.getString("deathReason")));

                final String world = save.getString("location.world");
                final String x = save.getString("location.x");
                final String y = save.getString("location.y");
                final String z = save.getString("location.z");
                final String location = world + "," + x + "," + y + "," + z;
                sender.sendMessage(MessageData.pluginName + "Location: " + location);
                break;
            }
        }
    }

    // Usage: /ir restore [player] [type] [index] [inventory/enderchest/health/food/experience]
    private static void doRestore(CommandSender sender, String[] args) {
        if (!sender.hasPermission("inventoryrollback.restore")) {
            sender.sendMessage(MessageData.pluginName + MessageData.noPermission);
            return;
        }
        if (!ConfigFile.enabled) {
            sender.sendMessage(MessageData.pluginName + MessageData.disabledMessage);
            return;
        }

        switch (args.length) {
            case 0:
            case 1: {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(MessageData.pluginName + MessageData.playerOnly);
                    return;
                }
                Player staff = (Player) sender;
                OfflinePlayer target = args.length == 1 ? getPlayer(args[0]) : staff;
                Inventory inv = new MainMenu(staff, target).getMenu();
                if (inv != null) {
                    staff.openInventory(inv);
                }
                break;
            }
            case 2: {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(MessageData.pluginName + MessageData.playerOnly);
                    return;
                }
                Player staff = (Player) sender;
                OfflinePlayer target = getPlayer(args[0]);
                LogType type;
                try {
                    type = LogType.valueOf(args[1].toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException ignored) {
                    sender.sendMessage(MessageData.pluginName + MessageData.error);
                    return;
                }
                Inventory inv = new RollbackListMenu(staff, target, type, 1).showBackups();
                if (inv != null) {
                    staff.openInventory(inv);
                }
                break;
            }
            case 3:
            case 4: {
                OfflinePlayer target = getPlayer(args[0]);
                LogType type;
                int index;
                try {
                    type = LogType.valueOf(args[1].toUpperCase(Locale.ROOT));
                    index = Integer.parseInt(args[2]);
                } catch (IllegalArgumentException ignored) {
                    sender.sendMessage(MessageData.pluginName + MessageData.error);
                    return;
                }
                Map.Entry<Long, ConfigurationSection> entry = getBackupByIndex(target, type, index);
                if (entry == null) {
                    sender.sendMessage(MessageData.pluginName + MessageData.noBackup(target.getName()));
                    return;
                }
                PlayerData playerData = new PlayerData(target, type, true);
                RestoreInventory restore = new RestoreInventory(playerData.getData(), entry.getKey());
                if (args.length == 3) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(MessageData.pluginName + MessageData.playerOnly);
                        return;
                    }
                    Player staff = (Player) sender;
                    Inventory inv = restore.getMenu(staff, target.getUniqueId(), type).showItems();
                    if (inv != null) {
                        staff.openInventory(inv);
                    }
                    return;
                } else {
                    switch (args[3].toLowerCase(Locale.ROOT)) {
                        case "inventory": {
                            restore.restoreInventory(sender, target);
                            break;
                        }
                        case "enderchest": {
                            restore.restoreEnderChest(sender, target);
                            break;
                        }
                        case "health": {
                            restore.restoreHealth(sender, target);
                            break;
                        }
                        case "food": {
                            restore.restoreFood(sender, target);
                            break;
                        }
                        case "experience": {
                            restore.restoreExperience(sender, target);
                            break;
                        }
                    }
                }
                break;
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String Label, String[] args) {
        if (args.length == 0) {
            //Give version information
            sender.sendMessage(
                    MessageData.pluginName + "Server is running v" + InventoryRollback.getPluginVersion()
                            + " - Created by danjono");
            return true;
        } else {
            switch (args[0].toLowerCase()) {
                case "restore": {
                    doRestore(sender, Arrays.copyOfRange(args, 1, args.length));
                    break;
                }
                case "backups": {
                    viewBackup(sender, Arrays.copyOfRange(args, 1, args.length));
                    break;
                }
                case "forcebackup": {
                    if (sender.hasPermission("inventoryrollback.forcebackup")) {
                        if (args.length == 1 || args.length > 2) {
                            sender.sendMessage(MessageData.pluginName + MessageData.error);
                            break;
                        }
                        OfflinePlayer offlinePlayer = getPlayer(args[1]);

                        if (!offlinePlayer.isOnline()) {
                            sender.sendMessage(
                                    MessageData.pluginName + MessageData.notOnline(offlinePlayer.getName()));
                            break;
                        }
                        final Player player = (Player) offlinePlayer;
                        new SaveInventory(player, LogType.FORCE, null, player.getInventory(), player
                                .getEnderChest()).saveToDiskAsync().thenAccept(unused -> sender.sendMessage(
                                MessageData.pluginName + MessageData.forceSaved(offlinePlayer.getName())));

                        break;
                    } else {
                        sender.sendMessage(MessageData.pluginName + MessageData.noPermission);
                    }
                    break;
                }
                case "enable": {
                    if (sender.hasPermission("InventoryRollback.enable")) {
                        setEnabled(true);
                        saveConfig();

                        sender.sendMessage(MessageData.pluginName + MessageData.enabledMessage);
                    } else {
                        sender.sendMessage(MessageData.pluginName + MessageData.noPermission);
                    }
                    break;
                }
                case "disable": {
                    if (sender.hasPermission("InventoryRollback.disable")) {
                        setEnabled(false);
                        saveConfig();

                        sender.sendMessage(MessageData.pluginName + MessageData.disabledMessage);
                    } else {
                        sender.sendMessage(MessageData.pluginName + MessageData.noPermission);
                    }
                    break;
                }
                case "reload": {
                    if (sender.hasPermission("InventoryRollback.reload")) {
                        InventoryRollback.startupTasks();

                        sender.sendMessage(MessageData.pluginName + MessageData.reloadMessage);
                    } else {
                        sender.sendMessage(MessageData.pluginName + MessageData.noPermission);
                    }
                    break;
                }
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        final String arg0;

        switch (args.length) {
            case 0:
                return subcommands.entrySet().stream().filter(entry -> sender.hasPermission(entry.getValue()))
                        .map(Map.Entry::getKey).sorted(Comparator.reverseOrder()).collect(Collectors.toList());
            case 1:
                arg0 = args[0].toLowerCase();
                return subcommands.entrySet().stream().filter(entry -> sender.hasPermission(entry.getValue()))
                        .map(Map.Entry::getKey)
                        .filter((s) -> s.toLowerCase().startsWith(arg0) || s.equalsIgnoreCase(arg0))
                        .sorted(Comparator.reverseOrder()).collect(Collectors.toList());
            case 2:
                arg0 = args[0];
                if (arg0.equalsIgnoreCase("restore") || arg0.equalsIgnoreCase("backups") ||
                        arg0.equalsIgnoreCase("forcebackup")) {
                    if (!sender.hasPermission(subcommands.get(arg0))) {
                        return Collections.emptyList();
                    }
                    final String arg1 = args[1].toLowerCase();
                    return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                            .filter((s) -> s.toLowerCase().startsWith(arg1) || s.equalsIgnoreCase(arg1))
                            .sorted(Comparator.reverseOrder()).collect(Collectors.toList());
                }
                break;
            case 3:
                arg0 = args[0];
                if (arg0.equalsIgnoreCase("restore") || arg0.equalsIgnoreCase("backups")) {
                    if (!sender.hasPermission(subcommands.get(arg0))) {
                        return Collections.emptyList();
                    }
                    final String arg2 = args[2];
                    return Arrays.stream(LogType.values())
                            .map(Enum::name)
                            .filter(logType -> logType.startsWith(arg2))
                            .collect(Collectors.toList());
                }
                break;
            // case 4 is backup index
            case 5:
                arg0 = args[0];
                if (arg0.equalsIgnoreCase("restore")) {
                    if (!sender.hasPermission(subcommands.get(arg0))) {
                        return Collections.emptyList();
                    }
                    return Stream.of("inventory", "experience", "enderchest", "health", "food")
                            .filter(arg -> arg.startsWith(args[4]))
                            .collect(Collectors.toList());
                }
                break;
        }
        return Collections.emptyList();
    }

}