package atlantis.combat.micro.protoss;

import atlantis.units.AUnit;
import atlantis.architecture.Manager;

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
