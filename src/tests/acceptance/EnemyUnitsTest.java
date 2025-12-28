package tests.acceptance;

import atlantis.game.A;
import atlantis.game.AtlantisGameCommander;
import atlantis.game.listeners.OnUnitMorph;
import atlantis.game.listeners.OnUnitRenegade;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.information.generic.Army;
import atlantis.information.generic.ArmyStrength;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class EnemyUnitsTest extends AbstractTestWithWorld {
    private AtlantisGameCommander gameCommander;
    private FakeUnit drone1;
    private FakeUnit drone2;
    private FakeUnit drone3;
    private FakeUnit drone4;
    private FakeUnit drone5;
    private FakeUnit drone6;
    private FakeUnit geyser;
    private FakeUnit lurkerEgg;
    private FakeUnit larva;
    private FakeUnit hydra;

    private double armyStrengthA = -1;
    private double armyStrengthB = -1;

    @Test
    public void fiveFrames() {
        gameCommander = new AtlantisGameCommander();

//        AtlantisRaceConfig.SUPPLY = AUnitType.Terran_Supply_Depot;

        createWorld(5, () -> {
//            System.err.println("\n===================== FRAME = " + A.now() + " ===========================");

            if (A.now() == 1) {
                drone4 = fakeEnemy(AUnitType.Zerg_Drone, 23);
                drone5 = fakeEnemy(AUnitType.Zerg_Drone, 24);
                drone6 = fakeEnemy(AUnitType.Zerg_Drone, 25);
                lurkerEgg = fakeEnemy(AUnitType.Zerg_Lurker_Egg, 26);
                hydra = fakeEnemy(AUnitType.Zerg_Hydralisk, 27);

                firstFrame();
            }
            else if (A.now() == 2) {
                secondFrame();
            }
            else if (A.now() == 3) {
                thirdFrame();
            }
            else if (A.now() == 4) {
                forthFrame();
            }
            else if (A.now() == 5) {
                fifthFrame();
            }

            gameCommander.invokedCommander();
        });
    }

    private void firstFrame() {
//        assertTrue(armyStrengthUnchached() >= 999);

//        EnemyUnits.discovered().print("Enemies at start");

//        System.err.println("ia  = " + armyStrengthUnchached()
//            + " / ED:" + EnemyUnits.discovered().size() + " / Fresh:" + EnemyUnits.freshDiscovered().size()
//        );

        EnemyUnitsUpdater.weDiscoveredEnemyUnit(drone1);
        EnemyUnitsUpdater.weDiscoveredEnemyUnit(drone2);
        EnemyUnitsUpdater.weDiscoveredEnemyUnit(drone3);
        EnemyUnitsUpdater.weDiscoveredEnemyUnit(lurkerEgg);
        EnemyUnitsUpdater.weDiscoveredEnemyUnit(larva);

//        System.err.println("ib  = " + armyStrengthUnchached() +
//            " / ED:" + EnemyUnits.discovered().size() + " / Fresh:" + EnemyUnits.freshDiscovered().size()
//        );

//        System.err.println("AAAAA lurkerEgg = " + lurkerEgg);
//        EnemyUnits.discovered().print("Discovered 1st");
    }
    
    private double armyStrengthUnchached() {
        ArmyStrength.clearCache();
//        EnemyUnits.clearCountCache();

        return Army.strength();
    }

    private void secondFrame() {

        // Visible
        drone1.changeRawUnitType(AUnitType.Zerg_Creep_Colony);
        drone2.changeRawUnitType(AUnitType.Zerg_Sunken_Colony);
        drone3.changeRawUnitType(AUnitType.Zerg_Lurker);
        geyser.changeRawUnitType(AUnitType.Zerg_Extractor);
        geyser.setEnemy();

        // Behind FoW
        drone4.changeRawUnitType(AUnitType.Zerg_Creep_Colony);
        drone5.changeRawUnitType(AUnitType.Zerg_Sunken_Colony);
        drone6.changeRawUnitType(AUnitType.Zerg_Lurker);

        OnUnitMorph.update(drone1);
        OnUnitMorph.update(drone2);
        OnUnitMorph.update(drone3);
//        OnUnitMorph.update(geyser);
        OnUnitRenegade.update(geyser);
    }

    private void thirdFrame() {
        assertEquals(AUnitType.Zerg_Creep_Colony, EnemyUnits.getFoggedUnit(drone1).type());
        assertEquals(AUnitType.Zerg_Sunken_Colony, EnemyUnits.getFoggedUnit(drone2).type());
        assertEquals(AUnitType.Zerg_Lurker, EnemyUnits.getFoggedUnit(drone3).type());
        assertEquals(AUnitType.Zerg_Extractor, EnemyUnits.getFoggedUnit(geyser).type());

        assertNull(EnemyUnits.getFoggedUnit(drone4));
        assertNull(EnemyUnits.getFoggedUnit(drone5));
        assertNull(EnemyUnits.getFoggedUnit(drone6));
    }

    private void forthFrame() {

        // Visible
        drone1.changeRawUnitType(AUnitType.Zerg_Creep_Colony);
        drone2.changeRawUnitType(AUnitType.Zerg_Sunken_Colony);
        drone3.changeRawUnitType(AUnitType.Zerg_Lurker);
        lurkerEgg.changeRawUnitType(AUnitType.Zerg_Lurker);

        // Behind FoW
        drone4.changeRawUnitType(AUnitType.Zerg_Creep_Colony);
        drone5.changeRawUnitType(AUnitType.Zerg_Sunken_Colony);
        drone6.changeRawUnitType(AUnitType.Zerg_Lurker);

//        EnemyUnits.discovered().print("Enemies");
//        Select.our().print("Ours");

//        System.err.println("i   = " + armyStrengthUnchached()
//            + " / ED:" + EnemyUnits.discovered().size() + " / Fresh:" + EnemyUnits.freshDiscovered().size()
//        );

        EnemyUnitsUpdater.weDiscoveredEnemyUnit(drone4);
        EnemyUnitsUpdater.weDiscoveredEnemyUnit(drone5);
        EnemyUnitsUpdater.weDiscoveredEnemyUnit(drone6);
//        System.err.println("ii  = " + armyStrengthUnchached()
//            + " / ED:" + EnemyUnits.discovered().size() + " / Fresh:" + EnemyUnits.freshDiscovered().size()
//        );

        armyStrengthA = armyStrengthUnchached();
        assertTrue(10 < armyStrengthA && armyStrengthA < 200);

        EnemyUnitsUpdater.weDiscoveredEnemyUnit(hydra);
    }

    private void fifthFrame() {
//        EnemyUnits.discovered().print("Fogged");
//        Select.enemy().print("Visible enemies");

//        System.err.println(EnemyUnits.getFoggedUnit(drone3).type());
//        System.err.println(EnemyUnits.getFoggedUnit(lurkerEgg).type());

        armyStrengthB = armyStrengthUnchached();
//        System.err.println("armyStrengthA = " + armyStrengthA);
//        System.err.println("armyStrengthB = " + armyStrengthB);
        assertTrue(armyStrengthA > armyStrengthB);
        assertTrue(10 < armyStrengthB && armyStrengthB < 110);
//        System.err.println("iii = " + armyStrengthUnchached()
//            + " / ED:" + EnemyUnits.discovered().size() + " / Fresh:" + EnemyUnits.freshDiscovered().size()
//        );

//        System.err.println("BBBBBB lurkerEgg = " + lurkerEgg);
//        EnemyUnits.discovered().print("Hmmm");

        assertEquals(AUnitType.Zerg_Creep_Colony, EnemyUnits.getFoggedUnit(drone1).type());
        assertEquals(AUnitType.Zerg_Sunken_Colony, EnemyUnits.getFoggedUnit(drone2).type());
        assertEquals(AUnitType.Zerg_Lurker, EnemyUnits.getFoggedUnit(drone3).type());
//        assertEquals(AUnitType.Zerg_Lurker, EnemyUnits.getFoggedUnit(lurkerEgg).type());

        assertEquals(AUnitType.Zerg_Creep_Colony, EnemyUnits.getFoggedUnit(drone4).type());
        assertEquals(AUnitType.Zerg_Sunken_Colony, EnemyUnits.getFoggedUnit(drone5).type());
        assertEquals(AUnitType.Zerg_Lurker, EnemyUnits.getFoggedUnit(drone6).type());
    }

    // =========================================================

    protected FakeUnit[] generateOur() {
        return fakeOurs(
            fake(AUnitType.Terran_Marine, 10)
        );
    }

    protected FakeUnit[] generateEnemies() {
        return fakeEnemies(
            geyser = fakeEnemy(AUnitType.Resource_Vespene_Geyser, 18),

            larva = fakeEnemy(AUnitType.Zerg_Larva, 19),
            drone1 = fakeEnemy(AUnitType.Zerg_Drone, 20),
            drone2 = fakeEnemy(AUnitType.Zerg_Drone, 21),
            drone3 = fakeEnemy(AUnitType.Zerg_Drone, 22)
//            drone1 = fake(AUnitType.Zerg_Drone),
//            drone2 = fake(AUnitType.Zerg_Drone),
//            drone3 = fake(AUnitType.Zerg_Drone),
//            drone4 = fake(AUnitType.Zerg_Drone),
//            drone5 = fake(AUnitType.Zerg_Drone),
//            drone6 = fake(AUnitType.Zerg_Drone),
//            lurkerEgg = fake(AUnitType.Zerg_Lurker_Egg),
//            hydra = fake(AUnitType.Zerg_Hydralisk),
        );
    }

    protected FakeUnit[] generateNeutral() {
        return new FakeUnit[]{
            geyser = fake(AUnitType.Resource_Vespene_Geyser)
        };
    }

}
