package io.github.onu_eccs1621_sp2025.westward.data;

import java.nio.file.Path;

/**
 * Paths to game data.
 * @author Dylan Catte, Ben Westover, Noah Sumerauer, Micah Lee
 * @since 1.0.0 Alpha 1
 * @version 1.0
 * @param perilScreensPath Path to files containing perils screen information.
 * @param landmarkScreensPath Path to the files containing landmark screen information.
 * @param shopScreensPath Path to files containing shop screen information.
 * @param tradingScreensPath Path to files containing trading screen information.
 * @param huntingGameScreensPath Path to files containing hunting game screen information.
 * @param statusesPath Path to member status effect files (i.e. disease).
 * @param savesDirectoryPath Path to game save files.
 * @param itemsPath Path to game item files (ammunition, foodstuffs, etc.)
 * @param landmarksPath Path to landmark description files.
 * @param eventsPath Path to game event files.
 * @param rolesPath Path to member role files.
 * @param imagesPath Path to game image directory.
 * @param audioPath Path to the game audio directory
 * @param sfxPath Path to the game sound effects directory
 * @param configPath Path to game config file.
 * @param translationsPath Path to translation directory.
 */
public record DataPaths(
        Path perilScreensPath,
        Path landmarkScreensPath,
        Path shopScreensPath,
        Path tradingScreensPath,
        Path huntingGameScreensPath,
        Path statusesPath,
        Path savesDirectoryPath,
        Path itemsPath,
        Path landmarksPath,
        Path eventsPath,
        Path rolesPath,
        Path imagesPath,
        Path audioPath,
        Path sfxPath,
        Path configPath,
        Path translationsPath
) {
}
