package de.noblitz.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MessageUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private MessageUtils() {}

    public static String color(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        // Support hex colors &#RRGGBB -> &x&R&R&G&G&B&B for 1.16+
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("&x");
            for (char c : hex.toCharArray()) {
                replacement.append('&').append(c);
            }
            matcher.appendReplacement(sb, replacement.toString());
        }
        matcher.appendTail(sb);
        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }

    public static void send(CommandSender sender, String message) {
        if (sender != null && message != null) {
            sender.sendMessage(color(message));
        }
    }

    public static void sendPrefixed(CommandSender sender, String prefix, String message) {
        if (sender != null && message != null) {
            String p = (prefix != null && !prefix.isEmpty()) ? prefix + " " : "";
            send(sender, p + message);
        }
    }
}
