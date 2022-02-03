package atlantis.production.constructing.position;

import atlantis.map.ABaseLocation;
import atlantis.map.AChoke;
import atlantis.map.Bases;
import atlantis.map.Chokes;
import atlantis.map.position.APosition;
import atlantis.production.constructing.ConstructionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class PositionModifier {

    /**
     * Constant used as a hint to indicate that base should be built in the nearest base location
     * (to the main base) that's still free.
     */
    public static final String BASE_AT_NEAREST_FREE = "NEAREST_FREE";

    /**
     * Constant used as a hint to indicate that building should be placed in the main base region.
     */
    public static final String NEAR_MAIN = "MAIN";

    /**
     * Constant used as a hint to indicate that building should be placed in the chokepoints of the main base.
     */
    public static final String NEAR_MAIN_CHOKEPOINT = "MAIN_CHOKEPOINT";
    public static final String NEAR_NATURAL_CHOKEPOINT = "NEAR_NATURAL_CHOKEPOINT";

    /**
     * Constant used as a hint to indicate that building should be placed in the "natural"
     * (also called the "expansion").
     */
    public static final String AT_NATURAL = "NATURAL";

    // =========================================================

    public static APosition toPosition(
        String modifier, AUnitType building, AUnit builder, ConstructionOrder constructionOrder
    ) {
        if (modifier.equals(NEAR_MAIN) || modifier.equals("NEAR_MAIN")) {
            if (constructionOrder.maxDistance() < 0) {
                constructionOrder.setMaxDistance(40);
            }
            return ASpecialPositionFinder.findPositionForBase_nearMainBase(building, builder, constructionOrder);
        }
        else if (modifier.equals(AT_NATURAL)) {
            if (constructionOrder.maxDistance() < 0) {
                constructionOrder.setMaxDistance(30);
            }
            return ASpecialPositionFinder.findPositionForBase_natural(building, builder, constructionOrder);
        }

        if (Select.main() == null) {
            return null;
        }

        if (modifier.equals(NEAR_MAIN_CHOKEPOINT)) {
            AChoke mainChoke = Chokes.mainChoke();
            if (mainChoke != null) {
                return APosition.create(mainChoke.center()).translateTilesTowards(Select.main(), 3.5);
            }
        }
        else if (modifier.equals(NEAR_NATURAL_CHOKEPOINT)) {
            AChoke chokepointForNatural = Chokes.natural(Select.main().position());
            if (chokepointForNatural != null && Select.main() != null) {
                ABaseLocation natural = Bases.natural(Select.main().position());
                return APosition.create(chokepointForNatural.center()).translateTilesTowards(natural, 5);
            }
        }

        return null;
    }

}
