package atlantis.combat.retreating;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.cache.Cache;

public class RetreatManager extends Manager {
    public static int GLOBAL_RETREAT_COUNTER = 0;
    private static Cache<Boolean> cache = new Cache<>();

    // =========================================================

    public RetreatManager(AUnit unit) {
        super(unit);
    }

    // =========================================================

    @Override
    public boolean applies() {
        return ShouldRetreat.shouldRetreat(unit);
    }

    @Override
    protected Manager handle() {
        if (handleRetreat()) return usedManager(this);

        return null;
    }

    protected boolean handleRetreat() {
//        if (ShouldRetreat.shouldRetreat(unit) && !FightInsteadAvoid.shouldFightCached()) {
//        if (ShouldRetreat.shouldRetreat(unit) && !FightInsteadAvoid.shouldFight()) {

        Selection nearEnemies = unit.enemiesNear().canAttack(unit, true, true, 5);
        HasPosition runAwayFrom = nearEnemies.center();

        if (runAwayFrom == null) {
            runAwayFrom = nearEnemies.first();
        }

        if (runAwayFrom == null && nearEnemies.notEmpty()) {
            A.errPrintln("Retreat runAwayFrom is NULL, despite:");
            nearEnemies.print("nearEnemies");
        }

//                    System.err.println("@ " + A.now() + " - RETREAT " + unit.typeWithHash());

        if (runAwayFrom != null) {
            if (retreatByRunningFromEnemy(runAwayFrom) || retreatByRunningTowardsBase()) {
                unitStartedRetreating(runAwayFrom);
                return true;
            }
        }

        return false;
    }

    private boolean retreatByRunningTowardsBase() {
        AUnit main = Select.mainOrAnyBuilding();
        if (main == null || unit.distTo(main) <= 20) return false;

        return unit.moveToMain(Actions.RUN_RETREAT, "RetreatTowardsBase");
    }

    private boolean retreatByRunningFromEnemy(HasPosition runAwayFrom) {
        return unit.runningManager().runFrom(runAwayFrom, 4, Actions.RUN_RETREAT, true);
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

    // =========================================================

//    public boolean getCachedShouldRetreat() {
//        return cache.has("shouldRetreat:" + unit.id()) && cache.get("shouldRetreat:" + unit.id());
//    }

    /**
     * Calculated per unit squad, not per unit.
     */
//    public  boolean shouldNotEngageCombatBuilding () {
//        if (unit.squad() == null) {
//            return false;
//        }
//
//        return cache.get(
//                "shouldNotEngageCombatBuilding:" + unit.squad().name(),
//                10,
//                () -> unit.combatEvalRelative() <= 2.4 && ArmyStrength.ourArmyRelativeStrength() <= 280
//        );
//    }

}
