package atlantis.combat.micro.zerg.overlord;

import atlantis.combat.squad.alpha.Alpha;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.managers.Manager;

public class FollowArmy extends Manager {

    public FollowArmy(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.id() % 3 == 0;
    }

    public Manager handle() {
        if (applies()) {
            APosition medianUnitPosition = Alpha.get().center();
            if (medianUnitPosition != null) {
                if (
                    unit.distTo(medianUnitPosition) > 2.5
                        && unit.move(medianUnitPosition, Actions.MOVE_FOLLOW, "Follow army", true)
                ) {
                    return usingManager(this);
                }
            }

            return null;
        }

        return null;
    }
}
