package atlantis.combat.missions;

import atlantis.Atlantis;
import atlantis.combat.micro.managers.AttackManager;
import atlantis.combat.micro.managers.ContainUnitManager;
import atlantis.map.AMap;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import bwapi.Color;

/**
 * This is the mission object that is used by battle squads and it indicates that we should attack 
 * the enemy at the <b>focusPoint</b>.
 */
public class MissionAttack extends Mission {

    public MissionAttack(String name) {
        super(name, new MissionContainFocusPointManager(), new ContainUnitManager());
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
            return handleNoEnemyBuildingButNotOverYet(unit);
        }

        // =========================================================

        return false;
    }

    private boolean handleNoEnemyBuildingButNotOverYet(AUnit unit) {
        APosition position = AMap.getRandomInvisiblePosition(unit.getPosition());
        if (position != null) {
            unit.attackPosition(position);
            Atlantis.game().drawLineMap(unit.getPosition(), position, Color.Red); //TODO DEBUG
            unit.setTooltip("#MA:Forward!");
            return true;
        }
        else {
            System.err.println("No invisible position found");
            return false;
        }
    }

}