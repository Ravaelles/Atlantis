package atlantis.combat.missions.attack;

import atlantis.combat.missions.Missions;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.generic.OurArmy;
import atlantis.information.strategy.GamePhase;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.Enemy;

public class ProtossMissionChangerWhenAttack extends MissionChangerWhenAttack {

    // === DEFEND ==============================================
    public boolean shouldChangeMissionToDefend() {
        if (Missions.counter() <= 1 || Missions.lastMissionChangedSecondsAgo() <= 20) return false;

        if (Enemy.protoss()) {
            if (defendVsProtoss()) return true;
            if (dontDefendVsProtoss()) return false;
        }

        if (Enemy.zerg()) {
            if (defendVsZerg()) return true;
        }

        if (EnemyInfo.isEnemyNearAnyOurBase() && A.supplyUsed() <= 70) {
            if (DEBUG) reason = "Enemy is near our building";
            return true;
        }

        if (ArmyStrength.ourArmyRelativeStrength() <= 130) {
            if (DEBUG) reason = "Hmm, we are weaker (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
            return true;
        }

        if (enemyHasHiddenUnitsAndWeDontHaveEnoughDetection()) {
            if (DEBUG) reason = "Not enough detection";
            return true;
        }

        return false;
    }

    private boolean defendVsZerg() {
        int combatUnits = Count.ourCombatUnits();
        if (combatUnits <= 3 || (combatUnits <= 4 && EnemyUnits.discovered().combatUnits().atLeast(8))) {
            if (DEBUG) reason = "Wait for more army";
            return true;
        }

        if (defendAgainstMassZerglings()) {
            if (DEBUG) reason = "Mass zerglings";
            return true;
        }

        return false;
    }

    // =========================================================

    private boolean dontDefendVsProtoss() {
        if (Count.dragoons() >= 1 && EnemyUnits.discovered().dragoons().empty()) return true;

        return false;
    }

    private boolean defendVsProtoss() {
        int strength = OurArmy.strength();

        if (Count.dragoons() == 0) {
            int zealots = Count.zealots();
            if (zealots <= 7 && EnemyUnits.discovered().zealots().count() > zealots) {
                if (DEBUG) reason = "Enemy has more Zealots";
                return true;
            }
        }

        if (EnemyUnits.discovered().dragoons().count() > Count.dragoons()) {
            if (Count.dragoons() <= 2) {
                if (DEBUG) reason = "Enemy has more Dragoons";
                return true;
            }
        }

        if (A.seconds() <= 400 && strength >= 150) {
            return false;
        }

        if (A.seconds() <= 700 && strength <= 300) {
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
