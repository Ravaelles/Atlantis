package atlantis.combat.retreating.protoss;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.HasUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import bwapi.Color;

public class ProtossStartRetreat extends HasUnit {
    private AUnit enemy;

    public ProtossStartRetreat(AUnit unit) {
        super(unit);
    }

    public boolean startRetreatingFrom(AUnit enemy) {
        this.enemy = enemy;

        return handleRetreat();
    }

    protected boolean handleRetreat() {
//        if (ShouldRetreat.shouldRetreat(unit) && !FightInsteadAvoid.shouldFightCached()) {
//        if (ShouldRetreat.shouldRetreat(unit) && !FightInsteadAvoid.shouldFight()) {

//        Selection nearEnemies = unit.enemiesNear().canAttack(unit, true, true, 5);
//        HasPosition runAwayFrom = nearEnemies.center();
//
//        if (runAwayFrom == null) {
//            runAwayFrom = nearEnemies.first();
//        }

        HasPosition runAwayFrom = enemy;

        if (runAwayFrom == null) {
            A.errPrintln("Retreat runAwayFrom is NULL");
            return false;
        }

//        unit.addLog("@ " + A.now() + " - RETREAT");
//        System.err.println("@ " + A.now() + " - RETREAT " + unit.idWithType());

        if (retreatByRunningFromEnemy(runAwayFrom) || retreatByRunningTowardsBase()) {
            unitStartedRetreating(runAwayFrom);
            unit.paintLine(unit.runningManager().runTo(), Color.Orange);
            return true;
        }

        return false;
    }

    private boolean retreatByRunningTowardsBase() {
        AUnit main = Select.mainOrAnyBuilding();
        if (main == null || unit.distTo(main) <= 20) return false;

        return unit.moveToMain(Actions.RUN_RETREAT, "RetreatTowardsBase");
    }

    private boolean retreatByRunningFromEnemy(HasPosition runAwayFrom) {
        return unit.runningManager().runFrom(runAwayFrom, 5, Actions.RUN_RETREAT, true);
    }

    private boolean unitStartedRetreating(HasPosition runAwayFrom) {
        unit.addLog("RetreatedFrom" + runAwayFrom);
        return true;

//        AUnit leader = unit.squadLeader();
//
//        if (leader == null || leader.isRetreating() || leader.equals(unit)) return false;
//        if (unit.distTo(leader) >= 8) return false;
//
//        return true;
//        return (new RetreatManager(leader)).forceHandle() != null;
    }
}
