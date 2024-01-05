package atlantis.combat.running.to_building;

import atlantis.game.A;
import atlantis.information.strategy.OurStrategy;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class ShouldRunTowardsBunker {
    public static boolean check(AUnit unit, HasPosition runAwayFrom) {
        if (A.seconds() >= 600) return false;
        if (unit.distTo(runAwayFrom) <= 2) return false;
        if (unit.enemiesNearInRadius(2) > 0) return false;

        AUnit bunker = position();
        if (bunker == null || bunker.distTo(unit) <= 4) return false;

        return true;

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
