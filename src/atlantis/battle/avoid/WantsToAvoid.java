package atlantis.battle.avoid;

import atlantis.architecture.Manager;
import atlantis.battle.avoid.dont.DontAvoidEnemy;
import atlantis.units.AUnit;
import atlantis.units.Units;

public class WantsToAvoid extends Manager {
    public WantsToAvoid(AUnit unit) {
        super(unit);
    }

    public Manager handleAvoidUnitOrUnits(Units enemies) {
        if (enemies.isEmpty()) return null;

        if (shouldNeverAvoidIf(enemies)) return null;
        if ((new DontAvoidEnemy(unit)).applies()) return null;

        return (new DoAvoidEnemies(unit, enemies)).handle();
    }

    // =========================================================

    // @ToDo - Remove and move into DontAvoidEnemy
    private boolean shouldNeverAvoidIf(Units enemies) {
        if (unit.isWorker() && unit.isGatheringMinerals()) return true;

        if (unit.isMelee() && unit.hp() >= 30 && enemies.onlyWorkers()) return true;

        return unit.isMelee()
            && !unit.isWorker()
            && !unit.isTerran()
            && (new DontAvoidEnemy(unit)).applies();

//        if (unit.isWorker() && enemies.onlyMelee() && unit.meleeEnemiesNearCount(3) == 0) {
//            unit.addLog("BraveWorker");
//            return unit.hp() >= 40;
//        }
    }

}
