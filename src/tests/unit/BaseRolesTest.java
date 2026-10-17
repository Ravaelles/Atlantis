package tests.unit;

import atlantis.map.analysis.BaseRoles;
import atlantis.map.position.APosition;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * M1 model slice: base roles are pure arithmetic over nodes, ground links
 * and our start - no game, no BWEM, no statics. Every rule below mirrors the
 * old port ({@code Stations.findStations}) so the wiring slice can swap
 * implementations without re-deciding anything.
 */
public class BaseRolesTest {

    private static BaseRoles.BaseNode node(int tx, int ty, boolean start, int minerals, int gas) {
        return new BaseRoles.BaseNode(APosition.create(tx, ty), start, minerals, gas);
    }

    /** All connected, Euclidean ground measure. */
    private static BaseRoles.GroundLink mesh() {
        return new BaseRoles.GroundLink() {
            @Override
            public boolean connected(BaseRoles.BaseNode a, BaseRoles.BaseNode b) {
                return true;
            }

            @Override
            public double groundDistance(BaseRoles.BaseNode a, BaseRoles.BaseNode b) {
                return a.position().distTo(b.position());
            }
        };
    }

    private static Map<BaseRoles.Role, APosition> rolesOf(List<BaseRoles.AssignedBase> assigned) {
        Map<BaseRoles.Role, APosition> roles = new HashMap<>();
        for (BaseRoles.AssignedBase base : assigned) {
            roles.put(base.role(), base.node().position());
        }
        return roles;
    }

    @Test
    public void mainIsOurStartAndNaturalIsTheNearestRichBase() {
        List<BaseRoles.BaseNode> nodes = new ArrayList<>();
        nodes.add(node(10, 10, true, 8, 1));
        nodes.add(node(30, 10, true, 8, 1));
        nodes.add(node(16, 10, false, 8, 1));
        nodes.add(node(50, 50, false, 8, 1));

        Map<BaseRoles.Role, APosition> roles = rolesOf(
            BaseRoles.assign(nodes, APosition.create(10, 10), mesh()));

        assertEquals(APosition.create(10, 10), roles.get(BaseRoles.Role.MAIN));
        assertEquals(APosition.create(16, 10), roles.get(BaseRoles.Role.NATURAL));
        assertEquals(APosition.create(50, 50), roles.get(BaseRoles.Role.EXPANSION));
    }

    @Test
    public void gaslessBasesAreMineralOnlyAndDisconnectedAreIslands() {
        List<BaseRoles.BaseNode> nodes = new ArrayList<>();
        BaseRoles.BaseNode main = node(10, 10, true, 8, 1);
        BaseRoles.BaseNode gasless = node(16, 10, false, 8, 0);
        BaseRoles.BaseNode island = node(100, 100, false, 8, 1);
        nodes.add(main);
        nodes.add(gasless);
        nodes.add(island);

        BaseRoles.GroundLink link = new BaseRoles.GroundLink() {
            @Override
            public boolean connected(BaseRoles.BaseNode a, BaseRoles.BaseNode b) {
                return !(a == island || b == island);
            }

            @Override
            public double groundDistance(BaseRoles.BaseNode a, BaseRoles.BaseNode b) {
                return a.position().distTo(b.position());
            }
        };

        Map<BaseRoles.Role, APosition> roles = rolesOf(BaseRoles.assign(nodes, APosition.create(10, 10), link));

        assertEquals(APosition.create(10, 10), roles.get(BaseRoles.Role.MAIN));
        assertEquals(gasless.position(), roles.get(BaseRoles.Role.MINERAL_ONLY));
        assertEquals(island.position(), roles.get(BaseRoles.Role.ISLAND));
    }

    @Test
    public void poorBasesNeverBecomeNatural() {
        List<BaseRoles.BaseNode> nodes = new ArrayList<>();
        nodes.add(node(10, 10, true, 8, 1));
        nodes.add(node(14, 10, false, 2, 1));
        nodes.add(node(20, 10, false, 8, 1));

        Map<BaseRoles.Role, APosition> roles = rolesOf(
            BaseRoles.assign(nodes, APosition.create(10, 10), mesh()));

        assertEquals(APosition.create(20, 10), roles.get(BaseRoles.Role.NATURAL));
        assertEquals(APosition.create(14, 10), roles.get(BaseRoles.Role.EXPANSION));
    }

    @Test
    public void emptyMapAssignsNothing() {
        assertEquals(0, BaseRoles.assign(
            new ArrayList<>(), APosition.create(10, 10), mesh()).size());
    }
}
