package atlantis.combat.missions.contain;

import atlantis.combat.missions.MissionChanger;
import atlantis.combat.missions.Missions;
import atlantis.combat.missions.attack.ProtossMissionChangerWhenAttack;
import atlantis.combat.missions.attack.TerranMissionChangerWhenAttack;
import atlantis.combat.missions.attack.ZergMissionChangerWhenAttack;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.We;

public abstract class MissionChangerWhenContain extends MissionChanger {

    public static MissionChanger get() {
        if (We.terran()) {
            return new TerranMissionChangerWhenContain();
        }
        else if (We.protoss()) {
            return new ProtossMissionChangerWhenContain();
        }
        else {
            return new ZergMissionChangerWhenContain();
        }
    }

//    public void changeMissionIfNeeded() {
//        if (Missions.recentlyChangedMission()) {
//            return;
//        }
//
//        reason = null;
//
//        if (AGame.isPlayingAsTerran()) {
//            TerranMissionChangerWhenContain.changeMissionIfNeeded();
//            return;
//        }
//        else if (AGame.isPlayingAsProtoss()) {
//            ProtossMissionChangerWhenContain.changeMissionIfNeeded();
//            return;
//        }
//        else {
//            ZergMissionChangerWhenContain.changeMissionIfNeeded();
//            return;
//        }
//    }

    // =========================================================

    protected static boolean baseUnderSeriousAttack() {
        AUnit main = Select.main();
        if (main != null && Select.enemyCombatUnits().inRadius(20, main).atLeast(minEnemiesToDefend())) {
            return true;
        }

        return false;
    }

    private static int minEnemiesToDefend() {
        if (A.supplyUsed() < 30) {
            return 1;
        }
        else if (A.supplyUsed() < 50) {
            return 2;
        }
        else if (A.supplyUsed() < 80) {
            return 3;
        }
        else {
            return 6;
        }
    }

}
