package atlantis.combat.missions.attack;

import atlantis.combat.micro.AAttackEnemyUnit;
import atlantis.combat.micro.managers.AdvanceUnitsManager;
import atlantis.combat.missions.focus.AFocusPoint;
import atlantis.combat.missions.Mission;
import atlantis.combat.missions.ProtossMissionAdjustments;
import atlantis.combat.squad.ASquadCohesionManager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
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

    // =========================================================

    @Override
    public boolean update(AUnit unit) {
        unit.setTooltipTactical("#MA");

        return handleAdvance(unit);
    }

    @Override
    public double optimalDist(AUnit unit) {
        return -1;
    }

    // =========================================================

    public boolean allowsToAttackEnemyUnit(AUnit unit, AUnit enemy) {
        if (A.supplyUsed() <= 40) {
            // Zealots vs Zealot fix
            if (ProtossMissionAdjustments.allowsToAttackEnemyUnits(unit, enemy)) {
                return true;
            }
        }

        return true;
    }

    private boolean handleAdvance(AUnit unit) {
        AFocusPoint focusPoint = focusPoint();

        // Invalid focus point, no enemy can be found, roam around map
        if (focusPoint == null && (!unit.isAttackingOrMovingToAttack() || unit.isIdle())) {
            return handleWeDontKnowWhereTheEnemyIs(unit);
        }

        if (ASquadCohesionManager.handle(unit)) {
            return true;
        }

        // Focus point is well known
        if (focusPoint != null && unit.lastPositioningActionMoreThanAgo(10)) {
            unit.setTooltipTactical("#MA:Advance" + AAttackEnemyUnit.canAttackEnemiesNowString(unit));
            return AdvanceUnitsManager.attackMoveToFocusPoint(unit, focusPoint);
        }

        unit.setTooltipTactical("#MA-NoFocus");
        return handleWeDontKnowWhereTheEnemyIs(unit);
    }

    @Override
    public boolean allowsToAttackCombatBuildings(AUnit unit, AUnit combatBuilding) {

        // Tanks always allowed
        if (unit.isTank() && unit.distToMoreThan(combatBuilding, 7.9)) {
            return true;
        }

        // Air units
        if (unit.isAir() && combatBuilding.isSunken()) {
            return true;
        }

        if (unit.friendsNearCount() <= 6) {
            return false;
        }

        // Standard infantry attack
//        boolean notStrongEnough = Select.ourCombatUnits().inRadius(6, unit).atMost(8);
//        if (notStrongEnough || unit.lastStoppedRunningLessThanAgo(30 * 10)) {
//        if (unit.lastStoppedRunningLessThanAgo(30 * 10)) {
//            return false;
//        }

        int buildings = Select.enemy().combatBuildings(false).inRadius(7, combatBuilding).count();

        return Select.ourRealUnits()
                .inRadius(6, unit)
                .excludeTypes(AUnitType.Terran_Medic)
                .atLeast(9 * buildings);
    }
}