package atlantis.combat.squad.beta;

import atlantis.combat.missions.Missions;
import atlantis.combat.missions.attack.FoundEnemyExposedExpansion;
import atlantis.combat.squad.Squad;
import atlantis.combat.squad.alpha.Alpha;

/**
 * Bravo is additional battle squad created when there are too many units in Alpha.
 * Its mission is the same as Alpha's. The goal is to split units into two groups.
 */
public class Bravo extends Squad {
    public static final int ALPHA_COUNT_THRESHOLD = 16;

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
        return Alpha.count() > ALPHA_COUNT_THRESHOLD
            || FoundEnemyExposedExpansion.getItFound() != null;
    }

    @Override
    public boolean allowsSideQuests() {
        return true;
    }

    // =========================================================

    public int expectedUnits() {
        return Math.max(3, Alpha.count() - ALPHA_COUNT_THRESHOLD);
    }

    public static int count() {
        return get().size();
    }
}
