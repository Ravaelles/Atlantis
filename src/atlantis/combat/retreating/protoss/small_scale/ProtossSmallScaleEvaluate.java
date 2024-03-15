package atlantis.combat.retreating.protoss.small_scale;

import atlantis.units.AUnit;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class ProtossSmallScaleEvaluate {
    protected static boolean meleeOverpoweredInRadius(
        AUnit unit, Selection friends, Selection enemies, double radius
    ) {
        return ourMeleeStrength(unit, friends, radius) <= meleeEnemiesStrength(unit, enemies, radius);
    }

    protected static double ourMeleeStrength(AUnit unit, Selection friends, double radius) {
        return friends.melee().inRadius(radius, unit).havingAtLeastHp(30).count()
            + (unit.hp() >= 30 ? 1 : 0.3);
    }

    protected static double meleeEnemiesStrength(AUnit unit, Selection enemies, double radius) {
        return enemies.melee().inRadius(radius, unit).count() * meleeEnemiesMultiplier();
    }

    protected static double meleeEnemiesMultiplier() {
        if (Enemy.protoss()) return 1;
        else if (Enemy.terran()) return 0.7;
        return 0.4;
    }
}
