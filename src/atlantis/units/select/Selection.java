package atlantis.units.select;

import atlantis.position.HasPosition;
import atlantis.position.PositionUtil;
import atlantis.repair.ARepairAssignments;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Units;
import atlantis.util.A;
import bwapi.Position;

import java.util.*;

public class Selection {

    protected final List<AUnit> data;

    /**
     * To cache a value we need all previous filters so in the end it looks like:
     * "our.buildings.inRadius:2,10"
     */
    protected String currentCachePath = null;

    // =========================================================

    protected Selection(Collection<AUnit> unitsData, String initCachePath) {
        this.data = new ArrayList<>(unitsData);
        this.currentCachePath = initCachePath;
    }

    // === Cache ===============================================

    protected String addToCachePath(String method) {
        currentCachePath += (currentCachePath.length() > 0 ? "." : "") + method;
//        System.out.println("path = " + currentCachePath);
        return currentCachePath;
    }

    // === Filter units ========================================

    /**
     * Selects only units of given type(s).
     */
    public Selection ofType(AUnitType... types) {
        data.removeIf(unit -> !typeMatches(unit, types));
        return this;
    }

    /**
     * Returns whether the type in unit matches one in the haystack
     */
    private boolean typeMatches(AUnit unit, AUnitType... haystack) {
        for (AUnitType type : haystack) {
            if (
                    type != null && unit.is(type)
                    || (unit != null && type != null && unit.is(AUnitType.Zerg_Egg) && type.equals(unit.getBuildType()))
            ) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns all units that are closer than <b>maxDist</b> tiles from given <b>otherUnit</b>.
     */
    public Selection inRadius(double maxDist, AUnit unit) {
        return Select.cache.get(
                addToCachePath("inRadius:" + maxDist + ":" + unit.id()),
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
    public Selection inRadius(double maxDist, HasPosition unitOrPosition) {
        return Select.cache.get(
                addToCachePath("inRadius:" + maxDist + ":" + unitOrPosition),
                1,
                () -> {
                    Iterator unitsIterator = data.iterator();// units.iterator();
                    while (unitsIterator.hasNext()) {
                        AUnit unit = (AUnit) unitsIterator.next();
                        if (unit.distTo(unitOrPosition) > maxDist) {
                            unitsIterator.remove();
                        }
                    }

                    return this;
                }
        );
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
        for (AUnit unit : data) {
            boolean typeMatches = false;
            for (AUnitType type : types) {
                if (unit.is(type) || (unit.is(AUnitType.Zerg_Egg) && type.equals(unit.getBuildType()))) {
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
    public Selection visible() {
        data.removeIf(unit -> !unit.isVisibleOnMap());
        return this;
    }

    /**
     * Selects only those units which are visible and not cloaked.
     */
    public Selection effVisible() {
        data.removeIf(unit -> unit.effCloaked() || !unit.isVisibleOnMap());
        return this;
    }

    /**
     * Selects only those units which are hidden, cloaked / burrowed. Not possible to be attacked.
     */
    public Selection effCloaked() {
        data.removeIf(unit -> !unit.effCloaked());
        return this;
    }

    public Selection cloakedButEffVisible() {
        data.removeIf(unit -> !unit.effCloaked());
        return this;
    }

    public Selection detectors() {
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

    public Selection tanksSieged() {
        data.removeIf(unit -> !unit.isType(AUnitType.Terran_Siege_Tank_Siege_Mode));
        return this;
    }

    public Selection tanks() {
        data.removeIf(unit -> !unit.isType(
                AUnitType.Terran_Siege_Tank_Siege_Mode,
                AUnitType.Terran_Siege_Tank_Tank_Mode
        ));
        return this;
    }

    public Selection groundUnits() {
        data.removeIf(unit -> !unit.isGroundUnit());
        return this;
    }

    /**
     * Selects units that are gathering minerals.
     */
    public Selection gatheringMinerals(boolean onlyNotCarryingMinerals) {
        data.removeIf(unit -> !unit.isGatheringMinerals() || (onlyNotCarryingMinerals && unit.isCarryingMinerals()));
        return this;
    }

    public Selection notGathering() {
        data.removeIf(unit -> unit.isGatheringMinerals() || unit.isGatheringGas());
        return this;
    }

    public Selection notGatheringGas() {
        data.removeIf(unit -> unit.isGatheringGas());
        return this;
    }

    /**
     * Selects units being infantry.
     */
    public Selection organic() {
        data.removeIf(unit -> !unit.type().isOrganic());
        return this;
    }

    public Selection transports(boolean excludeOverlords) {
        data.removeIf(unit -> !unit.type().isTransport() || (excludeOverlords && !unit.type().isTransportExcludeOverlords()));
        return this;
    }

    /**
     * Selects bases only (including Lairs and Hives).
     */
    public Selection bases() {
        data.removeIf(unit -> !unit.type().isBase());
        return this;
    }

    public Selection workers() {
        data.removeIf(unit -> !unit.isWorker());
        return this;
    }

    public Selection air() {
        data.removeIf(unit -> !unit.isAirUnit());
        return this;
    }

    /**
     * Selects melee units that is units which have attack range at most 1 tile.
     */
    public Selection melee() {
        data.removeIf(unit -> !unit.type().isMeleeUnit());
        return this;
    }

    /**
     * Selects only units that do not currently have max hit points.
     */
    public Selection wounded() {
        data.removeIf(unit -> !unit.isWounded());
        return this;
    }

    /**
     * Selects only units that do not currently have max hit points.
     */
    public Selection ranged() {
        data.removeIf(unit -> !unit.isRanged());
        return this;
    }

    /**
     * Selects only buildings.
     */
    public Selection buildings() {
        data.removeIf(unit -> !unit.type().isBuilding() && !unit.type().isAddon());
        return this;
    }

    public Selection combatUnits() {
        data.removeIf(unit -> !unit.isCompleted() || !unit.type().isCombatUnit());
        return this;
    }

    /**
     * Selects military buildings like Photon Cannon, Bunker, Spore Colony, Sunken Colony
     */
    public Selection combatBuildings() {
        data.removeIf(unit -> !unit.type().isCombatBuilding());
        return this;
    }

    /**
     * Selects only those Terran vehicles/buildings that can be repaired so it has to be:<br />
     * - mechanical<br />
     * - not 100% healthy<br />
     */
    public Selection repairable(boolean checkIfWounded) {
        data.removeIf(unit -> !unit.isCompleted() || !unit.type().isMechanical() || (checkIfWounded && !unit.isWounded()));
        return this;
    }

    public Selection burrowed() {
        data.removeIf(unit -> !unit.isBurrowed());
        return this;
    }

    public Selection loaded() {
        data.removeIf(unit -> !unit.isLoaded());
        return this;
    }

    public Selection unloaded() {
        data.removeIf(AUnit::isLoaded);
        return this;
    }

    /**
     * Selects these units (makes sense only for workers) who aren't assigned to repair any other unit.
     */
    public Selection notRepairing() {
        data.removeIf(unit -> unit.isRepairing() || ARepairAssignments.isRepairerOfAnyKind(unit));
        return this;
    }

    /**
     * Selects these transport/bunker units which have still enough room inside.
     */
    public Selection havingSpaceFree(int spaceRequired) {
        data.removeIf(unit -> unit.spaceRemaining() < spaceRequired);
        return this;
    }

    /**
     * Selects these units (makes sense only for workers) who aren't assigned to construct anything.
     */
    public Selection notConstructing() {
        data.removeIf(unit -> unit.isConstructing() || unit.isMorphing() || unit.isBuilder());
        return this;
    }

    public Selection free() {
        data.removeIf(u -> u.isBusy() || u.isLifted());
        return this;
    }

    /**
     * Selects these units which are not scouts.
     */
    public Selection notScout() {
        data.removeIf(AUnit::isScout);
        return this;
    }

    /**
     * Selects these units which are not carrynig nor minerals, nor gas.
     */
    public Selection notCarrying() {
        data.removeIf(unit -> unit.isCarryingGas() || unit.isCarryingMinerals());
        return this;
    }

    public Selection excludeTypes(AUnitType ...types) {
        data.removeIf(unit -> unit.is(types));
        return this;
    }

//    public Selection canShootAt(AUnit targetUnit) {
//        data.removeIf(unit -> !unit.isCompleted() || !unit.isAlive() || !unit.hasWeaponRange(targetUnit, 0));
//        return this;
//    }
//
//    public Selection canShootAt(AUnit targetUnit, double shootingRangeBonus) {
//        data.removeIf(unit -> !unit.isCompleted() || !unit.isAlive() || !unit.hasWeaponRange(targetUnit, shootingRangeBonus));
//        return this;
//    }
//
//    public Selection canShootAt(APosition position, double shootingRangeBonus) {
//        data.removeIf(unit -> !unit.isCompleted() || !unit.isAlive() || !unit.hasGroundWeaponRange(position, shootingRangeBonus));
//        return this;
//    }

//    public Selection canBeAttackedBy(AUnit attacker, boolean checkShootingRange, boolean checkVisibility) {
//        data.removeIf(target -> !attacker.canAttackTarget(target, checkShootingRange, checkVisibility, false, 0));
//        return this;
//    }

//    public Selection canBeAttackedBy(AUnit attacker) {
//        data.removeIf(target -> !attacker.canAttackTarget(
//                target, true, true, false, 0
//        ));
//        return this;
//    }

    public Selection canBeAttackedBy(AUnit attacker, double safetyMargin) {
        data.removeIf(target -> !attacker.canAttackTarget(
                target, true, true, false, safetyMargin
        ));
        return this;
    }

    /**
     * Selects only those units from current selection, which can be both <b>attacked by</b> given unit (e.g.
     * Zerglings can't attack Overlord) and are <b>in shot range</b> to the given <b>unit</b>.
     */
    public Selection canAttack(AUnit target, boolean checkShootingRange, boolean checkVisibility, double safetyMargin) {
        data.removeIf(attacker -> !attacker.canAttackTarget(
                target, checkShootingRange, checkVisibility, false, safetyMargin
        ));
        return this;
    }

    public Selection canAttack(AUnit target, double safetyMargin) {
        data.removeIf(attacker -> !attacker.canAttackTarget(
                target, true, true, false, safetyMargin
        ));
        return this;
    }

    public Selection inShootRangeOf(AUnit attacker) {
        return canBeAttackedBy(attacker, 0);
    }

    public Selection inShootRangeOf(double shootingRangeBonus, AUnit attacker) {
        return canBeAttackedBy(attacker, shootingRangeBonus);
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
//    public static Selection ourEggs() {
//        Selection selectedUnits = Select.ourIncludingUnfinished();
//        selectedUnits.list().removeIf(unit -> !unit.is(AUnitType.Zerg_Egg));
//        return selectedUnits;
//    }

    // =========================================================
    // Localization-related methods

    /**
     * From all units currently in selection, returns closest unit to given <b>position</b>.
     * @return
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

    // =========================================================
    // Special retrieve
    /**
     * Returns <b>true</b> if current selection contains at least one unit.
     */
    public boolean anyExists() {
        return !data.isEmpty();
    }

    /**
     * Returns first unit that matches previous conditions or null.
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

        for (Iterator it = data.iterator(); it.hasNext();) {
            AUnit unit = (AUnit) it.next();
            if (unit.hp() < lowestHealth) {
                lowestHealth = unit.hp();
                lowestHealthUnit = unit;
            }
        }

        return lowestHealthUnit;
    }

    public boolean areAllBusy() {
        for (Iterator<AUnit> it = (Iterator) data.iterator(); it.hasNext();) {
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
    public Selection exclude(AUnit unitToExclude) {
        data.remove(unitToExclude);
        return this;
    }

    public Selection exclude(Collection unitsToExclude) {
        data.removeAll(unitsToExclude);
        return this;
    }

    /**
     * Reverse the order in which units are returned.
     */
    public Selection reverse() {
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
    private AUnit dataFrom(Object unitOrData) {
//        return (unitOrData instanceof AFoggedUnit ? (AFoggedUnit) unitOrData : new AFoggedUnit((AUnit) unitOrData));
//        if (unitOrData instanceof AFoggedUnit) {
//            return (T) unitOrData;
//        }
//        else
        if (unitOrData instanceof AUnit) {
            return (AUnit) unitOrData;
        }

        throw new RuntimeException("Invalid dataFrom type");
    }

    @SuppressWarnings("unused")
    private Selection filterOut(Collection unitsToRemove) {
        data.removeAll(unitsToRemove);
        return this;
    }

    @SuppressWarnings("unused")
    private Selection filterAllBut(AUnit unitToLeave) {
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
        return data;
    }

    /**
     * Selects units as an iterable collection (list).
     */
    public List<AUnit> listUnits() {
        return data;
    }

    /**
     * Returns result as an <b>Units</b> object, which contains multiple useful methods to handle set of
     * units.
     */
    public Units units() {
        Units units = new Units(this.data);
//        units.addUnits((Collection) this.data);
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
    public List sortDataByDistanceTo(final HasPosition position, final boolean nearestFirst) {
//        if (position == null) {
//            return null;
//        }

        Collections.sort(data, new Comparator<HasPosition>() {
            @Override
            public int compare(HasPosition p1, HasPosition p2) {
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

    public List sortDataByDistanceTo(final AUnit unit, final boolean nearestFirst) {
        Collections.sort(data, new Comparator<AUnit>() {
            @Override
            public int compare(AUnit p1, AUnit p2) {
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

    public List sortByHealth() {
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

    public Selection clone() {
        return new Selection(this.data, currentCachePath);
    }

}
