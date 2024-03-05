package atlantis.information.strategy.response.protoss;

import atlantis.map.choke.Chokes;
import atlantis.production.orders.production.queue.add.AddToQueueToHave;
import atlantis.production.requests.ProductionRequests;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

public class RequestProtossDetection {
    public static boolean needDetectionAgainst(AUnit enemy) {
//        OurStrategicBuildings.setDetectorsNeeded(2);
//        ProductionRequests.getInstance().requestDetectorQuick(Chokes.mainChokeCenter());

//        return requestAtLeastOneObserver();

        return requestAtLeastOneCannon() || requestAtLeastOneObserver();
    }

    private static boolean requestAtLeastOneObserver() {
        AddToQueueToHave.haveAtLeastOneWithTopPriority(AUnitType.Protoss_Robotics_Facility, 1);
        AddToQueueToHave.haveAtLeastOneWithTopPriority(AUnitType.Protoss_Observatory, 1);
        AddToQueueToHave.haveAtLeastOneWithTopPriority(AUnitType.Protoss_Observer, 1);
        return true;

//        return AddToQueueToHave.haveAtLeastOneWithTopPriority(AUnitType.Protoss_Robotics_Facility, 1)
//            || AddToQueueToHave.haveAtLeastOneWithTopPriority(AUnitType.Protoss_Observatory, 1)
//            || AddToQueueToHave.haveAtLeastOneWithTopPriority(AUnitType.Protoss_Observer, 1);
    }

    private static boolean requestAtLeastOneCannon() {
        if (Have.no(AUnitType.Protoss_Forge)) {
            return whenNoForge();
        }

        return whenHaveForge();
    }

    private static boolean whenHaveForge() {
        int expectedCannons = minCannons();
        int cannons = Count.withPlanned(AUnitType.Protoss_Photon_Cannon);
        int needNewCannons = expectedCannons - cannons;
        if (cannons >= expectedCannons) return false;

        AddToQueueToHave.haveAtLeastOneWithTopPriority(AUnitType.Protoss_Photon_Cannon, needNewCannons);

        return needNewCannons > 0;
    }

    private static boolean whenNoForge() {
        boolean result = AddToQueueToHave.haveAtLeastOneWithTopPriority(AUnitType.Protoss_Forge, 1);

        return result || AddToQueueToHave.haveAtLeastOneWithTopPriority(AUnitType.Protoss_Photon_Cannon, minCannons());
    }

    private static int minCannons() {
        return 2;
    }
}
