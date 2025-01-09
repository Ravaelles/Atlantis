package tests.unit;

import atlantis.combat.targeting.generic.ATargeting;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;
import tests.unit.helpers.ClearAllCaches;

import static org.junit.jupiter.api.Assertions.*;

public class SelectionTest extends WorldStubForTests {
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
        ClearAllCaches.clearAll();

        usingFakeOurs(() -> {
            Select.our().print();
            Selection our = Select.our();

            assertEquals(22, our.size());
//            Select.our().print("our");
//            Select.ourWithUnfinished().print("Unfinished");
            assertEquals(2, our.ofType(
                AUnitType.Terran_Siege_Tank_Siege_Mode, AUnitType.Terran_Siege_Tank_Tank_Mode
            ).count());
            assertEquals(2, Select.ourWithUnfinished().ofType(
                AUnitType.Terran_Siege_Tank_Siege_Mode, AUnitType.Terran_Siege_Tank_Tank_Mode
            ).count());

//            our.realUnitsAndCombatBuildings().print("Real units and combat buildings");
//            System.err.println("our.realUnitsAndCombatBuildings().size() = " + our.realUnitsAndCombatBuildings().size());
            assertEquals(22, our.size());
            assertEquals(2, our.tanks().size());
            if (Count.tanks() != 2) {
                Select.ourTanks().print("Tanks");
            }
            assertEquals(2, Count.tanks());
            assertEquals(1, Select.countOurOfType(AUnitType.Terran_Siege_Tank_Tank_Mode));
            assertEquals(1, Select.countOurOfType(AUnitType.Terran_Siege_Tank_Siege_Mode));
            assertEquals(1, Select.our().ofType(AUnitType.Terran_Siege_Tank_Siege_Mode).count());
            assertEquals(2, Select.ourTanks().count());
            assertEquals(2, Select.ourTanks().notDeadMan().count());
            assertEquals(2, Select.ourTanks().count());
            assertEquals(2, Select.our().ofType(
                AUnitType.Terran_Siege_Tank_Siege_Mode, AUnitType.Terran_Siege_Tank_Tank_Mode
            ).count());
            assertEquals(2, Select.ourWithUnfinished().ofType(
                AUnitType.Terran_Siege_Tank_Siege_Mode, AUnitType.Terran_Siege_Tank_Tank_Mode
            ).count());
        });
    }

//    @Test
//    public void removesDuplicates() {
//        FakeUnit cannon = fake(AUnitType.Protoss_Photon_Cannon);
//        Strategy.setTo(TerranStrategies.TERRAN_MMG_vP);
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

    @Test
    public void testVariousMethods() {
        usingFakeOurs(() -> {
//            Select.our().print();
//            Select.enemy().print();

            Selection our = Select.our();
            AUnit zealot = our.first();
            AUnit muta = our.ofType(AUnitType.Zerg_Mutalisk).first();

//            our.print("OUR units");
//            our.realUnits().print("OUR REAL units");
//            our.exclude(our.realUnits()).print("OUR MINUS realUnits");
//            our.combatUnits().print("Combat units");
//            our.exclude(our.combatUnits()).print("OUR MINUS combatUnits");

            assertEquals(22, our.size());

            assertEquals(5, our.buildings().size());
            assertEquals(2, our.combatBuildings(false).size());
            assertEquals(3, our.combatBuildings(true).size());
            assertEquals(11, our.combatUnits().size());
            assertEquals(14, our.realUnits().size());

            assertTrue(our.realUnits().cannons().notEmpty());

//            our.exclude(our.combatUnits()).print();
//            our.inShootRangeOf().print();
//            A.println("zealot = " + zealot);

            assertNotEquals(0, our.combatUnits().sunkens().size());
            assertNotEquals(0, our.combatUnits().cannons().size());

            assertEquals(5, our.havingAntiAirWeapon().size());
            assertEquals(17, our.notHavingAntiAirWeapon().size());

            assertEquals(11, our.havingAntiGroundWeapon().size());

            assertEquals(0, zealot.friendsInRadius(4.9).size());
            assertEquals(0, our.inRadius(4.9, zealot).exclude(zealot).size());
            assertEquals(1, our.inRadius(4.9, zealot).size());

//            Select.from(our.sortDataByDistanceTo(zealot, true), "foo").print("Closest to zealot");
//            zealot.friendsInRadius(5.5).print("Friends in radius");
//            our.inRadius(5.3, zealot).print("In radius 5.3 of Zealot");
//            our.inRadius(5.2, zealot).print("In radius 5.2 of Zealot");
            assertEquals(2, zealot.friendsInRadius(5.2).size()); // Muta is 5.2 away, Overlord 5.3
            Select.clearCache();
            assertEquals(2, zealot.friendsInRadius(5.2).size());
//            our.inRadius(5.3, zealot).print("Friend in radius 5.3 of Zealot");
            assertEquals(4, our.inRadius(5.3, zealot).size());

            assertEquals(1, zealot.friendsNear().canAttack(zealot, 0).size());
            assertEquals(3, zealot.friendsNear().canAttack(zealot, 4.0).size());

//            muta.friendsNear().canBeAttackedBy(muta, 1).print("Test");
            assertEquals(4, muta.friendsNear().inShootRangeOf(muta).size());
            assertEquals(4, muta.friendsNear().canBeAttackedBy(muta, 1).size());
//            muta.friendsNear().inRadius(5, muta).print("Muta targets");
            assertEquals(6, muta.friendsNear().canBeAttackedBy(muta, 2).size());

            assertEquals(1, our.combatBuildingsAntiAir().size());
            assertEquals(2, our.combatBuildingsAntiLand().size());
            assertEquals(2, our.combatBuildings(false).size());
            assertEquals(3, our.combatBuildings(true).size());
        });
    }

    @Test
    public void targetsMostWounded() {
        FakeUnit mostWounded, closest;

        FakeUnit our = fake(AUnitType.Protoss_Dragoon, 10);
        FakeUnit[] enemies = fakeEnemies(
            closest = fake(AUnitType.Terran_Marine, 10.3).setHp(18),
            fake(AUnitType.Terran_Bunker, 10.5).setHp(99),
            fake(AUnitType.Terran_Bunker, 11.1),
            fake(AUnitType.Terran_Marine, 12.1).setHp(25),
            mostWounded = fake(AUnitType.Terran_Bunker, 13.1).setHp(12), // The bunker is relatively more wounded
            fake(AUnitType.Terran_Marine, 13.2),
            fake(AUnitType.Terran_Bunker, 13.5)
        );

        createWorld(1,
            () -> {
//                Select.enemy().print();

                assertEquals(mostWounded, Select.enemy().mostWounded());
                assertEquals(mostWounded, Select.enemy().mostWoundedOrNearest(our));
                assertEquals(mostWounded, ATargeting.defineBestEnemyToAttack(our));

                assertEquals(closest, Select.enemy().marines().mostWoundedOrNearest(our));
                assertEquals(closest, Select.enemy().nearestTo(our));
            }, () -> fakeOurs(our), () -> enemies
        );
    }
}
