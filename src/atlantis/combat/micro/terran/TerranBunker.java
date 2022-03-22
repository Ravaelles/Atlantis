package atlantis.combat.micro.terran;

import atlantis.combat.missions.Missions;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyInfo;
import atlantis.map.AChoke;
import atlantis.map.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.orders.build.AddToQueue;
import atlantis.production.requests.AntiLandBuildingManager;
import atlantis.production.requests.protoss.ProtossPhotonCannonAntiLand;
import atlantis.production.requests.zerg.ZergSunkenColony;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.Enemy;
import atlantis.util.We;

import static atlantis.production.AbstractDynamicUnits.addToQueueToMaxAtATime;

public class TerranBunker extends AntiLandBuildingManager {

    @Override
    public AUnitType type() {
        return AUnitType.Terran_Bunker;
    }

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

    @Override
    public int expected() {
        if (EnemyInfo.isDoingEarlyGamePush()) {
            return Enemy.zerg() ? 2 : 1;
        }

        return 1;
    }

    @Override
    public boolean handleBuildNew() {
        if (!Have.barracks()) {
            return false;
        }

        if (Count.bases() >= 2) {
            if (handleNaturalBunker()) {
                return true;
            }
        }

        return super.handleBuildNew();
    }

    public boolean handleDefensiveBunkers() {
        if (!EnemyInfo.isDoingEarlyGamePush()) {
            return false;
        }

        int existingBunkers = Count.existingOrInProductionOrInQueue(type());
        int expectedBunkers = expected();
        if (existingBunkers < expectedBunkers) {
            int neededBunkers = expectedBunkers - existingBunkers;

            for (int i = 0; i < neededBunkers; i++) {
                addToQueueToMaxAtATime(type(), neededBunkers);
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
//        boolean hasTurretNear = Select.ourOfTypeWithUnfinished(AUnitType.Terran_Bunker)
//                .inRadius(13, squadCenter).atLeast(1);
//        if (!hasTurretNear) {
//            AAntiAirBuildingRequests.requestCombatBuildingAntiAir(squadCenter);
//        }
//    }

//    private boolean handleMainBunker() {
//        if (!Enemy.terran() && AGame.timeSeconds() >= 300 && Count.bunkers() < 2) {
//            AChoke choke = Chokes.mainChoke();
//            if (choke != null) {
//                return reinforcePosition(choke.translateTilesTowards(5, Select.main()), false);
//            }
//        }
//
//        return false;
//    }

    private boolean handleNaturalBunker() {
        if (Count.bases() < 2) {
            return false;
        }

        AChoke naturalChoke = Chokes.natural();
        AUnit naturalBase = Select.ourBases().second();
        if (naturalBase != null && naturalChoke != null) {
            if (Count.existingOrPlannedBuildingsNear(type(), 6, naturalBase) == 0) {
                return reinforcePosition(naturalBase.translateTilesTowards(5, naturalChoke), false);
            }
        }

        return false;
    }

    private boolean handleMissionContain() {
        if (!Missions.isGlobalMissionContain()) {
            return false;
        }

        APosition focusPoint = Missions.globalMission().focusPoint();
        if (focusPoint == null) {
            return false;
        }

        return reinforcePosition(focusPoint, true);
    }

    private boolean reinforcePosition(HasPosition position, boolean checkReservedMinerals) {
        if (!Have.existingOrPlannedOrInQueue(type(), position, 12)) {
//            if (checkReservedMinerals ? AGame.canAffordWithReserved(84, 0) : AGame.canAfford(70, 0)) {
            if (checkReservedMinerals ? AGame.canAffordWithReserved(84, 0) : Count.ourCombatUnits() >= 2) {
                AddToQueue.withTopPriority(type(), position);
                return true;
            }
        }

        return false;
    }

    // =========================================================

    public static TerranBunker get() {
        if (instance == null) {
            return (TerranBunker) (instance = new TerranBunker());
        }

        return (TerranBunker) instance;
    }

}
