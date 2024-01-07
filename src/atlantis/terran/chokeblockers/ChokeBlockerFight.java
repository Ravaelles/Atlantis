package atlantis.terran.chokeblockers;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.combat.micro.attack.ProcessAttackUnit;
import atlantis.information.enemy.EnemyWhoBreachedBase;
import atlantis.units.AUnit;

public class ChokeBlockerFight extends Manager {
    public ChokeBlockerFight(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return !unit.isScv()
            && unit.lastUnderAttackLessThanAgo(40);
    }

    @Override
    public Manager handle() {
        AUnit enemyInRange = enemyInRange();
        if (enemyInRange != null) {
            if ((new ProcessAttackUnit(unit)).processAttackOtherUnit(enemyInRange)) return usedManager(this);
        }

        AUnit breachedBase = EnemyWhoBreachedBase.get();
        if (breachedBase != null && unit.canAttackTarget(breachedBase)) {
            if ((new ProcessAttackUnit(unit)).processAttackOtherUnit(breachedBase)) return usedManager(this);
        }

        return null;
    }

    private AUnit enemyInRange() {
        return unit.enemiesNear().inRadius(1.02, unit).groundUnits().mostWounded();
    }
}
