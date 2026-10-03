package atlantis.core.world;

import atlantis.map.position.APosition;
import atlantis.units.AUnitType;

/**
 * Stage E (see _AI/REVIEW.md §16): immutable per-frame projection of one unit.
 *
 * <p>A snapshot carries <b>data only</b>: no behaviour, no engine references,
 * no mutability. Systems read snapshots; they never mutate units through them.
 * The snapshot is produced fresh every frame by {@link World}; consumers must
 * never cache it across frames.</p>
 *
 * <p>Backed initially by {@code AUnit} (see {@link UnitSnapshots}); the
 * backing will move to the {@code World} registry as Stage E progresses.</p>
 */
public final class UnitSnapshot {

    private final int id;
    private final AUnitType type;
    private final APosition position;
    private final int hp;
    private final int shields;

    /**
     * Whether {@link #hp()} is a reading or a stand-in. Live units always know
     * theirs (even 0 for the dead is known); fogged units do not - their
     * {@code hp()} carries the compensated value ({@code maxHp()}, for army
     * sums) and must not be mistaken for "full health" by rules like
     * most-wounded targeting.
     *
     * <p>Why a flag and not {@code OptionalInt} or a sentinel: a snapshot is
     * built per unit per frame, so boxing allocates in the hot loop, and a
     * bare sentinel (-69) leaks into every comparison site - the same failure
     * mode as the old magic numbers. The flag keeps {@code hp()} a plain int
     * for the readers that do not care, and an explicit question for the ones
     * that do. See {@code _AI/NEXT.md} #2.</p>
     */
    private final boolean hpKnown;

    public UnitSnapshot(int id, AUnitType type, APosition position, int hp, int shields) {
        this(id, type, position, hp, shields, true);
    }

    public UnitSnapshot(int id, AUnitType type, APosition position, int hp, int shields, boolean hpKnown) {
        this.id = id;
        this.type = type;
        this.position = position;
        this.hp = hp;
        this.shields = shields;
        this.hpKnown = hpKnown;
    }

    public int id() {
        return id;
    }

    public AUnitType type() {
        return type;
    }

    public APosition position() {
        return position;
    }

    public int hp() {
        return hp;
    }

    public boolean hasKnownHp() {
        return hpKnown;
    }

    public int shields() {
        return shields;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    @Override
    public String toString() {
        return type + " #" + id + " (" + hp + "hp) @ " + position;
    }
}
