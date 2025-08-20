package atlantis.production.constructions.builders;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Selection;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

public class CancelConstructionsWhereEnemyIsTooClose {

    private static Selection enemies;

    public static boolean cancelIfNeeded(AUnit builder, Construction construction) {
        if (construction == null) return false;
        if (!construction.notStarted()) return false;

        enemies = builder.enemiesNear().combatUnits().havingAntiGroundWeapon();

        if (dangerousEnemiesNear(builder, construction)) return true;

        AUnit enemy = enemies.nearestTo(builder);

        if (builderTooCloseToEnemy(builder, enemy)) {
            return cancel(
                construction,
                "Enemy too close to builder: " + enemy + " (" + enemy.distToDigit(builder) + ")"
            );
        }

        if (positionTooCloseToEnemy(construction.buildPosition(), enemy)) {
            return cancel(
                construction,
                "Enemy too close to construction: " + enemy
                    + " (" + enemy.distToDigit(construction.buildPosition()) + ")"
            );
        }

        return false;
    }

    private static boolean dangerousEnemiesNear(AUnit builder, Construction construction) {
        return enemies.ofType(
            AUnitType.Protoss_Dark_Templar,
            AUnitType.Terran_Siege_Tank_Tank_Mode,
            AUnitType.Zerg_Lurker
        ).countInRadius(12.5, construction.buildPosition()) >= 1;
    }

    private static boolean positionTooCloseToEnemy(APosition position, AUnit enemy) {
        if (position == null) return false;

        double positionToEnemyDist = position.distTo(enemy) - enemy.groundWeaponRange();

        return positionToEnemyDist <= 5;
    }

    private static boolean cancel(Construction construction, String reason) {
        AUnitType type = construction.buildingType();
        construction.cancel(reason);

        ProductionOrder newOrder = AddToQueue.withHighPriority(type);
        ErrorLog.debug("BuilderCancel, newOrder: " + newOrder);

        return true;
    }

    private static boolean builderTooCloseToEnemy(AUnit builder, AUnit enemy) {
        double builderToEnemyDist = builder.distTo(enemy) - enemy.groundWeaponRange();

        if (We.protoss()) {
            return builderToEnemyDist <= 2 || (builderToEnemyDist <= 4 && builder.shieldWounded());
        }

        return builderToEnemyDist <= 2;
    }
}
