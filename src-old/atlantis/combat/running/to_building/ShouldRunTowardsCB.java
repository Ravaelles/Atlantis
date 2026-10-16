package atlantis.combat.running.to_building;

import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;

public class ShouldRunTowardsCB {
    public static boolean check(AUnit unit, HasPosition runAwayFrom) {
//        if (!We.terran()) return false;

//        if (unit.distTo(runAwayFrom) <= 2) return false;
//        if (unit.enemiesNearInRadius(2) > 0) return false;

        AUnit cb = position();
        if (cb == null) return false;

        if (A.seconds() >= 600 && Count.ourCombatUnits() >= 13) return false;
        if (unit.meleeEnemiesNearCount(1.7 + unit.woundPercent() / 75.0) > 0) return false;

        double distTo = cb.distTo(unit);

        return distTo >= 3 && distTo <= 30;

//        AUnit mainOrAnyBuilding = Select.mainOrAnyBuilding();
//        if (mainOrAnyBuilding == null) return false;
//
//        return unit.distTo(mainOrAnyBuilding) >= 20
//            && unit.meleeEnemiesNearCount(1.7) == 0;
    }

    public static AUnit position() {
        return Select.ourOfType(AUnitType.Terran_Bunker).nearestTo(Select.main());
    }
}
