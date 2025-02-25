package org.criticalking.criticalDiscord.commands;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import org.criticalking.criticalDiscord.CriticalDiscord;

public class CommandManager {
    private final CriticalDiscord plugin;

    public CommandManager(CriticalDiscord plugin) {
        this.plugin = plugin;
    }

    public void registerCommands() {
        // Register all commands here
        registerCommand("discord", new DiscordCommand(plugin));

    }

    private void registerCommand(String commandName, CommandExecutor executor) {
        PluginCommand command = plugin.getCommand(commandName);
        if (command != null) {
            command.setExecutor(executor);
            if (executor instanceof TabCompleter) {
                command.setTabCompleter((TabCompleter) executor);
            }
        } else {
            plugin.getLogger().warning("Command \"" + commandName + "\" is not defined in plugin.yml!");
        }
    }
}
