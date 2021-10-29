package atlantis.units.select;

import atlantis.AGame;
import atlantis.AtlantisConfig;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Units;
import atlantis.util.Cache;

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

    // CACHED variables
//    private static Cache<Object> cacheStatic = new Cache<>();

//    private SelectUnitsCache cache = new SelectUnitsCache();
//    private Cache<Integer> cacheInt = new Cache<>();
//    private Cache<AUnit> cacheUnit = new Cache<>();
//    private Cache<List<AUnit>> cacheList = new Cache<>();
//    private static AUnit _cached_mainBase = null;
    protected static SelectUnitsCache cache = new SelectUnitsCache();
    protected static Cache<Integer> cacheInt = new Cache<>();
    protected static Cache<AUnit> cacheUnit = new Cache<>();
//    private static Cache<List<AUnit>> cacheList = new Cache<>();

    // =====================================================================
    // Constructor is private, use our(), enemy() or neutral() methods

//    protected String addCacheKeyToCurrentCachePath(String method) {
//        currentCachePath += (currentCachePath.length() > 0 ? "." : "") + method;
//        System.out.println("path = " + currentCachePath);
//        return currentCachePath;
//    }

    // =====================================================================
    // Main static selectors

    /**
     * Selects all of our finished and existing units (units AND buildings).
     */
    public static Selection<AUnit> our() {
        String cachePath;
        return cache.get(
                cachePath = "our",
                1,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : ourUnits()) {
                        if (unit.isCompleted()) {
                            data.add(unit);    //TODO: make it more efficient by just querying the cache of known units
                        }
                    }

                    return new Selection<>(data, cachePath);
                }
        );
    }

    /**
     * Selects all visible enemy units. Since they're visible, the parameterized type is AUnit
     */
    public static Selection<? extends AUnit> enemy() {
        String cachePath;
        return cache.get(
                cachePath = "enemy",
                0,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : enemyUnits()) {
                        if (unit.isAlive()) {
                            data.add(unit);
                        }
                    }

                    return new Selection<>(data, cachePath);
                }
        );
    }

    /**
     * Selects all game units including minerals, geysers and enemy units.
     */
    public static Selection<? extends AUnit> all() {
        String cachePath;
        return cache.get(
                cachePath = "all",
                0,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    data.addAll(ourUnits());
                    data.addAll(enemyUnits());
                    data.addAll(neutralUnits());

                    return new Selection<>(data, "all");
                }
        );
    }

    // =====================================================================
    // Create base object

    /**
     * Selects all of our bases.
     */
    public static Selection<AUnit> ourBases() {
        String cachePath;
        return cache.get(
                cachePath = "ourBases",
                20,
                () -> {
                    if (AGame.isPlayingAsZerg()) {
                        return ourBuildings().ofType(
                                AUnitType.Zerg_Hatchery, AUnitType.Zerg_Lair,
                                AUnitType.Zerg_Hive, AUnitType.Protoss_Nexus, AUnitType.Terran_Command_Center
                        );
                    } else {
                        return our().ofType(AtlantisConfig.BASE);
                    }
                }
        );
    }

    /**
     * Selects our workers (that is of type Terran SCV or Zerg Drone or Protoss Probe).
     */
    public static Selection<AUnit> ourWorkers() {
        String cachePath;
        return cache.get(
                cachePath = "ourWorkers",
                1,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : ourUnits()) {
                        if (unit.isCompleted() && unit.isWorker()) {
                            data.add(unit);
                        }
                    }

                    return new Selection<>(data, cachePath);
                }
        );
    }

    /**
     * Selects all units of given type(s).
     */
    public static Selection<? extends AUnit> allOfType(AUnitType type) {
        String cachePath;
        return cache.get(
                cachePath = "allOfType:" + type.shortName(),
                0,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : allUnits()) {
                        if (unit.isCompleted() && unit.isType(type)) {
                            data.add(unit);
                        }
                    }

                    return new Selection<>(data, cachePath);
                }
        );
    }

    /**
     * Selects all visible enemy units. Since they're visible, the parameterized type is AUnit
     */
    public static Selection<? extends AUnit> enemyCombatUnits() {
        String cachePath;
        return cache.get(
                cachePath = "enemyCombatUnits",
                0,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : enemyUnits()) {
                        if (!unit.isWorker() && unit.isRealUnit()) {
                            data.add(unit);
                        }
                    }

                    return new Selection<>(data, cachePath);
                }
        );
    }

    /**
     * Selects enemy units of given type(s).
     */
//    public static Selection<? extends AUnit> enemies(AUnitType... type) {
    public static Selection<? extends AUnit> enemies(AUnitType type) {
        String cachePath;
        return cache.get(
                cachePath = "enemies",
                1,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : enemyUnits()) {
                        if (unit.isAlive() && unit.is(type)) {
                            data.add(unit);
                        }
                    }

                    return new Selection<>(data, cachePath);
                }
        );
    }

    /**
     * Selects our units of given type(s).
     */
    public static Selection<AUnit> ourOfType(AUnitType... type) {
        String cachePath;
        return cache.get(
                cachePath = "ourOfType",
                1,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : ourUnits()) {
                        if (unit.isCompleted() && unit.isType(type)) {
                            data.add(unit);
                        }
                    }

                    return new Selection<>(data, cachePath);
                }
        );
    }

    /**
     * Counts our completed units of given type.
     */
    public static int countOurOfType(AUnitType type) {
        return cacheInt.get(
                "countOurOfType:" + type.shortName(),
                1,
                () -> {
                    int total = 0;

                    for (AUnit unit : ourUnits()) {
                        if (unit.isCompleted() && unit.isType(type)) {
                            total++;
                        }
                    }

                    return total;
                }
        );
    }

    /**
     * Counts our units of given type.
     */
    public static int countOurOfTypeIncludingUnfinished(AUnitType type) {
        return cacheInt.get(
                "countOurOfTypeIncludingUnfinished:" + type.shortName(),
                1,
                () -> {
                    int total = 0;

                    for (AUnit unit : ourUnits()) {
                        if (unit.isType(type)) {
                            total++;
                        }
                    }

                    return total;
                }
        );
    }

    /**
     * Selects our units of given type(s).
     */
    public static Selection<AUnit> ourOfTypeIncludingUnfinished(AUnitType type) {
        String cachePath;
        return cache.get(
                cachePath = "ourOfTypeIncludingUnfinished:" + type.shortName(),
                1,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : ourUnits()) {
                        if (unit.isType(type)) {
                            data.add(unit);
                        }
                    }

                    return new Selection<>(data, cachePath);
                }
        );
    }

    /**
     * Selects all of our finished combat units (no buildings, workers, spider mines etc).
     */
    public static Selection<AUnit> ourCombatUnits() {
        String cachePath;
        return cache.get(
                cachePath = "ourCombatUnits",
                1,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : ourUnits()) {
                        if (unit.isCompleted() && unit.isRealUnit() && !unit.isWorker()) {
                            data.add(unit);
                        }
                    }

                    return new Selection<>(data, cachePath);
                }
        );
    }

    /**
     * Selects all of our units (units, buildings, but no spider mines etc), <b>even those unfinished</b>.
     */
    public static Selection<AUnit> ourIncludingUnfinished() {
        String cachePath;
        return cache.get(
                cachePath = "ourIncludingUnfinished",
                1,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : ourUnits()) {
                        data.add(unit);
                    }

                    return new Selection<>(data, cachePath);
                }
        );
    }

    /**
     * Selects our unfinished units.
     */
    public static Selection<AUnit> ourUnfinished() {
        String cachePath;
        return cache.get(
                cachePath = "ourUnfinished",
                1,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : ourUnits()) {
                        if (!unit.isCompleted()) {
                            data.add(unit);
                        }
                    }

                    return new Selection<>(data, cachePath);
                }
        );
    }

    /**
     * Selects our units, not buildings, not spider mines, not larvae.
     */
    public static Selection<AUnit> ourRealUnits() {
        String cachePath;
        return cache.get(
                cachePath = "ourRealUnits",
                1,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : ourUnits()) {
                        if (unit.isCompleted() && unit.isRealUnit()) {
                            data.add(unit);
                        }
                    }

                    return new Selection<>(data, cachePath);
                }
        );
    }

    /**
     * Selects our unfinished units.
     */
    public static Selection<AUnit> ourUnfinishedRealUnits() {
        String cachePath;
        return cache.get(
                cachePath = "ourUnfinishedRealUnits",
                1,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : ourUnits()) {

                        if (!unit.isCompleted() && unit.isRealUnit()) {
                            data.add(unit);
                        }
                    }

                    return new Selection<>(data, cachePath);
                }
        );
    }

    /**
     * Selects all visible enemy units. Since they're visible, the parameterized type is AUnit
     */
    public static Selection<? extends AUnit> enemyRealUnits() {
        String cachePath;
        return cache.get(
                cachePath = "enemyRealUnits",
                0,
                () -> enemyRealUnits(true, true, false)
        );
//        return enemyRealUnits(true, true, false);
    }

    /**
     * Selects all visible enemy units. Since they're visible, the parameterized type is AUnit
     */
    public static Selection<? extends AUnit> enemyRealUnits(boolean includeBuildings) {
        String cachePath;
        return cache.get(
                cachePath = "enemyRealUnits" + (includeBuildings ? "T" : "F"),
                0,
                () -> enemyRealUnits(true, true, includeBuildings)
        );
//        return enemyRealUnits(true, true, includeBuildings);
    }

    /**
     * Selects all visible enemy units. Since they're visible, the parameterized type is AUnit
     */
    public static Selection<? extends AUnit> enemyRealUnits(
            boolean includeGroundUnits, boolean includeAirUnits, boolean includeBuildings
    ) {
        String cachePath;
        return cache.get(
                cachePath = "enemyRealUnits" + (includeGroundUnits ? "T" : "F") + (includeAirUnits ? "T" : "F") + (includeBuildings ? "T" : "F"),
                0,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : enemyUnits()) {
                        if ((includeBuildings || !unit.isBuilding()) || unit.isRealUnit()) {
                            if ((includeGroundUnits && unit.isGroundUnit()) || (includeAirUnits && unit.isAirUnit())) {
                                data.add(unit);
                            }
                        }
                    }

                    return new Selection<>(data, cachePath);
                }
        );
    }

    /**
     * Selects all visible neutral units (minerals, geysers, critters). Since they're visible, the
     * parameterized type is AUnit
     */
    public static Selection<? extends AUnit> neutral() {
        String cachePath;
        return cache.get(
                cachePath = "neutral",
                1,
                () -> new Selection<>(neutralUnits(), cachePath)
        );
    }

    /**
     * Selects all (accessible) minerals on the map.
     */
    public static Selection<? extends AUnit> minerals() {
        String cachePath;
        return cache.get(
                cachePath = "minerals",
                5,
                () -> neutral().ofType(
                            AUnitType.Resource_Mineral_Field,
                            AUnitType.Resource_Mineral_Field_Type_2,
                            AUnitType.Resource_Mineral_Field_Type_3
                )
        );
    }

    /**
     * Selects all geysers on the map.
     */
    public static Selection<? extends AUnit> geysers() {
        String cachePath;
        return cache.get(
                cachePath = "geysers",
                10,
                () -> neutral().ofType(AUnitType.Resource_Vespene_Geyser)
        );
    }

    public static Selection<? extends AUnit> geyserBuildings() {
        String cachePath;
        return cache.get(
                cachePath = "geyserBuildings",
                10,
                () -> neutral().ofType(
                        AUnitType.Protoss_Assimilator,
                        AUnitType.Terran_Refinery,
                        AUnitType.Zerg_Extractor
                )
        );
    }

    /**
     * Create initial search-pool of units from given collection of units.
     */
    public static Selection<? extends AUnit> from(Collection<AUnit> units) {
        return new Selection<>(units, null);
    }

    /**
     * Create initial search-pool of units from Units object.
     */
    public static Selection<? extends AUnit> from(Units units) {
        return new Selection<>(units.list(), null);
    }

    /**
     * Create initial search-pool of units from given collection of units.
     */
//    public static Selection<AFoggedUnit> fromData(Collection<AFoggedUnit> units) {
//        return new Selection<AFoggedUnit>(units);
//    }

    // === Aux ======================================================

    public static void printCache() {
        System.out.println("--- Unit selection cache ---");
        cache.print(null, true);
        cacheUnit.print(null, true);
        cacheInt.print(null, true);
    }

    /**
     * Returns first unit being base. For your units this is most likely your main base, for enemy it will be
     * first discovered base.
     */
    public static AUnit mainBase() {
        String cachePath;
        return cacheUnit.get(
                cachePath = "mainBase",
                20,
                () -> {
                    List<AUnit> bases = ourBases().list();
                    return bases.isEmpty() ? Select.ourBuildings().first() : bases.get(0);
                }
        );
    }

    /**
     * Returns second (natural) base <b>or if we have only one base</b>, it returns the only base we have.
     */
    public static AUnit naturalBaseOrMain() {
        String cachePath;

        return cacheUnit.get(
                cachePath = "naturalBaseOrMainIfNoSecond",
                10,
                () -> {
                    List<? extends AUnit> bases = Select.ourBases().list();

                    if (bases.size() >= 2) {
                        return bases.get(1);
                    } else if (bases.size() == 1) {
                        return bases.get(0);
                    } else {
                        return null;
                    }
                }
        );
    }


    /**
     * Selects all our tanks, both sieged and unsieged.
     */
    public static Selection<AUnit> ourTanks() {
        String cachePath;
        return cache.get(
                cachePath = "ourTanks",
                1,
                () -> our().ofType(AUnitType.Terran_Siege_Tank_Siege_Mode, AUnitType.Terran_Siege_Tank_Tank_Mode)
        );
    }

    /**
     * Selects all our sieged tanks.
     */
    public static Selection<AUnit> ourTanksSieged() {
        String cachePath;
        return cache.get(
                cachePath = "ourTanksSieged",
                1,
                () -> our().ofType(AUnitType.Terran_Siege_Tank_Siege_Mode)
        );
    }

    /**
     * Selects all of our Marines, Firebats, Ghosts and Medics.
     */
    public static Selection<AUnit> ourTerranInfantry() {
        String cachePath;
        return cache.get(
                cachePath = "ourTerranInfantry",
                1,
                () -> our().ofType(AUnitType.Terran_Marine, AUnitType.Terran_Medic, AUnitType.Terran_Firebat, AUnitType.Terran_Ghost)
        );
    }

    /**
     * Selects all of our Marines, Firebats, Ghosts.
     */
    public static Selection<AUnit> ourTerranInfantryWithoutMedics() {
        String cachePath;
        return cache.get(
                cachePath = "ourTerranInfantryWithoutMedics",
                1,
                () -> our().ofType(AUnitType.Terran_Marine, AUnitType.Terran_Firebat, AUnitType.Terran_Ghost)
        );
    }

    /**
     * Selects all of our Zerg Larvas.
     */
    public static Selection<AUnit> ourLarva() {
        String cachePath;
        return cache.get(
                cachePath = "ourLarva",
                1,
                () -> {
                    Selection<? extends AUnit> selectedUnits = ourIncludingUnfinished();
                    selectedUnits.list().removeIf(unit -> !unit.is(AUnitType.Zerg_Larva));
                    return selectedUnits;
                }
        );
    }

    /**
     * Selects all our buildings including those unfinished.
     */
    public static Selection<AUnit> ourBuildingsIncludingUnfinished() {
        String cachePath;
        return cache.get(
                cachePath = "ourBuildingsIncludingUnfinished",
                1,
                () -> {
                    Selection<? extends AUnit> selectedUnits = Select.ourIncludingUnfinished();
                    selectedUnits.list().removeIf(unit -> !unit.type().isBuilding() && !unit.type().isAddon());
                    return selectedUnits;
                }
        );
    }

    /**
     * Selects all our finished buildings.
     */
    public static Selection<AUnit> ourBuildings() {
        String cachePath;
        return cache.get(
                cachePath = "ourBuildings",
                1,
                () -> our().buildings()
        );
    }

    /**
     * Returns first idle our unit of given type or null if no idle units found.
     */
    public static AUnit ourOneIdle(AUnitType type) {
        for (AUnit unit : ourUnits()) {
            if (unit.isCompleted() && unit.isIdle() && unit.is(type)) {
                return unit;
            }
        }
        return null;
    }

}
