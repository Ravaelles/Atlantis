package atlantis.combat.missions.attack;

import atlantis.combat.missions.MissionDecisions;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ArmyStrength;
import atlantis.production.dynamic.terran.tech.SiegeMode;
import atlantis.units.select.Count;

public class TerranMissionChangerWhenAttack extends MissionChangerWhenAttack {
    @Override
    public boolean shouldChangeMissionToContain() {
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

//        if (
//            EnemyInfo.hiddenUnitsCount() >= 2
//                && Count.ofType(AUnitType.Terran_Science_Vessel) == 0
//        ) {
//            if (DEBUG) reason = "Hidden unitz";
//            return true;
//        }

        return false;
    }

    private boolean enemyHasDefensiveBuildingsAndWeDontHaveEnoughTanks() {
        if (
            (A.supplyUsed() <= 120 && AGame.killsLossesResourceBalance() <= 900)
                && EnemyUnits.discovered().combatBuildingsAntiLand().notEmpty()
        ) {
            if (Count.tanks() <= 3 || !SiegeMode.isResearched()) return true;
        }

        return false;
    }
}
