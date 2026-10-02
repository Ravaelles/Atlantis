package tests.unit;

import atlantis.Atlantis;
import atlantis.config.AtlantisRaceConfig;
import atlantis.config.env.Env;
import atlantis.debug.painter.APainter;
import atlantis.game.A;
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
import atlantis.production.constructions.position.RequestBuildingNear;
import atlantis.production.dynamic.protoss.reinforce.BuildPylonFirst;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.units.AUnitType;
import atlantis.units.fogged.FakeFoggedUnit;
import atlantis.units.select.BaseSelect;
import atlantis.util.AConsole;
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
import java.lang.reflect.Modifier;
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

        (new MockEverything(this)).mockEverything();
//        HeuristicCombatEvaluator.clearCache();

        // Always reset the clock, including for world-based tests: AUnitTest
        // (and others) mix world-free tests with createWorld() tests, and the
        // A.now field left behind by the previous test then disagreed with the
        // mocked AGame.now(). A "5 frames ago" assertion was off by one purely
        // because of which test ran before. Frame 0 also keeps every modulo
        // division in the bot returning 0.
        // After MockEverything, because useFakeTime() stubs the aGame mock.
        useFakeTime(0);

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

    /**
     * Which race the enemy is in. Same idea as {@link #initRace()}: a test that
     * fills the enemy side with drones, lurkers and spore colonies has to say so,
     * otherwise every {@code Enemy.zerg()} branch in it answers for a Protoss.
     */
    public Race initEnemyRace() {
        return MockEverything.defaultEnemyRaceForTests();
    }

    protected void setUpTestLogic() {
        // initRace() is the override point a test uses to say which race it is
        // about (TravelToConstructTest has always overridden it) - using the
        // default directly here is what made the override dead code and every
        // test Protoss.
        AtlantisRaceConfig.MY_RACE = initRace();

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
        // Wipe the fake-unit world as well, not just the static mocks: production
        // caches are nuked at setUp, but static fields that hold Units/Orders
        // created by the previous test survive it, and their fakes get their
        // positions nulled underneath them - which is how a later test ended up
        // with "FakeUnit.position() is null" from a unit it never created.
        ClearAllCaches.clearAll();
        AbstractPositionFinder._STATUS = "Init";
        ConstructionRequests.constructions.clear();

        // Static "last error" fields are only cleared on success in production
        // (RequestBuildingNear.error() sets it, the success path clears it), so
        // they carry a failure from one test into the next one. Tests assert on
        // them being null, which made RequestBuildingNearTest order-dependent.
        RequestBuildingNear.lastError = null;
        BuildPylonFirst.lastError = null;

        // Close static mocks - PROPERTIES HAVE TO BE PUBLIC FOR THIS TO WORK.
        // close(), not reset(): reset only clears stubs and leaves the mock
        // registered in the thread, so any test that failed inside createWorld
        // (assertion error before the closing line was reached) leaked it into
        // whatever ran next - which is why failures looked order-dependent.
        // This runs from @AfterEach, so it happens even when the test threw.
        for (Field field : getClass().getFields()) {
            if (field.getType().toString().contains("MockedStatic")) {
                try {
                    Object object = field.get(this);
                    if (object != null) {
                        try {
                            ((MockedStatic) object).close();
                        } finally {
                            // Leave no closed mock behind: the helpers check
                            // for null before registering their own.
                            field.set(Modifier.isStatic(field.getModifiers()) ? null : this, null);
                        }
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Something went wrong here");
                } catch (MockitoException e) {
                    // Mock already closed, that's ok
                }
            }
        }

//        Cache.nukeAllCaches();

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

        // Keep the clock fields in sync with the mocked AGame.now(). Production
        // code reaches the frame number through A.now(), but tests read the
        // public A.now field - and world tests set that field while unit tests
        // did not. A test running after a world test therefore saw two
        // different "now" values, and every "N frames ago" assertion was off by
        // one. One source of truth, set here.
        A.now = framesNow;
        A.s = framesNow / 30;
    }

    protected void usingFakeOurs(Runnable runnable) {
        if (AbstractTestWithWorld.baseSelect != null) {
            AbstractTestWithWorld.baseSelect.close();
            AbstractTestWithWorld.baseSelect = null;
        }
        AbstractTestWithWorld.baseSelect = Mockito.mockStatic(BaseSelect.class);
        AbstractTestWithWorld.baseSelect.when(BaseSelect::ourUnitsWithUnfinishedList).thenReturn(mockOurUnits());

        runnable.run();

        AbstractTestWithWorld.baseSelect.close();
        AbstractTestWithWorld.baseSelect = null;
    }

    protected void usingFakeEnemy(Runnable runnable) {
        if (AbstractTestWithWorld.baseSelect != null) {
            AbstractTestWithWorld.baseSelect.close();
            AbstractTestWithWorld.baseSelect = null;
        }
        AbstractTestWithWorld.baseSelect = Mockito.mockStatic(BaseSelect.class);
        AbstractTestWithWorld.baseSelect.when(BaseSelect::enemyUnits).thenReturn(mockEnemyUnits());

        runnable.run();

        AbstractTestWithWorld.baseSelect.close();
        AbstractTestWithWorld.baseSelect = null;
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
        // No setUp() here on purpose. setUp() is @BeforeEach, so it has already
        // run - and it ends in FakeUnit.clearCache(), which nulls the position,
        // the hp and the id of *every* FakeUnit created so far. Calling it again
        // wiped the units the test had just built: positions became null, so
        // range assertions saw "nothing in range" and distance helpers threw
        // NPEs. setUpTestLogic() below is the part that needs the stubs.
        if (AbstractTestWithWorld.baseSelect != null) {
            AbstractTestWithWorld.baseSelect.close();
            AbstractTestWithWorld.baseSelect = null;
        }
        AbstractTestWithWorld.baseSelect = Mockito.mockStatic(BaseSelect.class);

        AbstractTestWithWorld.baseSelect.when(BaseSelect::ourUnitsWithUnfinishedList).thenReturn(Arrays.asList(ours));
        AbstractTestWithWorld.baseSelect.when(BaseSelect::enemyUnits).thenReturn(Arrays.asList(enemies));
        AbstractTestWithWorld.baseSelect.when(BaseSelect::neutralUnits).thenReturn(Arrays.asList(neutral));

        setUpTestLogic();

        runnable.run();

        if (AbstractTestWithWorld.baseSelect != null) {
            AbstractTestWithWorld.baseSelect.close();
            AbstractTestWithWorld.baseSelect = null;
        }
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
        return UnitTest.ourUnits = fakeUnits;
    }

    public static FakeUnit[] fakeEnemies(FakeUnit... fakeUnits) {
        for (FakeUnit unit : fakeUnits) {
            unit.setEnemy();
        }
        return UnitTest.enemyUnits = fakeUnits;
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
            AConsole.println("\nExpected: (" + expected.length + ")");
            for (Object o : expected) {
                AConsole.println(o);
            }
            AConsole.println("\nActual: (" + actual.length + ")");
            for (Object o : actual) {
                AConsole.println(o);
            }
        }

        if (missing != null) AConsole.println("\nMissing: " + missing);

        assertEquals(expected.length, actual.length);
        assertTrue(containsAll);
    }

}
