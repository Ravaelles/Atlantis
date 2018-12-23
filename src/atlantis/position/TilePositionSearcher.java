package atlantis.position;

import atlantis.information.AMap;

public abstract class TilePositionSearcher {
    private APosition initialPosition;

    // =========================================================

    public TilePositionSearcher(APosition initialPosition) {
        this.initialPosition = initialPosition;
    }

    // =========================================================

    abstract boolean isAcceptablePosition(APosition potentialPosition);

    // =========================================================

    public final APosition search() {
        int radius = 1;
        int maxRadius = 20;

        int initTX = initialPosition.getTileX();
        int initTY = initialPosition.getTileY();

        while (radius < maxRadius) {
            int xCounter = 0;
            int yCounter = 0;
            int doubleRadius = radius * 2;

            // Search horizontally
            int minTileX = Math.max(0, initTX - radius);
            int maxTileX = Math.min(initTX + radius, AMap.getMapWidthInTiles() - 1);
            for (int tileX = minTileX; tileX <= maxTileX; tileX++) {

                // Search vertically
                int minTileY = Math.max(0, initTY - radius);
                int maxTileY = Math.min(initTY + radius, AMap.getMapHeightInTiles() - 1);
                for (int tileY = minTileY; tileY <= maxTileY; tileY++) {
                    if (xCounter == 0 || yCounter == 0 || xCounter == doubleRadius || yCounter == doubleRadius) {
                        APosition potentialPosition = APosition.create(tileX, tileY);
                        if (isAcceptablePosition(potentialPosition)) {
                            return potentialPosition;
                        }
                    }

                    yCounter++;
                }
                xCounter++;
            }

            radius++;
        }

        throw new RuntimeException("Can't find acceptable position, maxRadius: " + maxRadius + ", initialPosition: " + this);
    }
}
