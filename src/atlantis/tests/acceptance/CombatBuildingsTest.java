package atlantis.tests.acceptance;

import atlantis.AtlantisConfig;
import atlantis.combat.ACombatUnitManager;
import atlantis.tests.unit.AbstractTestWithUnits;
import atlantis.tests.unit.FakeUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.BaseSelect;
import atlantis.units.select.Select;
import atlantis.util.A;
import atlantis.wrappers.ATech;
import bwapi.Race;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Arrays;

public class CombatBuildingsTest extends AbstractTestWithUnits {

    private FakeUnit our;
//    private FakeUnit enemy1;

    // =========================================================

    @Test
    public void neverRunsIntoCombatBuildings() {
        createWorld(() -> {
            int framesNow = 0;
            while (++framesNow <= 100) {
                usingFakeTime(framesNow);

                boolean result = ACombatUnitManager.update(our);

//                System.out.println("result = " + result);
                System.out.println(A.now() + " -       y:" + our.tx() + ", dist_to_build:" + distToNearestCombatBuild(our));
            }
        });
    }

    // =========================================================

    private void createWorld(Runnable runnable) {
        AtlantisConfig.MY_RACE = Race.Terran;
        AtlantisConfig.BASE = AUnitType.Terran_Command_Center;

        // === Units ======================================================

        our = fake(AUnitType.Terran_Marine, 10);

        int enemyY = 17;
        FakeUnit[] enemies = fakeUnits(
//                enemy1 = fake(AUnitType.Protoss_Photon_Cannon, enemyY),
//                fake(AUnitType.Protoss_Photon_Cannon, outsideRange),
//
                fake(AUnitType.Zerg_Sunken_Colony, enemyY),
                fake(AUnitType.Zerg_Sunken_Colony, enemyY + 10)
//
//                enemy2 = fake(AUnitType.Terran_Missile_Turret, enemyY),
//                fake(AUnitType.Terran_Missile_Turret, outsideRange),
//
//                enemy3 = fake(AUnitType.Zerg_Spore_Colony, enemyY),
//                fake(AUnitType.Zerg_Spore_Colony, outsideRange),
//
//                enemy4 = fake(AUnitType.Terran_Bunker, enemyY - 5),
//                fake(AUnitType.Terran_Bunker, outsideRange)
        );

        try (MockedStatic<BaseSelect> baseSelect = Mockito.mockStatic(BaseSelect.class)) {
            baseSelect.when(BaseSelect::ourUnits).thenReturn(Arrays.asList(our));
            baseSelect.when(BaseSelect::enemyUnits).thenReturn(Arrays.asList(enemies));

//            MockedStatic<Game> game = Mockito.mockStatic(Game.class);
//            game.when(Game::self).thenReturn(Arrays.asList(fogged));

            MockedStatic<ATech> aTech = Mockito.mockStatic(ATech.class);
            aTech.when(() -> ATech.isResearched(null)).thenReturn(false);

            runnable.run();

            aTech.reset();
        }
    }

    private String distToNearestCombatBuild(FakeUnit our) {
        return A.dist(our, Select.enemyCombatUnits().nearestTo(our));
    }

}
