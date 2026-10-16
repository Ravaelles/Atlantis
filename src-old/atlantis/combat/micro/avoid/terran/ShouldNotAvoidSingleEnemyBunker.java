package atlantis.combat.micro.avoid.terran;

import atlantis.units.AUnit;

public class ShouldNotAvoidSingleEnemyBunker {
    public static boolean check(AUnit unit, AUnit bunker) {
        if (!bunker.isBunker()) return false;

        if (unit.lastUnderAttackLessThanAgo(30 * 3)) return false;

        return unit.shields() >= 21 || unit.lastUnderAttackLessThanAgo(30 * 6);
    }
}
