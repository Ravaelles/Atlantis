package tests.unit.sim;

import atlantis.battle.cluster.Alpha;
import atlantis.battle.cluster.Missions;
import atlantis.combat.BattleCommander;
import atlantis.combat.FightTarget;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeMapTiles;
import tests.fakes.FakeUnit;
import tests.unit.AbstractTestWithUnits;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Attack march must not park mid-map (Wine 20261030_123059: mission
 * ATTACK_ENEMY_BASE, anchor [33,8], the army idled at (47-52, 96-105)
 * for 2000+ frames with no contact - ClusterMove/ClusterForm IDLE tags,
 * zero displacement toward the anchor). A scattered line with a far
 * attack anchor and no contact marches instead of bunching in place.
 */
@Tag("combat")
@Tag("full-tier")
public class AttackMarchResumesSimTest extends AbstractTestWithUnits {

    private static final int TILE = 32;
    private static final int TICKS = 1500;

    private static FakeUnit dragoon(double tx, double ty) {
        FakeUnit d = fake(AUnitType.Protoss_Dragoon, tx, ty);
        d.hp = 180;
        d.shields = 80;
        d.cooldown = 0;

        return d;
    }

    private static FakeUnit foe(double tx, double ty) {
        FakeUnit z = fake(AUnitType.Zerg_Zergling, tx, ty);
        z.setEnemy();

        return z;
    }

    private static void useArenaWalkability(final SimArena arena) {
        atlantis.map.MapTiles.useSource(new atlantis.map.MapTiles.Source() {
            @Override
            public boolean isWalkable(atlantis.map.position.HasPosition at) {
                atlantis.map.position.APosition p = at.position();

                return arena.isWalkablePx(p.x(), p.y());
            }

            @Override
            public boolean isExplored(atlantis.map.position.HasPosition at) {
                return true;
            }

            @Override
            public boolean isVisible(atlantis.map.position.HasPosition at) {
                return true;
            }

            @Override
            public boolean isBuildable(atlantis.map.position.HasPosition at, boolean alsoCheckBuildings) {
                return true;
            }

            @Override
            public boolean hasPathBetween(atlantis.map.position.HasPosition from, atlantis.map.position.HasPosition to) {
                return true;
            }

            @Override
            public boolean canBuildHere(atlantis.units.AUnit builder, atlantis.units.AUnitType building,
                                        atlantis.map.position.APosition at) {
                return true;
            }
        });
    }

    @Test
    public void scatteredLineMarchesToAFarAttackAnchor() {        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit a = dragoon(30, 18);
        FakeUnit b = dragoon(31, 20);
        FakeUnit c = dragoon(29, 22);
        FakeUnit foe = foe(33, 19);
        List<FakeUnit> ours = new ArrayList<>();
        ours.add(a);
        ours.add(b);
        ours.add(c);
        List<FakeUnit> foes = new ArrayList<>();
        foes.add(foe);

        SimArena arena = new SimArena(70 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(
            new FakeUnit[]{a, b, c, nexus}, foes.toArray(new FakeUnit[0]), arena);

        useArenaWalkability(arena);
        APosition anchor = APosition.create(60, 20);

        try {
            BattleCommander.clearForTests();
            EventLog.clearForTests();
            Missions.forceGlobalMissionAttack("test-attack-march");
            Alpha.gather();

            double[] dist = new double[16];
            StringBuilder ghosts = new StringBuilder();
            for (int i = 0; i < 1500; i++) {
                world.tick(() -> {
                    ZombiePolicies.chaseNearest(foes, ours);
                    for (FakeUnit u : ours) {
                        if (u.hp <= 0) continue;
                        BattleCommander.act(u, FightOrFlight.Tactic.FIGHT, anchor, a);
                    }
                    if (foe.hp <= 0) return;
                    if (atlantis.game.A.now() >= 300) foe.hp = 0;
                });
                if (i % 100 == 99) {
                    double d = 0;
                    for (FakeUnit u : ours) d += u.distTo(anchor);
                    dist[i / 100] = d / ours.size();
                    atlantis.map.position.APosition g = FightTarget.lastContact(a);
                    ghosts.append('[').append(i).append(':')
                        .append(g == null ? "none" : ((int) a.distTo(g) + "@" + a.lastCommand
                            + " a@" + ((int) a.tx()) + "," + ((int) a.ty())
                            + " g@" + ((int) g.tx()) + "," + ((int) g.ty()))).append(']');
                }
            }

            StringBuilder trail = new StringBuilder();
            for (int i = 0; i < dist.length; i++) trail.append(i).append(':').append((int) dist[i]).append(' ');
            String all = EventLog.toJsonLines();
            java.util.Map<String, Integer> idle = new java.util.TreeMap<>();
            java.util.Map<String, Integer> moves = new java.util.TreeMap<>();
            for (String row : all.split("\n")) {
                if (row.contains("\"key\":\"IDLE\"")) {
                    int t = row.indexOf("\"tag\":\"");
                    if (t >= 0) {
                        String tag = row.substring(t + 7, row.indexOf("\"", t + 7));
                        idle.merge(tag, 1, Integer::sum);
                    }
                }
            }
            for (tests.fakes.FakeUnit u : ours) {
                for (tests.fakes.FakeOrderSink.Record r : ((tests.fakes.FakeOrderSink) u.orderSink()).orders()) {
                    if ("move".equals(r.operation)) {
                        moves.merge(r.detail, 1, Integer::sum);
                    }
                }
            }
            java.util.List<java.util.Map.Entry<String, Integer>> top = new java.util.ArrayList<>(moves.entrySet());
            top.sort((x, y) -> y.getValue() - x.getValue());
            StringBuilder topDests = new StringBuilder();
            for (int k = 0; k < Math.min(6, top.size()); k++) {
                topDests.append(top.get(k).getKey()).append('x').append(top.get(k).getValue()).append(' ');
            }
            int formations = all.split("FORMATION_ORDER", -1).length - 1;
            int kites = all.split("stepped-aside", -1).length - 1;
            int ghostRows = all.split("ClusterGhost", -1).length - 1;
            double endDist = 0;
            for (FakeUnit u : ours) endDist += u.distTo(anchor);
            endDist /= ours.size();
            atlantis.map.position.APosition lastKill = FightTarget.lastContact(a);
            double endGhost = lastKill == null ? Double.MAX_VALUE : a.distTo(lastKill);
            double startDist = dist[0];
            assertTrue(endGhost > atlantis.combat.strategy.GhostAdvance.LOST_TILES
                && endDist < startDist - 5.0,
                "after contact is lost mid-map the line abandons the kill site and resumes "
                    + "the march to a far ATTACK anchor, no standing on the ghost: " + trail
                    + " ghosts:" + ghosts + " endGhost=" + (int) endGhost
                    + " endDist=" + (int) endDist + " startDist=" + (int) startDist
                    + " formationRows=" + formations + " kiteSteps=" + kites + " ghostRows=" + ghostRows
                    + " idle=" + idle + " topMoves=" + topDests);
        } finally {
            world.close();
            BattleCommander.clearForTests();
            EventLog.clearForTests();
            Missions.reset();
            tests.fakes.FakeMapTiles.installAsSource();
        }
    }

    @Test
    public void hoveredLineMarchesInsteadOfRedressing() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit a = dragoon(25, 14);
        FakeUnit b = dragoon(26, 16);
        FakeUnit c = dragoon(24, 17);
        List<FakeUnit> ours = new ArrayList<>();
        ours.add(a);
        ours.add(b);
        ours.add(c);

        SimArena arena = new SimArena(70 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(
            new FakeUnit[]{a, b, c, nexus}, new FakeUnit[0], arena);

        useArenaWalkability(arena);
        APosition anchor = APosition.create(60, 20);

        double startDist = 0;
        for (FakeUnit u : ours) startDist += u.distTo(anchor);
        startDist /= ours.size();

        try {
            BattleCommander.clearForTests();
            EventLog.clearForTests();
            Missions.forceGlobalMissionAttack("test-attack-march");
            Alpha.gather();

            StringBuilder branches = new StringBuilder();
            StringBuilder trail = new StringBuilder();
            double endSlotSum = 0;
            double endAnchorSum = 0;
            int endSamples = 0;
            for (int f = 0; f < 800; f++) {
                final int now = f;
                world.tick(() -> {
                    for (FakeUnit u : ours) {
                        BattleCommander.act(u, FightOrFlight.Tactic.FIGHT, anchor, a);
                    }
                });
                if (f >= 600) {
                    for (FakeUnit u : ours) {
                        atlantis.map.position.HasPosition slot =
                            atlantis.combat.cluster.ClusterRally.formationSlotFor(
                                u, a, anchor, FightOrFlight.Tactic.FIGHT);
                        if (slot != null) endSlotSum += u.distTo(slot);
                        endAnchorSum += u.distTo(anchor);
                        endSamples++;
                    }
                }
                if (f % 20 == 19) {
                    trail.append(f).append(':');
                    for (FakeUnit u : ours) {
                        atlantis.map.position.APosition t = u.targetPosition;
                        trail.append(u.id()).append("@(").append((int) u.tx()).append(',').append((int) u.ty())
                            .append(")->(").append(t == null ? "-,-" : ((int) t.tx() + "," + (int) t.ty()))
                            .append(") ");
                    }
                    trail.append(';');
                }
                if (f % 100 == 99) {
                    branches.append('[').append(f).append(':');
                    for (FakeUnit u : ours) {
                        atlantis.combat.strategy.BattleStrategy st =
                            atlantis.combat.strategy.BattleStrategies.pick(u, anchor);
                        atlantis.map.position.HasPosition slot =
                            atlantis.combat.cluster.ClusterRally.formationSlotFor(
                                u, a, anchor, FightOrFlight.Tactic.FIGHT);
                        atlantis.map.position.APosition ghost =
                            atlantis.combat.strategy.GhostAdvance.effectiveGhostPointFor(u);
                        boolean ongoing = atlantis.combat.ArmyContact.isOngoingFight();
                        double dAnchor;
                        try {
                            dAnchor = u.distTo(anchor);
                        } catch (Exception ignored) {
                            dAnchor = -1;
                        }
                        boolean home = false;
                        try {
                            atlantis.combat.cluster.HomeGround hg =
                                atlantis.combat.cluster.HomeGround.resolve();
                            AUnit main = hg != null ? hg.main() : null;
                            home = atlantis.combat.cluster.ClusterRally.isHomeAnchor(anchor, main);
                        } catch (Exception ignored) {
                        }
                        branches.append(u.id()).append('{').append(st)
                            .append(slot == null ? ",noslot" : ",slot" + (int) u.distTo(slot))
                            .append(ghost == null ? ",noghost" : ",gh" + (int) u.distTo(ghost))
                            .append(",dA").append(String.format("%.1f", dAnchor))
                            .append(ongoing ? ",ONGOING" : ",quiet")
                            .append(home ? ",HOME" : ",away")
                            .append(',').append(u.lastCommand).append('}');
                    }
                    branches.append(']');
                }
            }

            double endDist = 0;
            for (FakeUnit u : ours) endDist += u.distTo(anchor);
            endDist /= ours.size();
            double endSlot = endSamples == 0 ? -1 : endSlotSum / endSamples;
            double endAnchor = endSamples == 0 ? -1 : endAnchorSum / endSamples;

            assertTrue(endDist < 14.0,
                "far attack anchor with no contact marches instead of redressing slots: "
                    + startDist + " -> " + endDist + " branches:" + branches + " trail:" + trail);
            assertTrue(endSlot >= 0 && endSlot < 1.5 && endAnchor < 10.0,
                "arrived line dresses at the attack anchor instead of hovering: endSlot="
                    + String.format("%.2f", endSlot) + " endAnchor=" + String.format("%.2f", endAnchor)
                    + " branches:" + branches + " trail:" + trail);
        } finally {
            world.close();
            BattleCommander.clearForTests();
            EventLog.clearForTests();
            Missions.reset();
            tests.fakes.FakeMapTiles.installAsSource();
        }
    }
}
