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
import atlantis.util.log.ErrorLog;

public class FindPosition {
    public static APosition findForBuilding(
        AUnit builder, AUnitType building, Construction construction, HasPosition nearTo, double maxDistance
    ) {
        if (nearTo == null && building.isSupplyDepot()) nearTo = Select.ourOfType(AUnitType.Terran_Supply_Depot).last();

        if (maxDistance <= 5 && building.isBunker()) maxDistance = 10;
        if (maxDistance < 0) maxDistance = 50;
        construction.setMaxDistance(maxDistance);

        // === GAS extracting buildings ============================

        if (building.isGasBuilding()) return GasBuildingPositionFinder.findPositionForGasBuilding(building, nearTo);

            // === Base ================================================

        else if (building.isBase()) {
            return forNewBase(builder, building, construction, nearTo);
        }

        // =========================================================

        else if (building.isSupplyDepot()) {
            APosition position = SupplyDepotPositionFinder.findPosition(builder, construction, nearTo);
            if (position != null) return position;
        }

        // === Combat building =====================================

        else if (building.isCombatBuilding()) {
            APosition position = forCombatBuilding(builder, building, construction, nearTo, maxDistance);
            if (position != null) return position;
        }

        // =========================================================
        // STANDARD BUILDINGS

        // If we didn't specify location where to build, build somewhere near the main base
        nearTo = defineNearTo(nearTo);

        // Hopeless case, all units have died, just quit.
        if (nearTo == null) {
            ErrorLog.printMaxOncePerMinute("nearTo is still null for " + building);
            return null;
        }

        // =========================================================
        // Standard place

        return APositionFinder.findStandardPosition(builder, building, nearTo, maxDistance);
    }

    private static HasPosition defineNearTo(HasPosition nearTo) {
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
        return nearTo;
    }

    private static APosition forNewBase(AUnit builder, AUnitType building, Construction construction, HasPosition nearTo) {
        if (We.zerg()) {
            if (Count.larvas() == 0 || Count.bases() >= 3) {
                return APositionFinder.findStandardPosition(builder, building, nearTo, 30);
            }
        }

        return FindPositionForBaseNearestFree.find(building, builder, construction);
    }

    private static APosition forCombatBuilding(
        AUnit builder, AUnitType building, Construction construction, HasPosition nearTo, double maxDistance
    ) {
        if (building.isBunker()) {
            return TerranBunkerPositionFinder.findPosition(builder, construction, nearTo);
        }

        // =========================================================
        // Creep colony

        else if (building.is(AUnitType.Zerg_Creep_Colony)) {
            return ZergCreepColony.findPosition(building, builder, construction);
        }

        return null;
    }
}
