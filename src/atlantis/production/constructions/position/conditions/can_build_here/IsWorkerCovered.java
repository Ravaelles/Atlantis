package atlantis.production.constructions.position.conditions.can_build_here;

import atlantis.map.position.APosition;
import atlantis.production.constructions.builders.GetOptimalBuilder;
import atlantis.units.select.Select;

/**
 * A site our builders refuse to walk to: standing workers cover it.
 *
 * <p>When 4+ of our workers stand within 12 tiles of the tile, the builder
 * assignment answers nobody ({@code GetOptimalBuilder} - the free pool in
 * a young base is miners carrying loads, and stealing one is refused) and
 * the builderless construction dies to the "looks bugged" cancel, then
 * re-requests the same tile: the cancel storm (NEXT #145, measured 3988
 * CONSTRUCTION_CANCELs on the catalogue A/B arm). The catalogue declines
 * such tiles up front and the legacy search answers instead - same numbers
 * as the refusal, read not owned, so the two can never disagree about
 * what "covered" means.</p>
 */
public final class IsWorkerCovered {

    private IsWorkerCovered() {
    }

    /**
     * True when standing workers cover this tile: a builder assigned here
     * would be refused. Null-safe by construction - no site, no cover.
     */
    public static boolean check(APosition site) {
        if (site == null) return false;

        try {
            return Select.ourWorkers()
                .inRadius(GetOptimalBuilder.COVERED_RADIUS_TILES, site)
                .count() >= GetOptimalBuilder.COVERED_WORKERS;
        } catch (Exception | Error ignored) {
            return false;
        }
    }
}
