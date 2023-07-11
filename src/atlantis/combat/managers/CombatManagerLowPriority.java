package atlantis.combat.managers;

import atlantis.combat.missions.Mission;
import atlantis.units.AUnit;
import atlantis.architecture.Manager;
import atlantis.util.log.ErrorLog;

public class CombatManagerLowPriority extends Manager {

    public CombatManagerLowPriority(AUnit unit) {
        super(unit);
    }

    /**
     * If we're here, mission manager is allowed to take control over this unit.
     * Meaning no action was needed on *tactical* level - stick to *strategic* level.
     */
    public Manager handle() {
        Mission mission = unit.mission();
        if (mission == null) {
            return null;
        }

//            if (unit.debug())System.out.println("F " + unit);

        unit.setTooltipTactical(mission.name());
        if (mission.handle(unit) != null) {
            return unit.manager();
        }

        ErrorLog.printMaxOncePerMinute("No combat unit manager for " + unit);
        return null;
    }
}
