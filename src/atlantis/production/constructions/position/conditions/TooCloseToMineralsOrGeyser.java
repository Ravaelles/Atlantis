package atlantis.production.constructions.position.conditions;

import atlantis.game.A;
import atlantis.map.base.Bases;
import atlantis.map.position.APosition;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.We;

public class TooCloseToMineralsOrGeyser {

    /**
     * X-&gt;Y-&gt;Z, same as the siting core: the legacy spiral and the
     * catalogue rank loop ask per tile, but minerals and geysers do not move
     * mid-frame. One Select scan per frame, plain loops after that.
     */
    private static int memoFrame = Integer.MIN_VALUE;
    private static Object memoGame = null;
    private static java.util.List<AUnit> memoMinerals = java.util.Collections.emptyList();
    private static java.util.List<AUnit> memoGas = java.util.Collections.emptyList();

    private static void memo(int frame) {
        Object game = atlantis.Atlantis.game();
        if (frame == memoFrame && game == memoGame) return;
        memoFrame = frame;
        memoGame = game;
        memoMinerals = new java.util.ArrayList<>(Select.minerals().list());
        memoGas = new java.util.ArrayList<>(Select.geysersAndGasBuildings().list());
    }

    private static int countMineralsWithin(double dist, APosition position) {
        memo(atlantis.game.A.now());
        int count = 0;
        int left = position.tx(), top = position.ty();
        for (AUnit mineral : memoMinerals) {
            if (mineral == null) continue;
            if (mineral.distTo(position) <= dist) count++;
        }
        return count;
    }

    private static boolean anyMineralWithin(double dist, APosition position) {
        memo(atlantis.game.A.now());
        for (AUnit mineral : memoMinerals) {
            if (mineral == null) continue;
            if (mineral.distTo(position) <= dist) return true;
        }
        return false;
    }

    private static AUnit nearestGas(APosition position) {
        memo(atlantis.game.A.now());
        AUnit nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (AUnit gas : memoGas) {
            if (gas == null) continue;
            double d = gas.distTo(position);
            if (d < nearestDist) {
                nearestDist = d;
                nearest = gas;
            }
        }
        return nearest;
    }
    /**
     * Minimum tile distance from a mineral patch for a Protoss building that is not
     * a Cannon - the owner's ruling, 2026-10-10 (ideally one tile edge-to-edge).
     */
    public static final double PROSTOSS_MIN_DIST_TO_MINERAL = 3.1;

//    private static boolean allowCloseToBase(AUnit nearestBase, AUnitType building, APosition position) {
//        if (nearestBase == null) return false;
//        if (!building.isCannon()) return false;
//
//        return nearestBase.distTo(position) <= 3.5;
//    }
    /**
     * The same for a Photon Cannon, which is allowed closer because covering the
     * mineral line is what it is for - the owner's ruling, 2026-10-10.
     */
    public static final double PROSTOSS_MIN_DIST_TO_MINERAL_CANNON = 2.0;

    public static boolean isTooCloseToMineralsOrGeyser(AUnitType building, APosition position) {
        // No game, no crowding: game-less unit tests drive the planner with
        // stub terrain (PlacementSupportTest, PlacementPlannerTest), and the
        // mineral/geyser rules below read live supply, bases and unit lists.
        // Without a world there is nothing to crowd - same contract as the
        // standing-cover check (null-safe by construction, false with no
        // game). Live behavior unchanged: a game is always present there.
        if (atlantis.Atlantis.game() == null) return false;

        if (building.isMissileTurret()) return false;
        if (building.isBase()) return false;

        // A gas building (Assimilator / Refinery / Extractor) is BUILT ON the geyser: the
        // finder returns the geyser's own tile (GasBuildingPositionFinder), so the nearest
        // "gas building" from the candidate position is the geyser itself at ~2.2 tiles
        // (geyser centre to its top-left corner). With any geyser-distance threshold above
        // that (it has been 4.1 for Protoss), this condition rejected the ONLY valid site
        // the gas building has - every time, on every map: the owner's real-game report
        // ("the only problem is the Assimilator can't be built on the geyser") and OpenBW's
        // "Assimilator took too long (33s) / buildable:false" are the same defect. A geyser
        // distance rule cannot apply to the building that stands on a geyser by design,
        // same exemption as a base (returns false above).
        if (building.isGasBuilding()) return false;
//        if (building.isCombatBuilding() && !building.isCannon()) return false;
//        if (Select.main() == null) return false;

        // We have problem only if building is both close to base and to minerals or to geyser
//        AUnit nearestBase = Select.ourBasesWithUnfinished().nearestTo(position);
//        if (nearestBase == null && !building.isPylon() && !building.isCannon()) return false;

        double dist;

        dist = minDistToMineral(building, position);
        if (anyMineralWithin(dist, position)) {
            return failed("Too close to mineral (" + (int) dist + ")");
        }

        AUnit gasBuilding = nearestGas(position);
        if (
            gasBuilding != null
                && (dist = gasBuilding.distTo(position)) <= minDistToGeyser(building)
//                && !allowCloseToBase(nearestBase, building, position)
        ) {
            return failed("Too close to geyser (" + (int) dist + ")");
        }

        if (
            building.isCannon()
                && Bases.natural() != null
                && (dist = position.distTo(Bases.natural())) <= 7
                && countMineralsWithin(4, position) > 0
        ) {
            return failed("Cannon too close natural minerals (" + (int) dist + ")");
        }

        return false;
    }

    private static double minDistToGeyser(AUnitType building) {
//        if (true) return 0.1;

        if (We.protoss()) {
            if (building.isGateway()) return 5.1;
            if (building.isPylon()) return 5.1;
            if (building.isCannon()) return 5.1;

            return 4.1;
        }

        else if (We.terran()) {
            return 3.2;
        }

        return 2;
    }

    private static boolean failed(String reason) {
        AbstractPositionFinder._STATUS = reason;

        return true;
    }

    /**
     * The Protoss base case: ideally one tile of clearance edge-to-edge, which is
     * the owner's rule (2026-10-10). A flat tile distance cannot express "edge to
     * edge" exactly, so the numbers are the owner's chosen approximations of it:
     * <b>3.1 for anything but a Photon Cannon, 2.0 for a Cannon</b>.
     *
     * <p>
     * The previous base case was <b>2.8</b>, which the owner reported as "about two
     * tiles from the minerals" - close enough to crowd the mining workers' path and
     * to look wrong on screen. A Cannon is deliberately allowed closer (2.0): it is
     * a defensive structure that has to reach the mineral line, and it is the one
     * building whose whole point is covering it.
     * </p>
     */
    private static double minDistToMineral(AUnitType building, APosition position) {
        if (We.protoss()) {
            if (A.supplyTotal() >= 60) return 6;

            if (A.supplyTotal() >= 40 && Select.ourBuildings().countInRadius(10, position) <= 1) return 5.2;

            if (building.isPylon() && position.distToMain() >= 15) return 6.5;

            // Pylons never sit closer than 5 tiles (owner 2026-11-01): the
            // opening Pylon at 3.1 crowded the workers' path and looked wrong.
            if (building.isPylon()) return 5.0;

            // Cannons sit closer on purpose; everything else keeps a worker-wide gap.
            if (building.isCannon()) {
                return position.distToMain() >= 15 ? 3.2 : PROSTOSS_MIN_DIST_TO_MINERAL_CANNON;
            }

            return PROSTOSS_MIN_DIST_TO_MINERAL;
        }

        else if (We.terran()) {
            if (building.isBunker()) return 5;
            if (building.isBarracks()) return 3.5;

            return 3;
        }

        return 3;
    }
}
