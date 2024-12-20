package atlantis.combat.missions.defend.terran;

import atlantis.game.A;
import atlantis.information.generic.ArmyStrength;
import atlantis.units.select.Count;

public class TerranMissionChangerWhenDefendVsTerran {
    public static TerranMissionChangerWhenDefendVsTerran get() {
        return new TerranMissionChangerWhenDefendVsTerran();
    }

    public boolean shouldChangeMissionToAttack() {
        if (A.hasMinerals(800)) {
            if (TerranMissionChangerWhenDefend.DEBUG) TerranMissionChangerWhenDefend.reason = "vT - lots of minerals";
            return true;
        }

        int ourRelativeStrength = ArmyStrength.ourArmyRelativeStrength();

        if (ourRelativeStrength < 300) return false;

        if (Count.tanks() <= 4) return false;

        if (TerranMissionChangerWhenDefend.DEBUG) TerranMissionChangerWhenDefend.reason = "vT - lets do this";
        return true;
    }
}
