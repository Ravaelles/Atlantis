package atlantis.combat.micro.terran;

import atlantis.combat.missions.Missions;
import atlantis.debug.painter.AAdvancedPainter;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.map.AChoke;
import atlantis.map.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.Enemy;
import bwapi.Color;

import static atlantis.production.AbstractDynamicUnits.addToQueueToMaxAtATime;

public class TerranBunker {

    public static final AUnitType bunker = AUnitType.Terran_Bunker;

//    public static boolean handleOffensiveBunkers() {
////        if (true) return false;
//
//        if (!Have.barracks() || AGame.notNthGameFrame(50)) {
//            return false;
//        }
//
//        if (handleNaturalBunker()) {
//            return true;
//        }
//
//        if (handleMainBunker()) {
//            return true;
//        }
////
////        if (handleMissionContain()) {
////            return true;
////        }
//
////        if (handleReinforceMissionAttack()) {
////            return true;
////        }
//
//        return false;
//    }

    public static int expectedBunkers() {
        if (EnemyInfo.isDoingEarlyGamePush()) {
            return Enemy.zerg() ? 2 : 1;
        }

        return 1;
    }

    public static boolean handleDefensiveBunkers() {
        if (!EnemyInfo.isDoingEarlyGamePush()) {
            return false;
        }

        int existingBunkers = Count.existingOrInProductionOrInQueue(bunker);
        int expectedBunkers = expectedBunkers();
        if (existingBunkers < expectedBunkers) {
            int neededBunkers = expectedBunkers - existingBunkers;
            for (int i = 0; i < neededBunkers; i++) {
                addToQueueToMaxAtATime(bunker, neededBunkers);
//                System.err.println("Requested BUNKER");
            }
            return neededBunkers > 0;
        }

        return false;
    }

    // =========================================================

//    private static boolean handleReinforceMissionAttack() {
//        if (!Missions.isGlobalMissionAttack()) {
//            return false;
//        }
//
//        APosition squadCenter = Squad.alphaCenter();
//        if (squadCenter == null) {
//            return false;
//        }
//
//        boolean hasTurretNear = Select.ourOfTypeIncludingUnfinished(AUnitType.Terran_Bunker)
//                .inRadius(13, squadCenter).atLeast(1);
//        if (!hasTurretNear) {
//            AAntiAirBuildingRequests.requestCombatBuildingAntiAir(squadCenter);
//        }
//    }

    private static boolean handleMainBunker() {
        if (!Enemy.terran() && AGame.timeSeconds() >= 300 && Count.bunkers() < 2) {
            AChoke choke = Chokes.mainChoke();
            if (choke != null) {
                return reinforcePosition(choke.translateTilesTowards(5, Select.main()), false);
            }
        }

        return false;


//        if (choke == null) {
//            return false;
//        }
//
//        AAdvancedPainter.paintChoke(choke, Color.Cyan, "$");
//        AAdvancedPainter.paintCircle(choke.translateTilesTowards(5, Select.main()), 18, Color.Cyan);
//        if (!Have.base()) {
//            return false;
//        }
//
//        return reinforcePosition(choke.translateTilesTowards(5, Select.main()), false);
    }

    private static boolean handleNaturalBunker() {
        if (Count.bases() < 2) {
            return false;
        }

        AChoke naturalChoke = Chokes.natural();
        AUnit naturalBase = Select.ourBases().last();
        if (naturalBase != null && naturalChoke != null) {
            return reinforcePosition(naturalBase.translateTilesTowards(5, naturalChoke), false);
        }

        return false;
    }

    private static boolean handleMissionContain() {
        if (!Missions.isGlobalMissionContain()) {
            return false;
        }

        APosition focusPoint = Missions.globalMission().focusPoint();
        if (focusPoint == null) {
            return false;
        }

        return reinforcePosition(focusPoint, true);
    }

    private static boolean reinforcePosition(HasPosition position, boolean checkReservedMinerals) {
        if (!Have.existingOrPlannedOrInQueue(bunker, position, 12)) {
//            if (checkReservedMinerals ? AGame.canAffordWithReserved(84, 0) : AGame.canAfford(70, 0)) {
            if (checkReservedMinerals ? AGame.canAffordWithReserved(84, 0) : Count.ourCombatUnits() >= 2) {
                System.out.println("Request bunker");
                AddToQueue.withTopPriority(bunker, position);
                return true;
            }
        }

        return false;
    }

}
