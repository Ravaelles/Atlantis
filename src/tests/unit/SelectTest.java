package tests.unit;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.BaseSelect;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;
import tests.unit.helpers.ClearAllCaches;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SelectTest extends WorldStubForTests {

    // === Our ======================================================

    @Test
    public void our() {
        world(1, () -> {
        assertEquals(22, Select.our().size());
        });
    }

    @Test
    public void ourWithUnfinished() {
        ClearAllCaches.clearAll();
        int ourTx = 10;
        int enemyTx = 30;

        FakeUnit[] ours = fakeOurs(
            fake(AUnitType.Terran_Missile_Turret, 8),
            fake(AUnitType.Terran_Wraith, 9),
            fake(AUnitType.Terran_Bunker, 10),
            fake(AUnitType.Terran_Bunker, 11).setHp(0),
            fake(AUnitType.Terran_Bunker, 12).setCompleted(false)
        );

        FakeUnit[] enemies = fakeEnemies();

        world(1, ours, enemies, () -> {
//            Select.our().print("Our");
//            Select.ourWithUnfinished().print("Our with UNF");

        // The bunker on 11 has 0 hit points: a corpse. The engine drops a unit
        // from its player's unit list the moment it dies, so no selector in a
        // game can see one - the world harness answers with the living units for
        // exactly that reason (AbstractWorldCreatingTest.living). Every count
        // below is therefore over the four living units, whether or not the
        // selector bothers to check isAlive() itself.
        assertEquals(4, Select.ourWithUnfinished().size());
        assertEquals(3, Select.ourWithUnfinished().combatBuildings(true).size());
        assertEquals(2, Select.ourWithUnfinished().bunkers().size(), "10 and 12, the corpse on 11 is gone");
        assertEquals(1, Select.ourOfType(AUnitType.Terran_Bunker).size(),
            "only the completed living bunker: 12 is still under construction, 11 is dead");
        assertEquals(1, Select.our().bunkers().size(), "same two filters, on Select.our()");
        });
    }

    @Test
    public void ourRealUnits() {
        world(1, () -> {
//            Select.our().print();
//            Select.ourRealUnits().print();
//            Select.our().minus(Select.ourRealUnits()).print("Our units that are not real units");

        assertEquals(14, Select.ourRealUnits().size());
        });
    }

    // === Enemy ======================================================

    @Test
    public void enemy() {
        world(1, () -> {
        assertEquals(enemyUnits.length, BaseSelect.enemyUnits().size());
        });
    }

    @Test
    public void enemyRealUnits() {
        world(1, () -> {
        assertEquals(enemyUnits.length, Select.enemyUnits().size());
        assertTrue(Select.enemyUnits().size() >= 2);

        assertEquals(
            0,
            Select.enemyRealUnits(false, false, false).size()
        );

//            Select.enemy().print("All enemiez, while enemUnits.size()=" + Select.enemyUnits().size());
        assertEquals(
            GROUND_UNITS,
            Select.enemyRealUnits(true, false, false).size()
        );

//            Select.enemy().realUnits().print("Real units");
//            Select.enemy().combatBuildings(true).print("COMBAT_BUILDINGS");
//            Select.enemy().realUnitsAndCombatBuildings().print("REAL_UNITS + COMBAT_BUILDINGS");

        assertEquals(
            14,
            Select.enemy().realUnitsAndCombatBuildings().size()
        );

        assertEquals(
            GROUND_UNITS + BUILDINGS,
            Select.enemyRealUnits(true, false, true).size()
        );

        assertEquals(
            AIR_UNITS,
            Select.enemyRealUnits(false, true, false).size()
        );

        assertEquals(
            REAL_UNITS,
            Select.enemyRealUnits(true, true, false).size()
        );

        assertEquals(
            REAL_UNITS + BUILDINGS,
            Select.enemyRealUnits(true, true, true).size()
        );
        });
    }

    // === Neutral ======================================================

    @Test
    public void neutralUnits() {
        neutralInWorld = mockNeutralUnits().toArray(new FakeUnit[0]);

        world(1, () -> {
        assertEquals(MINERAL_COUNT, Select.minerals().size());
        assertEquals(GEYSER_COUNT, Select.geysers().size());

        assertEquals(neutralUnits.length, Select.neutral().size());

        assertEquals(MINERAL_COUNT, Select.minerals().size());
        assertEquals(GEYSER_COUNT, Select.geysers().size());
        });
    }

    // === Adding/removing =============================================

    @Test
    public void addsUnitsAndRemovesDuplicates() {
        world(1, () -> {
        AUnit unit1 = Select.our().first();
        AUnit unit2 = Select.our().last();

        Selection selection = Select.from(new AUnit[]{unit1});
        Selection selectionB = Select.from(new AUnit[]{unit2, unit2});

        assertEquals(1, selection.size());

        selection = selection.add(selectionB);

        assertEquals(3, selection.size());

        selection = selection.removeDuplicates();

        assertEquals(2, selection.size());
        });
    }

    // === Caching =====================================================

    @Test
    public void createsCacheKeysAsExpected() {
        world(1, () -> {
        Select.clearCache();
//            Select.cache().print("hmmm", true);
        assertEquals(0, Select.cache().size());

        Select.our();

        assertEquals(1, Select.cache().size());
        assertEquals("[our]", Select.cache().rawCacheData().keySet().toString());

        Select.our().melee();
//            Select.cache().printKeys();

        assertEquals(2, Select.cache().size());
        assertEquals("[our, our:melee]", Select.cache().rawCacheData().keySet().toString());
        });
    }

}
