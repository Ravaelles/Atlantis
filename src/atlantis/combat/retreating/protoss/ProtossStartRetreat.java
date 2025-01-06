package atlantis.combat.retreating.protoss;

import atlantis.game.A;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.HasUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

import static atlantis.units.actions.Actions.RUN_RETREAT;

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
//            unit.paintLine(unit.runningManager().runTo(), Color.Orange);
            return true;
        }

        if (retreatByRunningTowardsBase(unit)) {
            unitStartedRetreating(runAwayFrom);
            return true;
        }

        if (retreatTowardsLeaderForBetterCohesion()) {
            unitStartedRetreating(runAwayFrom);
            return true;
        }

        return false;
    }

    private boolean retreatTowardsLeaderForBetterCohesion() {
        AUnit leader = unit.squadLeader();
        if (leader == null) return false;
        if (unit.hp() <= 33) return false;
        if (unit.distTo(leader) <= 4) return false;

        int minEnemies = A.whenEnemyProtossTerranZerg(1, 2, 3);
        if (unit.meleeEnemiesNearCount(3.5) >= minEnemies) return false;

        if (unit.enemiesNear().inRadius(4.3, unit).atLeast(2)) return false;

        if (unit.squadSize() >= 15) {
            if (unit.distToNearestChokeCenter() <= 5) return false;
        }

        return unit.move(leader, RUN_RETREAT, "RetreatToCohesion")
            && notifyNearbyUnitsToRetreat(unit);
    }

    private boolean runTowardsLeader() {
        AUnit leader = unit.squadLeader();
        if (leader == null) return false;
        if (unit.distTo(leader) <= 3) return false;

        return unit.move(leader, RUN_RETREAT, "RetreatTowardsLeader");
    }

//    private boolean runTowardsBaseOrMainChoke() {
//        if (
//            unit.isDragoon()
//                && unit.shieldPercent() >= 70
//                && unit.distToBase() <= 30
//                && (
//                unit.friendsNear().countInRadius(2, unit) >= 2
//                    || unit.enemiesNear().countInRadius(4, unit) >= 2
//            )) return false;
//
//        return retreatTowardsMainChoke()
//            || retreatByRunningTowardsBase();
//    }

    private boolean shouldForceRetreatDirectlyFromEnemy() {
//        if (unit.woundHp() <= 20) return false;

        if (unit.enemiesThatCanAttackMe(2 + unit.woundPercent() / 40.0).count() >= 4) return true;

        if (unit.meleeEnemiesNearCount(6) >= 4) return false;
        if (unit.enemiesNear().groundUnits().inRadius(5, unit).atLeast(3)) return false;

        if (A.s <= 400) {
            if (unit.meleeEnemiesNearCount(2.2) > 0) return true;
            if (unit.lastUnderAttackLessThanAgo(30)) return true;
        }
        else {
            if (unit.meleeEnemiesNearCount(2.8) >= 2) return true;
        }

        if (unit.isRanged()) {
            if (unit.meleeEnemiesNearCount(2.2) >= 3) return true;
            if (unit.rangedEnemiesCount(1.2) >= 2) return true;
        }

        return false;
    }

    private static boolean retreatByRunningTowardsBase(AUnit unit) {
//        if (Count.ourCombatUnits() >= 12) return false;

        AUnit goTo = Select.mainOrAnyBuilding();
        if (goTo == null) return false;
        if (unit.distTo(goTo) <= (Count.ourCombatUnits() <= 8 ? 6.5 : 10)) return false;

//        if (Count.ourCombatUnits() <= 11) return false;

        if (unit.groundDistToMain() <= 20) return true;

        if (unit.meleeEnemiesNearCount(2.4) >= 3) {
            if (unit.distToNearestChokeCenter() >= 2.6) return true;
            if (unit.nearestChoke().width() >= 4) return true;

            return false;
        }

        if (unit.enemiesNear().ranged().canAttack(unit, 2.4).atLeast(2)) {
            if (unit.distToNearestChokeCenter() >= 2.6) return true;
//            if (unit.nearestChoke().width() >= 4) return true;

            return false;
        }

//        if (OurArmy.strength() >= 400) return false;
//        if (notSafeToRunTowardsMainOrMainChoke()) return false;

        return unit.moveToSafety(RUN_RETREAT, "RetreatTowardsBase")
            && notifyNearbyUnitsToRetreat(unit);
    }

    private static boolean notifyNearbyUnitsToRetreat(AUnit unit) {
        for (AUnit friend : unit.friendsNear().inRadius(1.5, unit).list()) {
            if (
                friend.isRetreating() || friend.isRunning() || friend.isAction(RUN_RETREAT) || friend.id() < unit.id()
            ) continue;

//            friend.move(unit, Actions.RUN_RETREAT, "RetreatTowardsBase");
            retreatByRunningTowardsBase(friend);
        }

        return true;
    }

//    private boolean retreatTowardsMainChoke() {
//        HasPosition goTo = mainChokeDefencePoint();
//        if (goTo == null || unit.distTo(goTo) <= 3) return false;
//
//        if (OurArmy.strength() >= 400) return false;
//
//        if (unit.enemiesNear().buildings().empty()) {
//            if (notSafeToRunTowardsMainOrMainChoke()) return false;
//        }
//
//        return unit.move(goTo, Actions.RUN_RETREAT, "RetreatToMainChoke");
//    }

    private boolean notSafeToRunTowardsMainOrMainChoke() {
        return unit.meleeEnemiesNearCount(2.9) >= (unit.woundPercent() >= 30 ? 2 : 3);
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

    private boolean retreatByRunningFromEnemy(HasPosition runAwayFrom) {
        double dist = unit.friendsNear().inRadius(2, unit).atLeast(1) ? 2.4 : 4;

        return unit.runningManager().runFrom(runAwayFrom, dist, RUN_RETREAT, true)
            && notifyNearbyUnitsToRetreat(unit);
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
