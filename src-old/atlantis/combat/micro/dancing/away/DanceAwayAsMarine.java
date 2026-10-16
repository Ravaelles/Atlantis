package atlantis.combat.micro.dancing.away;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.range.OurMarineRange;

public class DanceAwayAsMarine extends Manager {
    private AUnit target;

    public DanceAwayAsMarine(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isMarine()) return false;

        int cooldown = unit.cooldownRemaining();
        if (cooldown <= 5) return false;

        target = unit.target();
        if (target == null || !target.hasPosition()) return false;

        double distTo = unit.distTo(target);
        return distTo <= OurMarineRange.range() - 0.4;
    }

    @Override
    public Manager handle() {
        String logString = "DanceAwayMarine-" + unit.cooldownRemaining();
        unit.addLog(logString);

        if (danceAwayFromTarget(logString)) {
//            System.err.println("@ " + A.now() + " - " + unit.id() + " - __dance_@@@@_to___ " + target);
            return usedManager(this);
        }

        return null;
    }

    private boolean danceAwayFromTarget(String logString) {
        return unit.moveAwayFrom(target, 0.3, Actions.MOVE_DANCE_AWAY, logString);
    }
}
