package atlantis.combat.squad.positioning.terran;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.combat.missions.MissionManager;
import atlantis.units.AUnit;
import atlantis.util.We;

public class TerranComeCloser extends MissionManager {
    public TerranComeCloser(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!We.terran()) return false;
        if (unit.isDT()) return false;

        if (unit.isGroundUnit() && focus != null && (
            unit.friendsInRadius(1).groundUnits().atMost(1)
                && unit.friendsInRadius(2).groundUnits().atMost(5)
        )) {
            if (unit.isVulture()) return false;

            return true;
        }

        return false;
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            TerranTooFarFromLeader::new,
            TerranTooFarFromSquadCenter::new,
        };
    }
}
