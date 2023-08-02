package atlantis.combat.missions;

import atlantis.architecture.Manager;
import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.advance.focus.MissionFocusPoint;
import atlantis.decions.Decision;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.Units;

/**
 * Represents behavior for squad of units e.g. DEFEND, CONTAIN (enemy at his base), ATTACK etc.
 */
public abstract class Mission extends MissionHelper {
    private static Mission instance;
    protected MissionFocusPoint focusPointManager;
    private String name;

    // =========================================================

    protected Mission(String name) {
        this.name = name;
        instance = this;
    }

    // =========================================================

//    public abstract Manager handle(AUnit unit);

    public Manager handle(AUnit unit) {
        unit.setTooltipTactical("#MA");

        return managerClass(unit).handle();
    }

    // =========================================================

    /**
     * Optimal distance to focus point or -1 if not defined.
     */
    public abstract double optimalDist();

    protected abstract Manager managerClass(AUnit unit);

    public AFocusPoint focusPoint() {
        return focusPointManager.focusPoint();
    }

    // Template method
    public boolean allowsToRetreat(AUnit unit) {
        return true;
    }

    public Decision permissionToAttack(AUnit unit) {
        return Decision.INDIFFERENT;
    }

    // Template method
    public boolean allowsToAttackEnemyUnit(AUnit unit, AUnit enemy) {
        if (enemy.isCombatBuilding()) {
            return allowsToAttackCombatBuildings(unit, enemy);
        }

        return true;
    }

    // Template method
    public boolean allowsToAttackCombatBuildings(AUnit unit, AUnit combatBuilding) {
        if (unit.isInfantry() && unit.hp() <= 39) {
            return false;
        }

        return unit.friendsNearCount() >= 7;
    }

    // Template method
    public boolean forcesUnitToFight(AUnit unit, Units enemies) {
        return false;
    }

    // =========================================================

    // =========================================================

    @Override
    public String toString() {
        return "Mission " + name;
    }

    // =========================================================

    public static Mission get() {
        return instance;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

//    public APosition temporaryTarget() {
//        return temporaryTarget;
//    }
//
//    public void setTemporaryTarget(APosition temporaryTarget) {
//        this.temporaryTarget = temporaryTarget;
//    }

}
