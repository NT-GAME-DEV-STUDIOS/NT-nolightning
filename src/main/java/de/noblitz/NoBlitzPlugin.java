package de.noblitz;

import de.noblitz.commands.NoBlitzCommand;
import de.noblitz.config.ConfigManager;
import de.noblitz.listeners.LightningListener;
import de.noblitz.stats.StatsManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class NoBlitzPlugin extends JavaPlugin {

    private static NoBlitzPlugin instance;

    private ConfigManager configManager;
    private StatsManager statsManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize managers
        this.statsManager = new StatsManager();
        this.configManager = new ConfigManager(this);
        this.configManager.load();

        // Register event listener
        getServer().getPluginManager().registerEvents(new LightningListener(this), this);

        // Register command
        PluginCommand cmd = getCommand("noblitz");
        if (cmd != null) {
            NoBlitzCommand executor = new NoBlitzCommand(this);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        } else {
            getLogger().severe("Could not register command 'noblitz'! Please check plugin.yml.");
        }

        getLogger().info("=========================================");
        getLogger().info(" NoBlitz v" + getPluginMeta().getVersion() + " wurde erfolgreich aktiviert!");
        getLogger().info(" Blitz- & Gewitterschutz ist aktiv.");
        getLogger().info(" Modus: " + configManager.getWorldMode());
        getLogger().info("=========================================");
    }

    @Override
    public void onDisable() {
        getLogger().info("NoBlitz v" + getPluginMeta().getVersion() + " wurde deaktiviert. Auf Wiedersehen!");
        instance = null;
    }

    public static NoBlitzPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }
}
