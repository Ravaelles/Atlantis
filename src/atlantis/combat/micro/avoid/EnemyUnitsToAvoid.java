package atlantis.combat.micro.avoid;

import atlantis.combat.micro.avoid.margin.SafetyMargin;
import atlantis.debug.painter.APainter;
import atlantis.units.AUnit;
import atlantis.units.HasUnit;
import atlantis.units.Units;
import atlantis.util.cache.Cache;
import bwapi.Color;

import java.util.List;
import java.util.stream.Collectors;

public class EnemyUnitsToAvoid extends HasUnit {
    private static Cache<Units> cache = new Cache<>();

    private Units enemies;

    public EnemyUnitsToAvoid(AUnit unit) {
        super(unit);
    }

    public Units enemiesDangerouslyClose() {
        return unitsToAvoid(true);
    }

    public Units unitsToAvoid(boolean onlyDangerouslyClose) {
        return cache.get(
            "unitsToAvoid:" + unit.id() + "," + onlyDangerouslyClose,
            2,
            () -> {
                enemies = new Units();

                for (AUnit enemy : enemyUnitsToPotentiallyAvoid()) {
                    double safetyMargin = (new SafetyMargin(unit)).marginAgainst(enemy);
//                    System.err.println(
//                        enemy + " // " + String.format("%.2f", safetyMargin) + " // " + A.dist(enemy.distTo(unit))
//                    );
//                    enemy.paintLine(unit, Color.Yellow);
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

    protected List<? extends AUnit> enemyUnitsToPotentiallyAvoid() {
        return unit.enemiesNear()
            .removeDuplicates()
            .onlyCompleted()
            .havingWeapon()
            .canAttack(unit, true, true, 4.5)
            .havingPosition()
            .list();
    }
}
