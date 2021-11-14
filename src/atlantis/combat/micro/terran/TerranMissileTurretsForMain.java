package atlantis.combat.micro.terran;

import atlantis.enemy.AEnemyUnits;
import atlantis.information.AFoggedUnit;
import atlantis.map.ARegion;
import atlantis.map.ARegionBoundary;
import atlantis.position.APosition;
import atlantis.position.Positions;
import atlantis.production.orders.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.A;
import atlantis.util.Cache;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class TerranMissileTurretsForMain extends TerranMissileTurret {

    private static final double DIST_BETWEEN_BORDER_TURRETS = 8;
    private static final int MIN_TURRETS_FOR_BORDER = 3;
    private static final int MAX_TURRETS_FOR_BORDER_OVER_TIME = 7;
    private static final int TILES_MARGIN = 3;
    private static Cache<ArrayList<APosition>> cache = new Cache<>();

    public static boolean buildIfNeeded() {
        if (!Have.engBay()) {
            return false;
        }

        if (turretsForMain()) {
            System.out.println("Requested TURRET for MAIN");
            return true;
        }

//        if (handleReinforcePosition(turretForNatural(), 7)) {
//            System.out.println("Requested TURRET for NATURAL");
//            return true;
//        }

        return false;
    }

    // =========================================================

    private static boolean turretsForMain() {
        if (!Have.main()) {
            return false;
        }

        ArrayList<APosition> turretsProtectingMainBorders = positionsForTurretsNearMainBorder();

        for (int i = 0; i < turretsProtectingMainBorders.size(); i++) {
            APosition place = turretsProtectingMainBorders.get(i);
            if (
                    place != null
//                    && Count.inQueueOrUnfinished(turret, MIN_TURRETS_FOR_BORDER + 2) < MIN_TURRETS_FOR_BORDER
//                    && ConstructionRequests.countNotStartedOfType(turret) < Math.max(1, MIN_TURRETS_FOR_BORDER - 2)
                    && Count.inQueueOrUnfinished(turret, 8) <= 4
                    && Count.existingOrPlannedBuildingsNear(turret, TILES_MARGIN, place) == 0
            ) {
                AddToQueue.withHighPriority(turret, place).setMaximumDistance(TILES_MARGIN);
            }
        }

        return false;
    }

    public static ArrayList<APosition> positionsForTurretsNearMainBorder() {
        return cache.get(
            "positionsForTurretsNearMainBorder",
            30,
            () -> {
                ArrayList<APosition> places = new ArrayList<>();

                if (!Have.main()) {
                    return places;
                }

                ARegion region = Select.main().position().region();
                if (region == null) {
                    return places;
                }

                AFoggedUnit enemyBuilding = AEnemyUnits.nearestEnemyBuilding();
                if (enemyBuilding == null) {
                    return places;
                }

                Positions<ARegionBoundary> boundaries = new Positions<>();
                boundaries.addPositions(region.bounds());

                // =========================================================

                // Protect the place nearest to map edge - here a lot of invasions happen
                APosition placeForMapEdgeTurret = placeForMapEdgeTurret(boundaries, enemyBuilding.position());
                if (placeForMapEdgeTurret != null) {
                    places.add(placeForMapEdgeTurret);
                }

                // Protect cliffs around the main base region, looking from nearest side to enemy
                // place (TOTAL_TURRETS_FOR_BORDER -1) turrets along the border, each about
                // TOTAL_TURRETS_FOR_BORDER tiles away.
                for (int i = 0; i < totalTurretsForMainBorder() - 1; i++) {
                    APosition placeForTurret = placeForNextMainTurret(boundaries, enemyBuilding.position(), places);
                    if (placeForTurret != null) {
                        places.add(placeForTurret);
                    }
                }

                return places;
            }
        );
    }

    private static int totalTurretsForMainBorder() {
        int haveMaxAmountAtGameSeconds = 600;
        return Math.max(
                MIN_TURRETS_FOR_BORDER,
                MAX_TURRETS_FOR_BORDER_OVER_TIME * Math.min(1, A.seconds() / haveMaxAmountAtGameSeconds)
        );
    }

    private static APosition placeForMapEdgeTurret(
            Positions<ARegionBoundary> boundaries, APosition nearestTo
    ) {
        Positions<ARegionBoundary> nearMapEdge = new Positions<>(
                boundaries.list()
                .stream().filter(b -> b.position().nearMapEdge(3.8))
                .collect(Collectors.toList())
        );

//        System.out.println("nearMapEdge = " + nearMapEdge.size());
//        for (ARegionBoundary b : nearMapEdge.list()) {
//            System.out.println("b = " + b.position());
//        }

        return nearMapEdge.nearestTo(nearestTo);
    }

//    private static boolean protectMainBorders(Positions<ARegionBoundary> boundaries, APosition nearestEnemyBuilding) {
//        APosition placeForTurret = placeForNextMainTurret(boundaries, nearestEnemyBuilding);
//        if (placeForTurret != null && Count.inQueueOrUnfinished(turret, 4) <= 3) {
//            return AddToQueue.withTopPriority(turret, placeForTurret);
//        }
//        return false;
//    }

//    private static boolean protectMainBorders(Positions<ARegionBoundary> boundaries, APosition nearestEnemyBuilding) {
//        APosition placeForTurret = placeForNextMainTurret(boundaries, nearestEnemyBuilding);
//        if (placeForTurret != null && Count.inQueueOrUnfinished(turret, 4) <= 3) {
//            return AddToQueue.withTopPriority(turret, placeForTurret);
//        }
//        return false;
//    }

    private static APosition placeForNextMainTurret(
            Positions<ARegionBoundary> boundaries, APosition nearestEnemyBuilding, ArrayList<APosition> places
    ) {
        int counter = 0;
        APosition centralPoint = boundaries.nearestTo(nearestEnemyBuilding); // Region boundary nearest to enemy
        APosition potentialPlace = centralPoint;
        while (true) {
            if (!places.contains(potentialPlace)) {
                int turretsNear = Count.existingOrPlannedBuildingsNear(turret, DIST_BETWEEN_BORDER_TURRETS, potentialPlace);
                if (turretsNear == 0) {
                    return potentialPlace;
                }
            }

            // Look for other boundary points in certain distance since the last one
            potentialPlace = findBoundaryPointInDistOf(DIST_BETWEEN_BORDER_TURRETS, centralPoint, boundaries, places);

            if (counter++ >= 10) {
                return null;
            }
        }
    }

    private static APosition findBoundaryPointInDistOf(
            double baseDist, APosition position, Positions<ARegionBoundary> boundaries, ArrayList<APosition> places
    ) {
        double currentSearchDist = baseDist;
        while (currentSearchDist <= 3.2 * baseDist) {
            for (ARegionBoundary boundary : boundaries.list()) {
                if (!places.contains(boundary.position())) {
                    double dist = boundary.position().distTo(position);
                    if (currentSearchDist - 0.9 <= dist && dist <= currentSearchDist + 1.9) {
                        return boundary.position();
                    }
                }
            }

            currentSearchDist += baseDist;
        }

        return null;
    }

}
