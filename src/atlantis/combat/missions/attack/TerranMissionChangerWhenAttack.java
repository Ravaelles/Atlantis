package atlantis.combat.missions.attack;

import atlantis.combat.missions.MissionChanger;
import atlantis.combat.missions.MissionDecisions;
import atlantis.combat.missions.Missions;
import atlantis.combat.missions.contain.MissionChangerWhenContain;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.generic.ArmyStrength;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;

public class TerranMissionChangerWhenAttack extends MissionChangerWhenAttack {
    @Override
    public boolean shouldChangeMissionToContain() {
        if (A.supplyUsed() >= 174) {
            return false;
        }

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

//        if (
//            EnemyInfo.hiddenUnitsCount() >= 2
//                && Count.ofType(AUnitType.Terran_Science_Vessel) == 0
//        ) {
//            if (DEBUG) reason = "Hidden unitz";
//            return true;
//        }

        return false;
    }
}
