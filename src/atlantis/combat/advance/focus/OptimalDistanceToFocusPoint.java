package atlantis.combat.advance.focus;

import atlantis.combat.missions.Missions;
import atlantis.terran.chokeblockers.ChokeToBlock;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class OptimalDistanceToFocusPoint {
    public static double forUnit(AUnit unit) {
        if (unit.isDragoon()) {
            return ChokeToBlock.BASE_DIST_FROM_CHOKE_CENTER
                + 0.3
                + (unit.friendsNear().ofType(AUnitType.Protoss_Zealot).inRadius(0.5, unit).notEmpty() ? 1.0 : 0);
        }

        if (unit.isMedic()) return 0.5;
        if (unit.isMelee()) return 2;
//        return 4;
        return 0.5;
    }

    public static double toFocus(AUnit unit, AFocusPoint focusPoint) {
        if (focusPoint.isAroundChoke() && !Missions.isGlobalMissionSparta()) {
            return 15 - (unit.isMelee() ? 1.5 : 0);
        }

        return 0;
    }
}
