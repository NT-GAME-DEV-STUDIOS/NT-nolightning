package de.noblitz.listeners;

import de.noblitz.NoBlitzPlugin;
import de.noblitz.config.ConfigManager;
import de.noblitz.stats.StatsManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.CreeperPowerEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.PigZapEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class LightningListener implements Listener {

    private final NoBlitzPlugin plugin;
    private final ConfigManager config;
    private final StatsManager stats;

    public LightningListener(NoBlitzPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.stats = plugin.getStatsManager();
    }

    /**
     * Intercept and cancel or convert lightning strikes.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLightningStrike(LightningStrikeEvent event) {
        World world = event.getWorld();
        if (!config.isWorldProtected(world)) {
            return;
        }

        LightningStrike lightning = event.getLightning();
        LightningStrikeEvent.Cause cause = event.getCause();

        if (config.isMuteSound()) {
            lightning.setSilent(true);
        }

        if (config.isCancelCause(cause)) {
            event.setCancelled(true);
            stats.incrementBlockedStrikes();

            if (config.isVisualOnly()) {
                Location loc = lightning.getLocation();
                world.strikeLightningEffect(loc);
                if (config.isDebug()) {
                    plugin.getLogger().info("[Debug] Converted lightning strike at " + loc + " to visual-only effect (Cause: " + cause + ").");
                }
            } else {
                if (config.isDebug()) {
                    plugin.getLogger().info("[Debug] Blocked lightning strike at " + lightning.getLocation() + " (Cause: " + cause + ").");
                }
            }
        }
    }

    /**
     * Prevent fire ignition from lightning strikes.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getCause() == BlockIgniteEvent.IgniteCause.LIGHTNING) {
            World world = event.getBlock().getWorld();
            if (config.isWorldProtected(world) && config.isPreventFire()) {
                event.setCancelled(true);
                stats.incrementPreventedFires();
                if (config.isDebug()) {
                    plugin.getLogger().info("[Debug] Prevented lightning fire ignition at " + event.getBlock().getLocation());
                }
            }
        }
    }

    /**
     * Prevent lightning damage to players, mobs, and items.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof LightningStrike) {
            Entity entity = event.getEntity();
            World world = entity.getWorld();

            if (!config.isWorldProtected(world)) {
                return;
            }

            boolean isItem = (entity instanceof Item || entity instanceof ItemFrame || entity instanceof GlowItemFrame);

            if (isItem && config.isPreventItemDestruction()) {
                event.setCancelled(true);
                stats.incrementProtectedEntities();
                return;
            }

            if (config.isPreventEntityDamage()) {
                event.setCancelled(true);
                stats.incrementProtectedEntities();
                if (config.isDebug()) {
                    plugin.getLogger().info("[Debug] Prevented lightning damage to " + entity.getType() + " at " + entity.getLocation());
                }
            }
        }
    }

    /**
     * Prevent Creeper from becoming a Charged Creeper via lightning.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCreeperPower(CreeperPowerEvent event) {
        if (event.getCause() == CreeperPowerEvent.PowerCause.LIGHTNING) {
            World world = event.getEntity().getWorld();
            if (config.isWorldProtected(world) && config.isPreventMobTransformations()) {
                event.setCancelled(true);
                stats.incrementPreventedTransformations();
                if (config.isDebug()) {
                    plugin.getLogger().info("[Debug] Prevented Creeper charging at " + event.getEntity().getLocation());
                }
            }
        }
    }

    /**
     * Prevent entities from transforming when struck by lightning (Villager -> Witch, Mooshroom, etc.).
     */
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

    /**
     * Prevent Pig from turning into a Zombified Piglin.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPigZap(PigZapEvent event) {
        World world = event.getEntity().getWorld();
        if (config.isWorldProtected(world) && config.isPreventMobTransformations()) {
            event.setCancelled(true);
            stats.incrementPreventedTransformations();
            if (config.isDebug()) {
                plugin.getLogger().info("[Debug] Prevented PigZap at " + event.getEntity().getLocation());
            }
        }
    }

    /**
     * Automatically cancel thunderstorm weather if enabled.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onThunderChange(ThunderChangeEvent event) {
        if (event.toThunderState()) {
            World world = event.getWorld();
            if (config.isWorldProtected(world) && config.isPreventThunderstorms()) {
                event.setCancelled(true);
                if (config.isDebug()) {
                    plugin.getLogger().info("[Debug] Prevented thunderstorm in world " + world.getName());
                }
            }
        }
    }

    /**
     * Keep weather permanently clear if enabled.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onWeatherChange(WeatherChangeEvent event) {
        if (event.toWeatherState()) {
            World world = event.getWorld();
            if (config.isWorldProtected(world) && config.isAlwaysClearWeather()) {
                event.setCancelled(true);
                if (config.isDebug()) {
                    plugin.getLogger().info("[Debug] Prevented rain/storm in world " + world.getName());
                }
            }
        }
    }
}
