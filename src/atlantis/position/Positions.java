package atlantis.position;

import atlantis.util.A;
import bwapi.Position;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * This class is wrapper for ArrayList<Position>. It allows some helpful methods to be executed upon squad of
 * positions like sorting etc. TODO: check whether using PositionedObject instead of Positions yields correct
 * behavior
 */
//public class Positions<T extends Point<Position>> {
public class Positions<T extends HasPosition> {

    private final ArrayList<T> positions = new ArrayList<>();

    /**
     * This mapping can be used to store extra values assigned to positions e.g. if positions reprents mineral
     * fields, we can easily store info how many workers are gathering each mineral field thanks to this
     * mapping.
     */
    private HashMap<T, Double> positionValues;

    // =====================================================================

    public Positions() { }

    public Positions(Collection<T> positionsToAdd) {
        addPositions(positionsToAdd);
    }

    // =====================================================================
    // Basic functionality methods
    public Positions<T> addPosition(T positionToAdd) {
        positions.add(positionToAdd);
        return this;
    }

    public Positions<T> addPositions(Collection<T> positionsToAdd) {
        positions.addAll(positionsToAdd);
        return this;
    }

    public Positions<T> removePositions(Collection<T> positionsToRemove) {
        positions.removeAll(positionsToRemove);
        return this;
    }

    public Positions<T> removePosition(T positionToRemove) {
        positions.remove(positionToRemove);
        return this;
    }

    public int size() {
        return positions.size();
    }

    public boolean isEmpty() {
        return positions.isEmpty();
    }

    public T first() {
        return isEmpty() ? null : positions.get(0);
    }

    public T get(int index) {
        return positions.get(index);
    }

    // =========================================================
    // Special methods

    /**
     * Shuffle positions to have random sequence in the list.
     */
    public Positions<T> shuffle() {
        Collections.shuffle(positions);
        return this;
    }

    /**
     * Returns random positions.
     */
    public Position getRandom() {
        return (Position) A.getRandomListElement(positions);
    }

    /**
     * Sorts all positions according to the distance to <b>position</b>. If <b>nearestFirst</b> is true, then
     * after sorting first position will be the one closest to given position.
     */
    public Positions<T> sortByDistanceTo(final HasPosition position, final boolean nearestFirst) {
        Collections.sort(positions, new Comparator<T>() {
            @Override
            public int compare(T u1, T u2) {
                double distance1 = PositionUtil.distanceTo(position, u1);
                double distance2 = PositionUtil.distanceTo(position, u2);
                return nearestFirst ? Double.compare(distance1, distance2) : Double.compare(distance2, distance1);
//                return position.distTo(u1.position()) < position.distTo(u2.position())
//                        ? (nearestFirst ? -1 : 1) : (nearestFirst ? 1 : -1);
            }
        });

        return this;
    }

    /**
     * Sorts all positions according to the distance to <b>position</b>. If <b>nearestFirst</b> is true, then
     * after sorting first position will be the one closest to given position.
     */
    public Positions<T> sortByGroundDistanceTo(final HasPosition position, final boolean nearestFirst) {
        positions.sort(new Comparator<T>() {
            @Override
            public int compare(T u1, T u2) {
                double distToU1 = position.position().groundDistanceTo(u1);
                if (distToU1 < 0) {
                    distToU1 = 99999;
                }
                double distToU2 = position.position().groundDistanceTo(u2);;
                return distToU1 < distToU2 ? (nearestFirst ? -1 : 1) : (nearestFirst ? 1 : -1);
            }
        });

        return this;
    }

    // =========================================================
    // Value mapping methods
    public void changeValueBy(T position, double deltaValue) {
        ensureValueMappingExists();
        if (positionValues.containsKey(position)) {
            positionValues.put(position, positionValues.get(position) + deltaValue);
        } else {
            positionValues.put(position, deltaValue);
        }
    }

    public void setValueFor(T position, double newValue) {
        ensureValueMappingExists();
        positionValues.put(position, newValue);
    }

    public double getValueFor(T position) {
        if (positionValues == null) {
            return 0;
        } else {
            return positionValues.get(position);
        }
    }

    public T getPositionWithLowestValue() {
        ensureValueMappingExists();

        if (positions.isEmpty()) {
            return null;
        }

        T bestPosition = positions.get(0);
        double bestValue = positionValues.get(bestPosition);

        for (T position : positions) {
            if (positionValues.get(position) < bestValue) {
                bestValue = positionValues.get(position);
                bestPosition = position;
            }
        }

        return bestPosition;
    }

    private void ensureValueMappingExists() {
        positionValues = new HashMap<>();
        for (T position : positions) {
            positionValues.put(position, 0.0);
        }
    }

    // =========================================================
    // Override methods

    @Override
    public String toString() {
        String string = "Positions (" + positions.size() + "):\n";

        for (T position : positions) {
            string += "   - " + position + "\n";
        }

        return string;
    }

    // =========================================================
    // Auxiliary

    public void print() {
        System.out.println("Positions in list:");
        for (T position : list()) {
            System.out.println(position);
        }
        System.out.println();
    }

    // === Getters =============================================

    /**
     * Returns iterable collection of positions in this object.
     */
    public Collection<T> list() {
        ArrayList<T> copy = new ArrayList<>(positions);
        return copy;
    }

    /**
     * Returns iterable ArrayList of positions in this object.
     */
    public ArrayList<T> arrayList() {
        ArrayList<T> copy = new ArrayList<>(positions);
        return copy;
    }

    private static int _lastIndex = 0;
    
    public T nearestTo(HasPosition position) {
        double closestDist = 9999999;
        T closest = null;
        
        int index = 0;
        for (T t : positions) {
            if (t.position().distTo(position) < closestDist) {
                closestDist = t.position().distTo(position);
//                closest = APosition.create(otherPosition.x() / 32, otherPosition.y() / 32);
                closest = t;
                _lastIndex = index;
            }
            index++;
        }
        
        return closest;
    }

    public int getLastIndex() {
        return _lastIndex;
    }

}
