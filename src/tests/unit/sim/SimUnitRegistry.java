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

    /**
     * Supply locked by training queues (engine counts units in production).
     */
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

    /**
     * Spawns a finished unit on a tile and registers it.
     */
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

    /**
     * Raw used supply, in BWAPI units: live units plus training reservations.
     */
    public int supplyUsedRaw() {
        int used = reservedSupply;
        for (FakeUnit unit : ours) {
            if (unit == null || !unit.isAlive() || !unit.isCompleted()) continue;
            used += unit.type().ut().supplyRequired();
        }

        return used;
    }

    /**
     * Used supply the way the bot reads it: halved, exactly like
     * {@code AGame.supplyUsed()} ({@code self().supplyUsed() / 2}).
     * The sim feeds this into the game-state seam, so build-order gates
     * below see the same numbers as in a live game - raw counts here
     * opened every row at half the intended army.
     */
    public int supplyUsed() {
        return supplyUsedRaw() / 2;
    }

    /**
     * Raw total supply: completed providers only, exactly like the engine.
     */
    public int supplyTotalRaw() {
        int total = 0;
        for (FakeUnit unit : ours) {
            if (unit == null || !unit.isAlive() || !unit.isCompleted()) continue;
            total += unit.type().ut().supplyProvided();
        }

        return total;
    }

    /**
     * Total supply the way the bot reads it: halved, like {@code AGame}.
     */
    public int supplyTotal() {
        return supplyTotalRaw() / 2;
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
