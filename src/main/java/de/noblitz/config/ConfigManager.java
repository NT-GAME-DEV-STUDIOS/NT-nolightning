package de.noblitz.config;

import de.noblitz.NoBlitzPlugin;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.weather.LightningStrikeEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigManager {

    public enum WorldMode {
        ALL,
        WHITELIST,
        BLACKLIST
    }

    private final NoBlitzPlugin plugin;

    private boolean enabled;
    private boolean debug;

    private boolean cancelAllLightning;
    private final Map<LightningStrikeEvent.Cause, Boolean> causeFilters = new EnumMap<>(LightningStrikeEvent.Cause.class);
    private boolean visualOnly;

    private boolean preventFire;
    private boolean preventEntityDamage;
    private boolean preventItemDestruction;
    private boolean preventMobTransformations;
    private boolean muteSound;

    private boolean preventThunderstorms;
    private boolean alwaysClearWeather;

    private WorldMode worldMode = WorldMode.ALL;
    private final Set<String> configuredWorlds = new HashSet<>();
    private final Map<String, Boolean> runtimeOverrides = new ConcurrentHashMap<>();

    private String prefix;
    private final Map<String, String> messages = new HashMap<>();

    public ConfigManager(NoBlitzPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        this.enabled = config.getBoolean("enabled", true);
        this.debug = config.getBoolean("debug", false);

        // Strike rules
        this.cancelAllLightning = config.getBoolean("strike-rules.cancel-all-lightning", true);
        this.visualOnly = config.getBoolean("strike-rules.visual-only", false);

        causeFilters.clear();
        causeFilters.put(LightningStrikeEvent.Cause.WEATHER, config.getBoolean("strike-rules.cancel-causes.weather", true));
        causeFilters.put(LightningStrikeEvent.Cause.TRIDENT, config.getBoolean("strike-rules.cancel-causes.trident", false));
        causeFilters.put(LightningStrikeEvent.Cause.COMMAND, config.getBoolean("strike-rules.cancel-causes.command", false));
        causeFilters.put(LightningStrikeEvent.Cause.CUSTOM, config.getBoolean("strike-rules.cancel-causes.custom", false));

        // Protections
        this.preventFire = config.getBoolean("protection.prevent-fire", true);
        this.preventEntityDamage = config.getBoolean("protection.prevent-entity-damage", true);
        this.preventItemDestruction = config.getBoolean("protection.prevent-item-destruction", true);
        this.preventMobTransformations = config.getBoolean("protection.prevent-mob-transformations", true);
        this.muteSound = config.getBoolean("protection.mute-sound", false);

        // Weather
        this.preventThunderstorms = config.getBoolean("weather.prevent-thunderstorms", false);
        this.alwaysClearWeather = config.getBoolean("weather.always-clear-weather", false);

        // Worlds
        String modeStr = config.getString("worlds.mode", "ALL").toUpperCase(Locale.ROOT);
        try {
            this.worldMode = WorldMode.valueOf(modeStr);
        } catch (IllegalArgumentException ex) {
            this.worldMode = WorldMode.ALL;
        }

        this.configuredWorlds.clear();
        List<String> list = config.getStringList("worlds.list");
        for (String w : list) {
            this.configuredWorlds.add(w.toLowerCase(Locale.ROOT));
        }

        // Messages
        this.prefix = config.getString("messages.prefix", "&8[&bNoBlitz&8] &r");
        this.messages.clear();
        if (config.isConfigurationSection("messages")) {
            for (String key : config.getConfigurationSection("messages").getKeys(false)) {
                messages.put(key, config.getString("messages." + key, ""));
            }
        }
    }

    public boolean isWorldProtected(World world) {
        if (!enabled || world == null) {
            return false;
        }
        String worldName = world.getName().toLowerCase(Locale.ROOT);

        // Check runtime override first
        Boolean override = runtimeOverrides.get(worldName);
        if (override != null) {
            return override;
        }

        return switch (worldMode) {
            case ALL -> true;
            case WHITELIST -> configuredWorlds.contains(worldName);
            case BLACKLIST -> !configuredWorlds.contains(worldName);
        };
    }

    public boolean toggleWorld(String worldName) {
        String key = worldName.toLowerCase(Locale.ROOT);
        boolean current;
        if (runtimeOverrides.containsKey(key)) {
            current = runtimeOverrides.get(key);
        } else {
            current = switch (worldMode) {
                case ALL -> true;
                case WHITELIST -> configuredWorlds.contains(key);
                case BLACKLIST -> !configuredWorlds.contains(key);
            };
        }
        boolean newState = !current;
        runtimeOverrides.put(key, newState);
        return newState;
    }

    public boolean isCancelCause(LightningStrikeEvent.Cause cause) {
        if (cancelAllLightning) {
            return true;
        }
        if (cause == null) {
            return true;
        }
        return causeFilters.getOrDefault(cause, false);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isCancelAllLightning() {
        return cancelAllLightning;
    }

    public boolean isVisualOnly() {
        return visualOnly;
    }

    public boolean isPreventFire() {
        return preventFire;
    }

    public boolean isPreventEntityDamage() {
        return preventEntityDamage;
    }

    public boolean isPreventItemDestruction() {
        return preventItemDestruction;
    }

    public boolean isPreventMobTransformations() {
        return preventMobTransformations;
    }

    public boolean isMuteSound() {
        return muteSound;
    }

    public boolean isPreventThunderstorms() {
        return preventThunderstorms;
    }

    public boolean isAlwaysClearWeather() {
        return alwaysClearWeather;
    }

    public WorldMode getWorldMode() {
        return worldMode;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getMessage(String key, String def) {
        return messages.getOrDefault(key, def);
    }

    public Map<String, Boolean> getRuntimeOverrides() {
        return runtimeOverrides;
    }
}
