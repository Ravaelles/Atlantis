package atlantis.combat.squad.bravo;

import atlantis.combat.missions.Missions;
import atlantis.combat.missions.attack.FoundEnemyExposedExpansion;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnitBreachedBase;
import atlantis.information.generic.Army;
import atlantis.units.AUnit;
import atlantis.util.We;

public class ShouldHaveBravo {
    public static final int ALPHA_COUNT_THRESHOLD = 21;
    public static boolean _prev = false;

    public static boolean shouldHave() {
        //        if (true) return false;

        if (Missions.isGlobalMissionAttack()) {
            AUnit enemy = EnemyUnitBreachedBase.get();
            if (enemy != null) return t("BreachedBase");
        }

        if (We.terran() && A.supplyUsed() <= 170) return f("Terran and low supply");

        if (Alpha.count() >= ALPHA_COUNT_THRESHOLD + (_prev ? -4 : 0)) return t("Alpha count: " + Alpha.count());

        if (
            Alpha.count() >= ALPHA_COUNT_THRESHOLD
                && FoundEnemyExposedExpansion.getItFound() != null
                && (Alpha.count() >= 25 || Army.strength() >= 200)
        ) return t("Found expansion and strong");

        if (_prev) {
            if (Alpha.count() <= ALPHA_COUNT_THRESHOLD - 5) return f("Became too weak: " + Alpha.count());
        }

        return f("No need");
    }

    private static boolean t(String reason) {
        if (!_prev) System.err.println(A.minSec() + " - should have BRAVO: " + reason);

        return _prev = true;
    }

    private static boolean f(String reason) {
        if (_prev) System.err.println(A.minSec() + " - remove BRAVO: " + reason);

        return _prev = false;
    }
}
