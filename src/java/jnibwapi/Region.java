package jnibwapi;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Represents a region in a StarCraft map.
 * 
 * For a description of fields see: http://code.google.com/p/bwta/wiki/Region
 */
public class Region {
	
	public static final int numAttributes = 3;
	
	private final int ID;
	private final Position center;
	private final Position[] polygon;
	private Set<Region> connectedRegions = new HashSet<>();
	private Set<ChokePoint> chokePoints = new HashSet<>();
	private Set<Region> allConnectedRegions = null;
	
	public Region(int[] data, int index, int[] coordinates) {
		ID = data[index++];
		int centerX = data[index++];
		int centerY = data[index++];
		center = new Position(centerX, centerY);
		polygon = new Position[coordinates.length / 2];
		for (int i = 0; i < coordinates.length; i += 2) {
			polygon[i / 2] = new Position(coordinates[i], coordinates[i + 1]);
		}
	}
	
	public int getID() {
		return ID;
	}
	
	public Position getCenter() {
		return center;
	}
	
	/** @deprecated use {@link #getCenter()} instead */
	public int getCenterX() {
		return center.getPX();
	}
	
	/** @deprecated use {@link #getCenter()} instead */
	public int getCenterY() {
		return center.getPY();
	}
	
	public Position[] getPolygon() {
		return Arrays.copyOf(polygon, polygon.length);
	}
	
	protected void addChokePoint(ChokePoint chokePoint) {
		chokePoints.add(chokePoint);
	}
	
	public Set<ChokePoint> getChokePoints() {
		return Collections.unmodifiableSet(chokePoints);
	}
	
	protected void addConnectedRegion(Region other) {
		connectedRegions.add(other);
	}
	
	public Set<Region> getConnectedRegions() {
		return Collections.unmodifiableSet(connectedRegions);
	}
	
	/** Get all transitively connected regions for a given region */
	public Set<Region> getAllConnectedRegions() {
		// Evaluate on first call
		if (allConnectedRegions == null) {
			allConnectedRegions = new HashSet<Region>();
			LinkedList<Region> unexplored = new LinkedList<Region>();
			unexplored.add(this);
			while (!unexplored.isEmpty()) {
				Region current = unexplored.remove();
				if (allConnectedRegions.add(current)) {
					unexplored.addAll(current.getConnectedRegions());
				}
			}
		}
		return Collections.unmodifiableSet(allConnectedRegions);
	}
	
}
