const { Client, GatewayIntentBits, Collection } = require('discord.js');
const fs = require('fs');
const path = require('path');
const config = require('./config.json');
const createDBPool = require('./database/connection');

// Create a new client instance with required intents
const client = new Client({ intents: Object.values(GatewayIntentBits) });


// Create a collection to store commands
client.commands = new Collection();

// Load command files from the "commands" folder
const commandFiles = fs.readdirSync(path.join(__dirname, 'commands')).filter(file => file.endsWith('.js'));
for (const file of commandFiles) {
  const command = require(`./commands/${file}`);
  client.commands.set(command.name, command);
}

// Load event files from the "events" folder
const eventFiles = fs.readdirSync(path.join(__dirname, 'events')).filter(file => file.endsWith('.js'));
for (const file of eventFiles) {
  const event = require(`./events/${file}`);
  if (event.once) {
    client.once(event.name, (...args) => event.execute(...args, client));
  } else {
    client.on(event.name, (...args) => event.execute(...args, client));
  }
}

// Initialize MySQL connection pool and attach to client for easy access, while also making the discord_settings table.
(async () => {
  client.db = createDBPool(config.mysql.connectionString);
  client.settingsChannelResult = await client.db.query("SELECT value FROM discord_settings WHERE setting = 'link_channel' LIMIT 1");
  await client.db.query("CREATE TABLE IF NOT EXISTS discord_settings (setting VARCHAR(50) PRIMARY KEY, value VARCHAR(255));")
  client.discord = require('discord.js');
})();


// Log in to Discord with your bot token
client.login(config.token);
