package tests.fakes;

import atlantis.information.tech.ATech;
import bwapi.TechType;
import bwapi.UpgradeType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * What the stub world's player has researched: nothing, unless a test says
 * otherwise.
 *
 * <p>This replaces {@code Mockito.mockStatic(ATech.class)}, which mocked the whole
 * facade rather than the two questions the engine could not answer. Every
 * unstubbed method of that mock returned null or 0, so tests ran against
 * {@code getCurrentlyResearching() == null} and {@code costOf(...) == null} and
 * never noticed, because nothing asserted them.</p>
 *
 * <p>A test declares the tech tree next to the units it declares:</p>
 * <pre>
 *     FakeResearch.withResearched(TechType.Lockdown);
 *     FakeResearch.withUpgradeLevel(UpgradeType.Terran_Infantry_Weapons, 2);
 * </pre>
 */
public class FakeResearch implements ATech.Source {
    private static FakeResearch current = null;

    private final Set<TechType> researched = new HashSet<>();
    private final Map<UpgradeType, Integer> upgradeLevels = new HashMap<>();

    public static void installAsSource() {
        current = new FakeResearch();
        ATech.useSource(current);
    }

    public static FakeResearch current() {
        return current;
    }

    // =========================================================
    // What a test says about the world
    // =========================================================

    public static FakeResearch withResearched(TechType... techs) {
        for (TechType tech : techs) {
            current.researched.add(tech);
        }
        return current;
    }

    public static FakeResearch withUpgradeLevel(UpgradeType upgrade, int level) {
        current.upgradeLevels.put(upgrade, level);
        return current;
    }

    public static FakeResearch withoutResearch() {
        current.researched.clear();
        current.upgradeLevels.clear();
        return current;
    }

    // =========================================================

    @Override
    public boolean hasResearched(TechType tech) {
        return tech != null && researched.contains(tech);
    }

    @Override
    public int upgradeLevel(UpgradeType upgrade) {
        Integer level = upgradeLevels.get(upgrade);
        return level != null ? level : 0;
    }
}
