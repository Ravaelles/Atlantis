package tests.unit;

import atlantis.units.AUnitType;
import atlantis.units.select.BaseSelect;
import atlantis.units.select.Select;
import atlantis.util.Angle;
import atlantis.util.Callback;
import atlantis.util.cache.Cache;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import tests.acceptance.NonAbstractTestFakingGame;

import static atlantis.units.AUnitType.Protoss_Zealot;
import static org.junit.Assert.assertEquals;

public class CacheTest extends NonAbstractTestFakingGame {
    private static int counter = 0;

    @Test
    public void getIfValid() {
        FakeUnit our = fake(AUnitType.Terran_Marine, 10);

        FakeUnit[] enemies = fakeEnemies(
            fake(AUnitType.Zerg_Hydralisk, 14),
            fake(AUnitType.Zerg_Sunken_Colony, 16)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
            counter = 0;

            Select.enemy().print("Enemies");

            FakeUnit hydra = enemies[0];
            FakeUnit sunken = enemies[1];
            Cache<FakeUnit> cache = new Cache<>();

            assertEquals(null, cache.get("keyA"));

            Callback callback = () -> {
                counter++;
//                System.out.println("@@@@@@@@ ENEMY = " + Select.enemy().havingPosition().first());
                return Select.enemy().havingPosition().first();
            };

            FakeUnit getIfValid = cache.getIfValid(
                "keyA",
                3,
                callback
            );

            assertEquals(hydra, getIfValid);
            assertEquals(1, counter);

            FakeUnit simpleGetCall = cache.get(
                "keyA",
                3,
                callback
            );

            assertEquals(hydra, simpleGetCall);
            assertEquals(1, counter);

            getIfValid = cache.getIfValid(
                "keyA",
                3,
                callback
            );

            assertEquals(1, counter);

            hydra.position = null;

            FakeUnit nowUnitHasNoPosition = cache.getIfValid(
                "keyA",
                3,
                callback
            );

            assertEquals(2, counter);
            assertEquals(sunken, nowUnitHasNoPosition);

            nowUnitHasNoPosition = cache.getIfValid(
                "keyA",
                3,
                callback
            );

            assertEquals(2, counter);

            counter = 0;
        });
    }
}
