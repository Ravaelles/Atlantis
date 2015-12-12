package jnibwapi;

import java.util.Map;

/**
 * Represents a StarCraft base location.
 * 
 * For a description of fields see: http://code.google.com/p/bwta/wiki/BaseLocation
 */
public class BaseLocation extends Position {

	public static final int numAttributes = 10;

	// private final Position center; // @AtlantisChange
	// private final Position position; // @AtlantisChange
	private final Region region;
	private final int minerals;
	private final int gas;
	private final boolean island;
	private final boolean mineralOnly;
	private final boolean startLocation;

	public BaseLocation(int[] data, int index, Map<Integer, Region> idToRegion) {

		// OLD CODE
		// int x = data[index++];
		// int y = data[index++];
		// center = new Position(x, y);
		// int tx = data[index++];
		// int ty = data[index++];
		// position = new Position(tx, ty, PosType.BUILD);

		// NEW CODE
		super(data[index++], data[index++]);
		index++; // Read unsued value
		index++; // Read unsued value

		int regionID = data[index++];
		region = idToRegion.get(regionID);
		minerals = data[index++];
		gas = data[index++];
		island = (data[index++] == 1);
		mineralOnly = (data[index++] == 1);
		startLocation = (data[index++] == 1);
	}

	// /** The Position of the center of the BaseLocation */
	// public Position getCenter() {
	// return center;
	// }

	// /** The Position of the top left of the BaseLocation */
	// public Position getPosition() {
	// return position;
	// }

	// /** @deprecated use {@link #getPosition()} instead */
	// @Deprecated
	// public int getX() {
	// return position.getPX();
	// }
	//
	// /** @deprecated use {@link #getPosition()} instead */
	// @Deprecated
	// public int getY() {
	// return position.getPY();
	// }
	//
	// /** @deprecated use {@link #getPosition()} instead */
	// @Deprecated
	// public int getTx() {
	// return position.getBX();
	// }
	//
	// /** @deprecated use {@link #getPosition()} instead */
	// @Deprecated
	// public int getTy() {
	// return position.getBY();
	// }

	public Region getRegion() {
		return region;
	}

	/** @deprecated use {@link #getRegion()} instead */
	@Deprecated
	public int getRegionID() {
		return region.getID();
	}

	public int getMinerals() {
		return minerals;
	}

	public int getGas() {
		return gas;
	}

	public boolean isIsland() {
		return island;
	}

	public boolean isMineralOnly() {
		return mineralOnly;
	}

	public boolean isStartLocation() {
		return startLocation;
	}
}
