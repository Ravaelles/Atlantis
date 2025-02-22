package atlantis.combat.retreating.protoss.big_scale;

import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;

public class ProtossRetreatFromSunken {
    public static Decision decision(AUnit unit) {
        if (!hasSunkenInRadius(unit, 17)) return Decision.INDIFFERENT;

        if (retreatVsSunken(unit)) return Decision.TRUE;
        if (dontRetreatVsSunken(unit)) return Decision.FALSE;

        return Decision.INDIFFERENT;
    }

    private static boolean hasSunkenInRadius(AUnit unit, double radius) {
        AUnit sunken = EnemyUnits.buildings().sunkens().groundNearestTo(unit);

        if (sunken == null) return false;

        return sunken.groundDist(unit) <= radius;
    }

    private static boolean retreatVsSunken(AUnit unit) {
        if (unit.squadSize() >= 15) return false;
//        if (unit.lastRetreatedAgo() <= 30 * 5) return false;
        if (A.s % 20 <= 5 && unit.eval() <= 1.2) return true;

        return false;
    }

    private static boolean dontRetreatVsSunken(AUnit unit) {
        if (unit.squadSize() <= 4) return false;
        if (unit.lastRetreatedAgo() <= 30 * 12) return false;

        return unit.eval() >= 0.95;
    }
}
