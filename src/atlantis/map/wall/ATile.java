package atlantis.map.wall;

import bwem.Tile;

public class ATile {
    private final int tx;
    private final int ty;
    private final Tile tile;

    public ATile(int tx, int ty, Tile tile) {
        this.tx = tx;
        this.ty = ty;
        this.tile = tile;
    }

    public int tx() {
        return tx;
    }

    public int ty() {
        return ty;
    }
}
