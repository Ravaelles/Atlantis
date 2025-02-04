package atlantis.production.dynamic.reinforce.terran.turrets;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.strategy.AStrategy;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.position.Positions;
import atlantis.map.region.ARegion;
import atlantis.map.region.ARegionBoundary;
import atlantis.information.generic.InitialMainPosition;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.game.player.Enemy;
import atlantis.util.cache.Cache;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class TurretsForMain extends TerranMissileTurret {
    private final int BORDER_TURRETS_MIN_COUNT = 1;
    private final int BORDER_TURRETS_TOTAL_OVER_TIME = 0;
    //    private  final int BORDER_TURRETS_MIN_COUNT = 4;
//    private  final int BORDER_TURRETS_TOTAL_OVER_TIME = 7;
    private final int BORDER_TURRETS_MAX_DIST_BETWEEN = 8;
    private final int BORDER_TURRETS_ALLOW_MARGIN = 4;

    private Cache<ArrayList<APosition>> cacheList = new Cache<>();
    private Cache<APosition> cachePosition = new Cache<>();
    private static AUnit main;

    // =========================================================

    public boolean buildIfNeeded() {
        if (true) return false;

        if (A.everyFrameExceptNthFrame(71)) return false;
        if (A.supplyUsed() <= 20) return false;
        if (!Have.engBay() || !Have.base()) return false;

        if (turretForMainChoke()) {

            return true;
        }

        if (turretsForMainRegionBorders()) {

            return true;
        }

        if (turretsForMainBase()) {

            return true;
        }

        return false;
    }

    // =========================================================

    private boolean turretsForMainBase() {
        int turretsForMain = optimalMainBaseTurrets();

        if (turretsForMain <= 0) return false;
        if (exceededExistingAndInProduction()) return false;

        AUnit main = mainBase();
        if (main != null && main.isLifted()) return false;

        // =========================================================

        APosition forMainBase = positionForMainBaseTurret();
        if (
            forMainBase != null
                && Count.existingOrPlannedBuildingsNear(turret, 6, forMainBase) < turretsForMain
        ) {
            ProductionOrder productionOrder = AddToQueue.withHighPriority(turret, forMainBase);
            if (productionOrder != null) {
                productionOrder.setMaximumDistance(12);
            }
            return true;
        }

        return false;
    }

    private AUnit mainBase() {
        if (main != null) {
            return main;
        }

        return main = Select.ourBases().nearestTo(InitialMainPosition.initialMainPosition());
    }

    private int optimalMainBaseTurrets() {
        return baseNumOfTurrets() + (AGame.minerals() / 700);
    }

    private static int baseNumOfTurrets() {
        AStrategy enemyStrategy = EnemyInfo.strategy();

        if (Enemy.zerg()) {
            int base = 0;

            if (enemyStrategy.isAirUnits()) {
                base = 3;
            }

            return Math.min(6, base + (A.seconds() / 100));
        }

        else if (Enemy.protoss()) {
            if (enemyStrategy.isAirUnits()) {
                return 6;
            }

            return (EnemyInfo.hasHiddenUnits() || enemyStrategy.isGoingHiddenUnits())
                ? 2
                : (A.seconds() >= 400 ? 1 : 0);
        }

        else {
            if (EnemyUnits.discovered().ofType(AUnitType.Terran_Wraith).notEmpty()) {
                return 1;
            }

            return 0;
        }
    }

    private boolean turretForMainChoke() {
        APosition place = Chokes.mainChoke().translateTilesTowards(5, mainBase());
        if (place != null) {
            int existing = Count.existingOrPlannedBuildingsNear(turret, 5, place);
            ProductionOrder productionOrder = null;

            if (existing == 0) {
                productionOrder = AddToQueue.withHighPriority(turret, place);
            }

            if (existing <= 1 && A.hasMinerals(350)) {
                productionOrder = AddToQueue.withStandardPriority(turret, place);
            }

            if (productionOrder != null) {
                productionOrder.setMaximumDistance(12);
                return true;
            }
        }

        return false;
    }

    private boolean turretsForMainRegionBorders() {
//        if (true) return false;

        if (A.everyFrameExceptNthFrame(113)) return false;
        if (!A.hasMinerals(570)) return false;
        if (exceededExistingAndInProduction()) return false;

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

    private APosition positionForMainBaseTurret() {
        return cachePosition.get(
            "positionForMainBaseTurret",
            30,
            () -> {
                APosition enemyLocation = EnemyInfo.enemyLocationOrGuess();
                AUnit mineralNearestToEnemy = Select.minerals().inRadius(12, mainBase()).nearestTo(enemyLocation);

                if (mineralNearestToEnemy != null) {
                    return mineralNearestToEnemy.translateTilesTowards(6, enemyLocation);
                }

                return null;
            }
        );
    }

    public ArrayList<APosition> positionsForTurretsNearMainBorder() {
        return cacheList.get(
            "positionsForTurretsNearMainBorder",
            30,
            () -> {
                ArrayList<APosition> places = new ArrayList<>();

                if (!Have.main()) {
                    return places;
                }

                ARegion region = mainBase().position().region();
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
    private APosition modifyAgainstZerg(HasPosition placeForMapEdgeTurret) {
        return placeForMapEdgeTurret.translateTilesTowards(mainBase(), 5);
    }

    private int totalTurretsForMainBorder() {
        int haveMaxAmountAtGameSeconds = 600;
        return Math.max(
            BORDER_TURRETS_MIN_COUNT,
            BORDER_TURRETS_TOTAL_OVER_TIME * Math.min(1, A.seconds() / haveMaxAmountAtGameSeconds)
        );
    }

    private HasPosition placeForMapEdgeTurret(
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


//        for (ARegionBoundary b : nearMapEdge.list()) {

//        }

        return nearMapEdge.nearestTo(nearestTo);
    }

//    private  boolean protectMainBorders(Positions<ARegionBoundary> boundaries, APosition nearestEnemyBuilding) {
//        APosition placeForTurret = placeForNextMainTurret(boundaries, nearestEnemyBuilding);
//        if (placeForTurret != null && Count.inQueueOrUnfinished(turret, 4) <= 3) {
//            return AddToQueue.withTopPriority(turret, placeForTurret);
//        }
//        return false;
//    }

//    private  boolean protectMainBorders(Positions<ARegionBoundary> boundaries, APosition nearestEnemyBuilding) {
//        APosition placeForTurret = placeForNextMainTurret(boundaries, nearestEnemyBuilding);
//        if (placeForTurret != null && Count.inQueueOrUnfinished(turret, 4) <= 3) {
//            return AddToQueue.withTopPriority(turret, placeForTurret);
//        }
//        return false;
//    }

    private APosition placeForNextMainTurret(
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

    private APosition findBoundaryPointInDistOf(
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
