package atlantis.combat.squad.positioning.protoss.keep_formation;

import atlantis.architecture.Manager;
import atlantis.combat.squad.positioning.protoss.ProtossTooFarFromSquadCenter;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.We;

public class ProtossKeepFormation extends Manager {
    public ProtossKeepFormation(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.protoss()
            && unit.lastActionMoreThanAgo(10, Actions.ATTACK_UNIT)
            && (!unit.isMoving() || unit.lastActionMoreThanAgo(10, Actions.MOVE_FORMATION));
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossForceClusterDragoon.class,
            ProtossForceClusterZealot.class,
            ProtossTooFarFromSquadCenter.class,
        };
    }
}
