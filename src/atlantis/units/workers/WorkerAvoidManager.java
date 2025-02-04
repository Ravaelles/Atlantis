package atlantis.units.workers;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class WorkerAvoidManager extends Manager {
    private boolean wasAttackedRecenty;

    public WorkerAvoidManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isWorker()
            && (unit.hp() <= 39 || (wasAttackedRecenty = unit.lastUnderAttackLessThanAgo(120)))
            && enemiesNear();
    }

    private boolean enemiesNear() {
        return unit.enemiesNear()
            .combatUnits()
            .canAttack(unit, safetyMargin())
            .notEmpty();
    }

    private double safetyMargin() {
        return 2.6 + unit.woundPercent() / 35.0;
    }

    @Override
    public Manager handle() {
        if ((new AvoidEnemies(unit)).forceHandle() != null) {
            return usedManager(this);
        }

        unit.runningManager().stopRunning();
        (new GatherResources(unit)).invokeFrom(this);
        return null;
    }

    private Manager moveAwayFromEnemy() {
        AUnit enemy = unit.enemiesNear().combatUnits().nearestTo(unit);
//        if (unit.moveAwayFrom(enemy, 3, Actions.RUN_ENEMY, "AvoidHarass")) return usedManager(this);
        if (unit.runningManager().runFrom(enemy, 4, Actions.RUN_ENEMY, false)) {
            unit.setTooltip("AvoidHarass");
            return usedManager(this);
        }

        return null;
    }
}

