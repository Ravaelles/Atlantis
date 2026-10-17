package atlantis.production.dynamic.protoss.reinforce;

import atlantis.game.A;
import atlantis.decisions.goals.protoss.CannonGoal;
import atlantis.production.GoalBoardAdvisor;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.dynamic.protoss.ProtossSecureBasesCommander;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.game.player.Enemy;
import atlantis.units.select.Select;

import atlantis.util.AConsole;
import static atlantis.units.AUnitType.Protoss_Photon_Cannon;

public class ShouldSecureProtossBase {
    private final AUnit base;

    public ShouldSecureProtossBase(AUnit base) {
        this.base = base;
    }

    public boolean needsSecuring() {
        if (skipReinforcingMainBase(base)) return false;

        int cannonsNearby = existingCannonsNearby();

        return notEnoughCannons(cannonsNearby);
    }

    private static int expectedCannons() {
        // The number lives on the board (CannonGoal): same matrix, pinned in
        // stub tests, read here through a fresh snapshot. Per-base placement
        // below is untouched - the board names the total, never the site.
        return CannonGoal.wanted(GoalBoardAdvisor.snapshot());
    }

    private int existingCannonsNearby() {
        return Count.existingOrPlannedBuildingsNear(Protoss_Photon_Cannon, 12, base)
            + ConstructionRequests.countPreparingOfTypeInRadius(Protoss_Photon_Cannon, 12, base);
    }

    private static boolean notEnoughCannons(int cannonsNearby) {
        int expectedCannons = expectedCannons();

//        if (cannonsNearby < expectedCannons) AConsole.println(
//            "*************************** CANNONS = " + cannonsNearby + " / EXP: " + expectedCannons
//        );

        return cannonsNearby < expectedCannons;
    }

    private boolean skipReinforcingMainBase(AUnit base) {
        return base.isMainBase() && consideredMutasAndItsOk();
    }

    private static boolean consideredMutasAndItsOk() {
        if (Enemy.zerg() && A.supplyUsed() >= 110) return false;
        if (A.supplyUsed() >= 90 && Select.main().friendsNear().cannons().atLeast(1)) return false;

        return !ProtossSecureBasesCommander.hasMutas();
    }
}
