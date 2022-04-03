package atlantis.combat.missions.defend;

import atlantis.combat.missions.focus.AFocusPoint;
import atlantis.combat.missions.focus.MissionFocusPoint;
import atlantis.combat.missions.Missions;
import atlantis.config.AtlantisConfig;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.strategy.GamePhase;
import atlantis.map.AChoke;
import atlantis.map.Chokes;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.cache.Cache;
import atlantis.util.We;

public class MissionDefendFocusPoint extends MissionFocusPoint {

    private Cache<AFocusPoint> cache = new Cache<>();

    // =========================================================

    @Override
    public AFocusPoint focusPoint() {
        return cache.get(
            "focusPoint",
            29,
            () -> {
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

                // === Enemies that breached into base =============

                AUnit enemyNear = EnemyInfo.enemyNearAnyOurBase();
                if (enemyNear != null && enemyNear.isAlive()) {
                    int sunkens = Count.sunkens();

                    if (
                        sunkens == 0
                            || (
                            We.zerg() && sunkens > 0 && enemyNear.enemiesNear()
                                .ofType(AUnitType.Zerg_Sunken_Colony)
                                .notEmpty()
                        )
                    ) {
                        return new AFocusPoint(
                            enemyNear,
                            "EnemyInBase"
                        );
                    }
                }

                int basesWithUnfinished = Count.basesWithUnfinished();
                if (basesWithUnfinished <= 1 && Missions.isGlobalMissionSparta()) {
                    AChoke mainChoke = Chokes.mainChoke();
                    if (mainChoke != null) {
                        return new AFocusPoint(
                            mainChoke,
                            Select.main(),
                            "Choke300"
                        );
                    }
                }

                // === Natural choke if second base ================

                if (basesWithUnfinished >= 2) {
//                    AChoke natural = Chokes.natural();
                    AUnit lastBase = Select.ourWithUnfinishedOfType(AtlantisConfig.BASE).mostDistantTo(Select.main());
                    if (lastBase != null) {
                        return new AFocusPoint(
                            lastBase,
                            "LatestBase"
                        );
                    }
                }

                // === Around defensive building ===========================================

                if (We.zerg()) {
                    AUnit sunken = Select.ourOfType(AUnitType.Zerg_Sunken_Colony).mostDistantTo(mainBase);
                    if (sunken != null) {
                        return new AFocusPoint(
                            sunken.translateTilesTowards(3.5, mainBase),
                            "Sunken"
                        );
                    }
                }

                if (We.terran() && A.supplyUsed() <= 50 && Count.infantry() <= 13) {
                    AUnit bunker = Select.ourOfType(AUnitType.Terran_Bunker).mostDistantTo(mainBase);
                    if (GamePhase.isEarlyGame() && mainBase != null) {
                        if (bunker != null) {
                            return new AFocusPoint(bunker, "Bunker");
                        }
                    }
                }

                // If NO BASE exists, return any building
                if (mainBase == null) {
                    Selection selection = Select.ourBuildingsWithUnfinished();
                    if (selection == null || selection.first() == null) {
                        return null;
                    }

                    return new AFocusPoint(selection.first(), "AnyBuilding");
                }

                // === Main choke ================

                AChoke mainChoke = Chokes.mainChoke();
                if (mainChoke != null) {
                    return new AFocusPoint(
                        mainChoke,
                        Select.main(),
                        "MainChoke"
                    );
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
//                AUnit defBuilding = Select.ourOfTypeWithUnfinished(AtlantisConfig.DEFENSIVE_BUILDING_ANTI_LAND).mostDistantTo(mainBase);
//                if (defBuilding != null) {
//                    return defBuilding.translateTilesTowards(mainBase.position(), 5);
//                }

                // === Return position near the choke point ================

//                AChoke chokepointForNatural = AMap.getChokeForNatural(mainBase.position());
//                if (chokepointForNatural != null) {
//                    return APosition.create(chokepointForNatural.getCenter());
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

}