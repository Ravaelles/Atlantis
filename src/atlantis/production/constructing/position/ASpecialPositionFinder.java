package atlantis.production.constructing.position;

import atlantis.map.base.ABaseLocation;
import atlantis.map.choke.AChoke;
import atlantis.map.base.Bases;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.position.base.FindPositionForBase;
import atlantis.production.constructing.position.base.FindPositionForBaseNearestFree;
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
        if (modifier.equals(PositionModifier.MAIN) || modifier.equals("MAIN")) {
            if (construction != null && construction.maxDistance() < 0) {
                construction.setMaxDistance(40);
            }
            return FindPositionForBaseNearestFree.find(building, builder, construction);
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
            AChoke mainChoke = Chokes.mainChoke();
            if (mainChoke != null) {
                return APosition.create(mainChoke.center()).translateTilesTowards(Select.main(), 3.3);
            }
        }
        else if (modifier.equals(PositionModifier.NATURAL_CHOKE)) {
            AChoke chokepointForNatural = Chokes.natural(Select.main().position());
            if (chokepointForNatural != null && Select.main() != null) {
                ABaseLocation natural = Bases.natural(Select.main().position());
                return APosition.create(chokepointForNatural.center()).translateTilesTowards(natural, 5);
            }
        }

        return null;
    }
}
