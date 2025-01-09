package atlantis.combat.missions;

import atlantis.architecture.Manager;
import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.missions.attack.FoundEnemyExposedExpansion;
import atlantis.combat.squad.Squad;
import atlantis.units.AUnit;
import atlantis.util.log.ErrorLog;

public class MissionManager extends Manager {
    protected Mission mission;
    protected AFocusPoint focusPoint;

    public MissionManager(AUnit unit) {
        super(unit);
        if (unit != null) {
            mission = unit != null ? unit.mission() : null;
            focusPoint = defineFocusPoint();
        }
    }

    private AFocusPoint defineFocusPoint() {
        if (mission == null) {
            ErrorLog.printMaxOncePerMinute(
                "MissionManager.defineFocusPoint() - mission is null for " + unit
            );
            return null;
        }

        if (sideQuestsAreAllowedForThisUnit() && mission.isMissionAttackOrContain()) {
            AFocusPoint focus = FoundEnemyExposedExpansion.getItFound();
            if (focus != null) return focus;
        }

//        if (unit.isAlphaSquad()) {
//            AChoke focusChoke = CurrentFocusChoke.get();
//            if (focusChoke != null) {
//                return new AFocusPoint(
//                    focusChoke,
//                    Select.mainOrAnyBuilding(),
//                    "CurrentFocusChoke"
//                );
//            }
//        }

        return mission != null ? mission.focusPoint() : null;
    }

    private boolean sideQuestsAreAllowedForThisUnit() {
        Squad squad = unit.squad();
        if (squad == null) return false;

        return squad.allowsSideQuests();
    }
}
