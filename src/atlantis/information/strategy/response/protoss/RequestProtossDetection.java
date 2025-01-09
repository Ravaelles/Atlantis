package atlantis.information.strategy.response.protoss;

import atlantis.game.A;
import atlantis.production.orders.production.queue.add.AddToQueueToHave;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.game.player.Enemy;

public class RequestProtossDetection {
    public static boolean needDetectionAgainst(AUnit enemy) {
        if (requestCannons() && requestAtLeastOneObserver()) {
            return true;
        }

        return false;
    }

    private static boolean requestAtLeastOneObserver() {
        int atSupply = A.supplyUsed() <= 55 ? 55 : 30;

        if (Have.notEvenPlanned(AUnitType.Protoss_Cybernetics_Core)) {
            AddToQueueToHave.haveAtLeastOneWithTopPriority(AUnitType.Protoss_Cybernetics_Core, 1, atSupply);
        }
        AddToQueueToHave.haveAtLeastOneWithTopPriority(AUnitType.Protoss_Robotics_Facility, 1, atSupply);
        AddToQueueToHave.haveAtLeastOneWithTopPriority(AUnitType.Protoss_Observatory, 1, atSupply);

        if (Count.observers() <= 1) {
            AddToQueueToHave.haveAtLeastOneWithTopPriority(AUnitType.Protoss_Observer, 1, atSupply);
        }

        return true;

//        return AddToQueueToHave.haveAtLeastOneWithTopPriority(AUnitType.Protoss_Robotics_Facility, 1)
//            || AddToQueueToHave.haveAtLeastOneWithTopPriority(AUnitType.Protoss_Observatory, 1)
//            || AddToQueueToHave.haveAtLeastOneWithTopPriority(AUnitType.Protoss_Observer, 1);
    }

    private static boolean requestCannons() {
        if (Have.no(AUnitType.Protoss_Forge)) {
            return cannonsWhenNoForge();
        }

        return cannonsWhenHaveForge();
    }

    private static boolean cannonsWhenHaveForge() {
        int expectedCannons = minCannons();
        int cannons = Count.withPlanned(AUnitType.Protoss_Photon_Cannon);
        int needNewCannons = expectedCannons - cannons;
        if (cannons >= expectedCannons) return false;

        AddToQueueToHave.haveAtLeastOneWithTopPriority(AUnitType.Protoss_Photon_Cannon, needNewCannons);

        return needNewCannons > 0;
    }

    private static boolean cannonsWhenNoForge() {
        boolean result = AddToQueueToHave.haveAtLeastOneWithTopPriority(AUnitType.Protoss_Forge, 1);

        return AddToQueueToHave.haveAtLeastOneWithTopPriority(AUnitType.Protoss_Photon_Cannon, minCannons());
    }

    private static int minCannons() {
        if (Enemy.terran()) return 1;

        return 2;

//        int base = Count.ourCombatUnits() >= 8 ? 1 : 2;
//
//        return base + (Enemy.zerg() && Count.observers() == 0 ? 1 : 0);
    }
}
