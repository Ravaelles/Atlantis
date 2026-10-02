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

    public UnitSnapshot(int id, AUnitType type, APosition position, int hp, int shields) {
        this.id = id;
        this.type = type;
        this.position = position;
        this.hp = hp;
        this.shields = shields;
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
