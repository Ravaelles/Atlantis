package atlantis.map.scout;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.base.BaseLocations;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

import static atlantis.map.scout.ScoutState.nextPositionToUnit;

public class ScoutFreeBases extends Manager {

    public ScoutFreeBases(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isScout()) return false;
        if (unit.enemiesNear().combatUnits().inRadius(6, unit).notEmpty()) return false;

        return unit.hasNotMovedInAWhile() || (unit.hasNotMovedInAWhile() && unit.looksIdle());
    }

    @Override
    protected Manager handle() {
        if (nextPositionToUnit != null && !nextPositionToUnit.isPositionVisible()) {
            if (unit.move(
                nextPositionToUnit, Actions.MOVE_SCOUT, "ScoutBases" + A.now(), true
            )) return usedManager(this);
        }

        AUnit enemyBuilding = EnemyUnits.nearestEnemyBuilding();
        APosition position = enemyBuilding != null ? enemyBuilding.position() : unit.position();
        nextPositionToUnit = BaseLocations.nearestUnexploredStartingLocation(position);
        if (nextPositionToUnit != null) return null;

        nextPositionToUnit = BaseLocations.randomInvisibleStartingLocation();
        return null;
    }
}
