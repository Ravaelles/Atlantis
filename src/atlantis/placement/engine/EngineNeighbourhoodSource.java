package atlantis.placement.engine;

import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.placement.core.NeighbourhoodRegistry;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

/**
 * The engine-backed {@link NeighbourhoodRegistry.Source}: base positions and
 * the
 * main choke, through existing map ports.
 *
 * <p>
 * "Which neighbourhood is this tile in" is answered as "the base nearest it",
 * which is all the ranking needs (Stardust keeps named area sets because it
 * also
 * stamps whole-map Blocks per area; S3 ranks within one base, and the named
 * sets
 * are a later refinement the port already allows).
 * </p>
 */
public final class EngineNeighbourhoodSource implements NeighbourhoodRegistry.Source {

    @Override
    public NeighbourhoodRegistry.Neighbourhood nearestTo(int tx, int ty) {
        AUnit base = nearestBase(tx, ty);
        if (base == null)
            return null;

        AChoke exit = Chokes.mainChoke();
        boolean hasExit = exit != null;

        return new NeighbourhoodRegistry.Neighbourhood(
                base.tx(), base.ty(),
                hasExit ? exit.tx() : 0,
                hasExit ? exit.ty() : 0,
                hasExit);
    }

    /**
     * The base nearest the tile. Uses {@code mainOrAnyBuilding} as the anchor when
     * no owned base is in the lists yet (the Nexus is still a construction), so
     * placement works from the first frames.
     */
    private static AUnit nearestBase(int tx, int ty) {
        AUnit nearest = null;

        for (AUnit base : Select.ourBasesWithUnfinished().list()) {
            if (nearest == null) {
                nearest = base;
                continue;
            }
            if (dist(base, tx, ty) < dist(nearest, tx, ty))
                nearest = base;
        }

        if (nearest == null)
            nearest = Select.main();
        return nearest;
    }

    private static int dist(AUnit unit, int tx, int ty) {
        int dx = Math.abs(unit.tx() - tx);
        int dy = Math.abs(unit.ty() - ty);
        return Math.max(dx, dy);
    }
}
