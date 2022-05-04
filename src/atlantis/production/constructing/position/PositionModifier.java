package atlantis.production.constructing.position;

import atlantis.game.CameraManager;
import atlantis.game.GameSpeed;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.*;
import atlantis.map.position.APosition;
import atlantis.production.constructing.Construction;
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

        // === Bases ===========================================

//        if (modifier != null && modifier != "") {
//            System.err.println("modifier = " + modifier);
//        }

        if (modifier.equals(MAIN) ) {
            if (construction == null || construction.maxDistance() < 0) {
                construction.setMaxDistance(40);
            }
            return ASpecialPositionFinder.findPositionForBase_nearMainBase(building, builder, construction);
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

            return ASpecialPositionFinder.findPositionForBase_natural(building, builder);
        }
        else if (modifier.equals(ENEMY_NATURAL)) {
            APosition enemyNatural = Bases.enemyNatural();
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
            APosition enemyBase = EnemyUnits.enemyBase();
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
//            System.out.println("at = " + at);
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
                construction.setMaxDistance(6);
            }
            AChoke mainChoke = Chokes.mainChoke();
            if (mainChoke != null) {
                return APosition.create(mainChoke.center()).translateTilesTowards(main, 2.8);
            }
        }
        else if (modifier.equals(NATURAL_CHOKE)) {
            if (construction != null) {
                construction.setMaxDistance(6);
            }
            AChoke chokepointForNatural = Chokes.natural(main.position());
            if (chokepointForNatural != null && main != null) {
                ABaseLocation natural = Bases.natural(main.position());
//                return APosition.create(chokepointForNatural.center()).translateTilesTowards(natural, 5);
                return natural.translateTilesTowards(chokepointForNatural.center(), 8);
            }
        }

        return null;
    }

}
