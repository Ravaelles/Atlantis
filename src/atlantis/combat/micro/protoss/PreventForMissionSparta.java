package atlantis.combat.micro.protoss;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class PreventForMissionSparta extends Manager {

    public PreventForMissionSparta(AUnit unit) {
        super(unit);
    }

    public Manager handle() {
        if (unit.isMissionSparta()) {
            return usedManager(this);
        }

        return null;
    }
}
