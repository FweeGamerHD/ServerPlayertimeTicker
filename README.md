# ServerPlayertimeTicker

ServerPlayertimeTicker is a Minecraft Fabric mod that tracks and manages playtime for players on a server.

## Features

- **Automatic Playtime Tracking:** Automatically tracks the playtime of each player on the server.
- **Maximum Playtime Limit:** Enforces a maximum playtime limit for each player, kicking them from the server if exceeded.
- **Configurable:** Customize settings such as maximum playtime and hotbar message using a configuration file.
- **Real-time Updates:** Syncs configuration changes in almost real-time, ensuring the server adapts quickly.

## Usage

1. **Installation:**
   - Download the latest release JAR file from the [Releases](https://github.com/FweeGamerHD/ServerPlayertimeTicker/releases) page.
   - Place the downloaded JAR file into the `mods` folder of your Minecraft Fabric server.
   
2. **Configuration:**
   - After starting the server with the mod installed, a configuration file (`playtime_data.json`) will be generated in the `config` folder.
   - Edit the configuration file to adjust settings such as maximum playtime and hotbar message format.
   - Note: Use %m and %s as seen in the default config to place the minute and second counter in the text.

3. **Operation:**
   - The mod will automatically start tracking playtime for each player as soon as they join the server.
   - Players will see their remaining playtime above the hotbar.
   - If a player exceeds the maximum playtime, they will be automatically kicked from the server.

## Contributing

Contributions are welcome! If you'd like to contribute to the development of this mod, please follow these steps:

1. Fork the repository.
2. Create a new branch (`git checkout -b feature/fooBar`).
3. Commit your changes (`git commit -am 'Add some fooBar'`).
4. Push to the branch (`git push origin feature/fooBar`).
5. Create a new Pull Request.

## License

This mod is licensed under the [Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License](LICENSE).

