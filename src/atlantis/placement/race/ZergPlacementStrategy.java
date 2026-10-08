package atlantis.placement.race;

import atlantis.placement.core.BuildBlock;
import atlantis.placement.core.RacePlacementStrategy;

import java.util.Collections;
import java.util.List;

/**
 * Zerg placement - <b>design only, not implemented</b>
 * (`_AI/redesign/03_PLACEMENT.md` §4.5, §5.4 S6).
 *
 * <p>
 * Zerg is the race that stresses the contract hardest, and the contract already
 * allows it: {@code framesUntilAvailable} is a number the strategy owns, so
 * "available" means "creep has spread here" instead of "a Pylon powers it" -
 * the
 * core never learns what a creep colony is.
 * </p>
 *
 * <p>
 * What it would answer when implemented: availability from a projected creep
 * front (the strategy receives the catalogue and the request, so it can consult
 * arbitrary game state); minimal block templates, since Zerg buildings have no
 * adjacency rules worth encoding; and no power concept at all.
 * </p>
 *
 * <p>
 * Until then it is a working no-op - available everywhere, no templates.
 * </p>
 */
public final class ZergPlacementStrategy implements RacePlacementStrategy {

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
