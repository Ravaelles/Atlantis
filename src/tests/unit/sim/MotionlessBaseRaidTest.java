package tests.unit.sim;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.BattleCommander;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;
import tests.unit.AbstractTestWithUnits;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * StarEngine motionless-base raid (owner order): the enemy sits on a Nexus
 * with 10 Zealots holding still in their base. A Dragoon mass staged at home
 * must go there and win - the live suspicion is that the army waits at home
 * (it did, before the EnemyMainBase latch fix), so this pins the capability
 * half: ordered at the enemy base, the army CAN kill it.
 *
 * <p>Not a mission-layer test: the focus that sends the army is proven live
 * (010715 marched to [95,119] and won; 013929 held it). What this guards is
 * the fight itself - per-unit logic gluing, kiting away, or losing a
 * straight trade against motionless defenders would fail here first.</p>
 */
@Tag("combat")
@Tag("full-tier")
public class MotionlessBaseRaidTest extends AbstractTestWithUnits {

    private static final int TILE = 32;

    private static FakeUnit dragoon(double tx, double ty) {
        FakeUnit z = fake(AUnitType.Protoss_Dragoon, tx, ty);
        z.hp = 180;
        z.shields = 20;
        z.cooldown = 0;

        return z;
    }

    private static FakeUnit zealot(double tx, double ty) {
        FakeUnit z = fake(AUnitType.Protoss_Zealot, tx, ty);
        z.hp = 100;
        z.cooldown = 0;

        return z;
    }

    private static FakeUnit enemyZealot(double tx, double ty) {
        FakeUnit z = zealot(tx, ty);
        z.setEnemy();

        return z;
    }

    /**
     * Owner 2026-11-01: the pin is that the raid ATTACKS the base, not that
     * it kills everything (time is secondary; weird non-attacking behavior
     * is the defect). The Nexus below 95% hp means attacked; the loop exits
     * on first damage, so the dead-tick grind never runs. Collapse commits
     * the march (20 tiles out, per-tick AI to contact); the trade itself is
     * owned by the small sims.
     */
    @Test
    public void dragoonMassRaidsMotionlessBaseAndAttacksIt() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit foeNexus = fake(AUnitType.Protoss_Nexus, 110, 110);
        foeNexus.setEnemy();
        foeNexus.hp = 1500;

        List<FakeUnit> ours = new ArrayList<>();
        for (int i = 0; i < 12; i++) ours.add(dragoon(88 + i, 88));
        ours.add(nexus);

        List<FakeUnit> foes = new ArrayList<>(Arrays.asList(
            foeNexus,
            enemyZealot(108, 108), enemyZealot(109, 108), enemyZealot(110, 108),
            enemyZealot(108, 109), enemyZealot(109, 109), enemyZealot(110, 109),
            enemyZealot(108, 110), enemyZealot(109, 110), enemyZealot(110, 110),
            enemyZealot(111, 108)));

        SimArena arena = new SimArena(130 * TILE, 130 * TILE);
        SimWorld world = new SimWorld(
            ours.toArray(new FakeUnit[0]), foes.toArray(new FakeUnit[0]), arena);
        int attackFrame = -1;
        try {
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
            Alpha.gather();

            for (int i = 0; i < 6000; i++) {
                world.tick(() -> {
                    for (FakeUnit u : ours) {
                        if (u.hp <= 0 || !u.type().equals(AUnitType.Protoss_Dragoon)) continue;
                        BattleCommander.act(u, FightOrFlight.Tactic.FIGHT, foeNexus.position(), u);
                    }
                    ZombiePolicies.holdStill(foes, ours);
                });
                if (foeNexus.hp < 1500 * 0.95) {
                    attackFrame = world.frame();
                    break;
                }
            }
        }
        finally {
            world.close();
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
        }

        assertTrue(attackFrame >= 0,
            "the raid attacks the motionless base instead of holding standoff "
                + "forever (Nexus hp: " + foeNexus.hp + "/1500)");
    }
}
