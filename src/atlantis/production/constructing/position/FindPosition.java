package atlantis.production.constructing.position;

import atlantis.combat.micro.zerg.ZergCreepColony;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.position.base.FindPositionForBaseNearestFree;
import atlantis.production.constructing.position.terran.SupplyDepotPositionFinder;
import atlantis.production.constructing.position.terran.TerranBunkerPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;

public class FindPosition {
    public static APosition findForBuilding(
        AUnit builder, AUnitType building, Construction construction, HasPosition nearTo, double maxDistance
    ) {
        if (nearTo == null && building.isSupplyDepot()) {
            nearTo = Select.ourOfType(AUnitType.Terran_Supply_Depot).last();
        }

        if (building.isBunker() && maxDistance <= 5) {
            maxDistance = 10;
        }

        construction.setMaxDistance(maxDistance);

        // =========================================================
        // GAS extracting buildings

        if (building.isGasBuilding()) {
            return GasBuildingPositionFinder.findPositionForGasBuilding(building, nearTo);
        }

        // =========================================================
        // BASE

        else if (building.isBase()) {
            if (We.zerg()) {
                if (Count.larvas() == 0 || Count.bases() >= 3) {
                    return APositionFinder.findStandardPosition(builder, building, nearTo, 30);
                }
            }

            return FindPositionForBaseNearestFree.find(building, builder, construction);
        }

        // =========================================================

        else if (building.isBunker()) {
            return TerranBunkerPositionFinder.findPosition(builder, construction, nearTo);
        }
        else if (building.isSupplyDepot()) {
            APosition position = SupplyDepotPositionFinder.findPosition(builder, construction, nearTo);
            if (position != null) return position;
        }

        // =========================================================
        // Creep colony

        else if (building.is(AUnitType.Zerg_Creep_Colony)) {
            return ZergCreepColony.findPosition(building, builder, construction);
        }

        // =========================================================
        // STANDARD BUILDINGS

        // If we didn't specify location where to build, build somewhere near the main base
        if (nearTo == null) {
            if (AGame.isPlayingAsZerg()) {
                nearTo = Select.main().position();
            }
            else {
                if (Count.bases() >= 3) {
                    nearTo = Select.ourBases().random();
                }
                else {
                    nearTo = Select.main().position();
                }
            }
        }

        // If all of our bases have been destroyed, build somewhere near our first unit alive
        if (nearTo == null) {
            nearTo = Select.our().first().position();
        }

        // Hopeless case, all units have died, just quit.
        if (nearTo == null) {
            return null;
        }

        if (maxDistance < 0) {
            maxDistance = 50;
        }

        // =========================================================
        // Standard place

        return APositionFinder.findStandardPosition(builder, building, nearTo, maxDistance);
    }
}
