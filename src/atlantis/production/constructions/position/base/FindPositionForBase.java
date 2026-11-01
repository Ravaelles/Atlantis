package atlantis.production.constructions.position.base;

import atlantis.map.base.BaseLocations;
import atlantis.map.base.define.DefineNaturalBase;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.position.APositionFinder;
import atlantis.production.constructions.position.CatalogueSiteAdapter;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;
import atlantis.util.cache.Cache;

public class FindPositionForBase {
    protected static Cache<APosition> cache = new Cache<>();

    // =========================================================

    public static APosition forNewBase(AUnit builder, AUnitType building, Construction construction, HasPosition nearTo) {
        if (We.zerg()) {
            if (Count.larvas() == 0 || Count.bases() >= 3) {
                return APositionFinder.findStandardPosition(builder, building, nearTo, 30);
            }
        }

        // Migration A2 (owner #146): the catalogue answers base siting
        // first, constrained to the natural neighbourhood (the demand
        // already names it: V2 expansion goals carry NATURAL). The legacy
        // search below stays as the fallback until a game proves the
        // catalogue answers - the twin names which path fired, so the
        // mix reads off the log, not off a guess. Gas keeps its dedicated
        // finder (single geyser tile, no search); bases are searchable
        // ground, which is exactly what the catalogue ranks.
        APosition natural = BaseLocations.natural();
        if (natural != null && construction != null) construction.setNearTo(natural);
        APosition site = CatalogueSiteAdapter.siteFor(builder, building, construction);
        if (site != null) return site;

        return FindPositionForBaseNearestFree.find(building, builder, construction);
    }

    public static APosition findPositionForBase_nearMainBase(AUnitType building, AUnit builder, Construction construction) {
        APosition near = Select.main().translateByPixels(0, -64);

        construction.setNearTo(near);
        construction.setMaxDistance(25);

//        if (Select.main() != null) System.err.println("near = " + near + ", distToMain = " + A.dist(Select.main(), near));
//        if (true) AConsole.printStackTrace("findPositionForBase_nearMainBase");

        return APositionFinder.findStandardPosition(builder, building, near, construction.maxDistance());
    }

    public static APosition findPositionForBase_natural(AUnitType building, AUnit builder) {
        APosition near = DefineNaturalBase.natural();

        if (near == null) {
            System.err.println("Unknown natural position - it will break base position");

            return null;
        }

//        if (Select.main() != null) System.err.println("near NAT = " + near + ", distToMain = " + A.dist(Select.main(), near));
//        if (true) AConsole.printStackTrace("findPositionForBase_natural");

        return APositionFinder.findStandardPosition(builder, building, near, 5);
    }
}
