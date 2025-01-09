package atlantis.combat.squad.bravo;

import atlantis.combat.missions.Mission;
import atlantis.combat.missions.Missions;
import atlantis.combat.missions.attack.FoundEnemyExposedExpansion;
import atlantis.combat.squad.Squad;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.A;
import atlantis.information.generic.Army;
import atlantis.util.We;

/**
 * Bravo is additional battle squad created when there are too many units in Alpha.
 * Its mission is the same as Alpha's. The goal is to split units into two groups.
 */
public class Bravo extends Squad {
    protected static Bravo bravo = null;

    // =========================================================

    private Bravo() {
        super("Bravo", Missions.initialMission());
    }

    /**
     * Get first, main squad of units.
     */
    public static Bravo get() {
        if (bravo == null) {
            bravo = new Bravo();
        }

        return bravo;
    }

    // =========================================================

    @Override
    public boolean shouldHaveThisSquad() {
        return ShouldHaveBravo.shouldHave();
    }

    @Override
    public boolean allowsSideQuests() {
        return true;
    }

    @Override
    public Mission mission() {
        return Missions.ATTACK;
    }

    // =========================================================

    public int expectedUnits() {
        return A.inRange(6, Alpha.count() / 3, 14);
    }

    public static int count() {
        return get().size();
    }
}
