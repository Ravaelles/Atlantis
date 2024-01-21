package atlantis.map.scout;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;

import static atlantis.map.scout.ScoutState.nextPositionToUnit;
import static atlantis.map.scout.ScoutState.scoutingAroundBaseWasInterrupted;

public class ScoutRoaming extends Manager {
    public ScoutRoaming(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isRunning();
    }

    @Override
    public Manager handle() {
        nextPositionToUnit = null;
        scoutingAroundBaseWasInterrupted = true;

        if (A.seconds() >= 300) {
            return (new ScoutFreeBases(unit)).invoke(this);
        }

        return null;
    }
}
