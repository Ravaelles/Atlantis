package atlantis.combat.missions.attack;

import atlantis.combat.missions.MissionChanger;
import atlantis.combat.missions.Missions;
import atlantis.combat.missions.contain.MissionChangerWhenContain;
import atlantis.game.A;
import atlantis.information.generic.ArmyStrength;

public class TerranMissionChangerWhenAttack extends MissionChangerWhenContain {

    public static void changeMissionIfNeeded() {
        if (shouldChangeMissionToContain()) {
            changeMissionTo(Missions.CONTAIN);
        }
        else if (shouldChangeMissionToDefend()) {
            changeMissionTo(MissionChanger.defendOrSpartaMission());
        }
    }

    // === CONTAIN =============================================

    public static boolean shouldChangeMissionToContain() {
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

    public static boolean shouldChangeMissionToDefend() {
        if (baseUnderSeriousAttack()) {
            if (DEBUG) reason = "Protect base";
            return true;
        }

        return false;
    }

}
