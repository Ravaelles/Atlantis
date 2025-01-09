package atlantis.protoss.zealot;

import atlantis.architecture.Manager;
import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class ProtossZealotLongNotAttacked extends Manager {
    public static final double MIN_S = 0;
    private AUnit enemy;

    public ProtossZealotLongNotAttacked(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isZealot()) return false;
        if (unit.isAttacking()) return false;
        if (unit.cooldown() >= 5) return false;
        if (unit.eval() <= 0.95) return false;

        if (Enemy.zerg() && unit.isMissionDefendOrSparta() && unit.hp() >= 41) return true;

        if (unit.eval() >= 0.8 && unit.distToBase() <= 7) return true;
        if (alwaysAttackWhenEnemyInRange()) return true;

        if (unit.isRetreating()) return false;
        if (!unit.hasValidTarget()) return false;

        if (unit.cooldown() <= 3 && unit.enemiesNear().groundUnits().canAttack(unit, 0).notEmpty()) return true;

        if (!unit.isTargetInWeaponRangeAccordingToGame()) return false;

        if (unit.isActiveManager(this.getClass()) && unit.lastActionLessThanAgo(60)) return true;

        return unit.shields() >= 2 && canAttackNow();
    }

    private boolean alwaysAttackWhenEnemyInRange() {
        return unit.hp() >= 40
            && !unit.shotSecondsAgo(2)
            && unit.meleeEnemiesNearCount(1.05) == 1
            && unit.rangedEnemiesCount(2.1) == 0;
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
        if (unit.hp() <= (Enemy.protoss() ? 16 : 10)) return false;

        if (
            unit.shieldWound() > 20
                && unit.isRetreating()
                && unit.enemiesNear().canBeAttackedBy(unit, 0.2).empty()
        ) return false;

        double shotSecondsAgo = unit.shotSecondsAgo();
        if (shotSecondsAgo >= (2 + unit.woundPercent() / 40.0)) return true;

        return false;
    }

    private AUnit enemy() {
        Selection enemies = unit.enemiesNear().realUnitsAndCombatBuildings().inRadius(1.05, unit).notDeadMan();

        AUnit target = enemies.canBeAttackedBy(unit, 0).mostWounded();
        if (target != null) return target;

//        target = enemies.canBeAttackedBy(unit, -0.5).nearestTo(unit);
//        if (target != null) return target;

        return enemies.canBeAttackedBy(unit, -0.05).nearestTo(unit);
    }
}
