package atlantis.combat.micro.terran.tank;

import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.managers.Manager;

public class DontThinkAboutUnsieging extends Manager {

    public DontThinkAboutUnsieging(AUnit unit) {
        super(unit);
    }

    public Manager handle() {
        if (
            unit.lastActionLessThanAgo(30 * (3 + unit.id() % 4), Actions.SIEGE)
            || unit.lastAttackFrameLessThanAgo(30 * (unit.id() % 4))
        ) {
            return usingManager(this);
        }

        return null;
    }
}