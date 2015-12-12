package jnibwapi;

import atlantis.information.AtlantisMap;

/** Generalised representation of a position for JNIBWAPI. Immutable. */
public class Position {

	public static class Positions {
		public static final Position Invalid = new Position(1000, 1000, PosType.BUILD);
		public static final Position None = new Position(1000, 1001, PosType.BUILD);
		public static final Position Unknown = new Position(1000, 1002, PosType.BUILD);
	}

	public static enum PosType {
		PIXEL(1), WALK(8), BUILD(32);

		/** Length in pixels */
		public final int scale;

		private PosType(int size) {
			this.scale = size;
		}
	};

	private int x;
	private int y;

	/**
	 * Creates a new Position representing the given x and y as Pixel, Walk Tile, or Build Tile coordinates (depending
	 * on the PosType given).
	 */
	public Position(int x, int y, PosType posType) {
		this.x = x * posType.scale;
		this.y = y * posType.scale;
	}

	/**
	 * Creates a new Position representing the given x and y as Pixel coordinates.
	 */
	public Position(int x, int y) {
		this(x, y, PosType.PIXEL);
	}

	/**
	 * Returns the x-coordinate, in the scale appropriate for the given type
	 * 
	 * @see {@link #getPX()}, {@link #getBX()}, {@link #getWX()}
	 */
	public int getX(PosType posType) {
		return x / posType.scale;
	}

	/**
	 * Returns the y-coordinate, in the scale appropriate for the given type
	 * 
	 * @see {@link #getPY()}, {@link #getBY()}, {@link #getWY()}
	 */
	public int getY(PosType posType) {
		return y / posType.scale;
	}

	/** Returns the x-coordinate, in pixels */
	public int getPX() {
		return x / PosType.PIXEL.scale;
	}

	/** Returns the y-coordinate, in pixels */
	public int getPY() {
		return y / PosType.PIXEL.scale;
	}

	/** Returns the x-coordinate, in walk tiles */
	public int getWX() {
		return x / PosType.WALK.scale;
	}

	/** Returns the y-coordinate, in walk tiles */
	public int getWY() {
		return y / PosType.WALK.scale;
	}

	/** Returns the x-coordinate, in build tiles */
	public int getBX() {
		return x / PosType.BUILD.scale;
	}

	/** Returns the y-coordinate, in build tiles */
	public int getBY() {
		return y / PosType.BUILD.scale;
	}

	/**
	 * Get an approximate distance to the target position in Pixel coordinates.
	 * 
	 * Uses Starcraft's approximated distance function, which is reasonably accurate yet avoids a sqrt operation and
	 * saves some CPU cycles.
	 * 
	 * @see #getPDistance(Position)
	 **/
	public int getApproxPDistance(Position target) {
		int min = Math.abs(x - target.x);
		int max = Math.abs(y - target.y);
		if (max < min) {
			int temp = max;
			max = min;
			min = temp;
		}

		if (min < (max >> 2))
			return max;

		int minCalc = (3 * min) >> 3;
		return ((minCalc >> 5) + minCalc + max - (max >> 4) - (max >> 6));
	}

	public int getApproxWDistance(Position target) {
		return getApproxPDistance(target) / PosType.WALK.scale;
	}

	public int getApproxBDistance(Position target) {
		return getApproxPDistance(target) / PosType.BUILD.scale;
	}

	/**
	 * Returns true if the position is on the map. Note: if map info is unavailable, this function will check validity
	 * against the largest (256x256) map size.
	 */
	public boolean isValid() {
		if (x < 0 || y < 0)
			return false;
		Map map;
		if (JNIBWAPI.getInstance() != null)
			map = JNIBWAPI.getInstance().getMap();
		else
			map = null;
		if (map == null)
			return getBX() < 256 && getBY() < 256;
		else
			return x < map.getSize().getPX() && y < map.getSize().getPY();
	}

	/**
	 * Returns a <b>new</b> Position in the closest valid map position. Worked out at Build Tile resolution, like in
	 * BWAPI 3. Note: if map info is unavailable, this function will check validity against the largest (256x256) map
	 * size.
	 */
	public Position makeValid() {
		if (isValid())
			return this;
		//
		int newBtX = Math.max(getBX(), 0);
		int newBtY = Math.max(getBY(), 0);
		Map map;
		if (JNIBWAPI.getInstance() != null)
			map = JNIBWAPI.getInstance().getMap();
		else
			map = null;
		if (map == null) {
			newBtX = Math.min(newBtX, 256 - 1);
			newBtY = Math.min(newBtY, 256 - 1);
		} else {
			newBtX = Math.min(newBtX, map.getSize().getBX() - 1);
			newBtY = Math.min(newBtY, map.getSize().getBY() - 1);
		}
		return new Position(newBtX, newBtY, PosType.BUILD);
	}

	/**
	 * Returns a <b>new</b> Position that represents the effect of moving this position by delta (treated as a vector
	 * from the origin)
	 */
	public Position translated(Position delta) {
		return new Position(x + delta.x, y + delta.y);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	/** Positions are the same as long as they have the same Pixel coordinates. */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Position other = (Position) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[" + getBX() + "," + getBY() + "]";
	}

	// =========================================================
	// ===== Start of ATLANTIS CODE ============================
	// =========================================================

	/**
	 * Returns distance to other position in build tiles. One build tile equals to 32 pixels. Usage of build tiles
	 * instead of pixels is preferable, because it's easier to imagine distances if one knows building dimensions.
	 */
	public double distanceTo(Position position) {
		int dx = x - position.x;
		int dy = y - position.y;

		// Calculate approximate distance between the units. If it's less than let's say X tiles, we probably should
		// consider calculating more precise value
		double distanceApprx = getApproxBDistance(position);

		// Precision is fine, return approx value
		if (distanceApprx > 4.5) {
			return distanceApprx;
		}

		// Unit is too close and we need to know the exact distance, not approximization.
		else {
			return Math.sqrt(dx * dx + dy * dy) / 32;
		}
	}

	/**
	 * Returns distance to other position in pixels. Please use version "distanceTo" that uses build tiles.
	 */
	public double distanceToInPixels(Position position) {
		// Position properOurPosition = this;
		// if (properOurPosition instanceof Unit) {
		// properOurPosition = ((Unit) properOurPosition).getPosition();
		// }
		//
		// Position properOtherPosition = position;
		// if (properOtherPosition instanceof Unit) {
		// properOtherPosition = ((Unit) properOtherPosition).getPosition();
		// }
		//
		// int dx = properOurPosition.x - properOtherPosition.x;
		// int dy = properOurPosition.y - properOtherPosition.y;
		int dx = x - position.x;
		int dy = y - position.y;

		return Math.sqrt(dx * dx + dy * dy);
	}

	/**
	 * Returns ground distance to given position (in build tiles) or negative value if there's no connection.
	 */
	public double distanceGroundTo(Position position) {
		return AtlantisMap.getMap().getGroundDistance(this, position) / 32;
	}

	/**
	 * Returns a <b>new</b> Position that represents the effect of moving this position by [deltaX, deltaY].
	 */
	public Position translated(int deltaPixelX, int deltaPixelY) {
		return new Position(x + deltaPixelX, y + deltaPixelY);
	}

	// =========================================================
	// Setters

	/**
	 * <b>Be extra careful using it. Normally, you don't need to use it *ever*.</b>
	 */
	public void setPixelX(int x) {
		this.x = x;
	}

	/**
	 * <b>Be extra careful using it. Normally, you don't need to use it *ever*.</b>
	 */
	public void setPixelY(int y) {
		this.y = y;
	}

}
