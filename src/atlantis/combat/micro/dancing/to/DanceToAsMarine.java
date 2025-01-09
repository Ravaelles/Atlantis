package atlantis.combat.micro.dancing.to;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class DanceToAsMarine extends Manager {
    private AUnit target;

    public DanceToAsMarine(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isMarine()) return false;

        int cooldown = unit.cooldownRemaining();
        if (cooldown <= 4) return false;

        target = unit.target();
        if (target == null || !target.hasPosition()) return false;

        double distTo = unit.distTo(target);
        return distTo >= 3.66;
    }

    @Override
    public Manager handle() {
        String logString = "DanceToMarine-" + unit.cooldownRemaining();
        unit.addLog(logString);

        if (danceToTarget(logString)) {
//            System.err.println("@ " + A.now() + " - " + unit.id() + " - __dance_@@@@_to___ " + target);
            return usedManager(this);
        }

        return null;
    }

    private boolean danceToTarget(String logString) {
        APosition moveTo = unit.translateTilesTowards(0.25, target);
        if (moveTo == null || !moveTo.isWalkable()) return false;

        return unit.move(moveTo, Actions.MOVE_DANCE_TO, logString);
    }
}
