package atlantis.production.constructing.position.base;

import atlantis.game.A;
import atlantis.map.base.Bases;
import atlantis.map.base.define.DefineNatural;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.position.APositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.cache.Cache;
import atlantis.util.log.ErrorLog;

public class FindPositionForBase {
    protected static Cache<APosition> cache = new Cache<>();

    // =========================================================

    public static APosition findPositionForBase_nearestFreeBase(AUnitType building, AUnit builder, Construction construction) {
//        ABaseLocation baseLocationToExpand;
        HasPosition near;
        int ourBasesCount = Select.ourBases().count();

        if (A.seconds() <= 800 && ourBasesCount <= 1) {
            near = DefineNatural.natural();
        }
//        else if (ourBasesCount <= 2) {
        else {
            AUnit mainBase = Select.main();
            near = Bases.expansionFreeBaseLocationNearestTo(mainBase != null ? mainBase.position() : null);
        }
//        else {
//            baseLocationToExpand = Bases.expansionBaseLocationMostDistantToEnemy();
//        }

        if (near == null) {
            if (ourBasesCount <= 1) {
                ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("findPositionForBase_nearestFreeBase is null");
            }
            return null;
        }

//        APosition near = APosition.create(baseLocationToExpand.position()).translateByPixels(-64, -48);
//        near = APosition.create(baseLocationToExpand.position());
        if (construction != null) construction.setMaxDistance(3);

        return APositionFinder.findStandardPosition(
            builder, building, near, construction != null ? construction.maxDistance() : 3
        );
    }

    public static APosition findPositionForBase_nearMainBase(AUnitType building, AUnit builder, Construction construction) {
        APosition near = Select.main().translateByPixels(-64, -64);

        construction.setNearTo(near);
        construction.setMaxDistance(15);

//        if (Select.main() != null) System.err.println("near = " + near + ", distToMain = " + A.dist(Select.main(), near));
//        if (true) A.printStackTrace("findPositionForBase_nearMainBase");

        return APositionFinder.findStandardPosition(builder, building, near, construction.maxDistance());
    }

    public static APosition findPositionForBase_natural(AUnitType building, AUnit builder) {
        APosition near = DefineNatural.natural();

        if (near == null) {
            System.err.println("Unknown natural position - it will break base position");
            return null;
        }

//        if (Select.main() != null) System.err.println("near NAT = " + near + ", distToMain = " + A.dist(Select.main(), near));
//        if (true) A.printStackTrace("findPositionForBase_natural");

        return APositionFinder.findStandardPosition(builder, building, near, 5);
    }
}
