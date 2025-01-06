package atlantis.combat.micro.avoid;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.enemies.AttackNearbyEnemies;
import atlantis.game.A;
import atlantis.units.AUnit;

public class RunError extends Manager {
    public RunError(AUnit unit) {
        super(unit);
    }

    protected Manager handleErrorRun(AUnit unit) {
        if (A.isUms()) {
            if (!unit.isObserver()) A.errPrintln(A.now() + " ERROR_RUN for " + unit.nameWithId());
        }
        unit.addLog("RUN-ERROR");

        if (unit.noCooldown()) {
            AttackNearbyEnemies attackNearbyEnemies = new AttackNearbyEnemies(unit);
            if (attackNearbyEnemies.handleAttackNearEnemyUnits()) {
                unit.setTooltipTactical("Cant run, fight");
                return usedManager(this, "RunErrorAttack");
            }
        }

        unit.runningManager().stopRunning();
        return null;
    }
}