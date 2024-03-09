package atlantis.units.workers.defence.fight;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class WorkerFightEnemyProxyBuilding extends Manager {
    private static AUnit _enemyProxyBuilding = null;

    public WorkerFightEnemyProxyBuilding(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (A.isUms() || Count.ourCombatUnits() >= 7) return false;

        return true;
    }

    @Override
    public Manager handle() {
        if (handleEnemyBuildingsOffensive()) return usedManager(this);

        return null;
    }


    private boolean handleEnemyBuildingsOffensive() {
        AUnit enemyBuilding = enemyBuilding();

        if (enemyBuilding == null) return false;

        unit.attackUnit(enemyBuilding);
        unit.setTooltipTactical("Cheesy!");
        return true;
    }

    private AUnit enemyBuilding() {
        if (_enemyProxyBuilding != null) {
            System.err.println(A.now() + " _enemyProxyBuilding = " + _enemyProxyBuilding);
            if (_enemyProxyBuilding.isDead()) _enemyProxyBuilding = null;
            else return _enemyProxyBuilding;
        }

        return _enemyProxyBuilding = Select
            .enemy()
            .buildings()
            .inRadius(25, Select.main())
            .mostWounded();
    }
}
