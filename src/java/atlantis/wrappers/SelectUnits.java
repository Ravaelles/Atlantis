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

		// System.out.println("units in list:");
		// for (Unit unit : units.list()) {
		// System.out.println(unit);
		// }
		// System.out.println();

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
			if (position.distanceTo(unit) > maxDist) {
				filterOut(unit);
			}
		}

		return this;
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
	 * Selects all our workers that is Terran SCV or Zerg Drones or Protoss Probes.
	 */
	public static SelectUnits ourWorkers() {
		return our().ofType(AtlantisConfig.WORKER);
	}

	/**
	 * Selects all our finished buildings.
	 */
	public static SelectUnits ourBuildings() {
		return our().buildings();
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
	 * From all units currently in selection, returns closest unit to given <b>position</b>.
	 */
	public Unit nearestTo(Unit unit) {
		return nearestTo(unit.getPosition());
	}

	/**
	 * Returns first unit being base. For your units this is most likely your main base, for enemy it will be first
	 * discovered base.
	 */
	public static Unit mainBase() {
		Units bases = ourBases().units();
		return bases.isEmpty() ? null : bases.first();
	}

	// =========================================================

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
	// Auxiliary methods

	/**
	 * Returns <b>true</b> if current selection contains at least one unit.
	 */
	public boolean anyExists() {
		return !units.isEmpty();
	}

}
