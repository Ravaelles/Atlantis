package atlantis.combat.micro.terran.wraith;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class AttackTargetInRangeIfRanTooLong extends Manager {
    public AttackTargetInRangeIfRanTooLong(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.noCooldown()
            && unit.lastStartedRunningAgo() < unit.lastStartedAttackAgo()
            && unit.enemiesNear().effVisible().notEmpty();
    }

    protected Manager handle() {
        Selection targets = unit.enemiesNear().canBeAttackedBy(unit, -0.15);

        if (tryAttacking(targets.combatUnits())) {
            return usedManager(this, "AttackNearUnit");
        }

        if (tryAttacking(targets.combatBuildingsAntiLand())) {
            return usedManager(this, "AttackNearBuilding");
        }

        if (tryAttacking(targets.workers())) {
            return usedManager(this, "AttackNearWorker");
        }

        if (tryAttacking(targets.buildings())) {
            return usedManager(this, "AttackNearBuilding");
        }

        return null;
    }

    private boolean tryAttacking(Selection targets) {
        if (targets.empty()) return false;

        AUnit target = targets.mostWounded();
        unit.attackUnit(target);
        return true;
    }
}

