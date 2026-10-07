package atlantis.production.v2;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.position.APositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

import java.util.HashSet;
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
 */
public class LegacyPlacementPlanner implements PlacementPlanner {

    /** Build tiles already claimed in the current pass: "x:y". */
    private final Set<String> reservedTilesThisPass = new HashSet<>();

    /** Chosen once per pass: every building in a frame is served by one builder. */
    private AUnit passBuilder;

    @Override
    public void startPass() {
        reservedTilesThisPass.clear();
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

        // The finder is asked for one candidate; it excludes buildings already
        // on the map and our own reservations of this pass on top of that.
        HasPosition near = resolveNear(constraint);
        APosition position = APositionFinder.findStandardPosition(builder, unitType, near, 12);
        if (position == null)
            return PlacementReservation.failure();

        String tile = position.tx() + ":" + position.ty();
        if (!reservedTilesThisPass.add(tile))
            return PlacementReservation.failure();

        return PlacementReservation.success(position.tx(), position.ty(), targetFrame);
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
            case NEIGHBOURHOOD:
                return APosition.create(constraint.tileX() * 32, constraint.tileY() * 32);
            case ANYWHERE:
            default:
                // The legacy finder requires a centre to search around (it
                // asserts non-null), so "anywhere" means "around our base" -
                // which is what the legacy pipeline does too.
                AUnit base = Select.ourBases().first();
                return base != null ? base : Select.ourBuildings().first();
        }
    }
}
