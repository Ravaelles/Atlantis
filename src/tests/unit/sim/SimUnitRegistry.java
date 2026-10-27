package tests.unit.sim;

import atlantis.units.AUnitType;
import tests.fakes.FakeUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The sim's live unit lists: ours, enemies, neutrals, plus spawn and
 * supply answers. The {@code BaseSelect} mock reads these same lists,
 * so the bot and the sim never disagree about what exists.
 *
 * <p>Supply is raw BWAPI units (Probe 2, Zealot 4, Nexus 18, Pylon 16):
 * the same numbers the build order gates on ("8 - Pylon"). Training
 * reservations count as used - the engine counts units in production,
 * and so does the sim.</p>
 */
public final class SimUnitRegistry {

    private final List<FakeUnit> ours = new ArrayList<>();
    private final List<FakeUnit> enemies = new ArrayList<>();
    private final List<FakeUnit> neutral = new ArrayList<>();
    private final Map<Integer, FakeUnit> byId = new HashMap<>();

    /** Supply locked by training queues (engine counts units in production). */
    private int reservedSupply;

    public List<FakeUnit> ours() {
        return ours;
    }

    public List<FakeUnit> enemies() {
        return enemies;
    }

    public List<FakeUnit> neutral() {
        return neutral;
    }

    public void addOur(FakeUnit unit) {
        ours.add(unit);
        byId.put(unit.id(), unit);
    }

    public void addNeutral(FakeUnit unit) {
        neutral.add(unit);
        byId.put(unit.id(), unit);
    }

    public FakeUnit byId(int id) {
        return byId.get(id);
    }

    /** Spawns a finished unit on a tile and registers it. */
    public FakeUnit spawn(AUnitType type, int tx, int ty) {
        FakeUnit unit = new FakeUnit(type, tx, ty);
        unit.setCompleted(true);
        addOur(unit);

        return unit;
    }

    public void reserveSupply(int raw) {
        reservedSupply += raw;
    }

    public void releaseSupply(int raw) {
        reservedSupply = Math.max(0, reservedSupply - raw);
    }

    /** Raw used supply: live units plus training reservations. */
    public int supplyUsed() {
        int used = reservedSupply;
        for (FakeUnit unit : ours) {
            if (unit == null || !unit.isAlive() || !unit.isCompleted()) continue;
            used += unit.type().ut().supplyRequired();
        }

        return used;
    }

    /** Raw total supply: completed providers only, exactly like the engine. */
    public int supplyTotal() {
        int total = 0;
        for (FakeUnit unit : ours) {
            if (unit == null || !unit.isAlive() || !unit.isCompleted()) continue;
            total += unit.type().ut().supplyProvided();
        }

        return total;
    }

    public int completedCount(AUnitType type) {
        int count = 0;
        for (FakeUnit unit : ours) {
            if (unit != null && unit.isAlive() && unit.isCompleted() && type.equals(unit.type())) count++;
        }

        return count;
    }

    public List<FakeUnit> completedOfType(AUnitType type) {
        List<FakeUnit> out = new ArrayList<>();
        for (FakeUnit unit : ours) {
            if (unit != null && unit.isAlive() && unit.isCompleted() && type.equals(unit.type())) out.add(unit);
        }

        return out;
    }
}
