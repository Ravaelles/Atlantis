package atlantis.combat.managers;

import atlantis.architecture.Manager;
import atlantis.combat.missions.Mission;
import atlantis.units.AUnit;

public class CombatManagerLowPriority extends Manager {
    public CombatManagerLowPriority(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isCombatUnit();
    }

    /**
     * If we're here, mission manager is allowed to take control over this unit.
     * Meaning no action was needed on *tactical* level - stick to *strategic* level,
     * which is controlled by mission managers (MissionDefend / Attack / Contain etc.).
     */
    protected Manager handle() {
        Mission mission = unit.mission();
        if (mission == null) {
            return null;
        }

        unit.setTooltipTactical(mission.name());
        return mission.handleManagerClass(unit);
    }
}
