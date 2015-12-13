package atlantis.combat.group.missions;

import atlantis.information.AtlantisEnemyInformationManager;
import atlantis.information.AtlantisMap;
import atlantis.wrappers.SelectUnits;
import jnibwapi.Position;
import jnibwapi.Unit;

public class MissionAttack extends Mission {

    @Override
    public boolean update(Unit unit) {
        Position focusPoint = getFocusPoint();

        // Focus point is well known
        if (focusPoint != null) {
            if (focusPoint.distanceTo(unit) > 5) {
                unit.attack(focusPoint, false);
                unit.setTooltip("Mission focus");
                return true;
            }
        } // Invalid focus point, no enemy can be found, scatter
        else if (!unit.isMoving()) {
            Position position = AtlantisMap.getRandomInvisiblePosition(unit);
            if (position != null) {
                unit.attack(position, false);
                unit.setTooltip("Mission spread");
                return true;
            }
        }

        return false;
    }

    // =========================================================
    // =========================================================
    /**
     * Do not interrupt unit if it is engaged in combat.
     */
    @Override
    protected boolean canIssueOrderToUnit(Unit unit) {
        if (unit.isAttacking() || unit.isStartingAttack() || unit.isRunning()) {
            return false;
        }

        return true;
    }

    public static Position getFocusPoint() {

        // Try going near enemy base
        Position enemyBase = AtlantisEnemyInformationManager.getEnemyBase();
        if (enemyBase != null) {
            return enemyBase;
        }

        // Try going near any enemy building
        Unit enemyBuilding = AtlantisEnemyInformationManager.getNearestEnemyBuilding();
        if (enemyBuilding == null) {
            return enemyBuilding;
        }

        // Try going to any known enemy unit
        Unit anyEnemyUnit = SelectUnits.enemy().first();
        if (anyEnemyUnit == null) {
            return anyEnemyUnit;
        }

        // Absolutely no enemy unit can be found
        return null;
    }
}
