package atlantis.production.constructions.position.base;

import atlantis.map.base.BaseLocations;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.position.CatalogueSiteAdapter;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class FindPositionForBase {

    // =========================================================

    public static APosition forNewBase(AUnit builder, AUnitType building, Construction construction, HasPosition nearTo) {
        // Protoss-only bot: no Zerg branch (the legacy Zerg fallback died
        // with the spiral - a Zerg game breaks hundreds of Protoss
        // assumptions long before base siting matters).

        // Migration A4c (owner #146): pure catalogue. The legacy spiral
        // fallback is gone - a declined base retries through the demand
        // (no construction is minted on null, so nothing churns), and the
        // twin names every decline. The natural anchor below is the same
        // one the spiral used; only the search around it changed.
        APosition natural = BaseLocations.natural();
        if (natural != null && construction != null) {
            construction.setNearTo(natural);
            // The base demand lives in the natural neighbourhood (the
            // test pins <= 20, the legacy spiral window was 25-50): the
            // funnel hunt is bounded, so the ranker's global optimum
            // cannot win over the anchor. The planner's expansion
            // discipline (within 2 of a base location) applies on top
            // where map truth exists.
            construction.setMaxDistance(20);
        }

        return CatalogueSiteAdapter.siteFor(builder, building, construction);
    }
}
