package atlantis.combat.squad.beta;

import atlantis.combat.missions.Missions;
import atlantis.combat.squad.Squad;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.A;
import atlantis.information.enemy.EnemyWhoBreachedBase;
import atlantis.units.select.Count;

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
        return Alpha.count() >= 20;
    }

    // =========================================================

    public int expectedUnits() {
        return 0;
    }

    public static int count() {
        return get().size();
    }
}
