package atlantis.combat.squad.squads.bravo;

import atlantis.combat.missions.Missions;
import atlantis.combat.missions.attack.FoundEnemyExposedExpansion;
import atlantis.combat.missions.drops.ProtossShouldDropToIsland;
import atlantis.combat.squad.squads.alpha.Alpha;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnitBreachedBase;
import atlantis.information.generic.Army;
import atlantis.util.We;

public class ShouldHaveBravo {
    public static final int ALPHA_COUNT_THRESHOLD = 21;
    public static boolean _prev = false;

    public static boolean shouldHave() {
        //        if (true) return false;

        if (ProtossShouldDropToIsland.check()) {
            return t("Drop to island");
        }

        int alphaCount = Alpha.count();
        if (Missions.isGlobalMissionDefendOrSparta()) {
            return f("Always one squad when defending");
        }

        if (EnemyUnitBreachedBase.someone() && !Missions.isGlobalMissionDefendOrSparta()) {
            return t("BreachedBase");
        }

        if (alphaCount < ALPHA_COUNT_THRESHOLD && EnemyUnitBreachedBase.noone()) {
            return f("Way too few units");
        }

//        if (!_prev && alphaCount <= ALPHA_COUNT_THRESHOLD - 2) return f("Too few units");

        if (We.terran() && A.supplyUsed() <= 170) return f("Terran and low supply");

        if (
            alphaCount >= ALPHA_COUNT_THRESHOLD
                && FoundEnemyExposedExpansion.getIfFound() != null
                && (alphaCount >= 25 || Army.strength() >= 200)
        ) return t("Found expansion and strong");

//        if (_prev) {
//            if (alphaCount <= (ALPHA_COUNT_THRESHOLD - 5)) return f("Became too weak: " + alphaCount);
//        }

        if (_prev && EnemyUnitBreachedBase.someone()) return t("Still breached base");

        // Couldn't fix it, let it always be true
        return t("Simply allow it");

//        return alphaCount >= (ALPHA_COUNT_THRESHOLD + (_prev ? -4 : 0))
//            ? t("Alpha count: " + alphaCount)
//            : f("No need");
    }

    private static boolean t(String reason) {
//        if (!_prev) System.err.println(A.minSec() + " - should have BRAVO: " + reason);

        return _prev = true;
    }

    private static boolean f(String reason) {
//        if (_prev) System.err.println(A.minSec() + " - remove BRAVO: " + reason);

        return _prev = false;
    }
}
