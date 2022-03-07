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
    public static final String MAIN = "MAIN";
    public static final String MAIN_MINERALS = "MAIN_MINERALS";

    /**
     * Constant used as a hint to indicate that building should be placed in the chokepoints of the main base.
     */
    public static final String MAIN_CHOKE = "MAIN_CHOKE";
    public static final String NATURAL_CHOKE = "NATURAL_CHOKE";

    /**
     * Constant used as a hint to indicate that building should be placed in the "natural"
     * (also called the "expansion").
     */
    public static final String NATURAL = "NATURAL";

    // =========================================================

    public static APosition toPosition(
        String modifier, AUnitType building, AUnit builder, ConstructionOrder constructionOrder
    ) {
        AUnit main = Select.main();

        // === Bases ===========================================

        if (modifier.equals(MAIN) ) {
            if (constructionOrder == null || constructionOrder.maxDistance() < 0) {
                constructionOrder.setMaxDistance(40);
            }
            return ASpecialPositionFinder.findPositionForBase_nearMainBase(building, builder, constructionOrder);
        }
        else if (modifier.equals(NATURAL)) {
            if (constructionOrder == null || constructionOrder.maxDistance() < 0) {
                constructionOrder.setMaxDistance(30);
            }
            return ASpecialPositionFinder.findPositionForBase_natural(building, builder, constructionOrder);
        }

        if (main == null) {
            return null;
        }
        if (modifier.equals(MAIN_MINERALS)) {
            return main.translateTilesTowards(main.position().region().center(), 2);
        }

        // === Chokes ===========================================

        if (modifier.equals(MAIN_CHOKE)) {
            if (constructionOrder != null) {
                constructionOrder.setMaxDistance(6);
            }
            AChoke mainChoke = Chokes.mainChoke();
            if (mainChoke != null) {
                return APosition.create(mainChoke.center()).translateTilesTowards(main, 2.8);
            }
        }
        else if (modifier.equals(NATURAL_CHOKE)) {
            if (constructionOrder != null) {
                constructionOrder.setMaxDistance(6);
            }
            AChoke chokepointForNatural = Chokes.natural(main.position());
            if (chokepointForNatural != null && main != null) {
                ABaseLocation natural = Bases.natural(main.position());
                return APosition.create(chokepointForNatural.center()).translateTilesTowards(natural, 5);
            }
        }

        return null;
    }

}
