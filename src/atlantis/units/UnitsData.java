package atlantis.units;

import atlantis.information.UnitData;
import atlantis.util.AtlantisUtilities;
import atlantis.util.PositionUtil;
import bwapi.Position;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;


/**
 * This class is wrapper for ArrayList<UnitData>. It allows some helpful methods to be executed upon squad of
 * units like sorting etc.
 */
public class UnitsData {

    private ArrayList<UnitData> units = new ArrayList<>();

    /**
     * This mapping can be used to store extra values assigned to units e.g. if units reprents mineral fields,
     * we can easily store info how many workers are gathering each mineral field thanks to this mapping.
     */
    private HashMap<UnitData, Double> unitValues = null;

    // =====================================================================
    public UnitsData() {
    }

    // =====================================================================
    // Basic functionality methods
    public UnitsData addUnit(UnitData unitToAdd) {
        units.add(unitToAdd);
        return this;
    }

    public UnitsData addUnits(Collection<UnitData> unitsToAdd) {
        units.addAll(unitsToAdd);
        return this;
    }

    public UnitsData removeUnits(Collection<UnitData> unitsToRemove) {
        units.removeAll(unitsToRemove);
        return this;
    }

    public UnitsData removeUnit(UnitData unitToRemove) {
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
    public UnitData first() {
        return isEmpty() ? null : units.get(0);
    }

    /**
     * Returns random unit from the set.
     */
    public UnitData random() {
        return (UnitData) AtlantisUtilities.getRandomElement(units);
    }
    
    /**
     * Returns unit with <b>N</b>-th index.
     */
    public UnitData get(int index) {
        return units.get(index);
    }

    // =========================================================
    // Special methods
    /**
     * Shuffle units to have random sequence in the list.
     */
    public UnitsData shuffle() {
        Collections.shuffle(units);
        return this;
    }

    /**
     * Returns random units.
     */
    public UnitData getRandom() {
        return (UnitData) AtlantisUtilities.getRandomListElement(units);
    }

    /**
     * Sorts all units according to the distance to <b>position</b>. If <b>nearestFirst</b> is true, then
     * after sorting first unit will be the one closest to given position.
     */
    public UnitsData sortByDistanceTo(final Position position, final boolean nearestFirst) {
        if (position == null) {
            return null;
        }
        
        Collections.sort(units, new Comparator<UnitData>() {
            @Override
            public int compare(UnitData u1, UnitData u2) {
                if (u1 == null || !(u1 instanceof UnitData)) {
                    return -1;
                }
                if (u2 == null || !(u2 instanceof UnitData)) {
                    return 1;
                }
                double distance1 = PositionUtil.distanceTo(position, u1.getPosition());	//TODO: check whether this doesn't mix up position types
                double distance2 = PositionUtil.distanceTo(position, u2.getPosition());
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
        for (UnitData unit : units) {
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
    public void changeValueBy(UnitData unit, double deltaValue) {
        ensureValueMappingExists();
        if (unitValues.containsKey(unit)) {
            unitValues.put(unit, unitValues.get(unit) + deltaValue);
        } else {
            unitValues.put(unit, deltaValue);
        }
    }

    public void setValueFor(UnitData unit, double newValue) {
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

    public UnitData getUnitWithLowestValue() {
        return getUnitWithExtremeValue(true);
    }

    public UnitData getUnitWithHighestValue() {
        return getUnitWithExtremeValue(false);
    }

    private UnitData getUnitWithExtremeValue(boolean lowest) {
        ensureValueMappingExists();

        if (unitValues.isEmpty()) {
            return null;
        }

        UnitData bestUnit = unitValues.keySet().iterator().next();
        double bestValue = unitValues.get(bestUnit);

        if (lowest) {
            for (UnitData unit : unitValues.keySet()) {
                if (unitValues.get(unit) < bestValue) {
                    bestValue = unitValues.get(unit);
                    bestUnit = unit;
                }
            }
        }
        else {
            for (UnitData unit : unitValues.keySet()) {
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
        for (UnitData unit : units) {
            unitValues.put(unit, 0.0);
        }
        for (UnitData unit : unitValues.keySet()) {
            unitValues.put(unit, 0.0);
        }
    }

    // =========================================================
    // Override methods
    @Override
    public String toString() {
        String string = "Units (" + units.size() + "):\n";

        for (UnitData unitData : units) {
            string += "   - " + unitData.getType() + " (ID:" + unitData.getUnit().getID() + ")\n";
        }

        return string;
    }

    // =========================================================
    // Auxiliary
    public void print() {
        System.out.println("Units in list:");
        for (UnitData unit : list()) {
            System.out.println(unit + " // Dist to main base: " + (PositionUtil.distanceTo(unit.getPosition(), Select.mainBase().getPosition())));
        }
        System.out.println();
    }

    // =========================================================
    // Getters
    /**
     * Returns iterable collection of units in this object.
     */
    public Collection<UnitData> list() {
        ArrayList<UnitData> copy = new ArrayList<UnitData>();
        copy.addAll(units);
        return copy;
    }

    /**
     * Returns iterable ArrayList of units in this object.
     */
    public ArrayList<UnitData> arrayList() {
        ArrayList<UnitData> copy = new ArrayList<UnitData>();
        copy.addAll(units);
        return copy;
    }

    /**
     * @return iterator object for inner collection with the units.
     *
     */
    public Iterator<UnitData> iterator() {
        return units.iterator();
    }

}
