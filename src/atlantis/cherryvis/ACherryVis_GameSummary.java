package atlantis.cherryvis;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.game.player.APlayer;
import atlantis.map.AMap;
import atlantis.util.AFile;
import cherryvis.GameSummary;

/**
 * Game-end summary writer. Reads the game objects, fills the library's plain
 * {@link GameSummary} data object, and writes it next to the trace.
 */
public class ACherryVis_GameSummary {
    public void saveToFile(String directoryPath) {
        APlayer playerUs = AGame.playerUs();
        APlayer playerEnemy = AGame.enemy();

        GameSummary summary = new GameSummary();
        summary.gameDurationFrames = A.now();
        summary.map = AMap.getMapName();
        summary.playerName = playerUs.name();
        summary.playerRace = playerUs.getRace().name();
        summary.playerWin = AGame.won();
        summary.enemyName = playerEnemy.name();
        summary.enemyRace = playerEnemy.getRace().name();
        summary.enemyWin = AGame.lost();

        AFile.saveToFile(
            directoryPath + "/game_summary.json",
            summary.toJson(),
            true
        );
    }
}
