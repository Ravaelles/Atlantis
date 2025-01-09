package atlantis.production.requests;

import atlantis.map.position.HasPosition;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.requests.protoss.ProtossPhotonCannonAntiAir;
import atlantis.production.requests.zerg.ZergSporeColony;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.We;

public abstract class AntiAirBuildingCommander extends DynamicBuildingCommander {
    protected static AntiAirBuildingCommander instance = null;

    // =========================================================

    public abstract int expected();

    // =========================================================

    @Override
    public boolean shouldBuildNew() {
        return existingWithUnfinished() < expected();
    }

    public boolean requestToBuildNewAntiAirCombatBuilding() {
        if (We.terran()) return false;

        if (shouldBuildNew()) return requestOne(null);

        return false;
    }

    @Override
    public boolean requestOne(HasPosition at) {
        AUnitType buildType = typeToBuildFirst();

        // === Zerg fix - morph existing Creep Colonies ============

        if (We.zerg()) {
            AUnit creep = Select.ourOfType(AUnitType.Zerg_Creep_Colony).first();
            if (creep != null) {
                return creep.morph(buildType, null);
            }
        }

        // =========================================================

        if (at == null) {
            for (AUnit base : Select.ourBases().list()) {
                int numberOfAntiAirBuildingsNearBase = ConstructionRequests.countExistingAndPlannedInRadius(
                    buildType, 8, base.position()
                );

                for (int i = 0; i < expected() - numberOfAntiAirBuildingsNearBase; i++) {
                    AddToQueue.withTopPriority(buildType, base.position());
                }
            }
        }

        if (at != null) {
            AddToQueue.withTopPriority(buildType, at);
            return true;
        }

        return false;
    }

    // =========================================================

    public static AntiAirBuildingCommander get() {
        if (instance == null) {
            if (We.zerg()) {
                return instance = new ZergSporeColony();
            }
            else if (We.protoss()) {
                return instance = new ProtossPhotonCannonAntiAir();
            }
//            else if (We.terran()) {
//                return instance = new AntiAirBuildingCommander();
//            }
        }

        return instance;
    }

}
