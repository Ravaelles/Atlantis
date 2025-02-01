package atlantis.production.constructions.position.base;

import atlantis.map.base.define.DefineNaturalBase;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.position.APositionFinder;
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

        return FindPositionForBaseNearestFree.find(building, builder, construction);
    }

    public static APosition findPositionForBase_nearMainBase(AUnitType building, AUnit builder, Construction construction) {
        APosition near = Select.main().translateByPixels(0, -64);

        construction.setNearTo(near);
        construction.setMaxDistance(25);

//        if (Select.main() != null) System.err.println("near = " + near + ", distToMain = " + A.dist(Select.main(), near));
//        if (true) A.printStackTrace("findPositionForBase_nearMainBase");

        return APositionFinder.findStandardPosition(builder, building, near, construction.maxDistance());
    }

    public static APosition findPositionForBase_natural(AUnitType building, AUnit builder) {
        APosition near = DefineNaturalBase.natural();

        if (near == null) {
            System.err.println("Unknown natural position - it will break base position");
            return null;
        }

//        if (Select.main() != null) System.err.println("near NAT = " + near + ", distToMain = " + A.dist(Select.main(), near));
//        if (true) A.printStackTrace("findPositionForBase_natural");

        return APositionFinder.findStandardPosition(builder, building, near, 5);
    }
}
