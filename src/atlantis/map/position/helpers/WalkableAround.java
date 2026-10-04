package atlantis.map.position.helpers;

import atlantis.map.MapTiles;
import atlantis.map.position.APosition;

public class WalkableAround {
    /**
     * Walkable here, and walkable on the four diagonals {@code alsoCheckTilesInRadius}
     * away.
     *
     * <p>Every tile question goes through {@link MapTiles}, the port that already
     * answers {@code HasPosition.isWalkable()} - so this class used to be the one
     * place that hand-wrote {@code if (Env.isTesting()) return true;} in front of the
     * engine call, which is the thing the port exists to remove (MapTiles' javadoc
     * names five such branches in HasPosition alone). In a game the answers are the
     * same ones as before ({@code MapTiles.useEngine()} asks the engine, and
     * {@code position.isWalkable()} was itself a delegation to this port); in a stub
     * world the harness decides, and FakeMapTiles says every tile is walkable.</p>
     */
    public static boolean isWalkable(APosition position, int alsoCheckTilesInRadius) {
        if (!MapTiles.isWalkable(position)) {
            return false;
        }

//        for (int i = 0; i < alsoCheckTilesInRadius; i++) {
//            APosition pos;

            if (!MapTiles.isWalkable(position.translateByTiles(alsoCheckTilesInRadius, alsoCheckTilesInRadius))) return false;
            if (!MapTiles.isWalkable(position.translateByTiles(-alsoCheckTilesInRadius, -alsoCheckTilesInRadius))) return false;
            if (!MapTiles.isWalkable(position.translateByTiles(alsoCheckTilesInRadius, -alsoCheckTilesInRadius))) return false;
            if (!MapTiles.isWalkable(position.translateByTiles(-alsoCheckTilesInRadius, alsoCheckTilesInRadius))) return false;
//        }

//        int currentRadius = Math.min(2, alsoCheckTilesInRadius);
//        int maxRadius = alsoCheckTilesInRadius;
//        while (currentRadius <= maxRadius) {
//            int step = maxRadius;
//            for (int dtx = -currentRadius; dtx <= currentRadius; dtx += step) {
//                for (int dty = -currentRadius; dty <= currentRadius; dty += step) {
//                    if (
//                        dtx == -currentRadius || dtx == currentRadius
//                            || dty == -currentRadius || dty == currentRadius
//                    ) {
//                        position = position.translateByTiles(dtx, dty);
//                        if (!position.isWalkable()) {
//                            return false;
//                        }
//                    }
//                }
//            }
//
//            currentRadius += step;
//        }

        return true;
    }
}
