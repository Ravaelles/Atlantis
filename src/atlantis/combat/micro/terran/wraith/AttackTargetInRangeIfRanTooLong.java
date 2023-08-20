package atlantis.combat.micro.terran.wraith;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class AttackTargetInRangeIfRanTooLong extends Manager {
    public AttackTargetInRangeIfRanTooLong(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.noCooldown()
            && (unit.lastStartedAttackAgo() > 85 || unit.lastStartedRunningAgo() < unit.lastStartedAttackAgo())
            && unit.enemiesNear().effVisible().notEmpty()
            && (unit.hp() >= 80 || (unit.hp() >= 45 && TerranWraith.noAntiAirBuildingNearby(unit)));
    }

    protected Manager handle() {
        Selection targets = unit.enemiesNear().canBeAttackedBy(unit, -0.4);

        if (A.chance(10) || unit.lastStartedAttackLessThanAgo(30 * 7)) {
            if (A.chance(50) || unit.lastStartedRunningAgo() + 30 * 8 >= unit.lastStartedAttackAgo()) {
                if (tryAttacking(targets.combatUnits())) {
                    return usedManager(this, "AttackNearUnit");
                }
            }
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

//        if (unit.isMoving() && A.chance(20)) {
//            unit.holdPosition("HoldToAttack");
//            return true;
//        }

        AUnit target;

        if (unit.hp() <= 80 || A.chance(60)) {
            target = targets.random();
        }
        else {
            target = targets.mostWounded();
        }

        unit.attackUnit(target);
        return true;
    }
}

