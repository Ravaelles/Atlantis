package tests.acceptance;

import atlantis.OnUnitDiscover;
import atlantis.game.*;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnitType;
import org.junit.Test;
import tests.unit.FakeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FocusPointTest extends AbstractTestFakingGame {

    private AGameCommander gameCommander;
    private FakeUnit assimilator;
    private FakeUnit drone2;
    private FakeUnit drone3;
    private FakeUnit drone4;
    private FakeUnit drone5;
    private FakeUnit drone6;
    private FakeUnit geyser;
    private FakeUnit lurkerEgg;
    private FakeUnit larva;

    @Test
    public void neverRunsIntoCombatBuildings() {
        gameCommander = new AGameCommander();

        createWorld(5, () -> {
//            System.out.println("\n===================== FRAME = " + A.now() + " ===========================");

            if (A.now() == 1) {
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

            gameCommander.update();
        });
    }

    private void firstFrame() {
        OnUnitDiscover.update(assimilator);
//        EnemyInfo.weDiscoveredEnemyUnit(assimilator);
    }

    private void secondFrame() {
        EnemyUnits.selection().print();

        // Fake type change
        assimilator.changeRawUnitType(AUnitType.Resource_Vespene_Geyser);

//        OnUnitDestroyed.update(assimilator);
        OnUnitMorph.update(assimilator);

        EnemyUnits.selection().print();

//        // Visible
//        assimilator.changeRawUnitType(AUnitType.Zerg_Creep_Colony);
//        drone2.changeRawUnitType(AUnitType.Zerg_Sunken_Colony);
//        drone3.changeRawUnitType(AUnitType.Zerg_Lurker);
//        geyser.changeRawUnitType(AUnitType.Zerg_Extractor);
//        geyser.setEnemy();
//
//        // Behind FoW
//        drone4.changeRawUnitType(AUnitType.Zerg_Creep_Colony);
//        drone5.changeRawUnitType(AUnitType.Zerg_Sunken_Colony);
//        drone6.changeRawUnitType(AUnitType.Zerg_Lurker);
//
//        OnUnitMorph.update(assimilator);
//        OnUnitMorph.update(drone2);
//        OnUnitMorph.update(drone3);
////        OnUnitMorph.update(geyser);
//        OnUnitRenegade.update(geyser);
    }

    private void thirdFrame() {
//        assertEquals(AUnitType.Zerg_Creep_Colony, EnemyUnits.getFoggedUnit(assimilator).type());
//        assertEquals(AUnitType.Zerg_Sunken_Colony, EnemyUnits.getFoggedUnit(drone2).type());
//        assertEquals(AUnitType.Zerg_Lurker, EnemyUnits.getFoggedUnit(drone3).type());
//        assertEquals(AUnitType.Zerg_Extractor, EnemyUnits.getFoggedUnit(geyser).type());
//
//        assertNull(EnemyUnits.getFoggedUnit(drone4));
//        assertNull(EnemyUnits.getFoggedUnit(drone5));
//        assertNull(EnemyUnits.getFoggedUnit(drone6));
    }

    private void forthFrame() {

//        // Visible
//        assimilator.changeRawUnitType(AUnitType.Zerg_Creep_Colony);
//        drone2.changeRawUnitType(AUnitType.Zerg_Sunken_Colony);
//        drone3.changeRawUnitType(AUnitType.Zerg_Lurker);
//        lurkerEgg.changeRawUnitType(AUnitType.Zerg_Lurker);
//
//        // Behind FoW
//        drone4.changeRawUnitType(AUnitType.Zerg_Creep_Colony);
//        drone5.changeRawUnitType(AUnitType.Zerg_Sunken_Colony);
//        drone6.changeRawUnitType(AUnitType.Zerg_Lurker);
//
//        EnemyInfo.weDiscoveredEnemyUnit(drone4);
//        EnemyInfo.weDiscoveredEnemyUnit(drone5);
//        EnemyInfo.weDiscoveredEnemyUnit(drone6);
    }

    private void fifthFrame() {
//        EnemyUnits.unitsDiscoveredSelection().print("Fogged");
//        Select.enemy().print("Visible enemies");

//        assertEquals(AUnitType.Zerg_Creep_Colony, EnemyUnits.getFoggedUnit(assimilator).type());
//        assertEquals(AUnitType.Zerg_Sunken_Colony, EnemyUnits.getFoggedUnit(drone2).type());
//        assertEquals(AUnitType.Zerg_Lurker, EnemyUnits.getFoggedUnit(drone3).type());
//        assertEquals(AUnitType.Zerg_Lurker, EnemyUnits.getFoggedUnit(lurkerEgg).type());
//
//        assertEquals(AUnitType.Zerg_Creep_Colony, EnemyUnits.getFoggedUnit(drone4).type());
//        assertEquals(AUnitType.Zerg_Sunken_Colony, EnemyUnits.getFoggedUnit(drone5).type());
//        assertEquals(AUnitType.Zerg_Lurker, EnemyUnits.getFoggedUnit(drone6).type());
    }

    // =========================================================

    protected FakeUnit[] generateOur() {
        return fakeOurs(
                fake(AUnitType.Terran_Marine, 10)
        );
    }

    protected FakeUnit[] generateEnemies() {
        return fakeEnemies(
                assimilator = fakeEnemy(AUnitType.Protoss_Assimilator, 11)
        );
    }

    protected FakeUnit[] generateNeutral() {
        return new FakeUnit[] {
                geyser = fake(AUnitType.Resource_Vespene_Geyser, 9)
        };
    }

}
