package fr.aerwyn81.headblocks.services;

import fr.aerwyn81.headblocks.HeadBlocks;
import fr.aerwyn81.headblocks.utils.internal.InternalException;
import fr.aerwyn81.headblocks.utils.message.MessageUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlaceholdersService {

    public static String parse(Player player, String message) {
        message = message.replaceAll("%player%", player.getName())
                .replaceAll("%prefix%", LanguageService.getPrefix());

        if (message.contains("%progress%") || message.contains("%current%") || message.contains("%max%") || message.contains("%left%")) {
            message = parseInternal(player, message);
        } else {
            message = MessageUtils.colorize(message);
        }

        if (HeadBlocks.isPlaceholderApiActive)
            return PlaceholderAPI.setPlaceholders(player, message);

        return MessageUtils.centerMessage(message);
    }

    public static String parseInternal(Player player, String message) {
        String progress;
        int current;

        try {
            current = StorageService.getHeadsPlayer(player.getUniqueId()).size();
        } catch (InternalException ex) {
            player.sendMessage(LanguageService.getMessage("Messages.StorageError"));
            HeadBlocks.log.sendMessage(MessageUtils.colorize("Error while retrieving heads of " + player.getName() + ": " + ex.getMessage()));
            return MessageUtils.colorize(message);
        }

        int total = HeadService.getChargedHeadLocations().size();

        message = message.replaceAll("%current%", String.valueOf(current))
                .replaceAll("%max%", String.valueOf(total));

        if (message.contains("%progress%")) {
            progress = MessageUtils.createProgressBar(current, total,
                    ConfigService.getProgressBarBars(),
                    ConfigService.getProgressBarSymbol(),
                    ConfigService.getProgressBarCompletedColor(),
                    ConfigService.getProgressBarNotCompletedColor());

            message = message.replaceAll("%progress%", progress);
        }

        if (message.contains("%left%")) {
            message = message.replaceAll("%left%", String.valueOf(total - current));
        }

        return MessageUtils.colorize(message);
    }

    public static String[] parse(Player player, List<String> messages) {
        List<String> msgs = new ArrayList<>();

        messages.forEach(message -> msgs.add(parse(player, message)));

        return msgs.toArray(new String[0]);
    }
}
