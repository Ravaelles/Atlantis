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
//        System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - ...");

        if (unit.isWorker() && !unit.isScout()) return false;
        if (unit.isLoaded()) return false;
        if (unit.lastCommandIssuedAgo() >= 100) return false;
        if (unit.isDancingAway() && unit.isMoving() && unit.distToTargetPosition() >= 2) return false;
        if (unit.enemiesNear().canAttack(unit, 10).empty()) return false;

        if (unit.isMoving() && unit.lastStartedRunningAgo() <= 2) return true;

//        return true;
        return !(new DontAvoidEnemy(unit)).applies();
    }

    @Override
    protected Manager handle() {
        if (unit.isCombatUnit() && unit.enemiesThatCanAttackMe(3).empty()) return null;
//        if (unit.isMarine()) System.err.println(
//                "@ " + A.now() + " - AVOID ENEMIES " + unit.typeWithUnitId() + " - " + avoidEnemies());

        Units enemies = enemiesDangerouslyClose();
        if (enemies.isEmpty()) return null;

        if (wantsToAvoid.handleAvoidUnitOrUnits(enemies) != null && unit.isMoving()) {
            return usedManager(this);
        }

        return null;
    }

    // =========================================================

    public Units enemiesDangerouslyClose() {
        return enemyUnitsToAvoid.enemiesDangerouslyClose();
    }

    // =========================================================

    @Override
    public String toString() {
        String enemyString = "None!";

        if (unit.runningFromUnit() != null) {
            AUnit enemy = unit.runningFromUnit();
            enemyString = enemy == null ? "-" : A.substring(enemy.type().name(), 0, 10);
        }
        else if (unit.runningFromPosition() != null) {
            enemyString = unit.runningFromPosition().toString();
        }

//        return "AvoidEnemies(" + enemyString + "," + unit.lastRunningType() + ")" + ')';
        return "AvoidEnemies";
    }
}
