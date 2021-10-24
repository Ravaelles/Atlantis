package atlantis.combat.missions;

import atlantis.combat.micro.managers.AdvanceUnitsManager;
import atlantis.combat.squad.ASquadCohesionManager;
import atlantis.position.APosition;
import atlantis.units.AUnit;

/**
 * This is the mission object that is used by battle squads and it indicates that we should attack 
 * the enemy at the <b>focusPoint</b>.
 */
public class MissionAttack extends Mission {

    protected MissionAttack() {
        super("Attack");
        focusPointManager = new MissionAttackFocusPoint();
    }

    @Override
    public boolean update(AUnit unit) {
        unit.setTooltip("#MA");

        // =========================================================

        if (ASquadCohesionManager.handle(unit)) {
            return true;
        }

        return handleAdvance(unit);

        // =========================================================
    }

    private boolean handleAdvance(AUnit unit) {
        APosition focusPoint = focusPoint();

        // Focus point is well known
        if (focusPoint != null) {
            return AdvanceUnitsManager.moveToFocusPoint(unit, focusPoint);
        }

        // Invalid focus point, no enemy can be found, roam around map
        else if (!unit.isMoving() && !unit.isAttackingOrMovingToAttack()) {
            return handleWeDontKnowWhereTheEnemyIs(unit);
        }

        return false;
    }

    @Override
    public APosition focusPoint() {
        return focusPointManager.focusPoint();
    }

}