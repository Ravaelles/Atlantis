package tests.benchmark;

import atlantis.combat.CombatUnitManager;
import atlantis.units.AUnitType;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;


/**
 * Stage J: deterministic cost of one frame of combat decisions for a fixed
 * stub world. Not a JUnit test — run via {@code scripts/benchmark-trees.sh}.
 * Prints nanoseconds per frame per unit; compare across revisions, never
 * gate CI on absolute numbers.
 *
 * <p>Measures construction + traversal of the per-unit manager trees (the
 * exact path Stage C de-reflected), driven through the same stub world the
 * acceptance tests use.</p>
 */
public class TreeConstructionBenchmark extends WorldStubForTests {

    public static void main(String[] args) {
        new TreeConstructionBenchmark().run();
    }

    private void run() {
        // Same scaffolding JUnit @BeforeEach provides (mocks, fake time,
        // testing env); without it the world cannot be built standalone.
        setUp();

        FakeUnit[] ours = new FakeUnit[12];
        for (int i = 0; i < ours.length; i++) {
            ours[i] = new FakeUnit(AUnitType.Terran_Marine, 20 + i, 20);
        }
        FakeUnit[] enemies = new FakeUnit[] {
            new FakeUnit(AUnitType.Zerg_Zergling, 40, 20).setEnemy()
        };

        // Warmup so JIT settles before measuring.
        world(30, ours, enemies, () -> invokeForAll(ours));

        long best = Long.MAX_VALUE;
        int frames = 20;
        for (int round = 0; round < 5; round++) {
            long start = System.nanoTime();
            world(frames, ours, enemies, () -> invokeForAll(ours));
            long took = System.nanoTime() - start;
            if (took < best) best = took;
        }

        System.out.println("frames=" + (frames * 5) + " units=" + ours.length
            + " best-ns-per-frame-per-unit=" + (best / 5 / frames / ours.length));
    }

    private static void invokeForAll(FakeUnit[] ours) {
        for (FakeUnit unit : ours) {
            new CombatUnitManager(unit).invokeFrom(null);
        }
    }
}
