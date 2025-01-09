package atlantis.combat.missions.attack;

import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.missions.Missions;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnitBreachedBase;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.enemy.OurBuildingUnderAttack;
import atlantis.information.generic.Army;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.GamePhase;
import atlantis.information.strategy.Strategy;
import atlantis.production.dynamic.protoss.tech.ResearchSingularityCharge;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.game.player.Enemy;

public class ProtossMissionChangerWhenAttack extends MissionChangerWhenAttack {

    // === DEFEND ==============================================
    public boolean shouldChangeMissionToDefend() {
        if (Missions.lastMissionChangedSecondsAgo() <= 2) return false;

//        if (ourBuildingUnderAttack() != null) {
//            if (DEBUG) reason = "Enemy is near our building";
//            return forceMissionSpartaOrDefend(reason);
//        }

        if (A.s >= 60 * 10) {
            if (DEBUG) reason = "LateGame-attack";
            return false;
        }

        if (A.minerals() >= 2000 && Count.ourCombatUnits() >= 10) {
            if (DEBUG) reason = "2K_minerals";
            return false;
        }

        if (baseUnderAttack()) {
            if (DEBUG) reason = "Base_under_attack";
            return forceMissionSpartaOrDefend(reason);
        }

        if (Enemy.zerg() && Count.ourCombatUnits() <= 7 && Count.dragoons() <= 2 && Army.strengthWithoutCB() <= 120) {
            if (DEBUG) reason = "Defend_vZ_no_Goonz";
            return true;
        }

        if (A.s <= 60 * 6 && Count.ourWithUnfinished(AUnitType.Protoss_Cybernetics_Core) > 0 && Count.dragoons() <= 1 && A.s >= 60 * 4.3) {
            if (Enemy.protoss() && A.resourcesBalance() >= -100 && Count.zealots() <= 2 && (Army.strengthWithoutCB() >= 110 || Count.dragoons() >= 1)) {
                // No need to wait
            }
            else if (EnemyUnits.ranged() <= 1) {
                if (DEBUG) reason = "Invested in Goons";
                return true;
            }
        }

        if (Enemy.zerg()) {
            if (defendVsZerg()) return true;
        }

        if (Enemy.protoss()) {
            if (defendVsProtoss()) return true;
            if (dontDefendVsProtoss()) return false;
        }

//        if (Army.strengthWithoutCB() <= 120 && EnemyUnits.combatUnits() >= 2) {
        if (Army.strength() <= 125 && EnemyUnits.combatUnits() >= 2) {
            if (!ignoreWeAreWeaker()) {
                if (DEBUG) reason = "Hm, we are weaker (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
                return true;
            }
        }

        if (enemyHasHiddenUnitsAndWeDontHaveEnoughDetection()) {
            if (DEBUG) reason = "Not enough detection";
            return true;
        }

        return false;
    }

    private boolean baseUnderAttack() {
        if (A.supplyUsed() <= 160 && !A.hasMinerals(2000)) {
            AUnit enemyInBase = EnemyUnitBreachedBase.get();
            if (
                enemyInBase != null
                    && enemyInBase.hasPosition()
                    && enemyInBase.isVisibleUnitOnMap()
                    && enemyInBase.hp() > 0
                    && (enemyInBase.friendsInRadiusCount(5) >= 1 || enemyInBase.isCrucialUnit())
            ) {
                return true;
            }
        }

        return false;
    }

    private static boolean ignoreWeAreWeaker() {
        return (!Enemy.zerg() || (Count.dragoons() >= 0.65 * EnemyUnits.hydras()))
            && (!Enemy.protoss() || (Count.dragoons() >= 2 * EnemyUnits.dragoons()));
    }

    private static AUnit ourBuildingUnderAttack() {
        AUnit building = OurBuildingUnderAttack.get();

        return building != null
            && (A.supplyUsed() <= 140 || Army.strength() <= 160)
            && A.minerals() <= 800
            && A.s <= 60 * 12 ? building : null;
    }

    private boolean defendVsZerg() {
        int combatUnits = Count.ourCombatUnits();

        int hydras = EnemyUnits.hydras();
        int lings = EnemyUnits.lings();
        int goons = Count.dragoons();

        if (lings >= 2 && goons <= 1 && combatUnits <= 5 && lings >= 2.9 * combatUnits) {
            if (DEBUG) reason = "Too many lings, ratio: (" + A.digit((double) lings / (combatUnits + 0.01)) + ")";
            return forceMissionSpartaOrDefend(reason);
        }

//        if (
//            goons >= 12
//                && !ResearchSingularityCharge.isResearched()
//                && Army.strengthWithoutCB() <= 125
//        ) {
//            if (DEBUG) reason = "Goons (" + goons + ") and no goon range(" + Army.strength() + "%)";
//            return forceMissionSpartaOrDefend(reason);
//        }

//        if (goons >= 1 && hydras >= 2 && Army.strengthWithoutCB() <= 155 && !ResearchSingularityCharge.isResearched()) {
//            if (DEBUG) reason = "Hydras and no goon range(" + Army.strength() + "%)";
//            return forceMissionSpartaOrDefend(reason);
//        }

        if (Strategy.get().isRushOrCheese() && Army.strengthWithoutCB() >= 95) {
            if (DEBUG) reason = "Rush-or-cheese attack";
            return false;
        }

        if (!Strategy.get().isRushOrCheese() && goons <= 3 && Army.strength() <= 120 && (
            combatUnits <= 7 || (combatUnits <= 8 && EnemyUnits.discovered().combatUnits().atLeast(11))
        )) {
            if (DEBUG) reason = "Wait for more army";
            return true;
        }

//        if (Army.strength() <= 170 && !ResearchSingularityCharge.isResearched()) {
//            if (DEBUG) reason = "Wait for goon range(" + Army.strength() + "%)";
//            return true;
//        }

        if ((goons <= 1 || (goons <= 3 && Army.strength() <= 120)) && defendAgainstMassZerglings()) {
            return forceMissionSpartaOrDefend(reason = "Mass zerglings E!");
        }

        if (hydras > 0 && goons <= 6 && hydras * 2.5 >= goons && Army.strengthWithoutCB() <= 190) {
            if (DEBUG) reason = "Hydras and weaker army";
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
        int strength = Army.strength();

        int goons = Count.dragoons();
        int enemyGoons = EnemyUnits.dragoons();

        if (goons < enemyGoons && Army.strength() <= 115) {
            if (DEBUG) reason = "Enemy has more Goons";
            return true;
        }

        if (goons == 0 && strength <= 110) {
            int enemyZealots = EnemyUnits.discovered().zealots().count();
            if (enemyZealots >= 3 && enemyZealots > Math.min(10, Count.zealots())) {
                if (DEBUG) reason = "Enemy has too many Zealots: " + enemyZealots;
                return true;
            }
        }

        if (EnemyUnits.discovered().dragoons().count() > goons) {
            if (goons <= 2 && Army.strength() <= 300) {
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
            if (Count.dragoons() < 1.6 * EnemyUnits.dragoons()) {
                if (DEBUG) reason = "Too much risk, withdraw";
                return true;
            }
        }

        if (A.seconds() >= 450 && (strength <= 150 && goons <= 4)) {
            if (DEBUG) reason = "Wait for more Dragoons";
            return true;
        }

        if (
            strength <= 135
                && Count.dragoons() + 2 <= EnemyUnits.dragoons()
                && EnemyUnits.discovered().dragoons().atLeast(1)
        ) {
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
