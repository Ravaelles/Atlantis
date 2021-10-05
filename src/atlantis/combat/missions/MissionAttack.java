package atlantis.combat.missions;

import atlantis.combat.micro.managers.AttackManager;
import atlantis.position.APosition;
import atlantis.units.AUnit;

/**
 * This is the mission object that is used by battle squads and it indicates that we should attack 
 * the enemy at the <b>focusPoint</b>.
 */
public class MissionAttack extends Mission {

    public MissionAttack(String name) {
        super(name, new MissionAttackFocusPointManager(), new AttackManager());
    }

    @Override
    public boolean update(AUnit unit) {
        unit.setTooltip("#Attack");
        APosition focusPoint = focusPoint();

        // =========================================================

        // Focus point is well known
        if (focusPoint != null) {
            return AttackManager.attackFocusPoint(unit, focusPoint);
        }

        // Invalid focus point, no enemy can be found, roam around map
        else if (!unit.isAttacking()) {
            return handleNoEnemyBuilding(unit);
        }

        // =========================================================

        return false;
    }

}