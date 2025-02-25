# CriticalDiscord

CriticalDiscord is an integrated project that links Minecraft and Discord accounts using a shared MySQL database. The project consists of two main components:

- **Minecraft Plugin (in `/Plugin/`):**  
  Handles in-game account linking by generating and validating codes, then storing linking data in the database.

- **Discord Bot (in `/Bot/`):**  
  Manages Discord-side linking, allowing administrators to designate a link channel and processing user messages to validate codes and complete the linking process.

---

## Features

### Minecraft Plugin
- **Link Code Generation:**  
  Players use `/discord link` to generate a random 5-digit linking code.
- **Code Validation:**  
  Codes expire after 5 minutes. If a valid code exists, it prevents duplicate code generation.
- **Data Management:**  
  Maintains separate tables for pending linking codes and successfully linked accounts.
- **Force Unlink:**  
  Administrators can force unlink players if needed.

### Discord Bot
- **Assign Link Channel:**  
  Use `/assignlinkchannel <channel>` to designate a channel where linking codes are processed.
- **Message Monitoring:**  
  The bot deletes messages in the link channel, validates the submitted code against the database, and replies with a success (green embed) or error (red embed) message.
- **Integrated Experience:**  
  Works with the same MySQL database as the Minecraft plugin to ensure seamless account linking.

---

## Setup & Installation

### Minecraft Plugin (1.19.4+)
1. **Build the Plugin:**  
   Use Maven to build the project. The shaded JAR can be found in the `/Plugin/target/` folder.
2. **Configure:**  
   Edit `config.yml` to set your MySQL JDBC connection string, table names, and other settings.
3. **Deploy:**  
   Place the JAR in your server's `plugins/` directory and restart your server.
4. **Dependencies:**  
   Ensure PlaceholderAPI and any other required dependencies are installed on your server.

### Discord Bot
1. **Install Dependencies:**  
   In the `/Bot/` directory, run:
   ```bash
   npm install discord.js mysql2
   ```
2. **Configure:**  
   Update `config.json` with your bot token, MySQL connection string, and any other settings.
3. **Deploy:**  
   Start the bot with:
   ```bash
   node index.js
   ```
4. **Intents:**  
   Ensure the bot has all necessary gateway intents enabled in the Discord Developer Portal.

---

## Usage

- **Minecraft Plugin Commands:**
  - `/discord link`  
    Generates a linking code and sends it to the player.
  - `/discord unlink <player>`  
    (Admin) Force-unlinks a player's account.

- **Discord Bot Commands:**
  - `/assignlinkchannel <channel>`  
    Sets the channel where linking codes will be processed.
  
- **Linking Process (Discord Side):**
  - When a user sends a message in the designated link channel, the bot:
    1. Deletes the user's message.
    2. Checks if the message content matches a valid linking code in the database.
    3. Compares the stored Unix timestamp with the current time (codes expire after 5 minutes).
    4. If valid, deletes the pending code, inserts the user's Discord info into the linked table, and replies with a green "Linked Successfully" embed.
    5. If invalid or expired, replies with a red "Invalid Code" embed.
  - The reply is automatically deleted after a short delay to keep the channel clean.

---
Happy linking! <3
