package atlantis.combat.micro.terran;

import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.map.ARegion;
import atlantis.map.ARegionBoundary;
import atlantis.map.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.position.Positions;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.Cache;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class TerranMissileTurretsForMain extends TerranMissileTurret {

    private static final int BORDER_TURRETS_MIN_COUNT = 0;
    private static final int BORDER_TURRETS_TOTAL_OVER_TIME = 0;
//    private static final int BORDER_TURRETS_MIN_COUNT = 4;
//    private static final int BORDER_TURRETS_TOTAL_OVER_TIME = 7;
    private static final int BORDER_TURRETS_MAX_DIST_BETWEEN = 8;
    private static final int BORDER_TURRETS_ALLOW_MARGIN = 4;
    private static final int MAIN_BASE_TURRETS = 2;

    private static Cache<ArrayList<APosition>> cacheList = new Cache<>();
    private static Cache<APosition> cachePosition = new Cache<>();

    // =========================================================

    public static boolean buildIfNeeded() {
        if (!Have.engBay() || !Have.base()) {
            return false;
        }

        if (turretForMainChoke()) {
            System.out.println("Requested TURRET for MAIN CHOKE");
            return true;
        }

        if (turretsForMainRegionBorders()) {
            System.out.println("Requested TURRET for BORDERS");
            return true;
        }

        if (turretsForMainBase()) {
            System.out.println("Requested TURRET for MAIN BASE");
            return true;
        }

        return false;
    }

    // =========================================================

    private static boolean turretsForMainBase() {
        APosition forMainBase = positionForMainBaseTurret();

        if (
                forMainBase != null
                && Count.existingOrPlannedBuildingsNear(turret, 6, forMainBase) < MAIN_BASE_TURRETS
        ) {
            AddToQueue.withHighPriority(turret, forMainBase).setMaximumDistance(12);
            return true;
        }

        return false;
    }

    private static boolean turretForMainChoke() {
        APosition place = Chokes.mainChoke().translateTilesTowards(5, Select.main());
        if (place != null) {
            if (Count.existingOrPlannedBuildingsNear(turret, 5, place) == 0) {
                AddToQueue.withHighPriority(turret, place).setMaximumDistance(12);
                return true;
            }
        }

        return false;
    }

    private static boolean turretsForMainRegionBorders() {
        if (true) {
            return false;
        }

        ArrayList<APosition> turretsProtectingMainBorders = positionsForTurretsNearMainBorder();

        for (int i = 0; i < turretsProtectingMainBorders.size(); i++) {
            APosition place = turretsProtectingMainBorders.get(i);
            if (
                    place != null
                    && Count.inQueueOrUnfinished(turret, 8) <= BORDER_TURRETS_MIN_COUNT
                    && Count.existingOrPlannedBuildingsNear(turret, BORDER_TURRETS_ALLOW_MARGIN + 1.1, place) == 0
            ) {
                AddToQueue.withHighPriority(turret, place).setMaximumDistance(BORDER_TURRETS_ALLOW_MARGIN);
            }
        }

        return false;
    }

    // =========================================================

    private static APosition positionForMainBaseTurret() {
        return cachePosition.get(
                "positionForMainBaseTurret",
                30,
                () -> {
                    APosition enemyLocation = EnemyInfo.enemyLocationOrGuess();
                    AUnit mineralNearestToEnemy = Select.minerals().inRadius(12, Select.main()).nearestTo(enemyLocation);

                    if (mineralNearestToEnemy != null) {
                        return mineralNearestToEnemy.translateTilesTowards(6, enemyLocation);
                    }

                    return null;
                }
        );
    }

    public static ArrayList<APosition> positionsForTurretsNearMainBorder() {
        return cacheList.get(
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

                APosition enemyLocation = EnemyInfo.enemyLocationOrGuess();

                Positions<ARegionBoundary> boundaries = new Positions<>();
                boundaries.addPositions(region.boundaries());

                // =========================================================

                // Protect the place nearest to map edge - here a lot of invasions happen
                HasPosition placeForMapEdgeTurret = placeForMapEdgeTurret(boundaries, enemyLocation);
                if (placeForMapEdgeTurret != null) {
                    placeForMapEdgeTurret = modifyAgainstZerg(placeForMapEdgeTurret);

                    places.add(placeForMapEdgeTurret.position());
                }

                // Protect cliffs around the main base region, looking from nearest side to enemy
                // place (TOTAL_TURRETS_FOR_BORDER -1) turrets along the border, each about
                // TOTAL_TURRETS_FOR_BORDER tiles away.
                for (int i = 0; i < totalTurretsForMainBorder() - 1; i++) {
                    APosition placeForTurret = placeForNextMainTurret(boundaries, enemyLocation, places);
                    if (placeForTurret != null) {
                        places.add(placeForTurret);
                    }
                }

                return places;
            }
        );
    }

    /**
     * Zerg's Mutalisks can bypass initial defences and storm the main, whereas Protoss Shuttle can be guarded
     * by protecting against the land invasion. Therefore against Zerg back the turrets towards the base, so
     * they defend both border and the main at the same time.
     */
    private static APosition modifyAgainstZerg(HasPosition placeForMapEdgeTurret) {
        return placeForMapEdgeTurret.translateTilesTowards(Select.main(), 5);
    }

    private static int totalTurretsForMainBorder() {
        int haveMaxAmountAtGameSeconds = 600;
        return Math.max(
                BORDER_TURRETS_MIN_COUNT,
                BORDER_TURRETS_TOTAL_OVER_TIME * Math.min(1, A.seconds() / haveMaxAmountAtGameSeconds)
        );
    }

    private static HasPosition placeForMapEdgeTurret(
            Positions<ARegionBoundary> boundaries, APosition nearestTo
    ) {
        if (nearestTo == null) {
            return null;
        }

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
        ARegionBoundary centralPoint = boundaries.nearestTo(nearestEnemyBuilding); // Region boundary nearest to enemy
        APosition potentialPlace = centralPoint.position();
        while (true) {
            if (potentialPlace != null && !places.contains(potentialPlace)) {
                int turretsNear = Count.existingOrPlannedBuildingsNear(turret, BORDER_TURRETS_MAX_DIST_BETWEEN, potentialPlace);
                if (turretsNear == 0) {
                    return potentialPlace;
                }
            }

            // Look for other boundary points in certain distance since the last one
            potentialPlace = findBoundaryPointInDistOf(BORDER_TURRETS_MAX_DIST_BETWEEN, centralPoint, boundaries, places);

            if (counter++ >= 10) {
                return null;
            }
        }
    }

    private static APosition findBoundaryPointInDistOf(
            double baseDist, HasPosition position, Positions<ARegionBoundary> boundaries, ArrayList<APosition> places
    ) {
        double currentSearchDist = baseDist;
        while (currentSearchDist <= 3.2 * baseDist) {
            for (ARegionBoundary boundary : boundaries.list()) {
                if (boundary.position() != null && !places.contains(boundary.position())) {
                    double dist = boundary.distTo(position);
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
