package atlantis.combat.micro.terran.tank;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class DontThinkAboutUnsieging extends Manager {

    public DontThinkAboutUnsieging(AUnit unit) {
        super(unit);
    }

    public Manager handle() {
        if (
            unit.lastActionLessThanAgo(30 * (3 + unit.id() % 4), Actions.SIEGE)
            || unit.lastAttackFrameLessThanAgo(30 * (unit.id() % 4))
        ) {
            return usedManager(this);
        }

        return null;
    }
}