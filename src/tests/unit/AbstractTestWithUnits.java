package tests.unit;

import atlantis.Atlantis;
import atlantis.combat.eval.ACombatEvaluator;
import atlantis.combat.micro.avoid.AAvoidEnemies;
import atlantis.config.AtlantisConfig;
import atlantis.config.env.Env;
import atlantis.debug.painter.APainter;
import atlantis.game.AGame;
import atlantis.game.OnStart;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.tech.ATech;
import atlantis.map.position.PositionUtil;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.AbstractFoggedUnit;
import atlantis.units.FakeFoggedUnit;
import atlantis.units.select.BaseSelect;
import atlantis.units.select.Select;
import bwapi.*;
import org.junit.After;
import org.junit.Before;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.invocation.InvocationOnMock;
import tests.acceptance.AbstractTestFakingGame;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class AbstractTestWithUnits extends UnitTestHelper {

    public Game game;

    public MockedStatic<Env> env;
    public MockedStatic<AGame> aGame;
    public MockedStatic<ATech> aTech;
    public MockedStatic<PositionUtil> positionUtil;

    // =========================================================

    @Before
    public void before() {
        if (!(this instanceof AbstractTestFakingGame)) {
            useFakeTime(0); // This needs to be 0 so every modulo division returns 0
        }

        mockAtlantisConfig();
        APainter.disablePainting();
        Select.clearCache();
        BaseSelect.clearCache();
        ACombatEvaluator.clearCache();
        AbstractFoggedUnit.clearCache();
        EnemyInfo.clearCache();
        EnemyUnits.clearCache();
        AAvoidEnemies.clearCache();
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
        initBuildOrder();

        env = Mockito.mockStatic(Env.class);
        env.when(Env::isTesting).thenReturn(true);

        aTech = Mockito.mockStatic(ATech.class);
        aTech.when(() -> ATech.isResearched(TechType.Lockdown)).thenReturn(true);
        aTech.when(() -> ATech.isResearched(null)).thenReturn(false);
        aTech.when(() -> ATech.getUpgradeLevel(any())).thenReturn(0);

        positionUtil = Mockito.mockStatic(PositionUtil.class);
        positionUtil.when(() -> PositionUtil.groundDistanceTo(any(Position.class), any(Position.class)))
            .thenAnswer((InvocationOnMock invocationOnMock) -> {
                Position p1 = invocationOnMock.getArgument(0);
                Position p2 = invocationOnMock.getArgument(1);
                return p1.getDistance(p2) / 32.0;
            });
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
        AtlantisConfig.MY_RACE = Race.Zerg;
        AtlantisConfig.BASE = AUnitType.Terran_Command_Center;
        AtlantisConfig.GAS_BUILDING = AUnitType.Terran_Refinery;
        AtlantisConfig.WORKER = AUnitType.Terran_SCV;
        AtlantisConfig.BARRACKS = AUnitType.Terran_Barracks;
        AtlantisConfig.DEFENSIVE_BUILDING_ANTI_AIR = AUnitType.Terran_Missile_Turret;
        AtlantisConfig.DEFENSIVE_BUILDING_ANTI_LAND = AUnitType.Terran_Bunker;


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
        aGame.when(AGame::supplyTotal).thenReturn(5);
        aGame.when(AGame::supplyUsed).thenReturn(5);
        aGame.when(AGame::isPlayingAsZerg).thenReturn(true);
        aGame.when(AGame::isEnemyProtoss).thenReturn(true);
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
        try (MockedStatic<BaseSelect> baseSelect = Mockito.mockStatic(BaseSelect.class)) {
            baseSelect.when(BaseSelect::ourUnits).thenReturn(mockOurUnits());

            runnable.run();
        }
    }

    protected void usingFakeEnemy(Runnable runnable) {
        try (MockedStatic<BaseSelect> baseSelect = Mockito.mockStatic(BaseSelect.class)) {
            baseSelect.when(BaseSelect::enemyUnits).thenReturn(mockEnemyUnits());

            runnable.run();
        }
    }

    protected void usingFakeNeutral(Runnable runnable) {
//        try (MockedStatic<BaseSelect> baseSelect = Mockito.mockStatic(BaseSelect.class)) {
//            baseSelect.when(BaseSelect::neutralUnits).thenReturn(mockNeutralUnits());
//
//            runnable.run();
//        }

        List<AUnit> neutral = mockNeutralUnits();
        usingFakeOursEnemiesAndNeutral(new FakeUnit[]{}, new FakeUnit[]{}, neutral.toArray(new FakeUnit[0]), runnable);
    }

    protected void usingFakeOursAndFakeEnemies(FakeUnit[] ours, FakeUnit[] enemies, Runnable runnable) {
//        try (MockedStatic<BaseSelect> baseSelect = Mockito.mockStatic(BaseSelect.class)) {
//            baseSelect.when(BaseSelect::ourUnits).thenReturn(Arrays.asList(ours));
//            baseSelect.when(BaseSelect::enemyUnits).thenReturn(Arrays.asList(enemies));
//
//            runnable.run();
//        }

        usingFakeOursEnemiesAndNeutral(ours, enemies, new FakeUnit[]{}, runnable);
    }

    protected void usingFakeOurAndFakeEnemies(FakeUnit our, FakeUnit[] enemies, Runnable runnable) {
        usingFakeOursEnemiesAndNeutral(new FakeUnit[]{ our }, enemies, new FakeUnit[]{}, runnable);
    }

    protected void usingFakeOursEnemiesAndNeutral(
        FakeUnit[] ours, FakeUnit[] enemies, FakeUnit[] neutral, Runnable runnable
    ) {
        try (MockedStatic<BaseSelect> baseSelect = Mockito.mockStatic(BaseSelect.class)) {
            mockEverything();

            baseSelect.when(BaseSelect::ourUnits).thenReturn(Arrays.asList(ours));
            baseSelect.when(BaseSelect::enemyUnits).thenReturn(Arrays.asList(enemies));
            baseSelect.when(BaseSelect::neutralUnits).thenReturn(Arrays.asList(neutral));

            runnable.run();
        }
    }

    protected FakeUnit fake(AUnitType type) {
        return new FakeUnit(type, 10, 10);
    }

    protected FakeUnit fake(AUnitType type, double x) {
        return new FakeUnit(type, x, 10);
    }

    protected FakeUnit fakeEnemy(AUnitType type, double x) {
        return new FakeUnit(type, x, 10).setEnemy();
    }

    protected FakeUnit fake(AUnitType type, int x, int y) {
        return new FakeUnit(type, x, y);
    }

    protected FakeUnit[] fakeOurs(FakeUnit... fakeUnits) {
        return fakeUnits;
    }

    protected FakeUnit[] fakeEnemies(FakeUnit... fakeUnits) {
        for (FakeUnit unit : fakeUnits) {
            unit.setEnemy();
        }
        return fakeUnits;
    }

    protected FakeFoggedUnit fogged(AUnitType type, int x) {
        return FakeFoggedUnit.fromFake(fakeEnemy(type, x));
    }

    // =========================================================

    public void assertContainsAll(Object[] expected, Object[] actual) {
        boolean containsAll = (Arrays.asList(expected)).containsAll(Arrays.asList(actual));
        boolean lengthsMatch = expected.length == actual.length;

        if (!containsAll || !lengthsMatch) {
            System.err.println("\nExpected: (" + expected.length + ")");
            for (Object o : expected) {
                System.err.println(o);
            }
            System.err.println("\nActual: (" + actual.length + ")");
            for (Object o : actual ) {
                System.err.println(o);
            }
        }

        assertEquals(expected.length, actual.length);
        assertTrue(containsAll);
    }

}
