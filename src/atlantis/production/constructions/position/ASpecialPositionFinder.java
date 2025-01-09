package atlantis.production.constructions.position;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.position.base.FindPositionForBase;
import atlantis.production.constructions.position.modifier.PositionAtMainChoke;
import atlantis.production.constructions.position.modifier.PositionAtNaturalChoke;
import atlantis.production.constructions.position.modifier.PositionModifier;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.cache.Cache;

public class ASpecialPositionFinder {
    private static Cache<APosition> cache = new Cache<>();

    // =========================================================

    public static APosition positionModifierToPosition(
        String modifier, AUnitType building, AUnit builder, Construction construction
    ) {
        if (modifier.equals(PositionModifier.MAIN)) {
            if (construction != null && construction.maxDistance() < 0) {
                construction.setMaxDistance(40);
            }
//            return FindPositionForBaseNearestFree.find(building, builder, construction);
            return APositionFinder.findPositionForNew(builder, building, construction);
        }
        else if (modifier.equals(PositionModifier.NATURAL)) {
            if (construction != null && construction.maxDistance() < 0) {
                construction.setMaxDistance(20);
            }
            return FindPositionForBase.findPositionForBase_natural(building, builder);
        }

        if (Select.main() == null) {
            return null;
        }

        if (modifier.equals(PositionModifier.MAIN_CHOKE)) {
            return PositionAtMainChoke.atMainChoke(construction);
//            AChoke mainChoke = Chokes.mainChoke();
//            if (mainChoke != null) {
//                return APosition.create(mainChoke.center()).translateTilesTowards(Select.main(), 3.3);
//            }
        }
        else if (modifier.equals(PositionModifier.NATURAL_CHOKE)) {
            return PositionAtNaturalChoke.atNaturalChoke(construction);
        }

        return null;
    }
}
