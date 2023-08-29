package atlantis.combat.missions.defend.focus;

import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.advance.focus.MissionFocusPoint;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.AGame;
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

                if (AGame.isUms()) {
                    return null;
                }

                AUnit mainBase = Select.main();
                if (mainBase == null) {
                    AUnit firstUnit = Select.our().first();

                    if (firstUnit == null) {
                        return null;
                    }

                    return new AFocusPoint(
                        firstUnit,
                        "WeHaveNoBase"
                    );
                }

                // === Enemies that breached into base ===========================

                if ((focus = enemyWhoBreachedBase()) != null) return focus;

                // =========================================================

                if ((focus = SpecialDefendFocus.define()) != null) return focus;

                // ===============================================================
                // === Around defensive buildings ================================
                // ===============================================================

                if ((focus = ZergDefendFocus.define()) != null) return focus;
                if ((focus = TerranDefendFocus.define()) != null) return focus;

                if ((focus = somewhereAtNaturalBaseOrNaturalChoke()) != null) return focus;

                // If NO BASE exists, return any building
                if (mainBase == null) {
                    Selection selection = Select.ourBuildingsWithUnfinished();
                    if (selection == null || selection.first() == null) {
                        return null;
                    }

                    return new AFocusPoint(selection.first(), "AnyBuilding");
                }

                // === Main choke ================

                focus = atMainChoke();
                if (focus != null) {
                    return focus;
                }

                // === Focus enemy attacking the main base =================

//                AUnit nearEnemy = Select.enemy()
//                    .combatUnits()
//                    .excludeTypes(AUnitType.Protoss_Observer, AUnitType.Zerg_Overlord)
//                    .effVisible()
//                    .inRadius(12, mainBase)
//                    .nearestTo(mainBase);
//                if (nearEnemy != null) {
//                    return new AFocusPoint(
//                        nearEnemy,
//                        Select.main()
//                    );
//                }
//
//                // === Gather around defensive buildings ===================
//
//                AUnit defBuilding = Select.ourOfTypeWithUnfinished(AtlantisRaceConfig.DEFENSIVE_BUILDING_ANTI_LAND).mostDistantTo(mainBase);
//                if (defBuilding != null) {
//                    return defBuilding.translateTilesTowards(mainBase.position(), 5);
//                }

                // === Return position near the first building ================

                AUnit ourFirst = Select.our().first();
                if (ourFirst != null) {
                    return new AFocusPoint(
                        ourFirst,
                        "WeAlmostLost"
                    );
                }

                return null;
            }
        );
    }

    private AFocusPoint somewhereAtNaturalBaseOrNaturalChoke() {
        AFocusPoint focus;

//        if (Bases.hasBaseAtNatural()) {
//            focus = atNaturalChoke();
//            if (focus != null) {
//                return focus;
//            }
//        }

        Selection bases = Select.ourWithUnfinished().bases();
        if (bases.count() == 2) {
//            focus = atAnyBase();
//            if (focus != null) {
//                return focus;
//            }

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
