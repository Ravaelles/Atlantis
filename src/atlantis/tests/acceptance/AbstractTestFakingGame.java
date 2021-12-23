package atlantis.tests.acceptance;

import atlantis.Atlantis;
import atlantis.AtlantisConfig;
import atlantis.position.PositionUtil;
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

    // Static mock classes
    public MockedStatic<ATech> aTech;
    public MockedStatic<PositionUtil> positionUtil;

    // =========================================================

    protected void createWorld(int gameLengthInFrames, Runnable onFrame) {
        AtlantisConfig.MY_RACE = Race.Terran;
        AtlantisConfig.BASE = AUnitType.Terran_Command_Center;

        initGameObject();

        // === Units ======================================================

        our = generateOur();
        ourFirst = our[0];
        enemies = generateEnemies();

        try (MockedStatic<BaseSelect> baseSelect = Mockito.mockStatic(BaseSelect.class)) {
            baseSelect.when(BaseSelect::ourUnits).thenReturn(Arrays.asList(our));
            baseSelect.when(BaseSelect::enemyUnits).thenReturn(Arrays.asList(enemies));

//            MockedStatic<Game> game = Mockito.mockStatic(Game.class);
//            game.when(Game::self).thenReturn(Arrays.asList(fogged));

            mockOtherStaticClasses();

            int framesNow = 0;
            while (framesNow <= gameLengthInFrames) {
                useFakeTime(framesNow);

                onFrame.run();

                framesNow++;

                FakeOnFrameEnd.onFrameEnd(this);
            }

            cleanUpStaticMocks();
        }
    }

    /**
     * You have to define static mocks as public field of this class, so they can be automatically reset on test end.
     */
    private void mockOtherStaticClasses() {
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

    private void cleanUpStaticMocks() {
        for (Field field : getClass().getFields()) {
            if (field.getType().toString().contains("MockedStatic")) {
                try {
                    ((MockedStatic) field.get(this)).reset();
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Something went wrong here");
                }
            }
        }
    }

    private void initGameObject() {
        Game game = Mockito.mock(Game.class);
        Atlantis.getInstance().setGame(game);

        // Map dimensions
        when(game.mapWidth()).thenReturn(20);
        when(game.mapHeight()).thenReturn(20);

        // Walkability
        when(game.isWalkable(any(WalkPosition.class))).thenReturn(true);
    }

    // =========================================================

    protected abstract FakeUnit[] generateOur();

    protected abstract FakeUnit[] generateEnemies();

    // =========================================================

    protected FakeUnit nearestEnemy(FakeUnit unit) {
        return (FakeUnit) Select.enemyCombatUnits().nearestTo(unit);
    }

    protected String distToNearestEnemy(FakeUnit unit) {
        return A.dist(unit, nearestEnemy(unit));
    }

}
