package atlantis.combat.retreating.protoss;

import atlantis.game.A;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.HasUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
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
        if (enemy == null) return false;

//        A.printStackTrace("Start retreat " + unit);

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

        if (shouldForceRetreatDirectlyFromEnemy() && retreatByRunningFromEnemy(runAwayFrom)) {
            unitStartedRetreating(runAwayFrom);
            unit.paintLine(unit.runningManager().runTo(), Color.Orange);
            return true;
        }

        if (
            runTowardsBaseOrMainChoke()
                || retreatByRunningFromEnemy(runAwayFrom)
        ) {
            unitStartedRetreating(runAwayFrom);
            unit.paintLine(unit.runningManager().runTo(), Color.Orange);
            return true;
        }

        return false;
    }

    private boolean runTowardsBaseOrMainChoke() {
        if (unit.isDragoon()) return false;

        return retreatTowardsMainChoke()
            || retreatByRunningTowardsBase();
    }

    private boolean shouldForceRetreatDirectlyFromEnemy() {
        if (unit.woundHp() <= 20) return false;
        if (unit.meleeEnemiesNearCount(2.2) > 0) return true;
        if (unit.lastUnderAttackLessThanAgo(30)) return true;

        return false;
    }

    private boolean retreatTowardsMainChoke() {
        HasPosition goTo = mainChokeDefencePoint();
        if (goTo == null || unit.distTo(goTo) <= 3) return false;

        return unit.move(goTo, Actions.RUN_RETREAT, "RetreatToMainChoke");
    }

    private HasPosition mainChokeDefencePoint() {
        AChoke mainChoke = Chokes.mainChoke();
        if (mainChoke == null) return null;

        AUnit main = Select.main();
        if (main == null) return mainChoke;

        return mainChoke.translateTilesTowards(defencePointTowardsBase(), main);
    }

    private static double defencePointTowardsBase() {
        int combatUnits = Count.ourCombatUnits();
        if (combatUnits <= 4) return 2;
        if (combatUnits <= 9) return 2.5;
        return 3;
    }

    private boolean retreatByRunningTowardsBase() {
        AUnit goTo = Select.mainOrAnyBuilding();
        if (goTo == null || unit.distTo(goTo) <= 10) return false;

        return unit.moveToMain(Actions.RUN_RETREAT, "RetreatTowardsBase");
    }

    private boolean retreatByRunningFromEnemy(HasPosition runAwayFrom) {
        double dist = unit.friendsNear().inRadius(2, unit).atLeast(1) ? 2.5 : 3.5;

        return unit.runningManager().runFrom(runAwayFrom, dist, Actions.RUN_RETREAT, true);
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
