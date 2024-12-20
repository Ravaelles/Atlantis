package atlantis.combat.micro.zerg.overlord;

import atlantis.architecture.Manager;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class FollowArmy extends Manager {

    public FollowArmy(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.id() % 3 == 0;
    }

    protected Manager handle() {
        HasPosition medianUnitPosition = Alpha.get().center();
        if (medianUnitPosition != null) {
            if (
                unit.distTo(medianUnitPosition) > 2.5
                    && unit.move(medianUnitPosition, Actions.MOVE_FOLLOW, "Follow army", true)
            ) {
                return usedManager(this);
            }
        }

        return null;
    }
}
