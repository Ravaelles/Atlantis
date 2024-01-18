package atlantis.terran.chokeblockers;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.combat.micro.attack.ProcessAttackUnit;
import atlantis.game.A;
import atlantis.information.enemy.EnemyWhoBreachedBase;
import atlantis.units.AUnit;

public class ChokeBlockerFight extends Manager {
    public ChokeBlockerFight(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.hp() <= 24) return false;

        if (unit.isZealot() && A.seconds() <= 220) return true;

        return !unit.isScv()
            && unit.hp() >= 40;
//            && unit.lastUnderAttackLessThanAgo(40);
    }

    @Override
    public Manager handle() {
        AUnit enemyInRange = enemyInRange();
        if (enemyInRange != null) {
            if ((new ProcessAttackUnit(unit)).processAttackOtherUnit(enemyInRange)) return usedManager(this);
        }

        AUnit breachedBase = EnemyWhoBreachedBase.get();
        if (breachedBase != null && breachedBase.isDetected() && unit.canAttackTarget(breachedBase)) {
            if ((new ProcessAttackUnit(unit)).processAttackOtherUnit(breachedBase)) return usedManager(this);
        }

        return null;
    }

    private AUnit enemyInRange() {
        return unit.enemiesNear().groundUnits().canBeAttackedBy(unit, maxDistToAttack() - 1).mostWounded();
    }

    private double maxDistToAttack() {
        if (
            unit.enemiesNear().inRadius(7, unit).groundUnits().count() <= 1
        ) return 3;

        return 1.05;
    }
}
