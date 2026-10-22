package atlantis.information.tech;

import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;
import bwapi.UpgradeType;

import java.util.HashMap;
import java.util.Map;

/**
 * Upgrade completions are otherwise invisible: nothing logs them, so a
 * gas-healthy game cannot prove L2/L3 (NEXT #85) the way Singularity
 * needed its twin (#72). This watches per-upgrade levels and emits one
 * row per increase. Pure logic over passed-in numbers - the game clock
 * and the engine read stay with the caller.
 */
public final class UpgradeCompletionTracker {

    private static final Map<UpgradeType, Integer> lastLevels = new HashMap<UpgradeType, Integer>();

    private UpgradeCompletionTracker() {
    }

    /**
     * @return true exactly when this call observed a level increase (and
     * logged it); first sighting of an upgrade only records the baseline.
     */
    public static boolean note(UpgradeType upgrade, int level, int frame, String time) {
        Integer previous = lastLevels.get(upgrade);
        lastLevels.put(upgrade, level);
        if (previous != null && level > previous) {
            EventLog.key(LogKey.UPGRADE_COMPLETED)
                .param("upgrade", upgrade.name())
                .param("level", level)
                .log(frame, time);

            return true;
        }
        return false;
    }

    public static void clearForTests() {
        lastLevels.clear();
    }
}
