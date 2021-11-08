package atlantis.combat.missions;

import atlantis.combat.micro.managers.AdvanceUnitsManager;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

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

//        if (ASquadCohesionManager.handle(unit)) {
//            return true;
//        }

        return handleAdvance(unit);
    }

    private boolean handleAdvance(AUnit unit) {
        APosition focusPoint = focusPoint();

//        if (ASquadCohesionManager.handle(unit)) {
//            return true;
//        }

        // Focus point is well known
        if (focusPoint != null) {
            unit.setTooltip("#MA:Advance");
            return AdvanceUnitsManager.attackMoveToFocusPoint(unit, focusPoint);
        }

        // Invalid focus point, no enemy can be found, roam around map
        if (!unit.isMoving() && !unit.isAttackingOrMovingToAttack()) {
            return handleWeDontKnowWhereTheEnemyIs(unit);
        }

        unit.setTooltip("#MA-NoFocus");
        return false;
    }

    @Override
    public APosition focusPoint() {
        return focusPointManager.focusPoint();
    }

    @Override
    public boolean allowsToAttackDefensiveBuildings(AUnit unit, AUnit defensiveBuilding) {

        // Tanks always allowed
        if (unit.isTank()) {
            return true;
        }

        // Air units
        if (unit.isAirUnit() && defensiveBuilding.isSunken()) {
            return true;
        }

        // Standard infantry attack
        if (Count.ourCombatUnits() <= 40 || unit.lastStoppedRunningLessThanAgo(30 * 10)) {
            return false;
        }

        int buildings = Select.enemy().combatBuildings().inRadius(7, defensiveBuilding).count();

        return Select.ourRealUnits()
                .inRadius(6, unit)
                .excludeTypes(AUnitType.Terran_Medic)
                .atLeast(9 * buildings);
    }
}