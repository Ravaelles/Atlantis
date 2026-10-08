package atlantis.production.v2;

/**
 * A concrete production facility: an existing unit (Gateway #138) or a
 * building planned earlier in the same pass. The id lets the plan name the
 * producer, so the dispatcher commands exactly that one and one Gateway never
 * receives two items for the same slot.
 *
 * <p>
 * Pure domain - the registry builds these from the game; tests build them
 * by hand. Planned facilities get negative ids, so they never collide with a
 * game unit id.
 * </p>
 */
public final class ProducerFacility {

    private final int id;
    private final String typeId;
    private final int availableFromFrame;

    public ProducerFacility(int id, String typeId, int availableFromFrame) {
        this.id = id;
        this.typeId = typeId;
        this.availableFromFrame = Math.max(0, availableFromFrame);
    }

    public int id() {
        return id;
    }

    public String typeId() {
        return typeId;
    }

    /** Absolute frame this facility can start producing something new. */
    public int availableFromFrame() {
        return availableFromFrame;
    }

    public boolean isPlanned() {
        return id < 0;
    }

    @Override
    public String toString() {
        return typeId + "#" + id + "@ready:" + availableFromFrame;
    }
}
