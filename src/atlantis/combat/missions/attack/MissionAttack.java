package atlantis.combat.missions.attack;

import atlantis.combat.missions.Mission;
import atlantis.decions.Decision;
import atlantis.units.AUnit;

/**
 * This is the mission object that is used by battle squads and it indicates that we should attack
 * the enemy at the <b>focusPoint</b>.
 */
public class MissionAttack extends Mission {

    private final MissionAttackPermissionToAttack missionAttackPermissionToAttack = new MissionAttackPermissionToAttack();

    public MissionAttack(AUnit unit) {
        super("Attack");
        focusPointManager = new MissionAttackFocusPoint();
    }

    // =========================================================

    @Override
    public boolean update() {
        unit.setTooltipTactical("#MA");

        return MoveToAttackFocusPoint.move(unit, this);
    }

    @Override
    public double optimalDist() {
        return -1;
    }

    // =========================================================

    public Decision permissionToAttack() {
        return missionAttackPermissionToAttack.permissionToAttack();
    }

    @Override
    public boolean allowsToAttackEnemyUnit(AUnit enemy) {
        return MissionAttackVsEnemyUnit.allowsToAttackEnemyUnit(unit, enemy);
    }

    @Override
    public boolean allowsToAttackCombatBuildings(AUnit combatBuilding) {
        return MissionAttackVsCombatBuildings.allowsToAttackCombatBuildings(unit, combatBuilding);
    }
}
