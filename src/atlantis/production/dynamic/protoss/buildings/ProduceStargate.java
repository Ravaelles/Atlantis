package atlantis.production.dynamic.protoss.buildings;

import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

import static atlantis.units.AUnitType.Protoss_Fleet_Beacon;
import static atlantis.units.AUnitType.Protoss_Stargate;

public class ProduceStargate {
    public static boolean produce() {
        Decision decision = A.whenEnemyProtossTerranZerg(
            ProduceStargate::produceAgainstProtoss,
            ProduceStargate::produceAgainstTerran,
            ProduceStargate::produceAgainstZerg
        );

        if (decision.isTrue()) produceStargate();

        return false;
    }

    private static void produceStargate() {
        AddToQueue.withStandardPriority(Protoss_Stargate);
    }

    private static Decision produceAgainstTerran() {
        if (!Enemy.terran()) return Decision.INDIFFERENT;

        return Decision.FORBIDDEN;
    }

    private static Decision produceAgainstProtoss() {
        if (!Enemy.protoss()) return Decision.INDIFFERENT;

        Selection enemies = EnemyUnits.discovered();

        return Decision.fromBoolean(
            enemies.carriers().notEmpty() || enemies.ofType(Protoss_Fleet_Beacon).notEmpty()
        );
    }

    private static Decision produceAgainstZerg() {
        if (!Enemy.zerg()) return Decision.INDIFFERENT;
        if (A.supplyUsed() <= 60) return Decision.FORBIDDEN;

        Selection enemies = EnemyUnits.discovered();

        return Decision.fromBoolean(enemies.mutalisks().notEmpty());
    }
}
