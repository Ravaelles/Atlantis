package atlantis.production.constructing.position.modifier;

import atlantis.combat.micro.terran.bunker.position.BunkerEstimatePositionAtNatural;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.*;
import atlantis.map.base.ABaseLocation;
import atlantis.map.base.BaseLocations;
import atlantis.map.base.define.DefineNaturalBase;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.position.APositionFinder;
import atlantis.production.constructing.position.base.FindPositionForBase;
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

    public static final String MAP_CENTER = "MAP_CENTER";

    public static final String ENEMY_NATURAL = "ENEMY_NATURAL";
    public static final String ENEMY_MAIN = "ENEMY_MAIN";

    // =========================================================

    public static APosition toPosition(
        String modifier, AUnitType building, AUnit builder, Construction construction
    ) {
        AUnit main = Select.main();

        // === Bunker ==========================================

        if (building.isBunker()) {
            if (modifier.equals(NATURAL)) {
                return BunkerEstimatePositionAtNatural.define();
            }
//            return BunkerPositionModifier.modifierToPosition(modifier, building, builder, construction);
        }

        // === Bases ===========================================

//        if (modifier != null && modifier != "") {
//            System.err.println("modifier = " + modifier);
//        }

        if (modifier.equals(MAIN)) {
            if (construction == null || construction.maxDistance() < 0) {
                construction.setMaxDistance(40);
            }
            return FindPositionForBase.findPositionForBase_nearMainBase(building, builder, construction);
        }
        else if (modifier.equals(NATURAL)) {
//            double maxDist = construction.maxDistance();
//
//            if (construction == null || construction.maxDistance() < 0) {
//                maxDist = 30;
//            }
//
//            if (building.isBase()) {
//                maxDist = 1;
//            }

            return FindPositionForBase.findPositionForBase_natural(building, builder);
        }
        else if (modifier.equals(ENEMY_NATURAL)) {
            APosition enemyNatural = BaseLocations.enemyNatural();
            AChoke enemyNaturalChoke = Chokes.enemyNaturalChoke();

            if (enemyNatural != null && enemyNaturalChoke != null) {
                APosition at = enemyNaturalChoke.translateTilesTowards(4, enemyNatural);
                return APositionFinder.findStandardPosition(builder, building, at, 10);
            }
            else if (enemyNaturalChoke != null) {
                return APositionFinder.findStandardPosition(builder, building, enemyNaturalChoke, 10);
            }
            else if (enemyNatural != null) {
                return APositionFinder.findStandardPosition(builder, building, enemyNatural, 10);
            }
        }
        else if (modifier.equals(ENEMY_MAIN)) {
            AChoke enemyMainChoke = Chokes.enemyMainChoke();
            AUnit enemyBase = EnemyUnits.enemyBase();
            if (enemyBase != null && enemyMainChoke != null) {
                APosition at = enemyBase.translateTilesTowards(14, enemyMainChoke);
                return APositionFinder.findStandardPosition(builder, building, at, 10);
            }
            else if (enemyMainChoke != null) {
                return APositionFinder.findStandardPosition(builder, building, enemyMainChoke, 10);
            }
            else if (enemyBase != null) {
                return APositionFinder.findStandardPosition(builder, building, enemyBase, 10);
            }
        }
        else if (modifier.equals(MAP_CENTER)) {
            APosition at = new APosition(
                (AMap.getMapWidthInTiles() / 2) * 32,
                (AMap.getMapHeightInTiles() / 2) * 32
            );

            return APositionFinder.findStandardPosition(builder, building, at, 30);
        }

        if (main == null) {
            return null;
        }
        if (modifier.equals(MAIN_MINERALS)) {
            return main.translateTilesTowards(main.position().region().center(), 2);
        }

        // === Chokes ===========================================

        if (modifier.equals(MAIN_CHOKE)) {
            if (construction != null) {
                construction.setMaxDistance(9);
            }
            AChoke mainChoke = Chokes.mainChoke();
            if (mainChoke != null) {
                return APosition.create(mainChoke.center()).translateTilesTowards(
                    main,
                    1.2
//                    (mainChoke.width() <= 4 ? 1.7 : )
//                    2.8 + (mainChoke.width() <= 4 ? 1.7 : 0)
                );
            }
        }
        else if (modifier.equals(NATURAL_CHOKE)) {
            if (construction != null) {
                construction.setMaxDistance(9);
            }
            AChoke chokepointForNatural = Chokes.natural();
            if (chokepointForNatural != null && main != null) {
                ABaseLocation natural = DefineNaturalBase.naturalIfMainIsAt(main.position());
//                return APosition.create(chokepointForNatural.center()).translateTilesTowards(natural, 5);
                return natural.translateTilesTowards(chokepointForNatural.center(), 8);
            }
        }

        return null;
    }

}
