package atlantis.combat.micro.terran.wraith;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class AttackTargetInRange extends Manager {
    public AttackTargetInRange(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.noCooldown() && unit.hasAnyWeapon();
    }

    protected Manager handle() {
        Selection targets = unit.enemiesNear()
            .nonRanged()
            .canBeAttackedBy(unit, -0.3);

        if (!notAllowedToAttackBuildings()) targets = targets.nonBuildings();

        if (targets.empty()) return null;

        if (A.chance(30) || unit.enemiesNear().nonBuildings().inShootRangeOf(unit).notEmpty()) {
            if (tryAttacking(targets.nonBuildings())) {
                return usedManager(this, "AttackNearUnit");
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

    private boolean notAllowedToAttackBuildings() {
        return (unit.noCooldown() && A.chance(60))
            && unit.lastStartedAttackLessThanAgo(30 * 5);
    }

    protected boolean tryAttacking(Selection targets) {
        if (targets.empty()) return false;

//        if (unit.isMoving() && A.chance(20)) {
//            unit.holdPosition("HoldToAttack");
//            return true;
//        }

        AUnit target;

        if (unit.hp() <= 80 || A.chance(60)) {
            target = targets.nearestTo(unit);
        }
        else {
            target = targets.random();
        }

        unit.attackUnit(target);
        return true;
    }
}

