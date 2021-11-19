package atlantis.combat.squad.alpha;

import atlantis.combat.missions.Missions;
import atlantis.combat.squad.Squad;

/**
 * Alpha is battle squad that is the main battle squad. Most new units arrive here (handled by NewUnitsToSquadsAssigner)
 * and can be later transferred to other squads if needed (via ASquadManager).
 */
public class Alpha extends Squad {

    protected static Alpha alpha = null;

    private Alpha() {
        super("Alpha", Missions.getInitialMission());
    }

    /**
     * Get first, main squad of units.
     */
    public static Alpha get() {
        if (alpha == null) {
            alpha = new Alpha();
        }

        return alpha;
    }

    // =========================================================

    public int expectedUnits() {
        return 0;
    }

    public static int count() {
        return get().size();
    }

}
