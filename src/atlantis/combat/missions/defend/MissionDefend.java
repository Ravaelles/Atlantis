package atlantis.combat.missions.defend;

import atlantis.architecture.Manager;
import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.missions.Mission;
import atlantis.combat.missions.defend.focus.MissionDefendFocus;
import atlantis.units.AUnit;

public class MissionDefend extends Mission {
    protected AUnit unit;
    protected AUnit main;
    protected AFocusPoint focusPoint;

//    protected double focusPointToBase;
//    protected double unitToEnemy;
//    protected double unitToFocus;
//    protected double unitToBase;
//    protected double enemyToBase;
//    protected double enemyToFocus;

    public MissionDefend() {
        super("Defend");
        focusPointManager = new MissionDefendFocus();
    }

    // =========================================================
    @Override
    protected Manager managerClass(AUnit unit) {
        return new MissionDefendManager(unit);
    }

    @Override
    public double optimalDist() {
        return -1;
    }

    @Override
    public boolean allowsToAttackEnemyUnit(AUnit unit, AUnit enemy) {
        return (new MissionDefendAllowsToAttack(unit)).allowsToAttackEnemyUnit(enemy);
    }
}
