package atlantis.protoss.shuttle;

import atlantis.architecture.Manager;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class ProtossShuttleAvoidAA extends Manager {
    private Selection deadlyEnemies;
    private AUnit nearestEnemy;

    public ProtossShuttleAvoidAA(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        deadlyEnemies = unit.enemiesNear().ofType(AUnitType.Zerg_Scourge).inRadius(8, unit);
        nearestEnemy = deadlyEnemies.nearestTo(unit);

        return deadlyEnemies.count() > 0;
    }

    @Override
    public Manager handle() {
        HasPosition runTo = runTo();

        if (runTo != null && unit.move(runTo, Actions.MOVE_AVOID, null)) {
            return usedManager(this);
        }

        if (unit.moveAwayFrom(nearestEnemy, 5, Actions.MOVE_AVOID, null)) {
            return usedManager(this);
        }

        return null;
    }

    private HasPosition runTo() {
        APosition friends = unit.friendsNear().havingAntiAirWeapon().center();

        if (friends == null) return null;

        return nearestEnemy.translateTilesTowards(friends, 10);
    }
}
