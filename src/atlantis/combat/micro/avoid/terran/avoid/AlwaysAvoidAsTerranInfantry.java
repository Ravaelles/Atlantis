package atlantis.combat.micro.avoid.terran.avoid;

import atlantis.combat.micro.avoid.terran.fight.MarineCanAttackNearEnemy;
import atlantis.units.AUnit;
import atlantis.game.player.Enemy;

public class AlwaysAvoidAsTerranInfantry {
    private static AUnit unit;

    public static boolean asInfantry(AUnit unit) {
        if (!unit.isTerranInfantry()) return false;
        AlwaysAvoidAsTerranInfantry.unit = unit;

        if (alwaysVsProtoss(unit)) return true;
        if (alwaysVsZerg()) return true;

        return !MarineCanAttackNearEnemy.allowedForThisUnit(unit);
    }

    private static boolean alwaysVsProtoss(AUnit unit) {
        if (!Enemy.protoss()) return false;
        if (unit.hp() >= 23) return false;

        if (unit.cooldown() >= 5) return true;
        if (unit.meleeEnemiesNearCount(2.9) >= 2) return true;
        if (unit.meleeEnemiesNearCount(3.5) >= 3) return true;

        return unit.enemiesNear().ranged().canAttack(unit, 1.6).notEmpty();
    }

    private static boolean alwaysVsZerg() {
        if (!Enemy.zerg()) return false;
        if (unit.hasMedicInRange() && unit.hp() >= 26) return false;

        return unit.eval() <= 1.2
            && unit.enemiesNear().ranged().canAttack(unit, 2.2).notEmpty();
    }
}
