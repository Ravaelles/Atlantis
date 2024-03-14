package atlantis.combat.retreating.protoss;

import atlantis.units.AUnit;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class ProtossSmallScaleRetreat {
    public static boolean shouldSmallScaleRetreat(AUnit unit, Selection friends, Selection enemies) {
        if (unit.isRanged()) return asRanged(unit, friends, enemies);
        return asMelee(unit, friends, enemies);
    }

    private static boolean asMelee(AUnit unit, Selection friends, Selection enemies) {
//        if (unit.combatEvalRelative() >= 1.2) return false;

        if (meleeOverpoweredInRadius(unit, friends, enemies, 2.2)) return true;
        if (meleeOverpoweredInRadius(unit, friends, enemies, 3.2)) return true;

        return false;
    }

    private static boolean meleeOverpoweredInRadius(
        AUnit unit, Selection friends, Selection enemies, double radius
    ) {
        return ourMeleeStrength(unit, friends, radius) <= meleeEnemiesStrength(unit, enemies, radius);
    }

    private static double ourMeleeStrength(AUnit unit, Selection friends, double radius) {
        return friends.melee().inRadius(radius, unit).notRunning().count() + (unit.hp() >= 30 ? 1.1 : 1);
    }

    private static double meleeEnemiesStrength(AUnit unit, Selection enemies, double radius) {
        return enemies.melee().inRadius(radius, unit).count() * meleeEnemiesMultiplier();
    }

    private static double meleeEnemiesMultiplier() {
        if (Enemy.protoss()) return 1;
        else if (Enemy.terran()) return 0.7;
        return 0.4;
    }

    private static boolean asRanged(AUnit unit, Selection friends, Selection enemies) {
        if (enemies.onlyMelee() && unit.shieldDamageAtMost(30)) return false;

        if (unit.combatEvalRelative() <= 1.06 && unit.friendsInRadiusCount(5) < enemies.count()) return true;

        return false;
    }
}
