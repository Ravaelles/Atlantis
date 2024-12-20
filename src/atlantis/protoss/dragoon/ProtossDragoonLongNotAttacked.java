package atlantis.protoss.dragoon;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class ProtossDragoonLongNotAttacked extends Manager {
    public static final double MIN_S = 0;
    private AUnit enemy;

    public ProtossDragoonLongNotAttacked(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isAttacking()) return false;

        if (unit.isActiveManager(this.getClass()) && unit.lastActionLessThanAgo(60)) return true;

        return unit.shields() >= 5 && canAttackNow();
    }

    @Override
    public Manager handle() {
        if ((enemy = enemy()) == null) return null;

        if (unit.attackUnit(enemy)) {
            return usedManager(this);
        }

        return null;
    }

    private boolean canAttackNow() {
        if (unit.cooldown() >= 4) return false;
        if (unit.hp() <= 20) return false;

        double shotSecondsAgo = unit.shotSecondsAgo();
//        if (shotSecondsAgo <= 3) return false;
        if (shotSecondsAgo >= 7) return true;

        if (Enemy.zerg()) {
            if (unit.shieldWound() >= 20 && unit.meleeEnemiesNearCount(1.8) >= 2) return false;
            if (unit.meleeEnemiesNearCount(2.9) >= 3) return false;
            if (unit.meleeEnemiesNearCount(3.4) >= 4) return false;
        }

//        if (shotSecondsAgo >= 2 && unit.hp() >= 41) return true;
        if (unit.cooldown() <= 4 && unit.hp() >= 41 && unit.friendsInRadiusCount(7) >= 8) return true;

        if (
            unit.hp() >= 35
                && unit.lastStartedRunningMoreThanAgo(60)
                && unit.lastUnderAttackLessThanAgo(15)
        ) return true;

        if (unit.hp() <= 21 && shotSecondsAgo <= 4.5) return false;
        if (unit.hp() <= 61 && shotSecondsAgo <= 6) return false;

//        Selection enemiesThatCanAttackMeWithMargin = unit.enemiesThatCanAttackMe(2.5).havingAtLeastHp(8);
        Selection enemiesThatCanAttackMeWithMargin = unit.meleeEnemiesNear().inRadius(2.7, unit);

        boolean nooneThatCanAttackMe = enemiesThatCanAttackMeWithMargin.empty();
        if (nooneThatCanAttackMe) return true;

        if (shotSecondsAgo >= 5) return true;

        if (unit.meleeEnemiesNearCount(2.6) >= 2) return false;

        if (unit.combatEvalRelative() < 0.6 && enemiesThatCanAttackMeWithMargin.atLeast(2)) return false;

//        System.err.println("shotSecondsAgo = " + shotSecondsAgo);
//        unit.paintTextCentered(unit, "SS=" + A.digit(shotSecondsAgo), Color.Red);
//        if (shotSecondsAgo <= MIN_S) return false;

        double safetyMargin = 0.6 + unit.woundPercent() / 40.0;

        return (shotSecondsAgo >= 2 && unit.enemiesThatCanAttackMe(safetyMargin).havingAtLeastHp(8).empty())
            || nooneThatCanAttackMe;
    }

    private AUnit enemy() {
        Selection enemies = unit.enemiesNear().realUnitsAndCombatBuildings().notDeadMan();

        AUnit target = enemies.canBeAttackedBy(unit, 0).mostWounded();
        if (target != null) return target;

//        target = enemies.canBeAttackedBy(unit, -0.5).nearestTo(unit);
//        if (target != null) return target;

        return enemies.canBeAttackedBy(unit, -0.05).nearestTo(unit);
    }
}
