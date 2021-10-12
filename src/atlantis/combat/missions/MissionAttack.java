package atlantis.combat.missions;

import atlantis.combat.micro.managers.AdvanceUnitsManager;
import atlantis.combat.squad.AStickCloserOrSpreadOutManager;
import atlantis.position.APosition;
import atlantis.units.AUnit;

/**
 * This is the mission object that is used by battle squads and it indicates that we should attack 
 * the enemy at the <b>focusPoint</b>.
 */
public class MissionAttack extends Mission {

    protected MissionAttack() {
        super("Attack");
        focusPointManager = new MissionAttackFocusPointManager();
    }

    @Override
    public boolean update(AUnit unit) {
        unit.setTooltip("#Attack");

        // =========================================================

        if (AStickCloserOrSpreadOutManager.handle(unit)) {
            return true;
        }

        if (handleAdvance(unit)) {
            return true;
        }

        // =========================================================

        return false;
    }

    private boolean handleAdvance(AUnit unit) {
        APosition focusPoint = focusPoint();

        // Focus point is well known
        if (focusPoint != null) {
            return AdvanceUnitsManager.moveToFocusPoint(unit, focusPoint);
        }

        // Invalid focus point, no enemy can be found, roam around map
        else if (!unit.isMoving() && !unit.isAttacking()) {
            return handleWeDontKnowWhereTheEnemyIs(unit);
        }

        return false;
    }

    @Override
    public APosition focusPoint() {
        return focusPointManager.focusPoint();
    }

}