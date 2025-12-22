package atlantis.production.dynamic.reinforce.protoss;

import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.production.dynamic.protoss.ProtossSecureBasesCommander;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.game.player.Enemy;
import atlantis.units.select.Select;

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
        int minSupplyForSecondCannon = Enemy.terran() ? 100 : 85;
        int minerals = A.minerals();

        int total = (A.supplyTotal() >= minSupplyForSecondCannon ? 2 : 1)
            + (minerals >= 540 ? 1 : 0)
            + (Enemy.zerg() && A.supplyTotal() >= 130 ? 1 : 0)
            + (Enemy.zerg() && A.supplyTotal() >= 180 ? 1 : 0)
            + (Enemy.zerg() && A.supplyTotal() >= 190 ? 1 : 0)
            + mutasBonus()
            + (minerals >= 640 ? 1 : 0)
            + (minerals >= 800 ? 1 : 0);

        if (Enemy.protoss()) total = Math.min(total, 5 + (minerals >= 700 ? 1 : 0));
        if (Enemy.terran()) total = Math.min(total, 2);
        if (Enemy.zerg()) total = Math.min(total, 6);

        if (Enemy.zerg() && (A.supplyUsed() <= 150 && A.minerals() <= 550)) total = Math.min(3, total);

        return total;
    }

    private int existingCannonsNearby() {
        return Count.existingOrPlannedBuildingsNear(Protoss_Photon_Cannon, 12, base);
    }

    private static boolean notEnoughCannons(int cannonsNearby) {
        int expectedCannons = expectedCannons();

//        if (cannonsNearby < expectedCannons) A.println(
//            "*************************** CANNONS = " + cannonsNearby + " / EXP: " + expectedCannons
//        );

        return cannonsNearby < expectedCannons;
    }

    private static int mutasBonus() {
        if (!Enemy.zerg()) return 0;

        int mutas = EnemyUnits.mutas();
        if (mutas == 0) return 0;

        return 1 + (mutas / 4) + (A.minerals() / 500);
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
