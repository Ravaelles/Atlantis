package atlantis.placement.race;

import atlantis.placement.core.BuildBlock;
import atlantis.placement.core.RacePlacementStrategy;

import java.util.Collections;
import java.util.List;

/**
 * Terran placement - <b>design only, not implemented</b>
 * (`_AI/redesign/03_PLACEMENT.md` §4.5, §5.4 S6).
 *
 * <p>
 * The contract already accommodates Terran, so this class exists to say so in
 * code rather than in a comment. What it would answer, when someone implements
 * it:
 * </p>
 * <ul>
 * <li>{@code framesUntilAvailable} from <b>addon availability</b> - a Barracks
 * with a Tech Lab or Reactor frees the next production slot, which is the
 * Terran analogue of "wait for the Pylon";</li>
 * <li>lifted buildings as a state distinct from {@code USED} (the grid would
 * need the {@code SOFT_USED} flag the spec calls for), so a temporarily
 * lifted building does not permanently block its tile;</li>
 * <li>{@code requiresAvailability} true for addon-building types only - there
 * is
 * no Psi in Terran, so the answer is never "the terrain is not powered".</li>
 * </ul>
 *
 * <p>
 * Until then it answers "available everywhere" and offers no templates, which
 * makes it a working no-op rather than a source of wrong placements.
 * </p>
 */
public final class TerranPlacementStrategy implements RacePlacementStrategy {

    @Override
    public List<BuildBlock.Spec> blockTemplates() {
        return Collections.emptyList();
    }

    @Override
    public int framesUntilAvailable(int tx, int ty, String buildableTypeId) {
        return 0;
    }

    @Override
    public boolean requiresAvailability(String buildableTypeId) {
        return false;
    }
}
