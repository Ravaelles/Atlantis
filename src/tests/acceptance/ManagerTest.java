package tests.acceptance;

import atlantis.architecture.Manager;
import atlantis.combat.CombatUnitManager;
import atlantis.combat.micro.terran.TerranComsatStation;
import atlantis.game.A;
import atlantis.protoss.dragoon.ProtossDragoonCombatManager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.special.ManualOverrideManager;
import atlantis.units.special.RemoveDeadUnitsManager;
import atlantis.units.special.SpecialUnitsManager;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ManagerTest extends WorldStubForTests {
    @Test
    public void noExceptionIsThrown() {
        boolean status = false;
        FakeUnit comsat = fake(AUnitType.Terran_Comsat_Station, 11.9);
        TerranComsatStation comsatManager = null;

        try {
            TerranComsatStation.class.getDeclaredConstructor(AUnit.class);
            comsatManager = TerranComsatStation.class.getDeclaredConstructor(AUnit.class).newInstance(comsat);

            SpecialUnitsManager.class.getDeclaredConstructor(AUnit.class).newInstance(comsat);

            status = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertTrue(status);
        assertNotNull(comsatManager);
    }

    @Test
    public void canInstantiateManagers() {
        FakeUnit comsat;
        FakeUnit[] our = fakeOurs(
            comsat = fake(AUnitType.Terran_Comsat_Station, 11.9)
        );
        FakeUnit[] enemies = fakeEnemies(
            fake(AUnitType.Zerg_Sunken_Colony, 12)
        );

        createWorld(1, () -> {
                Manager comsatManager = (new TerranComsatStation(comsat)).instantiateManager(TerranComsatStation.class);

                assertNotNull(comsatManager);
            },
            () -> our,
            () -> enemies
        );
    }

    @Test
    public void managersBasicLogic() {
        FakeUnit unit;
        FakeUnit[] our = fakeOurs(
            fake(AUnitType.Terran_Command_Center, 10),
            fake(AUnitType.Terran_SCV, 11),
            fake(AUnitType.Terran_SCV, 12),
            fake(AUnitType.Terran_SCV, 13),
            fake(AUnitType.Terran_SCV, 14),
            unit = fake(AUnitType.Terran_Marine, 20),
            fake(AUnitType.Terran_Marine, 21)
        );
        FakeUnit[] enemies = fakeEnemies(
            fake(AUnitType.Zerg_Sunken_Colony, 40)
        );

        createWorld(4, () -> {
//                Manager combatManager = (new CombatUnitManager(unit)).instantiateManager(TerranComsatStation.class);
                Manager combatManager = (new CombatUnitManager(unit)).forceHandle();

//                System.err.println("combatManager = " + combatManager);
//                System.err.println(unit.manager());

                assertNotNull(combatManager);

                if (A.now() == 1) {
                    unit.managerLogs().addMessage("MessageA", unit);
                    System.err.println("Size at 1 = " + unit.managerLogs().messages().size());
                }
                else if (A.now() == 2) {
                    System.err.println("Size at 2 = " + unit.managerLogs().messages().size());
                    unit.managerLogs().addMessage("MessageB", unit);
                }
                else if (A.now() == 4) {
                    System.err.println("Size at 4 = " + unit.managerLogs().messages().size());
                    System.out.println(unit.managerLogs().lastMessage());
                    System.out.println(unit.managerLogs().toString());
                    System.out.println(unit.managerLogs().messages());
                }
            },
            () -> our,
            () -> enemies
        );
    }

    @Test
    public void managersParentStack() {
        FakeUnit unit;
        FakeUnit[] our = fakeOurs(
            unit = new FakeUnit(AUnitType.Protoss_Dragoon, 10, 11)
        );

        ManualOverrideManager manager1 = new ManualOverrideManager(unit);
        ProtossDragoonCombatManager manager2 = new ProtossDragoonCombatManager(unit);
        RemoveDeadUnitsManager manager3 = new RemoveDeadUnitsManager(unit);

        createWorld(1, () -> {
                assertEquals("", manager1.parentsStack());
                assertEquals("", manager2.parentsStack());
                assertEquals("", manager3.parentsStack());

                manager2.invokeFrom(manager1);
                manager3.invokeFrom(manager2);

                assertEquals("", manager1.parentsStack());
                assertEquals("ManualOverrideManager > ", manager2.parentsStack());
                assertEquals("ManualOverrideManager > ProtossDragoonCombatManager > ", manager3.parentsStack());
            },
            () -> our,
            () -> fakeEnemies()
        );

    }
}