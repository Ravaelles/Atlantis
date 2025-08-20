package atlantis.game.listeners;

import atlantis.Atlantis;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.game.player.Enemy;
import atlantis.map.AMap;
import atlantis.util.WriteJsonToFile;

public class SaveGameResultsToFile {
    public static void createAndSave(boolean winner) {
        String[] headers = {"Date", "Result", "Enemy", "Map", "Time [s]", "Killed/Lost", "Resource balance"};

//        String formatting = "%-18s %-8s %-16s %-32s %-9s %-14s %-16s";

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

        String input = "bwapi-data/read/game_results.txt";
        String output = "bwapi-data/write/game_results.txt";
        boolean copyResult = A.copy(input, output);
//        System.err.println("A copyResult = " + copyResult);
//        System.err.println("A fileExists(output) = " + A.fileExists(output));

//        copyResult = A.copy(output, input);
//        System.err.println("B copyResult = " + copyResult);
//        System.err.println("B fileExists(output) = " + A.fileExists(output));

        // =========================================================

//        boolean result = A.appendToFile(output, content);
        boolean result = WriteJsonToFile.writeOrAppend(
            output, headers, values, 32, true
        );

//        A.println("@@@@@@@@@@@@@@@@@@@@ Game result saved to: " + output
//            + " / result:" + (result ? "OK" : "FAILED")
//            + " / exists:" + A.fileExists(output)
//            + " / current working directory:" + System.getProperty("user.dir")
//        );
    }
}
