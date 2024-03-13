package atlantis.units.workers.defence.fight;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

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
//            System.err.println(A.now() + " _enemyProxyBuilding = " + _enemyProxyBuilding);
            if (_enemyProxyBuilding.isDead()) _enemyProxyBuilding = null;
            else return _enemyProxyBuilding;
        }

        AUnit main = Select.mainOrAnyBuilding();
        if (main == null) return null;

        Selection baseBuildings = Select
            .enemy()
            .buildings()
            .inRadius(25, main);

        if (baseBuildings.empty()) return _enemyProxyBuilding = null;

        if ((_enemyProxyBuilding = baseBuildings.onlyCompleted().combatBuildings(true).mostWounded()) != null) {
            return _enemyProxyBuilding;
        }

        if ((_enemyProxyBuilding = baseBuildings.combatBuildings(true).mostWounded()) != null) {
            return _enemyProxyBuilding;
        }

        return _enemyProxyBuilding = baseBuildings.mostWounded();
    }
}
