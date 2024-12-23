package fr.aerwyn81.headblocks.commands.list;

import fr.aerwyn81.headblocks.HeadBlocks;
import fr.aerwyn81.headblocks.commands.Cmd;
import fr.aerwyn81.headblocks.commands.HBAnnotations;
import fr.aerwyn81.headblocks.data.HeadLocation;
import fr.aerwyn81.headblocks.data.PlayerProfileLight;
import fr.aerwyn81.headblocks.services.HeadService;
import fr.aerwyn81.headblocks.services.LanguageService;
import fr.aerwyn81.headblocks.services.PlaceholdersService;
import fr.aerwyn81.headblocks.services.StorageService;
import fr.aerwyn81.headblocks.utils.bukkit.LocationUtils;
import fr.aerwyn81.headblocks.utils.bukkit.PlayerUtils;
import fr.aerwyn81.headblocks.utils.chat.ChatPageUtils;
import fr.aerwyn81.headblocks.utils.internal.CommandsUtils;
import fr.aerwyn81.headblocks.utils.internal.InternalException;
import fr.aerwyn81.headblocks.utils.message.MessageUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@HBAnnotations(command = "me", permission = "headblocks.use", alias = "m")
public class Me implements Cmd {

    @Override
    public boolean perform(CommandSender sender, String[] args) {
        PlayerProfileLight playerProfileLight = CommandsUtils.extractAndGetPlayerUuidByName(sender, args, PlayerUtils.hasPermission(sender, "headblocks.commands.progress.other"));
        if (playerProfileLight == null) {
            return true;
        }

        ArrayList<UUID> heads;

        try {
            heads = StorageService.getHeads();
        } catch (InternalException e) {
            sender.sendMessage(LanguageService.getMessage("Messages.StorageError"));
            return true;
        }

        if (heads.isEmpty()) {
            sender.sendMessage(LanguageService.getMessage("Messages.ListHeadEmpty"));
            return true;
        }

        StorageService.getHeadsPlayer(playerProfileLight.uuid()).whenComplete(pHeads -> {
            var playerHeads = new ArrayList<>(pHeads);

            heads.sort(Comparator.comparingInt(playerHeads::indexOf));

            ChatPageUtils cpu = new ChatPageUtils(sender)
                    .entriesCount(heads.size())
                    .currentPage(args);

            String message = LanguageService.getMessage("Chat.LineTitle");
            if (sender instanceof Player) {
                TextComponent titleComponent = new TextComponent(PlaceholdersService.parse(playerProfileLight.name(), playerProfileLight.uuid(), LanguageService.getMessage("Chat.StatsTitleLine")
                        .replaceAll("%headCount%", String.valueOf(playerHeads.size()))));
                cpu.addTitleLine(titleComponent);
            } else {
                sender.sendMessage(message);
            }

            for (int i = cpu.getFirstPos(); i < cpu.getFirstPos() + cpu.getPageHeight() && i < cpu.getSize(); i++) {
                UUID uuid = heads.get(i);

                HeadLocation headLocation = null;

                var chargedHead = HeadService.getChargedHeadLocations().stream().filter(h -> h.getUuid().equals(uuid)).findFirst();
                if (chargedHead.isPresent()) {
                    headLocation = chargedHead.get();
                }


                var headName = headLocation != null ? headLocation.getName() : uuid.toString();
                if (headName.isEmpty()) {
                    headName = uuid.toString();
                }

                if (sender instanceof Player) {
                    TextComponent msg = new TextComponent(MessageUtils.colorize("&6" + headName));

                    TextComponent own;
                    if (playerHeads.stream().anyMatch(s -> s.equals(uuid))) {
                        own = new TextComponent(LanguageService.getMessage("Chat.Box.Own"));
                        own.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(LanguageService.getMessage("Chat.Hover.Own"))));
                    } else {
                        own = new TextComponent(LanguageService.getMessage("Chat.Box.NotOwn"));
                        own.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(LanguageService.getMessage("Chat.Hover.NotOwn"))));
                    }

                    TextComponent tp = new TextComponent(LanguageService.getMessage("Chat.Box.Teleport"));

                    if (headLocation != null) {
                        var location = headLocation.getLocation();

                        if (location.getWorld() != null) {
                            tp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/headblocks tp " + location.getWorld().getName() + " " + (location.getX() + 0.5) + " " + (location.getY() + 1) + " " + (location.getZ() + 0.5 + " 0.0 90.0")));
                        }
                        tp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(LanguageService.getMessage("Chat.Hover.Teleport"))));
                    } else {
                        tp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(LanguageService.getMessage("Chat.Hover.BlockedTeleport"))));
                    }

                    TextComponent space = new TextComponent(" ");
                    if(PlayerUtils.hasPermission(sender, "headblocks.admin"))
                    {
                        cpu.addLine(own, space, tp, space, msg, space);
                    }
                    else
                    {
                        cpu.addLine(own, space, space, msg, space);
                    }
                } else {
                    sender.sendMessage((playerHeads.stream().anyMatch(s -> s.equals(uuid)) ?
                            LanguageService.getMessage("Chat.Box.Own") : LanguageService.getMessage("Chat.Box.NotOwn")) + " " +
                            MessageUtils.colorize("&6" + headName));
                }
            }

            cpu.addPageLine("me");
            cpu.build();
        });
        return true;
    }

    @Override
    public ArrayList<String> tabComplete(CommandSender sender, String[] args) {
        return args.length == 2 ? Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toCollection(ArrayList::new)) : new ArrayList<>();
    }
}