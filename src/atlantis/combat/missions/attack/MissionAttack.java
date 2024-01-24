package atlantis.combat.missions.attack;

import atlantis.architecture.Manager;
import atlantis.combat.missions.Mission;
import atlantis.combat.missions.attack.focus.MissionAttackFocusPoint;
import atlantis.decions.Decision;
import atlantis.units.AUnit;

/**
 * This is the mission object that is used by battle squads and it indicates that we should attack
 * the enemy at the <b>focusPoint</b>.
 */
public class MissionAttack extends Mission {
//    private final MissionAttackPermissionToAttack missionAttackPermissionToAttack = new MissionAttackPermissionToAttack();

    public MissionAttack() {
        super("Attack");
        focusPointManager = new MissionAttackFocusPoint();
    }

    // =========================================================

    @Override
    protected Manager managerClass(AUnit unit) {
        return new MissionAttackManager(unit);
    }

    @Override
    public double optimalDist() {
        return -1;
    }

    // =========================================================

    @Override
    public Decision permissionToAttack(AUnit unit) {
        return (new MissionAttackPermissionToAttack(unit)).permissionToAttack();
    }

    @Override
    public boolean allowsToAttackEnemyUnit(AUnit unit, AUnit enemy) {
        return (new MissionAttackAllowsToAttack(unit)).allowsToAttackEnemyUnit(enemy);
    }

//    @Override
//    public boolean allowsToAttackCombatBuildings(AUnit combatBuilding) {
//        return MissionAttackVsCombatBuildings.allowsToAttackCombatBuildings(unit, combatBuilding);
//    }
}
