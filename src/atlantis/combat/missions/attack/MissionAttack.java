package atlantis.combat.missions.attack;

import atlantis.combat.micro.AAttackEnemyUnit;
import atlantis.combat.micro.managers.AdvanceUnitsManager;
import atlantis.combat.missions.WeDontKnowEnemyEnemyUnit;
import atlantis.combat.missions.focus.AFocusPoint;
import atlantis.combat.missions.Mission;
import atlantis.combat.missions.ProtossMissionAdjustments;
import atlantis.combat.squad.ASquadCohesionManager;
import atlantis.game.A;
import atlantis.units.AUnit;

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

        return MissionAttackAdvance.advance(unit, this);
    }

    @Override
    public double optimalDist(AUnit unit) {
        return -1;
    }

    // =========================================================

    @Override
    public boolean allowsToAttackEnemyUnit(AUnit unit, AUnit enemy) {
        return MissionAttackVsEnemyUnit.allowsToAttackEnemyUnit(unit, enemy);
    }

    @Override
    public boolean allowsToAttackCombatBuildings(AUnit unit, AUnit combatBuilding) {
        return MissionAttackVsCombatBuildings.allowsToAttackCombatBuildings(unit, combatBuilding);
    }
}
