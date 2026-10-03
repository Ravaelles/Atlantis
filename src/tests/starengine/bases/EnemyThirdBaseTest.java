package tests.starengine.bases;

import atlantis.game.listeners.OnEnemyNewUnitDiscovered;
import atlantis.map.base.define.EnemyMainBase;
import atlantis.map.base.define.EnemyNaturalBase;
import atlantis.map.base.define.EnemyThirdBase;
import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;


import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnemyThirdBaseTest extends WorldStubForTests {
    private FakeUnit dragoon;
    private FakeUnit sunken;

    @Test
    public void enemyThirdBaseIsDetected() {
        FakeUnit[] ours = fakeOurs(
            dragoon = fake(AUnitType.Protoss_Dragoon, 10),
            fake(AUnitType.Protoss_Dragoon, 11),
            fake(AUnitType.Protoss_Dragoon, 11.1),
            fake(AUnitType.Protoss_Dragoon, 11.9)
        );
        FakeUnit[] enemies = fakeEnemies(
            sunken = fake(AUnitType.Zerg_Sunken_Colony, 12),
            fake(AUnitType.Zerg_Larva, 12)
        );

//        useStarEngine(); // Uncomment to use StarEngine

        world(25, ours, enemies, () -> {
            OnEnemyNewUnitDiscovered.update(fakeEnemy(AUnitType.Zerg_Sunken_Colony, 117, 14));

            APosition third = EnemyThirdBase.position();
            APosition natural = EnemyNaturalBase.get();
            APosition enemyMain = EnemyMainBase.get();
            System.err.println("enemyMain = " + enemyMain);
            System.err.println("natural = " + natural);
            System.err.println("third = " + third);

            double ourScore = dragoon.combatEvalAbsolute();
            double enemyScore = sunken.combatEvalAbsolute();

//                assertTrue(ourScore < -10);
//                assertTrue(enemyScore < -10);
        });
    }
}
