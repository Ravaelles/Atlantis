package atlantis.combat.missions.defend;

import atlantis.game.A;
import atlantis.information.generic.ArmyStrength;
import atlantis.units.select.Count;

public class TerranMissionChangerWhenDefendVsTerran {
    public static TerranMissionChangerWhenDefendVsTerran get() {
        return new TerranMissionChangerWhenDefendVsTerran();
    }

    public boolean shouldChangeMissionToAttack() {
        if (A.hasMinerals(800)) return true;

        int ourRelativeStrength = ArmyStrength.ourArmyRelativeStrength();

        if (ourRelativeStrength < 300) return false;

        if (Count.tanks() <= 4) return false;

        return true;
    }
}
