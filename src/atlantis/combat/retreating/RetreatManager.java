package atlantis.combat.retreating;

import atlantis.combat.micro.avoid.FightInsteadAvoid;
import atlantis.information.generic.ArmyStrength;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;
import atlantis.util.cache.Cache;

public class RetreatManager {

    public static int GLOBAL_RETREAT_COUNTER = 0;
    private static Cache<Boolean> cache = new Cache<>();

    // =========================================================

    public static boolean handleRetreat(AUnit unit) {
        if (ShouldRetreat.shouldRetreat(unit) && !FightInsteadAvoid.shouldFightCached(unit)) {
            if (TempDontRetreat.temporarilyDontRetreat(unit)) {
                return false;
            }

            Selection nearEnemies = unit.enemiesNear().canAttack(unit, true, true, 5);
            HasPosition runAwayFrom = nearEnemies.center();
            if (runAwayFrom == null) {
                runAwayFrom = nearEnemies.first();
            }

            if (runAwayFrom == null && nearEnemies.notEmpty()) {
                System.err.println("Retreat runAwayFrom is NULL, despite:");
                nearEnemies.print("nearEnemies");
            }

            if (runAwayFrom != null && unit.runningManager().runFrom(runAwayFrom, 4, Actions.RUN_RETREAT)) {
                unit.addLog("RetreatedFrom" + runAwayFrom);
                return true;
            }
        }

        return false;
    }

    // =========================================================

    public static boolean getCachedShouldRetreat(AUnit unit) {
        return cache.has("shouldRetreat:" + unit.id()) && cache.get("shouldRetreat:" + unit.id());
    }

    /**
     * Calculated per unit squad, not per unit.
     */
    public static boolean shouldNotEngageCombatBuilding(AUnit unit) {
        if (unit.squad() == null) {
            return false;
        }

        return cache.get(
                "shouldNotEngageCombatBuilding:" + unit.squad().name(),
                10,
                () -> unit.combatEvalRelative() <= 1.8 && ArmyStrength.ourArmyRelativeStrength() <= 250
        );
    }

}
