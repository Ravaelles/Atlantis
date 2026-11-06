package atlantis.placement.core;

/**
 * Street grid doctrine (owner order 2026-11-06, reviving the old
 * ProtossForbiddenByStreetGrid spacing): buildings stand on fixed
 * street tiles so corridors stay walkable, instead of every frame
 * re-searching the whole map tile by tile. Pure tile math - zero
 * per-frame cost, pinned without a game.
 *
 * <p>Grid 11 x 7, origins only (the legacy rule read the origin tile,
 * so a building may overhang a street the same way it always did).
 * Bases and gas buildings are exempt (a Nexus must land on its base
 * location whatever the modulo says; a refinery sits on its geyser):
 * <ul>
 * <li>2x2 (Pylon/Cannon): X % 11 == 2, Y % 7 in {2, 4};</li>
 * <li>anything else: X % 11 in {2, 4, 6}, Y % 7 in {2, 5}.</li>
 * </ul>
 *
 * <p>Streets bind the production hunt and the layout, never sited or
 * exact demands: a cannon creeping a choke or a wall piece sits where
 * the fight needs it, on any buildable tile. The supply-blocked Pylon
 * keeps the legacy escape hatch (anywhere when hard-blocked) at the
 * door, not in the grid.</p>
 */
public final class StreetGrid {

    private StreetGrid() {
    }

    /**
     * True when a building of this type may originate on this tile.
     * Fail-open for nulls.
     */
    public static boolean isStreetLegal(int tileX, int tileY, atlantis.units.AUnitType type) {
        if (type == null) return true;
        if (type.isBase() || type.isGasBuilding()) return true;
        if (tileX < 0 || tileY < 0) return true;

        if (type.getTilesWidth() == 2 && type.getTilesHeights() == 2) {
            return tileX % 11 == 2 && (tileY % 7 == 2 || tileY % 7 == 4);
        }

        int mx = tileX % 11;
        int my = tileY % 7;

        return (mx == 2 || mx == 4 || mx == 6) && (my == 2 || my == 5);
    }
}
