package tests.unit;

import atlantis.map.position.APosition;
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

public class SelectTest extends WorldStubForTests {

    // === Our ======================================================

    @Test
    public void our() {
        usingFakeOurs(() -> {
            assertEquals(ourUnits.length, Select.our().size());
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

        createWorld(ours, enemies, () -> {
//            Select.our().print("Our");
//            Select.ourWithUnfinished().print("Our with UNF");

            assertEquals(4, Select.ourWithUnfinished().size());
            assertEquals(3, Select.ourWithUnfinished().combatBuildings(true).size());
            assertEquals(2, Select.ourWithUnfinished().bunkers().size());
            assertEquals(2, Select.ourOfType(AUnitType.Terran_Bunker).size());
            assertEquals(2, Select.our().bunkers().size());
        });
    }

    @Test
    public void ourRealUnits() {
        usingFakeOurs(() -> {
//            Select.our().print();
//            Select.ourRealUnits().print();
//            Select.our().minus(Select.ourRealUnits()).print("Our units that are not real units");

            assertEquals(14, Select.ourRealUnits().size());
        });
    }

    // === Enemy ======================================================

    @Test
    public void enemy() {
        usingFakeEnemy(() -> {
            assertEquals(enemyUnits.length, BaseSelect.enemyUnits().size());
        });
    }

    @Test
    public void enemyRealUnits() {
        usingFakeEnemy(() -> {
            assertEquals(enemyUnits.length, Select.enemyUnits().size());

            assertEquals(
                0,
                Select.enemyRealUnits(false, false, false).size()
            );

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
        usingFakeNeutral(() -> {
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
        usingFakeOurs(() -> {
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
        usingFakeOurs(() -> {
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
