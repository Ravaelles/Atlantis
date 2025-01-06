package tests.unit;

import atlantis.Atlantis;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.config.AtlantisRaceConfig;
import atlantis.config.env.Env;
import atlantis.debug.painter.APainter;
import atlantis.game.AGame;
import atlantis.game.listeners.OnStart;
import atlantis.game.race.EnemyRace;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.strategy.AStrategy;
import atlantis.information.strategy.OurStrategy;
import atlantis.information.strategy.terran.TerranStrategies;
import atlantis.information.tech.ATech;
import atlantis.map.base.AllBaseLocations;
import atlantis.map.choke.AllChokes;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.fogged.AbstractFoggedUnit;
import atlantis.units.fogged.FakeFoggedUnit;
import atlantis.units.select.BaseSelect;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.Enemy;
import atlantis.util.Options;
import bwapi.Game;
import bwapi.Race;
import org.junit.After;
import org.junit.Before;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoException;
import tests.acceptance.AbstractTestFakingGame;
import tests.fakes.FakeBullets;
import tests.fakes.FakeUnit;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class AbstractTestWithUnits extends UnitTest {
    public Game game;

    protected int currentMinerals = 0;
    protected int currentGas = 0;
    protected int currentSupplyUsed = 0;
    protected int currentSupplyTotal = 0;

    public MockedStatic<Env> env;
    public MockedStatic<AGame> aGame;
    public MockedStatic<ATech> aTech;
    public MockedStatic<Enemy> enemy;
    public MockedStatic<EnemyRace> enemyRace;
    public MockedStatic<AllBaseLocations> allBaseLocations;
    public MockedStatic<AllChokes> allChokes;

    protected Options options = new Options();

    // =========================================================

    @Before
    public void before() {
        Env.markIsTesting(true);

        Env.readEnvFile(new String[]{});

        if (!(this instanceof AbstractTestFakingGame)) {
            useFakeTime(0); // This needs to be 0 so every modulo division returns 0
        }

        (new MockEverything(this)).mockEverything();

        APainter.disablePainting();

        AbstractFoggedUnit.clearCache();
        AvoidEnemies.clearCache();
        BaseSelect.clearCache();
        Count.clearCache();
        EnemyInfo.clearCache();
        EnemyUnits.clearCache();
        FakeBullets.allBullets.clear();
        ReservedResources.reset();
        Select.clearCache();
//        HeuristicCombatEvaluator.clearCache();
    }

    public Race initRace() {
        return MockEverything.defaultRaceForTests();
    }

    protected void beforeTestLogic() {
        AtlantisRaceConfig.MY_RACE = MockEverything.defaultRaceForTests();

        if (AtlantisRaceConfig.MY_RACE == null) {
            AtlantisRaceConfig.MY_RACE = MockEverything.defaultRaceForTests();
        }

        setUpBuildOrder();
        setUpStrategy();
    }

    @After
    public void after() {
        cleanUp();
    }

    /**
     * PROPERTIES HAVE TO BE PUBLIC FOR THIS TO WORK.
     */
    protected void cleanUp() {
//        Select.clearCache();
//        BaseSelect.clearCache();
//        EnemyUnits.clearCache();
//        EnemyInfo.clearCache();
        AbstractPositionFinder._STATUS = "Init";
        ConstructionRequests.constructions.clear();

        // Close static mocks - PROPERTIES HAVE TO BE PUBLIC FOR THIS TO WORK
        for (Field field : getClass().getFields()) {
            if (field.getType().toString().contains("MockedStatic")) {
                try {
                    Object object = field.get(this);
                    if (object != null) {
                        ((MockedStatic) object).close();
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Something went wrong here");
                } catch (MockitoException e) {
                    // Mock already closed, that's ok
                }
            }
        }

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
        OnStart.initializeAllStrategies();

        try {
            OnStart.initStrategyAndBuildOrder();
        } catch (RuntimeException e) {
            // Ignore
        }
    }

    protected void setUpStrategy() {
        OurStrategy.setTo(initBuildOrder());
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
        try (MockedStatic<BaseSelect> baseSelect = AbstractTestFakingGame.baseSelect = Mockito.mockStatic(BaseSelect.class)) {
            baseSelect.when(BaseSelect::ourUnitsWithUnfinishedList).thenReturn(mockOurUnits());

            runnable.run();
        }
    }

    protected void usingFakeEnemy(Runnable runnable) {
        try (MockedStatic<BaseSelect> baseSelect = AbstractTestFakingGame.baseSelect = Mockito.mockStatic(BaseSelect.class)) {
            baseSelect.when(BaseSelect::enemyUnits).thenReturn(mockEnemyUnits());

            runnable.run();
        }
    }

    protected void usingFakeNeutral(Runnable runnable) {
//        try (MockedStatic<BaseSelect> baseSelect = AbstractTestFakingGame.baseSelect = Mockito.mockStatic(BaseSelect.class)) {
//            baseSelect.when(BaseSelect::neutralUnits).thenReturn(mockNeutralUnits());
//
//            runnable.run();
//        }

        List<AUnit> neutral = mockNeutralUnits();
        usingFakeOursEnemiesAndNeutral(new FakeUnit[]{}, new FakeUnit[]{}, neutral.toArray(new FakeUnit[0]), runnable);
    }

    protected void usingFakeOursAndFakeEnemies(FakeUnit[] ours, FakeUnit[] enemies, Runnable runnable) {
//        try (MockedStatic<BaseSelect> baseSelect = AbstractTestFakingGame.baseSelect = Mockito.mockStatic(BaseSelect.class)) {
//            baseSelect.when(BaseSelect::ourUnits).thenReturn(Arrays.asList(ours));
//            baseSelect.when(BaseSelect::enemyUnits).thenReturn(Arrays.asList(enemies));
//
//            runnable.run();
//        }

        usingFakeOursEnemiesAndNeutral(ours, enemies, new FakeUnit[]{}, runnable);
    }

    protected void usingFakeOurAndFakeEnemies(FakeUnit our, FakeUnit[] enemies, Runnable runnable) {
        usingFakeOursEnemiesAndNeutral(new FakeUnit[]{our}, enemies, new FakeUnit[]{}, runnable);
    }

    protected void usingFakeOursEnemiesAndNeutral(
        FakeUnit[] ours, FakeUnit[] enemies, FakeUnit[] neutral, Runnable runnable
    ) {
        try (MockedStatic<BaseSelect> baseSelect = AbstractTestFakingGame.baseSelect = Mockito.mockStatic(BaseSelect.class)) {
//            (new MockEverything(this)).mockEverything();

            baseSelect.when(BaseSelect::ourUnitsWithUnfinishedList).thenReturn(Arrays.asList(ours));
            baseSelect.when(BaseSelect::enemyUnits).thenReturn(Arrays.asList(enemies));
            baseSelect.when(BaseSelect::neutralUnits).thenReturn(Arrays.asList(neutral));

            beforeTestLogic();

            runnable.run();
        }
    }

    protected static FakeUnit fake(AUnitType type) {
        return new FakeUnit(type, 10, 10);
    }

    protected static FakeUnit fake(AUnitType type, double x) {
        return new FakeUnit(type, x, 10);
    }

    protected static FakeUnit fakeEnemy(AUnitType type, double x) {
        return new FakeUnit(type, x, 10).setEnemy();
    }

    protected static FakeUnit fake(AUnitType type, double x, double y) {
        return new FakeUnit(type, x, y);
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
