package atlantis.placement.strategy;

import atlantis.map.position.APosition;
import atlantis.units.AUnitType;

import java.util.Set;
import java.util.function.BiPredicate;

/**
 * One rule for choosing a building tile (owner 2026-11-09): Pylons own
 * range, Gateways sit on existing power, everything else follows the
 * street grid. A strategy is a pure preference - it names a tile, it
 * never orders, queues or validates beyond its own shape. The legality
 * ladder downstream ({@code SitingVerdict}, physics, overlap,
 * reachability) still owns correctness, so a strategy can never serve
 * an unbuildable tile, only a shapely one.
 *
 * <p>Null means "no tile by my rules" - the caller falls back to the
 * legacy hunt (which is also the revert path: unplug the strategy and
 * the old behavior is back byte for byte).</p>
 */
public interface BuildingPlacementStrategy {

    /**
     * The preferred tile for this query, or null when no tile follows
     * the strategy's rules. Never throws: a failed read answers null,
     * never an exception - siting must fail closed into the fallback.
     */
    APosition find(PlacementQuery query);

    /**
     * What a strategy needs to know: the building, the anchor the old
     * spiral searched around, the radius when the caller bounded one,
     * tiles already refused this pass, and the caller's occupancy rule
     * (standing + unfinished footprints). Read-only view, no game writes.
     */
    final class PlacementQuery {
        private final AUnitType building;
        private final APosition centre;
        private final double radius;
        private final Set<String> excluded;
        private final BiPredicate<APosition, AUnitType> occupied;

        public PlacementQuery(
            AUnitType building,
            APosition centre,
            double radius,
            Set<String> excluded,
            BiPredicate<APosition, AUnitType> occupied
        ) {
            this.building = building;
            this.centre = centre;
            this.radius = radius;
            this.excluded = excluded;
            this.occupied = occupied;
        }

        public AUnitType building() {
            return building;
        }

        public APosition centre() {
            return centre;
        }

        public double radius() {
            return radius;
        }

        public Set<String> excluded() {
            return excluded;
        }

        public boolean isExcluded(int tx, int ty) {
            try {
                return excluded != null && excluded.contains(tx + ":" + ty);
            } catch (Exception | Error ignored) {
                return false;
            }
        }

        public boolean isOccupied(APosition tile, AUnitType type) {
            if (tile == null || occupied == null) return false;
            try {
                return occupied.test(tile, type);
            } catch (Exception | Error ignored) {
                return true;
            }
        }
    }
}
