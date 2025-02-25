module.exports = {
    name: 'assignlinkchannel',
    description: 'Sets the channel to use for linking codes.',
    options: [
      {
        name: 'channel',
        type: 7, // CHANNEL type
        description: 'The channel to set as the link channel',
        required: true
      }
    ],
    async execute(interaction, client) {
      // Check if the member has the required permission (e.g., Administrator)
      if (!interaction.member.permissions.has('ADMINISTRATOR')) {
        return interaction.reply({ content: 'You do not have permission to use this command.', ephemeral: true });
      }
      const channel = interaction.options.getChannel('channel');
      if (!channel) {
        return interaction.reply({ content: 'Invalid channel specified.', ephemeral: true });
      }
      try {
        // Save the link channel to the database (assumes table discord_settings with a UNIQUE key on "setting")
        const query = "INSERT INTO discord_settings (setting, value) VALUES ('link_channel', ?) ON DUPLICATE KEY UPDATE value = ?";
        await client.db.query(query, [channel.id, channel.id]);
        return interaction.reply({ content: `Link channel set to ${channel}.`, ephemeral: true });
      } catch (err) {
        console.error(err);
        return interaction.reply({ content: 'Error setting link channel in database.', ephemeral: true });
      }
    }
  };
  