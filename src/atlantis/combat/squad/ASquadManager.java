package atlantis.combat.squad;

import atlantis.combat.squad.alpha.Alpha;
import atlantis.combat.squad.beta.Beta;
import atlantis.units.AUnit;
import atlantis.units.select.Count;

import java.util.ArrayList;

/**
 * Commands all existing battle squads.
 */
public class ASquadManager {

    /** All squads. "Alpha" - main army. After some time "Beta" - always defend main + natural. */
    protected static ArrayList<Squad> squads = new ArrayList<>();

    // =========================================================
    // Manage squads

    public static boolean updateSquadTransfers() {
        if (shouldHaveBeta()) {
            handleReinforcements(Beta.get());
        }

        return false;
    }

    // =========================================================
    // Beta

    private static boolean shouldHaveBeta() {
        return Count.ourCombatUnits() >= 8;
    }

    private static void handleReinforcements(Squad squad) {
        int wantsMoreUnits = squad.wantsMoreUnits();

        if (wantsMoreUnits > 0) {
            transferFromAlphaTo(squad, wantsMoreUnits);
        }
    }

    // =========================================================

    private static void transferFromAlphaTo(Squad toSquad, int assignThisManyFromAlpha) {
        Alpha alpha = Alpha.get();
        alpha.sortByDistanceTo(toSquad.center(), true);
        assignThisManyFromAlpha = Math.min(assignThisManyFromAlpha, alpha.size());

        for (int i = 0; i < assignThisManyFromAlpha; i++) {
            AUnit transfer = alpha.get(0);
            alpha.removeUnit(transfer);
            toSquad.addUnit(transfer);
        }
    }

    public static ArrayList<Squad> allSquads() {
        return (ArrayList<Squad>) squads.clone();
    }
}
