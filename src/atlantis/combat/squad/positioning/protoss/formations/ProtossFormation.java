package atlantis.combat.squad.positioning.protoss.formations;

import atlantis.architecture.Manager;
import atlantis.combat.squad.positioning.protoss.cluster.ProtossKeepUnitsClustered;
import atlantis.combat.squad.positioning.protoss.dragoon.ProtossKeepUnitsCloseToBuildingsDuringDefend;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.We;

public class ProtossFormation extends Manager {
    public ProtossFormation(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.protoss()
            && unit.isCombatUnit()
            && unit.isAlphaSquad()
            && !unit.isRetreating()
            && !unit.leaderIsRetreating()
            && unit.lastActionMoreThanAgo(30 * 10, Actions.UNLOAD);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossMoonIdle.class,
            ProtossMoon.class,
//            ProtossKeepUnitsClustered.class,
//            ProtossKeepUnitsCloseToBuildingsDuringDefend.class,
        };
    }
}
