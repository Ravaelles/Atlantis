package atlantis.placement.engine;

import atlantis.placement.core.BuildLocation;
import atlantis.placement.core.BuildLocationCatalogue;

/**
 * Shared first key of every location ranking: a tile usable now beats one
 * that waits, and an impossible one ({@code framesUntilAvailable < 0})
 * sorts last. One method so availability orders identically everywhere -
 * a second opinion on "which tile is usable sooner" would be a second
 * source of truth.
 */
final class AvailabilityFirst {

    private AvailabilityFirst() {
    }

    static int compare(BuildLocation a, BuildLocation b) {
        return Integer.compare(normalise(a.framesUntilAvailable()), normalise(b.framesUntilAvailable()));
    }

    private static int normalise(int framesUntilAvailable) {
        return framesUntilAvailable < 0 ? Integer.MAX_VALUE : framesUntilAvailable;
    }
}
