package atlantis.position;

import bwapi.Point;

import java.util.Objects;

public class LargeTile implements Comparable<LargeTile> {

    public static final int LARGE_TILE_TILES = 6;

    private int largeX;
    private int largeY;

    public LargeTile(HasPosition fromPosition) {
        this.largeX = fromPosition.x() / (32 * LARGE_TILE_TILES);
        this.largeY = fromPosition.y() / (32 * LARGE_TILE_TILES);
    }

    @Override
    public String toString() {
        return "LargeTile{" + largeX + "," + largeY + "}";
    }

    //    @Override
//    public APosition getPosition() {
//        return null;
//    }

//    @Override
//    public int getX() {
//        return x;
//    }
//
//    @Override
//    public int getY() {
//        return y;
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LargeTile)) return false;
        LargeTile largeTile = (LargeTile) o;
        return largeX == largeTile.largeX && largeY == largeTile.largeY;
    }

    @Override
    public int hashCode() {
        return Objects.hash(largeX, largeY);
    }

    @Override
    public int compareTo(LargeTile o) {
        int compare = Integer.compare(largeX, o.largeX);
        if (compare == 0) {
            compare = Integer.compare(largeY, o.largeY);
        }
        return compare;
    }
}
