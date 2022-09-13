package fr.aerwyn81.headblocks.commands.list;

import fr.aerwyn81.headblocks.HeadBlocks;
import fr.aerwyn81.headblocks.commands.Cmd;
import fr.aerwyn81.headblocks.commands.HBAnnotations;
import fr.aerwyn81.headblocks.data.HeadLocation;
import fr.aerwyn81.headblocks.handlers.ConfigService;
import fr.aerwyn81.headblocks.handlers.HeadHandler;
import fr.aerwyn81.headblocks.handlers.LanguageHandler;
import fr.aerwyn81.headblocks.utils.InternalException;
import fr.aerwyn81.headblocks.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

@HBAnnotations(command = "remove", permission = "headblocks.admin")
public class Remove implements Cmd {
    private final HeadBlocks main;
    private final LanguageHandler languageHandler;
    private final HeadHandler headHandler;

    public Remove(HeadBlocks main) {
        this.main = main;
        this.languageHandler = main.getLanguageHandler();
        this.headHandler = main.getHeadHandler();
    }

    @Override
    public boolean perform(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length > 2) {
            player.sendMessage(languageHandler.getMessage("Messages.ErrorCommand"));
            return true;
        }

        HeadLocation head;

        if (args.length == 1) {
            var targetHead = headHandler.getHeadAt(player.getTargetBlock(null, 25).getLocation());

            if (targetHead == null) {
                player.sendMessage(languageHandler.getMessage("Messages.TargetBlockNotHead"));
                return true;
            }

            head = targetHead;
        } else {
            head = headHandler.getHeadByUUID(UUID.fromString(args[1]));
            if (head == null) {
                player.sendMessage(languageHandler.getMessage("Messages.RemoveLocationError"));
                return true;
            }
        }

        try {
            headHandler.removeHeadLocation(head, ConfigService.shouldResetPlayerData());
        } catch (InternalException ex) {
            sender.sendMessage(languageHandler.getMessage("Messages.StorageError"));
            HeadBlocks.log.sendMessage(MessageUtils.colorize("&cError while removing the head (" + head.getUuid().toString() + " at " + head.getLocation().toString() + ") from the storage: " + ex.getMessage()));
            return true;
        }

        Location loc = head.getLocation();
        player.sendMessage(MessageUtils.parseLocationPlaceholders(languageHandler.getMessage("Messages.HeadRemoved"), loc));
        return true;
    }

    @Override
    public ArrayList<String> tabComplete(CommandSender sender, String[] args) {
        return args.length == 2 ? main.getHeadHandler().getChargedHeadLocations().stream()
                .map(h -> h.getUuid().toString())
                .collect(Collectors.toCollection(ArrayList::new)) : new ArrayList<>();
    }
}
