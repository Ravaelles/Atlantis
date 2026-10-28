package atlantis.combat.focus;

/**
 * The attack decision as a pure function: snapshot in, target + reason
 * out. Verbatim the old {@code AttackFocusPoint} chain, only relocated -
 * known enemy base, else known enemy main, else the nearest unexplored
 * start, else our own main (which may itself be null on a baseless map).
 *
 * <p>No game reads here: no {@code Select}, no {@code EnemyUnits}, no
 * clock. The live seam ({@code AttackFocusPoint}) builds the snapshot
 * from game state; tests build it by hand. Either way the same branches
 * answer, pinned branch by branch.</p>
 */
public final class AttackDecider {

    private AttackDecider() {
    }

    public static AttackDecision decide(AttackSnapshot snapshot) {
        if (snapshot.enemyBase() != null) {
            return new AttackDecision(snapshot.enemyBase(), AttackReason.ENEMY_BASE_SEEN);
        }

        if (snapshot.enemyMain() != null) {
            return new AttackDecision(snapshot.enemyMain(), AttackReason.ENEMY_MAIN_KNOWN);
        }

        if (snapshot.unexploredStart() != null) {
            return new AttackDecision(snapshot.unexploredStart(), AttackReason.EXPLORE_UNEXPLORED_START);
        }

        return new AttackDecision(snapshot.ownMain(), AttackReason.FALLBACK_HOME);
    }
}
