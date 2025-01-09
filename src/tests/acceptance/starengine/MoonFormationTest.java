package tests.acceptance.starengine;

import atlantis.combat.squad.positioning.formations.moon.MoonUnitPositionsCalculator;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import java.util.Map;
import java.util.concurrent.Callable;

public class MoonFormationTest extends WorldStubForTests {
    private FakeUnit dragoon;
    private FakeUnit sunken;

    @Test
    public void moonShape() {
        Callable ours = () -> fakeOurs(
            dragoon = fake(AUnitType.Protoss_Dragoon, 10, 10),
            fake(AUnitType.Protoss_Dragoon, 11),
            fake(AUnitType.Protoss_Zealot, 11.1),
            fake(AUnitType.Protoss_Zealot, 11.9)
        );
        Callable enemies = () -> fakeEnemies(
            sunken = fake(AUnitType.Zerg_Sunken_Colony, 15, 25)
//            fake(AUnitType.Zerg_Larva, 12)
        );
//        dragoon = fake(AUnitType.Protoss_Dragoon, 10),
//        sunken = fake(AUnitType.Zerg_Sunken_Colony, 20)

        useStarEngine();

        createWorld(60, () -> {
                Selection units = dragoon.friendsNear().combatUnits().add(dragoon);
                AUnit leader = dragoon;
                HasPosition center = sunken;
                double radius = 9;
                double separation = 2;

                Map<AUnit, APosition> positions = MoonUnitPositionsCalculator.calculateUnitPositions(
                    units, center, leader, radius, separation
                );

                // Print the positions
                for (AUnit unit : positions.keySet()) {
                    APosition position = positions.get(unit);
//                    System.out.println(unit.idWithType() + " - " + position);

//                    if (A.now >= 40) {
                    unit.move(position, Actions.MOVE_FORMATION);
//                    }
                }
            },
            ours,
            enemies
        );
    }
}
