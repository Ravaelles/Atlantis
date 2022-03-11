package atlantis.combat.missions.attack;

import atlantis.combat.missions.MissionChanger;
import atlantis.combat.missions.Missions;
import atlantis.combat.missions.contain.MissionChangerWhenContain;
import atlantis.information.generic.ArmyStrength;

public class TerranMissionChangerWhenAttack extends MissionChangerWhenContain {

    public static void changeMissionIfNeeded() {
        if (shouldChangeMissionToContain()) {
            changeMissionTo(Missions.CONTAIN);
        }
        else if (shouldChangeMissionToDefend()) {
            changeMissionTo(MissionChanger.defend());
        }
    }

    // === CONTAIN =============================================

    public static boolean shouldChangeMissionToContain() {
//        if (OurStrategy.get().goingBio()) {
        if (!ArmyStrength.weAreStronger()) {
            if (DEBUG) debugReason = "We aren't stronger (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
            return true;
        }

        return false;
    }

    public static boolean shouldChangeMissionToDefend() {
        if (baseUnderSeriousAttack()) {
            if (DEBUG) debugReason = "Protect base";
            return true;
        }

        return false;
    }

}
