package atlantis.map.analysis;

import atlantis.map.position.APosition;
import atlantis.units.AUnitType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Which wall pieces plug a choke, in order, from a tile row.
 *
 * <p>
 * Pure: anchor slots + door + choke width in, piece orders out (or empty =
 * no wall). Pylon first (power), Gateway second (plug), then cannons; the
 * door tile never takes a piece. The fit rule is honest, not tight: plug
 * width (2 + 4 + 2 per cannon) must cover the choke minus the door tile,
 * otherwise the choke is too wide and the answer is cannons-as-spots
 * (the fortification path), never a gappy wall. Exact footprint packing
 * (no shared tiles, no one-tile holes) is NOT solved here - that is the
 * documented gap the JBWEB fork owns (04_MAPLIB M4); this plans the demand
 * the game proves.
 * </p>
 */
public final class WallPieces {

    /** One piece order: what, top-left where. */
    public static final class Piece {
        private final AUnitType type;
        private final APosition tile;

        Piece(AUnitType type, APosition tile) {
            this.type = type;
            this.tile = tile;
        }

        public AUnitType type() {
            return type;
        }

        public APosition tile() {
            return tile;
        }
    }

    private WallPieces() {
    }

    /**
     * @param slots anchor tiles in segment order (either direction; oriented
     * inside so the power piece sits base-side)
     * @param door the tile our units pass; never takes a piece, may be null
     * @param baseSide our base behind the choke; orients the row
     * @param chokeWidthTiles choke width in tiles ({@code AChoke.width()})
     * @param cannonsWanted guns the wall may spend (charter: 1-2)
     * @param kit the race's wall vocabulary (Protoss wall-in today)
     * @return piece orders, or empty when the choke is too wide to plug
     */
    public static List<Piece> plan(
        List<APosition> slots, APosition door, APosition baseSide,
        double chokeWidthTiles, int cannonsWanted, WallKit kit
    ) {
        List<Piece> pieces = new ArrayList<>();
        if (slots == null || slots.isEmpty() || kit == null) return pieces;

        List<APosition> row = new ArrayList<>(slots);
        if (baseSide != null && !row.isEmpty()
            && row.get(row.size() - 1).distTo(baseSide) < row.get(0).distTo(baseSide)) {
            Collections.reverse(row);
        }

        List<APosition> open = new ArrayList<>();
        for (APosition slot : row) {
            if (door != null && slot.tx() == door.tx() && slot.ty() == door.ty()) continue;
            open.add(slot);
        }

        int gunsNeeded = gunsNeeded(chokeWidthTiles, kit);
        if (gunsNeeded > cannonsWanted) return new ArrayList<Piece>();
        if (open.size() < 2 + gunsNeeded) return new ArrayList<Piece>();

        pieces.add(new Piece(kit.powerPiece(), open.get(0)));
        pieces.add(new Piece(kit.plugPiece(), open.get(1)));
        for (int i = 0; i < gunsNeeded; i++) {
            pieces.add(new Piece(kit.gunPiece(), open.get(2 + i)));
        }
        return pieces;
    }

    /**
     * Plug width covers the choke minus the door tile. Zero or negative need
     * means power + plug suffice.
     */
    public static int gunsNeeded(double chokeWidthTiles, WallKit kit) {
        double plug = chokeWidthTiles - 1;
        double have = kit.powerWidth() + kit.plugWidth();
        if (plug <= have) return 0;

        return (int) Math.ceil((plug - have) / kit.gunWidth());
    }
}
