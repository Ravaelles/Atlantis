package atlantis.combat.missions;

import atlantis.architecture.Manager;
import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.advance.leader.CurrentFocusChoke;
import atlantis.combat.missions.attack.FoundEnemyExposedExpansion;
import atlantis.map.choke.AChoke;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class MissionManager extends Manager {
    protected Mission mission;
    protected AFocusPoint focusPoint;

    public MissionManager(AUnit unit) {
        super(unit);
        mission = unit != null ? unit.mission() : null;
        focusPoint = defineFocusPoint();
    }

    private AFocusPoint defineFocusPoint() {
        if (sideQuestsAreAllowedForThisUnit() && mission.isMissionAttackOrContain()) {
            AFocusPoint focus = FoundEnemyExposedExpansion.getItFound();
            if (focus != null) return focus;
        }

        if (unit.isAlphaSquad()) {
            AChoke focusChoke = CurrentFocusChoke.get();
            if (focusChoke != null) {
                return new AFocusPoint(
                    focusChoke,
                    Select.mainOrAnyBuilding(),
                    "CurrentFocusChoke"
                );
            }
        }

        return mission != null ? mission.focusPoint() : null;
    }

    private boolean sideQuestsAreAllowedForThisUnit() {
        return unit.squad().allowsSideQuests();
    }
}
