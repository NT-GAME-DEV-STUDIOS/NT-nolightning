package de.noblitz.config;

import de.noblitz.NoBlitzPlugin;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

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
    private String language = "en";

    private boolean cancelAllLightning;
    private final Map<String, Boolean> causeFilters = new HashMap<String, Boolean>();
    private boolean visualOnly;

    private boolean preventFire;
    private boolean preventEntityDamage;
    private boolean preventItemDestruction;
    private boolean preventMobTransformations;
    private boolean muteSound;

    private boolean preventThunderstorms;
    private boolean alwaysClearWeather;

    private WorldMode worldMode = WorldMode.ALL;
    private final Set<String> configuredWorlds = new HashSet<String>();
    private final Map<String, Boolean> runtimeOverrides = new ConcurrentHashMap<String, Boolean>();

    private String prefix;
    private final Map<String, String> messages = new HashMap<String, String>();

    public ConfigManager(NoBlitzPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        this.enabled = config.getBoolean("enabled", true);
        this.debug = config.getBoolean("debug", false);
        this.language = config.getString("language", "en").toLowerCase(Locale.ROOT);
        if (!"de".equals(language) && !"en".equals(language)) {
            this.language = "en";
        }

        // Strike rules
        this.cancelAllLightning = config.getBoolean("strike-rules.cancel-all-lightning", true);
        this.visualOnly = config.getBoolean("strike-rules.visual-only", false);

        causeFilters.clear();
        causeFilters.put("WEATHER", config.getBoolean("strike-rules.cancel-causes.weather", true));
        causeFilters.put("TRIDENT", config.getBoolean("strike-rules.cancel-causes.trident", false));
        causeFilters.put("COMMAND", config.getBoolean("strike-rules.cancel-causes.command", false));
        causeFilters.put("CUSTOM", config.getBoolean("strike-rules.cancel-causes.custom", false));

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

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String lang) {
        String clean = (lang != null && lang.equalsIgnoreCase("de")) ? "de" : "en";
        this.language = clean;
        plugin.getConfig().set("language", clean);
        plugin.saveConfig();
        load();
    }

    public boolean isWorldProtected(World world) {
        if (!enabled || world == null) {
            return false;
        }

        String name = world.getName().toLowerCase(Locale.ROOT);
        Boolean override = runtimeOverrides.get(name);
        if (override != null) {
            return override;
        }

        switch (worldMode) {
            case ALL:
                return true;
            case WHITELIST:
                return configuredWorlds.contains(name);
            case BLACKLIST:
                return !configuredWorlds.contains(name);
            default:
                return true;
        }
    }

    public boolean toggleWorld(String worldName) {
        String name = worldName.toLowerCase(Locale.ROOT);
        World w = plugin.getServer().getWorld(worldName);
        boolean current = isWorldProtected(w);
        boolean newState = !current;
        runtimeOverrides.put(name, newState);
        return newState;
    }

    public boolean shouldCancelCause(String causeName) {
        if (cancelAllLightning) {
            return true;
        }
        Boolean shouldCancel = causeFilters.get(causeName.toUpperCase(Locale.ROOT));
        return shouldCancel != null && shouldCancel;
    }

    public boolean isCancelCause(String causeName) {
        return shouldCancelCause(causeName);
    }

    public String getMessage(String key, String fallback) {
        if (messages.containsKey(key)) {
            String val = messages.get(key);
            if (val != null && !val.isEmpty()) return val;
        }
        return getDefaultMessage(key, fallback);
    }

    private String getDefaultMessage(String key, String fallback) {
        boolean isDe = "de".equalsIgnoreCase(language);
        if ("no-permission".equals(key)) return isDe ? "&cDu hast keine Berechtigung für diesen Befehl!" : "&cYou do not have permission to execute this command!";
        if ("reload-success".equals(key)) return isDe ? "&aKonfiguration wurde erfolgreich neu geladen! &7({time}ms)" : "&aConfiguration successfully reloaded! &7({time}ms)";
        if ("toggle-enabled".equals(key)) return isDe ? "&7Blitzschutz für Welt &b{world} &awurde aktiviert&7." : "&7Lightning protection for world &b{world} &awas enabled&7.";
        if ("toggle-disabled".equals(key)) return isDe ? "&7Blitzschutz für Welt &b{world} &cwurde deaktiviert&7." : "&7Lightning protection for world &b{world} &cwas disabled&7.";
        if ("strike-success".equals(key)) return isDe ? "&7Blitz auf &b{target} &7beschworen (Visual Only: &e{visual}&7)." : "&7Lightning struck on &b{target} &7(Visual Only: &e{visual}&7).";
        if ("strike-player-not-found".equals(key)) return isDe ? "&cSpieler &e{target} &cwurde nicht gefunden." : "&cPlayer &e{target} &cwas not found.";
        if ("clearthunder-success".equals(key)) return isDe ? "&aGewitter in Welt &b{world} &aerfolgreich geklärt!" : "&aThunderstorm in world &b{world} &asuccessfully cleared!";
        if ("resetstats-success".equals(key)) return isDe ? "&aStatistiken wurden erfolgreich zurückgesetzt." : "&aStatistics were successfully reset.";
        if ("status-header".equals(key)) return isDe ? "&8=== &bNoBlitz Status &8===" : "&8=== &bNoBlitz Status &8===";
        if ("status-global".equals(key)) return isDe ? "&7Plugin-Status: {status} &7| Modus: &e{mode}" : "&7Plugin Status: {status} &7| Mode: &e{mode}";
        if ("status-world".equals(key)) return isDe ? "&7Welt &b{world}&7: {world_status}" : "&7World &b{world}&7: {world_status}";
        if ("status-stats".equals(key)) return isDe ? "&7Blockierte Blitze: &e{strikes} &7| Verhinderte Brände: &e{fires} &7| Geschützte Mobs: &e{mobs}" : "&7Blocked Strikes: &e{strikes} &7| Prevented Fires: &e{fires} &7| Protected Mobs: &e{mobs}";
        if ("lang-changed".equals(key)) return isDe ? "&aSprache erfolgreich auf &eDeutsch &ageändert!" : "&aLanguage successfully set to &eEnglish&a!";
        if ("unknown-command".equals(key)) return isDe ? "&cUnbekannter Befehl. Nutze &e/{label} help &cfür Hilfe." : "&cUnknown command. Use &e/{label} help &cfor help.";
        return fallback;
    }

    public boolean isEnabled() { return enabled; }
    public boolean isDebug() { return debug; }
    public boolean isCancelAllLightning() { return cancelAllLightning; }
    public boolean isVisualOnly() { return visualOnly; }
    public boolean isPreventFire() { return preventFire; }
    public boolean isPreventEntityDamage() { return preventEntityDamage; }
    public boolean isPreventItemDestruction() { return preventItemDestruction; }
    public boolean isPreventMobTransformations() { return preventMobTransformations; }
    public boolean isMuteSound() { return muteSound; }
    public boolean isPreventThunderstorms() { return preventThunderstorms; }
    public boolean isAlwaysClearWeather() { return alwaysClearWeather; }
    public WorldMode getWorldMode() { return worldMode; }
    public String getPrefix() { return prefix; }
}
