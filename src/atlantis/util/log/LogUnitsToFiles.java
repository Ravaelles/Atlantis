package atlantis.util.log;

import atlantis.game.A;
import atlantis.units.AUnit;

public class LogUnitsToFiles {

    public static void saveUnitLogToFile(String message, AUnit unit) {
        if (
                Log.SAVE_UNIT_LOGS_TO_FILES == 0
                || unit == null
                || ((!unit.isOur() || !unit.isCombatUnit()) && Log.SAVE_UNIT_LOGS_TO_FILES < 1)
        ) {
            return;
        }

        String file = "logs/units/" + (unit.isOur() ? "Our_" : "Enemy_") + unit.nameWithId() + ".txt";
        String content = String.format("%5d", A.now()) + ": " + message + "\n";

        handleClearTheFileIfNeeded(file, message);

        A.appendToFile(file, content);
    }

    private static void handleClearTheFileIfNeeded(String file, String message) {
        if (message == null || A.now() <= 1) {
            A.saveToFile(file, "", true);
        }
    }
}
