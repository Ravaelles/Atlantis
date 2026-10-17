package atlantis.placement.core;

/**
 * What a tile's weapon covers: the choke line, the anchor behind it, both
 * or neither. The verdict a coverage ranking decided, twinned so a game log
 * can attribute it. A value, not two booleans in order - the next coverage
 * axis extends this type instead of widening a signature.
 */
public final class CoverageVerdict {

    private final boolean choke;
    private final boolean anchor;

    public CoverageVerdict(boolean choke, boolean anchor) {
        this.choke = choke;
        this.anchor = anchor;
    }

    public boolean choke() {
        return choke;
    }

    public boolean anchor() {
        return anchor;
    }
}
