package atlantis.combat.missions.attack;

import atlantis.combat.missions.MissionManager;
import atlantis.combat.missions.WeDontKnowWhereEnemyIs;
import atlantis.combat.missions.focus.AFocusPoint;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.architecture.Manager;

public class MoveToFocusPoint extends MissionManager {

    public MoveToFocusPoint(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            MoveToFocusPointAsTerran.class,
            AdvanceAsLeader.class,
            WeDontKnowWhereEnemyIs.class,
        };
    }
}
