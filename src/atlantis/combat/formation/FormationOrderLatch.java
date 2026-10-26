package atlantis.combat.formation;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.cluster.ClusterMission;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnitBreachedBase;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;

/**
 * One shared latch for formation order issuance (NEXT #210, owner order):
 * while the posture is DEFEND, the same formation order is not re-issued
 * within 10 frames. Re-issuing every frame churns the line - the unit
 * never settles into the move - while identical orders carry no new
 * information.
 *
 * <p>Not a per-unit cooldown on purpose: the latch keys on the order
 * itself (unit + destination tile), so a fresh deployment or a changed
 * destination always flows immediately and only exact repeats wait.
 * A posture other than DEFEND forgets the latch (a new defense starts
 * undeployed) and never blocks. Denied callers hold position instead
 * of falling through to creep/gather - falling through would scatter
 * the line the latch is calming.</p>
 */
public final class FormationOrderLatch {

    /** Identical formation orders re-issue at most this often while defending. */
    public static final int ISSUE_EVERY_FRAMES = 10;

    private static int lastUnit = -1;
    private static int lastTx = Integer.MIN_VALUE;
    private static int lastTy = Integer.MIN_VALUE;
    private static int lastFrame = -1000000;

    private FormationOrderLatch() {
    }

    /** Test seam: the latch is per-game state, never cross-test. */
    public static void clearForTests() {
        lastUnit = -1;
        lastTx = Integer.MIN_VALUE;
        lastTy = Integer.MIN_VALUE;
        lastFrame = -1000000;
    }

    /**
     * True when this formation order may be issued now. ATTACK (or any
     * non-DEFEND posture) never blocks and forgets the latch; on DEFEND
     * an order identical to the last issued one waits out the window,
     * anything else flows.
     */
    public static boolean shouldIssue(AUnit unit, HasPosition dest) {
        if (currentPosture() != ClusterMission.Posture.DEFEND) {
            forget();
            return true;
        }
        if (unit == null || dest == null || dest.position() == null) return true;

        int now = A.now();
        if (unit.id() == lastUnit
            && dest.tx() == lastTx && dest.ty() == lastTy
            && now - lastFrame < ISSUE_EVERY_FRAMES) {
            return false;
        }

        lastUnit = unit.id();
        lastTx = dest.tx();
        lastTy = dest.ty();
        lastFrame = now;

        return true;
    }

    private static void forget() {
        lastUnit = -1;
        lastTx = Integer.MIN_VALUE;
        lastTy = Integer.MIN_VALUE;
        lastFrame = -1000000;
    }

    private static ClusterMission.Posture currentPosture() {
        try {
            return ClusterMission.postureFor(
                Alpha.get().size(), EnemyUnitBreachedBase.get() != null);
        }
        catch (RuntimeException ignored) {
            return ClusterMission.Posture.ATTACK;
        }
    }
}
