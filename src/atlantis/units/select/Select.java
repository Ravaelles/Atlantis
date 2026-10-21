package atlantis.units.select;

import atlantis.map.position.APosition;
import atlantis.core.world.UnitIndex;
import atlantis.game.AGame;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Units;
import atlantis.util.cache.Cache;

import java.util.*;

/**
 * This class allows to easily select units e.g. to select one of your Marines, nearest to given location, you
 * would run:<br />
 * <p>
 * <b> Select.our().ofType(AUnitType.Terran_Marine).nearestTo(Select.mainBase()) </b>
 * </p>
 * It uses nice flow and every next method filters out units that do not fulfill certain conditions.<br />
 * Unless clearly specified otherwise, this class returns <b>ONLY COMPLETED</b> units.
 */
public class Select<T extends AUnit> extends BaseSelect<T> {

    //    protected static SelectUnitsCache cache = new SelectUnitsCache();
    protected static Cache<Selection> cache = new Cache<>();
    protected static Cache<Integer> cacheInt = new Cache<>();

    /**
     * The frame-frozen unit lists every migrated select reads (Stage F):
     * rebuilt once per frame from the same live sources, so one
     * invalidation rule replaces per-key TTLs. Ours and all-units stay
     * separate sources: ours-membership is list membership, never a
     * predicate (sideless test doubles must not leak across sides).
     * Unmigrated keys keep their old caches below.
     */
    private static UnitIndex allUnitsIndex;
    private static int allUnitsIndexFrame = -1;
    private static UnitIndex oursIndex;
    private static int oursIndexFrame = -1;
    private static UnitIndex enemyIndex;
    private static int enemyIndexFrame = -1;
    private static UnitIndex neutralIndex;
    private static int neutralIndexFrame = -1;

    private static UnitIndex allUnitsIndex() {
        if (allUnitsIndexFrame != AGame.now()) {
            allUnitsIndex = new UnitIndex(allUnits());
            allUnitsIndexFrame = AGame.now();
        }
        return allUnitsIndex;
    }

    private static UnitIndex oursIndex() {
        if (oursIndexFrame != AGame.now()) {
            oursIndex = new UnitIndex(ourUnitsWithUnfinishedList());
            oursIndexFrame = AGame.now();
        }
        return oursIndex;
    }

    private static UnitIndex enemyIndex() {
        if (enemyIndexFrame != AGame.now()) {
            enemyIndex = new UnitIndex(enemyUnits());
            enemyIndexFrame = AGame.now();
        }
        return enemyIndex;
    }

    private static UnitIndex neutralIndex() {
        if (neutralIndexFrame != AGame.now()) {
            neutralIndex = new UnitIndex(neutralUnits());
            neutralIndexFrame = AGame.now();
        }
        return neutralIndex;
    }


    // =====================================================================

    /**
     * Drops every cached query. Called when our units change - a unit was
     * created or a building finished - which is exactly when
     * {@link #mainOrAnyBuildingPosition()} has to be recomputed: it answers
     * "where is our main", and the event that fires the clear may be the event
     * that moved the answer.
     */
    public static void clearCache() {
        cache.clear();
        cacheList.clear();
        cacheInt.clear();
        allUnitsIndexFrame = -1;
        oursIndexFrame = -1;
        enemyIndexFrame = -1;
        neutralIndexFrame = -1;
    }

    public static Cache cache() {
        return cache;
    }

    // Constructor is private, use our(), enemy() or neutral() methods

    // =====================================================================

    /**
     * Selects all of our finished and existing units (units AND buildings).
     */
    public static Selection our() {
        return new Selection(oursIndex().matching(
            unit -> unit.isCompleted()
        ), "our");
    }

    /**
     * Selects all visible enemy units. Since they're visible, the parameterized type is AUnit
     */
    public static Selection enemy() {
        return new Selection(enemyIndex().matching(
            AUnit::isAlive
        ), "enemy");
    }

    /**
     * Selects all game units including minerals, geysers and enemy units.
     */
    public static Selection all() {
        return new Selection(allUnitsIndex().all(), "all");
    }

    // =====================================================================
    // Create base object

    /**
     * Selects all of our bases.
     */
    public static Selection ourBases() {
        return ourOfType(
            AUnitType.Zerg_Hatchery, AUnitType.Zerg_Lair,
            AUnitType.Zerg_Hive, AUnitType.Protoss_Nexus, AUnitType.Terran_Command_Center
        );
    }

    public static Selection ourBasesWithUnfinished() {
        return ourWithUnfinished().ofType(
            AUnitType.Zerg_Hatchery, AUnitType.Zerg_Lair,
            AUnitType.Zerg_Hive, AUnitType.Protoss_Nexus, AUnitType.Terran_Command_Center
        );
    }

    /**
     * Selects our workers (that is of type Terran SCV or Zerg Drone or Protoss Probe).
     */
    public static Selection ourWorkers() {
        return new Selection(
            new TreeSet<>(oursIndex().matching(AUnit::isWorker)),
            "ourWorkers"
        );
    }

    /**
     * Selects all VISIBLE enemy units. Since they're visible, the type is AUnit.
     * Note that this might be null if no units are visible. For the list of ALL known enemy units
     *
     * @see EnemyUnits::visibleAndFogged()
     */
    public static Selection enemyCombatUnits() {
        return new Selection(enemyIndex().matching(
            unit -> !unit.isWorker() && (unit.isRealUnit() || unit.isCombatBuilding())
        ), "enemyCombatUnits");
    }

    /**
     * Selects enemy units of given type(s).
     */
//    public static Selection enemies(AUnitType... type) {
    public static Selection enemies(AUnitType type) {
        return new Selection(enemyIndex().matching(
            unit -> unit.is(type)
        ), "enemies:" + type.id());
    }

    /**
     * Selects our units of given type.
     */
    public static Selection ourOfType(AUnitType type) {
        return new Selection(oursIndex().matching(
            unit -> unit.isCompleted() && unit.is(type)
        ), "ourOfType:" + type.id());
    }

    public static Selection ourOfTypeWithUnfinished(AUnitType type) {
        return new Selection(oursIndex().matching(
            unit -> unit.isCompleted() && unit.is(type)
        ), "ourOfTypeWithUnfinished:" + type.id());
    }

    public static Selection ourOfType(AUnitType... types) {
        return new Selection(oursIndex().matching(
            unit -> unit.isCompleted() && unit.is(types)
        ), "ourOfType:" + AUnitType.arrayToIds(types));
    }

    /**
     * Counts our completed units of given type.
     */
    public static int countOurOfType(AUnitType type) {
//        return cacheInt.get(
//            "countOurOfType:" + type.id(),
//            type.isABuilding() ? 0 : 37,
//            () -> {
        int total = 0;

        for (AUnit unit : ourUnitsWithUnfinishedList()) {
            if (unit.isCompleted() && unit.is(type)) {
                total++;
            }
        }

        return total;
//            }
//        );
    }

    public static int countOurOfTypesWithUnfinished(AUnitType... types) {
//        return cacheInt.get(
//            "countOurOfTypesWithUnfinished:" + AUnitType.arrayToIds(types),
//            0,
//            () -> {
        int total = 0;

        for (AUnit unit : ourUnitsWithUnfinishedList()) {
            if (unit.is(types)) {
                total++;
            }
        }

        return total;
//            }
//        );
    }

    /**
     * Counts our units of given type.
     */
    public static int countOurOfTypeWithUnfinished(AUnitType type) {
//        return cacheInt.get(
//            "countOurOfTypeWithUnfinished:" + type.name(),
//            0,
//            () -> {
        int total = 0;

        for (AUnit unit : ourUnitsWithUnfinishedList()) {
            if (unit.is(type)) total++;
        }

        if (!type.isABuilding()) {
            AUnitType producer = type.whatBuildsIt();
            for (AUnit producingUnit : Select.ourOfType(producer).list()) {
                if (producingUnit.buildUnit() != null && producingUnit.buildUnit().type().is(type)) total++;
            }
        }
//        else {
//            total += ConstructionRequests.notStartedOfType(type).size();
//        }

        return total;
//            }
//        );
    }

    public static int countOurUnfinishedOfType(AUnitType type) {
        int total = 0;

        for (AUnit unit : oursIndex().all()) {
            if (!unit.isCompleted() && unit.is(type)) total++;
        }

        return total;
    }

    /**
     * Selects our units of given type(s).
     */
    public static Selection ourWithUnfinished(AUnitType type) {
        return new Selection(oursIndex().matching(
            unit -> unit.is(type)
        ), "ourOfTypeWithUnfinished:" + type.name());
    }

    /**
     * Selects all of our finished combat units (no buildings, workers, spider mines etc).
     */
    public static Selection ourCombatUnits() {
        return new Selection(oursIndex().matching(
            unit -> unit.isCompleted() && unit.isCombatUnit() && !unit.isABuilding()
        ), "ourCombatUnits");
    }

    /**
     * Selects all of our units (units, buildings, but no spider mines etc), <b>even those unfinished</b>.
     */
    public static Selection ourWithUnfinished() {
        return new Selection(oursIndex().matching(
            AUnit::isAlive
        ), "ourWithUnfinished");
    }

    public static Selection ourWithUnfinishedOfType(AUnitType type) {
        return new Selection(oursIndex().matching(
            unit -> unit.is(type)
        ), "ourWithUnfinishedOfType:" + type.id());
    }

    /**
     * Returns all:
     * - finished units,
     * - unfinished units,
     * - nont started constructions
     */
//    public static Selection ourOfTypeWithPlanned(AUnitType type) {
//        String cachePath;
//        return cache.get(
//                cachePath = "ourOfTypeWithPlanned:" + type.id(),
//                0,
//                () -> {
//                    List<AUnit> data = new ArrayList<>(ourUnits());
//                    data.addA
//
//                    return new Selection(data, cachePath);
//                }
//        );
//    }

    /**
     * Selects our unfinished units.
     */
    public static Selection ourUnfinished() {
        return new Selection(oursIndex().matching(
            unit -> !unit.isCompleted()
        ), "ourUnfinished");
    }

    /**
     * Selects our units, not buildings, not spider mines, not larvae.
     */
    public static Selection ourRealUnits() {
        return new Selection(oursIndex().matching(
            unit -> unit.isCompleted() && unit.isRealUnit()
        ), "ourRealUnits");
    }

    /**
     * Selects our unfinished units.
     */
    public static Selection ourUnfinishedRealUnits() {
        return new Selection(oursIndex().matching(
            unit -> !unit.isCompleted() && unit.isRealUnit()
        ), "ourUnfinishedRealUnits");
    }

    /**
     * Selects all visible enemy units. Since they're visible, the parameterized type is AUnit
     */
    public static Selection enemyRealUnits() {
        return enemyRealUnits(true, true, false);
    }

    /**
     * Selects all visible enemy units. Since they're visible, the parameterized type is AUnit
     */
    public static Selection enemyRealUnitsWithBuildings() {
        return new Selection(enemyIndex().matching(
            unit -> unit.isABuilding() || unit.isRealUnit()
        ), "enemyRealUnitsWithBuildings");
    }

    /**
     * Selects all visible enemy units. Since they're visible, the parameterized type is AUnit
     */
    public static Selection enemyRealUnits(
        boolean includeGroundUnits, boolean includeAirUnits, boolean includeBuildings
    ) {
        String key = "enemyRealUnits" + (includeGroundUnits ? "T" : "F") + (includeAirUnits ? "T" : "F") + (includeBuildings ? "T" : "F");
        return new Selection(enemyIndex().matching(
            unit -> (unit.isABuilding() || unit.isRealUnit())
                && (includeBuildings || !unit.isABuilding())
                && (includeGroundUnits || !unit.isGroundUnit())
                && (includeAirUnits || !unit.isAir())
        ), key);
    }

    /**
     * Selects all visible neutral units (minerals, geysers, critters). Since they're visible, the
     * parameterized type is AUnit
     */
    public static Selection neutral() {
        return new Selection(neutralIndex().all(), "neutral");
    }

    /**
     * Selects all (accessible) minerals on the map.
     */
    public static Selection minerals() {
        return new Selection(neutralIndex().ofType(
            AUnitType.Resource_Mineral_Field,
            AUnitType.Resource_Mineral_Field_Type_2,
            AUnitType.Resource_Mineral_Field_Type_3
        ), "minerals");
    }

    /**
     * Selects all (accessible) minerals on the map.
     */
    public static Selection mineralsAndGeysers() {
        return new Selection(neutralIndex().ofType(
            AUnitType.Resource_Vespene_Geyser,
            AUnitType.Resource_Mineral_Field,
            AUnitType.Resource_Mineral_Field_Type_2,
            AUnitType.Resource_Mineral_Field_Type_3
        ), "mineralsAndGeysers");
    }

    /**
     * Selects all geysers on the map.
     */
    public static Selection geysers() {
        return new Selection(
            neutralIndex().ofType(AUnitType.Resource_Vespene_Geyser), "geysers");
    }

    public static Selection geysersAndGasBuildings() {
        return new Selection(allUnitsIndex().ofType(
            AUnitType.Resource_Vespene_Geyser,
            AUnitType.Protoss_Assimilator,
            AUnitType.Terran_Refinery,
            AUnitType.Zerg_Extractor
        ), "geysersAndGasBuildings");
    }

    /**
     * Create initial search-pool of units from given collection of units.
     */
//    public static Selection from(Collection<AUnit> units, String initCachePath) {
//        return new Selection(units, initCachePath);
//    }
    public static Selection from(Collection<? extends AUnit> units, String initCachePath) {
        return new Selection(units, initCachePath);
    }

    /**
     * Create initial search-pool of units from Units object.
     */
    public static Selection from(Units units) {
        return new Selection(units.list(), null);
    }

    public static Selection from(AUnit[] units) {
        return new Selection(Arrays.asList(units), null);
    }

    // === Aux ======================================================

    public static void printCache() {
        System.out.println("--- Unit selection cache ---");
        cache.print(null, true);
        cacheInt.print(null, true);
    }

    /**
     * Returns first unit being base. For your units this is most likely your main base, for enemy it will be
     * first discovered base.
     */
    public static AUnit main() {
        AUnit main = mainFromCache();
        if (main != null) return main;

        // Why `ourWithUnfinished()` and not `ourBuildings()`: this runs during the
        // first seconds, when the base is not yet completed. `ourBuildings()`
        // excludes unfinished units, so it returned empty, `main()` returned null,
        // and the null propagated into the position finders as "near = null" -
        // which made every build position fail with "Can't physically build here"
        // (measured 2026-10-08 on OpenBW, where the client attaches a few frames
        // into the game: the bot produced nothing for the whole run).
        return ourWithUnfinished().bases().first();
    }

    private static AUnit mainFromCache() {
        // Was a 73-frame TTL cache; the frame index answers the same question
        // fresh every frame (a dead main answered for a minute was the stale
        // direction, never the fresh one).
        List<AUnit> bases = ourBases().list();
        return bases.isEmpty() ? Select.ourBuildings().first() : (bases.get(0).isAlive() ? bases.get(0) : null);
    }

    public static AUnit mainOrAnyBuilding() {
        AUnit main = Select.main();
        if (main != null) return main;

        return Select.ourBuildings().first();
    }

    public static AUnit mainOrAnyUnit() {
        AUnit main = Select.mainOrAnyBuilding();
        if (main != null) return main;

        return Select.ourRealUnits().first();
    }

    public static APosition mainOrAnyBuildingPosition() {
        AUnit building = mainOrAnyBuilding();
        return building != null ? building.position() : null;
    }

    public static boolean haveMain() {
        return main() != null;
    }

    /**
     * Returns second (natural) base <b>or if we have only one base</b>, it returns the only base we have.
     */
    public static AUnit naturalOrMain() {
        List<? extends AUnit> bases = Select.ourBases().notLifted().list();

        if (bases.size() >= 2) {
            return bases.get(1);
        }
        else if (bases.size() == 1) {
            return bases.get(0);
        }
        else {
            return null;
        }
    }


    /**
     * Selects all our tanks, both sieged and unsieged.
     */
    public static Selection ourTanks() {
        return our().tanks();
    }

    /**
     * Selects all of our Marines, Firebats, Ghosts and Medics.
     */
    public static Selection ourTerranInfantry() {
        return new Selection(oursIndex().completedOfType(
            AUnitType.Terran_Marine, AUnitType.Terran_Medic, AUnitType.Terran_Firebat, AUnitType.Terran_Ghost
        ), "ourTerranInfantry");
    }

    /**
     * Selects all of our Zerg Larvas.
     */
    public static Selection ourLarva() {
        return new Selection(oursIndex().matching(
            unit -> unit.isAlive() && unit.is(AUnitType.Zerg_Larva)
        ), "ourLarva");
    }

    /**
     * Selects all our buildings including those unfinished.
     */
    public static Selection ourBuildingsWithUnfinished() {
        return new Selection(oursIndex().matching(
            unit -> unit.isAlive() && (unit.type().isABuilding() || unit.type().isAddon())
        ), "ourBuildingsWithUnfinished");
    }

    public static Selection ourUnfinishedBuildings() {
        return new Selection(oursIndex().matching(
            unit -> unit.isAlive() && !unit.isCompleted()
                && (unit.type().isABuilding() || unit.type().isAddon())
        ), "ourUnfinishedBuildings");
    }

    /**
     * Selects all our finished buildings.
     */
    public static Selection ourBuildings() {
        return new Selection(oursIndex().matching(
            unit -> unit.isCompleted() && (unit.type().isABuilding() || unit.type().isAddon())
        ), "ourBuildings");
    }

    /**
     * Returns first idle our unit of given type or null if no idle units found.
     */
    public static AUnit ourOneNotTrainingUnits(AUnitType type) {
        for (AUnit unit : ourUnitsWithUnfinishedList()) {
            if (unit.isCompleted() && !unit.isTrainingAnyUnit() && unit.is(type) && !unit.isLifted()) {
                return unit;
            }
        }
        return null;
    }


    /**
     * Selects our workers (that is of type Terran SCV or Zerg Drone or Protoss Probe) that are either
     * gathering minerals or gas.
     */
    public static Selection ourWorkersThatGather(boolean onlyNotCarryingAnything) {
        Selection selectedUnits = Select.our();
        selectedUnits.list().removeIf(unit ->
            !unit.isWorker() || (!unit.isGatheringGas() && !unit.isGatheringMinerals())
                || (onlyNotCarryingAnything && (unit.isCarryingGas() || unit.isCarryingMinerals()))
        );
        return selectedUnits;
    }

    public static Selection ourWorkersMiningMinerals(boolean onlyNotCarryingAnything) {
        Selection selectedUnits = Select.our();
        selectedUnits.list().removeIf(unit ->
            !unit.isWorker() || !unit.isGatheringMinerals()
                || (onlyNotCarryingAnything && (unit.isCarryingGas() || unit.isCarryingMinerals()))
        );
        return selectedUnits;
    }

    public static Selection ourFree(AUnitType type) {
        return Select.ourOfType(type).free();
    }
}
