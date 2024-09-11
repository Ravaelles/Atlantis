package atlantis.combat.micro.avoid;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.terran.avoid.ShouldNeverAvoidAsTerran;
import atlantis.units.AUnit;
import atlantis.units.Units;

public class WantsToAvoid extends Manager {
    public WantsToAvoid(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return true;
    }

    public Manager unitOrUnits(Units enemies) {
        if (enemies.isEmpty()) return null;
        if (shouldNeverAvoidIf(enemies)) return null;

        // =========================================================

        FightInsteadAvoid fightInsteadAvoid = new FightInsteadAvoid(unit, enemies);
        if (fightInsteadAvoid.invokeFrom(this) != null) {
            return usedManager(fightInsteadAvoid);
        }

        // =========================================================

//        if (unit.isDragoon()) {
//            A.printStackTrace();
//        }

//        return avoid.singleUnit(enemies.first());

//        if (enemies.size() == 1 || unit.isDragoon()) {
//            return avoid.singleUnit(enemies.first());
//        }
//        else {
//            return avoid.groupOfUnits(enemies);
//        }

        return (new DoAvoidEnemies(unit)).handle();
    }

    // =========================================================

    private boolean shouldNeverAvoidIf(Units enemies) {
        if (unit.isMelee() && !unit.isTerran()) return true;

        if (unit.isWorker() && enemies.onlyMelee()) {
            unit.addLog("BraveWorker");
            return unit.hp() >= 40;
        }

        if ((new ShouldNeverAvoidAsTerran(unit)).shouldNeverAvoid()) return true;

        if (unit.isTank() && unit.cooldownRemaining() <= 0) return true;

//        if (unit.isWorker() || unit.isAir()) return false;

        return false;
    }

}
