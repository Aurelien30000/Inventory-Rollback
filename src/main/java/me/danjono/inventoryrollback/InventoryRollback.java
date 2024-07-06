package me.danjono.inventoryrollback;

import com.lishid.openinv.IOpenInv;
import me.danjono.inventoryrollback.UpdateChecker.UpdateResult;
import me.danjono.inventoryrollback.commands.Commands;
import me.danjono.inventoryrollback.config.ConfigFile;
import me.danjono.inventoryrollback.listeners.ClickGUI;
import me.danjono.inventoryrollback.listeners.EventLogs;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;
import java.util.logging.Logger;

public class InventoryRollback extends JavaPlugin {

    public static final Logger logger = Logger.getLogger("InventoryRollback");
    private static InventoryRollback instance;

    private static String gameVersion;

    public static InventoryRollback getInstance() {
        return instance;
    }

    public static String getPluginVersion() {
        return instance.getDescription().getVersion();
    }

    public static String getGameVersion() {
        return gameVersion;
    }

    private IOpenInv openInvAPI;

    public IOpenInv getOpenInvAPI() {
        return openInvAPI;
    }

    @Override
    public void onEnable() {
        instance = this;
        gameVersion = Bukkit.getMinecraftVersion();

        if (!isCompatible()) {
            logger.log(Level.WARNING, ChatColor.RED + " ** WARNING... Plugin may not be compatible with this version of Minecraft. **");
            logger.log(Level.WARNING, ChatColor.RED + " ** Tested versions: 1.17.1 to 1.20.4 **");
            logger.log(Level.WARNING, ChatColor.RED + " ** Please fully test the plugin before using on your server as features may be broken. **");
        }

        startupTasks();

        if (ConfigFile.openInvEnabled && Bukkit.getPluginManager().isPluginEnabled("OpenInv")) {
            openInvAPI = (IOpenInv) Bukkit.getPluginManager().getPlugin("OpenInv");
            logger.log(Level.INFO, ChatColor.GREEN + "Enabled OpenInv integration.");
        }

        if (ConfigFile.bStatsEnabled) {
            bStats();
        }

        this.getCommand("inventoryrollback").setExecutor(new Commands());

        this.getServer().getPluginManager().registerEvents(new ClickGUI(), instance);
        this.getServer().getPluginManager().registerEvents(new EventLogs(), instance);
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    public static void startupTasks() {
        ConfigFile config = new ConfigFile();

        config.setVariables();
        config.createStorageFolders();

        checkUpdate(ConfigFile.updateChecker);
    }

    public enum VersionName {
        v1_17_1,
        v1_18_0,
        v1_18_1,
        v1_18_2,
        v1_19_0,
        v1_19_1,
        v1_19_2,
        v1_19_3,
        v1_19_4,
        v1_20_0,
        v1_20_1,
        v1_20_2,
        v1_20_3,
        v1_20_4,
        v1_20_5,
        v1_20_6,
        v1_21;

        public boolean isAtLeast(VersionName versionName) {
            return this.ordinal() >= versionName.ordinal();
        }

        public boolean between(VersionName versionName1, VersionName versionName2) {
            return versionName1.ordinal() <= this.ordinal() && this.ordinal() <= versionName2.ordinal();
        }
    }

    private static VersionName VERSION = VersionName.v1_21;

    public static VersionName getVersion() {
        return VERSION;
    }

    private boolean isCompatible() {
        try {
            VERSION = VersionName.valueOf("v" + gameVersion.replace(".", "_"));
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @SuppressWarnings("unused")
    private void bStats() {
        final Metrics metrics = new Metrics(this, 1666);
    }

    public static void checkUpdate(boolean enabled) {
        if (!enabled)
            return;

        logger.log(Level.INFO, "Checking for updates...");

        final UpdateResult result = new me.danjono.inventoryrollback.UpdateChecker(instance, 48074, true).getResult();

        switch (result) {
            case FAIL_SPIGOT: {
                logger.log(Level.INFO, "Could not contact Spigot.");
                break;
            }
            case UPDATE_AVAILABLE: {
                logger.log(Level.INFO, ChatColor.AQUA + "===============================================================================");
                logger.log(Level.INFO, ChatColor.AQUA + "An update to InventoryRollback is available!");
                logger.log(Level.INFO, ChatColor.AQUA + "Download at https://www.spigotmc.org/resources/inventoryrollback.48074/");
                logger.log(Level.INFO, ChatColor.AQUA + "===============================================================================");
                break;
            }
            case NO_UPDATE: {
                logger.log(Level.INFO, ChatColor.AQUA + "You are running the latest version.");
                break;
            }
            default: {
                break;
            }
        }
    }

}
