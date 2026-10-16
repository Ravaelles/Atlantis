package atlantis.combat.missions.attack;

import atlantis.combat.missions.MissionChanger;
import atlantis.combat.missions.Missions;
import atlantis.combat.missions.defend.protoss.ProtossForceMissionDefend;
import atlantis.combat.missions.defend.protoss.ProtossShouldForceMissionAttack;
import atlantis.combat.squad.squads.alpha.Alpha;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnitBreachedBase;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.enemy.OurBuildingUnderAttack;
import atlantis.information.generic.Army;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.GamePhase;
import atlantis.information.strategy.Strategy;
import atlantis.map.base.BaseLocations;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.game.player.Enemy;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class ProtossMissionChangerWhenAttack extends MissionChangerWhenAttack {

    // === DEFEND ==============================================
    public boolean shouldChangeMissionToDefend() {
        if (ProtossForceMissionDefend.check(Army.strengthWithoutCB(), Count.ourCombatUnits())) {
            return forceMissionSpartaOrDefend(reason);
        }

        // =========================================================

        if (MissionChanger.lastMissionWasLessThanSecondsAgo(Missions.ATTACK, 4)) {
            return false;
        }

        // =========================================================

        if (ProtossShouldForceMissionAttack.shouldForce()) return false;

        if (ourBuildingUnderAttack() != null) {
            return forceMissionSpartaOrDefend(reason = "Enemy is near our building");
        }

//        if (Army.strength() <= 700) {
//            if (DEBUG) reason = "Temp";
//            return forceMissionSpartaOrDefend(reason);
//        }

        if (A.s >= 60 * 10 && (A.supplyUsed() >= 180 || A.minerals() >= 2000)) {
            if (DEBUG) reason = "LateGame-attack";
            return false;
        }

        if (A.minerals() >= 2000 && Count.ourCombatUnits() >= 20) {
            if (DEBUG) reason = "2K_minerals";
            return false;
        }

        if (hugeZerglingsArmy()) {
            if (DEBUG) reason = "HugeZerglings";
            return forceMissionSpartaOrDefend(reason);
        }

        if (A.s >= 60 * 10 && Army.strengthWithoutCB() <= 150 && Count.ourCombatUnits() <= 12) {
            if (DEBUG) reason = "TooFewForLateGame(" + Count.ourCombatUnits() + ")";
            return true;
        }

        if (baseUnderAttack()) {
            if (DEBUG) reason = "Base_under_attack";
            return forceMissionSpartaOrDefend(reason);
        }

        if (earlyGameWeakVsProtoss()) {
            if (DEBUG) reason = "EarlyGameWeakVsProtoss(OG:" + Count.dragoons() + ",EG:" + EnemyUnits.dragoons() + ")";
            return forceMissionSpartaOrDefend(reason);
        }

        if (forceDefendVsProtossHavingDragoons()) {
            if (DEBUG) reason = "EnemyHasGoonz";
            return forceMissionSpartaOrDefend(reason);
        }

        if (earlyExpansionVsProtoss()) {
            if (DEBUG) reason = "InvestedInExpansion";
            return true;
        }

        if (Enemy.protoss() && !EnemyInfo.hasRanged()) {
            return false;
        }

        if (!Enemy.terran() && alphaOutmatched()) {
            if (DEBUG) reason = "AlphaOutmatched";
            return forceMissionSpartaOrDefend(reason);
        }

        if (Army.strengthWithoutCB() <= 150 && Count.ourCombatUnits() <= 25) {
            if (DEBUG) reason = "TooWeak(" + Army.strengthWithoutCB() + "%)";
            return forceMissionSpartaOrDefend(reason);
        }

        if (defendAgainstMutas()) {
            if (DEBUG) reason = "MutasSoDefend";
            return forceMissionSpartaOrDefend(reason);
        }

        if (zergArmyCloseToNatural()) {
            if (DEBUG) reason = "ZergCloseToNatural";
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
        if (Army.strengthWithoutCB() <= 125 && EnemyUnits.combatUnits() >= 2) {
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

    private boolean hugeZerglingsArmy() {
        if (!Enemy.zerg()) return false;

        int lings = EnemyUnits.lings();
        return Army.strengthWithoutCB() <= 180
            && lings >= 30
            && Count.ourCombatUnits() <= 25
            && lings >= 3 * Count.ourCombatUnits();
    }

    private boolean earlyExpansionVsProtoss() {
        if (!Enemy.protoss()) return false;
        if (A.s >= 60 * 10) return false;
        if (Count.ourUnfinishedOfType(AUnitType.Protoss_Nexus) == 0) return false;
        if (Army.strengthWithoutCB() >= 400 && Count.ourCombatUnits() >= 20) return false;

        if (Count.workers() * 15 <= Count.basesWithUnfinished()) {
            return true;
        }

        return false;
    }

    private boolean forceDefendVsProtossHavingDragoons() {
        if (!Enemy.protoss()) return false;
        if (Count.ourCombatUnits() >= 21) return false;
        if (Count.dragoons() >= 6) return false;

        return EnemyInfo.hasRanged() && Army.strengthWithoutCB() <= 300;
    }

    private boolean alphaOutmatched() {
        if (A.supplyUsed() >= 170) return false;

        double eval = Alpha.evalOr(-1);

        if (eval >= 0.85 && Army.strengthWithoutOurCB() >= 260 && A.resourcesBalance() >= -200) return false;

        AUnit leader = Alpha.alphaLeader();
        if (leader == null) return false;
        if (leader.groundDistToMain() <= 50) return false;
        if (leader.enemiesNear().combatUnits().atMost(3)) return false;
        if (leader.friendsNear().combatUnits().atLeast(10)) return false;

        if (eval <= 0.8) return true;
//        if (eval <= 0.7 && leader.friendsInRadiusCount(9) <= 5) return true;

        return false;
    }

    private boolean earlyGameWeakVsProtoss() {
        if (!Enemy.protoss()) return false;
        int ourCU = Count.ourCombatUnits();
        if (ourCU >= 14) return false;

        int enemyGoons = EnemyUnits.dragoons();
        if (enemyGoons == 0 && !EnemyInfo.hasRanged()) return false;

        int enemyCU = EnemyUnits.combatUnits();
        if (ourCU <= enemyCU && Army.strengthWithoutCB() <= 160) return true;

        int dragoons = Count.dragoons();
        if (dragoons == 0 && enemyGoons > 0 && Army.strengthWithoutCB() <= 700) return true;
        if (dragoons == 0 && Army.strengthWithoutCB() <= 180) return true;

        if (dragoons <= 7 && enemyGoons >= 3) return true;
        if (dragoons <= 4 && enemyGoons >= 2) return true;
        if (dragoons <= 4 && (double) (enemyGoons / (dragoons + 0.1)) >= 1.5) return true;

        return Army.strengthWithoutCB() <= 150;
    }

    private boolean zergArmyCloseToNatural() {
        if (!Enemy.zerg()) return false;

        APosition natural = BaseLocations.natural();
        if (natural == null) return false;

        Selection enemies = EnemyUnits.discovered().combatUnits().inRadius(20, natural);
        if (enemies.count() <= (A.supplyUsed() >= 65 ? 1 : 0)) return false;

        AUnit leader = Alpha.alphaLeader();
        if (leader == null) return false;

        return leader.distTo(natural) >= 30 && leader.distToMain() >= 40;
    }

    private boolean defendAgainstMutas() {
        if (Count.corsairs() >= 2) return false;

        int mutas = EnemyUnits.count(AUnitType.Zerg_Mutalisk);
        if (mutas == 0) return false;

        AUnit nearestMuta = Select.enemyCombatUnits().mutalisks().nearestTo(Select.mainOrAnyBuilding());
        if (nearestMuta != null && nearestMuta.enemiesNear().buildings().notEmpty()) return true;

        if (Count.cannons() >= 5) return false;

        return A.supplyUsed() <= 140;
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
            && building.woundHp() >= 25
            && building.lastUnderAttackLessThanAgo(150)
            && (A.supplyUsed() <= 140)
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
//            if (DEBUG) reason = "Goons (" + goons + ") and no goon range(" + Army.strengthWithoutCB() + "%)";
//            return forceMissionSpartaOrDefend(reason);
//        }

//        if (goons >= 1 && hydras >= 2 && Army.strengthWithoutCB() <= 155 && !ResearchSingularityCharge.isResearched()) {
//            if (DEBUG) reason = "Hydras and no goon range(" + Army.strengthWithoutCB() + "%)";
//            return forceMissionSpartaOrDefend(reason);
//        }

        if (Strategy.get().isRushOrCheese() && Army.strengthWithoutCB() >= 95) {
            if (DEBUG) reason = "Rush-or-cheese attack";
            return false;
        }

        if (!Strategy.get().isRushOrCheese() && goons <= 3 && Army.strengthWithoutCB() <= 120 && (
            combatUnits <= 7 || (combatUnits <= 8 && EnemyUnits.discovered().combatUnits().atLeast(11))
        )) {
            if (DEBUG) reason = "Wait for more army";
            return true;
        }

//        if (Army.strengthWithoutCB() <= 170 && !ResearchSingularityCharge.isResearched()) {
//            if (DEBUG) reason = "Wait for goon range(" + Army.strengthWithoutCB() + "%)";
//            return true;
//        }

        if ((goons <= 1 || (goons <= 3 && Army.strengthWithoutCB() <= 120)) && defendAgainstMassZerglings()) {
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
        int strength = Army.strengthWithoutCB();

        int goons = Count.dragoons();
        int enemyGoons = EnemyUnits.dragoons();

        if (goons < enemyGoons && Army.strengthWithoutCB() <= 115) {
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
            if (goons <= 2 && Army.strengthWithoutCB() <= 300) {
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
        if (Enemy.terran()) return false;
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
