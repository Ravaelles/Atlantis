package atlantis.combat.squad.positioning;

import atlantis.architecture.Manager;
import atlantis.combat.missions.Missions;
import atlantis.game.A;
import atlantis.information.generic.ArmyStrength;
import atlantis.units.AUnit;

public class DoNotThinkOfImprovingCohesion extends Manager {

    public DoNotThinkOfImprovingCohesion(AUnit unit) {
        super(unit);
    }

    public Manager handle() {
        if (check()) {
            return usedManager(this);
        }

        return null;
    }

    private boolean check() {
        return (A.supplyUsed() >= 150 && ArmyStrength.ourArmyRelativeStrength() >= 50)
            || unit.friendsInRadius(4).groundUnits().count() >= 12
            || Missions.isGlobalMissionDefendOrSparta();
    }
}

