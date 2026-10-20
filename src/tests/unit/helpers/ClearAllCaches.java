package tests.unit.helpers;

import atlantis.battle.avoid.protoss.ProtossAvoidEnemies;
import atlantis.battle.cluster.Squad;
import atlantis.battle.cluster.Alpha;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.enemy.UnitsArchive;
import atlantis.information.generic.ArmyStrength;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.units.AliveEnemies;
import atlantis.units.fogged.AbstractFoggedUnit;
import atlantis.units.select.BaseSelect;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import tests.fakes.FakeBullets;
import tests.fakes.FakeUnit;
import atlantis.battle.cluster.Missions;
import atlantis.core.world.Worlds;

public class ClearAllCaches {
    /**
     * Drops every cached *query* result, but leaves unit state alone.
     *
     * <p>Use this when a test changes the set of units mid-test (see
     * {@code DynamicMockOurUnits.mockOur}): the cached selections are stale, but
     * the units the test just built are still valid. {@link #clearAll()} nulls
     * the position, hp and id of every FakeUnit, which silently turned every
     * unit added after the first frame into a unit with no position.</p>
     */
    public static void clearQueries() {
        AliveEnemies.clearCache();
        ArmyStrength.clearCache();
        ProtossAvoidEnemies.clearCache();
        BaseSelect.clearCache();
        Count.clearCache();
        EnemyInfo.clearCache();
        EnemyUnits.clearCache();
        ReservedResources.reset();
        Select.clearCache();
    }

    public static void clearAll() {
        // Was production code's job: AUnit.clearAUnitCache() reset the shared
        // registry whenever it ran, under `if (Env.isTesting())`. A cache-clearing
        // method deciding that the world ends is the environment leaking into the
        // core, and it fired once per fake unit. The harness that empties the
        // fakes now empties the registry with them.
        Worlds.reset();

        Missions.reset();
        UnitsArchive.reset();
        AbstractFoggedUnit.clearCache();
        FakeUnit.clearCache();
        AliveEnemies.clearCache();
        ArmyStrength.clearCache();
        ProtossAvoidEnemies.clearCache();
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
