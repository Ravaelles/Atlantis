package atlantis.production.v2;

/**
 * A production facility: an existing unit (Gateway) or a building planned
 * earlier in the same pass. The scheduler picks the facility that can produce
 * the item earliest.
 *
 * <p>
 * Pure domain - the registry builds these from the game; tests build them
 * by hand. The id is the producer type id from
 * {@link Producible#producerTypeId()},
 * which is how an item finds its facilities.
 * </p>
 */
public final class ProducerFacility {

    private final String typeId;
    private final int availableFromFrame;

    public ProducerFacility(String typeId, int availableFromFrame) {
        this.typeId = typeId;
        this.availableFromFrame = Math.max(0, availableFromFrame);
    }

    public static ProducerFacility ready(String typeId) {
        return new ProducerFacility(typeId, 0);
    }

    public String typeId() {
        return typeId;
    }

    /** First frame this facility can start producing something new. */
    public int availableFromFrame() {
        return availableFromFrame;
    }

    @Override
    public String toString() {
        return typeId + "@ready:" + availableFromFrame;
    }
}
