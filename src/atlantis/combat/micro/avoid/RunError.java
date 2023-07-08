package atlantis.combat.micro.avoid;

import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.units.AUnit;
import atlantis.architecture.Manager;

public class RunError {
    private final Avoid avoid;

    public RunError(Avoid avoid) {
        this.avoid = avoid;
    }

    protected Manager handleErrorRun(AUnit unit) {
//        System.err.println("ERROR_RUN for " + unit.nameWithId());
        unit.addLog("RUN-ERROR");

        if (unit.noCooldown()) {
            AttackNearbyEnemies.handleAttackNearEnemyUnits(unit);
            unit.setTooltipTactical("Cant run, fight");
        }

        return null;
    }
}