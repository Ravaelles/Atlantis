package atlantis.combat.squad.positioning;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class AllowTimeToReposition extends Manager {
    public AllowTimeToReposition(AUnit unit) {
        super(unit);
    }

    public Manager handle() {
        if (unit.lastPositioningActionLessThanAgo(13)) {
            return usedManager(this);
        }

        return null;
    }
}
