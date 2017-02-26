package atlantis.units;

import atlantis.position.APosition;
import atlantis.position.APositionedObject;
import atlantis.position.PositionOperationsWrapper;
import atlantis.util.AtlantisUtilities;
import bwapi.Position;
import bwapi.PositionedObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * This class is wrapper for ArrayList<AUnit>. It allows some helpful methods to be executed upon squad of
 * units like sorting etc.
 */
public class Units {

    /**
     * This mapping can be used to store extra values assigned to units e.g. if units represents mineral fields,
     * we can easily store info how many workers are gathering each mineral field thanks to this mapping.
     */
    private LinkedHashMap<AUnit, Double> units = new LinkedHashMap<>();
    
    // =====================================================================

    public Units() {
    }

    // === Base functionality ==============================================

    public Units addUnit(AUnit unitToAdd) {
        units.put(unitToAdd, null);
        return this;
    }

    public Units addUnits(Collection<AUnit> unitsToAdd) {
        for (AUnit unit : unitsToAdd) {
            units.put(unit, null);
        }
        return this;
    }

    public Units removeUnits(Collection<AUnit> unitsToRemove) {
        for (AUnit unit : unitsToRemove) {
            units.remove(unit);
        }
        return this;
    }

    public Units removeUnit(AUnit unitToRemove) {
        units.remove(unitToRemove);
        return this;
    }

    public int size() {
        return units.size();
    }

    public boolean isEmpty() {
        return units.isEmpty();
    }

    /**
     * Returns first unit from the set.
     */
    public AUnit first() {
        return isEmpty() ? null : units.keySet().iterator().next();
    }

    /**
     * Returns random unit from the set.
     */
    public AUnit random() {
        return (AUnit) AtlantisUtilities.getRandomElement(units.keySet());
    }
    
    /**
     * Returns unit with <b>N</b>-th index.
     */
    public AUnit get(int index) {
        Set<AUnit> keySet = units.keySet();

        int currentIndex = 0;
        for (AUnit unit : keySet) {
            if (currentIndex == index) {
                return unit;
            }
            else {
                currentIndex++;
            }
        }
        throw new RuntimeException("Units.get(index) is invalid, shouldn't reach here");
    }

    // === Special methods =====================================
    
    /**
     * Shuffle units to have random sequence in the list.
     */
    public Units shuffle() {
        Set<AUnit> keySet = units.keySet();

        // Create new mapping, with new order
        LinkedHashMap<AUnit, Double> newUnits = new LinkedHashMap<>();
        for (AUnit unit : keySet) {
            newUnits.put(unit, getValueFor(unit));
        }
        this.units = newUnits;
        
        return this;
    }

    /**
     * Returns random units.
     */
    public AUnit getRandom() {
        return (AUnit) AtlantisUtilities.getRandomElement(units.keySet());
    }

    // === Value mapping methods ===============================
    
    public void changeValueBy(AUnit unit, double deltaValue) {
        if (units.containsKey(unit)) {
            units.put(unit, units.get(unit) + deltaValue);
        } else {
            units.put(unit, deltaValue);
        }
    }

    public void setValueFor(AUnit unit, double newValue) {
        if (unit == null) {
            throw new IllegalArgumentException("Units unit shouldn't be null");
        }
        
        units.put(unit, newValue);
    }

    public double getValueFor(AUnit unit) {
//        if (unit == null) {
//            throw new IllegalArgumentException("Units unit shouldn't be null");
//        }
        
        if (units == null) {
            return 0;
        }
        else {
            return units.get(unit);
        }
    }

    public AUnit getUnitWithLowestValue() {
        return getUnitWithExtremeValue(true);
    }

    public AUnit getUnitWithHighestValue() {
        return getUnitWithExtremeValue(false);
    }

    private AUnit getUnitWithExtremeValue(boolean returnLowest) {
        if (units.isEmpty()) {
            return null;
        }

        AUnit bestUnit = null;
        
        // We're interested in MIN
        if (returnLowest) {
            double bestValue = Integer.MAX_VALUE;
            for (AUnit unit : units.keySet()) {
                if (bestUnit == null || getValueFor(unit) < bestValue) {
                    bestValue = getValueFor(unit);
                    bestUnit = unit;
                }
            }
        }
        
        // We're interested in MAX
        else {
            double bestValue = Integer.MIN_VALUE;
            for (AUnit unit : units.keySet()) {
                if (bestUnit == null || getValueFor(unit) > bestValue) {
                    bestValue = getValueFor(unit);
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
        
        ArrayList<AUnit> unitsList = new ArrayList<>();
        unitsList.addAll(units.keySet());
        
        Collections.sort(unitsList, new Comparator<APositionedObject>() {
            @Override
            public int compare(APositionedObject p1, APositionedObject p2) {
                if (p1 == null || !(p1 instanceof PositionedObject)) {
                    return -1;
                }
                if (p2 == null || !(p2 instanceof PositionedObject)) {
                    return 1;
                }
                double distance1 = p1.distanceTo(position);	//TODO: check whether this doesn't mix up position types
                double distance2 = p2.distanceTo(position);
                if (distance1 == distance2) {
                    return 0;
                }
                else {
                    return distance1 < distance2 ? (nearestFirst ? -1 : 1) : (nearestFirst ? 1 : -1);
                }
            }
        });
        
        // Create new mapping, with new order
        LinkedHashMap<AUnit, Double> newUnits = new LinkedHashMap<>();
        for (AUnit unit : unitsList) {
            newUnits.put(unit, getValueFor(unit));
        }
        this.units = newUnits;

        return this;
    }

    /**
     * Returns median PX and median PY for all units.
     */
    public APosition median() {
        if (isEmpty()) {
            return null;
        }
        
        return PositionOperationsWrapper.getPositionMedian(this);

//        ArrayList<Integer> xCoordinates = new ArrayList<>();
//        ArrayList<Integer> yCoordinates = new ArrayList<>();
//        for (AUnit unit : units.keySet()) {
//            xCoordinates.add(unit.getPosition().getX());	//TODO: check whether position is in Pixels
//            yCoordinates.add(unit.getPosition().getY());
//        }
//        Collections.sort(xCoordinates);
//        Collections.sort(yCoordinates);
//
//        return new Position(
//                xCoordinates.get(xCoordinates.size() / 2),
//                yCoordinates.get(yCoordinates.size() / 2)
//        );
    }
    
    // =========================================================
    // Override methods
    @Override
    public String toString() {
        String string = "Units (" + units.size() + "):\n";

        for (AUnit unit : units.keySet()) {
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
            System.out.println(unit + ", extra value: " + getValueFor(unit));
        }
        System.out.println();
    }

    // =========================================================
    // Getters
    /**
     * Returns iterable collection of units in this object.
     */
    public Collection<AUnit> list() {
        ArrayList<AUnit> copy = new ArrayList<AUnit>();
        copy.addAll(units.keySet());
        return copy;
    }

    /**
     * Returns iterable ArrayList of units in this object.
     */
    public ArrayList<AUnit> arrayList() {
        ArrayList<AUnit> copy = new ArrayList<AUnit>();
        copy.addAll(units.keySet());
        return copy;
    }

    /**
     * @return iterator object for inner collection with the units.
     *
     */
    public Iterator<AUnit> iterator() {
        return units.keySet().iterator();
    }

}
