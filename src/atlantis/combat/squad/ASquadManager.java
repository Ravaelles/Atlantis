package atlantis.combat.squad;

import atlantis.combat.squad.alpha.Alpha;
import atlantis.combat.squad.beta.Beta;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

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
        } else {
            removeBeta();
        }

        return false;
    }

    // =========================================================
    // Beta

    private static boolean shouldHaveBeta() {
        return Count.ourCombatUnits() >= 24 && Have.main();
    }

    private static void handleReinforcements(Squad squad) {
        int wantsMoreUnits = squad.wantsMoreUnits();

        if (wantsMoreUnits > 0) {
            transferFromAlphaTo(squad, wantsMoreUnits);
        }
    }

    private static void removeBeta() {
        Alpha alpha = Alpha.get();
        Beta beta = Beta.get();

        for (int i = 0; i < beta.size(); i++) {
            AUnit transfer = beta.get(0);
            transferUnitToSquad(transfer, alpha);
        }
    }

    // =========================================================

    private static void transferFromAlphaTo(Squad toSquad, int assignThisManyFromAlpha) {
        Alpha alpha = Alpha.get();
        alpha.sortByDistanceTo(toSquad.center(), true);
        assignThisManyFromAlpha = Math.min(assignThisManyFromAlpha, alpha.size());

        for (int i = 0; i < assignThisManyFromAlpha; i++) {
            AUnit transfer = alpha.get(0);
            transferUnitToSquad(transfer, toSquad);
        }
    }

    private static void transferUnitToSquad(AUnit unit, Squad toSquad) {
        if (unit.squad() != null) {
            unit.squad().removeUnit(unit);
        }

        toSquad.addUnit(unit);
        unit.setSquad(toSquad);
    }

    public static void removeUnitFromSquads(AUnit unit) {
        Squad squad = unit.squad();

//        if (unit.isOur() && unit.isCombatUnit()) {
//            System.out.println("unit destroyed " + unit + " // " + (squad != null ? squad.name() : null));
//        }
        if (squad != null) {
            squad.removeUnit(unit);
            unit.setSquad(null);
        }
    }

    public static ArrayList<Squad> allSquads() {
        return (ArrayList<Squad>) squads.clone();
    }

}
