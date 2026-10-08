package atlantis.production.v2;

import atlantis.map.base.BaseLocations;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.position.APositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The production-v2 placement adapter over the legacy position finder.
 *
 * <p>
 * M3 of _AI/redesign/01_PRODUCTION.md: the scheduler asks this seam where a
 * planned building will stand; the implementation delegates to
 * {@link APositionFinder#findStandardPosition} (the same code the legacy
 * construction pipeline uses, including its BWEM/map logic). Nothing in the
 * legacy finder changes - v2 consumes it, exactly the strangler shape the
 * redesign prescribes (phase-out of {@code Construction/**} comes later, and
 * this seam is what decouples the scheduler from it until then).
 * </p>
 *
 * <p>
 * {@code reservePlacement} is where a builder is chosen and the tile is
 * validated as buildable now; a failed reservation skips the item for this
 * pass and the next frame's pass retries. Tiles reserved earlier in the same
 * pass are excluded from the finder (otherwise two Pylons planned in one frame
 * would get the same spot - the position finder caches per builder/type/near
 * and knows nothing about this pass), and one builder is chosen per pass, not
 * per building.
 * </p>
 *
 * <p>
 * Block templates and pylon-power scoring (Stardust's richer model) are later
 * refinements behind this same interface - they change the body of this class
 * and nothing else, which is the whole point of the seam.
 * </p>
 * <p>
 * Pass-scoped state is reset by {@link ResourceTimeline#originFrame()}-independent
 * {@link #startPass()}: two identical buildings in one frame would otherwise
 * receive the same tile.
 * </p>
 */
public class LegacyPlacementPlanner implements PlacementPlanner {

    /**
     * Ranking seam for candidates: the legacy finder returns one validated tile,
     * so today there is nothing to choose between (see the NOTE in
     * {@link #reservePlacement}). When a finder exposes several validated
     * candidates, only this resolver is replaced - the scheduler does not
     * change.
     */
    public interface CandidateResolver {
        int choose(List<APosition> candidates, Producible building);
    }

    private static final CandidateResolver FIRST_CANDIDATE = new CandidateResolver() {
        @Override
        public int choose(List<APosition> candidates, Producible building) {
            return candidates.isEmpty() ? -1 : 0;
        }
    };

    private CandidateResolver candidateResolver = FIRST_CANDIDATE;

    public void useCandidateResolver(CandidateResolver resolver) {
        this.candidateResolver = resolver != null ? resolver : FIRST_CANDIDATE;
    }

    /** Build tiles already claimed in the current pass: "x:y". */
    private final Set<String> reservedTilesThisPass = new HashSet<>();

    /** Chosen once per pass: every building in a frame is served by one builder. */
    private AUnit passBuilder;

    /**
     * Reservations of the last pass, replayed to the dispatcher: a building
     * whose builder is already walking keeps reporting its committed frame, so
     * it is re-committed idempotently instead of being orphaned when the builder
     * dies (01_PRODUCTION.md §5A).
     */
    private final List<PlacementReservation> lastPassReservations = new ArrayList<>();

    @Override
    public void startPass() {
        reservedTilesThisPass.clear();
        lastPassReservations.clear();
        passBuilder = null;
    }

    @Override
    public PlacementReservation reservePlacement(Producible building, TargetPlacement constraint, int targetFrame) {
        AUnitType unitType = resolveUnitType(building);
        if (unitType == null)
            return PlacementReservation.failure();

        AUnit builder = builderForThisPass();
        if (builder == null)
            return PlacementReservation.failure();

        // One candidate from the legacy finder, which already excludes buildings
        // on the map and honours the constraint.
        //
        // NOTE for the next step (01_PRODUCTION.md's BuildPositionResolver):
        // choosing among SEVERAL candidate tiles by Pylon power value needs a
        // finder that can return more than one, which the legacy
        // findStandardPosition does not expose. `PylonPlacementScore` is written
        // and tested for that step; wiring it here would mean inventing
        // candidates the finder never validated, which is worse than the single
        // validated tile we get today.
        HasPosition near = resolveNear(constraint);
        APosition position = APositionFinder.findStandardPosition(builder, unitType, near, 12);
        if (position == null)
            return PlacementReservation.failure();

        List<APosition> candidates = new ArrayList<>();
        candidates.add(position);
        int chosen = candidateResolver.choose(candidates, building);
        if (chosen < 0)
            return PlacementReservation.failure();
        position = candidates.get(chosen);

        String tile = position.tx() + ":" + position.ty();
        if (!reservedTilesThisPass.add(tile))
            return PlacementReservation.failure();

        PlacementReservation reservation = PlacementReservation.success(position.tx(), position.ty(), targetFrame);
        lastPassReservations.add(reservation);
        return reservation;
    }

    /**
     * Reservation carried over from the previous pass for this item, already
     * marked as committed, or null when the item is new or was never started.
     */
    public PlacementReservation carriedOver(Producible building, int now) {
        AUnitType unitType = resolveUnitType(building);
        if (unitType == null) return null;

        for (Construction construction : ConstructionRequests.constructions) {
            if (construction.buildingType() != unitType || construction.buildPosition() == null) continue;
            return PlacementReservation
                    .success(construction.buildPosition().tx(), construction.buildPosition().ty()
                            + 0, now)
                    .committedAt(now);
        }
        return null;
    }

    private AUnit builderForThisPass() {
        if (passBuilder == null || !passBuilder.isAlive()) {
            passBuilder = Select.ourWorkers().first();
        }
        return passBuilder;
    }

    private AUnitType resolveUnitType(Producible producible) {
        if (producible instanceof UnitProducible) {
            return ((UnitProducible) producible).type();
        }
        return null;
    }

    private HasPosition resolveNear(TargetPlacement constraint) {
        switch (constraint.mode()) {
            case EXACT_TILE:
                return APosition.create(constraint.exactTileX() * 32, constraint.exactTileY() * 32);
            case NEIGHBOURHOOD:
                return APosition.create(constraint.tileX() * 32, constraint.tileY() * 32);
            case NAMED_AREA:
                return areaCentre(constraint.areaName());
            case ANYWHERE:
            default:
                // The legacy finder requires a centre to search around (it
                // asserts non-null), so "anywhere" means "around our base" -
                // which is what the legacy pipeline does too.
                AUnit base = Select.ourBases().first();
                return base != null ? base : Select.ourBuildings().first();
        }
    }

    /**
     * Build-order position modifiers, deliberately a small subset: MAIN and
     * NATURAL are what the shipped files use. Unknown names fall back to the
     * main base rather than failing the pass.
     */
    private HasPosition areaCentre(String areaName) {
        if ("NATURAL".equals(areaName)) {
            APosition natural = BaseLocations.natural();
            if (natural != null) return natural;
        }
        AUnit main = Select.main();
        if (main != null) return main;
        AUnit base = Select.ourBases().first();
        return base != null ? base : Select.ourBuildings().first();
    }
}
