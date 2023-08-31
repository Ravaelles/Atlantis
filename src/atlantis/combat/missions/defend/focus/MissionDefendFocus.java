package atlantis.combat.missions.defend.focus;

import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.advance.focus.MissionFocusPoint;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.enemy.EnemyWhoBreachedBase;
import atlantis.map.choke.AChoke;
import atlantis.map.base.Bases;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.cache.Cache;

public class MissionDefendFocus extends MissionFocusPoint {

    private Cache<AFocusPoint> cache = new Cache<>();

    // =========================================================

    @Override
    public AFocusPoint focusPoint() {
        return cache.get(
            "focusPoint",
            29,
            () -> {
                AFocusPoint focus = null;

                if (Select.main() == null) return fallbackToNearestEnemy();

                // === Enemies that breached into base ===========================

                if ((focus = enemyWhoBreachedBase()) != null) return focus;

                // === At second base ============================================

//                if ((focus = somewhereAtNaturalBaseOrNaturalChoke()) != null) return focus;

                // ===============================================================

                if ((focus = SpecialDefendFocus.define()) != null) return focus;

                // ===============================================================
                // === Around combat buildings ===================================
                // ===============================================================

                if ((focus = ZergDefendFocus.define()) != null) return focus;
                if ((focus = TerranDefendFocus.define()) != null) return focus;
                if ((focus = aroundCombatBuilding()) != null) return focus;

                // === Main choke ================================================

                if ((focus = atMainChoke()) != null) return focus;

                // === Return position near the first building ===================

                if ((focus = anyOfOurBuildings()) != null) {
                    return focus;
                }

                // === Hopeless case, go towards enemies =========================

                return fallbackToNearestEnemy();
            }
        );
    }

    private AFocusPoint whenNoMain() {
        AUnit main = Select.main();

        if (main != null) return null;

        return fallbackToNearestEnemy();
    }

    private AFocusPoint fallbackToNearestEnemy() {
        AUnit our = Select.ourBuildings().first();
        if (our == null) our = Select.our().first();

        AUnit enemy = EnemyUnits.discovered().groundUnits().havingPosition().nearestTo(our);

        if (enemy == null) return null;

        return new AFocusPoint(
            enemy,
            our,
            "FallbackEnemy(" + enemy.type() + ")"
        );
    }

    private static AFocusPoint aroundCombatBuilding() {
        AUnit base = Select.ourBases().last();

        if (base == null) return null;

        AUnit combatBuilding = Select
            .ourOfType(AtlantisRaceConfig.DEFENSIVE_BUILDING_ANTI_LAND)
            .mostDistantTo(base);

        if (combatBuilding == null) return null;

        return new AFocusPoint(
            combatBuilding.translateTilesTowards(base, 5),
            base,
            "Around" + combatBuilding.type().name()
        );
    }

    private static AFocusPoint anyOfOurBuildings() {
        AUnit ourBuilding;

        ourBuilding = Select.ourBuildings().first();

        if (ourBuilding == null) return null;

        return new AFocusPoint(
            ourBuilding,
            "AnyOfOurBuildings"
        );
    }

    private AFocusPoint somewhereAtNaturalBaseOrNaturalChoke() {
        AFocusPoint focus;

        Selection bases = Select.ourWithUnfinished().bases();
        if (bases.count() == 2) {
            AChoke naturalChoke = Chokes.natural();
            AUnit naturalBase = bases.last();

            if (naturalChoke != null) {
                return new AFocusPoint(
                    naturalChoke,
                    naturalBase,
                    "NaturalChoke"
                );
            }

            return new AFocusPoint(
                naturalBase,
                Select.main(),
                "NaturalBase"
            );
        }

        return null;
    }

    private static AFocusPoint enemyWhoBreachedBase() {
        AUnit enemyInBase = EnemyWhoBreachedBase.get();
        if (enemyInBase != null) {
            return new AFocusPoint(
                enemyInBase,
                "EnemyBreachedBase"
            );
        }
        return null;
    }

    // =========================================================

    private AFocusPoint atAnyBase() {
        //                    AChoke natural = Chokes.natural();
        AUnit lastBase = Select.ourWithUnfinishedOfType(AtlantisRaceConfig.BASE).mostDistantTo(Select.main());
        if (lastBase != null) {

            // === At natural =========================================

            if (Bases.hasBaseAtNatural()) {
//                AChoke naturalChoke = Chokes.mainChoke();
                AChoke naturalChoke = Chokes.mainChoke();
                if (naturalChoke != null) {
                    return atNaturalChoke();
                }
            }

            // === Standard ===========================================

            return new AFocusPoint(
                lastBase,
                "LastBase"
            );
        }

        return null;
    }

    // =========================================================

    protected static AFocusPoint atMainChoke() {
        AChoke mainChoke = Chokes.mainChoke();
        if (mainChoke == null) {
            return null;
        }

        return new AFocusPoint(
            mainChoke,
            Select.main(),
            "MainChoke"
        );
    }

    private AFocusPoint atNaturalChoke() {
//        AChoke choke = Chokes.nearestChoke(lastBase);
//        if (choke != null) {
//            return new AFocusPoint(
//                choke.translateTilesTowards(5, lastBase),
//                "LastBaseChoke"
//            );
//        }

        AChoke naturalChoke = Chokes.natural();

        if (naturalChoke == null) {
            return null;
        }

        APosition natural = Bases.natural();
        return new AFocusPoint(
            natural != null ? naturalChoke.translateTilesTowards(5, natural) : naturalChoke,
            natural != null ? natural : null,
            "NaturalChoke"
        );
    }

}