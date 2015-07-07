package atlantis.wrappers;

import java.util.Collection;

import jnibwapi.Position;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;
import atlantis.Atlantis;
import atlantis.AtlantisConfig;

/**
 * This class allows to easily select units e.g. to select one of your Marines, nearest to given location, you would
 * run:<br />
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
			if (unit.isAlive() && unit.isCompleted() && !unit.isSpiderMine()) {
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
			if (unit.isAlive() && unit.isCompleted() && !unit.isSpiderMine() && !unit.isType(AtlantisConfig.WORKER)) {
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
	 * Selects all visible enemy units.
	 */
	public static SelectUnits enemy() {
		Units units = new Units();

		for (Unit unit : Atlantis.getBwapi().getEnemyUnits()) {
			if (unit.isAlive()) {
				units.addUnit(unit);
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
	 * Selects all minerals on map.
	 */
	public static SelectUnits minerals() {
		Units units = new Units();

		units.addUnits(Atlantis.getBwapi().getNeutralUnits());
		SelectUnits selectUnits = new SelectUnits(units);

		return selectUnits.ofType(UnitTypes.Resource_Mineral_Field);
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
		units.addUnits(unitsCollection);

		SelectUnits selectUnits = new SelectUnits(units);
		return selectUnits;
	}

	/**
	 * Returns all units that are closer than <b>maxDist</b> tiles from given <b>position</b>.
	 */
	public SelectUnits inRadius(double maxDist, Position position) {
		for (Unit unit : units.list()) {
			if (unit.distanceTo(position) > maxDist) {
				filterOut(unit);
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
		for (Unit unit : units.list()) {
			for (UnitType type : types) {
				if (!unit.getType().equals(type)) {
					filterOut(unit);
				}
			}
		}

		return this;
	}

	/**
	 * Selects only those units which are idle. Idle is unit's class flag so be careful with that.
	 */
	public SelectUnits idle() {
		for (Unit unit : units.list()) {
			if (!unit.isIdle()) {
				filterOut(unit);
			}
		}

		return this;
	}

	// /**
	// * Selects units which do currently gather minerals but dont carry it.
	// */
	// public SelectUnits gatheringMineralsButNotCarryingIt() {
	// for (Unit unit : units.list()) {
	// if (!unit.isGatheringMinerals() || unit.isCarryingMinerals()) {
	// filterOut(unit);
	// }
	// }
	//
	// return this;
	// }

	/**
	 * Selects only buildings.
	 */
	public SelectUnits buildings() {
		for (Unit unit : units.list()) {
			if (!unit.isBuilding()) {
				filterOut(unit);
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
		for (Unit unit : units.list()) {
			if (!unit.isRepairableMechanically() || unit.isFullyHealthy() || !unit.isCompleted()) {
				filterOut(unit);
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
			if (!unit.isWorker()) {
				selectedUnits.filterOut(unit);
			}
		}
		return selectedUnits;
	}

	/**
	 * Selects our workers that are free to construct building or repair a unit. That means they mustn't repait any
	 * other unit or construct other building.
	 */
	public static SelectUnits ourWorkersFreeToBuildOrRepair() {
		SelectUnits selectedUnits = ourWorkers();

		for (Unit unit : selectedUnits.list()) {
			if (unit.isConstructing() || unit.isRepairing()) {
				selectedUnits.filterOut(unit);
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
				selectedUnits.filterOut(unit);
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

	// =========================================================
	// Localization-related methods

	/**
	 * From all units currently in selection, returns closest unit to given <b>position</b>.
	 */
	public Unit nearestTo(Position position) {
		units.sortByDistanceTo(position, true);
		// return filterAllBut(units.first());
		return units.first();
	}

	/**
	 * Returns first unit being base. For your units this is most likely your main base, for enemy it will be first
	 * discovered base.
	 */
	public static Unit mainBase() {
		if (_cached_mainBase == null) {
			Units bases = ourBases().units();
			_cached_mainBase = bases.isEmpty() ? null : bases.first();
		}

		return _cached_mainBase;
	}

	/**
	 * Returns first idle our unit of given type or null if no idle units found.
	 */
	public static Unit ourOneIdle(UnitType type) {
		for (Unit unit : Atlantis.getBwapi().getMyUnits()) {
			if (unit.isIdle() && unit.getType().equals(type)) {
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

	@SuppressWarnings("unused")
	private SelectUnits filterOut(Collection<Unit> unitsToRemove) {
		units.removeUnits(unitsToRemove);
		return this;
	}

	private SelectUnits filterOut(Unit unitToRemove) {
		units.removeUnit(unitToRemove);
		return this;
	}

	@SuppressWarnings("unused")
	private SelectUnits filterAllBut(Unit unitToLeave) {
		for (Unit unit : units.list()) {
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
	 * Selects units that match all previous criteria. <b>Units</b> class is used as a wrapper for result. See its
	 * javadoc too learn what it can do.
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

}
