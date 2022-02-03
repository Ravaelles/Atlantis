package atlantis.combat.missions.attack;

import atlantis.combat.missions.Missions;
import atlantis.combat.missions.contain.MissionChangerWhenContain;
import atlantis.game.A;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.OurStrategy;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

public class TerranMissionChangerWhenAttack extends MissionChangerWhenContain {

    public static void changeMissionIfNeeded() {
        if (shouldChangeMissionToContain()) {
            changeMissionTo(Missions.CONTAIN);
        }
        else if (shouldChangeMissionToDefend()) {
            changeMissionTo(Missions.DEFEND);
        }
    }

    // === CONTAIN =============================================

    public static boolean shouldChangeMissionToContain() {
        if (OurStrategy.get().goingBio()) {
            if (DEBUG) debugReason = "We aren't stronger (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
            return !ArmyStrength.weAreStronger();
        }

        return false;
    }

    public static boolean shouldChangeMissionToDefend() {
        if (Have.base() && Select.enemyCombatUnits().inRadius(15, Select.main()).atLeast(5)) {
            if (DEBUG) debugReason = "Protect base";
            return true;
        }

        return false;
    }

}
