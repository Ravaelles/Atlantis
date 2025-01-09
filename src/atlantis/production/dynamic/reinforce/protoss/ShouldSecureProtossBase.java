package atlantis.production.dynamic.reinforce.protoss;

import atlantis.game.A;
import atlantis.production.dynamic.protoss.ProtossSecureBasesCommander;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.game.player.Enemy;

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

    private int existingCannonsNearby() {
        return Count.existingOrPlannedBuildingsNear(Protoss_Photon_Cannon, 12, base);
    }

    private static boolean notEnoughCannons(int cannonsNearby) {
        return cannonsNearby < minCannons();
    }

    private static int minCannons() {
        int minSupplyForSecondCannon = Enemy.terran() ? 100 : 85;

        int total = (A.supplyTotal() >= minSupplyForSecondCannon ? 2 : 1)
            + (A.hasMinerals(530) ? 1 : 0)
            + (Enemy.zerg() && A.supplyTotal() >= 110 ? 1 : 0)
            + (A.hasMinerals(740) ? 1 : 0);

        if (Enemy.terran()) total = Math.min(total, 2);

        return total;
    }

    private boolean skipReinforcingMainBase(AUnit base) {
        return base.isMainBase() && consideredMutasAndItsOk();
    }

    private static boolean consideredMutasAndItsOk() {
        if (Enemy.zerg() && A.supplyUsed() >= 110) return false;

        return !ProtossSecureBasesCommander.hasMutas();
    }
}
