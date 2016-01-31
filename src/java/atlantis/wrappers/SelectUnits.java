package atlantis.wrappers;

import atlantis.Atlantis;
import atlantis.AtlantisConfig;
import java.util.Collection;
import java.util.Iterator;
import jnibwapi.Position;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;

/**
 * This class allows to easily select units e.g. to select one of your Marines, nearest to given location, you
 * would run:<br />
 * <p>
 * <b> SelectUnits.our().ofType(UnitTypes.Terran_Marine).nearestTo(somePlace) </b>
 * </p>
 * It uses nice flow and every next method filters out units that do not fulfill certain conditions.<br />
 * Unless clearly specified otherwise, this class returns <b>ONLY COMPLETED</b> units.
 */
public class SelectUnits {

    // =====================================================================
    // Collection<Unit> wrapper with extra methods
    private Units units;

    // CACHED variables
    private static Unit _cached_mainBase = null;

    // =====================================================================
    // Constructor is private, use our(), enemy() or neutral() methods
    private SelectUnits(Units units) {
        this.units = units;
    }

    // =====================================================================
    // Create base object
    /**
     * Selects all of our finished and existing units (units, buildings, but no spider mines etc).
     */
    public static SelectUnits our() {
        Units units = new Units();

        for (Unit unit : Atlantis.getBwapi().getMyUnits()) {
            if (unit.isAlive() && unit.isCompleted() && !unit.isSpiderMine() && !unit.isLarvaOrEgg()) {
                units.addUnit(unit);
            }
        }

        return new SelectUnits(units);
    }

    /**
     * Selects all of our finished combat units (no buildings, workers, spider mines etc).
     */
    public static SelectUnits ourCombatUnits() {
        Units units = new Units();

        for (Unit unit : Atlantis.getBwapi().getMyUnits()) {
            if (unit.isAlive() && unit.isCompleted() && !unit.isNotActuallyUnit() && !unit.isBuilding()
                    && !unit.isType(AtlantisConfig.WORKER)) {
                units.addUnit(unit);
            }
        }

        return new SelectUnits(units);
    }

    /**
     * Selects all of our units (units, buildings, but no spider mines etc), <b>even those unfinished</b>.
     */
    public static SelectUnits ourIncludingUnfinished() {
        Units units = new Units();

        for (Unit unit : Atlantis.getBwapi().getMyUnits()) {
            if (unit.isAlive() && !unit.isSpiderMine()) {
                units.addUnit(unit);
            }
        }

        return new SelectUnits(units);
    }

    /**
     * Selects our unfinished units.
     */
    public static SelectUnits ourUnfinished() {
        Units units = new Units();

        for (Unit unit : Atlantis.getBwapi().getMyUnits()) {
            if (unit.isAlive() && !unit.isCompleted()) {
                units.addUnit(unit);
            }
        }

        return new SelectUnits(units);
    }

    /**
     * Selects our unfinished units.
     */
    public static SelectUnits ourRealUnits() {
        Units units = new Units();

        for (Unit unit : Atlantis.getBwapi().getMyUnits()) {
            if (unit.isAlive() && unit.isCompleted() && !unit.isBuilding() && !unit.isNotActuallyUnit()) {
                units.addUnit(unit);
            }
        }

        return new SelectUnits(units);
    }

    /**
     * Selects our unfinished units.
     */
    public static SelectUnits ourUnfinishedRealUnits() {
        Units units = new Units();

        for (Unit unit : Atlantis.getBwapi().getMyUnits()) {
            if (unit.isAlive() && !unit.isCompleted() && !unit.isBuilding() && !unit.isNotActuallyUnit()) {
                units.addUnit(unit);
            }
        }

        return new SelectUnits(units);
    }

    /**
     * Selects all visible enemy units.
     */
    public static SelectUnits enemy() {
        Units units = new Units();

        for (Unit unit : Atlantis.getBwapi().getEnemyUnits()) {
            if (unit.isVisible() && unit.getHP() >= 1) {
                units.addUnit(unit);
            }
        }

        return new SelectUnits(units);
    }

    /**
     * Selects all visible enemy units.
     */
    public static SelectUnits enemy(boolean includeGroundUnits, boolean includeAirUnits) {
        Units units = new Units();

        for (Unit unit : Atlantis.getBwapi().getEnemyUnits()) {
            if (unit.isVisible() && unit.getHP() >= 1) {
                if ((unit.isGroundUnit() && includeGroundUnits) || (unit.isAirUnit() && includeAirUnits)) {
                    units.addUnit(unit);
                }
            }
        }

        return new SelectUnits(units);
    }

    /**
     * Selects all visible enemy units.
     */
    public static SelectUnits enemyRealUnits() {
        Units units = new Units();

        for (Unit unit : Atlantis.getBwapi().getEnemyUnits()) {
            if (unit.isAlive() && unit.isVisible() && !unit.isBuilding() && !unit.isNotActuallyUnit()) {
                units.addUnit(unit);
            }
        }

        return new SelectUnits(units);
    }

    /**
     * Selects all visible enemy units.
     */
    public static SelectUnits enemyRealUnits(boolean includeGroundUnits, boolean includeAirUnits) {
        Units units = new Units();

        for (Unit unit : Atlantis.getBwapi().getEnemyUnits()) {
            if (unit.isAlive() && unit.isVisible() && !unit.isBuilding() && !unit.isLarvaOrEgg()) {
                if ((unit.isGroundUnit() && includeGroundUnits) || (unit.isAirUnit() && includeAirUnits)) {
                    units.addUnit(unit);
                }
            }
        }

        return new SelectUnits(units);
    }

    /**
     * Selects all visible neutral units (minerals, geysers, critters).
     */
    public static SelectUnits neutral() {
        Units units = new Units();

        units.addUnits(Atlantis.getBwapi().getNeutralUnits());

        return new SelectUnits(units);
    }

    /**
     * Selects all minerals on the map.
     */
    public static SelectUnits minerals() {
        Units units = new Units();

        units.addUnits(Atlantis.getBwapi().getNeutralUnits());
        SelectUnits selectUnits = new SelectUnits(units);

        return selectUnits.ofType(UnitTypes.Resource_Mineral_Field);
    }

    /**
     * Selects all geysers on the map.
     */
    public static SelectUnits geysers() {
        Units units = new Units();

        units.addUnits(Atlantis.getBwapi().getNeutralUnits());
        SelectUnits selectUnits = new SelectUnits(units);

        return selectUnits.ofType(UnitTypes.Resource_Vespene_Geyser);
    }

    /**
     * Create initial search-pool of units from given collection of units.
     */
    public static SelectUnits from(Units units) {
        SelectUnits selectUnits = new SelectUnits(units);
        return selectUnits;
    }

    /**
     * Create initial search-pool of units from given collection of units.
     */
    public static SelectUnits from(Collection<Unit> unitsCollection) {
        Units units = new Units();
        for (Unit unit : unitsCollection) {
            units.addUnit(unit);
        }

        SelectUnits selectUnits = new SelectUnits(units);
        return selectUnits;
    }

    /**
     * Returns all units that are closer than <b>maxDist</b> tiles from given <b>position</b>.
     */
    public SelectUnits inRadius(double maxDist, Position position) {
        Iterator<Unit> unitsIterator = units.iterator();
        while (unitsIterator.hasNext()) {
            Unit unit = unitsIterator.next();
            if (unit.distanceTo(position) > maxDist) {
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
    public SelectUnits ofType(UnitType... types) {
        Iterator<Unit> unitsIterator = units.iterator();
        while (unitsIterator.hasNext()) {
            Unit unit = unitsIterator.next();
            boolean typeMatches = false;
            for (UnitType type : types) {
                if (unit.getType().equals(type) 
                        || (unit.getType().equals(UnitTypes.Zerg_Egg) && unit.getBuildType().equals(type))) {
                    typeMatches = true;
                    break;
                }
            }
            if (!typeMatches) {
                unitsIterator.remove();
            }
        }

        return this;
    }
    
    /**
     * Selects only units of given type(s).
     */
    public int countUnitsOfType(UnitType... types) {
        int total = 0;
        Iterator<Unit> unitsIterator = units.iterator();
        while (unitsIterator.hasNext()) {
            Unit unit = unitsIterator.next();
            boolean typeMatches = false;
            for (UnitType type : types) {
                if (unit.getType().equals(type) 
                        || (unit.getType().equals(UnitTypes.Zerg_Egg) && unit.getBuildType().equals(type))) {
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
    public SelectUnits idle() {
        Iterator<Unit> unitsIterator = units.iterator();
        while (unitsIterator.hasNext()) {
            Unit unit = unitsIterator.next();
            if (!unit.isIdle()) {
                unitsIterator.remove();
            }
        }

        return this;
    }

    /**
     * Selects units that are gathering minerals.
     */
    public SelectUnits gatheringMinerals(boolean onlyNotCarryingMinerals) {
        Iterator<Unit> unitsIterator = units.iterator();
        while (unitsIterator.hasNext()) {
            Unit unit = unitsIterator.next();
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
    public SelectUnits infantry() {
        Iterator<Unit> unitsIterator = units.iterator();
        while (unitsIterator.hasNext()) {
            Unit unit = unitsIterator.next();
            if (!unit.isInfantry()) {
                unitsIterator.remove();
            }
        }

        return this;
    }

    /**
     * Selects only units that do not currently have max hit points.
     */
    public SelectUnits wounded() {
        Iterator<Unit> unitsIterator = units.iterator();
        while (unitsIterator.hasNext()) {
            Unit unit = unitsIterator.next();
            if (!unit.isWounded()) {
                unitsIterator.remove();
            }
        }

        return this;
    }

    /**
     * Selects only buildings.
     */
    public SelectUnits buildings() {
        Iterator<Unit> unitsIterator = units.iterator();
        while (unitsIterator.hasNext()) {
            Unit unit = unitsIterator.next();
            if (!unit.isBuilding()) {
                unitsIterator.remove();
            }
        }
        return this;
    }

    /**
     * Selects only units that can fight in any way including: - infantry including Terran Medics, but not
     * workers - military buildings like Photon Cannon, Bunker, Spore Colony, Sunken Colony
     */
    public SelectUnits combatUnits() {
        Iterator<Unit> unitsIterator = units.iterator();
        while (unitsIterator.hasNext()) {
            Unit unit = unitsIterator.next();
            boolean isMilitaryBuilding = unit.isType(
                    UnitTypes.Terran_Bunker,
                    UnitTypes.Protoss_Photon_Cannon,
                    UnitTypes.Zerg_Sunken_Colony,
                    UnitTypes.Zerg_Spore_Colony
            );
            if (!unit.isCompleted() || !unit.isAlive() || (unit.isBuilding() && !isMilitaryBuilding)) {
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
    public SelectUnits toRepair() {
        Iterator<Unit> unitsIterator = units.iterator();
        while (unitsIterator.hasNext()) {
            Unit unit = unitsIterator.next();
            if (!unit.isRepairableMechanically() || unit.isFullyHealthy() || !unit.isCompleted()) {
                unitsIterator.remove();
            }
        }
        return this;
    }

    // =========================================================
    // Hi-level auxiliary methods
    /**
     * Selects all of our bases.
     */
    public static SelectUnits ourBases() {
        return our().ofType(AtlantisConfig.BASE);
    }

    /**
     * Selects our workers (that is of type Terran SCV or Zerg Drone or Protoss Probe).
     */
    public static SelectUnits ourWorkers() {
        SelectUnits selectedUnits = SelectUnits.our();
        for (Unit unit : selectedUnits.list()) {
            if (!unit.isWorker() && unit.isAlive()) {
                selectedUnits.units.removeUnit(unit);
            }
        }
        return selectedUnits;
    }

    /**
     * Selects our workers (that is of type Terran SCV or Zerg Drone or Protoss Probe) that are either 
     * gathering minerals or gas.
     */
    public static SelectUnits ourWorkersThatGather() {
        SelectUnits selectedUnits = SelectUnits.our();
        for (Unit unit : selectedUnits.list()) {
            if (!unit.isWorker() || (!unit.isGatheringGas() && !unit.isGatheringMinerals())) {
                selectedUnits.units.removeUnit(unit);
            }
        }
        return selectedUnits;
    }

    /**
     * Selects our workers that are free to construct building or repair a unit. That means they mustn't
     * repait any other unit or construct other building.
     */
    public static SelectUnits ourWorkersFreeToBuildOrRepair() {
        SelectUnits selectedUnits = ourWorkers();

        for (Unit unit : selectedUnits.list()) {
            if (unit.isConstructing() || unit.isRepairing()) {
                selectedUnits.units.removeUnit(unit);
            }
        }

        return selectedUnits;
    }

    /**
     * Selects all our finished buildings.
     */
    public static SelectUnits ourBuildings() {
        return our().buildings();
    }

    /**
     * Selects all our buildings including those unfinished.
     */
    public static SelectUnits ourBuildingsIncludingUnfinished() {
        SelectUnits selectedUnits = SelectUnits.ourIncludingUnfinished();
        for (Unit unit : selectedUnits.list()) {
            if (!unit.isBuilding()) {
                selectedUnits.units.removeUnit(unit);
            }
        }
        return selectedUnits;
    }

    /**
     * Selects all our tanks, both sieged and unsieged.
     */
    public static SelectUnits ourTanks() {
        return our().ofType(UnitTypes.Terran_Siege_Tank_Siege_Mode, UnitTypes.Terran_Siege_Tank_Tank_Mode);
    }

    /**
     * Selects all our sieged tanks.
     */
    public static SelectUnits ourTanksSieged() {
        return our().ofType(UnitTypes.Terran_Siege_Tank_Siege_Mode);
    }

    /**
     * Selects all of our Zerg Larvas.
     */
    public static SelectUnits ourLarva() {
        SelectUnits selectedUnits = SelectUnits.ourIncludingUnfinished();
        for (Unit unit : selectedUnits.list()) {
            if (!unit.getType().equals(UnitTypes.Zerg_Larva)) {
                selectedUnits.units.removeUnit(unit);
            }
        }
        return selectedUnits;
    }

    /**
     * Selects all of our Zerg Eggs.
     */
    public static SelectUnits ourEggs() {
        SelectUnits selectedUnits = SelectUnits.ourIncludingUnfinished();
        for (Unit unit : selectedUnits.list()) {
            if (!unit.getType().equals(UnitTypes.Zerg_Egg)) {
                selectedUnits.units.removeUnit(unit);
            }
        }
        return selectedUnits;
    }

    // =========================================================
    // Localization-related methods
    /**
     * From all units currently in selection, returns closest unit to given <b>position</b>.
     */
    public Unit nearestTo(Position position) {
        if (units.isEmpty() || position == null) {
            return null;
        }

        units.sortByDistanceTo(position, true);
        return units.first();
    }

    /**
     * Returns first unit being base. For your units this is most likely your main base, for enemy it will be
     * first discovered base.
     */
    public static Unit mainBase() {
        if (_cached_mainBase == null) {
            Units bases = ourBases().units();
            _cached_mainBase = bases.isEmpty() ? null : bases.first();
        }

        return _cached_mainBase;
    }

    /**
     * Returns second (natural) base <b>or if we have only one base</b>, it returns the only base we have.
     */
    public static Unit secondBaseOrMainIfNoSecond() {
        Collection<Unit> bases = SelectUnits.ourBases().list();
        if (bases.size() <= 1) {
            return bases.iterator().next();
        }
        else {
            Iterator<Unit> iterator = bases.iterator();
            iterator.next();
            return iterator.next();
        }
    }

    /**
     * Returns first idle our unit of given type or null if no idle units found.
     */
    public static Unit ourOneIdle(UnitType type) {
        for (Unit unit : Atlantis.getBwapi().getMyUnits()) {
            if (unit.isCompleted() && unit.isIdle() && unit.getType().equals(type)) {
                return unit;
            }
        }
        return null;
    }

    // =========================================================
    // Auxiliary methods
    /**
     * Returns <b>true</b> if current selection contains at least one unit.
     */
    public boolean anyExists() {
        return !units.isEmpty();
    }

    /**
     * Returns first unit that matches previous conditions or null if no units match conditions.
     */
    public Unit first() {
        return units.isEmpty() ? null : units.first();
    }

    // =========================================================
    // Operations on set of units
    /**
     * @return all units except for the given one
     */
    public SelectUnits exclude(Unit unitToExclude) {
        units.removeUnit(unitToExclude);
        return this;
    }

    @SuppressWarnings("unused")
    private SelectUnits filterOut(Collection<Unit> unitsToRemove) {
        units.removeUnits(unitsToRemove);
        return this;
    }

    // private SelectUnits filterOut(Unit unitToRemove) {
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
    private SelectUnits filterAllBut(Unit unitToLeave) {
        Iterator<Unit> unitsIterator = units.iterator();
        while (unitsIterator.hasNext()) {
            Unit unit = unitsIterator.next();
            if (unitToLeave != unit) {
                units.removeUnit(unit);
            }
        }
        return this;
    }

    @Override
    public String toString() {
        return units.toString();
    }

    // =========================================================
    // Get results
    /**
     * Selects units that match all previous criteria. <b>Units</b> class is used as a wrapper for result. See
     * its javadoc too learn what it can do.
     */
    public Units units() {
        return units;
    }

    /**
     * Selects units as an iterable collection (list).
     */
    public Collection<Unit> list() {
        return units().list();
    }

    /**
     * Returns number of units matching all previous conditions.
     */
    public int count() {
        return units.size();
    }

}
