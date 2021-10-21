package atlantis.units;

import atlantis.AGame;
import atlantis.Atlantis;
import atlantis.AtlantisConfig;
import atlantis.constructing.AConstructionManager;
import atlantis.information.AFoggedUnit;
import atlantis.position.APosition;
import atlantis.position.HasPosition;
import atlantis.repair.ARepairAssignments;
import atlantis.scout.AScoutManager;
import atlantis.util.A;
import atlantis.position.PositionUtil;
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
public class Select<T> {

    // =====================================================================
    // Collection<AUnit> wrapper with extra methods
    //private AUnits units;
    private final List<T> data;

    // CACHED variables
    private static AUnit _cached_mainBase = null;

    // =====================================================================
    // Constructor is private, use our(), enemy() or neutral() methods

    protected Select(Collection<T> unitsData) {
        data = new ArrayList<>();
        data.addAll(unitsData);
    }

    // =====================================================================
    // Helper for base object

    private static List<AUnit> ourUnits() {
        List<AUnit> data = new ArrayList<>();

        for (Unit u : AGame.getPlayerUs().getUnits()) {
//            AUnit unit = AUnit.createFrom(u);
//            if (unit.isCompleted()) {
//                data.add(unit);
//            }
            data.add(AUnit.createFrom(u));
        }

        return data;
    }

    private static List<AUnit> enemyUnits() {
        List<AUnit> data = new ArrayList<>();

        // === Handle UMS ==========================================

        if (AGame.isUms()) {
            Player playerUs = AGame.getPlayerUs();
            for (Player player : AGame.getPlayers()) {
                if (player.isEnemy(playerUs)) {
                    for (Unit u : player.getUnits()) {
                        AUnit unit = AUnit.createFrom(u);
                        data.add(unit);
                    }
                }
            }
        }

        // =========================================================

        else {
            for (Unit u : AGame.getEnemy().getUnits()) {
                AUnit unit = AUnit.createFrom(u);
                data.add(unit);
            }
        }

        return data;
    }

    private static List<AUnit> neutralUnits() {
        List<AUnit> data = new ArrayList<>();

        for (Unit u : Atlantis.game().neutral().getUnits()) {
            data.add(AUnit.createFrom(u));
        }

        return data;
    }

    private static List<AUnit> allUnits() {
        List<AUnit> data = new ArrayList<>();

        for (Unit u : Atlantis.game().getAllUnits()) {
            data.add(AUnit.createFrom(u));
        }

        return data;
    }

    // =====================================================================
    // Create base object
    /**
     * Selects all of our finished and existing units (units, buildings, but no spider mines etc).
     */
    public static Select<AUnit> our() {
        //Units units = new Units();
        List<AUnit> data = new ArrayList<>();

        for (AUnit unit : ourUnits()) {
            if (unit.isCompleted() && !unit.isUncontrollable()) {
                data.add(unit);	//TODO: make it more efficient by just querying the cache of known units
            }
        }

        return new Select<AUnit>(data);
    }

    /**
     * Selects all game units including minerals, geysers and enemy units.
     */
    public static Select<AUnit> all() {
        //Units units = new Units();
        List<AUnit> data = new ArrayList<>();

        data.addAll(ourUnits());
        data.addAll(enemyUnits());
        data.addAll(neutralUnits());

        return new Select<AUnit>(data);
    }

    /**
     * Selects all units of given type(s).
     */
    public static Select<AUnit> allOfType(AUnitType type) {
        List<AUnit> data = new ArrayList<>();

        for (AUnit unit : allUnits()) {
            if (unit.isCompleted() && unit.isType(type)) {
                data.add(unit);
            }
        }

        return new Select<AUnit>(data);
    }

    /**
     * Selects enemy units of given type(s).
     */
    public static Select<AUnit> enemies(AUnitType... type) {
        List<AUnit> data = new ArrayList<>();

        for (AUnit unit : enemyUnits()) {
            if (unit.isType(type)) {
                data.add(unit);
            }
        }

        return new Select<AUnit>(data);
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

        return new Select<AUnit>(data);
    }

    /**
     * Selects our units of given type(s).
     */
    public static Select<AUnit> ourUnfinishedOfType(AUnitType... type) {
        List<AUnit> data = new ArrayList<>();

        for (AUnit unit : ourUnits()) {
            if (!unit.isCompleted() && unit.isType(type)) {
                data.add(unit);
            }
        }

        return new Select<AUnit>(data);
    }

    /**
     * Counts our completed units of given type.
     */
    public static int countOurOfType(AUnitType... type) {
        int total = 0;

        for (AUnit unit : ourUnits()) {
            if (unit.isCompleted() && unit.isType(type)) {
                total++;
            }
        }

        return total;
    }

    /**
     * Counts our units of given type.
     */
    public static int countOurOfTypeIncludingUnfinished(AUnitType type) {
        int total = 0;

        for (AUnit unit : ourUnits()) {
            if (unit.isType(type)) {
                total++;
            }
        }

        return total;
    }

    /**
     * Selects our units of given type(s).
     */
    public static Select<AUnit> ourOfTypeIncludingUnfinished(AUnitType type) {
        List<AUnit> data = new ArrayList<>();

        for (AUnit unit : ourUnits()) {
            if (unit.isType(type)) {
                data.add(unit);
            }
        }

        return new Select<AUnit>(data);
    }

    /**
     * Selects all of our finished combat units (no buildings, workers, spider mines etc).
     */
    public static Select<AUnit> ourCombatUnits() {
        //Units units = new AUnits();
        List<AUnit> data = new ArrayList<>();

        for (AUnit unit : ourUnits()) {
            if (unit.isCompleted() && unit.isActualUnit() && !unit.isWorker()) {
                data.add(unit);	//TODO: make it more efficient by just querying the cache of known units
            }
        }

        return new Select<AUnit>(data);
    }

    /**
     * Selects all of our units (units, buildings, but no spider mines etc), <b>even those unfinished</b>.
     */
    public static Select<AUnit> ourIncludingUnfinished() {
        //Units units = new AUnits();
        List<AUnit> data = new ArrayList<>();

        for (AUnit unit : ourUnits()) {
            if (!unit.isUncontrollable()) {
                data.add(unit);	//TODO: make it more efficient by just querying the cache of known units
            }
        }

        return new Select<AUnit>(data);
    }

    /**
     * Selects our unfinished units.
     */
    public static Select<AUnit> ourUnfinished() {
        List<AUnit> data = new ArrayList<>();

        for (AUnit unit : ourUnits()) {
            if (!unit.isCompleted()) {
                data.add(unit);
            }
        }

        return new Select<AUnit>(data);
    }

    /**
     * Selects our units, not buildings, not spider mines, not larvae.
     */
    public static Select<AUnit> ourRealUnits() {
        List<AUnit> data = new ArrayList<>();

        for (AUnit unit : ourUnits()) {
            if (unit.isCompleted() && unit.isActualUnit()) {
                data.add(unit);
            }
        }

        return new Select<AUnit>(data);
    }

    /**
     * Selects our unfinished units.
     */
    public static Select<AUnit> ourUnfinishedRealUnits() {
        List<AUnit> data = new ArrayList<>();

        for (AUnit unit : ourUnits()) {

            if (!unit.isCompleted() && unit.isActualUnit()) {
                data.add(unit);
            }
        }

        return new Select<AUnit>(data);
    }

    /**
     * Selects all visible enemy units. Since they're visible, the parameterized type is AUnit
     */
    public static Select<AUnit> enemy() {
        List<AUnit> data = new ArrayList<>();

        //TODO: check whether enemy().getUnits() has the same behavior as getEnemyUnits()
        for (AUnit unit : enemyUnits()) {
            data.add(unit);
        }

        return new Select<AUnit>(data);
    }

    /**
     * Selects all visible enemy units. Since they're visible, the parameterized type is AUnit
     */
    public static Select<AUnit> enemy(boolean includeGroundUnits, boolean includeAirUnits) {
        List<AUnit> data = new ArrayList<>();

        for (AUnit unit : enemyUnits()) {
            if ((!unit.isAirUnit() && includeGroundUnits) || (unit.isAirUnit() && includeAirUnits)) {
                data.add(unit);
            }
        }

        return new Select<AUnit>(data);
    }

    /**
     * Selects all visible enemy units. Since they're visible, the parameterized type is AUnit
     */
    public static Select<AUnit> enemyCombatUnits() {
        List<AUnit> data = new ArrayList<>();

        for (AUnit unit : enemyUnits()) {
            if (!unit.isWorker() && unit.isActualUnit()) {
                data.add(unit);
            }
        }

        return new Select<AUnit>(data);
    }

    /**
     * Selects all visible enemy units. Since they're visible, the parameterized type is AUnit
     */
    public static Select<AUnit> enemyRealUnits() {
        return enemyRealUnits(true, true, false);
    }

    /**
     * Selects all visible enemy units. Since they're visible, the parameterized type is AUnit
     */
    public static Select<AUnit> enemyRealUnits(boolean includeBuildings) {
        return enemyRealUnits(true, true, includeBuildings);
    }

    /**
     * Selects all visible enemy units. Since they're visible, the parameterized type is AUnit
     */
    public static Select<AUnit> enemyRealUnits(
            boolean includeGroundUnits, boolean includeAirUnits, boolean includeBuildings
    ) {
        List<AUnit> data = new ArrayList<>();

        for (AUnit unit : enemyUnits()) {
            if ((includeBuildings || !unit.isBuilding()) || unit.isActualUnit()) {
                if ((includeGroundUnits && unit.isGroundUnit()) || (includeAirUnits && unit.isAirUnit())) {
                    data.add(unit);
                }
            }
        }

        return new Select<AUnit>(data);
    }

    /**
     * Selects all visible neutral units (minerals, geysers, critters). Since they're visible, the
     * parameterized type is AUnit
     */
    public static Select<AUnit> neutral() {
        return new Select<AUnit>(neutralUnits());
    }

    /**
     * Selects all (accessible) minerals on the map.
     */
    public static Select<AUnit> minerals() {
        return (Select<AUnit>) neutral().ofType(
                AUnitType.Resource_Mineral_Field,
                AUnitType.Resource_Mineral_Field_Type_2,
                AUnitType.Resource_Mineral_Field_Type_3
        );
    }

    /**
     * Selects all geysers on the map.
     */
    public static Select<AUnit> geysers() {
        return (Select<AUnit>) neutral().ofType(AUnitType.Resource_Vespene_Geyser);
    }

    public static Select<AUnit> geyserBuildings() {
        return (Select<AUnit>) neutral().ofType(
                AUnitType.Protoss_Assimilator,
                AUnitType.Terran_Refinery,
                AUnitType.Zerg_Extractor
        );
    }

    /**
     * Create initial search-pool of units from given collection of units.
     */
    public static Select<AUnit> from(Collection<AUnit> units) {
        return new Select<AUnit>(units);
    }

    /**
     * Create initial search-pool of units from Units object.
     */
    public static Select<AUnit> from(Units units) {
        return new Select<AUnit>(units.list());
    }

    /**
     * Create initial search-pool of units from given collection of units.
     */
    public static Select<AFoggedUnit> fromData(Collection<AFoggedUnit> units) {
        return new Select<AFoggedUnit>(units);
    }

    /**
     * Returns all units that are closer than <b>maxDist</b> tiles from given <b>otherUnit</b>.
     */
    public Select<T> inRadius(double maxDist, AUnit otherUnit) {
        Iterator<T> unitsIterator = data.iterator();// units.iterator();
        while (unitsIterator.hasNext()) {
            AUnit unit = (AUnit) unitsIterator.next();
            if (unit.distTo(otherUnit) > maxDist) {
                unitsIterator.remove();
            }
        }

        return this;
    }

    /**
     * Returns all units that are closer than <b>maxDist</b> tiles from given <b>position</b>.
     */
    public Select<?> inRadius(double maxDist, Position position) {
        Iterator<T> unitsIterator = data.iterator();// units.iterator();
        while (unitsIterator.hasNext()) {
            AUnit unit = (AUnit) unitsIterator.next();
            if (unit.distTo(position) > maxDist) {
                unitsIterator.remove();
            }
        }

        return this;
    }

    // =====================================================================
    // Filter units
    /**
     * Selects only units of given type(s).
     */
    public Select<?> ofType(AUnitType... types) {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            Object unitOrData = unitsIterator.next();
            boolean typeMatches = (unitOrData instanceof AUnit ? typeMatches((AUnit) unitOrData, types) : typeMatches((AFoggedUnit) unitOrData, types));
            if (!typeMatches) {
                unitsIterator.remove();
            }
        }

        return this;
    }

    /**
     * Returns whether the type in needle matches one in the haystack
     */
    private boolean typeMatches(AUnit needle, AUnitType... haystack) {
        AUnit unit = unitFrom(needle);

        for (AUnitType type : haystack) {
            if (unit.is(type)
                    || (unit.is(AUnitType.Zerg_Egg) && unit.getBuildType().equals(type))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether the type in needle matches one in the haystack
     *
     * @param needle
     * @param haystack
     * @return
     */
    private boolean typeMatches(AFoggedUnit needle, AUnitType... haystack) {

        for (AUnitType type : haystack) {
            if (needle.type().equals(type)
                    || (needle.type().equals(AUnitType.Zerg_Egg) && needle.getUnitType().equals(type))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Selects only units of given type(s).
     */
    public int countUnitsOfType(AUnitType... types) {
        int total = 0;
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AUnit unit = unitFrom(unitsIterator.next());
            boolean typeMatches = false;
            for (AUnitType type : types) {
                if (unit.is(type)
                        || (unit.is(AUnitType.Zerg_Egg) && unit.getBuildType().equals(type))) {
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
     * Selects only those units which are visible and not cloaked.
     */
    public Select<T> effVisible() {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AUnit unit = unitFrom(unitsIterator.next());	//TODO: will probably not work with enemy units
            if (unit.effCloaked()) {
                unitsIterator.remove();
            }
        }

        return this;
    }

    /**
     * Selects only those units which are hidden, cloaked / burrowed. Not possible to be attacked.
     */
    public Select<T> effCloaked() {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AUnit unit = unitFrom(unitsIterator.next());	//TODO: will probably not work with enemy units
            if (!unit.effCloaked()) {
                unitsIterator.remove();
            }
        }

        return this;
    }

    public Select<T> cloakedButEffVisible() {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AUnit unit = unitFrom(unitsIterator.next());	//TODO: will probably not work with enemy units
            if (!unit.isCloaked() || (unit.isCloaked() && !unit.effCloaked())) {
//                System.out.println(unit.shortName() + " // " + unit.getHitPoints());
                unitsIterator.remove();
            }
        }

        return this;
    }

    public Select<T> detectors() {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AUnit unit = unitFrom(unitsIterator.next());	//TODO: will probably not work with enemy units
            if (!unit.isType(
                    AUnitType.Protoss_Photon_Cannon,
                    AUnitType.Protoss_Observer,
                    AUnitType.Terran_Missile_Turret,
                    AUnitType.Terran_Science_Vessel,
                    AUnitType.Zerg_Overlord,
                    AUnitType.Zerg_Spore_Colony
            )) {
                unitsIterator.remove();
            }
        }

        return this;
    }

    public Select<T> tanksSieged() {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AUnit unit = unitFrom(unitsIterator.next());	//TODO: will probably not work with enemy units
            if (!unit.isType(AUnitType.Terran_Siege_Tank_Siege_Mode)) {
                unitsIterator.remove();
            }
        }

        return this;
    }

    public Select<T> tanks() {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AUnit unit = unitFrom(unitsIterator.next());	//TODO: will probably not work with enemy units
            if (!unit.isType(
                    AUnitType.Terran_Siege_Tank_Siege_Mode,
                    AUnitType.Terran_Siege_Tank_Tank_Mode
            )) {
                unitsIterator.remove();
            }
        }

        return this;
    }

    public Select<T> groundUnits() {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AUnit unit = unitFrom(unitsIterator.next());	//TODO: will probably not work with enemy units
            if (!unit.isGroundUnit()) {
                unitsIterator.remove();
            }
        }

        return this;
    }

    /**
     * Selects only those units which are idle. Idle is unit's class flag so be careful with that.
     */
    public Select<T> idle() {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AUnit unit = unitFrom(unitsIterator.next());	//TODO: will probably not work with enemy units
            if (!unit.isIdle()) {
                unitsIterator.remove();
            }
        }

        return this;
    }

    /**
     * Selects units that are gathering minerals.
     */
    public Select<T> gatheringMinerals(boolean onlyNotCarryingMinerals) {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AUnit unit = unitFrom(unitsIterator.next());	//TODO: will probably not work with enemy units
            if (!unit.isGatheringMinerals()) {
                if (onlyNotCarryingMinerals && !unit.isCarryingMinerals()) {
                    unitsIterator.remove();
                } else {
                    unitsIterator.remove();
                }
            }
        }

        return this;
    }

    /**
     * Selects units being infantry.
     */
    public Select<T> infantry() {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AFoggedUnit unit = dataFrom(unitsIterator.next());	//(unitOrData instanceof AUnit ? (AUnit) unitOrData : ((UnitData)unitOrData).getUnit()); 
            if (!unit.type().isOrganic()) { //replaced  isInfantry()
                unitsIterator.remove();
            }
        }

        return this;
    }

    /**
     * Selects bases only (including Lairs and Hives).
     */
    public Select<T> bases() {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AFoggedUnit unit = dataFrom(unitsIterator.next());
            if (!unit.type().isBase()) {
                unitsIterator.remove();
            }
        }

        return this;
    }

    public Select<T> workers() {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AFoggedUnit unit = dataFrom(unitsIterator.next());
            if (!unit.getUnit().isWorker()) {
                unitsIterator.remove();
            }
        }

        return this;
    }

    /**
     * Selects melee units that is units which have attack range at most 1 tile.
     */
    public Select<T> melee() {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AUnit unit = unitFrom(unitsIterator.next());
            if (!unit.type().isMeleeUnit()) {
                unitsIterator.remove();
            }
        }

        return this;
    }

    /**
     * Selects only units that do not currently have max hit points.
     */
    public Select<T> wounded() {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AUnit unit = unitFrom(unitsIterator.next());	//TODO: will work properly only on visible units
            if (unit.hp() >= unit.maxHp()) {
                unitsIterator.remove();
            }
        }

        return this;
    }

    /**
     * Selects only units that do not currently have max hit points.
     */
    public Select<T> ranged() {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AUnit unit = unitFrom(unitsIterator.next());
            if (!unit.melee()) {
                unitsIterator.remove();
            }
        }

        return this;
    }

    /**
     * Selects only buildings.
     */
    public Select<T> buildings() {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AFoggedUnit unit = dataFrom(unitsIterator.next());
            if (!unit.type().isBuilding() && !unit.type().isAddon()) {
                unitsIterator.remove();
            }
        }
        return this;
    }

    /**
     * Selects only units that can fight in any way including: - infantry including Terran Medics, but not
     * workers - military buildings like Photon Cannon, Bunker, Spore Colony, Sunken Colony
     */
    public Select<T> combatUnits() {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AFoggedUnit uData = dataFrom(unitsIterator.next());
            AUnit u = uData.getUnit();	//TODO: will work only on visible units...
            if (!u.isCompleted() || u.isWorker() || (uData.type().isBuilding() && !uData.type().isCombatBuilding())
                    || u.type().isInvincible() || u.type().isMine() || u.isType(AUnitType.Protoss_Observer)) {
                unitsIterator.remove();
            }
        }
        return this;
    }

    /**
     * Selects military buildings like Photon Cannon, Bunker, Spore Colony, Sunken Colony
     */
    public Select<T> combatBuildings() {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AUnit unit = unitFrom(unitsIterator.next());
            if (!unit.isBuilding() || !unit.type().isCombatBuilding()) {
                unitsIterator.remove();
            }
        }
        return this;
    }

    /**
     * Selects only those Terran vehicles/buildings that can be repaired so it has to be:<br />
     * - mechanical<br />
     * - not 100% healthy<br />
     */
    public Select<T> repairable(boolean checkIfWounded) {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AUnit unit = unitFrom(unitsIterator.next());
            if (!unit.isCompleted() || !unit.type().isMechanical() || (checkIfWounded && !unit.isWounded())) {
                unitsIterator.remove();
            }
        }
        return this;
    }

    public Select<T> burrowed() {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AUnit unit = unitFrom(unitsIterator.next());
            if (!unit.isBurrowed()) {
                unitsIterator.remove();
            }
        }
        return this;
    }

    /**
     * Selects these units (makes sense only for workers) who aren't assigned to repair any other unit.
     */
    public Select<T> notRepairing() {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AUnit unit = unitFrom(unitsIterator.next());
            if (unit.isRepairing() || ARepairAssignments.isRepairerOfAnyKind(unit)) {
                unitsIterator.remove();
            }
        }
        return this;
    }

    /**
     * Selects these transport/bunker units which have still enough room inside.
     */
    public Select<T> havingSpaceFree(int spaceRequired) {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AUnit unit = unitFrom(unitsIterator.next());
            if (unit.getSpaceRemaining() < spaceRequired) {
                unitsIterator.remove();
            }
        }
        return this;
    }

    /**
     * Selects these units (makes sense only for workers) who aren't assigned to construct anything.
     */
    public Select<T> notConstructing() {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AUnit unit = unitFrom(unitsIterator.next());
            if (unit.isConstructing() || unit.isMorphing() || unit.isBuilder()) {
                unitsIterator.remove();
            }
        }
        return this;
    }

    public Select<T> free() {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AUnit unit = unitFrom(unitsIterator.next());
            if (unit.isBusy()) {
                unitsIterator.remove();
            }
        }
        return this;
    }

    /**
     * Selects these units which are not scouts.
     */
    public Select<T> notScout() {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AUnit unit = unitFrom(unitsIterator.next());
            if (unit.isScout()) {
                unitsIterator.remove();
            }
        }
        return this;
    }

    /**
     * Selects these units which are not carrynig nor minerals, nor gas.
     */
    public Select<T> notCarrying() {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AUnit unit = unitFrom(unitsIterator.next());
            if (unit.isCarryingGas()|| unit.isCarryingMinerals()) {
                unitsIterator.remove();
            }
        }
        return this;
    }

    public Select<T> canShootAt(AUnit targetUnit) {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AUnit unit = unitFrom(unitsIterator.next());
            if (unit.isCompleted() && unit.isAlive()) {
                boolean isInShotRange = unit.hasWeaponRange(targetUnit, 0);
                if (!isInShotRange) {
                    unitsIterator.remove();
                }
            }
        }
        return this;
    }

    public Select<T> canShootAt(AUnit targetUnit, double shootingRangeBonus) {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AUnit unit = unitFrom(unitsIterator.next());
            if (unit.isCompleted() && unit.isAlive()) {
                boolean isInShotRange = unit.hasWeaponRange(targetUnit, shootingRangeBonus);
                if (!isInShotRange) {
                    unitsIterator.remove();
                }
            }
        }
        return this;
    }

    /**
     * Selects only those units from current selection, which can be both <b>attacked by</b> given unit (e.g.
     * Zerglings can't attack Overlord) and are <b>in shot range</b> to the given <b>unit</b>.
     */
    public Select<T> canBeAttackedBy(AUnit attacker, boolean checkShootingRange, boolean checkVisibility) {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AUnit target = unitFrom(unitsIterator.next());

            if (!attacker.hasWeaponToAttackThisUnit(target) || (checkVisibility && target.effCloaked())) {
                unitsIterator.remove();
            }
            else if (checkShootingRange && !attacker.hasWeaponRange(target, 0)) {
                unitsIterator.remove();
            }
        }
        return this;
    }

    public Select<T> canAttack(AUnit defender, boolean checkShootingRange, boolean checkVisibility) {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            AUnit attacker = unitFrom(unitsIterator.next());

            if (!attacker.canAttackThisUnit(defender, checkShootingRange, checkVisibility)) {
                unitsIterator.remove();
            }
            else if (checkShootingRange && !attacker.hasWeaponRange(defender, 0)) {
                unitsIterator.remove();
            }
        }
        return this;
    }

    public Select<T> inShootRangeOf(AUnit attacker) {
        return canBeAttackedBy(attacker, true, false);
    }

    public Select<T> inShootRangeOf(double shootingRangeBonus, AUnit attacker) {
        return canShootAt(attacker, shootingRangeBonus);
    }

//    public Select<T> inShootRangeOrAtMostTilesAway(double shootingRangeBonus, AUnit attacker) {
//    }

    // =========================================================
    // Hi-level auxiliary methods
    /**
     * Selects all of our bases.
     */
    public static Select<AUnit> ourBases() {
//        if (AGame.playsAsZerg()) {
//            return (Select<AUnit>) ourIncludingUnfinished().ofType(AUnitType.Zerg_Hatchery, AUnitType.Zerg_Lair, 
//                    AUnitType.Zerg_Hive, AUnitType.Protoss_Nexus, AUnitType.Terran_Command_Center);
//        }
//        else {
//            return (Select<AUnit>) ourIncludingUnfinished().ofType(AtlantisConfig.BASE);
//        }
        if (AGame.isPlayingAsZerg()) {
            return (Select<AUnit>) ourBuildings().ofType(AUnitType.Zerg_Hatchery, AUnitType.Zerg_Lair,
                    AUnitType.Zerg_Hive, AUnitType.Protoss_Nexus, AUnitType.Terran_Command_Center);
        } else {
            return (Select<AUnit>) ourBuildings().ofType(AtlantisConfig.BASE);
        }
    }

    /**
     * Selects our workers (that is of type Terran SCV or Zerg Drone or Protoss Probe).
     */
    public static Select<AUnit> ourWorkers() {
        Select<AUnit> selectedUnits = Select.our();
        //for (AUnit unit : selectedUnits.list()) {
//        System.out.println("########## OUR SIZE = " + selectedUnits.data.size());
        for (Iterator<AUnit> unitIter = selectedUnits.list().iterator(); unitIter.hasNext();) {
            AUnit unit = unitIter.next();

//            System.out.println(unit + " --> " +  !unit.isCompleted() + " / " +  !unit.isWorker() + " / " +  !unit.exists());
            if (!unit.isCompleted() || !unit.isWorker() || !unit.exists()) {
                unitIter.remove();
            }
        }
        return selectedUnits;
    }

    /**
     * Selects our workers (that is of type Terran SCV or Zerg Drone or Protoss Probe) that are either
     * gathering minerals or gas.
     */
    public static Select<AUnit> ourWorkersThatGather(boolean onlyNotCarryingAnything) {
        Select<AUnit> selectedUnits = Select.our();
        //for (AUnit unit : selectedUnits.list()) {
        for (Iterator<AUnit> unitIter = selectedUnits.list().iterator(); unitIter.hasNext();) {
            AUnit unit = unitIter.next();
            if (!unit.isWorker() || (!unit.isGatheringGas() && !unit.isGatheringMinerals())
                    || (onlyNotCarryingAnything && (unit.isCarryingGas() || unit.isCarryingMinerals()))) {
                unitIter.remove();
            }
        }
        return selectedUnits;
    }

    /**
     * Selects our workers that are free to construct building or repair a unit. That means they mustn't
     * repait any other unit or construct other building.
     */
    public static Select<AUnit> ourWorkersFreeToBuildOrRepair() {
        Select<AUnit> selectedUnits = ourWorkers();

        for (Iterator<AUnit> unitIter = selectedUnits.list().iterator(); unitIter.hasNext();) {
            AUnit unit = unitIter.next();
            if (unit.isConstructing() || unit.isRepairing() || AConstructionManager.isBuilder(unit)
                    || AScoutManager.isScout(unit) || unit.isRepairerOfAnyKind()) {
                unitIter.remove();
            }
        }

        return selectedUnits;
    }

    /**
     * Selects all our finished buildings.
     */
    public static Select<AUnit> ourBuildings() {
        return our().buildings();
    }

    /**
     * Selects all our buildings including those unfinished.
     */
    public static Select<AUnit> ourBuildingsIncludingUnfinished() {
        Select<AUnit> selectedUnits = Select.ourIncludingUnfinished();
        for (Iterator<AUnit> unitIter = selectedUnits.list().iterator(); unitIter.hasNext();) {
            AUnit unit = unitIter.next();
            if (!unit.type().isBuilding() && !unit.type().isAddon()) {
                unitIter.remove();
            }
        }
        return selectedUnits;
    }

    /**
     * Selects all our tanks, both sieged and unsieged.
     */
    public static Select<AUnit> ourTanks() {
        //cast is safe 'cuz our units are visible
        return (Select<AUnit>) our().ofType(AUnitType.Terran_Siege_Tank_Siege_Mode, AUnitType.Terran_Siege_Tank_Tank_Mode);
    }

    /**
     * Selects all our sieged tanks.
     */
    public static Select<AUnit> ourTanksSieged() {
        //cast is safe 'cuz our units are visible
        return (Select<AUnit>) our().ofType(AUnitType.Terran_Siege_Tank_Siege_Mode);
    }

    /**
     * Selects all of our Marines, Firebats, Ghosts and Medics.
     */
    public static Select<AUnit> ourTerranInfantry() {
        //cast is safe 'cuz our units are visible
        return (Select<AUnit>) our().ofType(AUnitType.Terran_Marine, AUnitType.Terran_Medic,
                AUnitType.Terran_Firebat, AUnitType.Terran_Ghost);
    }

    /**
     * Selects all of our Marines, Firebats, Ghosts.
     */
    public static Select<AUnit> ourTerranInfantryWithoutMedics() {
        //cast is safe 'cuz our units are visible
        return (Select<AUnit>) our().ofType(AUnitType.Terran_Marine,
                AUnitType.Terran_Firebat, AUnitType.Terran_Ghost);
    }

    /**
     * Selects all of our Zerg Larvas.
     */
    public static Select<AUnit> ourLarva() {
        Select<AUnit> selectedUnits = Select.ourIncludingUnfinished();
        for (Iterator<AUnit> unitIter = selectedUnits.list().iterator(); unitIter.hasNext();) {
            AUnit unit = unitIter.next();
            if (!unit.is(AUnitType.Zerg_Larva)) {
                unitIter.remove();
            }
        }
        return selectedUnits;
    }

    /**
     * Counts all of our Zerg Larvas.
     */
    public static int countOurLarva() {
        int total = 0;
        for (Iterator<AUnit> unitIter = our().list().iterator(); unitIter.hasNext();) {
            if (!unitIter.next().type().equals(AUnitType.Zerg_Larva)) {
                total++;
            }
        }
        return total;
    }

    /**
     * Selects all of our Zerg Eggs.
     */
    public static Select<AUnit> ourEggs() {
        Select<AUnit> selectedUnits = Select.ourIncludingUnfinished();
        for (Iterator<AUnit> unitIter = selectedUnits.list().iterator(); unitIter.hasNext();) {
            AUnit unit = unitIter.next();
            if (!unit.is(AUnitType.Zerg_Egg)) {
                unitIter.remove();
            }
        }
        return selectedUnits;
    }

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
        Collection<AUnit> bases = Select.ourBases().list();

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
    public Select<T> exclude(AUnit unitToExclude) {
        data.remove(unitToExclude);
        return this;
    }

    public Select<T> exclude(Collection<AUnit> unitsToExclude) {
        data.removeAll(unitsToExclude);
        return this;
    }

    /**
     * Reverse the order in which units are returned.
     */
    public Select<T> reverse() {
        Collections.reverse(data);
        return this;
    }

    /**
     * Returns a AUnit out of an entity that is either a AUnit or UnitData
     *
     * @param unitOrData
     * @return
     */
    private AUnit unitFrom(Object unitOrData) {
        return (unitOrData instanceof AUnit ? (AUnit) unitOrData : ((AFoggedUnit) unitOrData).getUnit());
    }

    /**
     * Returns a UnitData out of an entity that is either a AUnit or UnitData
     *
     * @param unitOrData
     * @return
     */
    private AFoggedUnit dataFrom(Object unitOrData) {
        return (unitOrData instanceof AFoggedUnit ? (AFoggedUnit) unitOrData : new AFoggedUnit((AUnit) unitOrData));
    }

    @SuppressWarnings("unused")
    private Select<T> filterOut(Collection<T> unitsToRemove) {
        data.removeAll(unitsToRemove);
        return this;
    }

    @SuppressWarnings("unused")
    private Select<T> filterAllBut(T unitToLeave) {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            T unit = unitsIterator.next();
            if (unitToLeave != unit) {
                data.remove(unit);
            }
        }
        return this;
    }

    @Override
    public String toString() {
        String string = "Units (" + data.size() + "):\n";

        for (Object unitOrData : data) {
            AUnit unit = unitFrom(unitOrData);
            string += "   - " + unit.type() + " (ID:" + unit.getID() + ")\n";
        }

        return string;
    }

    // =========================================================
    // Get results
    /**
     * Selects result as an iterable collection (list).
     */
    public List<T> list() {
        return data;
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
        Units units = new Units();
        units.addUnits((Collection<AUnit>) this.data);
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
//                System.out.println(((AUnit) unit).getShortName() + " - " + ((AUnit) unit).getHPPercent());
//            }
//        }

        return data;
    }

    public Select<T> clone() {
        return new Select<>(this.data);
    }

}
