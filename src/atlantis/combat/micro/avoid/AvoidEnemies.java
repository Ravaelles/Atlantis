package atlantis.combat.micro.avoid;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.dont.DontAvoidEnemy;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.util.cache.Cache;

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
//        if (unit.isMissionSparta() && unit.isHealthy()) return false;
//        if (unit.lastActionLessThanAgo(Math.max(6, unit.cooldownAbsolute() / 2), Actions.ATTACK_UNIT)) return false;

        return
//            !(new ShouldNotAvoid(unit, enemiesDangerouslyClose())).shouldNotAvoid()
            !(new DontAvoidEnemy(unit)).applies();
    }

    @Override
    protected Manager handle() {
//        if (unit.isDragoon()) System.err.println("@ " + A.now() + " - AVOID ENEMIES " + unit.typeWithUnitId() + " - ");

        return avoidEnemiesIfNeeded();
    }

    public Manager avoidEnemiesIfNeeded() {
        if (!applies()) return null;

        if (wantsToAvoid.unitOrUnits(enemiesDangerouslyClose()) != null) {
            return usedManager(unit.manager());
        }

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
