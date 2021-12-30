package atlantis.tests.unit;

import atlantis.enemy.EnemyInformation;
import atlantis.enemy.EnemyUnits;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SelectionTest extends AbstractTestWithUnits {

    @Test
    public void ranged() {
        usingFakeOurs(() -> {
//            Select.our().ranged().print();
            assertEquals(9, Select.our().ranged().size());
        });
    }

    @Test
    public void melee() {
        usingFakeOurs(() -> {
//            Select.our().melee().print();
            assertEquals(3, Select.our().melee().size());
        });
    }

    @Test
    public void removesDuplicates() {
        FakeUnit cannon = fake(AUnitType.Protoss_Photon_Cannon);
        EnemyInformation.weDiscoveredEnemyUnit(cannon);

        FakeUnit[] fakeEnemies = fakeEnemies(
                cannon,
                fake(AUnitType.Protoss_Zealot),
                fake(AUnitType.Zerg_Sunken_Colony),
                fake(AUnitType.Zerg_Larva)
        );

        EnemyInformation.weDiscoveredEnemyUnit(fake(AUnitType.Protoss_Dragoon));
        EnemyInformation.weDiscoveredEnemyUnit(fake(AUnitType.Zerg_Sunken_Colony));

        Selection enemies = Select.from(fakeEnemies).add(EnemyUnits.unitsDiscoveredSelection()).removeDuplicates();

//        enemies.print();

        assertEquals(6, enemies.size());
    }

}
