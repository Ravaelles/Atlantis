package tests.unit;

import atlantis.core.world.UnitIndex;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Stage F slice: per-frame queries answer from a frozen list, no TTLs.
 * Pure filtering over explicit units - the frame invalidation lives in
 * the single holder, not here.
 */
public class UnitIndexTest {

    private static List<AUnit> units() {
        List<AUnit> units = new ArrayList<>();
        units.add(fake(AUnitType.Protoss_Probe));
        units.add(fake(AUnitType.Protoss_Zealot));
        units.add(fake(AUnitType.Protoss_Dragoon));
        return units;
    }

    @Test
    public void filtersByType() {
        UnitIndex index = new UnitIndex(units());

        assertEquals(1, index.ofType(AUnitType.Protoss_Zealot).size());
        assertEquals(2, index.ofType(AUnitType.Protoss_Zealot, AUnitType.Protoss_Dragoon).size());
        assertEquals(0, index.ofType(AUnitType.Protoss_Dark_Templar).size());
    }

    @Test
    public void freezesTheList() {
        List<AUnit> live = units();
        UnitIndex index = new UnitIndex(live);
        live.add(fake(AUnitType.Protoss_Zealot));

        assertEquals(3, index.all().size());
        assertEquals(1, index.ofType(AUnitType.Protoss_Zealot).size());
    }

    @Test
    public void completedFilterSkipsUnfinished() {
        tests.fakes.FakeUnit probe = fake(AUnitType.Protoss_Probe);
        probe.setCompleted(false);
        List<AUnit> live = new ArrayList<>();
        live.add(probe);
        live.add(fake(AUnitType.Protoss_Zealot));

        UnitIndex index = new UnitIndex(live);

        assertEquals(1, index.ofType(AUnitType.Protoss_Probe).size());
        assertEquals(0, index.completedOfType(AUnitType.Protoss_Probe).size());
        assertEquals(1, index.completedOfType(AUnitType.Protoss_Zealot).size());
    }

    @Test
    public void matchingFiltersByPredicate() {
        UnitIndex index = new UnitIndex(units());

        assertEquals(1, index.matching(unit -> unit.is(AUnitType.Protoss_Dragoon)).size());
        assertEquals(3, index.matching(unit -> unit.isCompleted()).size());
        assertEquals(0, index.matching(unit -> false).size());
    }

    @Test
    public void allReturnsACopy() {        UnitIndex index = new UnitIndex(units());

        assertEquals(3, index.all().size());
        index.all().clear();

        assertEquals(3, index.all().size());
    }
}
