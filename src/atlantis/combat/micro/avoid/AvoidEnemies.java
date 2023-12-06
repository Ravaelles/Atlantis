package atlantis.combat.micro.avoid;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.margin.SafetyMargin;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.util.cache.Cache;

import java.util.List;
import java.util.stream.Collectors;

public class AvoidEnemies extends Manager {
    private static Cache<Units> cache = new Cache<>();
    private WantsToAvoid wantsToAvoid;
    private Units enemies;

    public AvoidEnemies(AUnit unit) {
        super(unit);
        wantsToAvoid = new WantsToAvoid(unit);
    }

    @Override
    public boolean applies() {
        if (unit.enemiesNear().canAttack(unit, 6).empty()) return false;
        if (unit.effUndetected() && unit.hp() >= 23) return false;
        if (unit.lastActionLessThanAgo(50, Actions.SPECIAL)) return false;
        if (shouldSkip()) return false;

        return true;
    }

    // =========================================================

    @Override
    protected Manager handle() {
        Manager manager = avoidEnemiesIfNeeded();

        return manager;
    }

    public Manager avoidEnemiesIfNeeded() {
        Units enemiesDangerouslyClose = unitsToAvoid();

        // =========================================================

        // Only ENEMY WORKERS
        if (
            unit.hpPercent() >= 70
                && !enemiesDangerouslyClose.isEmpty()
                && Select.from(enemiesDangerouslyClose).workers().size() == enemiesDangerouslyClose.size()
        ) {
//            unit.addLog("FightWorkers");
            return null;
        }

        else {
            if (wantsToAvoid.unitOrUnits(enemiesDangerouslyClose) != null) {
                return usedManager(unit.manager());
            }
        }

        return null;
    }

    public static void clearCache() {
        cache.clear();
    }

    // =========================================================

    private boolean shouldSkip() {
        if (isUnitCloakedAndRelativelySafe()) return true;

        if (
            unit.hp() <= 16 && unit.isMelee() && unit.isCombatUnit()
                && unit.enemiesNear().groundUnits().inRadius(1, unit).notEmpty()
        ) {
            unit.setTooltipTactical("Kamikaze");
            return true;
        }

        if (unit.enemiesNear().combatBuildings(false).notEmpty()) return false;

        if (
            unit.lastActionLessThanAgo(5, Actions.ATTACK_UNIT)
                && unit.lastStartedAttackMoreThanAgo(8)
        ) {
            unit.setTooltipTactical("StartAttack");
            return true;
        }

        return unit.isLoaded();
    }

    private boolean isUnitCloakedAndRelativelySafe() {
        return unit.effUndetected()
            && unit.enemiesNear().combatBuildingsAnti(unit).inRadius(9, unit).empty()
            && unit.enemiesNear().detectors().inRadius(11, unit).empty();
    }

    public Units unitsToAvoid() {
        return unitsToAvoid(true);
    }

    public Units unitsToAvoid(boolean onlyDangerouslyClose) {
        return cache.get(
            "unitsToAvoid:" + unit.id() + "," + onlyDangerouslyClose,
            1,
            () -> {
                enemies = new Units();

                for (AUnit enemy : enemyUnitsToPotentiallyAvoid()) {
                    double safetyMargin = (new SafetyMargin(unit)).calculateAgainst(enemy);
//                    System.err.println(
//                        enemy + " // " + String.format("%.2f", safetyMargin) + " // " + A.dist(enemy.distTo(unit))
//                    );
//                    APainter.paintLine(enemy, Color.Yellow);
                    enemies.addUnitWithValue(enemy, safetyMargin);
                }
//                enemies.print("Enemies to avoid");


                if (enemies.isEmpty()) {
                    return new Units();
                }

//                for (AUnit enemy : enemyUnitsToPotentiallyAvoid()) {


//                }

                if (onlyDangerouslyClose) {
                    return enemies.replaceUnitsWith(
                        enemies.stream()
                            .filter(e -> enemies.valueFor(e) < 0)
                            .collect(Collectors.toList())
                    );
                }
                else {
                    return enemies;
                }
            }
        );
    }

    public boolean shouldAvoidAnyUnit() {
        return unitsToAvoid().isNotEmpty();
    }

    public boolean shouldNotAvoidAnyUnit() {
        return !shouldAvoidAnyUnit();
    }

    // =========================================================

    protected List<? extends AUnit> enemyUnitsToPotentiallyAvoid() {
        return unit.enemiesNear()
            .removeDuplicates()
            .onlyCompleted()
            .havingWeapon()
            .canAttack(unit, true, true, 4.5)
            .havingPosition()
            .list();
    }

    @Override
    public String toString() {
        String enemyString = "NULL";

        if (unit.runningFrom() != null) {
            AUnit enemy = unit.runningFrom();
            enemyString = A.substring(enemy.type().name(), 0, 10);
        }

        return "AvoidEnemies(" + enemyString + ')';
    }
}
