package atlantis.combat.missions.defend.protoss;

import atlantis.Atlantis;
import atlantis.combat.missions.MissionHistory;
import atlantis.combat.missions.Missions;
import atlantis.combat.missions.attack.focus.EnemyExistingExpansion;
import atlantis.combat.missions.defend.MissionChangerWhenDefend;
import atlantis.combat.squad.squads.alpha.Alpha;
import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.enemy.EnemyUnitBreachedBase;
import atlantis.information.generic.Army;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.Strategy;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.game.player.Enemy;

public class ProtossMissionChangerWhenDefend extends MissionChangerWhenDefend {
    private static int strength;
    private static int dragoons;
    private int combatUnits;

    // === CONTAIN =============================================

//    private boolean changeFromSpartaToDefend() {
//        if (Missions.isGlobalMissionSparta() && Count.basesWithUnfinished() >= 2) return true;
//
//        return false;
//    }

    public boolean shouldChangeMissionToAttack() {
        strength = Army.strengthWithoutCB();
        dragoons = Count.dragoons();
        combatUnits = Count.ourCombatUnits();

        // === Force stay at DEFEND ================================

        if (ProtossForceMissionDefend.check(strength, combatUnits)) {
            return forceMissionSpartaOrDefend(reason);
        }

        // === FORCE ATTACK ========================================

        if (ProtossShouldForceMissionAttack.shouldForce(strength, dragoons, combatUnits)) {
            return forceMissionAttack(reason);
        }

        // =========================================================

        if (combatUnits <= 3) {
            if (DEBUG) reason = "TooFewArmy(" + combatUnits + ")";
            return false;
        }

        if (Enemy.zerg()) {
            if (Strategy.get().isGoingTech() && Army.strength() <= 180 && Count.dragoons() <= 5) {
                if (DEBUG) reason = "TechWait";
                forceMissionSpartaOrDefend("TechWait");
                return false;
            }

            if (Count.ourCombatUnits() >= 6 && pressureZergWithLittleEarlyLings()) {
                return true;
            }

            if (A.resourcesBalance() <= -300 && Count.ourCombatUnits() <= 15) {
                if (DEBUG) reason = "Low balance PvZ";
                forceMissionSpartaOrDefend(reason);
                return false;
            }

            if (!Strategy.get().isRushOrCheese() && Army.strengthWithoutCB() <= 200 && Count.ourCombatUnits() <= 20) {
                if (DEBUG) reason = "Cautious PvZ";
                forceMissionSpartaOrDefend(reason);
                return false;
            }
        }

        if (A.s <= 30 * 6 && dragoons <= 1 && Count.cannons() >= 2) {
            if (DEBUG) reason = "Wait for Goon with Cannons";
            return false;
        }

        Decision decision;

        if (Enemy.protoss()) {
            decision = shouldAttackVsProtoss();

            if (decision.notIndifferent()) return decision.toBoolean();
        }

        if (A.s <= 30 * 5 && rushAllowsAttack()) {
            return forceMissionAttack(reason);
        }

//        double changedAgo = Missions.lastMissionChangedSecondsAgo();
//        if (changedAgo <= 3) {
//            if (DEBUG) reason = "Mission just changed (" + (int) changedAgo + "s)";
//            return false;
//        }

////        if (A.supplyUsed() <= 110 && EnemyInfo.isEnemyNearAnyOurBase()) return false;
//        if (A.supplyUsed() <= 100 && Army.strength() <= 145 && EnemyExistingExpansion.notFound()) {
//            if (DEBUG) reason = "Early game and too weak (" + Army.strength() + "%)";
//            return false;
//        }

        if (ProtossStickCombatToMainBaseEarly.should()) {
            if (DEBUG) reason = "Stick to main early";
            forceMissionSpartaOrDefend(reason);
            return false;
        }

        int enemyCombatUnits = EnemyUnits.combatUnits();

        if (combatUnits <= 8 && strength <= 160) return false;
        if (enemyCombatUnits >= 12 && combatUnits <= 8) return false;
        if (enemyCombatUnits >= 15 && combatUnits <= 9) return false;

        if (Enemy.zerg()) {
            decision = shouldAttackVsZerg();

            if (decision.notIndifferent()) return decision.toBoolean();
        }

        if (Missions.isGlobalMissionSparta()) {
            return whenSparta();
        }

        if (strength >= 240 && dragoons >= 2 && A.resourcesBalance() > 0) {
            if (DEBUG) reason = "Ah, much stronger (" + strength + "%)";
            return true;
        }

        return false;
    }

    private static boolean pressureZergWithLittleEarlyLings() {
        if (
            A.s <= 60 * 7
                && EnemyUnits.zerglings() <= 4
                && EnemyUnits.hydras() <= 0
                && Count.ourCombatUnits() >= 5
                && Army.strengthWithoutCB() >= 160
                && !Have.cyberneticsCoreWithUnfinished() && dragoons <= 1
        ) {
            if (DEBUG) reason = "Pressure zerg with little lings";
            return true;
        }

        return false;
    }

    private boolean rushAllowsAttack() {
        if (Strategy.get().isRushOrCheese() && Count.ourCombatUnits() >= 2 && Army.strengthWithoutCB() >= 160) {
            if (DEBUG) reason = "Rush allows it";
            return true;
        }

        return false;
    }

    private Decision shouldAttackVsZerg() {
        int combatUnits = Count.ourCombatUnits();
        int enemyCombatUnits = EnemyUnits.combatUnits();

        if (dragoons <= 1) {
            double ratio = Count.ourCombatUnits() * 2.75;
            if (ratio <= EnemyUnits.combatUnits()) {
                if (DEBUG) reason = "Wait for more Goons (ratio: " + ratio + ")";
                return Decision.FALSE;
            }
        }

        if (A.s >= 60 * 6 && Count.ourCombatUnits() <= 15 && Army.strengthWithoutCB() <= 200) {
            if (DEBUG) reason = "Not enough army vZ (" + Count.ourCombatUnits() + ")";
            return Decision.FALSE;
        }

//        if (A.supplyUsed() <= 100 && strength <= 145 && EnemyExistingExpansion.notFound()) {
//            if (DEBUG) reason = "Early game and too weak (" + strength + "%)";
//            return Decision.FALSE;
//        }

        Decision decision = ProtossShouldPunishZergEarly.shouldPunishZergEarly();
        if (decision.notIndifferent()) {
            if (decision.isAllowed()) {
                if (DEBUG) reason = "PunishZergEarly";
                return Decision.TRUE;
            }
            return decision;
        }

        if (combatUnits >= 10) {
            if (
                strength >= 350
                    || (combatUnits >= 12 && EnemyExistingExpansion.found())
                    || combatUnits >= 25
                    || A.hasMinerals(1000)
            ) {
                if (DEBUG) reason = "StrongAndBig";
                return Decision.TRUE;
            }

//            return Decision.FALSE;
        }

//        if (strength <= 360 && combatUnits <= 7) return Decision.FALSE;

        if (A.s <= 650 && dragoons >= 1 && A.resourcesBalance() >= -250 && EnemyUnits.hydras() <= 2) {
            if (DEBUG) reason = "GoWithGoonz";
            return Decision.TRUE;
        }

        // Successfully defended early ling push, make pressure
        if (A.s <= 650 && combatUnits >= 8 && strength >= 130 + (A.resourcesBalance() >= -100 ? 0 : 30)) {
            if (EnemyUnits.zerglings() * 3 <= combatUnits) {
                if (DEBUG) reason = "DefendedPvZSoPress";
                return Decision.TRUE;
            }
        }

        if (dragoons <= 6 && combatUnits <= 8) {
            if (EnemyUnits.zerglings() * 3 >= dragoons) return Decision.FALSE;
        }

        if (combatUnits <= 5 && A.resourcesBalance() <= 100) return Decision.FALSE;

        return Decision.INDIFFERENT;
    }

    private boolean canPushEarlyVsProtoss() {
        if (!Enemy.protoss()) return false;
        if (A.seconds() >= 600) return false;
        if (Atlantis.LOST >= 3 && A.resourcesBalance() <= 800) return false;

        if (dragoons >= 1 && strength >= 120 && EnemyUnits.dragoons() == 0) {
            return true;
        }

        if (dragoons >= 2 && EnemyUnits.dragoons() == 0) {
            return true;
        }

        return (strength >= 190 && MissionHistory.numOfChanges() <= 2 && Count.ourCombatUnits() >= 3);
//            || (MissionHistory.numOfChanges() <= 3 && dragoons >= 6);
//        return relativeStrength >= 90 ;
    }

    private static boolean notAllowedToDoEarlyPushVsProtoss() {
        return dragoons < 2 && Count.ourCombatUnits() < 3;
    }

    private Decision shouldAttackVsProtoss() {
//        if (postEarlyGameDontAttackProtoss()) return Decision.FALSE;
        if (!Enemy.protoss()) return Decision.INDIFFERENT;

        int strength = Army.strength();

        if (
            strength >= 150
                && (Count.ourCombatUnits() >= 12 || !EnemyInfo.hasRanged())
                && Alpha.get().cohesionPercent() >= 70
        ) {
            if (DEBUG) reason = "Stronger Protoss! (" + strength + "%)";
            return Decision.TRUE;
        }

        if (shouldWaitVsProtossWithEarlyPush()) {
            reason = "Wait vs Protoss early";
            return Decision.FALSE;
        }

        if (dontPushEarlyVsProtoss()) {
            reason = "Don't push early vs Protoss";
            return Decision.FALSE;
        }

        if (canPushEarlyVsProtoss()) {
            reason = "Early push (" + this.strength + "%)";
            return Decision.TRUE;
        }

//        if (
//            Count.basesWithUnfinished() <= 2
//                && ExpansionCommander.lastExpandedLessThanSecondsAgo(50)
//        ) return Decision.FALSE;

        if (beBraveProtoss()) {
            if (DEBUG) reason = "Brave Protoss! (" + strength + "%)";
            return Decision.TRUE;
        }

        return Decision.INDIFFERENT;
    }

    private boolean shouldWaitVsProtossWithEarlyPush() {
        if (!Strategy.get().isGoingTech()) return false;

        if (Count.ourCombatUnits() <= 7) return true;

        return false;
    }

    private boolean dontPushEarlyVsProtoss() {
        int enemyDragoons = EnemyUnits.dragoons();
        if (enemyDragoons == 0) return false;

        AUnit leader = Alpha.alphaLeader();
        if (leader != null && leader.eval() >= 1.2) return false;

        return enemyDragoons >= dragoons - 2;
    }

    protected static boolean shouldEngageWithGoonsVsProtoss(int strength) {
        if (A.s >= 60 * 7) return false;

        int enemyDragoons = EnemyUnits.dragoons();
        if (enemyDragoons == 0 && EnemyUnits.combatUnits() <= 6) return true;

        AUnit leader = Alpha.alphaLeader();
        if (leader != null && leader.squad().cohesionPercent() <= 70) return false;

        if (A.resourcesBalance() >= 600 && (enemyDragoons <= 1 || Army.strengthWithoutCB() >= 200)) return true;

        return (strength >= (Enemy.protoss() ? 350 : 40) || dragoons >= 9)
            && (enemyDragoons == 0 || (dragoons >= enemyDragoons + 2))
            && (dragoons >= 3 || (dragoons >= 1 && enemyDragoons == 0))
            && (enemyDragoons <= 1 || dragoons >= 4 * enemyDragoons);
    }

    private static boolean postEarlyGameDontAttackProtoss() {
        return A.seconds() >= 300
            && A.supplyUsed() <= 190
            && A.minerals() <= 1000
            && Army.strength() <= 800;
    }

    private static boolean beBraveProtoss() {
        if (
            Enemy.protoss()
                && A.s <= 60 * 12
                && Atlantis.LOST >= 4
                && (A.resourcesBalance() <= 100 || Count.ourCombatUnits() <= 25)
        ) return false;

        return (A.resourcesBalance() >= 600 || Army.strengthWithoutCB() >= 500)
            && (Count.ourCombatUnits() >= 15 || A.resourcesBalance() >= 400)
            && (Count.ourCombatUnits() >= 15 || Army.strengthWithoutCB() >= 700)
            && (Count.ourCombatUnits() >= 15 || Army.strengthWithoutCB() >= 700)
            && EnemyUnitBreachedBase.noone();
//            && Select.enemyCombatUnits().atMost(3);
    }

    private boolean whenSparta() {
        if (ArmyStrength.ourArmyRelativeStrength() >= 200 && (
            AGame.killsLossesResourceBalance() >= 100
                || dragoons >= 3
                || Count.zealots() >= 7
        )) {
            if (DEBUG) reason = "Spartans strong! (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
            return true;
        }

        return false;
    }

    public boolean shouldChangeMissionToContain() {
        if (ArmyStrength.ourArmyRelativeStrength() < 200) return false;

        if (EnemyInfo.isEnemyNearAnyOurBase()) return false;

//        if ((GamePhase.isEarlyGame() && A.resourcesBalance() >= 300)) {
//            if (DEBUG) reason = "resources balance is good";
//            return true;
//        }

//        if (A.supplyUsed(90)) {
//            if (DEBUG) reason = "Supply quite big";
//            return true;
//        }

        if (DEBUG) reason = "Why not";
        return true;
    }

}
