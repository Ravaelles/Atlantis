package atlantis.combat.micro.avoid.zerg;

import atlantis.units.AUnit;

public class ShouldAlwaysAvoidAsZerg {

    private static int friendsVeryNear;
    private static int meleeEnemiesVeryNear;

    public static boolean shouldAlwaysAvoid(AUnit unit) {
        if (shouldSkip(unit)) {
            return false;
        }

        friendsVeryNear = unit.friendsInRadiusCount(2);
        meleeEnemiesVeryNear = unit.meleeEnemiesNearCount(3);

        if (asZergling(unit)) {
            return true;
        }

        return false;
    }

    private static boolean asZergling(AUnit unit) {
        if (!unit.isZergling()) {
            return false;
        }

        return unit.hp() <= 22 && friendsVeryNear <= 65 && meleeEnemiesVeryNear > 0;
    }

    private static boolean shouldSkip(AUnit unit) {
        return !unit.isZerg();
    }
}
