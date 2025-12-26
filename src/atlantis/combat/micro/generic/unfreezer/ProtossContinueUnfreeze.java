package atlantis.combat.micro.generic.unfreezer;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ProtossContinueUnfreeze extends Manager {
    public ProtossContinueUnfreeze(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isAction(Actions.UNFREEZE);
    }

    @Override
    public Manager handle() {
        if (unit.lastCommandIssuedAgo() >= 31) return null;

        if (
            unit.lastPositionChangedMoreThanAgo(50)
                && (unit.lastActionLessThanAgo(12, Actions.UNFREEZE) || unit.lastCommandIssuedAgo() <= 11)
        ) {
//            unit.paintCircleFilled(18, Color.Yellow);
//            System.err.println(A.now() + " - " + unit.typeWithUnitId() + " - ProtossContinueUnfreeze");
//            CenterCamera.on(unit);
            return usedManager(this);
        }

        return null;
    }
}
