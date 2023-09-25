package tests.unit;

import atlantis.information.enemy.EnemyUnits;
import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.information.strategy.OurStrategy;
import atlantis.information.strategy.TerranStrategies;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.BaseSelect;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import org.junit.Test;
import tests.acceptance.NonAbstractTestFakingGame;

import static org.junit.Assert.assertEquals;

public class SelectionTest extends NonAbstractTestFakingGame {

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
            Select.our().print();
            BaseSelect.clearCache();
            Select.clearCache();

            Selection our = Select.our();

            assertEquals(22, our.size());
            assertEquals(2, our.tanks().size());
            assertEquals(2, Count.tanks());
            assertEquals(1, Select.countOurOfType(AUnitType.Terran_Siege_Tank_Tank_Mode));
            assertEquals(1, Select.countOurOfType(AUnitType.Terran_Siege_Tank_Siege_Mode));
            assertEquals(1, Select.our().ofType(AUnitType.Terran_Siege_Tank_Siege_Mode).count());
            assertEquals(2, Select.our().ofType(
                AUnitType.Terran_Siege_Tank_Siege_Mode, AUnitType.Terran_Siege_Tank_Tank_Mode
            ).count());
            assertEquals(2, Select.ourWithUnfinished().ofType(
                AUnitType.Terran_Siege_Tank_Siege_Mode, AUnitType.Terran_Siege_Tank_Tank_Mode
            ).count());
            assertEquals(22, our.size());
        });
    }

//    @Test
//    public void removesDuplicates() {
//        FakeUnit cannon = fake(AUnitType.Protoss_Photon_Cannon);
//        OurStrategy.setTo(TerranStrategies.TERRAN_MMG_vP);
//        EnemyUnitsUpdater.weDiscoveredEnemyUnit(cannon);
//
//        FakeUnit[] fakeEnemies = fakeEnemies(
//            cannon,
//            fake(AUnitType.Protoss_Zealot),
//            fake(AUnitType.Zerg_Sunken_Colony),
//            fake(AUnitType.Zerg_Larva)
//        );
//
//        EnemyUnitsUpdater.weDiscoveredEnemyUnit(fake(AUnitType.Protoss_Dragoon));
//        EnemyUnitsUpdater.weDiscoveredEnemyUnit(fake(AUnitType.Zerg_Sunken_Colony));
//
//        Selection enemies = Select.from(fakeEnemies).add(EnemyUnits.discovered()).removeDuplicates();
//
////        enemies.print();
//
//        assertEquals(6, enemies.size());
//    }

    @Test
    public void testVariousMethods() {
        usingFakeOurs(() -> {
            Select.our().print();
            Select.enemy().print();

            Selection our = Select.our();
            AUnit zealot = our.first();
            AUnit muta = our.ofType(AUnitType.Zerg_Mutalisk).first();

//            our.exclude(our.combatUnits()).print();
//            our.inShootRangeOf().print();

            assertEquals(11, our.combatUnits().size());

            assertEquals(5, our.havingAntiAirWeapon().size());
            assertEquals(17, our.notHavingAntiAirWeapon().size());

            assertEquals(11, our.havingGroundWeapon().size());

            assertEquals(0, zealot.friendsInRadius(4.9).size());
            assertEquals(1, our.inRadius(4.9, zealot).size());
            assertEquals(3, zealot.friendsInRadius(5.0).size());
            assertEquals(4, our.inRadius(5.0, zealot).size());

            assertEquals(1, zealot.friendsNear().canAttack(zealot, 0).size());
            assertEquals(3, zealot.friendsNear().canAttack(zealot, 4.0).size());

//            muta.friendsNear().canBeAttackedBy(muta, 1).print("Test");
            assertEquals(4, muta.friendsNear().inShootRangeOf(muta).size());
            assertEquals(4, muta.friendsNear().canBeAttackedBy(muta, 1).size());
            assertEquals(9, muta.friendsNear().canBeAttackedBy(muta, 2).size());

            assertEquals(1, our.combatBuildingsAntiAir().size());
            assertEquals(2, our.combatBuildingsAntiLand().size());
            assertEquals(2, our.combatBuildings(false).size());
            assertEquals(3, our.combatBuildings(true).size());
        });
    }

}
