package atlantis.combat.micro.avoid;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.Units;

public class DoAvoidEnemies extends Manager {
    private ProcessAvoid avoid;
    private Units enemies;

    public DoAvoidEnemies(AUnit unit) {
        super(unit);
        avoid = new ProcessAvoid(unit);
        enemies = defineEnemies(unit);
    }

    private static Units defineEnemies(AUnit unit) {
        EnemyUnitsToAvoid enemyUnitsToAvoid = new EnemyUnitsToAvoid(unit);
        Units enemies = enemyUnitsToAvoid.unitsToAvoid(false);

        if (enemies.isEmpty() && (unit.hp() <= 30 || unit.woundPercent() >= 25)) {
            return enemyUnitsToAvoid.unitsToAvoid(false);
        }

        return enemies; // Can be empty
    }

    @Override
    public Manager handle() {
//        if (enemies.size() == 1) {

//        A.printStackTrace("Avoiding... " + unit.idWithHash());

        if (
            enemies.size() == 1
                ||
                (unit.isDragoon() && enemies.onlyRanged())
                || unit.isScout()
        ) {
            return avoid.singleUnit(enemies.first());
        }
        else if (enemies.size() >= 2) {
            return avoid.groupOfUnits(enemies);
        }

        return null;
    }
}
