package atlantis.map.base;

import atlantis.information.enemy.EnemyUnits;
import atlantis.map.AMap;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.position.Positions;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.cache.Cache;
import jbweb.Stations;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Bases {

    private static Cache<Object> cache = new Cache<>();

    // =========================================================

    /**
     * Returns starting location that's nearest to given position and is not yet explored (black space, not
     * fog of war).
     */
    public static APosition nearestUnexploredStartingLocation(HasPosition nearestTo) {
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

    public static APosition randomInvisibleStartingLocation() {

//        // Get list of all starting locations
//        Positions<ABaseLocation> startingLocations = new Positions<>();
//        startingLocations.addPositions(startingLocations(true));
//
//        // Sort them all by closest to given nearestTo position
//        startingLocations.sortByDistanceTo(nearestTo, true);

        AUnit enemyBase = EnemyUnits.enemyBase();

        // For every location...
        for (ABaseLocation baseLocation : nonStartingLocations()) {
            if (
                !baseLocation.position().isPositionVisible()
                    && (enemyBase == null || baseLocation.position().distToMoreThan(enemyBase, 15))
            ) {
                return baseLocation.position();
            }
        }
        return null;
    }

    public static ABaseLocation startingLocationBasedOnIndex(int index) {
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
    public static ABaseLocation expansionFreeBaseLocationNearestTo(HasPosition nearestTo) {

        // Get list of all base locations
        Positions<ABaseLocation> baseLocations = new Positions<>();
        baseLocations.addPositions(baseLocations());

        // Sort them all by closest to given nearestTo position
        if (nearestTo != null) {
            baseLocations.sortByDistanceTo(nearestTo, true);
        }

        // For every location...
        for (ABaseLocation baseLocation : baseLocations.list()) {
            if (isBaseLocationFreeOfBuildingsAndEnemyUnits(baseLocation) || !baseLocation.isExplored()) {
                if (hasBaseMinerals(baseLocation)) {
                    return baseLocation;
                }
            }
        }

        // For every location...
        for (ABaseLocation baseLocation : baseLocations.list()) {
            if (isBaseLocationFreeOfBuildingsAndEnemyUnits(baseLocation) || !baseLocation.isPositionVisible()) {
                return baseLocation;
            }
        }

        return null;
    }

    public static boolean hasBaseMinerals(HasPosition baseLocation) {
        return Select.minerals().inRadius(8, baseLocation).count() > 0;
    }

    /**
     * Returns free base location which is as far from enemy starting location as possible.
     */
    public static ABaseLocation expansionBaseLocationMostDistantToEnemy() {
        AUnit farthestTo = EnemyUnits.enemyBase();
        if (farthestTo == null) {
            return expansionFreeBaseLocationNearestTo(Select.ourBases().first().position());
        }

        // =========================================================

        // Get list of all base locations
        Positions<ABaseLocation> baseLocations = new Positions<>();
        baseLocations.addPositions(baseLocations());

        // Sort them all by closest to given nearestTo position
        baseLocations.sortByDistanceTo(farthestTo, false);

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
        if (Select.main() == null) {
            return null;
        }

        ABaseLocation naturalLocation = natural(Select.main().position());
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
            () -> Stations.allBases()
                .stream()
                .map(base -> ABaseLocation.create(base.getBWEMBase()))
                .collect(Collectors.toList())

//                () -> AMap.getMap()
//                        .getBases()
//                        .stream()
//                        .map(base -> ABaseLocation.create(base))
            // This won't work - fog of war :- (
//                        .filter(base -> Select.minerals().inRadius(8, base).atLeast(6))
//                        .collect(Collectors.toList())
        );
    }

    public static List<ABaseLocation> nonStartingLocations() {
        return (List<ABaseLocation>) cache.get(
            "nonStartingLocations",
            10,
            () -> AMap.getMap()
                .getStartingLocations()
                .stream()
                .map(tilePosition -> ABaseLocation.create(tilePosition))
                .collect(Collectors.toList())
        );
    }

    /**
     * Returns list of all places where players can start a game. Note, that if you play map for two players
     * and you know location of your own base. So you also know the location of enemy base (enemy *must* be
     * there), but still obviously you don't see him.
     */
    public static List<ABaseLocation> startingLocations(boolean excludeOurStartLocation) {
        AUnit mainBase = Select.main();

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
//        List<AUnit> ourUnits = Select.ourBuildingsWithUnfinished()
        List<AUnit> ourUnits = Select.ourBuildingsWithUnfinished()
            .bases()
            .inRadius(7, baseLocation.position()).list();
        for (AUnit our : ourUnits) {
            if (!our.isLifted()) {
                return false;
            }
        }

        // If any enemy unit is Near
        if (Select.enemy().inRadius(14, baseLocation.position()).count() > 0) return false;

        // Check for planned constructions
        for (Construction construction : ConstructionRequests.all()) {
            APosition constructionPlace = construction.positionToBuildCenter();
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
                AUnit enemyBase = EnemyUnits.enemyBase();
                if (enemyBase == null) {
                    return null;
                }

                ABaseLocation baseLocation = natural(enemyBase.position());
                if (baseLocation != null) {
                    return baseLocation.position().translateByTiles(2, 0);
                }

                return null;
            }
        );
    }

    public static boolean hasBaseAtNatural() {
        return (boolean) cache.get(
            "hasBaseAtNatural",
            57,
            () -> {
                APosition natural = natural();
                if (natural == null) return false;

                return Select.ourBuildingsWithUnfinished().bases().inRadius(8, natural).notEmpty();
            }
        );
    }

    public static AUnit hasBunkerAtNatural() {
        return (AUnit) cache.get(
            "hasBunkerAtNatural",
            57,
            () -> {
                APosition natural = natural();
                if (natural == null) return null;

                return Select.ourWithUnfinishedOfType(AUnitType.Terran_Bunker)
                    .inRadius(9, natural)
                    .nearestTo(natural);
            }
        );
    }
}
