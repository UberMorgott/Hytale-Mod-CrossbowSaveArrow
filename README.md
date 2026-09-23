# CrossbowSaveArrow

[![en](https://img.shields.io/badge/lang-English-blue)](README.md) [![ru](https://img.shields.io/badge/lang-Русский-green)](README.ru.md)

Each crossbow keeps its own loaded arrows. Switch weapons, move the crossbow to another slot or a chest, drop it, or give it to another player: its saved ammo stays with the item.

## Installation

Version 0.0.9 requires MixinTale 3.0.0 or later. Hyxin is no longer needed by CrossbowSaveArrow. Installing CrossbowSaveArrow alone is not enough: you must also install the MixinTale bootstrap.

### Single player — Windows

1. Close Hytale completely. If upgrading from 0.0.8, follow the upgrade steps below before continuing.
2. Download `MixinTale-Bootstrap-3.0.0.jar` from [MixinTale on CurseForge](https://www.curseforge.com/hytale/bootstrap/mixintale). Choose the Bootstrap download, not Developer Tools. Download `CrossbowSaveArrow-0.0.9.jar` from [CrossbowSaveArrow on CurseForge](https://www.curseforge.com/hytale/mods/crossbow-save-arrow). Keep both files as `.jar` files; do not unzip them.
3. Open File Explorer. Paste `%AppData%\Hytale\UserData` into its address bar and press Enter. `%AppData%` normally expands to `C:\Users\<your Windows username>\AppData\Roaming`. If your launcher uses a custom install folder, find that installation's `UserData` folder instead. For example, an installation in `D:\Hytale` may use `D:\Hytale\UserData`. Use that same `UserData` folder for all the following steps.
4. Inside `UserData`, open `EarlyPlugins`. Create the folder if it does not exist. Put `MixinTale-Bootstrap-3.0.0.jar` directly inside it. Default full destination: `%AppData%\Hytale\UserData\EarlyPlugins\MixinTale-Bootstrap-3.0.0.jar`.
5. Go back to `UserData`, then open `Mods`. Create the folder if it does not exist. Put `CrossbowSaveArrow-0.0.9.jar` directly inside it. Default full destination: `%AppData%\Hytale\UserData\Mods\CrossbowSaveArrow-0.0.9.jar`. You can also install CrossbowSaveArrow through the CurseForge app or the in-game mod browser, but the manual MixinTale step above is still required.
6. Check the two files are in different folders: MixinTale must NOT go into `Mods`; CrossbowSaveArrow must NOT go into `EarlyPlugins`. Do not put either JAR inside an extra subfolder.
7. Start Hytale. Before entering your world, open that world's settings and its `Mods` tab. Under `Global Mods`, enable `CrossbowSaveArrow` for that world, then apply/save the change. Copying the JAR is not enough. Repeat this for each world where you want the mod.
8. Enter the world and check the log as described below. Then load a crossbow, let the reload finish, switch to another weapon, and switch back. The crossbow should keep its loaded arrows.

### Dedicated server

1. Stop the server. Download the same two JAR files listed above.
2. In the server's root folder, create `earlyplugins` if it does not exist. Put `MixinTale-Bootstrap-3.0.0.jar` directly in `<ServerRoot>/earlyplugins/`.
3. In the same server root, create `mods` if it does not exist. Put `CrossbowSaveArrow-0.0.9.jar` directly in `<ServerRoot>/mods/`. MixinTale must NOT go into `mods`; CrossbowSaveArrow must NOT go into `earlyplugins`.
4. Add `--accept-early-plugins` to the server launch arguments, then start the server. Check the startup log for the lines below.

On Linux, folder names are case-sensitive. For a dedicated server, use lowercase `earlyplugins` and `mods` exactly; `EarlyPlugins` and `Mods` are different folders.

### Check that it loaded

After entering the world, open `%AppData%\Hytale\UserData\Logs`. With a custom install, open `UserData\Logs` under that installation instead, for example `D:\Hytale\UserData\Logs`. Open the newest log from this launch in a text editor and search for these messages. If there are several logs for the launch, check the server log too. On a dedicated server, check its console/startup log.

- `[MixinTale/INFO] MixinTale 3.0.0 ready` — the line continues with patch counts. A newer MixinTale will show its own version.
- `[CrossbowSaveArrow] Loaded! Each crossbow keeps its own loaded ammo.`

Both messages should be present. The MixinTale message alone does not show that CrossbowSaveArrow loaded. `[CrossbowSaveArrow] StatModifiersManager is not patched` means the patch did not apply; check the MixinTale log messages and the checklist below.

### Not working?

- Wrong folder? Check the launcher's actual `UserData` folder, especially if you have more than one installation. MixinTale belongs in `EarlyPlugins`; CrossbowSaveArrow belongs in `Mods`.
- MixinTale missing? You need the Bootstrap JAR, not just CrossbowSaveArrow or the Developer Tools ZIP. Restart Hytale completely after installing it.
- Old Hyxin-based CrossbowSaveArrow still installed? Remove the old CrossbowSaveArrow JAR from the global or world-specific `earlyplugins` folders as described below. Keep only one CrossbowSaveArrow version. Hyxin itself is only needed if another mod still requires it.
- Mod disabled for this world? Open the world's `Mods` tab and enable `CrossbowSaveArrow` under `Global Mods`.
- Linux server? Check the lowercase folder names `earlyplugins` and `mods`, and the `--accept-early-plugins` launch argument.

## Upgrading from 0.0.8

1. Close the game or stop the server.
2. Remove the old `CrossbowSaveArrow-0.0.8.jar` from the early-plugin folders where you installed it. Check `%AppData%\Hytale\UserData\EarlyPlugins` and `%AppData%\Hytale\UserData\Saves\<World>\earlyplugins` for each affected world, or `<ServerRoot>/earlyplugins` on a dedicated server. Substitute your actual `UserData` path if the launcher uses a custom folder. Remove any other older CrossbowSaveArrow JAR copies too.
3. Remove the Hyxin JAR only if no other mod needs it. CrossbowSaveArrow 0.0.9 uses MixinTale instead.
4. Install the two new JARs in their separate folders using the steps above, then enable CrossbowSaveArrow for your world.

Earlier instructions and comments that put CrossbowSaveArrow in `earlyplugins` apply to 0.0.8 and older. For 0.0.9, `EarlyPlugins` and `Mods` are folders, not ZIP archives. If you followed the old instructions and used an `earlyplugins` archive inside a world save, remove the old CrossbowSaveArrow JAR from that archive too; keep any unrelated plugins.


## What changed in 0.0.9

Fast mouse-wheel scrolling past a loaded crossbow could pull the selected hotbar slot back to it and make the ammo counter jump. This was a bug in the mod: the client and server disagreed about the crossbow's swap-away action. Version 0.0.9 replaces that action with the normal weapon swap. It does not add a switching delay.

Death and respawn no longer reset the ammo in the crossbow you are holding. Switching away does not refund arrows to your inventory; they stay loaded in the crossbow instead.

Interrupting a reload still loses the arrow being loaded, just like vanilla. Finish shooting or reloading before moving or handing over the crossbow: moving it at the exact moment its ammo changes can still transfer an outdated ammo count.

## Compatibility

- Hytale 0.6.x; built against Server 0.6.8.
- Vanilla crossbows and modded crossbows with an Ammo stat bonus and reload on Ability3. Detection does not depend on the item's name.
- More & Better Crossbows inherit the vanilla crossbow template and should work with this detection. This is based on their asset definitions, not a completed in-game compatibility test.
- Compatible with UltimateSaver. Other mods that change crossbow ammo or swap actions may conflict.

## Building


Install JDK 25. Download [MixinTale Developer Tools 3.0.0](https://www.curseforge.com/hytale/mods/mixintale-developer-tools), extract `MixinTale-API-3.0.0.jar` and `MixinTale-Processor-3.0.0.jar`, and place both in the repository's `libs/` folder. Create that folder if needed.

From the repository root, run `.\gradlew.bat build` on Windows or `./gradlew build` on Linux. The output is `build/libs/CrossbowSaveArrow-0.0.9.jar`. The Developer Tools JARs are build dependencies; players only need the Bootstrap JAR and CrossbowSaveArrow.


## Changelog

- 0.0.9 — moved from Hyxin to MixinTale. CrossbowSaveArrow now goes in `Mods`. Fixed hotbar snap-back and ammo counter jumps when scrolling past a loaded crossbow. Preserves held ammo through respawn and detects crossbows by their Ammo bonus and Ability3 reload. Interrupted reloads use vanilla arrow consumption.
- 0.0.8 — previous public release; used Hyxin and was installed in `earlyplugins`.
- 0.0.3 — removed item-name checks to support modded crossbows, including More & Better Crossbows.
- 0.0.2 — rewrote ammo storage; added per-item tracking and deferred arrow consumption, and fixed restoration when switching between crossbows, relogging, and updating metadata during reload. The deferred-consumption behavior is not retained in 0.0.9.
- 1.0.0 — initial release, as numbered in the original repository history.

## Source and license

By Morgott. [Source and issue tracker](https://github.com/UberMorgott/Hytale-Mod-CrossbowSaveArrow). Licensed under CC BY-NC 4.0; see [LICENSE](LICENSE). Patching framework: [MixinTale by Traktool](https://github.com/Traktool/MixinTale).
