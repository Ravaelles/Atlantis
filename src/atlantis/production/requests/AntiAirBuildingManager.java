package atlantis.production.requests;

import atlantis.information.decisions.OurStrategicBuildings;
import atlantis.map.position.HasPosition;
import atlantis.production.Requirements;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.orders.build.AddToQueue;
import atlantis.production.requests.protoss.ProtossPhotonCannonAntiAir;
import atlantis.production.requests.zerg.ZergSporeColony;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;

public abstract class AntiAirBuildingManager extends DynamicBuildingManager {

    private static AntiAirBuildingManager instance = null;

    // =========================================================

    public boolean handleBuildNew() {
        if (We.terran()) {
            return false;
        }

//        System.err.println("Should build new Spore?");

        if (shouldBuildNew()) {
            System.err.println("ENQUEUE NEW Spore Colony");
            return requestOne(null);
        }

        return false;
    }

    // =========================================================

    @Override
    public boolean shouldBuildNew() {
        if (!Requirements.hasRequirements(type())) {
            return false;
        }

        int existing = Count.existingOrInProductionOrInQueue(type());
        return existing < OurStrategicBuildings.antiLandBuildingsNeeded();
    }

    public int expectedUnits() {
        return 3 * Select.ourBases().count();
    }

    @Override
    public boolean requestOne(HasPosition at) {
        AUnitType building = type();

        if (at == null) {
            for (AUnit base : Select.ourBases().list()) {
                int numberOfAntiAirBuildingsNearBase = ConstructionRequests.countExistingAndPlannedInRadius(
                        building, 8, base.position()
                );

                for (int i = 0; i < expectedUnits() - numberOfAntiAirBuildingsNearBase; i++) {
                    AddToQueue.withTopPriority(building, base.position());
                }
            }
        }

        if (at != null) {
            AddToQueue.withTopPriority(building, at);
            return true;
        }

        return false;
    }

    /**
     * Quick air units are: Mutalisk, Wraith, Protoss Scout.
     */
//    public void requestAntiAirQuick(APosition where) {
//        AUnitType building = type();
////        int antiAirBuildings = ConstructionRequests.countExistingAndPlannedConstructions(building);
//
//        // === Ensure we have required units ========================================
//
//        int requiredParents = ConstructionRequests.countExistingAndNotFinished(building.whatIsRequired());
//        if (requiredParents == 0) {
//            AddToQueue.withHighPriority(building.whatIsRequired());
//            return;
//        }
//
//        // === Protect every base ==========================================
//
//        for (AUnit base : Select.ourBases().list()) {
//            int numberOfAntiAirBuildingsNearBase = ConstructionRequests.countExistingAndPlannedInRadius(
//                    building, 8, base.position()
//            );
//
//            for (int i = 0; i < expectedUnits() - numberOfAntiAirBuildingsNearBase; i++) {
//                AddToQueue.withTopPriority(building, base.position());
//            }
//        }
//    }

    // =========================================================

    public static AntiAirBuildingManager get() {
        if (instance == null) {
            if (We.zerg()) {
                return instance = new ZergSporeColony();
            }
            else if (We.protoss()) {
                return instance = new ProtossPhotonCannonAntiAir();
            }
//            else if (We.terran()) {
//                return instance = new AntiAirBuildingManager();
//            }
        }

        return instance;
    }

    public int existing() {
        return Count.ourOfTypeWithUnfinished(type());
    }
}
