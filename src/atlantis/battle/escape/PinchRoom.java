package atlantis.battle.escape;

import atlantis.map.position.APosition;

/**
 * Pinch standoff gate (NEXT #159/#167): a pinched Dragoon yields only
 * with room to kite into - without it the retreat trades the pocket
 * for a corner, and standing wins that trade (Wine N=5+5). Unknown
 * walkability fails OPEN (old behavior), never closed: the gate may
 * only hold back a yield it can see has nowhere to go.
 */
public final class PinchRoom {

    private PinchRoom() {
    }

    /** Longest walkable ray that counts as room to kite into. */
    public static final int STANDOFF_TILES = 5;

    public static boolean hasRoom(APosition pos, EscapeRays.Walkability walk) {
        if (pos == null || walk == null) return true;

        try {
            for (double clear : EscapeRays.clearance(pos, STANDOFF_TILES, walk)) {
                if (clear >= STANDOFF_TILES) return true;
            }
        } catch (Exception ignored) {
            return true;
        }
        return false;
    }
}
