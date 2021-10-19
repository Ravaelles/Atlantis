package atlantis.units;

import atlantis.position.APosition;
import atlantis.position.HasPosition;
import atlantis.position.PositionHelper;
import atlantis.util.A;
import bwapi.Position;
import bwta.BWTA;

import java.util.*;
import java.util.stream.Stream;

/**
 * This class is wrapper for ArrayList<AUnit>. It allows some helpful methods to be executed upon squad of
 * units like sorting etc.
 */
public class Units {

    /**
     * This mapping can be used to store extra values assigned to units e.g. if units represents mineral fields,
     * we can easily store info how many workers are gathering each mineral field thanks to this mapping.
     */
    private final ArrayList<AUnit> units = new ArrayList<>();
    private final Map<AUnit, Double> extraValues = new HashMap<>();

    // =====================================================================

    public Units(Collection<AUnit> units) {
        addUnits(units);
    }

    public Units() {
    }

    // === Base functionality ==============================================

    public Units addUnit(AUnit unitToAdd) {
        units.add(unitToAdd);
        extraValues.put(unitToAdd, null);
        return this;
    }

    public void addUnitWithValue(AUnit unitToAdd, double value) {
        addUnit(unitToAdd);
        setValueFor(unitToAdd, value);
    }

    public Units addUnits(Collection<AUnit> unitsToAdd) {
        for (AUnit unit : unitsToAdd) {
            addUnit(unit);
        }
        return this;
    }

    public Units removeUnits(Collection<AUnit> unitsToRemove) {
        for (AUnit unit : unitsToRemove) {
            units.remove(unit);
            extraValues.remove(unit);
        }
        return this;
    }

    public Units removeUnit(AUnit unitToRemove) {
        units.remove(unitToRemove);
        extraValues.remove(unitToRemove);
        return this;
    }

    public int size() {
        return units.size();
    }

    public boolean isEmpty() {
        return units.isEmpty();
    }

    public boolean isNotEmpty() {
        return !units.isEmpty();
    }

    /**
     * Returns first unit from the set.
     */
    public AUnit first() {
        return isEmpty() ? null : units.get(0);
    }

    /**
     * Returns random unit from the set.
     */
    public AUnit random() {
        return (AUnit) A.getRandomElement(units);
    }
    
    /**
     * Returns unit with <b>N</b>-th index.
     */
    public AUnit get(int index) {
        return units.get(index);
//        Set<AUnit> keySet = units.keySet();
//
//        int currentIndex = 0;
//        for (AUnit unit : keySet) {
//            if (currentIndex == index) {
//                return unit;
//            }
//            else {
//                currentIndex++;
//            }
//        }
//        throw new RuntimeException("Units.get(index) is invalid, shouldn't reach here");
    }

    public boolean has(AUnit unit) {
        return units.contains(unit);
    }

    public boolean contains(AUnit unit) {
        return units.contains(unit);
    }

    // === Special methods =====================================

    public Stream<AUnit> stream() {
        return units.stream();
    }

    /**
     * Shuffle units to have random sequence in the list.
     */
    public Units shuffle() {
        Collections.shuffle(units);
        
        return this;
    }

    // === Value mapping methods ===============================
    
    public void changeValueBy(AUnit unit, double deltaValue) {
        if (has(unit)) {
            extraValues.put(unit, extraValues.get(unit) + deltaValue);
        } else {
            extraValues.put(unit, deltaValue);
        }
    }

    public void setValueFor(AUnit unit, double newValue) {
        if (unit == null) {
            throw new IllegalArgumentException("Units unit shouldn't be null");
        }

        extraValues.put(unit, newValue);
    }

    public double valueFor(AUnit unit) {
        if (units == null || unit == null || !has(unit)) {
            throw new RuntimeException("Should never be here");
        }
        else {
            return extraValues.get(unit);
        }
    }

    public AUnit unitWithLowestValue() {
        return unitWithExtremeValue(true);
    }

    public double lowestValue() {
        return extraValues.get(unitWithLowestValue());
    }

    public AUnit unitWithHighestValue() {
        return unitWithExtremeValue(false);
    }

    private AUnit unitWithExtremeValue(boolean returnLowest) {
        if (units.isEmpty()) {
            return null;
        }

        AUnit bestUnit = null;
        
        // We're interested in MIN
        if (returnLowest) {
            double bestValue = Integer.MAX_VALUE;
            for (AUnit unit : units) {
                if (bestUnit == null || valueFor(unit) < bestValue) {
                    bestValue = valueFor(unit);
                    bestUnit = unit;
                }
            }
        }
        
        // We're interested in MAX
        else {
            double bestValue = Integer.MIN_VALUE;
            for (AUnit unit : units) {
                if (bestUnit == null || valueFor(unit) > bestValue) {
                    bestValue = valueFor(unit);
                    bestUnit = unit;
                }
            }
        }

        return bestUnit;
    }

    // === Location-related ====================================
    
    /**
     * Sorts all units according to the distance to <b>position</b>. If <b>nearestFirst</b> is true, then
     * after sorting first unit will be the one closest to given position.
     */
    public Units sortByDistanceTo(final Position position, final boolean nearestFirst) {
        if (position == null) {
            return null;
        }

        units.sort(new Comparator<AUnit>() {
            @Override
            public int compare(AUnit p1, AUnit p2) {
                if (p1 == null) {
                    return -1;
                }
                if (p2 == null) {
                    return 1;
                }
                double distance1 = p1.getPosition().distanceTo(position);
                double distance2 = p2.getPosition().distanceTo(position);
                if (distance1 == distance2) {
                    return 0;
                } else {
                    return distance1 < distance2 ? (nearestFirst ? -1 : 1) : (nearestFirst ? 1 : -1);
                }
            }
        });

        return this;
    }
    
    /**
     * Sorts all units according to the distance to <b>position</b>. If <b>nearestFirst</b> is true, then
     * after sorting first unit will be the one closest to given position.
     */
    public Units sortByGroundDistanceTo(final Position position, final boolean nearestFirst) {
        if (position == null) {
            return null;
        }

        units.sort(new Comparator<AUnit>() {
            @Override
            public int compare(AUnit p1, AUnit p2) {
                if (!(p1 instanceof HasPosition)) {
                    return -1;
                }
                if (!(p2 instanceof HasPosition)) {
                    return 1;
                }
                double distance1 = BWTA.getGroundDistance(
                        p1.getPosition().toTilePosition(), position.toTilePosition()
                );
                double distance2 = BWTA.getGroundDistance(
                        p2.getPosition().toTilePosition(), position.toTilePosition()
                );
                if (distance1 == distance2) {
                    return 0;
                } else {
                    return distance1 < distance2 ? (nearestFirst ? -1 : 1) : (nearestFirst ? 1 : -1);
                }
            }
        });

        return this;
    }

    /**
     * Returns median PX and median PY for all units.
     */
    public APosition median() {
        if (isEmpty()) {
            return null;
        }
        
        return PositionHelper.getPositionMedian(this);
    }
    
    /**
     * Returns average PX and average PY for all units.
     */
    public APosition average() {
        if (isEmpty()) {
            return null;
        }
        
        return PositionHelper.getPositionAverage(this);
    }
    
    /**
     * Returns average PX and average PY for all units.
     */
    public APosition averageDistanceWeightedTo(AUnit unit, double power) {
        if (isEmpty()) {
            return null;
        }
        
        return PositionHelper.getPositionAverageDistanceWeightedTo(unit, this, power);
    }
    
    // =========================================================
    // Override methods

    @Override
    public String toString() {
        String string = "Units (" + units.size() + "):\n";

        for (AUnit unit : units) {
            string += "   - " + unit.getType() + " (ID:" + unit.getID() + ")\n";
        }

        return string;
    }

    // =========================================================
    // Auxiliary

    public void print() {
        System.out.println("Units in list:");
        for (AUnit unit : list()) {
//            System.out.println(unit + " // Dist to main base: " + (PositionUtil.distanceTo(unit, Select.mainBase())));
            System.out.println(unit + ", extra value: " + valueFor(unit));
        }
        System.out.println();
    }

    // === Getters =============================================

    /**
     * Returns iterable collection of units in this object.
     */
    public Collection<AUnit> list() {
        return (Collection<AUnit>) units.clone();
    }

    /**
     * @return iterator object for inner collection with the units.
     *
     */
    public Iterator<AUnit> iterator() {
        return units.iterator();
    }

}
