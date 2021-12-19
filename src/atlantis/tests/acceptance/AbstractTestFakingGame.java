package atlantis.tests.acceptance;

import atlantis.Atlantis;
import atlantis.AtlantisConfig;
import atlantis.combat.micro.avoid.AAvoidUnits;
import atlantis.debug.APainter;
import atlantis.enemy.EnemyInformation;
import atlantis.enemy.EnemyUnits;
import atlantis.information.AFoggedUnit;
import atlantis.tests.unit.AbstractTestWithUnits;
import atlantis.tests.unit.FakeFoggedUnit;
import atlantis.tests.unit.FakeUnit;
import atlantis.tests.unit.UnitTestHelper;
import atlantis.units.AUnitType;
import atlantis.units.select.BaseSelect;
import atlantis.units.select.Select;
import atlantis.wrappers.ATech;
import bwapi.Race;
import org.junit.Before;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class AbstractTestFakingGame extends AbstractTestWithUnits {

    protected FakeUnit[] our;
    protected FakeUnit ourFirst;
    protected FakeUnit[] enemies;

    // =========================================================

    protected void createWorld(int gameLengthInFrames, Runnable onFrame) {
        AtlantisConfig.MY_RACE = Race.Terran;
        AtlantisConfig.BASE = AUnitType.Terran_Command_Center;

        // === Units ======================================================

        our = generateOur();
        ourFirst = our[0];
        enemies = generateEnemies();

        try (MockedStatic<BaseSelect> baseSelect = Mockito.mockStatic(BaseSelect.class)) {
            baseSelect.when(BaseSelect::ourUnits).thenReturn(Arrays.asList(our));
            baseSelect.when(BaseSelect::enemyUnits).thenReturn(Arrays.asList(enemies));

//            MockedStatic<Game> game = Mockito.mockStatic(Game.class);
//            game.when(Game::self).thenReturn(Arrays.asList(fogged));

            MockedStatic<ATech> aTech = Mockito.mockStatic(ATech.class);
            aTech.when(() -> ATech.isResearched(null)).thenReturn(false);

            int framesNow = 0;
            while (framesNow <= gameLengthInFrames) {
                usingFakeTime(framesNow);

                onFrame.run();

                framesNow++;

                FakeOnFrameEnd.onFrameEnd(this);
            }

            aTech.reset();
        }
    }

    protected abstract FakeUnit[] generateOur();
    protected abstract FakeUnit[] generateEnemies();

}
