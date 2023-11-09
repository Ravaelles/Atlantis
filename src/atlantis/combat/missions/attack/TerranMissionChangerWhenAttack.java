package atlantis.combat.missions.attack;

import atlantis.combat.missions.MissionDecisions;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ArmyStrength;
import atlantis.production.dynamic.terran.tech.SiegeMode;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

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
        if (MissionDecisions.baseUnderSeriousAttack()) {
            if (DEBUG) reason = "Protect base";
            return true;
        }

        if (enemyHasDefensiveBuildingsAndWeDontHaveEnoughTanks()) {
            if (DEBUG) reason = "Not enough tanks to break defences";
            return true;
        }

        if (enemyHasHiddenUnitsAndWeDontHaveEnoughDetection()) {
            if (DEBUG) reason = "Not enough detection";
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

    private boolean enemyHasHiddenUnitsAndWeDontHaveEnoughDetection() {
        if (Count.ofType(AUnitType.Terran_Science_Vessel) > 0) return false;

        if (EnemyUnits.discovered().effUndetected().size() >= 2) {
            if (Select.ourOfType(AUnitType.Terran_Comsat_Station).havingEnergy(150).empty()) return true;
        }

        return false;
    }

    private boolean enemyHasDefensiveBuildingsAndWeDontHaveEnoughTanks() {
        int ourCombatUnits = Count.ourCombatUnits();

        if (
            A.supplyUsed() <= 120
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
