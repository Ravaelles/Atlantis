package atlantis.wrappers;

import java.util.Collection;

import jnibwapi.Position;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;
import atlantis.Atlantis;
import atlantis.AtlantisConfig;

/**
 * This class allows to easily select units e.g. to select one of your Marines,
 * nearest to given location, you would run:<br />
 * <p>
 * <b> SelectUnits.our().ofType(UnitTypes.Terran_Marine).nearestTo(somePlace)
 * </b>
 * </p>
 * It uses nice flow and every next method filters out units that do not fulfill
 * certain conditions.
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

	public static SelectUnits enemy() {
		Units units = new Units();

		for (Unit unit : Atlantis.getBwapi().getEnemyUnits()) {
			if (unit.isAlive()) {
				units.addUnit(unit);
			}
		}

		return new SelectUnits(units);
	}

	public static SelectUnits neutral() {
		Units units = new Units();

		units.addUnits(Atlantis.getBwapi().getNeutralUnits());

		return new SelectUnits(units);
	}

	public static SelectUnits minerals() {
		Units units = new Units();

		units.addUnits(Atlantis.getBwapi().getNeutralUnits());
		SelectUnits selectUnits = new SelectUnits(units);

		return selectUnits.ofType(UnitTypes.Resource_Mineral_Field);
	}

	public static SelectUnits from(Units units) {
		SelectUnits selectUnits = new SelectUnits(units);
		return selectUnits;
	}

	public static SelectUnits from(Collection<Unit> unitsCollection) {
		Units units = new Units();
		units.addUnits(unitsCollection);

		SelectUnits selectUnits = new SelectUnits(units);
		return selectUnits;
	}

	// public static SelectUnits all() {
	// Units units = new Units();
	//
	// units.addUnits(xvr.getBwapi().getMyUnits());
	//
	// return new SelectUnits(units);
	// }

	// =========================================================
	// Get results

	public Units units() {
		return units;
	}

	public Collection<Unit> list() {
		return units().list();
	}

	// =====================================================================
	// Filter units

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

	public SelectUnits ofType(UnitTypes type1Allowed, UnitTypes type2Allowed) {
		for (Unit unit : units.list()) {
			if (!unit.getType().equals(type1Allowed) && !unit.getType().equals(type2Allowed)) {
				filterOut(unit);
			}
		}

		return this;
	}

	public SelectUnits idle() {
		for (Unit unit : units.list()) {
			if (!unit.isIdle()) {
				filterOut(unit);
			}
		}

		return this;
	}

	public SelectUnits buildings() {
		for (Unit unit : units.list()) {
			if (!unit.isBuilding()) {
				filterOut(unit);
			}
		}
		return this;
	}

	public SelectUnits toRepair() {
		for (Unit unit : units.list()) {
			if (!unit.isRepairableMechanically() || unit.isFullyHealthy() || !unit.isCompleted()) {
				filterOut(unit);
			}
		}
		return this;
	}

	// =========================================================
	// Hi-level methods

	public static SelectUnits ourBases() {
		return our().ofType(AtlantisConfig.BASE);
	}

	public static SelectUnits ourWorkers() {
		return our().ofType(AtlantisConfig.WORKER);
	}

	public static SelectUnits ourTanks() {
		return our().ofType(UnitTypes.Terran_Siege_Tank_Siege_Mode, UnitTypes.Terran_Siege_Tank_Tank_Mode);
	}

	public static SelectUnits ourTanksSieged() {
		return our().ofType(UnitTypes.Terran_Siege_Tank_Siege_Mode);
	}

	// =========================================================
	// Localization-related methods

	/**
	 * Returns closest unit to given <b>position</b>.
	 */
	public Unit nearestTo(Position position) {
		units.sortByDistanceTo(position, true);
		// return filterAllBut(units.first());
		return units.first();
	}

	/**
	 * Returns all units that are closer than <b>maxDist</b> tiles from given
	 * <b>position</b>.
	 */
	public SelectUnits inRadius(double maxDist, Position position) {
		for (Unit unit : units.list()) {
			if (position.distanceTo(unit) > maxDist) {
				filterOut(unit);
			}
		}

		return this;
	}

	/**
	 * Returns first unit being base. For your units this is most likely your
	 * main base, for enemy it will be first discovered base.
	 */
	public static Unit firstBase() {
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

	public boolean anyExists() {
		return !units.isEmpty();
	}

}
