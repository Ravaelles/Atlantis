package atlantis.battle.cluster;

import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.select.Count;

/**
 * One place deciding which ground the army wants, and why (NEXT #197).
 * The old mission changer answered the same question with thirty
 * interleaved conditions and no owner; each reason below owns its
 * shape, its numbers carry their provenance, and the order is explicit:
 * the standing headcount first, the early-game flips next, hold last.
 *
 * <p>Thresholds: the trapped line is sim-measured
 * ({@code TrappedDragoonsAttackOutTest}: hold loses dragoons at 6 and 8
 * zealots, wins at 4; dragoon floor and the 650 s window ride the
 * live-proven old class, GoWithGoonz shape). The banked line rides the
 * old StrongAndBig/beBrave shape (four-digit bank behind a real army);
 * a below-headcount banked flip has no provenance and is deliberately
 * not implemented.</p>
 */
public final class PostureReasons {

    private PostureReasons() {
    }

    /** A line that can trade on open ground instead of donating boxed in. */
    public static final int TRAPPED_MIN_COMBAT_UNITS = 4;

    /** Enough dragoons to hold a kite line once out. */
    public static final int TRAPPED_MIN_DRAGOONS = 2;

    /** A zealot mass the base cannot kite around (sim-bracketed: 4 no, 6 yes). */
    public static final int TRAPPED_MIN_ENEMY_ZEALOTS = 6;

    /** Early game, single-base economies with no room to kite at home. */
    public static final int TRAPPED_MAX_GAME_SECONDS = 650;

    /** A bank that buys its own army while the attack runs. */
    public static final int BANKED_MINERALS = 1000;

    /** One decision with its reason. Posture is a direction, never a tile. */
    public static final class Verdict {
        public final ClusterMission.Posture posture;
        public final PostureReason reason;

        Verdict(PostureReason reason, ClusterMission.Posture posture) {
            this.reason = reason;
            this.posture = posture;
        }
    }

    /**
     * Pure decision: posture plus the reason, from explicit inputs so it
     * pins without a game. The live inputs ride {@link #decideLive()}.
     */
    public static Verdict decide(
        int ours, int ourDragoons, int enemyZealots, int minerals, int gameSeconds
    ) {
        if (ours >= ClusterMission.ATTACK_ARMY_SIZE) {
            if (minerals >= BANKED_MINERALS) {
                return new Verdict(PostureReason.BANKED_MINERALS, ClusterMission.Posture.ATTACK);
            }

            return new Verdict(PostureReason.HEADCOUNT, ClusterMission.Posture.ATTACK);
        }

        if (ours >= TRAPPED_MIN_COMBAT_UNITS
            && ourDragoons >= TRAPPED_MIN_DRAGOONS
            && enemyZealots >= TRAPPED_MIN_ENEMY_ZEALOTS
            && gameSeconds <= TRAPPED_MAX_GAME_SECONDS) {
            return new Verdict(PostureReason.TRAPPED_BY_MASS_ZEALOTS, ClusterMission.Posture.ATTACK);
        }

        return new Verdict(PostureReason.HOLD_GROUND, ClusterMission.Posture.DEFEND);
    }

    /** The live reading behind {@link #decide}: counts, bank and clock. */
    public static Verdict decideLive() {
        return decide(
            Count.ourCombatUnits(),
            Count.dragoons(),
            EnemyUnits.zealots(),
            A.minerals(),
            A.s);
    }
}
