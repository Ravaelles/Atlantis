package atlantis.production.v2;

import java.util.ArrayList;
import java.util.List;

/**
 * A game adapter question: "which facilities of this type exist right now, and
 * which will exist within this plan?".
 *
 * <p>
 * The production engine (M4) answers from live units plus the plan's own
 * earlier items; a test answers from a hand-built list. Keeping this as an
 * interface is the DIP seam that lets the scheduler run without a game.
 * </p>
 */
public interface ProducerFacilityRegistry {

    /**
     * Facilities of the given type, sorted by availability frame (earliest first).
     */
    List<ProducerFacility> facilitiesOf(String typeId);
}
