package tests.unit.helpers;

import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.units.fogged.AbstractFoggedUnit;
import atlantis.units.select.BaseSelect;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import tests.fakes.FakeBullets;
import tests.fakes.FakeUnit;

public class ClearAllCaches {
    public static void clearAll() {
        AbstractFoggedUnit.clearCache();
        FakeUnit.clearCache();
        AvoidEnemies.clearCache();
        BaseSelect.clearCache();
        Count.clearCache();
        EnemyInfo.clearCache();
        EnemyUnits.clearCache();
        FakeBullets.allBullets.clear();
        if (Queue.get() != null) Queue.get().clearCache();
        ReservedResources.reset();
        Select.clearCache();
    }
}
