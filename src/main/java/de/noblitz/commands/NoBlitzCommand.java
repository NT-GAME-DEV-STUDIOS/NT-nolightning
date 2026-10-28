package de.noblitz.commands;

import de.noblitz.NoBlitzPlugin;
import de.noblitz.config.ConfigManager;
import de.noblitz.stats.StatsManager;
import de.noblitz.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class NoBlitzCommand implements TabExecutor {

    private final NoBlitzPlugin plugin;
    private final ConfigManager config;
    private final StatsManager stats;

    public NoBlitzCommand(NoBlitzPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.stats = plugin.getStatsManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender, label);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        switch (sub) {
            case "status":
                handleStatus(sender);
                break;
            case "reload":
                handleReload(sender);
                break;
            case "toggle":
                handleToggle(sender, args);
                break;
            case "strike":
                handleStrike(sender, args);
                break;
            case "clearthunder":
            case "clear":
                handleClearThunder(sender, args);
                break;
            case "resetstats":
                handleResetStats(sender);
                break;
            default:
                MessageUtils.sendPrefixed(sender, config.getPrefix(), "&cUnbekannter Befehl. Nutze &e/" + label + " help &cfür Hilfe.");
                break;
        }
        return true;
    }

    private void sendHelp(CommandSender sender, String label) {
        MessageUtils.send(sender, "&8&m-----------------&r &bNoBlitz &8v" + plugin.getDescription().getVersion() + " &8&m-----------------");
        MessageUtils.send(sender, "&b/" + label + " help &7- Zeigt diese Hilfeseite");
        MessageUtils.send(sender, "&b/" + label + " status &7- Zeigt Plugin-Status und Statistiken");
        if (sender.hasPermission("noblitz.reload")) {
            MessageUtils.send(sender, "&b/" + label + " reload &7- Lädt die Konfiguration neu");
        }
        if (sender.hasPermission("noblitz.toggle")) {
            MessageUtils.send(sender, "&b/" + label + " toggle [welt] &7- Schaltet Blitzschutz für eine Welt um");
        }
        if (sender.hasPermission("noblitz.strike")) {
            MessageUtils.send(sender, "&b/" + label + " strike <spieler> [visual:true/false] &7- Beschwört einen Blitz");
        }
        if (sender.hasPermission("noblitz.clearthunder")) {
            MessageUtils.send(sender, "&b/" + label + " clearthunder [welt] &7- Beendet Gewitter und Sturm");
        }
        if (sender.hasPermission("noblitz.resetstats")) {
            MessageUtils.send(sender, "&b/" + label + " resetstats &7- Setzt Blockierungs-Statistiken zurück");
        }
        MessageUtils.send(sender, "&8&m---------------------------------------------------");
    }

    private void handleStatus(CommandSender sender) {
        if (!sender.hasPermission("noblitz.status") && !sender.hasPermission("noblitz.use")) {
            sendNoPerm(sender);
            return;
        }

        World senderWorld = (sender instanceof Player) ? ((Player) sender).getWorld() : Bukkit.getWorlds().get(0);
        boolean isProtected = config.isWorldProtected(senderWorld);

        MessageUtils.send(sender, config.getMessage("status-header", "&8=== &bNoBlitz Status &8==="));

        String globalStatus = config.isEnabled() ? "&aAktiv" : "&cDeaktiviert";
        String worldStatus = isProtected ? "&aGeschützt" : "&cUngeschützt";

        String msgGlobal = config.getMessage("status-global", "&7Plugin-Status: {status} &7| Modus: &e{mode}")
                .replace("{status}", globalStatus)
                .replace("{mode}", config.getWorldMode().name());
        MessageUtils.send(sender, msgGlobal);

        String msgWorld = config.getMessage("status-world", "&7Welt &b{world}&7: {world_status}")
                .replace("{world}", senderWorld.getName())
                .replace("{world_status}", worldStatus);
        MessageUtils.send(sender, msgWorld);

        MessageUtils.send(sender, "&7Regeln: &7Alle Blitze blockieren: &e" + config.isCancelAllLightning() +
                " &7| Nur Visual: &e" + config.isVisualOnly() +
                " &7| Feuer verhindern: &e" + config.isPreventFire() +
                " &7| Schaden verhindern: &e" + config.isPreventEntityDamage());

        String msgStats = config.getMessage("status-stats", "&7Blockierte Blitze: &e{strikes} &7| Verhinderte Brände: &e{fires} &7| Geschützte Mobs: &e{mobs}")
                .replace("{strikes}", String.valueOf(stats.getBlockedStrikes()))
                .replace("{fires}", String.valueOf(stats.getPreventedFires()))
                .replace("{mobs}", String.valueOf(stats.getProtectedEntities() + stats.getPreventedTransformations()));
        MessageUtils.send(sender, msgStats);
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("noblitz.reload")) {
            sendNoPerm(sender);
            return;
        }

        long start = System.currentTimeMillis();
        config.load();
        long diff = System.currentTimeMillis() - start;

        String msg = config.getMessage("reload-success", "&aKonfiguration wurde erfolgreich neu geladen! &7({time}ms)")
                .replace("{time}", String.valueOf(diff));
        MessageUtils.sendPrefixed(sender, config.getPrefix(), msg);
    }

    private void handleToggle(CommandSender sender, String[] args) {
        if (!sender.hasPermission("noblitz.toggle")) {
            sendNoPerm(sender);
            return;
        }

        World targetWorld;
        if (args.length >= 2) {
            targetWorld = Bukkit.getWorld(args[1]);
            if (targetWorld == null) {
                MessageUtils.sendPrefixed(sender, config.getPrefix(), "&cWelt &e" + args[1] + " &cwurde nicht gefunden!");
                return;
            }
        } else if (sender instanceof Player) {
            targetWorld = ((Player) sender).getWorld();
        } else {
            MessageUtils.sendPrefixed(sender, config.getPrefix(), "&cBitte gib eine Welt an: /noblitz toggle <welt>");
            return;
        }

        boolean newState = config.toggleWorld(targetWorld.getName());
        String msgKey = newState ? "toggle-enabled" : "toggle-disabled";
        String def = newState ? "&7Blitzschutz für Welt &b{world} &awurde aktiviert&7." : "&7Blitzschutz für Welt &b{world} &cwurde deaktiviert&7.";

        String msg = config.getMessage(msgKey, def).replace("{world}", targetWorld.getName());
        MessageUtils.sendPrefixed(sender, config.getPrefix(), msg);
    }

    private void handleStrike(CommandSender sender, String[] args) {
        if (!sender.hasPermission("noblitz.strike")) {
            sendNoPerm(sender);
            return;
        }

        if (args.length < 2) {
            MessageUtils.sendPrefixed(sender, config.getPrefix(), "&cVerwendung: /noblitz strike <spieler> [visual:true/false]");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            String msg = config.getMessage("strike-player-not-found", "&cSpieler &e{target} &cwurde nicht gefunden.")
                    .replace("{target}", args[1]);
            MessageUtils.sendPrefixed(sender, config.getPrefix(), msg);
            return;
        }

        boolean visualOnly = false;
        if (args.length >= 3) {
            visualOnly = Boolean.parseBoolean(args[2]);
        }

        if (visualOnly) {
            target.getWorld().strikeLightningEffect(target.getLocation());
        } else {
            target.getWorld().strikeLightning(target.getLocation());
        }

        String msg = config.getMessage("strike-success", "&7Blitz auf &b{target} &7beschworen (Visual Only: &e{visual}&7).")
                .replace("{target}", target.getName())
                .replace("{visual}", String.valueOf(visualOnly));
        MessageUtils.sendPrefixed(sender, config.getPrefix(), msg);
    }

    private void handleClearThunder(CommandSender sender, String[] args) {
        if (!sender.hasPermission("noblitz.clearthunder")) {
            sendNoPerm(sender);
            return;
        }

        World targetWorld;
        if (args.length >= 2) {
            targetWorld = Bukkit.getWorld(args[1]);
            if (targetWorld == null) {
                MessageUtils.sendPrefixed(sender, config.getPrefix(), "&cWelt &e" + args[1] + " &cwurde nicht gefunden!");
                return;
            }
        } else if (sender instanceof Player) {
            targetWorld = ((Player) sender).getWorld();
        } else {
            targetWorld = Bukkit.getWorlds().get(0);
        }

        targetWorld.setThundering(false);
        targetWorld.setStorm(false);
        targetWorld.setWeatherDuration(6000);

        String msg = config.getMessage("clearthunder-success", "&aGewitter in Welt &b{world} &aerfolgreich geklärt!")
                .replace("{world}", targetWorld.getName());
        MessageUtils.sendPrefixed(sender, config.getPrefix(), msg);
    }

    private void handleResetStats(CommandSender sender) {
        if (!sender.hasPermission("noblitz.resetstats")) {
            sendNoPerm(sender);
            return;
        }

        stats.reset();
        String msg = config.getMessage("resetstats-success", "&aStatistiken wurden erfolgreich zurückgesetzt.");
        MessageUtils.sendPrefixed(sender, config.getPrefix(), msg);
    }

    private void sendNoPerm(CommandSender sender) {
        String msg = config.getMessage("no-permission", "&cDu hast keine Berechtigung für diesen Befehl!");
        MessageUtils.sendPrefixed(sender, config.getPrefix(), msg);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> sub = new ArrayList<>();
            sub.add("help");
            sub.add("status");
            if (sender.hasPermission("noblitz.reload")) sub.add("reload");
            if (sender.hasPermission("noblitz.toggle")) sub.add("toggle");
            if (sender.hasPermission("noblitz.strike")) sub.add("strike");
            if (sender.hasPermission("noblitz.clearthunder")) sub.add("clearthunder");
            if (sender.hasPermission("noblitz.resetstats")) sub.add("resetstats");
            return filterStartingWith(args[0], sub);
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            if ((sub.equals("toggle") && sender.hasPermission("noblitz.toggle")) ||
                    (sub.equals("clearthunder") && sender.hasPermission("noblitz.clearthunder"))) {
                List<String> worlds = new ArrayList<>();
                for (World w : Bukkit.getWorlds()) {
                    worlds.add(w.getName());
                }
                return filterStartingWith(args[1], worlds);
            }
            if (sub.equals("strike") && sender.hasPermission("noblitz.strike")) {
                List<String> players = new ArrayList<>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    players.add(p.getName());
                }
                return filterStartingWith(args[1], players);
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("strike") && sender.hasPermission("noblitz.strike")) {
            return filterStartingWith(args[2], Arrays.asList("true", "false"));
        }

        return Collections.emptyList();
    }

    private List<String> filterStartingWith(String prefix, List<String> list) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (String item : list) {
            if (item.toLowerCase(Locale.ROOT).startsWith(lower)) {
                result.add(item);
            }
        }
        return result;
    }
}
