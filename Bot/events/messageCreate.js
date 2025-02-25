module.exports = {
    name: 'messageCreate',
    async execute(message, client) {
      // Ignore bot messages and messages not in a guild.
      if (message.author.bot || !message.guild) return;
      try {
        // Retrieve the link channel ID from the settings table.
        const settingsResult = client.settingsChannelResult;
        if (!settingsResult || settingsResult.length === 0) return;
        const linkChannelId = settingsResult[0][0].value;

        if ("" + message.channel.id != "" + linkChannelId) return; // Only process messages in the assigned link channel.
      } catch (err) {
        console.error("Error fetching link channel setting:", err);
        return;
      }
      
      // Delete the user's message.
      try {
        await message.delete();
      } catch (err) {
        console.error("Failed to delete message:", err);
      }
      
      // The message content is assumed to be the code.
      const code = message.content.trim();
      
      try {
        // Check if the provided code exists in the link table.
        let codeResult = await client.db.query("SELECT * FROM to_link_users WHERE code = ? LIMIT 1", [code]);
        codeResult = codeResult[0][0];
        const playerUUID = codeResult.player_uuid;
        let embed;
        if (codeResult.code && codeResult.code.length > 0) {
          // Parse the creation_unix as a number.
          const creationUnix = Number(codeResult.creation_unix);
          const currentTime = Date.now();
          if (currentTime - creationUnix >= 300000) {
            // Code expired: delete it and report invalid.
            await client.db.query("DELETE FROM to_link_users WHERE code = ?", [code])[0];
            embed = new client.discord.EmbedBuilder()
              .setTitle("Invalid Code")
              .setDescription("The code you provided has expired.")
              .setColor("#FF0000");
          } else {
            // Valid code found.
            // Delete the row from the link table.
            await client.db.query("DELETE FROM to_link_users WHERE code = ?", [code])[0];
            // Insert linking data into the linked table.
            await client.db.query(
              "INSERT INTO linked_users (discord_id, player_uuid, discord_username) VALUES (?, ?, ?)",
              [message.author.id, playerUUID, message.author.tag]
            )[0];
            embed = new client.discord.EmbedBuilder()
              .setTitle("Linked Successfully")
              .setDescription("Your code was valid and your account has been linked!")
              .setColor("#00FF00");
          }
        } else {
          // Code is invalid.
          embed = new client.discord.EmbedBuilder()
            .setTitle("Invalid Code")
            .setDescription("The code you provided is invalid.")
            .setColor("#FF0000");
        }
        
        // Send the reply embed in the same channel.
        const reply = await message.channel.send({ embeds: [embed] });
        // Delete the reply after 5 seconds.
        setTimeout(async () => {
          try {
            await reply.delete();
          } catch (err) {
            console.error("Failed to delete reply:", err);
          }
        }, 5000);
      } catch (err) {
        console.error("Error processing code:", err);
      }
    }
  };
  