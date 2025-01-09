package atlantis.combat.retreating.terran;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Count;

public class TerranShouldFullRetreat {
    private static AUnit unit;

    public static boolean shouldFullRetreat(AUnit unit) {
        if (A.isUms() && Count.bases() == 0) return false;

        TerranShouldFullRetreat.unit = unit;

        if (unit.distToBunker() <= 2.5) return false;

        return unit.eval() <= 0.9;
    }

}
