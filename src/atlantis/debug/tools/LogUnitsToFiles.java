package atlantis.debug.tools;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.util.AFile;
import atlantis.util.log.Log;

public class LogUnitsToFiles {

    public static void saveUnitLogToFile(String message, AUnit unit) {
        if (
                Log.SAVE_UNIT_LOGS_TO_FILES == 0
                || unit == null
//                || Atlantis.KILLED <= 2
                || ((!unit.isOur() || !unit.isCombatUnit()) && Log.SAVE_UNIT_LOGS_TO_FILES < 1)
        ) {
            return;
        }

        String file = "logs/units/" + (unit.isOur() ? "Our_" : "Enemy_") + unit.nameWithId() + ".txt";
        String content = String.format("%5d", A.now()) + ": " + message + "\n";

        // Print to stderr
//        System.err.println(content);

        handleClearTheFileIfNeeded(file, message);

        AFile.appendToFile(file, content);
    }

    private static void handleClearTheFileIfNeeded(String file, String message) {
        if (message == null || A.now() <= 1) {
            AFile.saveToFile(file, "", true);
        }
    }
}
