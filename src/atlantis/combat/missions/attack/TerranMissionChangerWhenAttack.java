package atlantis.combat.missions.attack;

import atlantis.Atlantis;
import atlantis.combat.missions.MissionDecisions;
import atlantis.combat.missions.Missions;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.A;
import atlantis.information.decisions.terran.TerranDecisions;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.generic.Army;
import atlantis.information.strategy.OurStrategy;
import atlantis.production.dynamic.terran.tech.ResearchStimpacks;
import atlantis.production.dynamic.terran.tech.ResearchU238;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.game.player.Enemy;

public class TerranMissionChangerWhenAttack extends MissionChangerWhenAttack {
    @Override
    public boolean shouldChangeMissionToContain() {
        if (true) return false;

        if (A.supplyUsed() >= 174) return false;

//        if (OurStrategy.get().goingBio()) {
        if (!ArmyStrength.weAreMuchStronger()) {
            if (DEBUG) reason = "We aren't stronger (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
            return true;
        }

        return false;
    }

    @Override
    public boolean shouldChangeMissionToDefend() {
        if (A.isUms()) return false;

        if (Enemy.protoss()) {
            if (defendVsProtoss()) return true;
        }

        if (MissionDecisions.baseUnderSeriousAttack()) {
            if (DEBUG) reason = "Protect base";
            return true;
        }

        if (enemyHasHiddenUnitsAndWeDontHaveEnoughDetection()) {
            if (DEBUG) reason = "Not enough detection";
            return true;
        }

        if (
            OurStrategy.get().isRushOrCheese()
                && ArmyStrength.ourArmyRelativeStrength() >= 95
                && A.seconds() <= 400
                && (!OurStrategy.get().goingBio() || Count.medics() >= 2)
        ) {
            if (DEBUG) reason = "Rush or cheese and strength still ok";
            return false;
        }

        if (armyStrengthTooWeak()) {
            if (DEBUG) reason = "Army too weak (" + Army.strengthWithoutCB() + "%)";
            return true;
        }

//        if (A.minerals() >= 2000) {
//            return false;
//        }

        if (EnemyInfo.hasMutas() && enemyHasDefensiveBuildingsAndWeArentStrongEnough()) {
            if (DEBUG) reason = "Not enough tanks to break defences";
            return true;
        }

//        if (Select.our().air().atMost(1)) {
//            if (DEBUG) reason = "Not enough AIR support";
//            return true;
//        }

        if (notEnoughTanksAndNotEarlyGame()) {
            if (DEBUG) reason = "Not enough tanks to attack safely";
            return true;
        }

        if (
            Enemy.protoss()
                && EnemyInfo.hasHiddenUnits()
                && !Have.scienceVessel()
                && Select.ourOfType(AUnitType.Terran_Comsat_Station).havingEnergy(75).empty()
                && ArmyStrength.ourArmyRelativeStrength() <= 400
        ) {
            if (DEBUG) reason = "No Science Vessel";
            return true;
        }

//        if (
//            EnemyInfo.hiddenUnitsCount() >= 2
//                && Count.ofType(AUnitType.Terran_Science_Vessel) == 0
//        ) {
//            if (DEBUG) reason = "Hidden unitz";
//            return true;
//        }

        return false;
    }

    private boolean defendVsProtoss() {
        if (
            A.seconds() <= 400
                && EnemyUnits.discovered().combatUnits().atLeast(4)
                && Alpha.count() <= 10
        ) {
            if (DEBUG) reason = "Discard early push";
            return true;
        }

        if (waitForTechOrTanksBeforeEngagingGoons()) {
            if (DEBUG) reason = "Wait for tech or tanks before engaging goons";
            return true;
        }

        if (Missions.historyCount() >= 1 && Count.ourCombatUnits() <= 30) {
            if (DEBUG) reason = "Not enough terran units";
            return true;
        }

        return false;
    }

    private static boolean waitForTechOrTanksBeforeEngagingGoons() {
        return !ResearchStimpacks.isResearched()
            && !ResearchU238.isResearched()
            && EnemyUnits.dragoons() >= 2
            && Count.tanks() <= 5;
    }

    private boolean armyStrengthTooWeak() {
        int ourArmyRelativeStrength = ArmyStrength.ourArmyRelativeStrength();

        if (ourArmyRelativeStrength <= 90) return true;

        return ourArmyRelativeStrength <= 115 && A.seconds() % 7 == 0;
    }

    private boolean notEnoughTanksAndNotEarlyGame() {
        return !Enemy.terran()
            && TerranDecisions.DONT_PRODUCE_TANKS_AT_ALL.isTrue()
            && A.seconds() >= 500
            && Atlantis.LOST >= 10
            && Count.tanks() <= 4
            && ArmyStrength.ourArmyRelativeStrength() <= 300
            && !A.hasMinerals(700);
    }

    private boolean enemyHasHiddenUnitsAndWeDontHaveEnoughDetection() {
        if (Count.ofType(AUnitType.Terran_Science_Vessel) > 0) return false;

        if (EnemyUnits.discovered().effUndetected().size() >= 2) {
            if (Select.ourOfType(AUnitType.Terran_Comsat_Station).havingEnergy(130).empty()) return true;
        }

        return false;
    }

    private boolean enemyHasDefensiveBuildingsAndWeArentStrongEnough() {
        int ourCombatUnits = Count.ourCombatUnits();

        if (
            A.supplyUsed() <= 70
                && (
                EnemyInfo.combatBuildingsAntiLand() >= 12 * ourCombatUnits
                    || EnemyInfo.combatBuildingsAntiLand() >= 6 * Count.marines()
            )
        ) {
            return true;
//            if (Count.tanks() <= 3 || !SiegeMode.isResearched()) return true;
        }

        return false;
    }
}
