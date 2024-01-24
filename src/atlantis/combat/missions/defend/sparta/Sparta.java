package atlantis.combat.missions.defend.sparta;

import atlantis.combat.missions.defend.MissionDefend;
import atlantis.combat.missions.defend.focus.MissionDefendFocusPoint;
import atlantis.util.Enemy;
import atlantis.util.We;

/**
 * Make Zealots stand in one line and defend narrow choke point like in 300.
 * That's why this is Sparta!!!
 */
public class Sparta extends MissionDefend {
    public Sparta() {
        super();
        setName("Sparta");
        focusPointManager = new MissionDefendFocusPoint();
    }

    // =========================================================

    public static boolean canUseSpartaMission() {
        if (We.terran()) return false;
        if (We.zerg() && Enemy.protoss()) return false;

        return true;
    }
}
