package atlantis.production.v2;

import atlantis.map.position.APosition;
import atlantis.production.v2.PylonPlacementScore.Tile;

import java.util.ArrayList;
import java.util.List;

/**
 * Power-smart Pylon siting (Phase 5(a) slice): among several VALIDATED
 * candidate tiles, warp where the Pylon unlocks the most unpowered
 * building room - not where the finder happened to land first (measured
 * 2026-10-19: V2 Pylons land wherever, powered space fills, tech
 * starves behind unpowered Core/Forge ground).
 *
 * <p>Pure over points: the planner validates (each candidate went through
 * the full find chain) and reads the map once; this only ranks. Ties keep
 * the first candidate, so the finder's own preference survives instead of
 * being overwritten - same contract as {@link PylonPlacementScore#best}.
 * With fewer than two distinct candidates there is nothing to choose and
 * the first tile stands.</p>
 */
public final class PylonPowerSiting {

    private PylonPowerSiting() {
    }

    /**
     * @return the candidate to build on; never null when the list is not
     * empty (first tile wins ties and singletons).
     */
    public static APosition best(
        List<APosition> validated, List<Tile> buildableTiles, List<Tile> alreadyPowered
    ) {
        if (validated == null || validated.isEmpty()) return null;
        if (validated.size() == 1) return validated.get(0);

        List<Tile> candidates = new ArrayList<>();
        for (APosition position : validated) {
            if (position != null) candidates.add(new Tile(position.tx(), position.ty()));
        }
        if (candidates.isEmpty()) return null;

        Tile best = PylonPlacementScore.best(candidates, buildableTiles, alreadyPowered);
        if (best == null) return validated.get(0);

        for (APosition position : validated) {
            if (position != null && position.tx() == best.x && position.ty() == best.y) return position;
        }
        return validated.get(0);
    }
}
