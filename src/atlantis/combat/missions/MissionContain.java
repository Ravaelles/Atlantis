package atlantis.combat.missions;

import atlantis.combat.micro.AAttackEnemyUnit;
import atlantis.combat.micro.AAvoidDefensiveBuildings;
import atlantis.combat.micro.AAvoidEnemyMeleeUnitsManager;
import atlantis.combat.micro.managers.AdvanceUnitsManager;
import atlantis.combat.squad.AStickCloserOrSpreadOutManager;
import atlantis.position.APosition;
import atlantis.units.AUnit;

public class MissionContain extends Mission {

    protected MissionContain() {
        super("Contain");
        focusPointManager = new MissionContainFocusPointManager();
    }

    @Override
    public boolean update(AUnit unit) {
        unit.setTooltip("#Contain");
        APosition focusPoint = focusPoint();

        // =========================================================

//        if (handleUnitSafety(unit, true, true)) {
//            return true;
//        }

        if (AStickCloserOrSpreadOutManager.handle(unit)) {
            return true;
        }

        // Focus point is well known
        if (focusPoint != null && AdvanceUnitsManager.moveToFocusPoint(unit, focusPoint)) {
            return true;
        }

        // Invalid focus point, no enemy can be found, roam around map
//        else if (!unit.isAttacking()) {
//            return handleNoEnemyBuilding(unit);
//        }

        // =========================================================

        return false;
    }

}