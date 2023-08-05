package atlantis.combat.squad.positioning;

import atlantis.architecture.Manager;
import atlantis.combat.missions.MissionManager;
import atlantis.combat.squad.positioning.terran.TerranEnsureBall;
import atlantis.units.AUnit;

public class ComeCloser extends MissionManager {
    public ComeCloser(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isGroundUnit() && focusPoint != null && (
            unit.friendsInRadius(1).groundUnits().atMost(1)
                && unit.friendsInRadius(2).groundUnits().atMost(5)
        )) {
            if (unit.isVulture()) {
                return false;
            }

            return true;
        }

        return false;
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            TooFarFromLeader.class,
            TooFarFromSquadCenter.class,
        };
    }
}
