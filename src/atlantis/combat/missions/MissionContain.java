package atlantis.combat.missions;

import atlantis.AGame;
import atlantis.combat.micro.AAttackEnemyUnit;
import atlantis.combat.micro.AAvoidDefensiveBuildings;
import atlantis.combat.micro.managers.AdvanceUnitsManager;
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

//        if (AGame.isPlayingAsTerran() && handleTerran(unit)) {
//            return true;
//        }

        if (AAvoidDefensiveBuildings.avoidCloseBuildings(unit)) {
            return true;
        }

        // Allow to attack nearby enemy units
        if (AAttackEnemyUnit.handleAttackNearbyEnemyUnits(unit, 8)) {
            return true;
        }

        // Focus point is well known
        if (focusPoint != null) {
            AdvanceUnitsManager.moveToFocusPoint(unit, focusPoint);
            return true;
        }

        // Invalid focus point, no enemy can be found, roam around map
//        else if (!unit.isAttacking()) {
//            return handleNoEnemyBuilding(unit);
//        }

        // =========================================================

        return false;
    }

    // =========================================================

    private boolean handleTerran(AUnit unit) {
        if (unit.isTank() && !unit.isSieged()) {
            unit.siege();
            return true;
        }

        return false;
    }

}