package atlantis.production.constructions.position.conditions;

import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.information.strategy.Strategy;
import atlantis.map.ReachableTiles;
import atlantis.map.base.BaseLocations;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.region.ARegion;
import atlantis.map.region.MainRegion;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.We;

public class IsProbablyInAnotherRegion {
    public static boolean differentRegion(AUnit builder, AUnitType building, APosition position, HasPosition nearTo) {
        if (building.isBase()) return false;
        if (handleIsBadRegionForFirstBunker(building, position)) return true;

        // The 60-supply escape reads the same in both worlds: at 60+ the
        // region no longer constrains pylons, in the game or in a test.
        if (building.isPylon() && A.supplyTotal() >= 60) return false;

        if (defaultPylonMustStayHome(builder, building, position, nearTo)) return true;

        if (Env.isTesting()) return apprxForTesting(builder, building, position, nearTo);

//        if (
//            nearTo != null
////                && (A.supplyTotal() <= 50 || building.isCombatBuilding())
//                && !nearTo.regionsMatch(position)
//        ) {
        // `MapTiles.hasPathBetween`, not `position.hasPathTo`: the latter is a bare
        // `Game.hasPath`, which on OpenBW is implemented via BWAPI regions and answers
        // **false for every pair of positions** (the server publishes `regionCount = 0`
        // - see _AI/CHALLENGES/OpenBW.md). `MapTiles.hasPathBetween` already carries the
        // engine-then-map fallback that `MapTiles` needed for the same reason; this
        // condition simply never used it.
        //
        // Measured 2026-10-10 on TauCross: of 549 rejections here, **every one** was
        // `directHasPathTo=false / MapTilesHasPathBetween=true`, with the candidate 1-3
        // tiles from `nearTo` - i.e. the candidates were fine and only this predicate
        // said otherwise. It was the last condition blocking the first Pylon once the
        // search origin was fixed.
        if (
            !ReachableTiles.between(position, nearTo)
//                ||
//                position.groundDist(nearTo.position()) >= 40
        ) {
            return failed("Region mismatch");
        }
//        }

        return false;

//        double groundDistance = builder.position().groundDistanceTo(position);
//
//        boolean result = groundDistance >= 20
//            && builder.distTo(position) * 1.5 >= groundDistance
//            && position.regionsMatch(nearTo);
//
//        if (result) {
//            AbstractPositionFinder._CONDITION_THAT_FAILED = "Probably in another region";
//        }
//
//        return result;
    }

    private static boolean failed(String reason) {
        AbstractPositionFinder._STATUS = reason;

        return true;
    }

    /**
     * Early pylons stay home (owner, lost game vs early aggression: the
     * default-sited 2nd pylon walked to the natural and died there with
     * the game). The default search starts at the main, but its rings
     * reach connected ground - reachability passes while the pylon lands
     * where nothing defends it. Until 60 supply a pylon from a
     * main-anchored search cannot land outside the main region. Two
     * exemptions, both deliberate: an origin away from home means an
     * explicit request, not the default null-nearTo search; an expansion
     * strategy means the natural pylon is the point. Public as a seam:
     * the stub partition (one region per base) answers the same question
     * as BWTA here, so this pins directly.
     */
    public static boolean defaultPylonMustStayHome(AUnit builder, AUnitType building, APosition position, HasPosition nearTo) {
        if (!building.isPylon()) return false;
        if (A.supplyTotal() > 60) return false;
        if (Strategy.get().isExpansion()) return false;
        if (nearTo == null || nearTo.position() == null || position == null) return false;

        ARegion main = MainRegion.mainRegion();
        if (main == null) return false;
        if (!main.equals(nearTo.position().region())) return false;

        if (main.equals(position.region())) return false;

        AbstractPositionFinder._STATUS = "Early pylon outside main region";

        return true;
    }

    private static boolean handleIsBadRegionForFirstBunker(AUnitType building, APosition position) {
        if (!We.terran() || !building.isBunker()) return false;
        if (Count.bunkers() >= 2) return false;

//        System.err.println("MISMATCH = " + !position.regionsMatch(MainRegion.mainRegion()));
        if (!position.regionsMatch(MainRegion.mainRegion())) {
            return failed("Bad region for first bunker");
        }

//        System.out.println("position = " + position.region());
//        System.out.println("MainRegion.mainRegion() = " + MainRegion.mainRegion());
//        System.out.println("position.regionsMatch(MainRegion.mainRegion()) = " + position.regionsMatch(MainRegion.mainRegion()));

        return false;
    }

    private static boolean apprxForTesting(AUnit builder, AUnitType building, APosition position, HasPosition nearTo) {
        return !BaseLocations.nearestTo(position).equals(BaseLocations.nearestTo(nearTo))
            && failed("Approx for testing IsProbablyInAnotherRegion");
    }
}
