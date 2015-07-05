package atlantis.wrappers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import jnibwapi.Position;
import atlantis.util.RUtilities;

/**
 * This class is wrapper for ArrayList<Position>. It allows some helpful methods to be executed upon group of positions
 * like sorting etc.
 */
public class Positions<T extends Position> {

	private ArrayList<T> positions = new ArrayList<>();

	/**
	 * This mapping can be used to store extra values assigned to positions e.g. if positions reprents mineral fields,
	 * we can easily store info how many workers are gathering each mineral field thanks to this mapping.
	 */
	private HashMap<Position, Double> positionValues;

	// =====================================================================

	public Positions() {
	}

	// =====================================================================
	// Basic functionality methods

	public Positions addPosition(T positionToAdd) {
		positions.add(positionToAdd);
		return this;
	}

	public Positions addPositions(Collection<T> positionsToAdd) {
		positions.addAll(positionsToAdd);
		return this;
	}

	public Positions removePositions(Collection<T> positionsToRemove) {
		positions.removeAll(positionsToRemove);
		return this;
	}

	public Positions removePosition(T positionToRemove) {
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
	public Positions shuffle() {
		Collections.shuffle(positions);
		return this;
	}

	/**
	 * Returns random positions.
	 */
	public Position getRandom() {
		return (Position) RUtilities.getRandomListElement(positions);
	}

	/**
	 * Sorts all positions according to the distance to <b>position</b>. If <b>nearestFirst</b> is true, then after
	 * sorting first position will be the one closest to given position.
	 */
	public Positions sortByDistanceTo(final Position position, final boolean nearestFirst) {
		Collections.sort(positions, new Comparator<Position>() {
			@Override
			public int compare(Position u1, Position u2) {
				return position.distanceTo(u1) < position.distanceTo(u2) ? (nearestFirst ? 1 : -1) : (nearestFirst ? -1
						: 1);
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
		for (Position position : positions) {
			positionValues.put(position, 0.0);
		}
	}

	// =========================================================
	// Override methods

	@Override
	public String toString() {
		String string = "Positions (" + positions.size() + "):\n";

		for (Position position : positions) {
			string += "   - " + position + "\n";
		}

		return string;
	}

	// =========================================================
	// Auxiliary

	public void print() {
		System.out.println("Positions in list:");
		for (Position position : list()) {
			System.out.println(position);
		}
		System.out.println();
	}

	// =========================================================
	// Getters

	/**
	 * Returns iterable collection of positions in this object.
	 */
	public Collection<T> list() {
		ArrayList<T> copy = new ArrayList<>();
		copy.addAll(positions);
		return copy;
	}

	/**
	 * Returns iterable ArrayList of positions in this object.
	 */
	public ArrayList<T> arrayList() {
		ArrayList<T> copy = new ArrayList<>();
		copy.addAll(positions);
		return copy;
	}

}
