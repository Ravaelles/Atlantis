package atlantis.protoss.dt;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.base.define.EnemyThirdBase;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public class DarkTemplarIdle extends Manager {
    public DarkTemplarIdle(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        return !unit.isMoving() && !unit.shotSecondsAgo(2);
        return !unit.isMoving() || A.everyNthGameFrame(9);
    }

    @Override
    public Manager handle() {
        AUnit enemyBuilding = enemyBuilding();
        if (enemyBuilding != null && unit.move(enemyBuilding, Actions.MOVE_ENGAGE)) {
            return usedManager(this, "DT-Forward");
        }

        AUnit enemy = EnemyUnits.discovered().groundUnits().nearestTo(unit);
        if (enemy != null && unit.move(enemy, Actions.MOVE_ENGAGE)) {
            return usedManager(this, "DT-Engage");
        }

        return null;
    }

    private AUnit enemyBuilding() {
        Selection buildings = EnemyUnits.discovered().buildings();

        if (unit.idIsEven()) {
            AUnit enemyThird = EnemyThirdBase.get();
            if (enemyThird != null) return buildings.nearestTo(unit);
        }

        return buildings.nearestTo(unit);
    }
}
