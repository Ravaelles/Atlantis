package atlantis.combat.squad.positioning;

import atlantis.architecture.Manager;
import atlantis.combat.missions.Missions;
import atlantis.game.A;
import atlantis.information.generic.ArmyStrength;
import atlantis.units.AUnit;

public class DoNotThinkOfImprovingCohesion {
    public static boolean dontThink(AUnit unit) {
        return (A.supplyUsed() >= 185 && ArmyStrength.ourArmyRelativeStrength() >= 250)
            || unit.enemiesNear().inRadius(6.2, unit).notEmpty()
            || unit.friendsInRadius(4).groundUnits().count() >= 13
            || Missions.isGlobalMissionDefendOrSparta();
    }
}

