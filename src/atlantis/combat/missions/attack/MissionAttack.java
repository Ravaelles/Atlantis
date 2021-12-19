package atlantis.combat.missions.attack;

import atlantis.combat.micro.managers.AdvanceUnitsManager;
import atlantis.combat.missions.AFocusPoint;
import atlantis.combat.missions.Mission;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

/**
 * This is the mission object that is used by battle squads and it indicates that we should attack 
 * the enemy at the <b>focusPoint</b>.
 */
public class MissionAttack extends Mission {

    public MissionAttack() {
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
        AFocusPoint focusPoint = focusPoint();

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
    public boolean allowsToAttackCombatBuildings(AUnit unit, AUnit combatBuilding) {

        // Tanks always allowed
        if (unit.isTank() && unit.distToMoreThan(combatBuilding, 7.6)) {
            return true;
        }

        // Air units
        if (unit.isAir() && combatBuilding.isSunken()) {
            return true;
        }

        // Standard infantry attack
//        boolean notStrongEnough = Select.ourCombatUnits().inRadius(6, unit).atMost(8);
//        if (notStrongEnough || unit.lastStoppedRunningLessThanAgo(30 * 10)) {
        if (unit.lastStoppedRunningLessThanAgo(30 * 10)) {
            return false;
        }

        int buildings = Select.enemy().combatBuildings(false).inRadius(7, combatBuilding).count();

        return Select.ourRealUnits()
                .inRadius(6, unit)
                .excludeTypes(AUnitType.Terran_Medic)
                .atLeast(9 * buildings);
    }
}