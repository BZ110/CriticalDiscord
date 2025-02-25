module.exports = {
    name: 'ping',
    description: 'Replies with Pong!',
    // Example: add required permissions (e.g., 'ADMINISTRATOR')
    permissions: ['ADMINISTRATOR'],
    // Options for the slash command (if any)
    options: [],
    async execute(interaction, client) {
      // Check if the member has the required permissions
      if (this.permissions.length > 0) {
        const missing = this.permissions.filter(perm => !interaction.member.permissions.has(perm));
        if (missing.length) {
          return interaction.reply({ content: 'You do not have permission to use this command.', ephemeral: true });
        }
      }
      // Create an embed.
      const embed = new client.discord.EmbedBuilder()
        .setTitle('Pong!')
        .setColor('#0099ff');

    // Measure the latency of the bot, and sql server (client.db)
    const ping =    Date.now() - interaction.createdTimestamp;
    const timeBeforeRequest = Date.now();
                    await client.db.query('SELECT 1');
    const sqlPing = Date.now() - timeBeforeRequest;

    embed.setDescription(`**Bot Latency** is ${ping}ms.\n**SQL Latency** is ${sqlPing}ms.`);
    embed.setFooter({ text: "Readings may vary depending on the server's load." });

      await interaction.reply({ ephemeral: true, embeds: [embed] });
    }
  };
  