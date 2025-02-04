package atlantis.combat.missions;

import atlantis.combat.missions.attack.MissionChangerWhenAttack;
import atlantis.combat.missions.contain.MissionChangerWhenContain;
import atlantis.combat.missions.defend.MissionChangerWhenDefend;
import atlantis.combat.missions.defend.protoss.sparta.Sparta;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.generic.Army;
import atlantis.information.strategy.GamePhase;
import atlantis.information.strategy.OurStrategy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.game.player.Enemy;
import atlantis.util.We;

public abstract class MissionChanger {
    public static final int MISSIONS_ENFORCED_FOR_SECONDS = 12;

    public static final boolean DEBUG = true;
    //    public static final boolean DEBUG = false;
    public static String reason = "---";

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
        return Missions.lastMissionEnforcedSecondsAgo() <= MISSIONS_ENFORCED_FOR_SECONDS;
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
        Alpha.get().setMission(newMission);

        if (Missions.globalMission().equals(newMission)) {
            return;
        }

        Missions.setGlobalMissionTo(newMission, reason);
        MissionHistory.missionHistory.add(newMission);

//        A.errPrintln("Change to " + newMission);
//        A.printStackTrace("Change to " + newMission);
    }

    public static boolean forceMissionAttack(String reason) {
        return Missions.forceGlobalMissionAttack(reason);
    }

    public static void forceMissionContain(String reason) {
        Missions.forceGlobalMissionContain(reason);
    }

    public static boolean forceMissionSpartaOrDefend(String reason) {
        if (Sparta.canUseSpartaMission()) {
            Missions.forceGlobalMissionSparta(reason);
        }
        else {
            Missions.forceGlobalMissionDefend(reason);
        }

        return true;
    }

    protected static boolean defendAgainstMassZerglings() {
        if (!Enemy.zerg()) return false;

        if (
            Count.ourCombatUnits() <= 6
                && EnemyUnits.discovered().ofType(AUnitType.Zerg_Zergling).atLeast(7)
                && Army.strength() <= 125
        ) {
            if (DEBUG) reason = "Mass zerglings A";
            return true;
        }

        if (
            A.seconds() <= 340
                && EnemyUnits.discovered().ofType(AUnitType.Zerg_Zergling).atLeast(9)
                && (!We.zerg() || !ArmyStrength.weAreStronger())
        ) {
            if (DEBUG) reason = "Mass zerglings B";
            return true;
        }

        if (
            Count.dragoons() <= 3 && Count.zealots() <= 5
                && EnemyUnits.discovered().ofType(AUnitType.Zerg_Zergling).atLeast(11)
        ) {
            if (DEBUG) reason = "Mass zerglings C";
            return true;
        }

        return false;
    }
}
