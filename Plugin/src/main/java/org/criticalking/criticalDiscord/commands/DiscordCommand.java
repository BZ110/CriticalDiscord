package org.criticalking.criticalDiscord.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.criticalking.criticalDiscord.CriticalDiscord;
import org.criticalking.criticalDiscord.SQLInstance;

import java.util.Random;

public class DiscordCommand implements CommandExecutor {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final CriticalDiscord plugin;

    public DiscordCommand(CriticalDiscord plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        Player player = (Player) sender;

        // Check for "link" subcommand
        if (args.length > 0 && args[0].equalsIgnoreCase("link")) {
            Random random = new Random();
            StringBuilder randomNumber = new StringBuilder();

            for (int i = 0; i < 5; i++) {
                int digit = random.nextInt(10); // Generates a random number from 0 to 9
                randomNumber.append(digit);
            }
            String rand = randomNumber.toString();

            // Run the database operation asynchronously
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                SQLInstance instance = plugin.getSqlInstance();
                boolean success = instance.putCode(rand, player);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (success) {
                        String code = plugin.getConfig().getString("discord_rand", "Your random code is ");
                        String cp = plugin.getConfig().getString("discord_expl", "Copy and paste this code inside the #link channel.");
                        player.sendMessage(code + rand);
                        player.sendMessage(cp);
                    } else {
                        player.sendMessage("A code has already been generated recently or you are already linked.");
                    }
                });
            });
            return true;
        }

        // Check for "unlink" subcommand
        if (args.length > 0 && args[0].equalsIgnoreCase("unlink")) {
            // Check for permission
            if (!sender.hasPermission("criticaldiscord.unlink")) {
                sender.sendMessage(miniMessage.deserialize("<red>You do not have permission to use that command.</red>"));
                return true;
            }
            // Expect a player name argument
            if (args.length < 2) {
                sender.sendMessage("Usage: /discord unlink <player>");
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                SQLInstance instance = plugin.getSqlInstance();
                boolean unlinked = instance.forceUnlink(target);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (unlinked) {
                        sender.sendMessage("Player " + target.getName() + " has been unlinked.");
                    } else {
                        sender.sendMessage("Failed to unlink player " + target.getName() + " or they were not linked.");
                    }
                });
            });
            return true;
        }

        // Default behavior: send a clickable Discord join message
        String discordLink = plugin.getConfig().getString("discord_link", "https://google.com");
        String discordMsg = plugin.getConfig().getString("discord_msg", "Click here to join our Discord!");

        Component clickableMessage = miniMessage.deserialize(
                "<aqua><click:open_url:'" + discordLink + "'>" + discordMsg + "</click></aqua>"
        );

        sender.sendMessage(clickableMessage);
        return true;
    }
}
