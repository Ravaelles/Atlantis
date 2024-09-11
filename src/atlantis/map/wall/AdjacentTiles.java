package atlantis.map.wall;

import atlantis.map.position.APosition;
import bwem.Tile;

public class AdjacentTiles {
    public static APosition[] to(APosition current) {
        return new APosition[]{
            current.left(),
            current.right(),
            current.top(),
            current.bottom()
        };
    }

//    public static ATile[] to(Tile current) {
//
//    }
}
