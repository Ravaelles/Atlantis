package atlantis.map.position.helpers;

import atlantis.config.env.Env;
import atlantis.map.position.APosition;

public class WalkableAround {
    public static boolean isWalkable(APosition position, int alsoCheckTilesInRadius) {
        if (Env.isTesting()) return true;

        if (!position.isWalkable()) {
            return false;
        }

        int currentRadius = Math.min(2, alsoCheckTilesInRadius);
        int maxRadius = alsoCheckTilesInRadius;
        while (currentRadius <= maxRadius) {
            int step = maxRadius;
            for (int dtx = -currentRadius; dtx <= currentRadius; dtx += step) {
                for (int dty = -currentRadius; dty <= currentRadius; dty += step) {
                    if (
                        dtx == -currentRadius || dtx == currentRadius
                            || dty == -currentRadius || dty == currentRadius
                    ) {
                        position = position.translateByTiles(dtx, dty);
                        if (!position.isWalkable()) {
                            return false;
                        }
                    }
                }
            }

            currentRadius += step;
        }

        return true;
    }
}
