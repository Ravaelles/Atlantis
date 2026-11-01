package atlantis.production.constructions.position;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.ReachableTiles;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.position.base.FindPositionForBase;
import atlantis.production.constructions.position.protoss.FindPositionForCannon;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.workers.FreeWorkers;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

public class FindPosition {
//    public static APosition findForBuildingNear(AUnitType building, HasPosition near) {
//        return findForBuilding(null, building, null, near, 8);
//    }

    public static APosition findForBuilding(
        AUnit builder, AUnitType building, Construction construction, HasPosition nearTo, double maxDistance
    ) {
        return findForBuildingRaw(builder, building, construction, nearTo, maxDistance);

//        APosition raw = findForBuildingRaw(builder, building, construction, nearTo, maxDistance);
//        if (raw == null) return null;
//        if (building.isBase() || building.isGasBuilding()) return raw;
//
//        return raw.translateByTiles(-building.dimensionLeftTiles(), -building.dimensionUpTiles());
    }

    private static APosition findForBuildingRaw(
        AUnit builder, AUnitType building, Construction construction, HasPosition nearTo, double maxDistance
    ) {
        AbstractPositionFinder._STATUS = "Reset";

        // =========================================================

        nearTo = DefineNearTo.defineNearTo(building, nearTo);
//        System.err.println("nearTo = " + nearTo + " for " + building + " / main = " + Select.mainOrAnyBuildingPosition());

        if (builder == null) builder = defineBuilder();

        if (maxDistance < 0) maxDistance = MaxBuildingDist.MAX_DIST;

        if (construction != null) construction.setMaxDistance(maxDistance);

        // === GAS extracting buildings ============================

        if (building.isGasBuilding()) return GasBuildingPositionFinder.findPositionForGasBuilding(building, nearTo);

            // === Base ================================================

        else if (building.isBase()) {
            return FindPositionForBase.forNewBase(builder, building, construction, nearTo);
        }

        // =========================================================

        // === Combat building =====================================

        else if (building.isCombatBuilding()) {
            APosition position = forCombatBuilding(builder, building, construction, nearTo, maxDistance);
            if (position != null) return position;
        }

        // =========================================================
        // Standard building

        APosition standardPosition = APositionFinder.findStandardPosition(builder, building, nearTo, maxDistance);

        if (standardPosition == null && !ignorePositionNotFoundException(building, standardPosition)) {
            ErrorLog.printMaxOncePerMinute(
                "findStandardPosition returned null at " + A.minSec()
                    + "\n    / reason:" + AbstractPositionFinder._STATUS
                    + "\n    / building:" + building
                    + "\n    / near:" + nearTo
                    + "\n    / bases:" + Select.ourBases().count()
                    + " (unfinished: " + Select.ourWithUnfinished().bases().count() + ")"
                    + "\n    / freeSupply:" + AGame.supplyFree()
                    + " (" + AGame.supplyUsed() + "/" + AGame.supplyTotal() + ")"
                    + "\n    / minerals:" + A.minerals()
                    + "\n    / builder:" + builder
                    + "\n    / max:" + maxDistance
            );
        }

        APositionFinder.clearCache();

        // Final guard (owner 2026-10-23, NEXT #145, corrected 2026-10-24):
        // a site beyond the search contract is not built. The cap is the
        // wider of the user cap and the order radius, so the guard never
        // eats the spiral's own ring edge - and it names itself, so the
        // next "Can't find place (reason: OK)" is attributable in the
        // console alone instead of needing the twin.
        if (standardPosition != null) {
            double cap = BuildSiteDistance.capFor(maxDistance);
            if (BuildSiteDistance.isTooFar(standardPosition, nearTo, cap)) {
                atlantis.util.log.ErrorLog.printMaxOncePerMinute(
                    "[LVL=HIGH] SITE_TOO_FAR " + building + " at " + standardPosition
                        + " is " + ((int) standardPosition.groundDist(nearTo))
                        + " ground tiles from " + nearTo + " (cap " + ((int) cap) + ") - not built"
                );

                return null;
            }

            // Owner 2026-11-01: the main base must reach the site by ground.
            // Last and once per search (reachability is the costly query): a
            // disconnected region (UMS/island pocket, no BWEM areas so the
            // per-ring ground guard above fell back to Euclidean) declines
            // instead of stranding the builder. Skipped when no base stands
            // yet - there is nothing to measure from.
            AUnit main = Select.mainOrAnyBuilding();
            if (main != null && main.position() != null
                && !ReachableTiles.between(main.position(), standardPosition)) {
                atlantis.util.log.ErrorLog.printMaxOncePerMinute(
                    "[LVL=HIGH] SITE_UNREACHABLE " + building + " at " + standardPosition
                        + " has no ground path from the main - not built"
                );

                return null;
            }
        }

        return standardPosition;
    }

    private static AUnit defineBuilder() {
        // Owner: always the free pool. No raw fallback: when no worker
        // is truly free the siting fails visibly and retries, instead of
        // burning a scout or double-linking a builder (the missing-Gateway
        // shape: a hijacked pick never builds and never names itself).

        return FreeWorkers.get().first();
    }

    private static boolean ignorePositionNotFoundException(AUnitType building, APosition standardPosition) {
//        if (We.protoss() && !building.needsPower()) return true;

        if (A.supplyFree() >= 3) {
//            if (building.isPylon()) return true;
            if (!"OK".equals(AbstractPositionFinder._STATUS)) {
                if ("Can't physically build here".equals(AbstractPositionFinder._STATUS)) return true;
                if (AbstractPositionFinder._STATUS.contains(" modulo ")) return true;
            }
        }

        if (standardPosition == null
            && Count.workers() >= 4
            && (!We.protoss() || Count.pylons() >= 1)
        ) return true;

        return We.protoss() && (Count.pylons() - 1) <= Count.forgeWithUnfinished();
    }

    private static APosition forCombatBuilding(
        AUnit builder, AUnitType building, Construction construction, HasPosition nearTo, double maxDistance
    ) {
        if (building.isCannon()) {
            return FindPositionForCannon.find(nearTo, builder, construction);
        }

        return null;
    }
}
