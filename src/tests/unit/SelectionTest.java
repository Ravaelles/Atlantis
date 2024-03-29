package tests.unit;

import atlantis.information.enemy.EnemyUnits;
import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.units.AUnit;
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
    public void cloneByRemovingIf() {
        usingFakeOurs(() -> {
            Selection our = Select.our();
//            Select.our().print();

            assertEquals(22, our.size());

//            our.print("Pre");

            assertEquals(19, our.cloneByRemovingIf(AUnit::isMelee, "isMelee").size());

//            our.print("Post");

            assertEquals(22, our.size());
        });
    }

    @Test
    public void ranged() {
        usingFakeOurs(() -> {
            assertEquals(9, Select.our().ranged().size());
        });
    }

    @Test
    public void melee() {
        usingFakeOurs(() -> {
            assertEquals(3, Select.our().melee().size());
        });
    }

    @Test
    public void tanks() {
        usingFakeOurs(() -> {
//            Select.our().print();

            Selection our = Select.our();

            assertEquals(22, our.size());
            assertEquals(2, our.tanks().size());
//            assertEquals(2, Select.our().tanks().size());
            assertEquals(22, our.size());
        });
    }

    @Test
    public void removesDuplicates() {
        FakeUnit cannon = fake(AUnitType.Protoss_Photon_Cannon);
        EnemyUnitsUpdater.weDiscoveredEnemyUnit(cannon);

        FakeUnit[] fakeEnemies = fakeEnemies(
                cannon,
                fake(AUnitType.Protoss_Zealot),
                fake(AUnitType.Zerg_Sunken_Colony),
                fake(AUnitType.Zerg_Larva)
        );

        EnemyUnitsUpdater.weDiscoveredEnemyUnit(fake(AUnitType.Protoss_Dragoon));
        EnemyUnitsUpdater.weDiscoveredEnemyUnit(fake(AUnitType.Zerg_Sunken_Colony));

        Selection enemies = Select.from(fakeEnemies).add(EnemyUnits.discovered()).removeDuplicates();

//        enemies.print();

        assertEquals(6, enemies.size());
    }

}
