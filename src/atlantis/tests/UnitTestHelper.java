package atlantis.tests;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import bwapi.Game;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

public class UnitTestHelper {

    public static int OUR_REAL_UNITS = 3;
    public static int OUR_GROUND_UNITS = 2;
    public static int OUR_AIR_UNITS = 2;

    public static int REAL_UNITS = 10;
    public static int GROUND_UNITS = 5;
    public static int BUILDINGS = 5;
    public static int AIR_UNITS = 5;
    public static int SPELLS = 2;

//    public static int NEUTRAL_UNITS = 5;
    public static int MINERAL_COUNT = 3;
    public static int GEYSER_COUNT = 2;

    public static AUnit[] ourUnits;
    public static AUnit[] enemyUnits;
    public static AUnit[] neutralUnits;

    // =========================================================

    public static AUnit[] generateUnits(boolean trueIfOurFalseIfEnemy) {
        int y = 10;

        return new AUnit[] {
                // Ground
                new FakeUnit(AUnitType.Protoss_Zealot, 10, y).setOur(trueIfOurFalseIfEnemy),
                new FakeUnit(AUnitType.Protoss_Dragoon, 20, y).setOur(trueIfOurFalseIfEnemy),
                new FakeUnit(AUnitType.Terran_SCV, 20, y).setOur(trueIfOurFalseIfEnemy),
                new FakeUnit(AUnitType.Terran_Siege_Tank_Tank_Mode, 20, y).setOur(trueIfOurFalseIfEnemy),
                new FakeUnit(AUnitType.Terran_Siege_Tank_Siege_Mode, 20, y).setOur(trueIfOurFalseIfEnemy),

                // Buildings
                new FakeUnit(AUnitType.Protoss_Photon_Cannon, 26, y).setOur(trueIfOurFalseIfEnemy),
                new FakeUnit(AUnitType.Terran_Comsat_Station, 28, y).setOur(trueIfOurFalseIfEnemy),
                new FakeUnit(AUnitType.Terran_Command_Center, 26, y).setOur(trueIfOurFalseIfEnemy),
                new FakeUnit(AUnitType.Zerg_Creep_Colony, 26, y).setOur(trueIfOurFalseIfEnemy),
                new FakeUnit(AUnitType.Zerg_Sunken_Colony, 26, y).setOur(trueIfOurFalseIfEnemy),

                // Non-real
                new FakeUnit(AUnitType.Protoss_Scarab, 25, y).setOur(trueIfOurFalseIfEnemy),
                new FakeUnit(AUnitType.Terran_Vulture_Spider_Mine, 25, y).setOur(trueIfOurFalseIfEnemy),
                new FakeUnit(AUnitType.Zerg_Larva, 26, y).setOur(trueIfOurFalseIfEnemy),
                new FakeUnit(AUnitType.Zerg_Egg, 27, y).setOur(trueIfOurFalseIfEnemy),
                new FakeUnit(AUnitType.Zerg_Lurker_Egg, 28, y).setOur(trueIfOurFalseIfEnemy),

                // Air
                new FakeUnit(AUnitType.Protoss_Carrier, 18, y).setOur(trueIfOurFalseIfEnemy),
                new FakeUnit(AUnitType.Terran_Dropship, 18, y).setOur(trueIfOurFalseIfEnemy),
                new FakeUnit(AUnitType.Zerg_Scourge, 15, y).setOur(trueIfOurFalseIfEnemy),
                new FakeUnit(AUnitType.Zerg_Mutalisk, 15, y).setOur(trueIfOurFalseIfEnemy),
                new FakeUnit(AUnitType.Zerg_Mutalisk, 15, y).setOur(trueIfOurFalseIfEnemy),

                // Special
                new FakeUnit(AUnitType.Spell_Scanner_Sweep, 26, y).setOur(trueIfOurFalseIfEnemy),
                new FakeUnit(AUnitType.Spell_Dark_Swarm, 26, y).setOur(trueIfOurFalseIfEnemy),
        };
    }

    // =========================================================

    public static List<AUnit> mockOurUnits() {
        int ourY = 10;

        ourUnits = generateUnits(true);

        return Arrays.asList(ourUnits);
    }

    public static List<AUnit> mockEnemyUnits() {
        int y = 8;

        enemyUnits = generateUnits(false);

        return Arrays.asList(enemyUnits);
    }

    public static List<AUnit> mockNeutralUnits() {
        int neutralY = 2;

        neutralUnits = new AUnit[] {
                new FakeUnit(AUnitType.Resource_Mineral_Field, 10, neutralY).setNeutral(),
                new FakeUnit(AUnitType.Resource_Mineral_Field_Type_2, 11, neutralY).setNeutral(),
                new FakeUnit(AUnitType.Resource_Mineral_Field_Type_3, 15, neutralY).setNeutral(),

                new FakeUnit(AUnitType.Resource_Vespene_Geyser, 2, neutralY).setNeutral(),
                new FakeUnit(AUnitType.Resource_Vespene_Geyser, 18, neutralY).setNeutral(),
        };

        return Arrays.asList(neutralUnits);
    }

    public static List<AUnit> generateUnitsList(boolean trueIfOurFalseIfEnemy) {
        return Arrays.asList(generateUnits(trueIfOurFalseIfEnemy));
    }

    public static Game gameMock() {
        Game game = Mockito.mock(Game.class);
        when(game.getFrameCount()).thenReturn(0);
        return game;
    }

}