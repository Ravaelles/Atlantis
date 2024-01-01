package atlantis.combat.running.to_base;

import atlantis.game.A;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.position.Positions;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class ShouldRunToBase {
    public static boolean check(AUnit unit, HasPosition runAwayFrom) {
        if (A.seconds() >= 500) return false;
//        if (unit.nearestMeleeEnemyDist() <= 1.5) return false;

//        Positions<HasPosition> positions = new Positions<>();
//        positions.addPosition(Chokes.enemyMainChoke());
//        positions.addPosition(Chokes.enemyNaturalChoke());
//
//        if (positions.isEmpty()) return false;

//        return positions.nearestTo(unit).distTo(unit) <= 15;

        AUnit mainOrAnyBuilding = Select.mainOrAnyBuilding();
        if (mainOrAnyBuilding == null) return false;
        
        return unit.distTo(mainOrAnyBuilding) >= 20
            && unit.meleeEnemiesNearCount(1.7) == 0;
    }

    public static AUnit position() {
        return Select.mainOrAnyBuilding();
    }
}
