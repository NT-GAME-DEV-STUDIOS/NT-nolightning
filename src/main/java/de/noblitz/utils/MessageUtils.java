package de.noblitz.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MessageUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private MessageUtils() {}

    /**
     * Translates legacy color codes, hex color codes (&#RRGGBB), and MiniMessage tags into Adventure Component.
     */
    public static Component format(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }

        // If it looks like MiniMessage tags (<...>), use MiniMessage
        if (message.contains("<") && message.contains(">")) {
            try {
                return MINI_MESSAGE.deserialize(message);
            } catch (Exception ignored) {
                // Fallback to legacy formatting if MiniMessage parsing fails
            }
        }

        // Translate hex colors &#123456 -> &x&1&2&3&4&5&6
        String formatted = translateHexColorCodes(message);
        return LEGACY_SERIALIZER.deserialize(formatted);
    }

    private static String translateHexColorCodes(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("&x");
            for (char c : hex.toCharArray()) {
                replacement.append('&').append(c);
            }
            matcher.appendReplacement(sb, replacement.toString());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Send a formatted message to a sender.
     */
    public static void send(CommandSender sender, String message) {
        if (sender == null || message == null || message.isEmpty()) {
            return;
        }
        sender.sendMessage(format(message));
    }

    /**
     * Send a formatted message with prefix.
     */
    public static void sendPrefixed(CommandSender sender, String prefix, String message) {
        send(sender, prefix + message);
    }
}
