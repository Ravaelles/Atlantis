package atlantis.wrappers;

import atlantis.Atlantis;
import atlantis.AtlantisConfig;
import atlantis.information.UnitData;
import atlantis.util.PositionUtil;
import atlantis.util.AtlantisUtilities;
import atlantis.util.UnitUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import bwapi.Position;
import bwapi.PositionedObject;
import bwapi.Unit;
import bwapi.UnitType;

/**
 * This class allows to easily select units e.g. to select one of your Marines, nearest to given location, you
 * would run:<br />
 * <p>
 * <b> Select.our().ofType(UnitType.Terran_Marine).nearestTo(Select.mainBase()) </b>
 * </p>
 * It uses nice flow and every next method filters out units that do not fulfill certain conditions.<br />
 * Unless clearly specified otherwise, this class returns <b>ONLY COMPLETED</b> units.
 */
public class Select<T> {

    // =====================================================================
    // Collection<Unit> wrapper with extra methods
    //private Units units;
    private List<T> data;

    // CACHED variables
    private static Unit _cached_mainBase = null;

    // =====================================================================
    // Constructor is private, use our(), enemy() or neutral() methods
    protected Select(Collection<T> unitsData) {
        data = new ArrayList<>();
        data.addAll(unitsData);
    }

    // =====================================================================
    // Create base object
    /**
     * Selects all of our finished and existing units (units, buildings, but no spider mines etc).
     */
    public static Select<Unit> our() {
        //Units units = new Units();
        List<Unit> data = new ArrayList<>();

        //self().getUnits() replaces getMyUnits()
        for (Unit unit : Atlantis.getBwapi().self().getUnits()) {
            if (unit.exists() && unit.isCompleted() && !UnitUtil.isType(unit.getType(), UnitType.Terran_Vulture_Spider_Mine, UnitType.Zerg_Larva, UnitType.Zerg_Egg)) {
                data.add(unit);	//TODO: make it more efficient by just querying the cache of known units
            }
        }
        return new Select<Unit>(data);

    }

    /**
     * Selects all of our finished combat units (no buildings, workers, spider mines etc).
     */
    public static Select<Unit> ourCombatUnits() {
        //Units units = new Units();
        List<Unit> data = new ArrayList<>();
        
        for (Unit unit : Atlantis.getBwapi().self().getUnits()) {
            if (unit.exists() && unit.isCompleted() && !UnitUtil.isNotActuallyUnit(unit.getType()) && !unit.getType().isBuilding()
                    && !unit.getType().equals(AtlantisConfig.WORKER)) {
                data.add(unit);	//TODO: make it more efficient by just querying the cache of known units
            }
        }

        return new Select<Unit>(data);
    }

    /**
     * Selects all of our units (units, buildings, but no spider mines etc), <b>even those unfinished</b>.
     */
    public static Select<Unit> ourIncludingUnfinished() {
        //Units units = new Units();
        List<Unit> data = new ArrayList<>();

        for (Unit unit : Atlantis.getBwapi().self().getUnits()) {
            if (unit.exists() && !unit.getType().equals(UnitType.Terran_Vulture_Spider_Mine)) {
                data.add(unit);	//TODO: make it more efficient by just querying the cache of known units
            }
        }

        return new Select<Unit>(data);
    }

    /**
     * Selects our unfinished units.
     */
    public static Select<Unit> ourUnfinished() {
        //Units units = new Units();
        List<Unit> data = new ArrayList<>();

        for (Unit unit : Atlantis.getBwapi().self().getUnits()) {
            if (unit.exists() && !unit.isCompleted()) {
                data.add(unit);
            }
        }

        return new Select<Unit>(data);
    }

    /**
     * Selects our unfinished units.
     */
    public static Select<Unit> ourRealUnits() {
        List<Unit> data = new ArrayList<>();

        for (Unit unit : Atlantis.getBwapi().self().getUnits()) {
            if (unit.exists() && unit.isCompleted() && !unit.getType().isBuilding() && !UnitUtil.isNotActuallyUnit(unit.getType())) {
                data.add(unit);
            }
        }

        return new Select<Unit>(data);
    }

    /**
     * Selects our unfinished units.
     */
    public static Select<Unit> ourUnfinishedRealUnits() {
        List<Unit> data = new ArrayList<>();

        for (Unit unit : Atlantis.getBwapi().self().getUnits()) {
            if (unit.exists() && !unit.isCompleted() && !unit.getType().isBuilding() && !UnitUtil.isNotActuallyUnit(unit.getType())) {
                data.add(unit);
            }
        }

        return new Select<Unit>(data);
    }

    /**
     * Selects all visible enemy units. Since they're visible, the parameterized type is Unit
     */
    public static Select<Unit> enemy() {
        List<Unit> data = new ArrayList<>();

        //TODO: check whether enemy().getUnits() has the same behavior as  getEnemyUnits()
        for (Unit unit : Atlantis.getBwapi().enemy().getUnits()) {
            if (unit.isVisible() && unit.getHitPoints() >= 1) {
                data.add(unit);
            }
        }

        return new Select<Unit>(data);
    }

    /**
     * Selects all visible enemy units. Since they're visible, the parameterized type is Unit
     */
    public static Select<Unit> enemy(boolean includeGroundUnits, boolean includeAirUnits) {
        List<Unit> data = new ArrayList<>();

        for (Unit unit : Atlantis.getBwapi().enemy().getUnits()) {
            if (unit.isVisible() && unit.getHitPoints() >= 1) {
                if ((!unit.getType().isFlyer() && includeGroundUnits) || (unit.getType().isFlyer() && includeAirUnits)) {
                    data.add(unit);
                }
            }
        }

        return new Select<Unit>(data);
    }

    /**
     * Selects all visible enemy units. Since they're visible, the parameterized type is Unit
     */
    public static Select<Unit> enemyRealUnits() {
        List<Unit> data = new ArrayList<>();

        for (Unit unit : Atlantis.getBwapi().enemy().getUnits()) {
            if (unit.exists() && unit.isVisible() && !unit.getType().isBuilding() && !UnitUtil.isNotActuallyUnit(unit.getType())) {
                data.add(unit);
            }
        }

        return new Select<Unit>(data);
    }

    /**
     * Selects all visible enemy units. Since they're visible, the parameterized type is Unit
     */
    public static Select<Unit> enemyRealUnits(boolean includeGroundUnits, boolean includeAirUnits) {
        List<Unit> data = new ArrayList<>();

        for (Unit unit : Atlantis.getBwapi().enemy().getUnits()) {
            if (unit.exists() && unit.isVisible() && !unit.getType().isBuilding() && !UnitUtil.isType(unit.getType(), UnitType.Zerg_Larva, UnitType.Zerg_Egg)) {
                if ((!unit.getType().isFlyer() && includeGroundUnits) || (unit.getType().isFlyer() && includeAirUnits)) {
                    data.add(unit);
                }
            }
        }

        return new Select<Unit>(data);
    }

    /**
     * Selects all visible neutral units (minerals, geysers, critters). Since they're visible, the
     * parameterized type is Unit
     */
    public static Select<Unit> neutral() {
        List<Unit> data = new ArrayList<>();

        data.addAll(Atlantis.getBwapi().getNeutralUnits());

        return new Select<Unit>(data);
    }

    /**
     * Selects all (accessible) minerals on the map.
     */
    public static Select<Unit> minerals() {
        /*Units units = new Units();

        units.addUnits(Atlantis.getBwapi().getNeutralUnits());*/
        Select<Unit> selectUnits = neutral();

        return (Select<Unit>) selectUnits.ofType(UnitType.Resource_Mineral_Field);
    }

    /**
     * Selects all geysers on the map.
     */
    public static Select<Unit> geysers() {
        /*Units units = new Units();

        units.addUnits(Atlantis.getBwapi().getNeutralUnits());*/
        Select<Unit> selectUnits = neutral();

        return (Select<Unit>) selectUnits.ofType(UnitType.Resource_Vespene_Geyser);
    }

    /**
     * Create initial search-pool of units from given collection of units.
     */
    public static Select<Unit> from(List<Unit> units) {
        Select<Unit> selectUnits = new Select<Unit>(units);
        return selectUnits;
    }

    /**
     * Create initial search-pool of units from given collection of units.
     */
    public static Select<UnitData> fromData(Collection<UnitData> units) {
        Select<UnitData> selectUnits = new Select<UnitData>(units);
        return selectUnits;
    }

    /**
     * Returns all units that are closer than <b>maxDist</b> tiles from given <b>position</b>.
     */
    public Select<?> inRadius(double maxDist, Position position) {
        Iterator<T> unitsIterator = data.iterator();// units.iterator();
        while (unitsIterator.hasNext()) {
            PositionedObject unit = (PositionedObject) unitsIterator.next();
            if (PositionUtil.distanceTo(unit.getPosition(), position) > maxDist) {
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
    public Select<?> ofType(UnitType... types) {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            Object unitOrData = unitsIterator.next();

            boolean typeMatches = (unitOrData instanceof Unit ? typeMatches((Unit) unitOrData, types) : typeMatches((UnitData) unitOrData, types));

            /*for (UnitType type : types) {
                if (unit.getType().equals(type) 
                        || (unit.getType().equals(UnitType.Zerg_Egg) && unit.getBuildType().equals(type))) {
                    typeMatches = true;
                    break;
                }
            }*/
            if (!typeMatches) {
                unitsIterator.remove();
            }
        }

        return this;
    }

    /**
     * Returns whether the type in needle matches one in the haystack
     *
     * @param Unit|UnitData needle
     * @param haystack
     * @return
     */
    private boolean typeMatches(Unit needle, UnitType... haystack) {
        Unit unit = unitFrom(needle);

        for (UnitType type : haystack) {
            if (unit.getType().equals(type)
                    || (unit.getType().equals(UnitType.Zerg_Egg) && unit.getBuildType().equals(type))) {
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
    private boolean typeMatches(UnitData needle, UnitType... haystack) {

        for (UnitType type : haystack) {
            if (needle.getType().equals(type)
                    || (needle.getType().equals(UnitType.Zerg_Egg) && needle.getBuildType().equals(type))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Selects only units of given type(s).
     */
    public int countUnitsOfType(UnitType... types) {
        int total = 0;
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            Unit unit = unitFrom(unitsIterator.next());
            boolean typeMatches = false;
            for (UnitType type : types) {
                if (unit.getType().equals(type)
                        || (unit.getType().equals(UnitType.Zerg_Egg) && unit.getBuildType().equals(type))) {
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
     * Selects only those units which are idle. Idle is unit's class flag so be careful with that.
     */
    public Select<T> idle() {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            Unit unit = unitFrom(unitsIterator.next());	//TODO: will probably not work with enemy units
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
            Unit unit = unitFrom(unitsIterator.next());	//TODO: will probably not work with enemy units
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
            UnitData unit = dataFrom(unitsIterator.next());	//(unitOrData instanceof Unit ? (Unit) unitOrData : ((UnitData)unitOrData).getUnit()); 
            if (!unit.getType().isOrganic()) { //replaced  isInfantry()
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
            Unit unit = unitFrom(unitsIterator.next());	//TODO: will work properly only on visible units
            // unit.getHitPoints() >= unit.getType().maxHitPoints() replaces !isWounded()
            if (unit.getHitPoints() >= unit.getType().maxHitPoints()) {
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
            UnitData uData = dataFrom(unitsIterator.next());
            if (!uData.getType().isBuilding()) {
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
            UnitData uData = dataFrom(unitsIterator.next());
            boolean isMilitaryBuilding = UnitUtil.isType(uData.getType(),
                    UnitType.Terran_Bunker,
                    UnitType.Protoss_Photon_Cannon,
                    UnitType.Zerg_Sunken_Colony,
                    UnitType.Zerg_Spore_Colony
            );
            Unit u = uData.getUnit();	//TODO: will work only on visible units...
            if (!u.isCompleted() || !u.exists() || (uData.getType().isBuilding() && !isMilitaryBuilding)) {
                unitsIterator.remove();
            }
        }
        return this;
    }

    /**
     * Selects only those Terran vehicles that can be repaired so it has to be:<br />
     * - mechanical<br />
     * - not 100% healthy<br />
     */
    public Select<T> toRepair() {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            Unit unit = unitFrom(unitsIterator.next());

            //isMechanical replaces  isRepairableMechanically
            //unit.getHitPoints() >= unit.getType().maxHitPoints() replaces isFullyHealthy
            if (!unit.getType().isMechanical() || unit.getHitPoints() >= unit.getType().maxHitPoints() || !unit.isCompleted()) {
                unitsIterator.remove();
            }
        }
        return this;
    }

    /**
     * Selects only those units from current selection, which are both <b>capable of attacking</b> given unit
     * (e.g. Zerglings can't attack Overlord) and are <b>in shot range</b> to the given <b>unit</b>.
     */
    public Select<T> thatCanShoot(Unit targetUnit) {
        Iterator<T> unitsIterator = data.iterator();
        while (unitsIterator.hasNext()) {
            Unit unit = unitFrom(unitsIterator.next());
            if (!unit.isCompleted() || !unit.isAlive()) {
                boolean isInShotRange = unit.hasRangeToAttack(targetUnit, 0.5);
                if (!isInShotRange) {
                    unitsIterator.remove();
                }
                else {
                    System.out.println(unit.getType().getShortName() + " in range (" 
                            + unit.distanceTo(targetUnit) + ") to attack " + targetUnit.getType().getShortName());
                }
            }
        }
        return this;
    }
    
    // =========================================================
    // Hi-level auxiliary methods
    /**
     * Selects all of our bases.
     */
    public static Select<Unit> ourBases() {
        return (Select<Unit>) our().ofType(AtlantisConfig.BASE);	//cast is safe 'cuz our units are visible
    }

    /**
     * Selects our workers (that is of type Terran SCV or Zerg Drone or Protoss Probe).
     */
    public static Select<Unit> ourWorkers() {
        Select<Unit> selectedUnits = Select.our();
        //for (Unit unit : selectedUnits.list()) {
        for (Iterator<Unit> unitIter = selectedUnits.list().iterator(); unitIter.hasNext();) {
            Unit unit = unitIter.next();
            if (!unit.isCompleted() || !unit.getType().isWorker() || !unit.exists()) {
                unitIter.remove();
            }
        }
        return selectedUnits;
    }

    /**
     * Selects our workers (that is of type Terran SCV or Zerg Drone or Protoss Probe) that are either
     * gathering minerals or gas.
     */
    public static Select<Unit> ourWorkersThatGather() {
        Select<Unit> selectedUnits = Select.our();
        //for (Unit unit : selectedUnits.list()) {
        for (Iterator<Unit> unitIter = selectedUnits.list().iterator(); unitIter.hasNext();) {
            Unit unit = unitIter.next();
            if (!unit.getType().isWorker() || (!unit.isGatheringGas() && !unit.isGatheringMinerals())) {
                unitIter.remove();
            }
        }
        return selectedUnits;
    }

    /**
     * Selects our workers that are free to construct building or repair a unit. That means they mustn't
     * repait any other unit or construct other building.
     */
    public static Select<Unit> ourWorkersFreeToBuildOrRepair() {
        Select<Unit> selectedUnits = ourWorkers();

        for (Iterator<Unit> unitIter = selectedUnits.list().iterator(); unitIter.hasNext();) {
            Unit unit = unitIter.next();
            if (unit.isConstructing() || unit.isRepairing()) {
                unitIter.remove();
            }
        }

        return selectedUnits;
    }

    /**
     * Selects all our finished buildings.
     */
    public static Select<Unit> ourBuildings() {
        return our().buildings();
    }

    /**
     * Selects all our buildings including those unfinished.
     */
    public static Select<Unit> ourBuildingsIncludingUnfinished() {
        Select<Unit> selectedUnits = Select.ourIncludingUnfinished();
        for (Iterator<Unit> unitIter = selectedUnits.list().iterator(); unitIter.hasNext();) {
            Unit unit = unitIter.next();
            if (!unit.getType().isBuilding()) {
                unitIter.remove();
            }
        }
        return selectedUnits;
    }

    /**
     * Selects all our tanks, both sieged and unsieged.
     */
    public static Select<Unit> ourTanks() {
        //cast is safe 'cuz our units are visible
        return (Select<Unit>) our().ofType(UnitType.Terran_Siege_Tank_Siege_Mode, UnitType.Terran_Siege_Tank_Tank_Mode);
    }

    /**
     * Selects all our sieged tanks.
     */
    public static Select<Unit> ourTanksSieged() {
        //cast is safe 'cuz our units are visible
        return (Select<Unit>) our().ofType(UnitType.Terran_Siege_Tank_Siege_Mode);
    }

    /**
     * Selects all of our Marines, Firebats, Ghosts and Medics.
     */
    public static Select<Unit> ourTerranInfantry() {
        //cast is safe 'cuz our units are visible
        return (Select<Unit>) our().ofType(UnitType.Terran_Marine, UnitType.Terran_Medic,
                UnitType.Terran_Firebat, UnitType.Terran_Ghost);
    }

    /**
     * Selects all of our Marines, Firebats, Ghosts.
     */
    public static Select<Unit> ourTerranInfantryWithoutMedics() {
        //cast is safe 'cuz our units are visible
        return (Select<Unit>) our().ofType(UnitType.Terran_Marine,
                UnitType.Terran_Firebat, UnitType.Terran_Ghost);
    }

    /**
     * Selects all of our Zerg Larvas.
     */
    public static Select<Unit> ourLarva() {
        Select<Unit> selectedUnits = Select.ourIncludingUnfinished();
        for (Iterator<Unit> unitIter = selectedUnits.list().iterator(); unitIter.hasNext();) {
            Unit unit = unitIter.next();
            if (!unit.getType().equals(UnitType.Zerg_Larva)) {
                unitIter.remove();
            }
        }
        return selectedUnits;
    }

    /**
     * Selects all of our Zerg Eggs.
     */
    public static Select<Unit> ourEggs() {
        Select<Unit> selectedUnits = Select.ourIncludingUnfinished();
        for (Iterator<Unit> unitIter = selectedUnits.list().iterator(); unitIter.hasNext();) {
            Unit unit = unitIter.next();
            if (!unit.getType().equals(UnitType.Zerg_Egg)) {
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
    public T nearestTo(Position position) {
        if (data.isEmpty() || position == null) {
            return null;
        }

        sortDataByDistanceTo(position, true);
        return data.get(0);	//first();
    }

    /**
     * Returns first unit being base. For your units this is most likely your main base, for enemy it will be
     * first discovered base.
     */
    public static Unit mainBase() {
        if (_cached_mainBase == null) {
            List<Unit> bases = ourBases().list();
            _cached_mainBase = bases.isEmpty() ? null : bases.get(0);	//first();
        }
        return _cached_mainBase;
    }

    /**
     * Returns second (natural) base <b>or if we have only one base</b>, it returns the only base we have.
     */
    public static Unit secondBaseOrMainIfNoSecond() {
        Collection<Unit> bases = Select.ourBases().list();
        
        int counter = 0;
        for (Unit base : bases) {
            if (bases.size() <= 1) {
                return base;
            }
            else if (counter > 0) {
                return base;
            }
            
            counter++;
        }
        
        return null;
    }

    /**
     * Returns first idle our unit of given type or null if no idle units found.
     */
    public static Unit ourOneIdle(UnitType type) {
        for (Unit unit : Atlantis.getBwapi().self().getUnits()) {
            if (unit.isCompleted() && unit.isIdle() && unit.getType().equals(type)) {
                return unit;
            }
        }
        return null;
    }

    /**
     * Returns nearest enemy to the given position (or unit).
     */
    public static Unit nearestEnemy(Position position) {
        return Select.enemy().nearestTo(position);
    }

    // =========================================================
    // Auxiliary methods
    /**
     * Returns <b>true</b> if current selection contains at least one unit.
     */
    public boolean anyExists() {
        return !data.isEmpty();
    }

    /**
     * Returns first unit that matches previous conditions or null if no units match conditions.
     */
    public T first() {
        return data.isEmpty() ? null : data.get(0);	// first();
    }

    /**
     * Returns random unit that matches previous conditions or null if no units matched all conditions.
     */
    public T random() {
        return (T) AtlantisUtilities.getRandomElement(data); //units.random();
    }

    /**
     * Returns a Unit out of an entity that is either a Unit or UnitData
     *
     * @param unitOrData
     * @return
     */
    private Unit unitFrom(Object unitOrData) {
        return (unitOrData instanceof Unit ? (Unit) unitOrData : ((UnitData) unitOrData).getUnit());
    }

    /**
     * Returns a UnitData out of an entity that is either a Unit or UnitData
     *
     * @param unitOrData
     * @return
     */
    private UnitData dataFrom(Object unitOrData) {
        return (unitOrData instanceof UnitData ? (UnitData) unitOrData : new UnitData((Unit) unitOrData));
    }

    // =========================================================
    // Operations on set of units
    /**
     * @return all units except for the given one
     */
    public Select<T> exclude(T unitToExclude) {
        data.remove(unitToExclude);
        return this;
    }

    @SuppressWarnings("unused")
    private Select<T> filterOut(Collection<T> unitsToRemove) {
        data.removeAll(unitsToRemove);
        return this;
    }

    // private Select filterOut(Unit unitToRemove) {
    // // units.removeUnit(unitToRemove);
    // Iterator<Unit> unitsIterator = units.iterator();
    // while (unitsIterator.hasNext()) {
    // Unit unit = unitsIterator.next();
    // if (unitToRemove.equals(unit)) {
    // units.removeUnit(unit);
    // }
    // }
    // return this;
    // }
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
            Unit unit = unitFrom(unitOrData);
            string += "   - " + unit.getType() + " (ID:" + unit.getID() + ")\n";
        }

        return string;
    }

    // =========================================================
    // Get results
    /**
     * Selects units that match all previous criteria. <b>Units</b> class is used as a wrapper for result. See
     * its javadoc too learn what it can do.
     */
//    public UnitsData unitsData() { 
//        return data;
//    }
     
    /**
     * Selects result as an iterable collection (list).
     */
    public List<T> list() {
        return data;
    }
    
    /**
     * Selects units as an iterable collection (list).
     */
    public List<Unit> listUnits() {
        return (List<Unit>) data;
    }

    /**
     * Returns number of units matching all previous conditions.
     */
    public int count() {
        return data.size();
    }

    /**
     * Sorts data list by distance to a given position
     *
     * @param position
     * @param nearestFirst
     * @return
     */
    public List<T> sortDataByDistanceTo(final Position position, final boolean nearestFirst) {
        if (position == null) {
            return null;
        }

        Collections.sort(data, new Comparator<T>() {
            @Override
            public int compare(T p1, T p2) {
                if (p1 == null || !(p1 instanceof PositionedObject)) {
                    return -1;
                }
                if (p2 == null || !(p2 instanceof PositionedObject)) {
                    return 1;
                }
                UnitData data1 = dataFrom(p1);
                UnitData data2 = dataFrom(p2);
                double distance1 = PositionUtil.distanceTo(position, data1.getPosition());	//TODO: check whether this doesn't mix up position types
                double distance2 = PositionUtil.distanceTo(position, data2.getPosition());
                if (distance1 == distance2) {
                    return 0;
                } else {
                    return distance1 < distance2 ? (nearestFirst ? -1 : 1) : (nearestFirst ? 1 : -1);
                }
            }
        });

        return data;
    }

}
