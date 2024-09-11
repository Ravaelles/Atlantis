package atlantis.combat.micro.terran.bunker;

import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.production.requests.AntiLandBuildingCommander;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;
import atlantis.util.Enemy;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

public class TerranBunker extends AntiLandBuildingCommander {
    @Override
    public AUnitType type() {
        return AUnitType.Terran_Bunker;
    }

    @Override
    public int expected() {
        if (EnemyInfo.isDoingEarlyGamePush()) {
            return (Enemy.zerg() || Enemy.protoss()) ? 2 : 1;
        }

        return 1;
    }

    @Override
    public boolean shouldBuildNew() {
        if (!We.terran()) {
            ErrorLog.printMaxOncePerMinute("shouldBuildNew (Bunker) called for non-terran");
            return false;
        }

        if (!Have.barracks()) return false;

        return (new ShouldBuildNewBunker()).shouldBuildNew();
    }

    @Override
    public boolean requestToBuildNewAntiLandCombatBuilding() {
        if (!shouldBuildNew()) return false;

        System.err.println("@ " + A.now() + " - REQUESTED NEW BUNKER, NOW DEFINE POS");
        return (new NewBunker()).requestNewAndAutomaticallyDecidePosition();
    }

    // =========================================================

//    private boolean handleMissionContain() {
//        if (!Missions.isGlobalMissionContain()) return false;
//
//        APosition focusPoint = Missions.globalMission().focusPoint();
//        if (focusPoint == null) return false;
//
//        return reinforcePosition(focusPoint, true);
//    }

//    private boolean reinforcePosition(HasPosition position, boolean checkReservedMinerals) {
//        if (!Have.existingOrPlannedOrInQueue(type(), position, 12)) {
////            if (checkReservedMinerals ? AGame.canAffordWithReserved(84, 0) : AGame.canAfford(70, 0)) {
//            if (checkReservedMinerals ? AGame.canAffordWithReserved(84, 0) : Count.ourCombatUnits() >= 2) {
//                AddToQueue.withTopPriority(type(), position);
//                return true;
//            }
//        }
//
//        return false;
//    }

    // =========================================================

//    public TerranBunker getInstance() {
//        if (instance == null) {
//            return (TerranBunker) (instance = new TerranBunker());
//        }
//
//        return (TerranBunker) instance;
//    }

}
