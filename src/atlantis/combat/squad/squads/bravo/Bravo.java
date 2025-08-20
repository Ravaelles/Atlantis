package atlantis.combat.squad.squads.bravo;

import atlantis.combat.missions.Mission;
import atlantis.combat.missions.Missions;
import atlantis.combat.squad.Squad;
import atlantis.combat.squad.squads.alpha.Alpha;
import atlantis.game.A;

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
        return A.inRange(3, Alpha.count() / 4, 14);
    }

    public static int count() {
        return get().size();
    }
}
