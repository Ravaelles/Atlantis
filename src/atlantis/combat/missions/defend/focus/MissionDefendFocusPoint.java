package atlantis.combat.missions.defend.focus;

import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.advance.focus.MissionFocusPoint;
import atlantis.combat.missions.Missions;
import atlantis.combat.missions.defend.focus.terran.TerranMissionDefendFocus;
import atlantis.combat.missions.defend.protoss.ProtossStickCombatToMainBaseEarly;
import atlantis.config.ActiveMap;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.information.enemy.*;
import atlantis.information.generic.OurArmy;
import atlantis.information.strategy.OurStrategy;
import atlantis.map.base.Bases;
import atlantis.map.base.define.DefineNaturalBase;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.path.OurClosestBaseToEnemy;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import atlantis.util.We;
import atlantis.util.cache.Cache;

public class MissionDefendFocusPoint extends MissionFocusPoint {
    private Cache<AFocusPoint> cache = new Cache<>();

    // =========================================================

    @Override
    public AFocusPoint focusPoint() {
        return cache.getIfValid(
            "focusPoint",
            17,
            () -> {
                AFocusPoint focus = null;

                if (Select.main() == null) return fallbackToNearestEnemy();

                // === AliveEnemies that breached into base ===========================

                if ((focus = buildingUnderAttack()) != null) return focus;

                // =========================================================

                if (OurStrategy.get().isExpansion()) {
                    if ((focus = atThirdBase()) != null) return focus;
                    if (A.s <= 60 * 8 && (focus = aroundCombatBuilding()) != null) return focus;
                }

                // =========================================================

                if ((focus = stickToMain()) != null) return focus;
                if ((focus = enemyWhoBreachedBase()) != null) return focus;
                if ((focus = enemyCloserToBaseThanAlpha()) != null) return focus;
                if ((focus = stickToCannon()) != null) return focus;

                // === Path to enemy =============================================

//                if ((focus = PathToEnemyFocus.getIfApplies()) != null) return focus;
//                if ((focus = somewhereAtNaturalBaseOrNaturalChoke()) != null) return focus;

                // =========================================================

                if (EnemyInfo.hasHiddenUnits() && !OurInfo.hasMobileDetection()) {
                    if ((focus = aroundCombatBuilding()) != null) return focus;
                }

                // === Natural choke if weak ================================================

                int combatUnits = Count.ourCombatUnits();
                if (combatUnits >= 6) {
                    if ((focus = atNaturalChoke()) != null) return focus;
                }

                // === At 3rd base =============================================

                if ((focus = atThirdBase()) != null) return focus;

                // =========================================================

                if ((focus = aroundCombatBuilding()) != null) return focus;

                // ===============================================================

                if ((focus = SpecialMissionDefendFocus.define()) != null) return focus;

                // === Natural choke ================================================

                if ((focus = atNaturalChoke()) != null) return focus;

                // === Main choke ================================================

                if (!Enemy.terran() && (combatUnits <= 4 || ActiveMap.isGosu())) {
                    if (Count.basesWithUnfinished() <= 1 && (focus = atMainChoke()) != null) return focus;
                }

                // === Last base ================================================

                if ((focus = atLastBase()) != null) return focus;

                // === Main choke ================================================

                if ((focus = atMainChoke()) != null) return focus;

                // ===============================================================
                // === Around combat buildings ===================================
                // ===============================================================

//                if (ArmyStrength.weAreStronger() && Count.ourCombatUnits() <= 8) {
//                if (Count.ourCombatUnits() <= 8) {
                if ((focus = ZergMissionDefendFocus.define()) != null) return focus;
                if ((focus = TerranMissionDefendFocus.define()) != null) return focus;
//                if ((focus = aroundCombatBuilding()) != null) return focus;
//                }

                // === Return position near the most exposed building ============

                if ((focus = ourMostExposedBuilding()) != null) {
                    return focus;
                }

                // === Hopeless case, go towards enemies =========================

                return fallbackToNearestEnemy();
            }
        );
    }

    private AFocusPoint buildingUnderAttack() {
        AUnit ourBuildingUnderAttack = OurBuildingUnderAttack.get();
        if (ourBuildingUnderAttack != null) {
            return new AFocusPoint(
                ourBuildingUnderAttack,
                Select.mainOrAnyBuilding(),
                "BuildingUnderAttack"
            );
        }

        return null;
    }

    private AFocusPoint stickToMain() {
        if (ProtossStickCombatToMainBaseEarly.should()) {
            AUnit main = Select.main();
            if (main == null) return null;

            if (Count.dragoons() >= 2 || OurArmy.strength() >= 140) return null;

            if (main.enemiesNear().inRadius(10, main).notEmpty()) return null;

            AChoke mainChoke = Chokes.mainChoke();
            if (mainChoke == null) return null;

            HasPosition position = main;

            return new AFocusPoint(
                position.translateTilesTowards(-3, mainChoke),
//                main.translateTilesTowards(1, Select.minerals().nearestTo(main)),
                main,
                "NearMain"
            );
        }

        return null;
    }

    private AFocusPoint stickToCannon() {
        if (
            !We.protoss()
                || !OurStrategy.get().isExpansion()
                || !Missions.isGlobalMissionDefend()
                || Count.cannonsWithUnfinished() <= 0
        ) return null;

        AUnit cannon = Select.ourOfTypeWithUnfinished(AUnitType.Protoss_Photon_Cannon).mostDistantTo(Select.main());
        if (cannon == null) return null;

        HasPosition goTo = cannon;

        AChoke choke = Chokes.natural();
        if (choke != null) goTo = goTo.translateTilesTowards(1, choke);

        return new AFocusPoint(
            goTo,
            cannon,
            "HugCannon"
        );
    }

    private AFocusPoint atLastBase() {
        if (A.supplyUsed() < 50 || Count.bases() < 2) return null;

        HasPosition lastBase = OurClosestBaseToEnemy.get();

        if (lastBase == null) return null;

        AChoke choke = Chokes.nearestChoke(lastBase.position(), "MAIN");

        return new AFocusPoint(
            choke != null ? choke : lastBase,
            lastBase,
            "LastBase"
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

        AUnit enemy = EnemyUnits.discovered().groundUnits()
            .havingPosition().havingAtLeastHp(1).nearestTo(our);

        if (enemy == null || !enemy.hasPosition()) return null;

        return new AFocusPoint(
            enemy.position(),
            our,
            "FallbackEnemy(" + enemy.type() + ")"
        );
    }

    private static AFocusPoint aroundCombatBuilding() {
//        if (We.protoss()) {
//            if (Count.ourCombatUnits() >= 13) return null;
//            if (Count.bases() >= 2 && OurArmy.strength() >= 80) return null;
//        }

//        if (We.protoss() && Count.basesWithUnfinished() <= 1) return null;

//        AUnit base = Select.ourBases().last();
//        AUnit base = Select.ourBases().second();
//        AUnit base = Select.ourBases().second();
//
//        if (base == null) return null;

        AUnit main = Select.mainOrAnyBuilding();

        AUnit combatBuilding = Select
            .ourOfTypeWithUnfinished(AtlantisRaceConfig.DEFENSIVE_BUILDING_ANTI_LAND)
//            .ourOfType(AtlantisRaceConfig.DEFENSIVE_BUILDING_ANTI_LAND)
            .groundFarthestTo(main);

        if (combatBuilding == null) return null;
        if (combatBuilding.distTo(main) <= 10 && Bases.natural() != null) return null;

        return new AFocusPoint(
            combatBuilding.translateTilesTowards(main, 1),
            combatBuilding,
//            base,
            "Around" + combatBuilding.type().name()
        );
    }

    private static AFocusPoint ourMostExposedBuilding() {
        AUnit ourBuilding;
        AUnit enemyBuilding = EnemyUnits.buildings().first();

        Selection ourBuildings = Select.ourBuildings();

        if (enemyBuilding != null) ourBuilding = ourBuildings.groundNearestTo(enemyBuilding);
        else {
            AChoke natural = Chokes.natural();
            if (natural != null) ourBuilding = ourBuildings.nearestTo(natural);
            ourBuilding = ourBuildings.first();
        }

        if (ourBuilding == null) return null;

        return new AFocusPoint(
            ourBuilding,
            "AnyOfOurBuildings"
        );
    }

    private AFocusPoint atThirdBase() {
        Selection bases = Select.ourWithUnfinished().bases();

        if (bases.count() >= 3) {
            AUnit base = bases.list().get(2);
            if (base.distToBase() >= 25) return null;

            AChoke choke = Chokes.nearestChoke(base);

            if (choke != null) {
                return new AFocusPoint(
                    choke,
                    base,
                    "ThirdBase"
                );
            }
        }

        return null;
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
        if (A.supplyUsed() >= 160 || A.hasMinerals(2000)) return null;

        AUnit enemyInBase = EnemyUnitBreachedBase.get();
        if (enemyInBase != null && enemyInBase.hasPosition() && enemyInBase.effVisible() && enemyInBase.hp() > 0) {
            return new AFocusPoint(
                enemyInBase,
                "EnemyBreachedBase"
            );
        }
        return null;
    }

    private static AFocusPoint enemyCloserToBaseThanAlpha() {
        AUnit enemyTooCloseToBase = EnemyCloserToBaseThanAlpha.get();
        if (enemyTooCloseToBase != null && enemyTooCloseToBase.hp() > 0) {
            return new AFocusPoint(
                enemyTooCloseToBase,
                "EnemyCloseToBase"
            );
        }
        return null;
    }

    // =========================================================

//    private AFocusPoint atAnyBase() {
//        //                    AChoke natural = Chokes.natural();
//        AUnit lastBase = Select.ourWithUnfinishedOfType(AtlantisRaceConfig.BASE).mostDistantTo(Select.main());
//        if (lastBase != null) {
//
//            // === At natural =========================================
//
//            if (BaseLocations.hasBaseAtNatural()) {
////                AChoke naturalChoke = Chokes.mainChoke();
//                AChoke naturalChoke = Chokes.mainChoke();
//                if (naturalChoke != null) {
//                    return atNaturalChoke();
//                }
//            }
//
//            // === Standard ===========================================
//
//            return new AFocusPoint(
//                lastBase,
//                "LastBase"
//            );
//        }
//
//        return null;
//    }

    // =========================================================

    public static AFocusPoint atMainChoke() {
//        if (We.terran() && A.supplyUsed() < 50) return null;

        AChoke mainChoke = Chokes.mainChoke();
        if (mainChoke == null) {
            return null;
        }

        if (Count.basesWithUnfinished() >= 2) return null;

        HasPosition point = mainChoke;

        if (We.protoss() && Count.dragoons() <= 2) {
            point = point.translateTilesTowards(1.3, Select.main());
        }

        return new AFocusPoint(
//            mainChoke.translateTilesTowards(0.5, Select.main()),
            point,
            Select.main(),
            "MainChoke"
        ).forceAroundChoke(mainChoke);
    }

    private AFocusPoint atNaturalChoke() {
        int minUnitsToHave = We.protoss() ? 6 : 8;
        if (Count.ourCombatUnits() < minUnitsToHave) return null;
//        if (Count.basesWithUnfinished() <= 1) return null;

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

        APosition natural = DefineNaturalBase.natural();
        return new AFocusPoint(
            naturalChoke,
//            natural != null ? naturalChoke.translateTilesTowards(5, natural) : naturalChoke,
            natural,
            "NaturalChoke"
        ).forceAroundChoke(naturalChoke);
    }

}
