package atlantis.combat.missions;

import atlantis.combat.missions.attack.MissionChangerWhenAttack;
import atlantis.combat.missions.contain.MissionChangerWhenContain;
import atlantis.combat.missions.defend.MissionChangerWhenDefend;
import atlantis.combat.missions.defend.sparta.Sparta;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.GamePhase;
import atlantis.information.strategy.OurStrategy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.Enemy;
import atlantis.util.We;

public abstract class MissionChanger {
    public static final int MISSIONS_ENFORCED_FOR_SECONDS = 20;

    public static final boolean DEBUG = true;
    //    public static final boolean DEBUG = false;
    public static String reason = "";

    // =========================================================

    /**
     * Takes care of current strategy.
     */
    public static void evaluateGlobalMission() {
        if (A.everyFrameExceptNthFrame(26)) return;

        // === Handle UMS ==========================================

        if (AGame.isUms()) {
            if (!Missions.isGlobalMissionAttack()) forceMissionAttack("UmsAlwaysAttack");
            return;
        }

        // =========================================================

        if (OurStrategy.get().isRushOrCheese() && GamePhase.isEarlyGame()) {
            return;
        }

        if (!Have.main() || lastMissionChangedJustSomeTimeAgo()) {
            return;
        }

        reason = "";

        MissionChanger.changeCurrentMissionIfNeeded();
    }

    private static boolean lastMissionChangedJustSomeTimeAgo() {
        return Missions.lastMissionEnforcedAgo() <= MISSIONS_ENFORCED_FOR_SECONDS * 30;
    }

    public static void forceEvaluateGlobalMission() {
        changeCurrentMissionIfNeeded();
    }

    protected abstract void changeMissionIfNeeded();

    private static void changeCurrentMissionIfNeeded() {
//        if (Missions.recentlyChangedMission()) {
//            return;
//        }

        if (Missions.isGlobalMissionAttack()) {
            MissionChangerWhenAttack.get().changeMissionIfNeeded();
        }
        else if (Missions.isGlobalMissionContain()) {
            MissionChangerWhenContain.get().changeMissionIfNeeded();
        }
        else if (Missions.isGlobalMissionDefendOrSparta()) {
            MissionChangerWhenDefend.get().changeMissionIfNeeded();
        }
    }

    // =========================================================

    public static Mission defendOrSpartaMission() {
//        if (We.protoss() || We.terran()) {

        if (A.seconds() <= 60 * 7 && Count.basesWithUnfinished() <= 1) {
            if (Sparta.canUseSpartaMission()) {
                return Missions.SPARTA;
            }
        }

        return Missions.DEFEND;
    }

    public static void notifyThatUnitRetreated(AUnit unit) {
        if (A.isUms()) {
            return;
        }

        if (OurStrategy.get().isRushOrCheese() && GamePhase.isEarlyGame()) {
            return;
        }

        if (Missions.isFirstMission()) {
            if (Missions.isGlobalMissionAttack() && unit.friendsNear().atLeast(3)) {
                forceMissionSpartaOrDefend("BetterDefendRatherThanAttack");
//                forceMissionContain("BetterContainRatherThanAttacking");
            }
        }
    }

    // =========================================================

    protected static void changeMissionTo(Mission newMission) {
        Missions.setGlobalMissionTo(newMission, reason);
        MissionHistory.missionHistory.add(newMission);

        Alpha.get().setMission(newMission);

        A.errPrintln("Change to " + newMission);
//        A.printStackTrace("Change to " + newMission);
    }

    public static void forceMissionAttack(String reason) {
        if (ArmyStrength.ourArmyRelativeStrength() >= 90 || Count.ourCombatUnits() <= 4) {
            Missions.forceGlobalMissionAttack(reason);
        }
    }

    public static void forceMissionContain(String reason) {
        Missions.forceGlobalMissionContain(reason);
    }

    public static void forceMissionSpartaOrDefend(String reason) {
        if (Sparta.canUseSpartaMission()) {
            Missions.forceGlobalMissionSparta(reason);
        }
        else {
            Missions.forceGlobalMissionDefend(reason);
        }
    }

    protected static boolean defendAgainstMassZerglings() {
        if (
            Enemy.zerg()
                && A.seconds() <= 260
                && EnemyUnits.discovered().ofType(AUnitType.Zerg_Zergling).atLeast(9)
                && (!We.zerg() || !ArmyStrength.weAreStronger())
        ) {
            if (DEBUG) reason = "Mass zerglings";
            return true;
        }

        return false;
    }
}
