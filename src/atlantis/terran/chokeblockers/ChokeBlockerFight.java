package atlantis.terran.chokeblockers;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.ProcessAttackUnit;
import atlantis.information.enemy.EnemyUnitBreachedBase;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class ChokeBlockerFight extends Manager {
    public ChokeBlockerFight(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.hp() <= 24) return false;

        if (unit.isZealot()) {
            if (
                unit.lastUnderAttackLessThanAgo(10)
                    && unit.distToNearestChoke() <= 2
                    && unit.distToNearestChokeCenter() <= 4
            ) return true;

            if (dontFightAsWoundedZealot()) return false;
            if (anyOtherBlockerIsFighting()) return true;

            return false;
        }

        return !unit.isScv()
            && unit.hp() >= 40;
//            && unit.lastUnderAttackLessThanAgo(40);
    }

    private boolean dontFightAsWoundedZealot() {
//        if (unit.cooldown() >= 5) return false;

        if (unit.hp() <= 35) {
            if (unit.lastUnderAttackLessThanAgo(120)) return true;
            if (unit.friendsInRadiusCount(2) == 0) return true;
        }

        return false;
    }

    @Override
    public Manager handle() {
        AUnit enemyInRange = possibleEnemies().canBeAttackedBy(unit, 0).mostWounded();
        if (enemyInRange != null) {
            if ((new ProcessAttackUnit(unit)).processAttackOtherUnit(enemyInRange)) return usedManager(this);
        }

        AUnit nearestEnemy = possibleEnemies().inRadius(1.5, unit).nearestTo(unit);
        if (nearestEnemy != null) {
            if ((new ProcessAttackUnit(unit)).processAttackOtherUnit(nearestEnemy)) return usedManager(this);
        }

        AUnit breachedBase = EnemyUnitBreachedBase.get();
        if (breachedBase != null && breachedBase.isDetected() && unit.canAttackTarget(breachedBase)) {
            System.err.println("breachedBase = " + breachedBase);
            if ((new ProcessAttackUnit(unit)).processAttackOtherUnit(breachedBase)) return usedManager(this);
        }

        return null;
    }

    private Selection possibleEnemies() {
        APosition chokeCenter = ChokeToBlock.get().center();
        if (chokeCenter == null) return unit.enemiesNear()
            .groundUnits()
            .effVisible();

        return unit.enemiesNear()
            .groundUnits()
            .inRadius(3, chokeCenter)
            .canBeAttackedBy(unit, 4)
            .effVisible();
    }

    private boolean anyOtherBlockerIsFighting() {
        for (AUnit blocker : ChokeBlockersAssignments.get().blockers) {
            if (unit.equals(blocker)) continue;

            if (blocker.isAttacking()) return true;
            if (
                blocker.lastUnderAttackLessThanAgo(10)
                    && blocker.enemiesNear().inRadius(2, unit).notEmpty()
                    && blocker.distToNearestChokeCenter() <= 3
            ) return true;
        }

        return false;
    }

    private AUnit enemyInRange() {
        return possibleEnemies().canBeAttackedBy(unit, maxDistToAttack() - 1).mostWounded();
    }

    private double maxDistToAttack() {
//        int maxEnemies = Enemy.protoss() ? 1 : 3;

//        if (
//            possibleEnemies().inRadius(7, unit).groundUnits().count() <= maxEnemies
//        ) return 1.5;

        return 1.4;
    }
}
