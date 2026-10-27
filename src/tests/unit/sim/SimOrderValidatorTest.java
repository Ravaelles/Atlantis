package tests.unit.sim;

import atlantis.map.position.APosition;
import atlantis.map.snapshot.MapSnapshot;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;
import tests.unit.AbstractTestWithUnits;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sim order validation pins (StarEngine §3/§5): every bot order the sim
 * consumes is validated like the engine would, and rejections name the
 * rule. Pure geometry + bank - no world, no game, milliseconds.
 */
@Tag("production")
public class SimOrderValidatorTest extends AbstractTestWithUnits {

    /**
     * Two rooms, one wall between them, no link: region 1 | region 2.
     */
    static String twoRoomsJson() {
        StringBuilder tiles = new StringBuilder("[");
        StringBuilder regionOf = new StringBuilder("[");
        for (int ty = 0; ty < 8; ty++) {
            if (ty > 0) {
                tiles.append(',');
                regionOf.append(',');
            }
            StringBuilder row = new StringBuilder();
            StringBuilder reg = new StringBuilder();
            for (int tx = 0; tx < 10; tx++) {
                boolean wall = tx == 4 || tx == 5;
                row.append(wall ? 'x' : '.');
                if (tx > 0) reg.append(',');
                reg.append(tx < 4 ? 1 : 2);
            }
            tiles.append('"').append(row).append('"');
            regionOf.append(reg);
        }
        tiles.append(']');
        regionOf.append(']');

        return "{\"map\":\"Rooms\",\"w\":10,\"h\":8,"
            + "\"tiles\":" + tiles + ","
            + "\"regionOf\":" + regionOf + ","
            + "\"regions\":["
            + "{\"id\":1,\"cx\":2,\"cy\":4,\"neighbors\":[]},"
            + "{\"id\":2,\"cx\":7,\"cy\":4,\"neighbors\":[]}],"
            + "\"chokes\":[],"
            + "\"resources\":["
            + "{\"t\":\"M\",\"x\":1,\"y\":1,\"amount\":1500},"
            + "{\"t\":\"G\",\"x\":2,\"y\":6,\"amount\":5000}],"
            + "\"starts\":[{\"x\":2,\"y\":2}],"
            + "\"units\":[],"
            + "\"bases\":[{\"x\":2,\"y\":2,\"startIndex\":0,"
            + "\"mineralIds\":[0],\"gasIds\":[1]}],"
            + "\"regionReach\":[1,0,0,1]}";
    }

    private SimGround ground() {
        return new SimGround(MapSnapshot.parse(twoRoomsJson()));
    }

    private SimBank rich() {
        return new SimBank(1000, 1000);
    }

    private List<FakeUnit> noBuildings() {
        return new ArrayList<>();
    }

    @Test
    public void openGroundBuildIsAccepted() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 2, 2);

        SimOrder order = SimOrderValidator.validateBuild(
            ground(), probe, AUnitType.Protoss_Pylon, 2, 2, rich(), noBuildings(), true);

        assertTrue(order.accepted(), "open ground builds: " + order);
    }

    @Test
    public void wallFootprintIsRefused() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 2, 2);

        SimOrder order = SimOrderValidator.validateBuild(
            ground(), probe, AUnitType.Protoss_Pylon, 4, 2, rich(), noBuildings(), true);

        assertFalse(order.accepted(), "the wall band is not buildable");
        assertTrue(order.reason.contains("not buildable"), "the reason names the rule: " + order);
    }

    @Test
    public void unreachableSiteIsRefused() {
        // Builder in the left room, site in the right room, regions unlinked.
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 2, 2);

        SimOrder order = SimOrderValidator.validateBuild(
            ground(), probe, AUnitType.Protoss_Pylon, 7, 2, rich(), noBuildings(), true);

        assertFalse(order.accepted(), "no path, no building");
        assertTrue(order.reason.contains("reach"), "the reason names the rule: " + order);
    }

    @Test
    public void unpoweredProtossBuildingIsRefused() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 2, 2);

        SimOrder order = SimOrderValidator.validateBuild(
            ground(), probe, AUnitType.Protoss_Gateway, 0, 2, rich(), noBuildings(), true);

        assertFalse(order.accepted(), "no Pylon, no psi");
        assertTrue(order.reason.contains("psi"), "the reason names the rule: " + order);
    }

    @Test
    public void poweredSiteBuilds() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 2, 2);
        FakeUnit pylon = fake(AUnitType.Protoss_Pylon, 0, 0);
        List<FakeUnit> buildings = new ArrayList<>(Collections.singletonList(pylon));

        SimOrder order = SimOrderValidator.validateBuild(
            ground(), probe, AUnitType.Protoss_Gateway, 0, 2, rich(), buildings, true);

        assertTrue(order.accepted(), "psi covered, footprint open: " + order);
    }

    @Test
    public void refineryNeedsItsGeyser() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 2, 2);

        SimOrder open = SimOrderValidator.validateBuild(
            ground(), probe, AUnitType.Protoss_Assimilator, 2, 2, rich(), noBuildings(), true);
        assertFalse(open.accepted(), "no geyser under the tile");

        SimOrder onGas = SimOrderValidator.validateBuild(
            ground(), probe, AUnitType.Protoss_Assimilator, 2, 6, rich(), noBuildings(), true);
        assertTrue(onGas.accepted(), "the geyser tile takes the refinery: " + onGas);
    }

    @Test
    public void brokeBankIsRefused() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 2, 2);
        SimBank broke = new SimBank(10, 0);

        SimOrder order = SimOrderValidator.validateBuild(
            ground(), probe, AUnitType.Protoss_Pylon, 2, 2, broke, noBuildings(), true);

        assertFalse(order.accepted(), "10 minerals buy no Pylon");
        assertTrue(order.reason.startsWith("bank cannot cover"), "funding reason: " + order);
    }

    @Test
    public void moveNeedsWalkableDestinationAndPath() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 2, 2);
        SimGround ground = ground();

        assertTrue(SimOrderValidator.validateMove(
            ground, probe, APosition.create(2, 6)).accepted(), "open ground walks");
        assertFalse(SimOrderValidator.validateMove(
            ground, probe, APosition.create(4, 2)).accepted(), "walls do not walk");
        assertFalse(SimOrderValidator.validateMove(
            ground, probe, APosition.create(7, 2)).accepted(), "no path, no move");
    }

    @Test
    public void trainNeedsLiveCompletedFacilityAndBank() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 2, 2);
        FakeUnit dead = fake(AUnitType.Protoss_Nexus, 2, 2);
        dead.hp = 0;

        assertFalse(SimOrderValidator.validateTrain(
            dead, AUnitType.Protoss_Probe, rich()).accepted(), "corpses train nothing");

        FakeUnit unfinished = fake(AUnitType.Protoss_Gateway, 2, 2);
        unfinished.setCompleted(false);
        assertFalse(SimOrderValidator.validateTrain(
                unfinished, AUnitType.Protoss_Zealot, rich()).accepted(),
            "a warping Gateway trains nothing");

        assertFalse(SimOrderValidator.validateTrain(
                nexus, AUnitType.Protoss_Probe, new SimBank(10, 0)).accepted(),
            "10 minerals train no Probe");

        assertTrue(SimOrderValidator.validateTrain(
            nexus, AUnitType.Protoss_Probe, rich()).accepted(), "funded Nexus trains");
    }
}
