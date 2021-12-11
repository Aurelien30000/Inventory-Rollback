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

    private static String packageVersion;

    public static InventoryRollback getInstance() {
        return instance;
    }

    public static String getPluginVersion() {
        return instance.getDescription().getVersion();
    }

    public static String getPackageVersion() {
        return packageVersion;
    }

    private IOpenInv openInvAPI;

    public IOpenInv getOpenInvAPI() {
        return openInvAPI;
    }

    @Override
    public void onEnable() {
        instance = this;
        packageVersion = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];

        if (!isCompatible()) {
            logger.log(Level.WARNING, ChatColor.RED + " ** WARNING... Plugin may not be compatible with this version of Minecraft. **");
            logger.log(Level.WARNING, ChatColor.RED + " ** Tested versions: 1.8.8 to 1.18 **");
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

    private enum CompatibleVersions {
        v1_8_R1,
        v1_8_R2,
        v1_8_R3,
        v1_9_R1,
        v1_9_R2,
        v1_10_R1,
        v1_11_R1,
        v1_12_R1,
        v1_13_R1,
        v1_13_R2,
        v1_14_R1,
        v1_15_R1,
        v1_16_R1,
        v1_16_R2,
        v1_16_R3,
        v1_17_R1,
        v1_18_R1;
    }

    public enum VersionName {
        v1_8,
        v1_9_v1_12,
        v1_13_v1_16,
        v1_17,
        v1_18_PLUS;

        public boolean greaterThanOrEqualTo(VersionName versionName) {
            return this.ordinal() >= versionName.ordinal();
        }

    }

    private static VersionName version = VersionName.v1_18_PLUS;

    public static VersionName getVersion() {
        return version;
    }

    private boolean isCompatible() {
        for (CompatibleVersions v : CompatibleVersions.values()) {
            if (v.name().equalsIgnoreCase(packageVersion)) {
                if (v.name().contains("v1_8")) {
                    version = VersionName.v1_8;
                } else if (v.name().contains("v1_9")
                        || v.name().contains("v1_10")
                        || v.name().contains("v1_11")
                        || v.name().contains("v1_12")) {
                    version = VersionName.v1_9_v1_12;
                } else if (v.name().contains("v1_13")
                        || v.name().contains("v1_14")
                        || v.name().contains("v1_15")
                        || v.name().contains("v1_16")) {
                    version = VersionName.v1_13_v1_16;
                } else if (v.name().contains("v1_17")) {
                    version = VersionName.v1_17;
                }
                return true;
            }
        }

        return false;
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
