package atlantis.units;

import atlantis.AGame;
import atlantis.Atlantis;
import atlantis.AtlantisConfig;
import atlantis.constructing.AConstructionManager;
//import atlantis.information.AFoggedUnit;
import atlantis.position.APosition;
import atlantis.position.HasPosition;
import atlantis.repair.ARepairAssignments;
import atlantis.scout.AScoutManager;
import atlantis.util.A;
import atlantis.position.PositionUtil;
import atlantis.util.Cache;
import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;

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
public class Select<T extends AUnit> {

    private final List<T> data;

    // CACHED variables
    private static SelectUnitsCache cache = new SelectUnitsCache();
    private static Cache<Integer> cacheInt = new Cache<>();
    private static Cache<List<AUnit>> cacheList = new Cache<>();
    private static AUnit _cached_mainBase = null;

    // =====================================================================
    // Constructor is private, use our(), enemy() or neutral() methods

    protected Select(Collection<T> unitsData) {
        data = new ArrayList<>(unitsData);
    }

    // =====================================================================
    // Helper for base object

    private static List<AUnit> ourUnits() {
        return cacheList.get(
                "ourUnits",
                1,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (Unit u : AGame.getPlayerUs().getUnits()) {
                        data.add(AUnit.createFrom(u));
                    }

//                    System.out.println("------------");
//                    for (AUnit unit : data) {
//                        System.out.println(unit);
//                    }

                    return data;
                }
        );
    }

    private static List<AUnit> enemyUnits() {
        return cacheList.get(
                "enemyUnits",
                0,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    // === Handle UMS ==========================================

                    if (AGame.isUms()) {
                        Player playerUs = AGame.getPlayerUs();
                        for (Player player : AGame.getPlayers()) {
                            if (player.isEnemy(playerUs)) {
                                for (Unit u : player.getUnits()) {
                                    AUnit unit = AUnit.createFrom(u);
                                    if (unit.isAlive()) {
                                        data.add(unit);
                                    } else {
                                        System.err.println("Enemy unit not alive? Seems a terrible problem.");
                                        System.err.println(unit);
                                        System.err.println(unit.hp() + " // " + unit.isVisibleOnMap() + " // " + unit.effVisible());
                                    }
                                }
                            }
                        }
                    }

                    // =========================================================

                    else {
                        for (Unit u : AGame.getEnemy().getUnits()) {
                            AUnit unit = AUnit.createFrom(u);
                            if (unit.isAlive()) {
                                data.add(unit);
                            }
                        }
                    }

                    return data;
                }
        );
    }

    private static List<AUnit> neutralUnits() {
        return cacheList.get(
                "neutralUnits",
                3,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (Unit u : Atlantis.game().neutral().getUnits()) {
                        AUnit unit = AUnit.createFrom(u);
                        if (unit.isAlive()) {
                            data.add(unit);
                        }
                    }

                    return data;
                }
        );
    }

    private static List<AUnit> allUnits() {
        return cacheList.get(
                "allUnits",
                0,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (Unit u : Atlantis.game().getAllUnits()) {
                        AUnit unit = AUnit.createFrom(u);
                        if (unit.isAlive()) {
                            data.add(unit);
                        }
                    }

                    return data;
                }
        );
    }

    // =====================================================================
    // Create base object
    /**
     * Selects all of our finished and existing units (units, buildings, but no spider mines etc).
     */
    public static Select<AUnit> our() {
        return (Select<AUnit>) cache.get(
                "our",
                1,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : ourUnits()) {
                        if (unit.isCompleted()) {
                            data.add(unit);    //TODO: make it more efficient by just querying the cache of known units
                        }
                    }

                    return new Select<>(data);
                }
        );
    }

    /**
     * Selects all game units including minerals, geysers and enemy units.
     */
    public static Select<? extends AUnit> all() {
        return cache.get(
                "all",
                0,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    data.addAll(ourUnits());
                    data.addAll(enemyUnits());
                    data.addAll(neutralUnits());

                    return new Select<>(data);
                }
        );
    }

    /**
     * Selects all units of given type(s).
     */
    public static Select<? extends AUnit> allOfType(AUnitType type) {
        return cache.get(
                "allOfType:" + type.shortName(),
                0,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : allUnits()) {
                        if (unit.isCompleted() && unit.isType(type)) {
                            data.add(unit);
                        }
                    }

                    return new Select<>(data);
                }
        );
    }

    /**
     * Selects enemy units of given type(s).
     */
//    public static Select<? extends AUnit> enemies(AUnitType... type) {
    public static Select<? extends AUnit> enemies(AUnitType type) {
        List<AUnit> data = new ArrayList<>();

        for (AUnit unit : enemyUnits()) {
            if (unit.isAlive() && unit.is(type)) {
                data.add(unit);
            }
        }

        return new Select<>(data);
    }

    /**
     * Selects our units of given type(s).
     */
    public static Select<AUnit> ourOfType(AUnitType... type) {
        List<AUnit> data = new ArrayList<>();

        for (AUnit unit : ourUnits()) {
            if (unit.isCompleted() && unit.isType(type)) {
                data.add(unit);
            }
        }

        return new Select<>(data);
    }

    /**
     * Selects our units of given type(s).
     */
//    public static Select<AUnit> ourUnfinishedOfType(AUnitType... type) {
//        List<AUnit> data = new ArrayList<>();
//
//        for (AUnit unit : ourUnits()) {
//            if (!unit.isCompleted() && unit.isType(type)) {
//                data.add(unit);
//            }
//        }
//
//        return new Select<AUnit>(data);
//    }

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
    public static Select<AUnit> ourOfTypeIncludingUnfinished(AUnitType type) {
        return (Select<AUnit>) cache.get(
                "ourOfTypeIncludingUnfinished:" + type.shortName(),
                1,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : ourUnits()) {
                        if (unit.isType(type)) {
                            data.add(unit);
                        }
                    }

                    return new Select<>(data);
                }
        );
    }

    /**
     * Selects all of our finished combat units (no buildings, workers, spider mines etc).
     */
    public static Select<AUnit> ourCombatUnits() {
        return (Select<AUnit>) cache.get(
                "ourCombatUnits",
                1,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : ourUnits()) {
                        if (unit.isCompleted() && unit.isRealUnit() && !unit.isWorker()) {
                            data.add(unit);
                        }
                    }

                    return new Select<>(data);
                }
        );
    }

    /**
     * Selects all of our units (units, buildings, but no spider mines etc), <b>even those unfinished</b>.
     */
    public static Select<AUnit> ourIncludingUnfinished() {
        return (Select<AUnit>) cache.get(
                "ourIncludingUnfinished",
                1,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : ourUnits()) {
                        data.add(unit);
                    }

                    return new Select<>(data);
                }
        );
    }

    /**
     * Selects our unfinished units.
     */
    public static Select<AUnit> ourUnfinished() {
        return (Select<AUnit>) cache.get(
                "ourUnfinished",
                1,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : ourUnits()) {
                        if (!unit.isCompleted()) {
                            data.add(unit);
                        }
                    }

                    return new Select<>(data);
                }
        );
    }

    /**
     * Selects our units, not buildings, not spider mines, not larvae.
     */
    public static Select<AUnit> ourRealUnits() {
        return (Select<AUnit>) cache.get(
                "ourRealUnits",
                1,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : ourUnits()) {
                        if (unit.isCompleted() && unit.isRealUnit()) {
                            data.add(unit);
                        }
                    }

                    return new Select<>(data);
                }
        );
    }

    /**
     * Selects our unfinished units.
     */
    public static Select<AUnit> ourUnfinishedRealUnits() {
        return (Select<AUnit>) cache.get(
                "ourUnfinishedRealUnits",
                1,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : ourUnits()) {

                        if (!unit.isCompleted() && unit.isRealUnit()) {
                            data.add(unit);
                        }
                    }

                    return new Select<>(data);
                }
        );
    }

    /**
     * Selects all visible enemy units. Since they're visible, the parameterized type is AUnit
     */
    public static Select<? extends AUnit> enemy() {
        return cache.get(
                "enemy",
                0,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : enemyUnits()) {
                        if (unit.isAlive()) {
                            data.add(unit);
                        }
                    }

                    return new Select<>(data);
                }
        );
    }

    /**
     * Selects all visible enemy units. Since they're visible, the parameterized type is AUnit
     */
//    public static Select<? extends AUnit> enemy(boolean includeGroundUnits, boolean includeAirUnits) {
//        List<AUnit> data = new ArrayList<>();
//
//        for (AUnit unit : enemyUnits()) {
//            if ((!unit.isAirUnit() && includeGroundUnits) || (unit.isAirUnit() && includeAirUnits)) {
//                data.add(unit);
//            }
//        }
//
//        return new Select<>(data);
//    }

    /**
     * Selects all visible enemy units. Since they're visible, the parameterized type is AUnit
     */
    public static Select<? extends AUnit> enemyCombatUnits() {
        return cache.get(
                "enemyCombatUnits",
                0,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : enemyUnits()) {
                        if (!unit.isWorker() && unit.isRealUnit()) {
                            data.add(unit);
                        }
                    }

                    return new Select<>(data);
                }
        );
    }

    /**
     * Selects all visible enemy units. Since they're visible, the parameterized type is AUnit
     */
    public static Select<? extends AUnit> enemyRealUnits() {
        return cache.get(
                "enemyRealUnits",
                0,
                () -> enemyRealUnits(true, true, false)
        );
//        return enemyRealUnits(true, true, false);
    }

    /**
     * Selects all visible enemy units. Since they're visible, the parameterized type is AUnit
     */
    public static Select<? extends AUnit> enemyRealUnits(boolean includeBuildings) {
        return cache.get(
                "enemyRealUnits" + (includeBuildings ? "T" : "F"),
                0,
                () -> enemyRealUnits(true, true, includeBuildings)
        );
//        return enemyRealUnits(true, true, includeBuildings);
    }

    /**
     * Selects all visible enemy units. Since they're visible, the parameterized type is AUnit
     */
    public static Select<? extends AUnit> enemyRealUnits(
            boolean includeGroundUnits, boolean includeAirUnits, boolean includeBuildings
    ) {
        return cache.get(
                "enemyRealUnits" + (includeGroundUnits ? "T" : "F") + (includeAirUnits ? "T" : "F") + (includeBuildings ? "T" : "F"),
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

                    return new Select<>(data);
                }
        );
    }

    /**
     * Selects all visible neutral units (minerals, geysers, critters). Since they're visible, the
     * parameterized type is AUnit
     */
    public static Select<? extends AUnit> neutral() {
        return cache.get(
                "neutral",
                1,
                () -> new Select<>(neutralUnits())
        );
    }

    /**
     * Selects all (accessible) minerals on the map.
     */
    public static Select<? extends AUnit> minerals() {
        return cache.get(
                "minerals",
                5,
                () -> {
                    return neutral().ofType(
                            AUnitType.Resource_Mineral_Field,
                            AUnitType.Resource_Mineral_Field_Type_2,
                            AUnitType.Resource_Mineral_Field_Type_3
                    );
                }
        );
    }

    /**
     * Selects all geysers on the map.
     */
    public static Select<? extends AUnit> geysers() {
        return cache.get(
                "geysers",
                10,
                () -> neutral().ofType(AUnitType.Resource_Vespene_Geyser)
        );
    }

    public static Select<? extends AUnit> geyserBuildings() {
        return cache.get(
                "geyserBuildings",
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
    public static Select<? extends AUnit> from(Collection<AUnit> units) {
        return new Select<>(units);
    }

    /**
     * Create initial search-pool of units from Units object.
     */
    public static Select<? extends AUnit> from(Units units) {
        return new Select<>(units.list());
    }

    /**
     * Create initial search-pool of units from given collection of units.
     */
//    public static Select<AFoggedUnit> fromData(Collection<AFoggedUnit> units) {
//        return new Select<AFoggedUnit>(units);
//    }

    /**
     * Returns all units that are closer than <b>maxDist</b> tiles from given <b>otherUnit</b>.
     */
    public Select<? extends AUnit> inRadius(double maxDist, AUnit unit) {
        return cache.get(
                "inRadius:" + maxDist + ":" + unit.id(),
                0,
                () -> {
                    data.removeIf(u -> u.distTo(unit) > maxDist);
                    return this;
                }
        );
    }

    /**
     * Returns all units that are closer than <b>maxDist</b> tiles from given <b>position</b>.
     */
    public Select<? extends AUnit> inRadius(double maxDist, Position position) {
//        return cache.get(
//                "inRadius:" + maxDist + ":" + position.toString(),
//                0,
//                () -> {
                    Iterator<T> unitsIterator = data.iterator();// units.iterator();
                    while (unitsIterator.hasNext()) {
                        AUnit unit = (AUnit) unitsIterator.next();
                        if (unit.distTo(position) > maxDist) {
                            unitsIterator.remove();
                        }
                    }

                    return this;
//                }
//        );
    }

    // =====================================================================
    // Filter units
    /**
     * Selects only units of given type(s).
     */
    public Select<? extends AUnit> ofType(AUnitType... types) {
        data.removeIf(unit -> !typeMatches(unit, types));
        return this;
    }

    /**
     * Returns whether the type in unit matches one in the haystack
     */
    private boolean typeMatches(AUnit unit, AUnitType... haystack) {
        for (AUnitType type : haystack) {
            if (unit.is(type) || (unit.is(AUnitType.Zerg_Egg) && unit.getBuildType().equals(type))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether the type in unit matches one in the haystack
     */
//    private boolean typeMatches(AFoggedUnit unit, AUnitType... haystack) {
//        for (AUnitType type : haystack) {
//            if (unit.type().equals(type) || (unit.type().equals(AUnitType.Zerg_Egg) && unit.type().equals(type))) {
//                return true;
//            }
//        }
//        return false;
//    }

    /**
     * Selects only units of given type(s).
     */
    public int countUnitsOfType(AUnitType... types) {
        int total = 0;
        for (T unit : data) {
            boolean typeMatches = false;
            for (AUnitType type : types) {
                if (unit.is(type) || (unit.is(AUnitType.Zerg_Egg) && unit.getBuildType().equals(type))) {
                    typeMatches = true;
                    break;
                }
            }
            if (typeMatches) {
                total++;
            }
        }

        return total;
    }

    /**
     * Selects only those units which are VISIBLE ON MAP (not behind fog of war).
     */
    public Select<? extends AUnit> visible() {
        data.removeIf(unit -> !unit.isVisibleOnMap());
        return this;
    }

    /**
     * Selects only those units which are visible and not cloaked.
     */
    public Select<? extends AUnit> effVisible() {
        data.removeIf(unit -> unit.effCloaked() || !unit.isVisibleOnMap());
        return this;
    }

    /**
     * Selects only those units which are hidden, cloaked / burrowed. Not possible to be attacked.
     */
    public Select<? extends AUnit> effCloaked() {
        data.removeIf(unit -> !unit.effCloaked());
        return this;
    }

    public Select<? extends AUnit> cloakedButEffVisible() {
        data.removeIf(unit -> !unit.isCloaked() || (unit.isCloaked() && !unit.effCloaked()));
        return this;
    }

    public Select<? extends AUnit> detectors() {
        data.removeIf(unit -> !unit.isType(
                AUnitType.Protoss_Photon_Cannon,
                AUnitType.Protoss_Observer,
                AUnitType.Terran_Missile_Turret,
                AUnitType.Terran_Science_Vessel,
                AUnitType.Zerg_Overlord,
                AUnitType.Zerg_Spore_Colony
        ));
        return this;
    }

    public Select<? extends AUnit> tanksSieged() {
        data.removeIf(unit -> !unit.isType(AUnitType.Terran_Siege_Tank_Siege_Mode));
        return this;
    }

    public Select<? extends AUnit> tanks() {
        data.removeIf(unit -> !unit.isType(
                AUnitType.Terran_Siege_Tank_Siege_Mode,
                AUnitType.Terran_Siege_Tank_Tank_Mode
        ));
        return this;
    }

    public Select<? extends AUnit> groundUnits() {
        data.removeIf(unit -> !unit.isGroundUnit());
        return this;
    }

    /**
     * Selects only those units which are idle. Idle is unit's class flag so be careful with that.
     */
    public Select<? extends AUnit> idle() {
        data.removeIf(unit -> !unit.isIdle());
        return this;
    }

    /**
     * Selects units that are gathering minerals.
     */
    public Select<? extends AUnit> gatheringMinerals(boolean onlyNotCarryingMinerals) {
        data.removeIf(unit -> !unit.isGatheringMinerals() || (onlyNotCarryingMinerals && unit.isCarryingMinerals()));
        return this;
    }

    /**
     * Selects units being infantry.
     */
    public Select<? extends AUnit> organic() {
        data.removeIf(unit -> !unit.type().isOrganic());
        return this;
    }

    public Select<? extends AUnit> transports(boolean excludeOverlords) {
        data.removeIf(unit -> !unit.type().isTransport() || (excludeOverlords && !unit.type().isTransportExcludeOverlords()));
        return this;
    }

    /**
     * Selects bases only (including Lairs and Hives).
     */
    public Select<? extends AUnit> bases() {
        data.removeIf(unit -> !unit.type().isBase());
        return this;
    }

    public Select<? extends AUnit> workers() {
        data.removeIf(unit -> !unit.isWorker());
        return this;
    }

    /**
     * Selects melee units that is units which have attack range at most 1 tile.
     */
    public Select<? extends AUnit> melee() {
        data.removeIf(unit -> !unit.type().isMeleeUnit());
        return this;
    }

    /**
     * Selects only units that do not currently have max hit points.
     */
    public Select<? extends AUnit> wounded() {
        data.removeIf(unit -> !unit.isWounded());
        return this;
    }

    /**
     * Selects only units that do not currently have max hit points.
     */
    public Select<? extends AUnit> ranged() {
        data.removeIf(unit -> !unit.isRanged());
        return this;
    }

    /**
     * Selects only buildings.
     */
    public Select<? extends AUnit> buildings() {
        data.removeIf(unit -> !unit.type().isBuilding() && !unit.type().isAddon());
        return this;
    }

    public Select<? extends AUnit> combatUnits() {
        data.removeIf(unit -> !unit.isCompleted() || !unit.type().isCombatUnit());
        return this;
    }

    /**
     * Selects military buildings like Photon Cannon, Bunker, Spore Colony, Sunken Colony
     */
    public Select<? extends AUnit> combatBuildings() {
        data.removeIf(unit -> !unit.isBuilding() || !unit.type().isCombatBuilding());
        return this;
    }

    /**
     * Selects only those Terran vehicles/buildings that can be repaired so it has to be:<br />
     * - mechanical<br />
     * - not 100% healthy<br />
     */
    public Select<? extends AUnit> repairable(boolean checkIfWounded) {
        data.removeIf(unit -> !unit.isCompleted() || !unit.type().isMechanical() || (checkIfWounded && !unit.isWounded()));
        return this;
    }

    public Select<? extends AUnit> burrowed() {
        data.removeIf(unit -> !unit.isBurrowed());
        return this;
    }

    public Select<? extends AUnit> loaded() {
        data.removeIf(unit -> !unit.isLoaded());
        return this;
    }

    public Select<? extends AUnit> unloaded() {
        data.removeIf(AUnit::isLoaded);
        return this;
    }

    /**
     * Selects these units (makes sense only for workers) who aren't assigned to repair any other unit.
     */
    public Select<? extends AUnit> notRepairing() {
        data.removeIf(unit -> unit.isRepairing() || ARepairAssignments.isRepairerOfAnyKind(unit));
        return this;
    }

    /**
     * Selects these transport/bunker units which have still enough room inside.
     */
    public Select<? extends AUnit> havingSpaceFree(int spaceRequired) {
        data.removeIf(unit -> unit.spaceRemaining() < spaceRequired);
        return this;
    }

    /**
     * Selects these units (makes sense only for workers) who aren't assigned to construct anything.
     */
    public Select<? extends AUnit> notConstructing() {
        data.removeIf(unit -> unit.isConstructing() || unit.isMorphing() || unit.isBuilder());
        return this;
    }

    public Select<? extends AUnit> free() {
        data.removeIf(AUnit::isBusy);
        return this;
    }

    /**
     * Selects these units which are not scouts.
     */
    public Select<? extends AUnit> notScout() {
        data.removeIf(AUnit::isScout);
        return this;
    }

    /**
     * Selects these units which are not carrynig nor minerals, nor gas.
     */
    public Select<? extends AUnit> notCarrying() {
        data.removeIf(unit -> unit.isCarryingGas() || unit.isCarryingMinerals());
        return this;
    }

    public Select<? extends AUnit> canShootAt(AUnit targetUnit) {
        data.removeIf(unit -> !unit.isCompleted() || !unit.isAlive() || !unit.hasWeaponRange(targetUnit, 0));
        return this;
    }

    public Select<? extends AUnit> canShootAt(AUnit targetUnit, double shootingRangeBonus) {
        data.removeIf(unit -> !unit.isCompleted() || !unit.isAlive() || !unit.hasWeaponRange(targetUnit, shootingRangeBonus));
        return this;
    }

    public Select<? extends AUnit> canShootAt(APosition position, double shootingRangeBonus) {
        data.removeIf(unit -> !unit.isCompleted() || !unit.isAlive() || !unit.hasGroundWeaponRange(position, shootingRangeBonus));
        return this;
    }

    /**
     * Selects only those units from current selection, which can be both <b>attacked by</b> given unit (e.g.
     * Zerglings can't attack Overlord) and are <b>in shot range</b> to the given <b>unit</b>.
     */
    public Select<? extends AUnit> canBeAttackedBy(AUnit attacker, boolean checkShootingRange, boolean checkVisibility) {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AUnit target = unitsIterator.next();

            if (!attacker.hasWeaponToAttackThisUnit(target) || (checkVisibility && target.effCloaked())) {
                unitsIterator.remove();
            }
            else if (checkShootingRange && !attacker.hasWeaponRange(target, 0)) {
                unitsIterator.remove();
            }
        }
        return this;
    }

    public Select<? extends AUnit> canAttack(AUnit defender, boolean checkShootingRange, boolean checkVisibility) {
        data.removeIf(attacker ->
                !attacker.canAttackThisUnit(defender, checkShootingRange, checkVisibility)
                || (checkShootingRange && !attacker.hasWeaponRange(defender, 0))
        );
        return this;
    }

    public Select<? extends AUnit> inShootRangeOf(AUnit attacker) {
        return canBeAttackedBy(attacker, true, false);
    }

    public Select<? extends AUnit> inShootRangeOf(double shootingRangeBonus, AUnit attacker) {
        return canShootAt(attacker, shootingRangeBonus);
    }

//    public Select<? extends AUnit> inShootRangeOrAtMostTilesAway(double shootingRangeBonus, AUnit attacker) {
//    }

    // =========================================================
    // Hi-level auxiliary methods
    /**
     * Selects all of our bases.
     */
    public static Select<AUnit> ourBases() {
        if (AGame.isPlayingAsZerg()) {
            return (Select<AUnit>) ourBuildings().ofType(
                    AUnitType.Zerg_Hatchery, AUnitType.Zerg_Lair,
                    AUnitType.Zerg_Hive, AUnitType.Protoss_Nexus, AUnitType.Terran_Command_Center
            );
        } else {
            return (Select<AUnit>) our().ofType(AtlantisConfig.BASE);
        }
    }

    /**
     * Selects our workers (that is of type Terran SCV or Zerg Drone or Protoss Probe).
     */
    public static Select<AUnit> ourWorkers() {
        return (Select<AUnit>) cache.get(
                "ourWorkers",
                1,
                () -> {
                    List<AUnit> data = new ArrayList<>();

                    for (AUnit unit : ourUnits()) {
                        if (unit.isCompleted() && unit.isWorker()) {
                            data.add(unit);
                        }
                    }

                    return new Select<>(data);
                }
        );
//        Select<? extends AUnit> selectedUnits = Select.our();
//        selectedUnits.list().removeIf(unit -> !unit.isWorker());
//        return (Select<AUnit>) selectedUnits;
    }

    /**
     * Selects our workers (that is of type Terran SCV or Zerg Drone or Protoss Probe) that are either
     * gathering minerals or gas.
     */
    public static Select<AUnit> ourWorkersThatGather(boolean onlyNotCarryingAnything) {
        Select<? extends AUnit> selectedUnits = Select.our();
        selectedUnits.list().removeIf(unit ->
                !unit.isWorker() || (!unit.isGatheringGas() && !unit.isGatheringMinerals())
                || (onlyNotCarryingAnything && (unit.isCarryingGas() || unit.isCarryingMinerals()))
        );
        return (Select<AUnit>) selectedUnits;
    }

    /**
     * Selects our workers that are free to construct building or repair a unit. That means they mustn't
     * repait any other unit or construct other building.
     */
    public static Select<AUnit> ourWorkersFreeToBuildOrRepair() {
        Select<? extends AUnit> selectedUnits = Select.ourWorkers();
        selectedUnits.list().removeIf(unit ->
                unit.isConstructing() || unit.isRepairing() || AConstructionManager.isBuilder(unit)
                || AScoutManager.isScout(unit) || unit.isRepairerOfAnyKind()
        );

        return (Select<AUnit>) selectedUnits;
    }

    /**
     * Selects all our finished buildings.
     */
    public static Select<AUnit> ourBuildings() {
        return (Select<AUnit>) our().buildings();
    }

    /**
     * Selects all our buildings including those unfinished.
     */
    public static Select<AUnit> ourBuildingsIncludingUnfinished() {
        Select<? extends AUnit> selectedUnits = Select.ourIncludingUnfinished();
        selectedUnits.list().removeIf(unit -> !unit.type().isBuilding() && !unit.type().isAddon());
        return (Select<AUnit>) selectedUnits;
    }

    /**
     * Selects all our tanks, both sieged and unsieged.
     */
    public static Select<AUnit> ourTanks() {
        return (Select<AUnit>) our().ofType(AUnitType.Terran_Siege_Tank_Siege_Mode, AUnitType.Terran_Siege_Tank_Tank_Mode);
    }

    /**
     * Selects all our sieged tanks.
     */
    public static Select<AUnit> ourTanksSieged() {
        return (Select<AUnit>) our().ofType(AUnitType.Terran_Siege_Tank_Siege_Mode);
    }

    /**
     * Selects all of our Marines, Firebats, Ghosts and Medics.
     */
    public static Select<AUnit> ourTerranInfantry() {
        return (Select<AUnit>) our().ofType(AUnitType.Terran_Marine, AUnitType.Terran_Medic, AUnitType.Terran_Firebat, AUnitType.Terran_Ghost);
    }

    /**
     * Selects all of our Marines, Firebats, Ghosts.
     */
    public static Select<AUnit> ourTerranInfantryWithoutMedics() {
        return (Select<AUnit>) our().ofType(AUnitType.Terran_Marine, AUnitType.Terran_Firebat, AUnitType.Terran_Ghost);
    }

    /**
     * Selects all of our Zerg Larvas.
     */
    public static Select<AUnit> ourLarva() {
        Select<? extends AUnit> selectedUnits = ourIncludingUnfinished();
        selectedUnits.list().removeIf(unit -> !unit.is(AUnitType.Zerg_Larva));
        return (Select<AUnit>) selectedUnits;
    }

    /**
     * Counts all of our Zerg Larvas.
     */
//    public static int countOurLarva() {
//        return Select.ourOfType(AUnitType.Zerg_Larva).count();
//    }

    /**
     * Selects all of our Zerg Eggs.
     */
//    public static Select<AUnit> ourEggs() {
//        Select<? extends AUnit> selectedUnits = Select.ourIncludingUnfinished();
//        selectedUnits.list().removeIf(unit -> !unit.is(AUnitType.Zerg_Egg));
//        return selectedUnits;
//    }

    // =========================================================
    // Localization-related methods
    
    /**
     * From all units currently in selection, returns closest unit to given <b>position</b>.
     */
    public AUnit nearestTo(HasPosition position) {
        if (data.isEmpty() || position == null) {
            return null;
        }

        sortDataByDistanceTo(position, true);

        return data.isEmpty() ? null : (AUnit) data.get(0);
    }

    public AUnit mostDistantTo(HasPosition position) {
        if (data.isEmpty() || position == null) {
            return null;
        }

        sortDataByDistanceTo(position, false);

        return data.isEmpty() ? null : (AUnit) data.get(0);
    }

    /**
     * From all units currently in selection, returns closest unit to given <b>position</b>.
     */
    public AUnit nearestToOrNull(HasPosition position, double maxLength) {
        if (data.isEmpty() || position == null) {
            return null;
        }

//        Position position;
//        if (positionOrUnit instanceof APosition) {
//            position = (APosition) positionOrUnit;
//        } else if (positionOrUnit instanceof Position) {
//            position = (Position) positionOrUnit;
//        } else {
//            position = ((AUnit) positionOrUnit).getPosition();
//        }

        sortDataByDistanceTo(position, true);
        AUnit nearestUnit = (AUnit) data.get(0);
        
        if (nearestUnit != null && nearestUnit.distTo(position) < maxLength) {
            return nearestUnit;
        }
        else {
            return null;
        }
    }

    public AUnit mostWounded() {
        if (data.isEmpty()) {
            return null;
        }

        sortByHealth();

        return (AUnit) data.get(0);
    }

    /**
     * Returns first unit being base. For your units this is most likely your main base, for enemy it will be
     * first discovered base.
     */
    public static AUnit mainBase() {
        if (_cached_mainBase == null || !_cached_mainBase.isAlive()) {
            List<AUnit> bases = ourBases().list();
            _cached_mainBase = bases.isEmpty() ? Select.ourBuildings().first() : bases.get(0);
        }
        return _cached_mainBase;
    }

    /**
     * Returns second (natural) base <b>or if we have only one base</b>, it returns the only base we have.
     */
    public static AUnit secondBaseOrMainIfNoSecond() {
        List<? extends AUnit> bases = Select.ourBases().list();

        int counter = 0;
        for (AUnit base : bases) {
            if (bases.size() <= 1) {
                return base;
            } else if (counter > 0) {
                return base;
            }

            counter++;
        }

        return null;
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

    // =========================================================
    // Special retrieve
    /**
     * Returns <b>true</b> if current selection contains at least one unit.
     */
    public boolean anyExists() {
        return !data.isEmpty();
    }

    /**
     * Returns first unit that matches previous conditions or null if no units match conditions.
     */
    public AUnit first() {
        return data.isEmpty() ? null : (AUnit) data.get(0);
    }

    public AUnit randomWithSeed(int seed) {
        Random rand = new Random(seed);
        return data.isEmpty() ? null : (AUnit) data.get(rand.nextInt(data.size()));
    }

    /**
     * Returns first unit that matches previous conditions or null if no units match conditions.
     */
    public AUnit last() {
        return data.isEmpty() ? null : (AUnit) data.get(data.size() - 1);
    }

    /**
     * Returns random unit that matches previous conditions or null if no units matched all conditions.
     */
    public AUnit random() {
        return (AUnit) A.getRandomElement(data);
    }

    // === High-level of abstraction ===========================
    
    public AUnit lowestHealth() {
        int lowestHealth = 99999;
        AUnit lowestHealthUnit = null;
        
        for (Iterator<T> it = data.iterator(); it.hasNext();) {
            AUnit unit = (AUnit) it.next();
            if (unit.hp() < lowestHealth) {
                lowestHealth = unit.hp();
                lowestHealthUnit = unit;
            }
        }

        return lowestHealthUnit;
    }
    
    public boolean areAllBusy() {
        for (Iterator<AUnit> it = (Iterator<AUnit>) data.iterator(); it.hasNext();) {
            AUnit unit = it.next();
            if (!unit.isBusy()) {
                return false;
            }
        }

        return true;
    }

    // === Operations on set of units ==========================
    /**
     * @return all units except for the given one
     */
    public Select<? extends AUnit> exclude(AUnit unitToExclude) {
        data.remove(unitToExclude);
        return this;
    }

    public Select<? extends AUnit> exclude(Collection<AUnit> unitsToExclude) {
        data.removeAll(unitsToExclude);
        return this;
    }

    /**
     * Reverse the order in which units are returned.
     */
    public Select<? extends AUnit> reverse() {
        Collections.reverse(data);
        return this;
    }

    /**
     * Returns a AUnit out of an entity that is either a AUnit or AFoggedUnit
     *
     * @param unitOrData
     * @return
     */
//    private AUnit unitFrom(Object unitOrData) {
//        return (unitOrData instanceof AUnit ? (AUnit) unitOrData : ((AFoggedUnit) unitOrData).getUnit());
//    }

    /**
     * Returns a UnitData out of an entity that is either a AUnit or UnitData
     *
     * @param unitOrData
     * @return
     */
    private T dataFrom(Object unitOrData) {
//        return (unitOrData instanceof AFoggedUnit ? (AFoggedUnit) unitOrData : new AFoggedUnit((AUnit) unitOrData));
//        if (unitOrData instanceof AFoggedUnit) {
//            return (T) unitOrData;
//        }
//        else
        if (unitOrData instanceof AUnit) {
            return (T) unitOrData;
        }

        throw new RuntimeException("Invalid dataFrom type");
    }

    @SuppressWarnings("unused")
    private Select<? extends AUnit> filterOut(Collection<T> unitsToRemove) {
        data.removeAll(unitsToRemove);
        return this;
    }

    @SuppressWarnings("unused")
    private Select<? extends AUnit> filterAllBut(T unitToLeave) {
        for (AUnit unit : data) {
            if (unitToLeave != unit) {
                data.remove(unit);
            }
        }
        return this;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("Units (" + data.size() + "):\n");

        for (AUnit unit : data) {
            string.append("   - ").append(unit.type()).append(" (ID:").append(unit.getID()).append(")\n");
        }

        return string.toString();
    }

    // =========================================================
    // Get results
    /**
     * Selects result as an iterable collection (list).
     */
    public List<AUnit> list() {
        return (List<AUnit>) data;
    }

    /**
     * Selects units as an iterable collection (list).
     */
    public List<AUnit> listUnits() {
        return (List<AUnit>) data;
    }

    /**
     * Returns result as an <b>Units</b> object, which contains multiple useful methods to handle set of
     * units.
     */
    public Units units() {
        Units units = new Units(this.data);
//        units.addUnits((Collection<AUnit>) this.data);
        return units;
    }

    /**
     * Returns number of units matching all previous conditions.
     */
    public int count() {
        return data.size();
    }

    public boolean atLeast(int min) {
        return data.size() >= min;
    }

    public boolean atMost(int max) {
        return data.size() <= max;
    }

    /**
     * Returns true if there're no units that fullfilled all previous conditions.
     */
    public boolean isEmpty() {
        return data.size() == 0;
    }

    public boolean isNotEmpty() {
        return data.size() > 0;
    }

    /**
     * Returns number of units matching all previous conditions.
     */
    public int size() {
        return data.size();
    }

    /**
     * Sorts data list by distance to a given position
     *
     * @param position
     * @param nearestFirst
     * @return
     */
    public List<T> sortDataByDistanceTo(final HasPosition position, final boolean nearestFirst) {
//        if (position == null) {
//            return null;
//        }

        Collections.sort(data, new Comparator<T>() {
            @Override
            public int compare(T p1, T p2) {
                if (!(p1 instanceof HasPosition)) {
                    throw new RuntimeException("Invalid comparison: " + p1);
                }
                if (!(p2 instanceof HasPosition)) {
                    throw new RuntimeException("Invalid comparison: " + p2);
                }
//                AFoggedUnit data1 = dataFrom(p1);
//                AFoggedUnit data2 = dataFrom(p2);
//                double distance1 = PositionUtil.distanceTo(position, data1.getPosition());
//                double distance2 = PositionUtil.distanceTo(position, data2.getPosition());
                double distance1 = PositionUtil.distanceTo(position, p1);
                double distance2 = PositionUtil.distanceTo(position, p2);
                if (distance1 == distance2) {
                    return 0;
                } else {
                    return distance1 < distance2 ? (nearestFirst ? -1 : 1) : (nearestFirst ? 1 : -1);
                }
            }
        });

        return data;
    }

    public List<T> sortDataByDistanceTo(final AUnit unit, final boolean nearestFirst) {
        Collections.sort(data, new Comparator<T>() {
            @Override
            public int compare(T p1, T p2) {
                if (!(p1 instanceof HasPosition)) {
                    throw new RuntimeException("Invalid comparison: " + p1);
                }
                if (!(p2 instanceof HasPosition)) {
                    throw new RuntimeException("Invalid comparison: " + p2);
                }
//                AFoggedUnit data1 = dataFrom(p1);
//                AFoggedUnit data2 = dataFrom(p2);
//                double distance1 = PositionUtil.distanceTo(position, data1.getPosition());
//                double distance2 = PositionUtil.distanceTo(position, data2.getPosition());
                double distance1 = PositionUtil.distanceTo(unit, p1);
                double distance2 = PositionUtil.distanceTo(unit, p2);
                if (distance1 == distance2) {
                    return 0;
                } else {
                    return distance1 < distance2 ? (nearestFirst ? -1 : 1) : (nearestFirst ? 1 : -1);
                }
            }
        });

        return data;
    }

    public List<T> sortByHealth() {
        if (data.isEmpty()) {
            return null;
        }

        Collections.sort(data, Comparator.comparingDouble(u -> ((AUnit) u).hpPercent()));

//        if (data.size() > 1) {
//            System.out.println("data = ");
//            for (T unit :
//                    data) {
//                System.out.println(((AUnit) unit).shortName() + " - " + ((AUnit) unit).getHPPercent());
//            }
//        }

        return data;
    }

    public Select<? extends AUnit> clone() {
        return new Select<>(this.data);
    }

}
