package atlantis.combat.missions;

import atlantis.AGame;
import atlantis.Atlantis;
import atlantis.strategy.GamePhase;
import atlantis.strategy.OurStrategy;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.A;
import atlantis.util.Enemy;

public class TerranMissionChangerWhenContain {

    public static void changeMissionIfNeeded() {
        if (shouldChangeMissionToDefend()) {
            Missions.setGlobalMissionDefend();
        }

        else if (shouldChangeMissionToAttack()) {
            Missions.setGlobalMissionAttack();
        }
    }

    // =========================================================

    protected static boolean shouldChangeMissionToDefend() {
//        if (Atlantis.LOST_RESOURCES <= 150) {
//            return false;
//        }

        if (Have.base() && Select.enemyCombatUnits().inRadius(15, Select.mainBase()).atLeast(3)) {
            return true;
        }

        if (Missions.counter() >= 3) {
            if (Enemy.zerg()) {
                return true;
            }
        }

        if (OurStrategy.get().goingBio() && !Missions.isFirstMission()) {
            if (Count.tanks() <= 1) {
                return Count.ourCombatUnits() <= 6;
            }
        }

        if (Enemy.protoss()) {
            return Count.ourCombatUnits() <= 12;
        }

        return Count.tanks() <= 1 && Count.ourCombatUnits() <= 12;
//        return Select.ourTanks().count() == 0 || Select.ourCombatUnits().count() <= 9;
    }

    protected static boolean shouldChangeMissionToAttack() {
        if (Select.mainBase() != null) {
            if (Select.enemy().inRadius(14, Select.mainBase()).atLeast(2)) {
                return false;
            }
        }

        if (killsBalanceSaysSo()) {
            return true;
        }

        if (OurStrategy.get().goingBio() && (Count.medics() >= 9 || Count.ourCombatUnits() >= 40)) {
            return Count.ourCombatUnits() >= Math.min(40, 15 + Missions.counter() * 2);
        }

        return Select.ourTanks().count() >= 2 || Select.ourCombatUnits().count() >= 40;
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
