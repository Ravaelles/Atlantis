package atlantis.map;

import atlantis.enemy.AEnemyUnits;
import atlantis.position.APosition;
import atlantis.position.Positions;
import atlantis.production.constructing.AConstructionRequests;
import atlantis.production.constructing.ConstructionOrder;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.Cache;
import bwapi.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BaseLocations {

    private static Cache<Object> cache = new Cache<>();

    // =========================================================

    /**
     * Returns starting location that's nearest to given position and is not yet explored (black space, not
     * fog of war).
     */
    public static APosition getNearestUnexploredStartingLocation(APosition nearestTo) {
        if (nearestTo == null) {
            return null;
        }

        // Get list of all starting locations
        Positions<ABaseLocation> startingLocations = new Positions<>();
        startingLocations.addPositions(startingLocations(true));

        // Sort them all by closest to given nearestTo position
        startingLocations.sortByDistanceTo(nearestTo, true);

        // For every location...
        for (ABaseLocation baseLocationPosition : startingLocations.list()) {
            if (!baseLocationPosition.position().isExplored()) {
                return APosition.create(baseLocationPosition);
            }
        }
        return null;
    }

    public static ABaseLocation getStartingLocationBasedOnIndex(int index) {
        ArrayList<ABaseLocation> baseLocations = new ArrayList<>();
        baseLocations.addAll(startingLocations(true));

        if (baseLocations.isEmpty()) {
            return null;
        }

        return baseLocations.get(index % baseLocations.size());
    }

    /**
     * Returns nearest free base location where we don't have base built yet.
     */
    public static ABaseLocation getExpansionFreeBaseLocationNearestTo(Position nearestTo) {

        // Get list of all base locations
        Positions<ABaseLocation> baseLocations = new Positions<>();
        baseLocations.addPositions(baseLocations());

        // Sort them all by closest to given nearestTo position
        if (nearestTo != null) {
            baseLocations.sortByDistanceTo(nearestTo, true);
        }

        // For every location...
        for (ABaseLocation baseLocation : baseLocations.list()) {
            if (isBaseLocationFreeOfBuildingsAndEnemyUnits(baseLocation)) {
                return baseLocation;
            }
        }
        return null;
    }

    /**
     * Returns free base location which is as far from enemy starting location as possible.
     */
    public static ABaseLocation getExpansionBaseLocationMostDistantToEnemy() {
        APosition farthestTo = AEnemyUnits.enemyBase();
        if (farthestTo == null) {
            return getExpansionFreeBaseLocationNearestTo(Select.ourBases().first().position());
        }

        // =========================================================

        // Get list of all base locations
        Positions<ABaseLocation> baseLocations = new Positions<>();
        baseLocations.addPositions(baseLocations());

        // Sort them all by closest to given nearestTo position
        if (farthestTo != null) {
            baseLocations.sortByDistanceTo(farthestTo, false);
        }

        // For every location...
        for (ABaseLocation baseLocation : baseLocations.list()) {
            if (isBaseLocationFreeOfBuildingsAndEnemyUnits(baseLocation)) {
                return baseLocation;
            }
        }
        return null;
    }

    /**
     * Returns nearest base location (by the actual ground distance) to the given base location.
     */
    public static APosition natural() {
        if (Select.mainBase() == null) {
            return null;
        }

        ABaseLocation naturalLocation = natural(Select.mainBase().position());
        if (naturalLocation != null) {
            return naturalLocation.position();
        }

        return null;
    }

    /**
     * Returns nearest base location (by the actual ground distance) to the given base location.
     */
    public static ABaseLocation natural(APosition nearestTo) {
        // Get all base locations, sort by being closest to given nearestTo position
        Positions<ABaseLocation> baseLocations = new Positions<>();
        baseLocations.addPositions(baseLocations());
        baseLocations.sortByGroundDistanceTo(nearestTo, true);

        for (ABaseLocation baseLocation : baseLocations.list()) {
            if (baseLocation.isStartLocation() || !nearestTo.hasPathTo(baseLocation.position())) {
                continue;
            }
            return baseLocation;
        }

        return null;
    }

    /**
     * Returns list of places that have geyser and mineral fields so they are the places where you could build
     * a base. Starting locations are also included here.
     */
    public static List<ABaseLocation> baseLocations() {
        return (List<ABaseLocation>) cache.get(
                "baseLocations",
                -1,
                () -> AMap.getMap()
                        .getBases()
                        .stream()
                        .map(base -> ABaseLocation.create(base))
                        .collect(Collectors.toList())
        );
    }

    /**
     * Returns list of all places where players can start a game. Note, that if you play map for two players
     * and you know location of your own base. So you also know the location of enemy base (enemy *must* be
     * there), but still obviously you don't see him.
     */
    public static List<ABaseLocation> startingLocations(boolean excludeOurStartLocation) {
        AUnit mainBase = Select.mainBase();

        return (List<ABaseLocation>) cache.get(
                "startingLocations:" + excludeOurStartLocation,
                -1,
                () -> AMap.getMap()
                            .getStartingLocations()
                            .stream()
                            .map(tilePosition -> ABaseLocation.create(tilePosition))
                            .filter(base -> base.distToMoreThan(mainBase, 10))
                            .collect(Collectors.toList())
        );
//                    ArrayList<ABaseLocation> startingLocations = new ArrayList<>();
//                    for (ABaseLocation baseLocation : AMap.getBaseLocations()) {
//                        if (baseLocation.isStartLocation()) {
//
//                            // Exclude our base location if needed.
//                            if (excludeOurStartLocation) {
//                                AUnit mainBase = Select.mainBase();
//                                if (mainBase != null && PositionUtil.distanceTo(mainBase, baseLocation.position()) <= 10) {
//                                    continue;
//                                }
//                            }
//
//                            startingLocations.add(baseLocation);
//                        }
//                    }
//                    return startingLocations;
//                }
//        );
    }

    /**
     * Returns true if given base location is free from units, meaning it's a good place for expansion.
     * <p>
     * Trying to avoid:
     * - existing buildings
     * - any enemy units
     * - planned constructions
     */
    public static boolean isBaseLocationFreeOfBuildingsAndEnemyUnits(ABaseLocation baseLocation) {

        // If we have any base, FALSE.
        if (Select.ourBases().inRadius(7, baseLocation.position()).count() > 0) {
            return false;
        }

        // If any enemy unit is nearby
        if (Select.enemy().inRadius(11, baseLocation.position()).count() > 0) {
            return false;
        }

        // Check for planned constructions
        for (ConstructionOrder constructionOrder : AConstructionRequests.getAllConstructionOrders()) {
            APosition constructionPlace = constructionOrder.getPositionToBuildCenter();
            if (constructionPlace != null && constructionPlace.distTo(baseLocation.position()) < 8) {
                return false;
            }
        }

        // All conditions have been fulfilled.
        return true;
    }

    public static APosition enemyNatural() {
        return (APosition) cache.get(
                "enemyNatural",
                60,
                () -> {
                    APosition enemyBase = AEnemyUnits.enemyBase();
                    if (enemyBase == null) {
                        return null;
                    }

                    ABaseLocation baseLocation = natural(enemyBase);
                    if (baseLocation != null) {
                        return baseLocation.position();
                    }

                    return null;
                }
        );
    }
}
