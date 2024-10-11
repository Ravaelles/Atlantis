package atlantis.combat.missions.attack;

import atlantis.combat.missions.Missions;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.enemy.OurBuildingUnderAttack;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.generic.OurArmy;
import atlantis.information.strategy.GamePhase;
import atlantis.information.strategy.OurStrategy;
import atlantis.production.dynamic.protoss.tech.ResearchSingularityCharge;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.Enemy;

public class ProtossMissionChangerWhenAttack extends MissionChangerWhenAttack {

    // === DEFEND ==============================================
    public boolean shouldChangeMissionToDefend() {
        if (Missions.lastMissionChangedSecondsAgo() <= 2) return false;

        if (ourBuildingUnderAttack()) {
            if (DEBUG) reason = "Enemy is near our building";
            return forceMissionSpartaOrDefend(reason);
        }

        if (A.s >= 60 * 10) {
            if (DEBUG) reason = "LateGame-attack";
            return false;
        }

        if (A.minerals() >= 2000) {
            if (DEBUG) reason = "2K_minerals";
            return false;
        }

        if (Enemy.zerg()) {
            if (defendVsZerg()) return true;
        }

        if (Enemy.protoss()) {
            if (defendVsProtoss()) return true;
            if (dontDefendVsProtoss()) return false;
        }

        if (ArmyStrength.ourArmyRelativeStrength() <= 120 && EnemyUnits.combatUnits() >= 2) {
            if (DEBUG) reason = "Hm, we are weaker (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
            return true;
        }

        if (enemyHasHiddenUnitsAndWeDontHaveEnoughDetection()) {
            if (DEBUG) reason = "Not enough detection";
            return true;
        }

        return false;
    }

    private static boolean ourBuildingUnderAttack() {
        return OurBuildingUnderAttack.notNull()
            && (A.supplyUsed() <= 140 || OurArmy.strength() <= 160)
            && A.minerals() <= 800
            && A.s <= 60 * 12;
    }

    private boolean defendVsZerg() {
        int combatUnits = Count.ourCombatUnits();

        if (OurStrategy.get().isRushOrCheese() && OurArmy.strength() >= 95) {
            if (DEBUG) reason = "Rush-or-cheese attack";
            return false;
        }

        if (!OurStrategy.get().isRushOrCheese() && Count.dragoons() <= 3 && OurArmy.strength() <= 120 && (
            combatUnits <= 7 || (combatUnits <= 8 && EnemyUnits.discovered().combatUnits().atLeast(11))
        )) {
            if (DEBUG) reason = "Wait for more army";
            return true;
        }

//        if (OurArmy.strength() <= 170 && !ResearchSingularityCharge.isResearched()) {
//            if (DEBUG) reason = "Wait for goon range(" + OurArmy.strength() + "%)";
//            return true;
//        }

        if (Count.dragoons() <= 1 && defendAgainstMassZerglings()) {
            return forceMissionSpartaOrDefend(reason = "Mass zerglings E");
        }

        if (EnemyUnits.hydras() >= 4 && OurArmy.strength() <= 136 && !ResearchSingularityCharge.isResearched()) {
            if (DEBUG) reason = "Hydras and no goon range(" + OurArmy.strength() + "%)";
            return true;
        }

        return false;
    }

    // =========================================================

    private boolean dontDefendVsProtoss() {
//        if (Count.dragoons() >= 2 && EnemyUnits.discovered().dragoons().empty()) return true;

        return false;
    }

    private boolean defendVsProtoss() {
        int strength = OurArmy.strength();

        if (Count.dragoons() == 0 && strength <= 700) {
            int enemyZealots = EnemyUnits.discovered().zealots().count();
            if (enemyZealots > Math.min(10, Count.zealots())) {
                if (DEBUG) reason = "Enemy has too many Zealots: " + enemyZealots;
                return true;
            }
        }

        if (EnemyUnits.discovered().dragoons().count() > Count.dragoons()) {
            if (Count.dragoons() <= 2 && OurArmy.strength() <= 300) {
                if (DEBUG) reason = "Enemy has more Dragoons";
                return true;
            }
        }

        if (strength >= 170) {
            return false;
        }
//        if (A.seconds() <= 400 && strength >= 150) {
//            return false;
//        }

        if (A.seconds() <= 700 && strength <= 200 && A.resourcesBalance() < 0) {
            if (DEBUG) reason = "Too much risk, withdraw";
            return true;
        }

        if (A.seconds() >= 450 && (strength <= 150 && Count.dragoons() <= 4)) {
            if (DEBUG) reason = "Wait for more Dragoons";
            return true;
        }

        if (strength <= 150 && EnemyUnits.discovered().dragoons().atLeast(1)) {
            if (DEBUG) reason = "Enemy has Goons";
            return true;
        }

        return false;
    }

    private boolean enemyHasHiddenUnitsAndWeDontHaveEnoughDetection() {
        if (Count.observers() > 0) return false;
        if (Have.cannon()) return false;

        return EnemyUnits.discovered().effUndetected().size() >= 2;
    }

    // === CONTAIN =============================================

    public boolean shouldChangeMissionToContain() {
        if (true) return false;
        if (A.supplyUsed() >= 176) return false;

        if (ArmyStrength.ourArmyRelativeStrength() <= 270) {
            if (DEBUG) reason = "Not strong enough to attack (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
            return true;
        }

        if (!GamePhase.isLateGame() && EnemyInfo.startedWithCombatBuilding && !ArmyStrength.weAreMuchStronger()) {
            if (DEBUG) reason = "startedWithCombatBuilding & !weAreMuchStronger";
            return true;
        }

        return false;
    }

}
