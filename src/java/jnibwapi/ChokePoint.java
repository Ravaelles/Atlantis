package jnibwapi;

import java.util.Map;

/**
 * Represents a choke point in a StarCraft map.
 * 
 * For a description of fields see: http://code.google.com/p/bwta/wiki/Chokepoint
 */
public class ChokePoint {
	
	public static final int numAttributes = 9;
	public static final double fixedScale = 100.0;
	
	private final Position center;
	private final double radius;
	private final int firstRegionID;
	private final int secondRegionID;
	private final Position firstSide;
	private final Position secondSide;
	private final Region firstRegion;
	private final Region secondRegion;
	
	public ChokePoint(int[] data, int index, Map<Integer, Region> idToRegion) {
		int centerX = data[index++];
		int centerY = data[index++];
		center = new Position(centerX, centerY);
		radius = data[index++] / fixedScale;
		firstRegionID = data[index++];
		secondRegionID = data[index++];
		int firstSideX = data[index++];
		int firstSideY = data[index++];
		firstSide = new Position(firstSideX, firstSideY);
		int secondSideX = data[index++];
		int secondSideY = data[index++];
		secondSide = new Position(secondSideX, secondSideY);
		firstRegion = idToRegion.get(firstRegionID);
		secondRegion = idToRegion.get(secondRegionID);
	}
	
	public Region getOtherRegion(Region region) {
		return region.equals(firstRegion) ? secondRegion : firstRegion;
	}
	
	public Region getFirstRegion() {
		return firstRegion;
	}
	
	public Region getSecondRegion() {
		return secondRegion;
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
	
	public double getRadius() {
		return radius;
	}
	
	/** @deprecated use {@link #getFirstRegion()} instead */
	public int getFirstRegionID() {
		return firstRegionID;
	}
	
	/** @deprecated use {@link #getSecondRegion()} instead */
	public int getSecondRegionID() {
		return secondRegionID;
	}
	
	public Position getFirstSide() {
		return firstSide;
	}
	
	public Position getSecondSide() {
		return secondSide;
	}
	
	/** @deprecated use {@link #getFirstSide()} instead */
	public int getFirstSideX() {
		return firstSide.getPX();
	}
	
	/** @deprecated use {@link #getFirstSide()} instead */
	public int getFirstSideY() {
		return firstSide.getPY();
	}
	
	/** @deprecated use {@link #getSecondSide()} instead */
	public int getSecondSideX() {
		return secondSide.getPX();
	}
	
	/** @deprecated use {@link #getSecondSide()} instead */
	public int getSecondSideY() {
		return secondSide.getPY();
	}
}
