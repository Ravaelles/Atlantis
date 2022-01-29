package atlantis.units.select;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.terran.repair.ARepairAssignments;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Units;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Selection {

    protected List<AUnit> data;

    /**
     * To cache a value we need all previous filters so in the end it looks like:
     * "our.buildings.inRadius:2,10"
     */
    protected String currentCachePath = null;

    // =========================================================

    protected Selection(Collection<? extends AUnit> unitsData, String initCachePath) {
        this.data = new ArrayList<>(unitsData);
        this.currentCachePath = initCachePath;
    }

    // === Cache ===============================================

    protected String addToCachePath(String method) {
        if (currentCachePath != null) {
            currentCachePath += (currentCachePath.length() > 0 ? "." : "") + method;
        }
//        System.out.println("path = " + currentCachePath);
        return currentCachePath;
    }

    // === Filter units ========================================

    /**
     * Selects only units of given type(s).
     */
    public Selection ofType(AUnitType... types) {
        return clone((unit -> !typeMatches(unit, types)));
    }

    /**
     * Returns whether the type in unit matches one in the haystack
     */
    private boolean typeMatches(AUnit unit, AUnitType... haystack) {
        for (AUnitType type : haystack) {
            if (
                    type != null && unit.is(type)
                    || (unit != null && type != null && unit.is(AUnitType.Zerg_Egg) && type.equals(unit.buildType()))
            ) {
                return true;
            }
        }
        return false;
    }

    public Selection add(Selection otherSelection) {
        data.addAll(otherSelection.data);
        return clone();
    }

    /**
     * Returns all units that are closer than <b>maxDist</b> tiles from given <b>otherUnit</b>.
     */
    public Selection inRadius(double maxDist, AUnit unit) {
        return Select.cache.get(
                addToCachePath("inRadius:" + maxDist + ":" + unit.id()),
                0,
                () -> {
//                    data.removeIf(u -> u.distTo(unit) > maxDist);
                    return clone((u -> u.distTo(unit) > maxDist));
                }
        );
    }

    /**
     * Returns all units that are closer than <b>maxDist</b> tiles from given <b>position</b>.
     */
    public Selection inRadius(double maxDist, HasPosition unitOrPosition) {
        return Select.cache.get(
                addToCachePath("inRadius:" + maxDist + ":" + unitOrPosition),
                0,
                () -> {
                    Iterator unitsIterator = data.iterator();// units.iterator();
                    while (unitsIterator.hasNext()) {
                        AUnit unit = (AUnit) unitsIterator.next();
                        if (unit.distTo(unitOrPosition) > maxDist) {
                            unitsIterator.remove();
                        }
                    }
                    return clone();
                }
        );
    }

    /**
     * Returns whether the type in unit matches one in the haystack
     */
//    private boolean typeMatches(FoggedUnit unit, AUnitType... haystack) {
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
                if (unit.is(type) || (unit.is(AUnitType.Zerg_Egg) && type.equals(unit.buildType()))) {
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
        return clone((unit -> !unit.isVisibleOnMap()));
    }

    /**
     * Selects only those units which are visible and not cloaked.
     */
    public Selection effVisible() {
        return clone((unit -> !unit.isDetected() && unit.effCloaked()));
    }

    /**
     * Selects only those units which are hidden, cloaked / burrowed. Not possible to be attacked.
     */
    public Selection effCloaked() {
        return clone((unit -> !unit.effCloaked()));
    }

    public Selection detectors() {
        data.removeIf(unit -> !unit.is(
                AUnitType.Protoss_Photon_Cannon,
                AUnitType.Protoss_Observer,
                AUnitType.Terran_Missile_Turret,
                AUnitType.Terran_Science_Vessel,
                AUnitType.Zerg_Overlord,
                AUnitType.Zerg_Spore_Colony
        ));
        return clone();
    }

    public Selection tanksSieged() {
        return clone((unit -> !unit.is(AUnitType.Terran_Siege_Tank_Siege_Mode)));
    }

    public Selection tanks() {
        data.removeIf(unit -> !unit.is(
                AUnitType.Terran_Siege_Tank_Siege_Mode,
                AUnitType.Terran_Siege_Tank_Tank_Mode
        ));
        return clone();
    }

    public Selection groundUnits() {
        return clone((unit -> !unit.isGroundUnit()));
    }

    /**
     * Selects units that are gathering minerals.
     */
    public Selection gatheringMinerals(boolean onlyNotCarryingMinerals) {
        return clone((unit -> !unit.isGatheringMinerals() || (onlyNotCarryingMinerals && unit.isCarryingMinerals())));
    }

    public Selection notGathering() {
        return clone((unit -> unit.isGatheringMinerals() || unit.isGatheringGas()));
    }

    public Selection notGatheringGas() {
        return clone((unit -> unit.isGatheringGas()));
    }

    /**
     * Selects units being infantry.
     */
    public Selection organic() {
        return clone((unit -> !unit.type().isOrganic()));
    }

    public Selection transports(boolean excludeOverlords) {
        return clone((unit -> !unit.type().isTransport() || (excludeOverlords && !unit.type().isTransportExcludeOverlords())));
    }

    /**
     * Selects bases only (including Lairs and Hives).
     */
    public Selection bases() {
        return clone((unit -> !unit.type().isBase()));
    }

    public Selection workers() {
        return clone((unit -> !unit.isWorker()));
    }

    public Selection air() {
        return clone((unit -> !unit.isAir()));
    }

    /**
     * Selects melee units that is units which have attack range at most 1 tile.
     */
    public Selection melee() {
        return clone((unit -> !unit.isMelee()));
//        return clone(unit -> !unit.isMelee());
    }

    /**
     * Selects only units that do not currently have max hit points.
     */
    public Selection ranged() {
        return clone(unit -> !unit.isRanged());
    }

    public Selection wounded() {
        return clone((unit -> !unit.isWounded()));
    }

    public Selection criticallyWounded() {
        return clone((unit -> unit.hp() >= 21));
    }

    public Selection terranInfantryWithoutMedics() {
        return clone((unit -> !unit.isTerranInfantryWithoutMedics()));
    }

    /**
     * Selects only buildings.
     */
    public Selection buildings() {
        return clone((unit -> !unit.type().isBuilding() && !unit.type().isAddon()));
    }

    /**
     * Fogged units may have no position if e.g. unit moved and we don't know where it is.
     */
    public Selection havingPosition() {
        return clone((unit -> unit.position() == null));
    }

    public int totalHp() {
        return data.stream()
                .map(AUnit::hp)
                .reduce(0, Integer::sum);
    }

    public Selection combatUnits() {
        return clone((unit -> !unit.isCompleted() || !unit.type().isCombatUnit()));
    }

    /**
     * Selects military buildings like Photon Cannon, Bunker, Spore Colony, Sunken Colony
     */
    public Selection combatBuildings(boolean includeCreepColonies) {
        return clone((unit -> includeCreepColonies ? !unit.type().isCombatBuildingOrCreepColony() : !unit.type().isCombatBuilding()));
    }

    public Selection onlyCompleted() {
        return clone((unit -> !unit.isCompleted()));
    }

    /**
     * Selects only those Terran vehicles/buildings that can be repaired so it has to be:<br />
     * - mechanical<br />
     * - not 100% healthy<br />
     */
    public Selection repairable(boolean checkIfWounded) {
        return clone((unit -> !unit.isCompleted() || !unit.type().isMechanical() || (checkIfWounded && !unit.isWounded())));
    }

    public Selection burrowed() {
        return clone((unit -> !unit.isBurrowed()));
    }

    public Selection loaded() {
        return clone((unit -> !unit.isLoaded()));
    }

    public Selection unloaded() {
        return clone((AUnit::isLoaded));
    }

    /**
     * Selects these units (makes sense only for workers) who aren't assigned to repair any other unit.
     */
    public Selection notRepairing() {
        return clone((unit -> unit.isRepairing() || ARepairAssignments.isRepairerOfAnyKind(unit)));
    }

    /**
     * Selects these transport/bunker units which have still enough room inside.
     */
    public Selection havingSpaceFree(int spaceRequired) {
        return clone((unit -> unit.spaceRemaining() < spaceRequired));
    }

    public Selection havingEnergy(int minEnergy) {
        return clone((unit -> !unit.energy(minEnergy)));
    }

    public Selection notHavingHp(int hp) {
        return clone((unit -> unit.hpMoreThan(hp)));
    }

    /**
     * Selects these units (makes sense only for workers) who aren't assigned to construct anything.
     */
    public Selection notConstructing() {
        return clone((unit -> unit.isConstructing() || unit.isMorphing() || unit.isBuilder()));
    }

    public Selection free() {
        return clone((u -> u.isBusy() || u.isLifted()));
    }

    /**
     * Selects these units which are not scouts.
     */
    public Selection notScout() {
        return clone((AUnit::isScout));
    }

    /**
     * Selects these units which are not carrynig nor minerals, nor gas.
     */
    public Selection notCarrying() {
        return clone((unit -> unit.isCarryingGas() || unit.isCarryingMinerals()));
    }

    public Selection excludeTypes(AUnitType ...types) {
        return clone((unit -> unit.is(types)));
    }

    public Selection hasPathFrom(HasPosition fromPosition) {
        return clone((unit -> !unit.hasPathTo(fromPosition)));
    }

//    public Selection canShootAt(AUnit targetUnit) {
//        data.removeIf(unit -> !unit.isCompleted() || !unit.isAlive() || !unit.hasWeaponRange(targetUnit, 0));
//        return clone();
//    }
//
//    public Selection canShootAt(AUnit targetUnit, double shootingRangeBonus) {
//        data.removeIf(unit -> !unit.isCompleted() || !unit.isAlive() || !unit.hasWeaponRange(targetUnit, shootingRangeBonus));
//        return clone();
//    }
//
//    public Selection canShootAt(APosition position, double shootingRangeBonus) {
//        data.removeIf(unit -> !unit.isCompleted() || !unit.isAlive() || !unit.hasGroundWeaponRange(position, shootingRangeBonus));
//        return clone();
//    }

//    public Selection canBeAttackedBy(AUnit attacker, boolean checkShootingRange, boolean checkVisibility) {
//        data.removeIf(target -> !attacker.canAttackTarget(target, checkShootingRange, checkVisibility, false, 0));
//        return clone();
//    }

//    public Selection canBeAttackedBy(AUnit attacker) {
//        data.removeIf(target -> !attacker.canAttackTarget(
//                target, true, true, false, 0
//        ));
//        return clone();
//    }

    public Selection canBeAttackedBy(AUnit attacker, double extraMargin) {
        data.removeIf(target -> !attacker.canAttackTarget(
                target, true, true, false, extraMargin
        ));
        return clone();
    }

    /**
     * Selects only those units from current selection, which can be both <b>attacked by</b> given unit (e.g.
     * Zerglings can't attack Overlord) and are <b>in shot range</b> to the given <b>unit</b>.
     */
    public Selection canAttack(AUnit target, boolean checkShootingRange, boolean checkVisibility, double safetyMargin) {
        data.removeIf(attacker -> !attacker.canAttackTarget(
                target, checkShootingRange, checkVisibility, false, safetyMargin
        ));
        return clone();
    }

    public Selection canAttack(AUnit target, double safetyMargin) {
        data.removeIf(attacker -> !attacker.canAttackTarget(
                target, true, true, false, safetyMargin
        ));
        return clone();
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

    public AUnit second() {
        return data.size() < 2 ? null : (AUnit) data.get(1);
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
        return clone();
    }

    public Selection exclude(Collection unitsToExclude) {
        data.removeAll(unitsToExclude);
        return clone();
    }

    /**
     * Reverse the order in which units are returned.
     */
    public Selection reverse() {
        Collections.reverse(data);
        return clone();
    }

    /**
     * Returns a AUnit out of an entity that is either a AUnit or FoggedUnit
     *
     * @param unitOrData
     * @return
     */
//    private AUnit unitFrom(Object unitOrData) {
//        return (unitOrData instanceof AUnit ? (AUnit) unitOrData : ((FoggedUnit) unitOrData).getUnit());
//    }

    /**
     * Returns a UnitData out of an entity that is either a AUnit or UnitData
     *
     * @param unitOrData
     * @return
     */
    private AUnit dataFrom(Object unitOrData) {
//        return (unitOrData instanceof FoggedUnit ? (FoggedUnit) unitOrData : new FoggedUnit((AUnit) unitOrData));
//        if (unitOrData instanceof FoggedUnit) {
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
        return clone();
    }

    @SuppressWarnings("unused")
    private Selection filterAllBut(AUnit unitToLeave) {
        for (AUnit unit : data) {
            if (unitToLeave != unit) {
                data.remove(unit);
            }
        }
        return clone();
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("Units (" + data.size() + "):\n");

        for (AUnit unit : data) {
            string.append("   - ").append(unit.type()).append(" (ID:").append(unit.id()).append(")\n");
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
     * Returns result as an <b>Units</b> object, which contains multiple useful methods to handle set of
     * units.
     */
    public Units units() {
        return new Units(this.data);
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

    public boolean empty() {
        return data.size() == 0;
    }

    public boolean isNotEmpty() {
        return data.size() > 0;
    }

    public boolean notEmpty() {
        return data.size() > 0;
    }

    /**
     * Returns number of units matching all previous conditions.
     */
    public int size() {
        return data.size();
    }

    public List<AUnit> sortDataByDistanceTo(final HasPosition position, final boolean nearestFirst) {
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

                double distance1 = p1.distTo(position);
                double distance2 = p2.distTo(position);

                return nearestFirst ? Double.compare(distance1, distance2) : Double.compare(distance2, distance1);
            }
        });

        return data;
    }

    public List<AUnit> sortDataByDistanceTo(final AUnit unit, final boolean nearestFirst) {
        Collections.sort(data, new Comparator<AUnit>() {
            @Override
            public int compare(AUnit p1, AUnit p2) {
                if (!(p1 instanceof HasPosition)) {
                    throw new RuntimeException("Invalid comparison: " + p1);
                }
                if (!(p2 instanceof HasPosition)) {
                    throw new RuntimeException("Invalid comparison: " + p2);
                }

                double distance1 = unit.distTo(p1);
                double distance2 = unit.distTo(p2);

                return nearestFirst ? Double.compare(distance1, distance2) : Double.compare(distance2, distance1);
            }
        });

        return data;
    }

    public Selection sortByHealth() {
        if (data.isEmpty()) {
            return new Selection(new ArrayList<>(), "");
        }

        data.sort(Comparator.comparingDouble(AUnit::hpPercent));

        return clone();
    }

    public Selection clone() {
        return new Selection(this.data, currentCachePath);
    }

    public Selection clone(Predicate<AUnit> newDataPredicate) {
        List<AUnit> newData = new ArrayList<>(data);
        newData.removeIf(newDataPredicate);
        return new Selection(newData, currentCachePath);
    }

    public APosition center() {
        return units().average();
    }

    public Selection removeDuplicates() {
        data = data.stream().distinct().collect(Collectors.toList());
        return clone();
    }

    public void print() {
        print(null);
    }

    public void print(String message) {
        System.out.println("=== " + (message != null ? message : currentCachePath) + " (" + size() + ") ===");
        for (AUnit unit : data) {
            System.out.println(unit);
        }
        System.out.println();
    }
}
