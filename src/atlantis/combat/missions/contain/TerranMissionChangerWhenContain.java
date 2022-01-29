package atlantis.combat.missions.contain;

import atlantis.Atlantis;
import atlantis.combat.missions.MissionChanger;
import atlantis.combat.missions.Missions;
import atlantis.combat.missions.attack.TerranMissionChangerWhenAttack;
import atlantis.combat.missions.defend.TerranMissionChangerWhenDefend;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.strategy.OurStrategy;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.Enemy;

public class TerranMissionChangerWhenContain extends MissionChanger {

    public static void changeMissionIfNeeded() {
        if (shouldChangeMissionToDefend() && !TerranMissionChangerWhenDefend.shouldChangeMissionToContain()) {
            Missions.setGlobalMissionDefend();
        }

        else if (shouldChangeMissionToAttack() && !TerranMissionChangerWhenAttack.shouldChangeMissionToContain()) {
            Missions.setGlobalMissionAttack();
        }
    }

    // =========================================================

    protected static boolean shouldChangeMissionToDefend() {
        if (
                A.supplyUsed(150)
                || Atlantis.LOST <= 4
        ) {
            return false;
        }

        if (
                Count.ourCombatUnits() <= 20 && Atlantis.LOST >= 2
        ) {
            return true;
        }

//        if (EnemyInformation.isEnemyNearAnyOurBuilding()) {
//            return true;
//        }

        // === Zerg ======================================================

        if (Enemy.zerg()) {
            if (Missions.counter() <= 3) {
//                return A.resourcesBalance() < 150 || Count.ourCombatUnits() <= 15;
                return Count.ourCombatUnits() <= 3 && A.resourcesBalance() < -100;
            }
        }

//        if (OurStrategy.get().goingBio() && !Missions.isFirstMission()) {
//            if (Count.tanks() <= 1) {
//                return Count.ourCombatUnits() <= 6;
//            }
//        }
        // === Protoss ======================================================

        if (Enemy.protoss()) {
            if (A.resourcesBalance() <= -200 && Count.ourCombatUnits() <= 20) {
                return true;
            }

            return Count.ourCombatUnits() <= 14;
        }

        // =========================================================

        return Count.tanks() <= 1 && Count.ourCombatUnits() <= 5;
//        return Select.ourTanks().count() == 0 || Select.ourCombatUnits().count() <= 9;
    }

    protected static boolean shouldChangeMissionToAttack() {
        if (Missions.counter() >= 3 && A.supplyUsed() <= 90) {
            return false;
        }

        if (A.seconds() >= 600 && (A.supplyUsed() >= 195 || Count.ourCombatUnits() >= 40)) {
            return true;
        }

        if (Select.main() != null) {
            if (Select.enemy().inRadius(14, Select.main()).atLeast(3)) {
                return false;
            }
        }

//        if (killsBalanceSaysSo()) {
//            return true;
//        }

        if (OurStrategy.get().goingBio() && (Count.medics() >= 9 || Count.ourCombatUnits() >= 40)) {
            return Count.ourCombatUnits() >= Math.min(40, 15 + Missions.counter() * 2);
        }

        return Select.ourTanks().count() >= 5 || Select.ourCombatUnits().count() >= 40;
    }

    protected static boolean killsBalanceSaysSo() {
        if (AGame.timeSeconds() <= 400 && AGame.killsLossesResourceBalance() >= 900) {
            return true;
        }

        return AGame.timeSeconds() <= 700 && AGame.killsLossesResourceBalance() >= 1600;
    }

    /**
     * Defines how many military units we should have before pushing forward towards the enemy.
     */
//    protected static int defineMinUnitsToStrategicallyAttack() {
//        return 18;
//    }
//
//    protected static boolean shouldChangeMissionToContain() {
//        int ourCombatUnits = Select.ourCombatUnits().count();
//
//        return ourCombatUnits <= 13;
//    }

}
