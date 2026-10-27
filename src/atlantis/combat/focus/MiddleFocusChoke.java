package atlantis.combat.focus;

import atlantis.map.choke.AChoke;
import atlantis.map.path.PathToEnemyBase;

import java.util.ArrayList;

/**
 * The middle choke on the path to the enemy base: the ground a Protoss
 * army stages on once it is big enough to leave the main choke. Same
 * semantics as the deleted {@code combat.advance.focus_choke} version
 * (middle = {@code chokes.get(size / 2)}, null when the path names no
 * choke) - new home next to the decision that consumes it.
 */
public final class MiddleFocusChoke {

    private MiddleFocusChoke() {
    }

    /**
     * The middle choke of the path to the enemy base, or null when the
     * path names none (UMS/baseless, or the path is not known yet) -
     * the caller falls through to the next defended ground, same as the
     * old null path. Null-safe: never throws.
     */
    public static AChoke get() {
        try {
            ArrayList<AChoke> chokes = PathToEnemyBase.chokesLeadingToEnemyBase();
            if (chokes == null || chokes.isEmpty()) return null;

            return chokes.get(chokes.size() / 2);
        } catch (RuntimeException ignored) {
            return null;
        }
    }
}
