package atlantis.combat.generic.enemy_in_range;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.combat.micro.attack.ProcessAttackUnit;
import atlantis.units.AUnit;
import atlantis.util.We;

public class ProtossUnitHasEnemyInRange extends Manager {
    private AUnit enemyInRange;

    public ProtossUnitHasEnemyInRange(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.protoss()
            && unit.squad().targeting().lastTargetIfAlive() == null
            && unit.isCombatUnit()
//            && unit.combatEvalRelative() > 1
//            && !unit.hasTarget()
            && unit.hp() >= 40
            && unit.enemiesNear().notEmpty()
            && unit.cooldown() <= 2
            && (enemyInRange = unit.enemiesNear().canBeAttackedBy(unit, 0).mostWounded()) != null
            && allowedToAttackThisEnemy();
    }

    private boolean allowedToAttackThisEnemy() {
        if (unit.isMelee()) return true;

        return unit.distTo(enemyInRange) >= 2.6;
    }

    @Override
    protected Manager handle() {
        if ((new ProcessAttackUnit(unit)).processAttackOtherUnit(enemyInRange)) return usedManager(this);

        return null;
    }
}
