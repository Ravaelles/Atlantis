package atlantis.units.workers;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.AvoidEnemiesIfNeeded;
import atlantis.units.AUnit;

public class WorkerAvoidManager extends Manager {
    public WorkerAvoidManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isWorker()
            && unit.hp() <= 33
            && enemiesNear();
    }

    private boolean enemiesNear() {
        return unit.enemiesNear()
            .combatUnits()
            .canAttack(unit, 1.9 + unit.woundPercent() / 120.0)
            .notEmpty();
    }

    @Override
    public Manager handle() {
        if ((new AvoidEnemiesIfNeeded(unit)).invoke(this) != null) {
            return usedManager(this);
        }

        return null;
    }
}

