package tests.unit;

import atlantis.Atlantis;
import atlantis.config.AtlantisRaceConfig;
import atlantis.config.env.Env;
import atlantis.debug.painter.APainter;
import atlantis.game.AGame;
import atlantis.game.listeners.OnGameStarted;
import atlantis.game.player.Enemy;
import atlantis.game.race.EnemyRace;
import atlantis.information.strategy.AStrategy;
import atlantis.information.strategy.Strategy;
import atlantis.information.strategy.terran.TerranStrategies;
import atlantis.information.tech.ATech;
import atlantis.map.base.AllBaseLocations;
import atlantis.map.choke.AllChokes;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.units.AUnitType;
import atlantis.units.fogged.FakeFoggedUnit;
import atlantis.units.select.BaseSelect;
import atlantis.util.Options;
import atlantis.util.cache.Cache;
import bwapi.Game;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoException;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;
import tests.unit.helpers.ClearAllCaches;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class AbstractTestWithUnits extends UnitTest {
    public Game game;

    protected int currentMinerals = 0;
    protected int currentGas = 0;
    protected int currentSupplyUsed = 0;
    protected int currentSupplyTotal = 0;

    public static MockedStatic<Env> env;
    public static MockedStatic<AGame> aGame;
    public static MockedStatic<ATech> aTech;
    public static MockedStatic<Enemy> enemy;
    public static MockedStatic<EnemyRace> enemyRace;
    public static MockedStatic<AllBaseLocations> allBaseLocations;
    public static MockedStatic<AllChokes> allChokes;

    protected Options options = new Options();

    // =========================================================

    @BeforeEach
    public void setUp() {
        Env.markIsTesting(true);
        Env.readEnvFile(new String[]{});

        clearCaches();

        if (!(this instanceof AbstractTestWithWorld)) {
            useFakeTime(0); // This needs to be 0 so every modulo division returns 0
        }

        (new MockEverything(this)).mockEverything();
//        HeuristicCombatEvaluator.clearCache();

        init();
    }

    public void init() {
    }

    private static void clearCaches() {
        APainter.disablePainting();

        ClearAllCaches.clearAll();
    }

    public Race initRace() {
        return MockEverything.defaultRaceForTests();
    }

    protected void setUpTestLogic() {
        AtlantisRaceConfig.MY_RACE = MockEverything.defaultRaceForTests();

        if (AtlantisRaceConfig.MY_RACE == null) {
            AtlantisRaceConfig.MY_RACE = MockEverything.defaultRaceForTests();
        }

        setUpBuildOrder();
        setUpStrategy();
    }

    @AfterEach
    public void tearDown() {
        cleanUp();
    }

    /**
     * PROPERTIES HAVE TO BE PUBLIC FOR THIS TO WORK.
     */
    protected void cleanUp() {
        AbstractPositionFinder._STATUS = "Init";
        ConstructionRequests.constructions.clear();

        // Close static mocks - PROPERTIES HAVE TO BE PUBLIC FOR THIS TO WORK
        for (Field field : getClass().getFields()) {
            if (field.getType().toString().contains("MockedStatic")) {
                try {
                    Object object = field.get(this);
                    if (object != null) {
                        ((MockedStatic) object).reset();
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Something went wrong here");
                } catch (MockitoException e) {
                    // Mock already closed, that's ok
                }
            }
        }

        Cache.nukeAllCaches();

        ReservedResources.reset();

        game = null;
        Atlantis.getInstance().setGame(null);
    }

    // =========================================================

    protected int currentMinerals() {
        return currentMinerals;
    }

    protected int currentGas() {
        return currentGas;
    }

    protected int currentSupplyUsed() {
        return currentSupplyUsed;
    }

    protected int currentSupplyTotal() {
        return currentSupplyTotal;
    }

    protected int currentSupplyFree() {
        return currentSupplyTotal - currentSupplyUsed;
    }

    public AStrategy initBuildOrder() {
        return TerranStrategies.TERRAN_Tests;
    }

    protected void setUpBuildOrder() {
        OnGameStarted.initializeAllStrategies();

        try {
            OnGameStarted.initStrategyAndBuildOrder();
        } catch (RuntimeException e) {
            // Ignore
        }
    }

    protected void setUpStrategy() {
        Strategy.setTo(initBuildOrder());
    }

    // =========================================================

    protected void useFakeTime(int framesNow) {
        game = Atlantis.game() == null ? newGameMock(framesNow) : Atlantis.game();

        when(game.getFrameCount()).thenReturn(framesNow);

        if (Atlantis.game() == null) {
            Atlantis.getInstance().setGame(game);
        }
    }

    protected void usingFakeOurs(Runnable runnable) {
        if (AbstractTestWithWorld.baseSelect != null) {
            AbstractTestWithWorld.baseSelect.close();
            AbstractTestWithWorld.baseSelect = null;
        }
        AbstractTestWithWorld.baseSelect = Mockito.mockStatic(BaseSelect.class);
        AbstractTestWithWorld.baseSelect.when(BaseSelect::ourUnitsWithUnfinishedList).thenReturn(mockOurUnits());

        runnable.run();
    }

    protected void usingFakeEnemy(Runnable runnable) {
        if (AbstractTestWithWorld.baseSelect != null) {
            AbstractTestWithWorld.baseSelect.close();
            AbstractTestWithWorld.baseSelect = null;
        }
        AbstractTestWithWorld.baseSelect = Mockito.mockStatic(BaseSelect.class);
        AbstractTestWithWorld.baseSelect.when(BaseSelect::enemyUnits).thenReturn(mockEnemyUnits());

        runnable.run();
    }

    protected void usingFakeNeutral(Runnable runnable) {
        List<FakeUnit> neutral = mockNeutralUnits();
        usingFakeOursEnemiesAndNeutral(new FakeUnit[]{}, new FakeUnit[]{}, neutral.toArray(new FakeUnit[0]), runnable);
    }

    public void usingFakeOursAndFakeEnemies(FakeUnit[] ours, FakeUnit[] enemies, Runnable runnable) {
        usingFakeOursEnemiesAndNeutral(ours, enemies, new FakeUnit[]{}, runnable);
    }

    protected void usingFakeOurAndFakeEnemies(FakeUnit our, FakeUnit[] enemies, Runnable runnable) {
        usingFakeOursEnemiesAndNeutral(new FakeUnit[]{our}, enemies, new FakeUnit[]{}, runnable);
    }

    protected void usingFakeOursEnemiesAndNeutral(
        FakeUnit[] ours, FakeUnit[] enemies, FakeUnit[] neutral, Runnable runnable
    ) {
        setUp();

        if (AbstractTestWithWorld.baseSelect != null) {
            AbstractTestWithWorld.baseSelect.close();
            AbstractTestWithWorld.baseSelect = null;
        }
        AbstractTestWithWorld.baseSelect = Mockito.mockStatic(BaseSelect.class);

//        try (MockedStatic<BaseSelect> baseSelect = AbstractTestWithWorld.baseSelect = Mockito.mockStatic(BaseSelect.class)) {
        AbstractTestWithWorld.baseSelect.when(BaseSelect::ourUnitsWithUnfinishedList).thenReturn(Arrays.asList(ours));
        AbstractTestWithWorld.baseSelect.when(BaseSelect::enemyUnits).thenReturn(Arrays.asList(enemies));
        AbstractTestWithWorld.baseSelect.when(BaseSelect::neutralUnits).thenReturn(Arrays.asList(neutral));

        setUpTestLogic();

        runnable.run();
//        }
    }

    public static FakeUnit fake(AUnitType type) {
        return new FakeUnit(type, 10, 10);
    }

    public static FakeUnit fake(AUnitType type, double tx) {
        return new FakeUnit(type, tx, 10);
    }

    public static FakeUnit fakeEnemy(AUnitType type, double tx) {
        return new FakeUnit(type, tx, 10).setEnemy();
    }

    public static FakeUnit fakeEnemy(AUnitType type, double tx, double ty) {
        return new FakeUnit(type, tx, ty).setEnemy();
    }

    public static FakeUnit fake(AUnitType type, double tx, double ty) {
        return new FakeUnit(type, tx, ty);
    }

    public static FakeUnit[] fakeOurs(FakeUnit... fakeUnits) {
        return (FakeUnit[]) fakeUnits;
    }

    public static FakeUnit[] fakeEnemies(FakeUnit... fakeUnits) {
        for (FakeUnit unit : fakeUnits) {
            unit.setEnemy();
        }
        return fakeUnits;
    }

    protected static FakeFoggedUnit fogged(AUnitType type, double x) {
        return FakeFoggedUnit.fromFake(fakeEnemy(type, x));
    }

    // =========================================================

    public void assertContainsAll(Object[] expected, Object[] actual) {
//        boolean containsAll = (Arrays.asList(expected)).containsAll(Arrays.asList(actual));
        boolean lengthsMatch = expected.length == actual.length;
        boolean containsAll = true;
        Object missing = null;

        List<Object> expectedList = Arrays.asList(expected);
        List<Object> actualList = Arrays.asList(actual);

//        boolean containsAll = actualList.containsAll(expectedList);

        for (Object object : expectedList) {
            if (!actualList.contains(object)) {
                containsAll = false;
                missing = object;
                break;
            }
        }

        if (!containsAll || !lengthsMatch) {
            System.err.println("\nExpected: (" + expected.length + ")");
            for (Object o : expected) {
                System.err.println(o);
            }
            System.err.println("\nActual: (" + actual.length + ")");
            for (Object o : actual) {
                System.err.println(o);
            }
        }

        if (missing != null) System.err.println("\nMissing: " + missing);

        assertEquals(expected.length, actual.length);
        assertTrue(containsAll);
    }

}
