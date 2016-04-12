package atlantis.units;

import atlantis.util.AtlantisUtilities;
import atlantis.util.PositionUtil;
import atlantis.wrappers.APositionedObject;
import bwapi.Position;
import bwapi.PositionedObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This class is wrapper for ArrayList<AUnit>. It allows some helpful methods to be executed upon squad of
 * units like sorting etc.
 */
public class Units {

    private ArrayList<AUnit> units = new ArrayList<>();

    /**
     * This mapping can be used to store extra values assigned to units e.g. if units reprents mineral fields,
     * we can easily store info how many workers are gathering each mineral field thanks to this mapping.
     */
    private HashMap<AUnit, Double> unitValues = null;

    // =====================================================================
    public Units() {
    }

    // =====================================================================
    // Basic functionality methods
    public Units addUnit(AUnit unitToAdd) {
        units.add(unitToAdd);
        return this;
    }

    public Units addUnits(Collection<AUnit> unitsToAdd) {
        units.addAll(unitsToAdd);
        return this;
    }

    public Units removeUnits(Collection<AUnit> unitsToRemove) {
        units.removeAll(unitsToRemove);
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
        return units.isEmpty() && (unitValues == null || unitValues.isEmpty());
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
        return (AUnit) AtlantisUtilities.getRandomElement(units);
    }
    
    /**
     * Returns unit with <b>N</b>-th index.
     */
    public AUnit get(int index) {
        return units.get(index);
    }

    // =========================================================
    // Special methods
    /**
     * Shuffle units to have random sequence in the list.
     */
    public Units shuffle() {
        Collections.shuffle(units);
        return this;
    }

    /**
     * Returns random units.
     */
    public AUnit getRandom() {
        return (AUnit) AtlantisUtilities.getRandomListElement(units);
    }

    /**
     * Sorts all units according to the distance to <b>position</b>. If <b>nearestFirst</b> is true, then
     * after sorting first unit will be the one closest to given position.
     */
    public Units sortByDistanceTo(final Position position, final boolean nearestFirst) {
        if (position == null) {
            return null;
        }
        
        Collections.sort(units, new Comparator<APositionedObject>() {
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

        return this;
    }

    /**
     * Returns median PX and median PY for all units.
     */
    public Position positionMedian() {
        if (isEmpty()) {
            return null;
        }

        ArrayList<Integer> xCoordinates = new ArrayList<>();
        ArrayList<Integer> yCoordinates = new ArrayList<>();
        for (AUnit unit : units) {
            xCoordinates.add(unit.getPosition().getX());	//TODO: check whether position is in Pixels
            yCoordinates.add(unit.getPosition().getX());
        }
        Collections.sort(xCoordinates);
        Collections.sort(yCoordinates);

        return new Position(
                xCoordinates.get(xCoordinates.size() / 2),
                yCoordinates.get(yCoordinates.size() / 2)
        );
    }

    // =========================================================
    // Value mapping methods
    public void changeValueBy(AUnit unit, double deltaValue) {
        ensureValueMappingExists();
        if (unitValues.containsKey(unit)) {
            unitValues.put(unit, unitValues.get(unit) + deltaValue);
        } else {
            unitValues.put(unit, deltaValue);
        }
    }

    public void setValueFor(AUnit unit, double newValue) {
        ensureValueMappingExists();
        
        if (unit == null) {
            throw new IllegalArgumentException("Units unit shouldn't be null");
        }
        
        unitValues.put(unit, newValue);
    }

    public double getValueFor(AUnit unit) {
//        ensureValueMappingExists();
        
        if (unit == null) {
            throw new IllegalArgumentException("Units unit shouldn't be null");
        }
        
        if (unitValues == null) {
            return 0;
        }
//        if (unitValues == null || unitValues.isEmpty()) {
//            return 0;
//        } else {
//        }
        return unitValues.get(unit);
    }

    public AUnit getUnitWithLowestValue() {
        return getUnitWithExtremeValue(true);
    }

    public AUnit getUnitWithHighestValue() {
        return getUnitWithExtremeValue(false);
    }

    private AUnit getUnitWithExtremeValue(boolean lowest) {
        ensureValueMappingExists();

        if (unitValues.isEmpty()) {
            return null;
        }

        AUnit bestUnit = unitValues.keySet().iterator().next();
        double bestValue = unitValues.get(bestUnit);

        if (lowest) {
            for (AUnit unit : unitValues.keySet()) {
                if (unitValues.get(unit) < bestValue) {
                    bestValue = unitValues.get(unit);
                    bestUnit = unit;
                }
            }
        }
        else {
            for (AUnit unit : unitValues.keySet()) {
                if (unitValues.get(unit) > bestValue) {
                    bestValue = unitValues.get(unit);
                    bestUnit = unit;
                }
            }
        }

        return bestUnit;
    }

    private void ensureValueMappingExists() {
        if (unitValues == null) {
            unitValues = new HashMap<>();
        }
        for (AUnit unit : units) {
            unitValues.put(unit, 0.0);
        }
        for (AUnit unit : unitValues.keySet()) {
            unitValues.put(unit, 0.0);
        }
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
            System.out.println(unit + " // Dist to main base: " + (PositionUtil.distanceTo(unit, Select.mainBase())));
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
        copy.addAll(units);
        return copy;
    }

    /**
     * Returns iterable ArrayList of units in this object.
     */
    public ArrayList<AUnit> arrayList() {
        ArrayList<AUnit> copy = new ArrayList<AUnit>();
        copy.addAll(units);
        return copy;
    }

    /**
     * @return iterator object for inner collection with the units.
     *
     */
    public Iterator<AUnit> iterator() {
        return units.iterator();
    }

}
