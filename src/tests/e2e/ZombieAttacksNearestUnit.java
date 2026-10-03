package tests.e2e;

import atlantis.util.AMath;
import tests.acceptance.FakeOnFrameEnd;
import tests.fakes.FakeUnit;

import java.util.List;

/**
 * The opponent in scenario tests: no build order, no micro, no mercy.
 *
 * <p>Each zombie chases the nearest living victim at its own top speed and
 * mauls it when in reach (via {@code attackUnit}, so our managers observe a
 * genuinely attacking enemy). Damage itself is resolved by
 * {@link ScenarioCombat}, not here - this class is behaviour only.</p>
 *
 * <p>This is deliberately the dumbest opponent that still rushes: a fixed,
 * exact-timing attack with zero variance. A ladder bot "sometimes rushes";
 * zombies always do, which is what makes a defense test reproducible. When
 * the OpenBW runner (see {@code _AI/IDEA-E2E-TESTS.md}) can host Atlantis,
 * the same scenarios keep their forces, timing and assertions and only swap
 * this driver for the real engine.</p>
 */
public class ZombieAttacksNearestUnit {

    private final FakeUnit[] zombies;

    public ZombieAttacksNearestUnit(FakeUnit... zombies) {
        this.zombies = zombies;
    }

    /**
     * One frame of the horde: every living zombie steps toward its nearest
     * living victim, attacking when in reach.
     */
    public void onFrame(List<FakeUnit> victims) {
        for (FakeUnit zombie : zombies) {
            if (!zombie.isAlive()) continue;

            FakeUnit victim = nearestAliveVictim(zombie, victims);
            if (victim == null) return;

            if (inReach(zombie, victim)) {
                zombie.attackUnit(victim);
            }
            else {
                stepTowards(zombie, victim);
            }
        }
    }

    private FakeUnit nearestAliveVictim(FakeUnit zombie, List<FakeUnit> victims) {
        FakeUnit nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (FakeUnit victim : victims) {
            if (!victim.isAlive()) continue;

            double dist = zombie.distTo(victim);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = victim;
            }
        }

        return nearest;
    }

    private boolean inReach(FakeUnit zombie, FakeUnit victim) {
        return zombie.distTo(victim) <= zombie.groundWeaponRange() + 0.5;
    }

    /**
     * Same stepping math as {@code FakeOnFrameEnd} (which only moves our
     * units): top speed in pixels per frame toward the victim.
     */
    private void stepTowards(FakeUnit zombie, FakeUnit victim) {
        int speedInPixels = (int) (zombie.maxSpeed() * FakeOnFrameEnd.UNIT_SPEED_MODIFIER_PER_FRAME);
        if (speedInPixels <= 0) return;

        zombie.position = zombie.position.translateByPixels(
            AMath.inRange(-speedInPixels, victim.position().x - zombie.position.x, speedInPixels),
            AMath.inRange(-speedInPixels, victim.position().y - zombie.position.y, speedInPixels)
        );
    }
}
