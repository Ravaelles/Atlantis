package atlantis.combat.micro.avoid;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.dont.DontAvoidEnemy;
import atlantis.combat.micro.avoid.terran.avoid.TerranShouldNeverAvoid;
import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.util.We;

public class WantsToAvoid extends Manager {
    public WantsToAvoid(AUnit unit) {
        super(unit);
    }

    public Manager unitOrUnits(Units enemies) {
        if (enemies.isEmpty()) return null;
        if (shouldNeverAvoidIf(enemies)) return null;

        if ((new DontAvoidEnemy(unit)).applies()) return null;

        return (new DoAvoidEnemies(unit, enemies)).handle();
    }

    // =========================================================

    private boolean shouldNeverAvoidIf(Units enemies) {
        if (unit.isMelee() && !unit.isTerran()) return true;

        if (unit.isWorker() && enemies.onlyMelee()) {
            unit.addLog("BraveWorker");
            return unit.hp() >= 40;
        }

        if (We.terran()) {
            if ((new TerranShouldNeverAvoid(unit)).shouldNeverAvoid()) return true;

            if (unit.isTank() && unit.cooldownRemaining() <= 0) return true;
        }

        return false;
    }

}
