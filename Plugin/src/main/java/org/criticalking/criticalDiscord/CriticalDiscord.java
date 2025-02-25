package org.criticalking.criticalDiscord;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.criticalking.criticalDiscord.commands.CommandManager;

import java.util.Objects;
import java.util.logging.Logger;

public final class CriticalDiscord extends JavaPlugin {

    private Logger log;
    private SQLInstance sqlInstance;

    @Override
    public void onEnable() {
        // Plugin startup logic
        log = getLogger();
        sqlInstance = new SQLInstance(getConfig().getString("jdbc_connection_string"), this);

        log.info("----");
        log.info("  CriticalDiscord");
        log.info("  By Critical <3");
        log.info("----");

        if(Objects.equals(getConfig().getString("jdbc_connection_string"), "DEFAULT")) {
            log.info("Looks like you haven't booted up this plugin yet.");
            log.info("Change the config.yml, and then reload this plugin.");
            saveDefaultConfig();
            Bukkit.getPluginManager().disablePlugin(this);
        } else {
            log.info("Connecting to MySQL...");
            sqlInstance.init();

            // Create the 2 required tables.
            Table tableToLink = new Table();
            tableToLink.setTableName(getConfig().getString("table_to_link"));
            tableToLink.addColumn("player_uuid", "TEXT");
            tableToLink.addColumn("creation_unix", "TEXT");
            tableToLink.addColumn("code", "TEXT");

            Table tableLinked = new Table();
            tableLinked.setTableName(getConfig().getString("table_linked"));
            tableLinked.addColumn("player_uuid", "TEXT");
            tableLinked.addColumn("discord_id", "TEXT");
            tableLinked.addColumn("discord_username", "TEXT");

            log.info("Registering Tables...");
            sqlInstance.addTable(tableToLink, "LINK");
            sqlInstance.addTable(tableLinked, "LINKED");
            log.info("Tables Registered! Done startup.");

            new CommandManager(this).registerCommands();
        }
    }

    public SQLInstance getSqlInstance() {
        return sqlInstance;
    }

    @Override
    public void onDisable() {
        log = getLogger();
        log.info("Shutting down...");
        sqlInstance.close();
    }
}
