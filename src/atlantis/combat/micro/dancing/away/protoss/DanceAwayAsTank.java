package atlantis.combat.micro.dancing.away.protoss;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class DanceAwayAsTank extends Manager {
    private AUnit target;

    public DanceAwayAsTank(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isTankUnsieged()) return false;

        int cooldown = unit.cooldownRemaining();
        if (cooldown <= 4) return false;

        target = unit.target();
        if (target == null || !target.hasPosition()) return false;

        double distTo = unit.distTo(target);
        return distTo <= 6.75;
    }

    @Override
    public Manager handle() {
        String logString = "DanceAwayTank-" + unit.cooldownRemaining();
        unit.addLog(logString);

        if (danceAwayFromTarget(logString)) {
//            System.err.println("@ " + A.now() + " - " + unit.id() + " - __dance_@@@@_to___ " + target);
            return usedManager(this);
        }

        return null;
    }

    private boolean danceAwayFromTarget(String logString) {
        return unit.moveAwayFrom(target, 0.5, Actions.MOVE_DANCE_AWAY, logString);
    }
}
