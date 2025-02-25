const { REST, Routes } = require('discord.js');
const config = require('../config.json');

module.exports = {
  name: 'ready',
  once: true,
  async execute(client) {
    console.log(`Logged in as ${client.user.tag}`);

    try {
      // Build an array of command data from the loaded commands in client.commands
      const commands = [];
      client.commands.forEach(command => {
        commands.push({
          name: command.name,
          description: command.description,
          options: command.options,
        });
      });

      const rest = new REST({ version: '10' }).setToken(config.token);
      console.log('Started refreshing application commands.');

      // If a guildId is specified in config, register commands as guild commands
      if (config.guildId) {
        await rest.put(
          Routes.applicationGuildCommands(config.clientId, config.guildId),
          { body: commands }
        );
      } else {
        // Otherwise register as global commands (note: these can take up to an hour to update)
        await rest.put(
          Routes.applicationCommands(config.clientId),
          { body: commands }
        );
      }

      console.log('Successfully reloaded application commands.');
    } catch (error) {
      console.error('Error registering application commands:', error);
    }
  }
};
