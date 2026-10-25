package atlantis.combat.focus;

import atlantis.map.choke.AChoke;
import atlantis.map.position.HasPosition;

/**
 * Which ground the army defends (NEXT #194, 02_COMBAT Squad target).
 *
 * <p>The mission names the place; the formation decides how to stand on
 * it (slots, standoff depths, plugs); micro executes. Depths and offsets
 * deliberately live elsewhere (L2): this seam answers L1 only.
 *
 * <p>Skeleton: pinned, not yet consumed. Slice 2 wires one consumer
 * 1:1 (ProtossDefendFocusPoint choke branches), later slices the rest.
 * Under AEC (CONVENTIONS §31) this is the designed home for every new
 * "where does the army stand" condition - not another inline if.
 */
public final class DefendedPlace {

    private DefendedPlace() {
    }

    /**
     * Priority order, highest first. The declaration order IS the
     * decision: first non-null input wins (pinned by DefendedPlaceTest).
     */
    public enum Kind {
        BREACH,
        SPARTA_CHOKE,
        MAIN_CHOKE,
        NATURAL_CHOKE,
        BASE
    }

    /** The defended ground plus why it was picked. */
    public static final class Answer {
        public final Kind kind;
        public final HasPosition ground;

        Answer(Kind kind, HasPosition ground) {
            this.kind = kind;
            this.ground = ground;
        }
    }

    /**
     * First non-null input in Kind order wins; null when nothing
     * qualifies (UMS/baseless) - the caller falls back, same as the old
     * null path. Pure: no statics, no cache (callers cache), no orders.
     */
    public static Answer defendGround(
        HasPosition breachEnemy,
        AChoke spartaChoke,
        AChoke mainChoke,
        AChoke naturalChoke,
        HasPosition base
    ) {
        if (breachEnemy != null) return new Answer(Kind.BREACH, breachEnemy);
        if (spartaChoke != null) return new Answer(Kind.SPARTA_CHOKE, spartaChoke);
        if (mainChoke != null) return new Answer(Kind.MAIN_CHOKE, mainChoke);
        if (naturalChoke != null) return new Answer(Kind.NATURAL_CHOKE, naturalChoke);
        if (base != null) return new Answer(Kind.BASE, base);

        return null;
    }
}
