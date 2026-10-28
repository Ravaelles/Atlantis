package atlantis.combat.focus;

import atlantis.map.position.HasPosition;

/**
 * Everything the attack decision reads, nothing it does. Deliberately
 * engine-free: a snapshot is built from live game state at the seam
 * ({@code AttackFocusPoint}) and by hand in tests - the decider never
 * touches {@code Select}, {@code EnemyUnits} or the map, so StarEngine
 * pins the decision without issuing a single order (owner: full
 * separation of decisions from execution).
 *
 * <p>All four corners nullable: an unknown enemy and a baseless map are
 * ordinary inputs, not exceptions - the decider names the fallback
 * instead.</p>
 */
public final class AttackSnapshot {

    private final HasPosition enemyBase;
    private final HasPosition enemyMain;
    private final HasPosition unexploredStart;
    private final HasPosition ownMain;

    public AttackSnapshot(
        HasPosition enemyBase,
        HasPosition enemyMain,
        HasPosition unexploredStart,
        HasPosition ownMain
    ) {
        this.enemyBase = enemyBase;
        this.enemyMain = enemyMain;
        this.unexploredStart = unexploredStart;
        this.ownMain = ownMain;
    }

    public HasPosition enemyBase() {
        return enemyBase;
    }

    public HasPosition enemyMain() {
        return enemyMain;
    }

    public HasPosition unexploredStart() {
        return unexploredStart;
    }

    public HasPosition ownMain() {
        return ownMain;
    }
}
