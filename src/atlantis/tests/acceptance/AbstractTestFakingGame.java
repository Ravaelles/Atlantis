package atlantis.tests.acceptance;

import atlantis.AGame;
import atlantis.Atlantis;
import atlantis.AtlantisConfig;
import atlantis.OnStart;
import atlantis.position.PositionUtil;
import atlantis.production.orders.ABuildOrderLoader;
import atlantis.production.orders.CurrentBuildOrder;
import atlantis.production.orders.TerranBuildOrder;
import atlantis.strategy.TerranStrategies;
import atlantis.tests.unit.AbstractTestWithUnits;
import atlantis.tests.unit.FakeUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.BaseSelect;
import atlantis.units.select.Select;
import atlantis.util.A;
import atlantis.wrappers.ATech;
import bwapi.Game;
import bwapi.Position;
import bwapi.Race;
import bwapi.WalkPosition;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public abstract class AbstractTestFakingGame extends AbstractTestWithUnits {

    protected FakeUnit[] our;
    protected FakeUnit ourFirst;
    protected FakeUnit[] enemies;
    protected FakeUnit[] neutral;

    public Game game;
    public MockedStatic<AGame> aGame;
    public MockedStatic<ATech> aTech;
    public MockedStatic<PositionUtil> positionUtil;

    // =========================================================

    protected void createWorld(int proceedUntilFrameReached, Runnable onFrame) {

        // === Units ======================================================

        our = generateOur();
        ourFirst = our[0];
        enemies = generateEnemies();
        neutral = generateNeutral();

        // === Mock static classes ========================================

        try (MockedStatic<BaseSelect> baseSelect = Mockito.mockStatic(BaseSelect.class)) {
            baseSelect.when(BaseSelect::ourUnits).thenReturn(Arrays.asList(our));
            baseSelect.when(BaseSelect::enemyUnits).thenReturn(Arrays.asList(enemies));
            baseSelect.when(BaseSelect::neutralUnits).thenReturn(Arrays.asList(neutral));

            initAtlantisConfig();
            initGameObject();
            initAGameObject();

            mockOtherStaticClasses();

            int framesNow = 1;
            while (framesNow <= proceedUntilFrameReached) {
                useFakeTime(framesNow);

                onFrame.run();

                framesNow++;

                FakeOnFrameEnd.onFrameEnd(this);
            }

            cleanUpStaticMocks();
        }
    }

    private void initAtlantisConfig() {
        AtlantisConfig.MY_RACE = Race.Terran;
        AtlantisConfig.BASE = AUnitType.Terran_Command_Center;
        AtlantisConfig.GAS_BUILDING = AUnitType.Terran_Refinery;
        AtlantisConfig.WORKER = AUnitType.Terran_SCV;
        AtlantisConfig.BARRACKS = AUnitType.Terran_Barracks;
        AtlantisConfig.DEFENSIVE_BUILDING_ANTI_AIR = AUnitType.Terran_Missile_Turret;
        AtlantisConfig.DEFENSIVE_BUILDING_ANTI_LAND = AUnitType.Terran_Bunker;
    }

    /**
     * You have to define static mocks as public field of this class, so they can be automatically reset on test end.
     */
    private void mockOtherStaticClasses() {
        initBuildOrder();

        aTech = Mockito.mockStatic(ATech.class);
        aTech.when(() -> ATech.isResearched(null)).thenReturn(false);

        positionUtil = Mockito.mockStatic(PositionUtil.class);
        positionUtil.when(() -> PositionUtil.groundDistanceTo(any(Position.class), any(Position.class)))
                .thenAnswer((InvocationOnMock invocationOnMock) -> {
                    Position p1 = invocationOnMock.getArgument(0);
                    Position p2 = invocationOnMock.getArgument(1);
                    return p1.getDistance(p2) / 32.0;
                });
    }

    private void initBuildOrder() {
        OnStart.initializeAllStrategies();
        OnStart.initStrategyAndBuildOrder();
    }

    private void initGameObject() {
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

    private void initAGameObject() {
        aGame = Mockito.mockStatic(AGame.class);
        aGame.when(AGame::supplyTotal).thenReturn(5);
        aGame.when(AGame::supplyUsed).thenReturn(5);
    }

    protected void useFakeTime(int framesNow) {
        super.useFakeTime(framesNow);

        aGame.when(AGame::now).thenReturn(framesNow);
    }

    private void cleanUpStaticMocks() {
        game = null;

        for (Field field : getClass().getFields()) {
            if (field.getType().toString().contains("MockedStatic")) {
                try {
                    Object object = field.get(this);
                    if (object != null) {
                        ((MockedStatic) object).close();
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Something went wrong here");
                }
            }
        }
    }

    // =========================================================

    protected abstract FakeUnit[] generateOur();

    protected abstract FakeUnit[] generateEnemies();

    protected FakeUnit[] generateNeutral() {
        return new FakeUnit[] { };
    }

    // =========================================================

    protected FakeUnit nearestEnemy(FakeUnit unit) {
        return (FakeUnit) Select.enemyCombatUnits().nearestTo(unit);
    }

    protected String distToNearestEnemy(FakeUnit unit) {
        return A.dist(unit, nearestEnemy(unit));
    }

}
