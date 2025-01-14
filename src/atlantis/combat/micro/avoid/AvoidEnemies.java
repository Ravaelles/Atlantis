package atlantis.combat.micro.avoid;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.always.ProtossAlwaysAvoidEnemy;
import atlantis.combat.micro.avoid.dont.DontAvoidEnemy;
import atlantis.combat.micro.avoid.dont.protoss.ObserverDontAvoidEnemy;
import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.util.We;
import atlantis.util.cache.Cache;

import java.lang.reflect.ParameterizedType;

public class AvoidEnemies extends Manager {
    private static Cache<Units> cache = new Cache<>();
    private WantsToAvoid wantsToAvoid;
    private EnemyUnitsToAvoid enemyUnitsToAvoid;

    public AvoidEnemies(AUnit unit) {
        super(unit);
        wantsToAvoid = new WantsToAvoid(unit);
        enemyUnitsToAvoid = new EnemyUnitsToAvoid(unit);
    }

    public static void clearCache() {
        cache.clear();
    }

    // =========================================================

    @Override
    public boolean applies() {
        if (unit.effUndetected()) return false;

//        System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - ...");

//        if (unit.isMissionSparta() && unit.isHealthy()) return false;
//        if (unit.lastActionLessThanAgo(Math.max(6, unit.cooldownAbsolute() / 2), Actions.ATTACK_UNIT)) return false;

        if (We.protoss()) {
            if ((new ProtossAlwaysAvoidEnemy(unit)).applies()) return true;

            Decision decision;
            if ((decision = ObserverDontAvoidEnemy.shouldAvoid(unit)).notIndifferent()) return decision.toBoolean();
        }


        return !(new DontAvoidEnemy(unit)).applies();
    }

    @Override
    protected Manager handle() {
//        if (unit.isDragoon()) System.err.println("@ " + A.now() + " - AVOID ENEMIES " + unit.typeWithUnitId() + " - ");

        return avoidEnemies();
    }

    public Manager avoidEnemies() {
        if (!applies()) return null;

        Units enemies = enemiesDangerouslyClose();

        if (wantsToAvoid.unitOrUnits(enemies) != null) {
//            System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - ##### AVOID ##### " + unit.runningManager().runningFromUnit());
//            return usedManager(unit.manager());
            return usedManager(this);
        }

        if (unit.isRunning()) {
            unit.runningManager().stopRunning();
            unit.addLog("StopAvoiding");
            unit.stop("StopAvoiding");
            return null;
        }

//        if (unit.isWounded() && unit.meleeEnemiesNearCount(2.5) > 0) {
//            System.err.println("@@@@@@@@@@ HELL? ");
//            enemies.print("ENEMIES - " + enemies.size());
//            unit.managerLogs().print();
//            unit.manager().printParentsStack();
//        }

        return null;
    }

    // =========================================================

    public Units enemiesDangerouslyClose() {
        return enemyUnitsToAvoid.enemiesDangerouslyClose();
    }

//    public boolean shouldAvoidAnyUnit() {
//        return enemiesDangerouslyClose().isNotEmpty();
//    }

//    public boolean shouldNotAvoidAnyUnit() {
//        return !shouldAvoidAnyUnit();
//    }

    // =========================================================

    @Override
    public String toString() {
        String enemyString = "NULL";

        if (unit.runningFromUnit() != null) {
            AUnit enemy = unit.runningFromUnit();
            enemyString = enemy == null ? "-" : A.substring(enemy.type().name(), 0, 10);
        }

        return "AvoidEnemies(" + enemyString + ')';
    }
}
