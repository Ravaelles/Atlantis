package atlantis.combat.generic.enemy_in_range.protoss;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.ProcessAttackUnit;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;
import atlantis.util.We;

public class ProtossRangedAttackEnemiesInRange extends Manager {
    private AUnit enemy;

    public ProtossRangedAttackEnemiesInRange(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.protoss()
            && unit.isRanged()
            && unit.cooldown() >= 3
            && unit.enemiesNear().inRadius(8, unit).notEmpty()
            && unit.enemiesNear().ranged().inRadius(13, unit).empty()
//            && unit.combatEvalRelative() >= 1
            && unit.meleeEnemiesNearCount(2.2 + unit.woundPercent() / 80.0) == 0
//            && unit.lastAttackFrameMoreThanAgo(30 * 2)
            && (unit.shieldDamageAtMost(30) || unit.lastUnderAttackMoreThanAgo(30 * 6))
            && (enemy = enemyInRangeToAttack()) != null;
    }

    private AUnit enemyInRangeToAttack() {
        Selection enemies = unit.enemiesNear();
        
        AUnit nearest = enemies.inShootRangeOf(unit).mostWounded();
        if (nearest != null) return nearest;

        return enemies.nearestTo(unit);
    }

    @Override
    public Manager handle() {
        if ((new ProcessAttackUnit(unit)).processAttackOtherUnit(enemy)) {
//            System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - OK - RangedAttackEnemy");

            unit.addLog("RangedAttackEnemy");
            return usedManager(this);
        }

        return null;
    }
}
