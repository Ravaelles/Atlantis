package tests.unit.sim;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.production.v2.engine.ProductionEngine;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.BaseSelect;
import org.mockito.Mockito;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeOrderSink;
import tests.fakes.FakeUnit;

import java.util.*;

/**
 * The economy half of StarEngine: owns the sim universe, ticks the
 * clock, feeds the bot's numbers, consumes its orders, and advances
 * the world the engine left behind.
 *
 * <p>One frame, in full: mine income → publish stocks/supply → run the
 * REAL {@code ProductionEngine.updateFrame} → consume the order sink
 * (validate every order, accept funded ones) → advance constructions,
 * training and research → move builders → reassign miners.</p>
 *
 * <p>The driver never decides <em>what</em> to build - that is the bot,
 * running unmodified through its own seams. It only decides what the
 * engine would do with the order (accept/reject), deducts the bank,
 * walks the builder, and spawns the finished unit. Dynamics live here,
 * never in the snapshot.</p>
 */
public final class SimDriver {

    /**
     * Frames a fresh miner walks before the first payout.
     */
    static final int FIRST_TRIP_FRAMES = 120;

    /**
     * Builder arrival radius, in pixels.
     */
    private static final double ARRIVE_PX = 48.0;
    private final SimMap map;
    private final SimGround ground;
    private final SimConfig config;
    private final SimFields fields;
    private final SimUnitRegistry units = new SimUnitRegistry();
    private final SimBank bank;
    private final SimMining mining = new SimMining();
    private final SimConstructions constructions = new SimConstructions();
    private final ProductionEngine engine = new ProductionEngine();
    private final SimMovement movement;
    private final SimTechSource techSource = new SimTechSource();
    private final Map<Integer, Integer> sinkCursor = new HashMap<>();
    private final List<WarpRecord> warpLog = new ArrayList<>();
    private final List<SimOrder> rejectedOrders = new ArrayList<>();
    private final List<String> cancels = new ArrayList<>();
    private int trainSeen;
    private int fundingRefusals;
    private int frame;
    private boolean gasAssigned;

    public SimDriver(SimMap map, SimConfig config, SimFields fields, int startMinerals) {
        this.map = map;
        this.ground = new SimGround(map.snap);
        this.config = config;
        this.fields = fields;
        this.bank = new SimBank(startMinerals, 0);
        this.movement = new SimMovement(tileArena(map.snap));
        tests.unit.helpers.ClearAllCaches.clearQueries();
        // The driver runs outside buildWorld, which is the only harness
        // path that initializes the strategy (setUpTestLogic) - without
        // this every placement sweep dies on Strategy.get(). Same call
        // the harness makes, Protoss-only like the whole scenario.
        atlantis.game.listeners.OnGameStarted.initializeAllStrategies();
        atlantis.information.strategy.Strategy.setTo(
            atlantis.information.strategy.protoss.ProtossStrategies.PROTOSS_Zealot_into_Goon);
        tests.fakes.FakeUnitData.TRAIN.clear();
        tests.fakes.FakeUnitData.CANCEL.clear();
        trainSeen = tests.fakes.FakeUnitData.TRAIN.size();
        installSelection();
        atlantis.information.tech.ATech.useSource(techSource);
    }

    private static bwapi.UpgradeType upgradeOf(String typeName) {
        try {
            return bwapi.UpgradeType.valueOf(typeName);
        } catch (RuntimeException | Error ignored) {
            return null;
        }
    }

    private static bwapi.TechType techOf(String typeName) {
        try {
            return bwapi.TechType.valueOf(typeName);
        } catch (RuntimeException | Error ignored) {
            return null;
        }
    }

    // =========================================================
    // Universe setup (scenario use).

    private static List<AUnit> living(List<FakeUnit> list) {
        List<AUnit> alive = new ArrayList<>();
        if (list == null) return alive;

        for (FakeUnit unit : list) {
            if (unit == null || unit.hp <= 0) continue;
            if (!unit.isAlive()) continue;
            alive.add(unit);
        }

        return alive;
    }

    private static SimArena tileArena(atlantis.map.snapshot.MapSnapshot snap) {
        SimArena arena = new SimArena(snap.w * 32, snap.h * 32);
        for (int ty = 0; ty < snap.h; ty++) {
            String row = snap.tiles.get(ty);
            int runStart = -1;
            for (int tx = 0; tx <= snap.w; tx++) {
                boolean blocked = tx < snap.w && tx < row.length() && row.charAt(tx) == 'x';
                if (blocked && runStart < 0) runStart = tx;
                if (!blocked && runStart >= 0) {
                    arena.block(runStart * 32, ty * 32, (tx - runStart) * 32, 32);
                    runStart = -1;
                }
            }
        }

        return arena;
    }

    public SimUnitRegistry units() {
        return units;
    }

    public SimGround ground() {
        return ground;
    }

    public SimBank bank() {
        return bank;
    }

    public SimConfig config() {
        return config;
    }

    public SimMap map() {
        return map;
    }

    // =========================================================
    // One frame.

    /**
     * Spawns the opening: Nexus on the base tile, workers on the line.
     */
    public FakeUnit spawnBase(int baseIndex, int workers) {
        MapSnapshotRef base = baseRef(baseIndex);
        FakeUnit nexus = units.spawn(AUnitType.Protoss_Nexus, base.x, base.y);
        List<int[]> slots = mineralSlots(baseIndex, workers);
        for (int i = 0; i < workers; i++) {
            int[] slot = slots.get(i % slots.size());
            FakeUnit probe = units.spawn(AUnitType.Protoss_Probe, slot[0], slot[1]);
            probe.setAction(atlantis.units.actions.Actions.GATHER_MINERALS);
            mining.assign(probe, SimMining.Job.MINING, 0);
        }

        return nexus;
    }

    /**
     * Neutral minerals + geysers, exactly as the snapshot stores them.
     */
    public void spawnResources() {
        for (FakeUnit mineral : map.minerals()) units.addNeutral(mineral);
        for (FakeUnit geyser : map.geysers()) units.addNeutral(geyser);
    }

    public void step() {
        frame++;
        A.setNow(frame, frame / 30);

        mining.earn(bank, units, frame, bases());
        syncFields();

        engine.updateFrame();

        consumeSink();
        constructions.advance(new SimFrame(this, frame));
        if (!config.isFast()) movement.stepAll(new ArrayList<FakeUnit>(units.ours()));
        syncFields();
    }

    public int frame() {
        return frame;
    }

    public List<WarpRecord> warpLog() {
        return new ArrayList<>(warpLog);
    }

    public List<SimOrder> rejectedOrders() {
        return new ArrayList<>(rejectedOrders);
    }

    /**
     * Proximity-finish cancels (execution, not validation - cf. live twin rows).
     */
    public List<String> cancels() {
        return new ArrayList<>(cancels);
    }

    void logCancel(AUnitType type, APosition tile, String reason) {
        cancels.add("f" + frame + " cancel " + type.name()
            + " [" + tile.tx() + "," + tile.ty() + "] " + reason);
    }

    /**
     * Rejections that are NOT funding (those are the real findings).
     */
    public List<SimOrder> invalidOrders() {
        List<SimOrder> out = new ArrayList<>();
        for (SimOrder order : rejectedOrders) {
            if (!order.reason.startsWith("bank cannot cover")) out.add(order);
        }

        return out;
    }

    // =========================================================
    // Driver hooks used by the engines.

    public int fundingRefusals() {
        return fundingRefusals;
    }

    public void close() {
        if (AbstractTestWithWorld.baseSelect != null) {
            AbstractTestWithWorld.baseSelect.close();
            AbstractTestWithWorld.baseSelect = null;
        }
        tests.fakes.FakeUnitData.TRAIN.clear();
        tests.fakes.FakeUnitData.CANCEL.clear();
        atlantis.information.tech.ATech.useEngine();
        SimMap.uninstall();
    }

    FakeUnit unitById(int id) {
        return units.byId(id);
    }

    List<FakeUnit> completedBuildings() {
        List<FakeUnit> out = new ArrayList<>();
        for (FakeUnit unit : units.ours()) {
            if (unit != null && unit.isAlive() && unit.isCompleted() && unit.type().isABuilding()) {
                out.add(unit);
            }
        }

        return out;
    }

    FakeUnit assignBuilder(atlantis.map.position.HasPosition site) {
        List<FakeUnit> free = mining.freeProbes(units, 100);
        FakeUnit best = null;
        double bestDist = Double.MAX_VALUE;
        for (FakeUnit probe : free) {
            try {
                double dist = probe.position().distTo(site);
                if (dist < bestDist) {
                    bestDist = dist;
                    best = probe;
                }
            } catch (RuntimeException ignored) {
            }
        }
        if (best != null) mining.assign(best, SimMining.Job.BUILDING, frame);

        return best;
    }

    void freeBuilder(FakeUnit builder) {
        if (builder == null || !builder.isAlive()) return;

        builder.constructing = false;
        builder.lastCommand = "None";
        mining.assign(builder, SimMining.Job.MINING, frame);
    }

    void moveBuilderToward(SimFrame simFrame, FakeUnit builder, APosition dest) {
        if (builder == null || dest == null) return;

        if (config.isFast()) {
            stepToward(builder, dest);
        }
        else if (!"Move".equals(builder.lastCommand)) {
            builder.lastCommand = "Move";
            builder.targetPosition = dest;
        }
    }

    FakeUnit spawnWarpBuilding(AUnitType type, APosition tile) {
        FakeUnit building = units.spawn(type, tile.tx(), tile.ty());
        building.setCompleted(false);
        building.simRemainingBuildTime = type.totalTrainTime();

        return building;
    }

    void logWarp(AUnitType type, APosition tile) {
        warpLog.add(new WarpRecord(frame, "building", type.name(),
            tile.tx(), tile.ty(), bank.minerals(), supplyUsed(), supplyTotal()));
    }

    private FakeUnit freeFacilityFor(AUnitType type) {
        AUnitType producer = type.whatBuildsIt();
        if (producer == null) return null;

        for (FakeUnit unit : units.ours()) {
            if (unit == null || !unit.isAlive() || !unit.isCompleted()) continue;
            if (!producer.equals(unit.type())) continue;
            if (constructions.isTraining(unit.id())) continue;

            return unit;
        }

        return null;
    }

    void logTrain(AUnitType type, FakeUnit facility) {
        APosition at = facility.position();
        warpLog.add(new WarpRecord(frame, "unit", type.name(),
            at.tx(), at.ty(), bank.minerals(), supplyUsed(), supplyTotal()));
    }

    void logRejected(SimOrder order) {
        rejectedOrders.add(order);
        if (order.reason.startsWith("bank cannot cover")) fundingRefusals++;
    }

    void completeTraining(int facilityId, AUnitType type) {
        FakeUnit facility = unitById(facilityId);
        APosition at = facility != null && facility.position() != null
            ? facility.position() : APosition.create(0, 0);
        FakeUnit unit = units.spawn(type, at.tx() + 1, at.ty());
        units.releaseSupply(type.ut().supplyRequired());
        if (facility != null) facility.setTrainingQueue(new ArrayList<AUnitType>());
        if (type.isWorker()) {
            unit.setAction(atlantis.units.actions.Actions.GATHER_MINERALS);
            mining.assign(unit, SimMining.Job.MINING, frame);
        }
    }

    // =========================================================

    void completeResearch(int facilityId, boolean isUpgrade, String name) {
        FakeUnit facility = unitById(facilityId);
        if (facility != null) {
            facility.researching = null;
            facility.upgrading = null;
        }
        techSource.complete(name);
    }

    void onBuildingCompleted(AUnitType type) {
        if (type.isGasBuilding() && !gasAssigned) {
            gasAssigned = true;
            List<FakeUnit> miners = mining.freeProbes(units, 3);
            for (FakeUnit probe : miners) {
                mining.assign(probe, SimMining.Job.GAS, frame);
            }
        }
    }

    private void syncFields() {
        fields.setMinerals(bank.minerals());
        fields.setGas(bank.gas());
        fields.setSupply(supplyUsed(), supplyTotal());
    }

    private int supplyUsed() {
        return units.supplyUsed();
    }

    private int supplyTotal() {
        return units.supplyTotal();
    }

    private int bases() {
        int count = 0;
        for (FakeUnit unit : units.ours()) {
            if (unit != null && unit.isAlive() && unit.isCompleted() && unit.type().isBase()) count++;
        }

        return Math.max(1, count);
    }

    private void consumeSink() {
        for (FakeUnit unit : units.ours()) {
            if (unit == null) continue;
            FakeOrderSink sink = (FakeOrderSink) unit.orderSink();
            if (sink == null) continue;
            List<FakeOrderSink.Record> records = sink.orders();
            int from = sinkCursor.getOrDefault(unit.id(), 0);
            for (int i = from; i < records.size(); i++) {
                consumeRecord(unit, records.get(i));
            }
            sinkCursor.put(unit.id(), records.size());
        }
        // trainForced in the stub never reaches the sink: OrderFallback
        // appends to the global list instead (processTrain is the engine
        // path). The sim consumes the delta and attributes each unit to
        // a free facility of its producer type, mirroring the director.
        List<AUnitType> trained = tests.fakes.FakeUnitData.TRAIN;
        for (int i = trainSeen; i < trained.size(); i++) {
            consumeTrain(trained.get(i));
        }
        trainSeen = trained.size();
    }

    private void consumeRecord(FakeUnit unit, FakeOrderSink.Record record) {
        if ("research".equals(record.operation)) {
            consumeResearch(unit, record.detail, false);
        }
        else if ("upgrade".equals(record.operation)) {
            consumeResearch(unit, record.detail, true);
        }
    }

    private void consumeTrain(AUnitType type) {
        if (type == null) return;

        FakeUnit facility = freeFacilityFor(type);
        if (facility == null || constructions.isTraining(facility.id())) return;

        SimOrder verdict = SimOrderValidator.validateTrain(facility, type, bank);
        if (!verdict.accepted()) {
            logRejected(verdict);

            return;
        }
        bank.spend(type.mineralPrice(), type.gasPrice(), "train " + type.name());
        units.reserveSupply(type.ut().supplyRequired());
        facility.setTrainingQueue(java.util.Collections.singletonList(type));
        constructions.startTraining(facility.id(), type);
        logTrain(type, facility);
    }

    private void consumeResearch(FakeUnit facility, String typeName, boolean isUpgrade) {
        if (constructions.isResearching(facility.id())) return;

        bwapi.UpgradeType upgrade = isUpgrade ? upgradeOf(typeName) : null;
        bwapi.TechType tech = !isUpgrade ? techOf(typeName) : null;
        if (upgrade == null && tech == null) {
            logRejected(SimOrder.rejected(isUpgrade ? SimOrder.Verb.UPGRADE : SimOrder.Verb.RESEARCH,
                facility, null, null, "unknown research " + typeName));

            return;
        }
        int costMinerals = isUpgrade ? upgrade.mineralPrice() : tech.mineralPrice();
        int costGas = isUpgrade ? upgrade.gasPrice() : tech.gasPrice();
        SimOrder verdict = SimOrderValidator.validateResearch(
            facility, null, bank, costMinerals, costGas, isUpgrade);
        if (!verdict.accepted()) {
            logRejected(verdict);

            return;
        }
        bank.spend(costMinerals, costGas, "research " + typeName);
        if (isUpgrade) facility.upgrading = upgrade;
        else facility.researching = tech;
        int frames = isUpgrade ? upgrade.upgradeTime() : tech.researchTime();
        constructions.startResearch(facility.id(), isUpgrade, typeName, frames);
    }

    private void stepToward(FakeUnit unit, APosition dest) {
        double speed;
        try {
            speed = unit.maxSpeed();
        } catch (RuntimeException e) {
            speed = 4.0;
        }
        double dx = dest.x() - unit.position.x();
        double dy = dest.y() - unit.position.y();
        double dist = Math.hypot(dx, dy);
        if (dist < 1.0) return;

        double step = Math.min(dist, speed);
        unit.position = unit.position.translateByPixels(
            (int) Math.round(dx / dist * step), (int) Math.round(dy / dist * step));
    }

    // =========================================================

    private void installSelection() {
        if (AbstractTestWithWorld.baseSelect != null) {
            AbstractTestWithWorld.baseSelect.close();
            AbstractTestWithWorld.baseSelect = null;
        }
        AbstractTestWithWorld.baseSelect = Mockito.mockStatic(BaseSelect.class);
        AbstractTestWithWorld.baseSelect.when(BaseSelect::ourUnitsWithUnfinishedList)
            .thenAnswer(invocation -> living(units.ours()));
        AbstractTestWithWorld.baseSelect.when(BaseSelect::enemyUnits)
            .thenAnswer(invocation -> living(units.enemies()));
        AbstractTestWithWorld.baseSelect.when(BaseSelect::neutralUnits)
            .thenAnswer(invocation -> living(units.neutral()));
        AbstractTestWithWorld.baseSelect.when(BaseSelect::allUnits).thenAnswer(invocation -> {
            List<AUnit> alive = new ArrayList<>(living(units.ours()));
            alive.addAll(living(units.enemies()));
            alive.addAll(living(units.neutral()));

            return alive;
        });
    }

    private MapSnapshotRef baseRef(int baseIndex) {
        atlantis.map.snapshot.MapSnapshot.Base base = map.snap.bases.get(baseIndex);
        MapSnapshotRef ref = new MapSnapshotRef();
        ref.x = base.x;
        ref.y = base.y;

        return ref;
    }

    private List<int[]> mineralSlots(int baseIndex, int workers) {
        atlantis.map.snapshot.MapSnapshot.Base base = map.snap.bases.get(baseIndex);
        List<int[]> slots = new ArrayList<>();
        for (int id : base.mineralIds) {
            atlantis.map.snapshot.MapSnapshot.Res res = map.snap.resources.get(id);
            slots.add(new int[]{res.x, res.y});
        }
        if (slots.isEmpty()) slots.add(new int[]{base.x + 3, base.y});

        return slots;
    }

    /**
     * Test-side number feeds (the harness fields behind A.minerals() etc).
     */
    public interface SimFields {
        void setMinerals(int minerals);

        void setGas(int gas);

        void setSupply(int used, int total);
    }

    /**
     * Warp/complete log entry, in Wine BUILDINGS.json spirit.
     */
    public static final class WarpRecord {
        public final int frame;
        public final String kind;
        public final String type;
        public final int tx;
        public final int ty;
        public final int bank;
        public final int supplyUsed;
        public final int supplyTotal;

        WarpRecord(int frame, String kind, String type, int tx, int ty,
                   int bank, int supplyUsed, int supplyTotal) {
            this.frame = frame;
            this.kind = kind;
            this.type = type;
            this.tx = tx;
            this.ty = ty;
            this.bank = bank;
            this.supplyUsed = supplyUsed;
            this.supplyTotal = supplyTotal;
        }

        @Override
        public String toString() {
            return "f" + frame + " " + kind + " " + type
                + " [" + tx + "," + ty + "] bank=" + bank
                + " supply=" + supplyUsed + "/" + supplyTotal;
        }
    }

    private static final class MapSnapshotRef {
        int x;
        int y;
    }

    /**
     * ATech answers from sim completions, not the engine.
     */
    private static final class SimTechSource implements atlantis.information.tech.ATech.Source {
        private final Set<String> done = new HashSet<>();

        void complete(String name) {
            done.add(name);
        }

        @Override
        public boolean hasResearched(bwapi.TechType tech) {
            return tech != null && done.contains(tech.toString());
        }

        @Override
        public int upgradeLevel(bwapi.UpgradeType upgrade) {
            return upgrade != null && done.contains(upgrade.toString()) ? 1 : 0;
        }
    }
}
