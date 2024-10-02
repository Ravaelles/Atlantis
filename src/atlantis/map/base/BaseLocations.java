package atlantis.map.base;

import atlantis.config.AtlantisRaceConfig;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.AMap;
import atlantis.map.base.define.DefineNaturalBase;
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

public class BaseLocations {
    private static Cache<Object> cache = new Cache<>();

    // =========================================================

    /**
     * Returns starting location that's nearest to given position and is not yet explored (black space, not
     * fog of war).
     */
    public static APosition nearestUnexploredStartingLocation(HasPosition nearestTo) {
        if (nearestTo == null) return null;

        // Get list of all starting locations
        Positions<ABaseLocation> startingLocations = new Positions<>(startingLocations(true));

        ABaseLocation location = startingLocations.unexplored().groundNearestTo(nearestTo);
        return location != null ? location.position() : null;
    }

    public static HasPosition nearestUnexploredBaseLocation(HasPosition nearestTo) {
        if (nearestTo == null) return null;

        // Get list of all starting locations
        Positions<ABaseLocation> baseLocations = new Positions<>(baseLocations());

        return baseLocations.unexplored().groundNearestTo(nearestTo);
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

        return baseLocations.get((index + 1) % baseLocations.size());
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
            baseLocations.sortByGroundDistanceTo(nearestTo, true);
        }

        // For every location...
        for (ABaseLocation baseLocation : baseLocations.list()) {
            if (baseLocationLooksFree(baseLocation) && !baseLocation.isExplored()) {
//                if (hasBaseMinerals(baseLocation)) {
                return baseLocation;
//                }
            }
        }

        // For every location...
        for (ABaseLocation baseLocation : baseLocations.list()) {
            if (baseLocationLooksFree(baseLocation) && !baseLocation.isPositionVisible()) {
//                if (hasBaseMinerals(baseLocation)) {
                return baseLocation;
//                }
            }
        }

        return null;
    }

//    public static boolean hasBaseMinerals(HasPosition baseLocation) {
//        // Doesn't work due to fog of war removing visible units.
//        // To make this work, need to cache previously seen minerals.
//        System.err.println("Select.minerals().inRadius(10, baseLocation).count() = " + Select.minerals().inRadius(10, baseLocation).count());
//        return Select.minerals().inRadius(10, baseLocation).count() >= 5;
//    }

    /**
     * Returns free base location which is as far from enemy starting location as possible.
     */
    public static ABaseLocation expansionBaseLocationMostDistantToEnemy() {
        AUnit farthestTo = EnemyUnits.nearestEnemyBuilding();
        if (farthestTo == null) {
            return expansionFreeBaseLocationNearestTo(Select.ourBases().first().position());
        }

        // =========================================================

        // Get list of all base locations
        Positions<ABaseLocation> baseLocations = new Positions<>();
        baseLocations.addPositions(baseLocations());

        // Sort them all by closest to given nearestTo position
        baseLocations.sortByGroundDistanceTo(farthestTo, false);

        // For every location...
        for (ABaseLocation baseLocation : baseLocations.list()) {
            if (baseLocationLooksFree(baseLocation)) {
                return baseLocation;
            }
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
    public static boolean baseLocationLooksFree(ABaseLocation baseLocation) {

        // If there's any existing, alive, non-lifted base, then it's not free.
        int existingBases = Select.ourBasesWithUnfinished()
            .inRadius(7, baseLocation.position())
            .havingAtLeastHp(1)
            .notLifted()
            .count();

        if (existingBases > 0) return false;

        // If any enemy unit is Near
        if (Select.enemyRealUnitsWithBuildings().inRadius(8, baseLocation.position()).effVisible().count() >= 2)
            return false;

        // Check for planned constructions
        for (Construction construction : ConstructionRequests.notStartedOfType(AtlantisRaceConfig.BASE)) {
            APosition constructionPlace = construction.positionToBuildCenter();
            if (constructionPlace != null && constructionPlace.distTo(baseLocation.position()) <= 5) return false;
        }

        // All conditions have been fulfilled.
        return true;
    }

    public static APosition enemyNatural() {
        return (APosition) cache.get(
            "enemyNatural",
            60,
            () -> {
                APosition enemyMain = EnemyInfo.enemyMain();
                if (enemyMain == null) {
                    return null;
                }

                ABaseLocation baseLocation = DefineNaturalBase.naturalIfMainIsAt(enemyMain);
                if (baseLocation != null) {
                    return baseLocation.position().translateByTiles(2, 0);
                }

                return null;
            }
        );
    }

    public static APosition enemyThird() {
        return (APosition) cache.getIfValid(
            "enemyThird",
            30 * 11,
            () -> {
                AUnit enemyBase = EnemyUnits.enemyBase();
                if (enemyBase == null) return null;

                APosition enemyNatural = BaseLocations.enemyNatural();
                if (enemyNatural == null) return null;

                double bestDist = 9999;
                ABaseLocation bestBase = null;

                for (ABaseLocation baseLocation : BaseLocations.baseLocations()) {
                    if (baseLocation.isStartLocation()) continue;

                    double distToMain = enemyBase.groundDist(baseLocation);
                    double distToNatural = enemyNatural.groundDist(baseLocation);
                    if (
                        distToMain < bestDist
                            && distToMain >= 12
                            && distToNatural >= 12
                    ) {
                        bestDist = distToMain;
                        bestBase = baseLocation;
                    }
                }

//                ABaseLocation baseLocation = DefineNaturalBase.naturalIfMainIsAt(enemyBase.position());
//                if (baseLocation != null) {
//                    return baseLocation.position().translateByTiles(2, 0);
//                }

                return bestBase.position();
            }
        );
    }

    public static boolean hasBaseAtNatural() {
        return (boolean) cache.get(
            "hasBaseAtNatural",
            57,
            () -> {
                APosition natural = DefineNaturalBase.natural();
                if (natural == null) return false;

                return Select.ourBuildingsWithUnfinished().bases().inRadius(8, natural).notEmpty();
            }
        );
    }

    public static AUnit bunkerAtNatural() {
        return (AUnit) cache.get(
            "hasBunkerAtNatural",
            57,
            () -> {
                APosition natural = DefineNaturalBase.natural();
                if (natural == null) return null;

                return Select.ourWithUnfinishedOfType(AUnitType.Terran_Bunker)
                    .inRadius(9, natural)
                    .nearestTo(natural);
            }
        );
    }
}
