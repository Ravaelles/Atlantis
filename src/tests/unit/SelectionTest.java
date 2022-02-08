package tests.unit;

import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SelectionTest extends AbstractTestWithUnits {

    @Test
    public void chainingSubsequentCallsWorksAsExpected() {
        usingFakeOurs(() -> {
            int total = 22;
            Selection selection = Select.our();
            FakeUnit first = (FakeUnit) Select.our().first();

            assertEquals(total, selection.size());

            selection.ranged();

            assertEquals(total, selection.size());

            selection.ranged().inRadius(1, first);

            assertEquals(total, selection.size());
        });
    }

    @Test
    public void ranged() {
        usingFakeOurs(() -> {
//            Select.our().print();
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
        EnemyInfo.weDiscoveredEnemyUnit(cannon);

        FakeUnit[] fakeEnemies = fakeEnemies(
                cannon,
                fake(AUnitType.Protoss_Zealot),
                fake(AUnitType.Zerg_Sunken_Colony),
                fake(AUnitType.Zerg_Larva)
        );

        EnemyInfo.weDiscoveredEnemyUnit(fake(AUnitType.Protoss_Dragoon));
        EnemyInfo.weDiscoveredEnemyUnit(fake(AUnitType.Zerg_Sunken_Colony));

        Selection enemies = Select.from(fakeEnemies).add(EnemyUnits.selection()).removeDuplicates();

//        enemies.print();

        assertEquals(6, enemies.size());
    }

}
