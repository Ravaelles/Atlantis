package atlantis.combat.retreating.protoss;

import atlantis.units.AUnit;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class ProtossSmallScaleRetreat {
    protected static boolean shouldSmallScaleRetreat(AUnit unit, Selection friends, Selection enemies) {
        if (unit.isRanged()) return asRanged(unit, friends, enemies);
        if (unit.isMelee()) return asMelee(unit, friends, enemies);

        return false;
    }

    private static boolean asMelee(AUnit unit, Selection friends, Selection enemies) {
        if (meleeOverpoweredInRadius(unit, friends, enemies, 1.1)) return true;
        if (meleeOverpoweredInRadius(unit, friends, enemies, 3.1)) return true;

        return false;
    }

    private static boolean meleeOverpoweredInRadius(
        AUnit unit, Selection friends, Selection enemies, double radius
    ) {
        return friends.inRadius(radius, unit).atMost(1 + meleeEnemiesStrength(unit, enemies, radius));
    }

    private static int meleeEnemiesStrength(AUnit unit, Selection enemies, double radius) {
        return (int) (enemies.melee().inRadius(radius, unit).count() * meleeEnemiesMultiplier());
    }

    private static double meleeEnemiesMultiplier() {
        if (Enemy.protoss()) return 1;
        else if (Enemy.terran()) return 0.7;
        return 0.4;
    }

    private static boolean asRanged(AUnit unit, Selection friends, Selection enemies) {
        if (enemies.onlyMelee() && unit.shieldDamageAtMost(30)) return false;

        if (unit.combatEvalRelative() <= 106 && unit.friendsInRadiusCount(5) < enemies.count()) return true;

        return false;
    }
}
