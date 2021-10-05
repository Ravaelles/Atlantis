package atlantis.combat.missions;

import atlantis.combat.micro.AAvoidDefensiveBuildings;
import atlantis.combat.micro.managers.AttackManager;
import atlantis.position.APosition;
import atlantis.units.AUnit;

public class MissionContain extends MissionAttack {

    private final MissionContainFocusPointManager focusPointManager;

    protected MissionContain(String name) {
        super(name);
        focusPointManager = new MissionContainFocusPointManager();
    }

    @Override
    public APosition focusPoint() {
//        return getFocusPointManager().focusPoint();
        return focusPointManager.focusPoint();
    }

//    @Override
//    public boolean update(AUnit unit) {
//        return getUnitManager().updateUnit(unit);
//    }

    @Override
    public boolean update(AUnit unit) {
        unit.setTooltip("#Contain");
        APosition focusPoint = focusPoint();

        // =========================================================

        if (AAvoidDefensiveBuildings.avoidCloseBuildings(unit)) {
            return true;
        }

        // Focus point is well known
        if (focusPoint != null && unit.distanceTo(focusPoint) > 8) {
            return AttackManager.attackFocusPoint(unit, focusPoint);
        }

        // Invalid focus point, no enemy can be found, roam around map
//        else if (!unit.isAttacking()) {
//            return handleNoEnemyBuilding(unit);
//        }

        // =========================================================

        return false;
    }

}