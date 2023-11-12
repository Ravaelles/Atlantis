package atlantis.map.scout;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.base.Bases;
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
        return (unit.isScout() && unit.hasNotMovedInAWhile())
            || (unit.hasNotMovedInAWhile() && unit.looksIdle());
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
        nextPositionToUnit = Bases.nearestUnexploredStartingLocation(position);
        if (nextPositionToUnit != null) return null;

        nextPositionToUnit = Bases.randomInvisibleStartingLocation();
        return null;
    }
}
