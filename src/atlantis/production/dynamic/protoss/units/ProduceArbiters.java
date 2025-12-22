package atlantis.production.dynamic.protoss.units;

import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;

import static atlantis.production.AbstractDynamicUnits.trainNowIfHaveWhatsRequired;

public class ProduceArbiters {
    public static void arbiters() {
        if (Count.ofType(AUnitType.Protoss_Arbiter_Tribunal) == 0) return;

        int existing  = Count.ourWithUnfinished(AUnitType.Protoss_Arbiter);
        if (existing >= A.whenEnemyProtossTerranZerg(2, 5, 2)) return;

        trainNowIfHaveWhatsRequired(AUnitType.Protoss_Arbiter, true);
    }
}
