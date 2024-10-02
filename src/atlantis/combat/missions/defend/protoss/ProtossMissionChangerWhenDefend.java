package atlantis.combat.missions.defend.protoss;

import atlantis.combat.missions.MissionChanger;
import atlantis.combat.missions.MissionHistory;
import atlantis.combat.missions.Missions;
import atlantis.combat.missions.defend.MissionChangerWhenDefend;
import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.enemy.EnemyWhoBreachedBase;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.generic.OurArmy;
import atlantis.production.dynamic.expansion.ExpansionCommander;
import atlantis.units.select.Count;
import atlantis.util.Enemy;

public class ProtossMissionChangerWhenDefend extends MissionChangerWhenDefend {
    private int relativeStrength;
    private int strength;
    private int dragoons;

    // === CONTAIN =============================================

//    private boolean changeFromSpartaToDefend() {
//        if (Missions.isGlobalMissionSparta() && Count.basesWithUnfinished() >= 2) return true;
//
//        return false;
//    }

    public boolean canChange() {
        if (Missions.lastMissionChangedSecondsAgo() <= 10) return false;
        if (EnemyInfo.isEnemyNearAnyOurBase()) return false;

//        @Here
//        if (true) return false;

        relativeStrength = ArmyStrength.ourArmyRelativeStrength();

////        if (A.seconds() <= 400) {
////            if (Enemy.protoss()) {
//////                if (notAllowedToDoEarlyPushVsProtoss()) return false;
////                if (canPushEarlyVsProtoss()) {
////                    return true;
////                }
////            }
////
////            if (AGame.killsLossesResourceBalance() < 0) return false;
////            else {
////                if (Enemy.terran() && relativeStrength >= 110) {
////                    reason = "Early game pressure (" + relativeStrength + "%)";
////                    return true;
////                }
////
////                if (Enemy.protoss() && relativeStrength >= 200) {
////                    reason = "Early game push (" + relativeStrength + "%)";
////                    return true;
////                }
////
//////                else {
//////                    return false;
//////                }
////            }
//        }
//
////        if (GamePhase.isEarlyGame() && Count.dragoons() <= 3) {
//        if (GamePhase.isEarlyGame()) {
//            if (
//                EnemyStrategy.get().isRushOrCheese()
//                    && (A.resourcesBalance() < 350 || !ArmyStrength.weAreMuchStronger())
//            ) return false;
//
//            if (Count.cannons() >= 1 && Count.ourCombatUnits() <= 8) return false;
//
//            if (EnemyUnits.discovered().ofType(AUnitType.Protoss_Zealot).atLeast(4)) return false;
//        }

        return true;
    }

    private boolean canPushEarlyVsProtoss() {
        if (A.seconds() >= 600) return false;
//        if (EnemyUnits.dragoons() >= 1) return false;

        int dragoons = Count.dragoons();

        if (dragoons >= 1 && OurArmy.strength() >= 130) {
            return true;
        }

        if (dragoons >= 2 && OurArmy.strength() >= 160) {
            return true;
        }

        return (relativeStrength >= 190 && MissionHistory.numOfChanges() <= 2 && Count.zealots() >= 3);
//            || (MissionHistory.numOfChanges() <= 3 && Count.dragoons() >= 6);
//        return relativeStrength >= 90 ;
    }

    private static boolean notAllowedToDoEarlyPushVsProtoss() {
        return Count.dragoons() < 2 && Count.ourCombatUnits() < 3;
    }

    public boolean shouldChangeMissionToAttack() {
        if (!canChange()) return false;

        // @Temp
//        if (Count.ourCombatUnits() <= 7) return false;

        Decision decision;

        if (Enemy.protoss()) {
            decision = shouldAttackVsProtoss();

            if (decision.notIndifferent()) return decision.toBoolean();

//            if (postEarlyGameDontAttackProtoss()) return false;
//
//            if (Count.basesWithUnfinished() <= 2 && ExpansionCommander.lastExpandedLessThanSecondsAgo(50)) return false;
//
//            if (beBraveProtoss()) {
//                if (DEBUG) reason = "Brave Protoss! (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
//                return true;
//            }
//
//            if (canPushEarlyVsProtoss()) {
//                reason = "Early push (" + relativeStrength + "%)";
//                return true;
//            }
        }

        strength = ArmyStrength.ourArmyRelativeStrength();
        dragoons = Count.dragoons();

        if (Enemy.zerg()) {
            decision = shouldAttackVsZerg();

            if (decision.notIndifferent()) return decision.toBoolean();
        }

        if (Missions.isGlobalMissionSparta()) {
            return whenSparta();
        }

        if (strength >= 240 && dragoons >= 2) {
            if (DEBUG) reason = "Ah, much stronger (" + strength + "%)";
            return true;
        }

        return false;
    }

    private Decision shouldAttackVsZerg() {
        int combatUnits = Count.ourCombatUnits();

        Decision decision = ProtossShouldPunishZergEarly.shouldPunishZergEarly();
        if (decision.notIndifferent()) {
            if (decision.isAllowed()) {
                MissionChanger.forceMissionAttack("PunishZergEarly");
            }
            return decision;
        }

//        if (strength <= 360 && combatUnits <= 7) return Decision.FALSE;

        // Successfully defended early ling push, make pressure
        if (A.s <= 650 && combatUnits >= 8 && strength >= 130 + (A.resourcesBalance() >= -100 ? 0 : 30)) {
            if (EnemyUnits.zerglings() * 3 <= combatUnits) {
                MissionChanger.forceMissionAttack("DefendedPvZSoPress");
                return Decision.TRUE;
            }
        }

        if (dragoons <= 6 && combatUnits <= 8) {
            if (EnemyUnits.zerglings() * 3 >= dragoons) return Decision.FALSE;
        }

        if (combatUnits <= 5 && A.resourcesBalance() <= 100) return Decision.FALSE;

        return Decision.INDIFFERENT;
    }

    private Decision shouldAttackVsProtoss() {
//        if (postEarlyGameDontAttackProtoss()) return Decision.FALSE;

        int strength = OurArmy.strength();

        if (
            Count.dragoons() >= 1
                && EnemyUnits.discovered().dragoons().empty()
        ) {
            if (DEBUG) reason = "Engage with Goons! (" + strength + "%)";
            return Decision.TRUE;
        }

        if (strength >= 170) {
            if (DEBUG) reason = "Stronger Protoss! (" + strength + "%)";
            return Decision.TRUE;
        }

        if (canPushEarlyVsProtoss()) {
            reason = "Early push (" + relativeStrength + "%)";
            MissionChanger.forceMissionAttack(reason);
            return Decision.TRUE;
        }

        if (
            Count.basesWithUnfinished() <= 2
                && ExpansionCommander.lastExpandedLessThanSecondsAgo(50)
        ) return Decision.FALSE;

        if (beBraveProtoss()) {
            if (DEBUG) reason = "Brave Protoss! (" + strength + "%)";
            return Decision.TRUE;
        }

        return Decision.INDIFFERENT;
    }

    private static boolean postEarlyGameDontAttackProtoss() {
        return A.seconds() >= 300
            && A.supplyUsed() <= 190
            && A.minerals() <= 1000
            && OurArmy.strength() <= 800;
    }

    private static boolean beBraveProtoss() {
        return Count.ourCombatUnits() >= 5
            && ArmyStrength.ourArmyRelativeStrength() >= 300
            && EnemyWhoBreachedBase.noone();
//            && Select.enemyCombatUnits().atMost(3);
    }

    private boolean whenSparta() {
        if (ArmyStrength.ourArmyRelativeStrength() >= 200 && (
            AGame.killsLossesResourceBalance() >= 100
                || Count.dragoons() >= 3
                || Count.zealots() >= 7
        )) {
            if (DEBUG) reason = "Spartans strong! (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
            return true;
        }

        return false;
    }

    public boolean shouldChangeMissionToContain() {
        if (!canChange()) return false;

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
