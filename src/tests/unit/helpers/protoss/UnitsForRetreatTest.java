package tests.unit.helpers.protoss;

import atlantis.units.AUnitType;
import tests.fakes.FakeUnit;

import static tests.unit.AbstractTestWithUnits.*;

public class UnitsForRetreatTest {
    public static final int OURS_BASE_TX = 10;
    public static final int ENEMIES_BASE_TX = 16;

    public static FakeUnit[] ours(AUnitType type, int number) {
        FakeUnit[] ours = new FakeUnit[number];

        for (int i = 0; i < number; i++) {
            ours[i] = fake(type, OURS_BASE_TX - i * 0.2, 10 + i * 0.5 + FakeUnit.firstFreeId / 10.0);
        }

        return ours;
    }

    public static FakeUnit[] enemies(AUnitType type, int number) {
        FakeUnit[] enemies = new FakeUnit[number];

        for (int i = 0; i < number; i++) {
            enemies[i] = fakeEnemy(type, ENEMIES_BASE_TX + i * 0.25, 10 + i * 0.5 + FakeUnit.firstFreeId / 10.0);
        }

        return enemies;
    }
}
