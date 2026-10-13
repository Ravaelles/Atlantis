package atlantis.game.listeners;

import atlantis.Atlantis;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.game.player.Enemy;
import atlantis.map.AMap;
import atlantis.util.AConsole;
import atlantis.util.AFile;
import atlantis.util.WriteJsonToFile;

public class SaveGameResultsToFile {
    public static void createAndSave(boolean winner) {
        String[] headers = {"Date", "Result", "Enemy", "Map", "Time [s]", "Killed/Lost", "Resource balance"};


        String[] values = {
            A.getCurrentDateInFormatYMDHHmm(),
            winner ? "WIN" : "LOSS",
            Enemy.name(),
            AMap.mapFileNameWithoutPath(),
            AGame.timeSeconds() + "",
            "K:" + Atlantis.KILLED + "/ L:" + Atlantis.LOST,
            A.resourcesBalance() + ""
        };

        // === Copy input first ===========================================

        // The write (and read) directories are not part of the repo or the
        // Wine install - a fresh checkout or a fresh Wine game dir has neither,
        // and the write then fails with "Error writing to file" (measured
        // 2026-10-06). Create them before touching the files.
        AFile.createDirectory("bwapi-data/write");
        AFile.createDirectory("bwapi-data/read");

        String input = "bwapi-data/read/game_results.txt";
        String output = "bwapi-data/write/game_results.txt";
        boolean copyResult = AFile.copy(input, output);


        // =========================================================

        boolean result = WriteJsonToFile.writeOrAppend(
            output, headers, values, 32, true
        );

    }
}
