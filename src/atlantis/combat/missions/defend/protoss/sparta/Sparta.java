package atlantis.combat.missions.defend.protoss.sparta;

import atlantis.architecture.Manager;
import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.missions.Missions;
import atlantis.combat.missions.defend.MissionDefend;
import atlantis.combat.missions.defend.MissionDefendManager;
import atlantis.combat.missions.defend.focus.MissionDefendFocusPoint;
import atlantis.game.A;
import atlantis.information.generic.Army;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.game.player.Enemy;
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

        int ourCombatUnits = Count.ourCombatUnits();
        if (ourCombatUnits <= 5) return true;

        if (ourCombatUnits >= 9) return false;
        if (ourCombatUnits >= 4 && Army.strength() >= 160) return false;

//        if (true) return false;
//        if (true) return false;

//        if (We.protoss()) {
//            if (!Enemy.zerg()) return false;
//            if (!Enemy.zerg()) return false;
//        }
        if (A.s >= 330) return false;

        return Count.basesWithUnfinished() <= 1 && focusPointIsValidForSparta();
//        return MissionHistory.numOfChanges() <= 1 &&;
    }

    private static boolean focusPointIsValidForSparta() {
        if (A.now() <= 30) return true;

//        AFocusPoint focusPoint = Missions.globalMission().focusPoint();
//        if (focusPoint == null) return false;

        AChoke mainChoke = Chokes.mainChoke();
        if (mainChoke == null) return false;

        return mainChoke.width() <= 5;
    }

    // =========================================================

    @Override
    protected Manager managerClass(AUnit unit) {
        return new MissionDefendManager(unit);
    }
}
