package tests.aux.repro;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.commanders.ConstructionStatusChanger;
import atlantis.units.AUnitType;
import tests.fakes.FakeUnit;
import tests.unit.sim.SimArena;
import tests.unit.sim.SimWorld;

/**
 * One-sentence README: drives a STARTING Nexus with a live gathering builder past the 2400f horizon and checks the PARKED_STARTING twin fires.
 *
 * Shape under test (owner live report: 9-min silent Nexus park, builder assigned, no twins): demoteParkedStarting shields on motion/gathering so the row parks forever with no witness. Oracle: twinParkedStarting returns true once, then false (once-per-construction), status stays STARTING. Exit codes: 0 = HOLDS (twins once, no demote), 1 = SILENT (bug reproduced: no twin on an ancient parked row), 2 = INCONCLUSIVE (world setup failed).
 */
public final class ParkedStartingRepro {

    public static void main(String[] args) throws Exception {
        org.mockito.MockedStatic<atlantis.game.AGame> aGame =
            org.mockito.Mockito.mockStatic(atlantis.game.AGame.class);
        aGame.when(atlantis.game.AGame::now).thenReturn(3000);
        aGame.when(atlantis.game.AGame::supplyUsed).thenReturn(20);
        aGame.when(atlantis.game.AGame::supplyTotal).thenReturn(33);
        aGame.when(atlantis.game.AGame::supplyFree).thenReturn(13);
        aGame.when(atlantis.game.AGame::minerals).thenReturn(400);
        aGame.when(atlantis.game.AGame::gas).thenReturn(100);

        org.mockito.MockedStatic<main.Main> ourRace =
            org.mockito.Mockito.mockStatic(main.Main.class);
        ourRace.when(main.Main::ourRace).thenReturn("Protoss");

        int verdict = 2;
        SimWorld world = null;
        try {
            ConstructionRequests.constructions.clear();

            FakeUnit nexus = new FakeUnit(AUnitType.Protoss_Nexus, 20, 20);
            FakeUnit probe = new FakeUnit(AUnitType.Protoss_Probe, 30, 30);
            world = new SimWorld(
                new FakeUnit[]{ nexus, probe }, new FakeUnit[]{},
                new SimArena(100 * 32, 60 * 32));

            Construction construction = new Construction(AUnitType.Protoss_Nexus);
            construction.setBuilder(probe);
            construction.setPositionToBuild(APosition.create(12, 10));
            construction.setStatus(ConstructionOrderStatus.STARTING);

            System.out.println("=== ParkedStartingRepro ===");
            boolean first = ConstructionStatusChanger.twinParkedStarting(construction, probe);
            boolean second = ConstructionStatusChanger.twinParkedStarting(construction, probe);
            System.out.println("  first twin: " + first + ", second twin: " + second
                + ", status: " + construction.status());

            if (first && !second
                && construction.status() == ConstructionOrderStatus.STARTING) {
                System.out.println("RESULT: HOLDS - parked row names itself once, never demotes");
                verdict = 0;
            } else if (!first) {
                System.out.println("RESULT: SILENT - ancient parked STARTING produced no twin");
                verdict = 1;
            } else {
                System.out.println("RESULT: INCONCLUSIVE - unexpected twin/demote shape");
                verdict = 2;
            }
        } finally {
            if (world != null) world.close();
            ConstructionRequests.constructions.clear();
            ourRace.close();
            aGame.close();
        }
        System.exit(verdict);
    }
}
