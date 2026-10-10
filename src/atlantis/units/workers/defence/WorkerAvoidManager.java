package atlantis.units.workers.defence;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.protoss.ProtossAvoidEnemies;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

public class WorkerAvoidManager extends Manager {
    private boolean wasAttackedRecenty;

    public WorkerAvoidManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isWorker()
            && !unit.isAttacking()
            && (unit.hp() <= 39 || (wasAttackedRecenty = unit.lastUnderAttackLessThanAgo(5 * 30)))
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
        if (unit.distToBase() <= 20 && unit.allUnitsNear().groundUnits().countInRadius(1.5, unit) >= 3) {
            if (runTowardsMineralsToBecomeTransparent(unit)) {
                return usedManager(this);
            }
        }

        if ((new ProtossAvoidEnemies(unit)).forceHandle() != null) {
            return usedManager(this);
        }

//        unit.runningManager().stopRunning();
        return null;
    }

    private boolean runTowardsMineralsToBecomeTransparent(AUnit unit) {
        AUnit mineral = Select.minerals().nearestTo(unit);
        if (mineral == null) return false;

        return unit.gather(mineral);
    }

//    private Manager moveAwayFromEnemy() {
//        AUnit enemy = unit.enemiesNear().combatUnits().nearestTo(unit);
////        if (unit.moveAwayFrom(enemy, 3, Actions.RUN_ENEMY, "AvoidHarass")) return usedManager(this);
//        if (unit.runningManager().runFrom(enemy, 4, Actions.RUN_ENEMY, false)) {
//            unit.setTooltip("AvoidHarass");
//            return usedManager(this);
//        }
//
//        return null;
//    }
}

