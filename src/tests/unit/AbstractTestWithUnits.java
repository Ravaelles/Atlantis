package tests.unit;

import atlantis.Atlantis;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.config.AtlantisRaceConfig;
import atlantis.config.env.Env;
import atlantis.debug.painter.APainter;
import atlantis.game.AGame;
import atlantis.game.events.OnStart;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.strategy.OurStrategy;
import atlantis.information.strategy.TerranStrategies;
import atlantis.information.tech.ATech;
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
import bwapi.*;
import org.junit.After;
import org.junit.Before;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoException;
import tests.acceptance.AbstractTestFakingGame;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class AbstractTestWithUnits extends UnitTestHelper {

    public Game game;

    public MockedStatic<Env> env;
    public MockedStatic<AGame> aGame;
    public MockedStatic<ATech> aTech;
    public MockedStatic<Enemy> enemy;

    protected Options options = new Options();

    // =========================================================

    @Before
    public void before() {
        Env.markIsTesting(true);

        Env.readEnvFile(new String[]{});

        if (!(this instanceof AbstractTestFakingGame)) {
            useFakeTime(0); // This needs to be 0 so every modulo division returns 0
        }

        mockAtlantisConfig();

        APainter.disablePainting();

        Select.clearCache();
        BaseSelect.clearCache();
        Count.clearCache();
//        HeuristicCombatEvaluator.clearCache();
        AbstractFoggedUnit.clearCache();
        EnemyInfo.clearCache();
        EnemyUnits.clearCache();
        AvoidEnemies.clearCache();
        ReservedResources.reset();
    }

    protected void beforeTestLogic() {
        if (AtlantisRaceConfig.MY_RACE == null) {
            AtlantisRaceConfig.MY_RACE = Race.Terran;
        }

        initBuildOrder();
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

    protected void mockEverything() {
        mockAtlantisConfig();
        mockGameObject();
        mockAGameObject();
        mockOtherStaticClasses();
    }

    /**
     * You have to define static mocks as public field of this class, so they can be automatically reset on test end.
     */
    protected void mockOtherStaticClasses() {
        env = Mockito.mockStatic(Env.class);
        env.when(Env::isTesting).thenReturn(true);

        aTech = Mockito.mockStatic(ATech.class);
        aTech.when(() -> ATech.isResearched(TechType.Lockdown)).thenReturn(true);
        aTech.when(() -> ATech.isResearched(null)).thenReturn(false);
        aTech.when(() -> ATech.getUpgradeLevel(any())).thenReturn(0);

        enemy = Mockito.mockStatic(Enemy.class);
        enemy.when(() -> Enemy.terran()).thenReturn(false);
        enemy.when(() -> Enemy.protoss()).thenReturn(true);
        enemy.when(() -> Enemy.zerg()).thenReturn(false);

        // This is not needed for green tests and was causing standard AUnit::distTo(AUnit) to return 0
//        positionUtil = Mockito.mockStatic(PositionUtil.class);
//        positionUtil.when(() -> PositionUtil.groundDistanceTo(any(Position.class), any(Position.class)))
//            .thenAnswer((InvocationOnMock invocationOnMock) -> {
//                Position p1 = invocationOnMock.getArgument(0);
//                Position p2 = invocationOnMock.getArgument(1);
//                System.err.println("positionUtil groundDistanceTo A = " + p1 + " / B = " + p2);
//                return p1.getDistance(p2) / 32.0;
//            });
    }

    protected void initBuildOrder() {
        OnStart.initializeAllStrategies();

        try {
            OnStart.initStrategyAndBuildOrder();
        } catch (RuntimeException e) {
            // Ignore
        }
    }

    protected void mockAtlantisConfig() {
        AtlantisRaceConfig.MY_RACE = Race.Terran;
        AtlantisRaceConfig.BASE = AUnitType.Terran_Command_Center;
        AtlantisRaceConfig.GAS_BUILDING = AUnitType.Terran_Refinery;
        AtlantisRaceConfig.SUPPLY = AUnitType.Terran_Supply_Depot;
        AtlantisRaceConfig.WORKER = AUnitType.Terran_SCV;
        AtlantisRaceConfig.BARRACKS = AUnitType.Terran_Barracks;
        AtlantisRaceConfig.DEFENSIVE_BUILDING_ANTI_AIR = AUnitType.Terran_Missile_Turret;
        AtlantisRaceConfig.DEFENSIVE_BUILDING_ANTI_LAND = AUnitType.Terran_Bunker;
    }

    protected void mockGameObject() {
        if ((game = Atlantis.game()) == null) {
            game = Mockito.mock(Game.class);
            Atlantis.getInstance().setGame(game);
        }

        // Map dimensions
        when(game.mapWidth()).thenReturn(20);
        when(game.mapHeight()).thenReturn(20);

        // Walkability
        when(game.isWalkable(any(WalkPosition.class))).thenReturn(true);
    }

    protected void mockAGameObject() {
        aGame = Mockito.mockStatic(AGame.class);

        boolean optionSupplyUsed = options != null && options.has("supplyUsed");
        boolean optionSupplyTotal = options != null && options.has("supplyTotal");

        if (optionSupplyUsed) {
            int supplyUsed = options.getInt("supplyUsed");
            aGame.when(AGame::supplyUsed).thenReturn(supplyUsed);
            if (!optionSupplyTotal) {
                aGame.when(AGame::supplyTotal).thenReturn(supplyUsed + 4);
                aGame.when(AGame::supplyFree).thenReturn(4);
            }
        }
        if (optionSupplyTotal) {
            int supplyTotal = options.getInt("supplyTotal");
            aGame.when(AGame::supplyTotal).thenReturn(supplyTotal);
            aGame.when(AGame::supplyFree).thenReturn(4);
        }

        aGame.when(AGame::minerals).thenReturn(444);
        aGame.when(AGame::gas).thenReturn(333);
        aGame.when(AGame::isPlayingAsTerran).thenReturn(true);
        aGame.when(AGame::isEnemyProtoss).thenReturn(true);

//        try (MockedStatic<We> we = Mockito.mockStatic(We.class)) {
//            we.when(We::terran).thenReturn(true);
//        }
    }

    protected void setUpStrategy() {
        OurStrategy.setTo(TerranStrategies.TERRAN_Tests);
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
            mockEverything();

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
