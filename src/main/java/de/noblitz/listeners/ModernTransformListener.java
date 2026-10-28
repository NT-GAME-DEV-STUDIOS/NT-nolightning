package de.noblitz.listeners;

import de.noblitz.NoBlitzPlugin;
import de.noblitz.config.ConfigManager;
import de.noblitz.stats.StatsManager;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTransformEvent;

public class ModernTransformListener implements Listener {

    private final NoBlitzPlugin plugin;
    private final ConfigManager config;
    private final StatsManager stats;

    public ModernTransformListener(NoBlitzPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.stats = plugin.getStatsManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityTransform(EntityTransformEvent event) {
        if (event.getTransformReason() == EntityTransformEvent.TransformReason.LIGHTNING) {
            World world = event.getEntity().getWorld();
            if (config.isWorldProtected(world) && config.isPreventMobTransformations()) {
                event.setCancelled(true);
                stats.incrementPreventedTransformations();
                if (config.isDebug()) {
                    plugin.getLogger().info("[Debug] Prevented entity transformation for " + event.getEntity().getType() + " at " + event.getEntity().getLocation());
                }
            }
        }
    }
}
