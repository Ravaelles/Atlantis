package atlantis.production.constructions.position;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnitType;

import java.util.HashMap;
import java.util.Map;

/**
 * Engine-refused tiles (owner #151): tiles the engine refused a Build on
 * while our own math said buildable, with the builder present and still.
 * The refresh loop re-serves the same cursed tile forever (003802:
 * 3000+ BUILD_REFUSED_WHY on [119,12]; 184529: 3869 attempts on one
 * Nexus tile) - remembering the refusal breaks the chase at the second
 * attempt instead of the three-thousandth.
 *
 * <p>Self-correcting, not a blacklist: entries expire after
 * {@link #REFUSAL_TTL_FRAMES} frames, and a tile that stops refusing
 * simply stops being re-added. Transient refusals (move-order timing,
 * a unit standing on the tile) age out on their own; hard refusals
 * (engine says no in every flag combination) re-add every time they
 * fire, so they stay refused as long as they keep refusing.</p>
 *
 * <p>Read by the catalogue ({@code propose} declines refused tiles with
 * a twin); the legacy spiral does not consult it yet - the twin names
 * which path answered, so a legacy-served refused tile stays visible.</p>
 */
public final class RefusedTiles {

    /** Long enough to outlive any chase, short enough to forgive a freed tile. */
    public static final int REFUSAL_TTL_FRAMES = 2400;

    private static final Map<String, Integer> refusedAt = new HashMap<>();

    private RefusedTiles() {
    }

    public static void note(HasPosition tile, AUnitType building) {
        if (tile == null || building == null) return;

        try {
            APosition p = APosition.create(tile);
            refusedAt.put(key(p.tx(), p.ty(), building), A.now());
        } catch (Exception | Error ignored) {
        }
    }

    public static boolean isRefused(HasPosition tile, AUnitType building) {
        if (tile == null || building == null) return false;

        try {
            APosition p = APosition.create(tile);
            Integer at = refusedAt.get(key(p.tx(), p.ty(), building));
            if (at == null) return false;

            return A.now() - at < REFUSAL_TTL_FRAMES;
        } catch (Exception | Error ignored) {
            return false;
        }
    }

    public static void clearForTests() {
        refusedAt.clear();
    }

    private static String key(int tx, int ty, AUnitType building) {
        return tx + "," + ty + "," + building.id();
    }
}
