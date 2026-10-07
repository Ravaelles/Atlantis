package atlantis.production.v2;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.position.APositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

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
 * pass and the next frame's pass retries. Block templates and pylon-power
 * scoring (Stardust's richer model) are later refinements behind this same
 * interface.
 * </p>
 */
public class LegacyPlacementPlanner implements PlacementPlanner {

    @Override
    public PlacementReservation reservePlacement(Producible building, TargetPlacement constraint, int targetFrame) {
        AUnitType unitType = resolveUnitType(building);
        if (unitType == null)
            return PlacementReservation.failure();

        HasPosition near = resolveNear(constraint);
        AUnit builder = findBuilderNear(near);
        if (builder == null)
            return PlacementReservation.failure();

        APosition position = APositionFinder.findStandardPosition(builder, unitType, near, 12);
        if (position == null)
            return PlacementReservation.failure();

        return PlacementReservation.success(position.tx(), position.ty(), targetFrame);
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
                return null;
        }
    }

        private AUnit findBuilderNear(HasPosition near) {
            if (near == null) return null;
            return Select.ourWorkers().nearestTo(near);
        }
    }
