package atlantis.combat.micro.avoid;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.buildings.AvoidCombatBuildings;
import atlantis.combat.micro.avoid.margin.SafetyMargin;
import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.util.cache.Cache;

import java.util.List;
import java.util.stream.Collectors;

public class AvoidEnemies extends Manager {
    private static Cache<Units> cache = new Cache<>();
    private AvoidCombatBuildings avoidCombatBuildings;
    private WantsToAvoid wantsToAvoid;
    private Units enemies;

    public AvoidEnemies(AUnit unit) {
        super(unit);
        avoidCombatBuildings = new AvoidCombatBuildings(unit);
        wantsToAvoid = new WantsToAvoid(unit);
    }

    @Override
    public boolean applies() {
        return true;
    }

    // =========================================================

    @Override
    public Manager handle() {
        Manager manager = avoidEnemiesIfNeeded();

        return manager;
    }

    public Manager avoidEnemiesIfNeeded() {
        if (shouldSkip()) {
            return null;
        }

        // =========================================================

        Units enemiesDangerouslyClose = unitsToAvoid();

//        if (!enemiesDangerouslyClose.isEmpty()) {
////            System.err.println("@ " + A.now() + " - No-one close");
//            AUnit nearestEnemy = unit.enemiesNear().nearestTo(unit);
//            APainter.paintTextCentered(unit.position().translateByTiles(0, -1),
//                "C=" + enemiesDangerouslyClose.size() + "(" + (nearestEnemy != null ? A.dist(unit, nearestEnemy) : "-") + ")",
//                Color.Green
//            );
//            return null;
//        }

        // @Check commented out, this should be handled by a manager
//        if (
//            onlyEnemyCombatBuildingsAreNear(enemiesDangerouslyClose)
//                || unit.isAir()
//                || unit.hp() <= 40
//                || unit.combatEvalRelative() < 3.0
//        ) {
//            if (avoidCombatBuildings.handle() != null) {
//                unit.addLog("KeepAway");
//                return usedManager(this);
//            }
//        }

//        APainter.paintLine(unit.targetPosition(), Color.Grey);
//        for (AUnit enemy : enemiesDangerouslyClose.list()) {
//            APainter.paintLine(enemy, Color.Orange);
////            APainter.paintTextCentered(A.dist(enemy), Color.Yellow);
//        }

        // =========================================================

        // Only ENEMY WORKERS
        if (
            unit.hpPercent() >= 70
                && !enemiesDangerouslyClose.isEmpty()
                && Select.from(enemiesDangerouslyClose).workers().size() == enemiesDangerouslyClose.size()
        ) {
            unit.addLog("FightWorkers");
            return null;
        }

        else {
            if (wantsToAvoid.unitOrUnits(enemiesDangerouslyClose) != null) {
                return usedManager(this);
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
                && unit.enemiesNear().groundUnits().effVisible().inRadius(1, unit).notEmpty()
        ) {
            unit.setTooltipTactical("Kamikaze");
            return true;
        }

        if (unit.enemiesNear().combatBuildings(false).notEmpty()) {
            return false;
        }

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

    private boolean onlyEnemyCombatBuildingsAreNear(Units enemiesDangerouslyClose) {
        return Select.from(enemiesDangerouslyClose).combatBuildings(false).size() == enemiesDangerouslyClose.size();
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
//                System.out.println("enemyUnitsToPotentiallyAvoid() = " + enemyUnitsToPotentiallyAvoid().size());
                for (AUnit enemy : enemyUnitsToPotentiallyAvoid()) {
                    double safetyMargin = (new SafetyMargin(unit)).calculateAgainst(enemy);
//                    System.err.println(
//                        enemy + " // " + String.format("%.2f", safetyMargin) + " // " + A.dist(enemy.distTo(unit))
//                    );
//                    APainter.paintLine(enemy, Color.Yellow);
                    enemies.addUnitWithValue(enemy, safetyMargin);
                }
//                enemies.print("Enemies to avoid");

//                System.out.println(unit + " enemies near = " + enemyUnitsToPotentiallyAvoid().size());

                if (enemies.isEmpty()) {
                    return new Units();
                }

//                for (AUnit enemy : enemyUnitsToPotentiallyAvoid()) {
////                    //System.out.println(enemy + " which is " + A.dist(enemy, unit) + " away");
//                    System.out.println(enemy + " has value: " + enemies.valueFor(enemy));
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

//    public  double lowestSafetyMarginForAnyEnemy() {
//        Units enemies = unitsToAvoid(false);
//        if (enemies.isNotEmpty()) {
//            return enemies.lowestValue();
//        }
//
////        return 9876;
//        return 0;
//    }

    public boolean shouldAvoidAnyUnit() {
        return unitsToAvoid().isNotEmpty();
    }

    public boolean shouldNotAvoidAnyUnit() {
        return !shouldAvoidAnyUnit();
    }

    // =========================================================

    protected List<? extends AUnit> enemyUnitsToPotentiallyAvoid() {
        return unit.enemiesNear()
//                .nonBuildings() // This is because we rely on AvoidCombatBuildings
            .removeDuplicates()
            .onlyCompleted()
            .canAttack(unit, true, true, 4.5)
            .havingPosition()
            .list();
    }

    @Override
    public String toString() {
        String enemyString = "NULL";

        if (enemies != null && !enemies.isEmpty()) {
            AUnit enemy = enemies.first();
            enemyString = (enemy + "/" + enemy.getClass().getSimpleName());
        }

        return "AvoidEnemies(" + enemyString + ')';
    }
}
