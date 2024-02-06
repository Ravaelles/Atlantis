package atlantis.units.workers.defence.fight;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class WorkerFightEnemyProxyBuilding extends Manager {
    public WorkerFightEnemyProxyBuilding(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (A.isUms() || (A.seconds() >= 90 && Count.ourCombatUnits() >= 7)) return false;

        return true;
    }

    @Override
    public Manager handle() {
        if (handleEnemyBuildingsOffensive()) return usedManager(this);

        return null;
    }


    private boolean handleEnemyBuildingsOffensive() {
        for (AUnit enemyBuilding : Select.enemy().buildings().inRadius(20, unit).list()) {
            unit.attackUnit(enemyBuilding);
            unit.setTooltipTactical("Cheesy!");
            return true;
        }

        return false;
    }
}
