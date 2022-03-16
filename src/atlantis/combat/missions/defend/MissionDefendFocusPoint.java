package atlantis.combat.missions.defend;

import atlantis.combat.missions.AFocusPoint;
import atlantis.combat.missions.MissionFocusPoint;
import atlantis.combat.missions.Missions;
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

                // === Enemies that breached into base =============

                if (Have.base()) {
                    AUnit enemyNear = EnemyInfo.enemyNearAnyOurBase();
                    if (enemyNear != null && enemyNear.isAlive()) {
                        return new AFocusPoint(
                            enemyNear,
                            "EnemyInBase"
                        );
                    }
                }

                if (Missions.isGlobalMissionSparta()) {
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

                if (Count.basesWithUnfinished() >= 2) {
                    AChoke natural = Chokes.natural();
                    if (natural != null) {
                        return new AFocusPoint(
                            natural,
                            Select.main(),
                            "DefNaturalChoke"
                        );
                    }
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

                AUnit building = Select.ourBuildings().first();
                if (building != null) {
                    return new AFocusPoint(
                        Chokes.nearestChoke(building.position()),
                        building,
                        "FirstBuilding"
                    );
                }

                return null;
            }
        );
    }

}