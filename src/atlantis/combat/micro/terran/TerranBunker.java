package atlantis.combat.micro.terran;

import atlantis.AGame;
import atlantis.combat.missions.Missions;
import atlantis.debug.AAdvancedPainter;
import atlantis.map.AChoke;
import atlantis.map.Chokes;
import atlantis.position.APosition;
import atlantis.position.HasPosition;
import atlantis.production.orders.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import bwapi.Color;

public class TerranBunker {

    public static final AUnitType bunker = AUnitType.Terran_Bunker;

    public static boolean handleOffensiveBunkers() {
        if (true) return false;

        if (!Have.barracks() || AGame.notNthGameFrame(50)) {
            return false;
        }

//        if (handleMainBunker()) {
//            return true;
//        }
//
//        if (handleMissionContain()) {
//            return true;
//        }

//        if (handleReinforceMissionAttack()) {
//            return true;
//        }

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
//        boolean hasTurretNearby = Select.ourOfTypeIncludingUnfinished(AUnitType.Terran_Bunker)
//                .inRadius(13, squadCenter).atLeast(1);
//        if (!hasTurretNearby) {
//            AAntiAirBuildingRequests.requestCombatBuildingAntiAir(squadCenter);
//        }
//    }


    private static boolean handleMainBunker() {
        AChoke choke = Chokes.mainChoke();

        if (choke == null) {
            return false;
        }

        AAdvancedPainter.paintChoke(choke, Color.Cyan, "$");
        AAdvancedPainter.paintCircle(choke.translateTilesTowards(5, Select.main()), 18, Color.Cyan);
        if (!Have.base()) {
            return false;
        }

        return reinforcePosition(choke.translateTilesTowards(5, Select.main()), false);
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
