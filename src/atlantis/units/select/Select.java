package atlantis.units.select;

import atlantis.config.AtlantisConfig;
import atlantis.information.enemy.EnemyUnits;
import atlantis.production.constructing.BuilderManager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Units;
import atlantis.util.We;
import atlantis.util.cache.Cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
    protected static Cache<AUnit> cacheUnit = new Cache<>();

    protected static int microCacheForFrames = 1;

    // =====================================================================

    public static void clearCache() {
        cache.clear();
        cacheList.clear();
        cacheInt.clear();
        cacheUnit.clear();
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
        String cachePath;
        return cache.get(
                cachePath = "our",
                microCacheForFrames,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : ourUnits()) {
                        if (unit.isCompleted()) {
                            data.add(unit);
                        }
                    }

                    return new Selection(data, cachePath);
                }
        );
    }

    /**
     * Selects all visible enemy units. Since they're visible, the parameterized type is AUnit
     */
    public static Selection enemy() {
        String cachePath;
        return cache.get(
                cachePath = "enemy",
                0,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : enemyUnits()) {
                        if (unit.isAlive()) {
                            data.add(unit);
                        } else {
                            System.err.println("Enemy unit but no alive = " + unit);
                        }
                    }

                    return new Selection(data, cachePath);
                }
        );
    }

    public static Selection enemyFoggedUnits() {
        String cachePath;
        return cache.get(
                cachePath = "enemyFoggedUnits",
                0,
                () -> {
                    List<AUnit> data = new ArrayList<>();
                    data.addAll(EnemyUnits.discovered().list());

                    return new Selection(data, cachePath);
                }
        );
    }

    /**
     * Selects all game units including minerals, geysers and enemy units.
     */
    public static Selection all() {
        String cachePath;
        return cache.get(
                cachePath = "all",
                0,
                () -> {
                    List<AUnit> data = new ArrayList<>(allUnits());

                    return new Selection(data, "all");
                }
        );
    }

    // =====================================================================
    // Create base object

    /**
     * Selects all of our bases.
     */
    public static Selection ourBases() {
        String cachePath;
        return cache.get(
                cachePath = "ourBases",
                0,
                () -> {
                    if (We.zerg()) {
                        return ourOfType(
                                AUnitType.Zerg_Hatchery, AUnitType.Zerg_Lair,
                                AUnitType.Zerg_Hive, AUnitType.Protoss_Nexus, AUnitType.Terran_Command_Center
                        );
                    } else {
                        return ourOfType(AtlantisConfig.BASE);
                    }
                }
        );
    }

    /**
     * Selects our workers (that is of type Terran SCV or Zerg Drone or Protoss Probe).
     */
    public static Selection ourWorkers() {
        String cachePath;
        return cache.get(
                cachePath = "ourWorkers",
                microCacheForFrames,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : ourUnits()) {
                        if (unit.isCompleted() && unit.isWorker()) {
                            data.add(unit);
                        }
                    }

                    return new Selection(data, cachePath);
                }
        );
    }

    /**
     * Selects all units of given type(s).
     */
    public static Selection allOfType(AUnitType type) {
        String cachePath;
        return cache.get(
                cachePath = "allOfType:" + type.name(),
                0,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : allUnits()) {
                        if (unit.isCompleted() && unit.is(type)) {
                            data.add(unit);
                        }
                    }

                    return new Selection(data, cachePath);
                }
        );
    }

    /**
     * Selects all VISIBLE enemy units. Since they're visible, the type is AUnit.
     * Note that this might be null if no units are visible. For the list of ALL known enemy units
     * @see EnemyUnits::visibleAndFogged()
     */
    public static Selection enemyCombatUnits() {
        String cachePath;
        return cache.get(
                cachePath = "enemyCombatUnits",
                0,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : enemyUnits()) {
                        if (!unit.isWorker() && unit.isRealUnit() || unit.isCombatBuilding()) {
                            data.add(unit);
                        }
                    }

                    return new Selection(data, cachePath);
                }
        );
    }

    /**
     * Selects enemy units of given type(s).
     */
//    public static Selection enemies(AUnitType... type) {
    public static Selection enemies(AUnitType type) {
        String cachePath;
        return cache.get(
                cachePath = "enemies:" + type.id(),
                microCacheForFrames,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : enemyUnits()) {
                        if (unit.is(type)) {
                            data.add(unit);
                        }
                    }

                    return new Selection(data, cachePath);
                }
        );
    }

    /**
     * Selects our units of given type.
     */
    public static Selection ourOfType(AUnitType type) {
        String cachePath;
        return cache.get(
                cachePath = "ourOfType:" + type.id(),
                microCacheForFrames,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : ourUnits()) {
                        if (unit.isCompleted() && unit.is(type)) {
                            data.add(unit);
                        }
                    }

                    return new Selection(data, cachePath);
                }
        );
    }

    public static Selection ourOfType(AUnitType... types) {
        String cachePath;
        return cache.get(
                cachePath = "ourOfType:" + AUnitType.arrayToIds(types),
                microCacheForFrames,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : ourUnits()) {
                        if (unit.isCompleted() && unit.is(types)) {
                            data.add(unit);
                        }
                    }

                    return new Selection(data, cachePath);
                }
        );
    }

    /**
     * Counts our completed units of given type.
     */
    public static int countOurOfType(AUnitType type) {
        return cacheInt.get(
                "countOurOfType:" + type.name(),
                type.isBuilding() ? 0 : 37,
                () -> {
                    int total = 0;

                    for (AUnit unit : ourUnits()) {
                        if (unit.isCompleted() && unit.is(type)) {
                            total++;
                        }
                    }

                    return total;
                }
        );
    }

    public static int countOurOfTypes(AUnitType... types) {
        return cacheInt.get(
                "countOurOfTypes:" + AUnitType.arrayToIds(types),
                0,
                () -> {
                    int total = 0;

                    for (AUnit unit : ourUnits()) {
                        if (unit.isCompleted() && unit.is(types)) {
                            total++;
                        }
                    }

                    return total;
                }
        );
    }

    public static int countOurOfTypesWithUnfinished(AUnitType... types) {
        return cacheInt.get(
                "countOurOfTypesWithUnfinished:" + AUnitType.arrayToIds(types),
                0,
                () -> {
                    int total = 0;

                    for (AUnit unit : ourUnits()) {
                        if (unit.is(types)) {
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
    public static int countOurOfTypeWithUnfinished(AUnitType type) {
        return cacheInt.get(
                "countOurOfTypeWithUnfinished:" + type.name(),
                microCacheForFrames,
                () -> {
                    int total = 0;

                    for (AUnit unit : ourUnits()) {
                        if (unit.is(type)) {
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
    public static Selection ourWithUnfinished(AUnitType type) {
        String cachePath;
        return cache.get(
                cachePath = "ourOfTypeWithUnfinished:" + type.name(),
                microCacheForFrames,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : ourUnits()) {
                        if (unit.is(type)) {
                            data.add(unit);
                        }
                    }

                    return new Selection(data, cachePath);
                }
        );
    }

    /**
     * Selects all of our finished combat units (no buildings, workers, spider mines etc).
     */
    public static Selection ourCombatUnits() {
        String cachePath;
        return cache.get(
                cachePath = "ourCombatUnits",
                microCacheForFrames,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : ourUnits()) {
                        if (unit.isCompleted() && unit.isCombatUnit()) {
                            data.add(unit);
                        }
                    }

                    return new Selection(data, cachePath);
                }
        );
    }

    /**
     * Selects all of our units (units, buildings, but no spider mines etc), <b>even those unfinished</b>.
     */
    public static Selection ourWithUnfinished() {
        String cachePath;
        return cache.get(
                cachePath = "ourWithUnfinished",
                microCacheForFrames,
                () -> {
                    List<AUnit> data = new ArrayList<>(ourUnits());

                    return new Selection(data, cachePath);
                }
        );
    }

    public static Selection ourWithUnfinishedOfType(AUnitType type) {
        String cachePath;
        return cache.get(
                cachePath = "ourWithUnfinishedOfType:" + type.id(),
                microCacheForFrames,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : ourWithUnfinishedUnits()) {
                        if (unit.is(type)) {
                            data.add(unit);
                        }
                    }

                    return new Selection(data, cachePath);
                }
        );
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
//                microCacheForFrames,
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
        String cachePath;
        return cache.get(
                cachePath = "ourUnfinished",
                microCacheForFrames,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : ourUnits()) {
                        if (!unit.isCompleted()) {
                            data.add(unit);
                        }
                    }

                    return new Selection(data, cachePath);
                }
        );
    }

    /**
     * Selects our units, not buildings, not spider mines, not larvae.
     */
    public static Selection ourRealUnits() {
        String cachePath;
        return cache.get(
                cachePath = "ourRealUnits",
                microCacheForFrames,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : ourUnits()) {
                        if (unit.isCompleted() && unit.isRealUnit()) {
                            data.add(unit);
                        }
                    }

                    return new Selection(data, cachePath);
                }
        );
    }

    /**
     * Selects our unfinished units.
     */
    public static Selection ourUnfinishedRealUnits() {
        String cachePath;
        return cache.get(
                cachePath = "ourUnfinishedRealUnits",
                microCacheForFrames,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : ourUnits()) {
                        if (!unit.isCompleted() && unit.isRealUnit()) {
                            data.add(unit);
                        }
                    }

                    return new Selection(data, cachePath);
                }
        );
    }

    /**
     * Selects all visible enemy units. Since they're visible, the parameterized type is AUnit
     */
    public static Selection enemyRealUnits() {
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
    public static Selection enemyRealUnitsWithBuildings() {
        String cachePath;
        return cache.get(
                cachePath = "enemyRealUnitsWithBuildings",
                0,
                () -> enemyRealUnits(true, true, true)
        );
    }

    /**
     * Selects all visible enemy units. Since they're visible, the parameterized type is AUnit
     */
    public static Selection enemyRealUnits(
            boolean includeGroundUnits, boolean includeAirUnits, boolean includeBuildings
    ) {
        String cachePath;
        return cache.get(
                cachePath = "enemyRealUnits" + (includeGroundUnits ? "T" : "F") + (includeAirUnits ? "T" : "F") + (includeBuildings ? "T" : "F"),
                0,
                () -> {
                    List<AUnit> data = new ArrayList<>(enemyUnits());
                    data.removeIf(
                            u -> (
                                !u.isCompleted()
                                || (!u.isABuilding() && !u.isRealUnit())
                                || (!includeBuildings && u.isABuilding())
                                || (!includeGroundUnits && u.isGroundUnit())
                                || (!includeAirUnits && u.isAir())
                            )
                    );

                    return new Selection(data, cachePath);
                }
        );
    }

    /**
     * Selects all visible neutral units (minerals, geysers, critters). Since they're visible, the
     * parameterized type is AUnit
     */
    public static Selection neutral() {
        String cachePath;
        return cache.get(
                cachePath = "neutral",
                microCacheForFrames,
                () -> new Selection(neutralUnits(), cachePath)
        );
    }

    /**
     * Selects all (accessible) minerals on the map.
     */
    public static Selection minerals() {
        String cachePath;
        return cache.get(
                cachePath = "minerals",
                30,
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
    public static Selection geysers() {
        String cachePath;
        return cache.get(
                cachePath = "geysers",
                50,
                () -> neutral().ofType(AUnitType.Resource_Vespene_Geyser)
        );
    }

    public static Selection geyserBuildings() {
        String cachePath;
        return cache.get(
                cachePath = "geyserBuildings",
                30,
                () -> all().ofType(
                        AUnitType.Resource_Vespene_Geyser,
                        AUnitType.Protoss_Assimilator,
                        AUnitType.Terran_Refinery,
                        AUnitType.Zerg_Extractor
                )
        );
    }

    /**
     * Create initial search-pool of units from given collection of units.
     */
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
        cacheUnit.print(null, true);
        cacheInt.print(null, true);
    }

    /**
     * Returns first unit being base. For your units this is most likely your main base, for enemy it will be
     * first discovered base.
     */
    public static AUnit main() {
        String cachePath;
        AUnit base = cacheUnit.get(
                cachePath = "main",
                30,
                () -> {
                    List<AUnit> bases = ourBases().list();
                    return bases.isEmpty() ? Select.ourBuildings().first() : (bases.get(0).isAlive() ? bases.get(0) : null);
                }
        );

        if (base != null && base.isAlive()) {
            return base;
        }

        return null;
    }

    public static boolean haveMain() {
        return main() != null;
    }

    /**
     * Returns second (natural) base <b>or if we have only one base</b>, it returns the only base we have.
     */
    public static AUnit naturalOrMain() {
        String cachePath;

        return cacheUnit.get(
                cachePath = "naturalOrMain",
                0,
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
    public static Selection ourTanks() {
        String cachePath;
        return cache.get(
                cachePath = "ourTanks",
                microCacheForFrames,
                () -> our().ofType(AUnitType.Terran_Siege_Tank_Siege_Mode, AUnitType.Terran_Siege_Tank_Tank_Mode)
        );
    }

    /**
     * Selects all our sieged tanks.
     */
    public static Selection ourTanksSieged() {
        String cachePath;
        return cache.get(
                cachePath = "ourTanksSieged",
                microCacheForFrames,
                () -> our().ofType(AUnitType.Terran_Siege_Tank_Siege_Mode)
        );
    }

    /**
     * Selects all of our Marines, Firebats, Ghosts and Medics.
     */
    public static Selection ourTerranInfantry() {
        String cachePath;
        return cache.get(
                cachePath = "ourTerranInfantry",
                microCacheForFrames,
                () -> our().ofType(AUnitType.Terran_Marine, AUnitType.Terran_Medic, AUnitType.Terran_Firebat, AUnitType.Terran_Ghost)
        );
    }

    /**
     * Selects all of our Marines, Firebats, Ghosts.
     */
    public static Selection ourTerranInfantryWithoutMedics() {
        String cachePath;
        return cache.get(
                cachePath = "ourTerranInfantryWithoutMedics",
                microCacheForFrames,
                () -> our().ofType(AUnitType.Terran_Marine, AUnitType.Terran_Firebat, AUnitType.Terran_Ghost)
        );
    }

    /**
     * Selects all of our Zerg Larvas.
     */
    public static Selection ourLarva() {
        String cachePath;
        return cache.get(
                cachePath = "ourLarva",
                microCacheForFrames,
                () -> {
                    Selection selectedUnits = ourWithUnfinished();
                    selectedUnits.list().removeIf(unit -> !unit.is(AUnitType.Zerg_Larva));
                    return selectedUnits;
                }
        );
    }

    /**
     * Selects all our buildings including those unfinished.
     */
    public static Selection ourBuildingsWithUnfinished() {
        String cachePath;
        return cache.get(
                cachePath = "ourBuildingsWithUnfinished",
                microCacheForFrames,
                () -> {
                    Selection selectedUnits = Select.ourWithUnfinished();
                    selectedUnits.list().removeIf(unit -> !unit.type().isBuilding() && !unit.type().isAddon());
                    return selectedUnits;
                }
        );
    }

    /**
     * Selects all our finished buildings.
     */
    public static Selection ourBuildings() {
        String cachePath;
        return cache.get(
                cachePath = "ourBuildings",
                microCacheForFrames,
                () -> our().buildings()
        );
    }

    /**
     * Returns first idle our unit of given type or null if no idle units found.
     */
    public static AUnit ourOneNotTrainingUnits(AUnitType type) {
        for (AUnit unit : ourUnits()) {
            if (unit.isCompleted() && !unit.isTrainingAnyUnit() && unit.is(type) && !unit.isLifted()) {
                return unit;
            }
        }
        return null;
    }

    /**
     * Selects our workers that are free to construct building or repair a unit. That means they mustn't
     * repait any other unit or construct other building.
     */
    public static Selection ourWorkersFreeToBuildOrRepair() {
        Selection selectedUnits = Select.ourWorkers();
        selectedUnits.list().removeIf(unit ->
                unit.isConstructing() || unit.isRepairing()
                        || BuilderManager.isBuilder(unit) || unit.isScout()
                        || unit.isRepairerOfAnyKind()
        );

        return selectedUnits;
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

}
