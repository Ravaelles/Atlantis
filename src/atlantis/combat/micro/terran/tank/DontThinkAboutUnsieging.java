package atlantis.combat.micro.terran.tank;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class DontThinkAboutUnsieging extends Manager {
    public DontThinkAboutUnsieging(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isTankSieged() && unit.hp() >= 60;
    }

    public Manager handle() {
        if (
            unit.hasCooldown()
                || unit.lastActionLessThanAgo(30 * (5 + unit.id() % 4), Actions.SIEGE)
                || unit.lastAttackFrameLessThanAgo(30 * (2 + unit.id() % 4))
        ) {
            return usedManager(this);
        }

        return null;
    }
}