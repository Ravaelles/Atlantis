package atlantis.combat.micro.avoid;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.units.actions.Actions;
import atlantis.util.cache.Cache;

public class AvoidEnemiesIfNeeded extends Manager {
    private static Cache<Units> cache = new Cache<>();
    private WantsToAvoid wantsToAvoid;
    //    private Units enemiesDangerouslyClose;
    private EnemyUnitsToAvoid enemyUnitsToAvoid;
    public boolean forceAvoid = false;

    public AvoidEnemiesIfNeeded(AUnit unit) {
        super(unit);
        wantsToAvoid = new WantsToAvoid(unit);
        enemyUnitsToAvoid = new EnemyUnitsToAvoid(unit);
//        enemiesDangerouslyClose = new Units();
    }

    public static void clearCache() {
        cache.clear();
    }

    // =========================================================

    @Override
    public boolean applies() {
        if (forceAvoid) return true;

        if (unit.lastActionLessThanAgo(Math.max(6, unit.cooldownAbsolute() / 2), Actions.ATTACK_UNIT)) return false;

        return !(new ShouldNotAvoid(unit, enemiesDangerouslyClose())).shouldNotAvoid();
    }

    @Override
    protected Manager handle() {
        Manager manager = avoidEnemiesIfNeeded();

        return manager;
    }

    public Manager avoidEnemiesIfNeeded() {
        if (wantsToAvoid.unitOrUnits(enemiesDangerouslyClose()) != null) {
            return usedManager(unit.manager());
        }

        return null;
    }

    // =========================================================

    public Units enemiesDangerouslyClose() {
        return enemyUnitsToAvoid.enemiesDangerouslyClose();
    }

    public boolean shouldAvoidAnyUnit() {
        return enemiesDangerouslyClose().isNotEmpty();
    }

    public boolean shouldNotAvoidAnyUnit() {
        return !shouldAvoidAnyUnit();
    }

    // =========================================================

    @Override
    public String toString() {
        String enemyString = "NULL";

        if (unit.runningFrom() != null) {
            AUnit enemy = unit.runningFrom();
            enemyString = A.substring(enemy.type().name(), 0, 10);
        }

        return "AvoidEnemiesIfNeeded(" + enemyString + ')';
    }
}
