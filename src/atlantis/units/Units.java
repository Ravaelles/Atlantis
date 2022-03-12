package atlantis.units;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.position.PositionHelper;
import atlantis.map.position.PositionUtil;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

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
    private ArrayList<AUnit> units = new ArrayList<>();
    private TreeSet<Integer> unitIds = new TreeSet<>();
    private Map<AUnit, Double> extraValues = new HashMap<>();

    // =====================================================================

    public Units(Units otherUnits) {
        this.units = new ArrayList<>(otherUnits.units);
        this.unitIds = new TreeSet<>(otherUnits.unitIds);
        this.extraValues = new HashMap<>(otherUnits.extraValues);
    }

    public Units(List<? extends AUnit> units) {
        this.units = new ArrayList<>(units);
    }

//    public Units(Collection<AUnit> units) {
//        units = new ArrayList<>();
//        addUnits(units);
//    }

    public Units() {
    }

    // === Base functionality ==============================================

    public Units addUnit(AUnit unitToAdd) {
        units.add(unitToAdd);
        unitIds.add(unitToAdd.id());
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
            removeUnit(unit);
        }
        return this;
    }

    public Units removeUnit(AUnit unitToRemove) {
        units.remove(unitToRemove);
        unitIds.remove(unitToRemove.id());
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
        return unitIds.contains(unit.id());
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
    
    public void incrementValue(AUnit unit) {
        changeValueBy(unit, 1);
    }

    public void changeValueBy(AUnit unit, double deltaValue) {
        if (has(unit)) {
            extraValues.put(unit, extraValues.get(unit) + deltaValue);
        } else {
            extraValues.put(unit, deltaValue);
        }
    }

    public void setValueFor(AUnit unit, Double value) {
        if (unit == null) {
            throw new IllegalArgumentException("Units unit shouldn't be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("Units value shouldn't be null");
        }
        if (!units.contains(unit)) {
            throw new RuntimeException("Unit isn't in the list. Use addUnitWithValue");
        }

        extraValues.put(unit, value);
    }

    public boolean hasValueFor(AUnit unit) {
        return extraValues.containsKey(unit) && extraValues.get(unit) != null;
    }

    public double valueFor(AUnit unit) {
        if (unit == null) {
            System.err.println("Invalid unit: NULL. Return -1 as fallback.");
            return -1;
        }
        if (!hasValueFor(unit)) {
            System.err.println("No unit value for " + unit.name() + ". Return -1 as fallback.");
            return -1;
        }

        return extraValues.get(unit);
    }

    public Object valueForOrNull(AUnit unit) {
        assert !units.isEmpty();
        assert unit != null;
        assert has(unit);

        Double value = extraValues.get(unit);
        return value;
    }

    public AUnit unitWithLowestValue() {
        return unitWithExtremeValue(true);
    }

    public double lowestValue() {
        assert !units.isEmpty();

        return extraValues.get(unitWithLowestValue());
    }

    public AUnit unitWithHighestValue() {
        return unitWithExtremeValue(false);
    }

    private AUnit unitWithExtremeValue(boolean returnLowest) {
        assert !units.isEmpty();
        AUnit bestUnit = null;
        
        double bestValue = returnLowest ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        for (AUnit unit : units) {
//                System.out.println("######################");
//                System.out.println(extraValues);
//                for (AUnit u : extraValues.keySet()) {
//                    System.out.println(u + " // " + (extraValues.containsKey(u) ? extraValues.get(u) : "NO"));
//                }
//                System.out.println("######################");
            if (bestUnit == null || (returnLowest ? (valueFor(unit) < bestValue) : (valueFor(unit) > bestValue))) {
                bestValue = valueFor(unit);
                bestUnit = unit;
            }
        }

        return bestUnit;
    }

    /**
     * Use carefully as it leaves the old extraValues object.
     */
    public Units replaceUnitsWith(List<AUnit> newUnits) {
        this.units = (ArrayList<AUnit>) newUnits;
        return this;
    }

    // === Location-related ====================================
    
    /**
     * Sorts all units according to the distance to <b>position</b>. If <b>nearestFirst</b> is true, then
     * after sorting first unit will be the one closest to given position.
     */
    public Units sortByDistanceTo(final HasPosition position, final boolean nearestFirst) {
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
                double distance1 = p1.distTo(position);
                double distance2 = p2.distTo(position);
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
    public Units sortByGroundDistTo(final HasPosition position, final boolean nearestFirst) {
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
                double distance1 = PositionUtil.groundDistanceTo(
                        p1.position(), position.position()
                );
                double distance2 = PositionUtil.groundDistanceTo(
                        p2.position(), position.position()
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
        
        return PositionHelper.getPositionAverage(units);
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

    public boolean onlyRanged() {
        ArrayList<AUnit> onlyRanged = new ArrayList<>(this.units);
        onlyRanged.removeIf(u -> !u.isRanged());
        return onlyRanged.size() == this.units.size();
    }

    public boolean onlyMelee() {
        ArrayList<AUnit> onlyRanged = new ArrayList<>(this.units);
        onlyRanged.removeIf(u -> !u.isMelee());
        return onlyRanged.size() == this.units.size();
    }

    public boolean onlyAir() {
        ArrayList<AUnit> list = new ArrayList<>(this.units);
        list.removeIf(u -> !u.isAir());
        return list.isEmpty();
    }
    
    // =========================================================
    // Override methods

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("Units (" + units.size() + "):\n");

        for (AUnit unit : units) {
            string.append("   - ").append(unit.type()).append(" (ID:").append(unit.id()).append(") ")
                    .append(hasValueFor(unit) ? valueForOrNull(unit) : "")
                    .append("\n");
        }

        return string.toString();
    }

    // =========================================================
    // Auxiliary

    public void print() {
        print(null);
    }

    public void print(String message) {
//        System.out.println("Units in list:");
        System.out.println("===" + (message == null ? "" : " " + message + " (" + size() + ") ") + "==========");
        for (AUnit unit : list()) {
            System.out.print(unit);
            if (hasValueFor(unit)) {
                System.out.println(", extra value: " + (hasValueFor(unit) ? valueFor(unit) : ""));
            } else {
                System.out.println();
            }
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

    public AUnit[] array() {
        AUnit[] array = new AUnit[units.size()];
        return units.toArray(array);
    }

    public Units clone() {
        return new Units(this);
    }

    /**
     * @return iterator object for inner collection with the units.
     *
     */
    public Iterator<AUnit> iterator() {
        return units.iterator();
    }

    public void add(List<AUnit> newUnits) {
        for (AUnit unit : newUnits) {
            addUnit(unit);
        }
    }

    public void add(Selection newUnits) {
        for (AUnit unit : newUnits.list()) {
            addUnit(unit);
        }
    }

    public Selection selection() {
        return Select.from(this);
    }
}
