package atlantis.combat;
import atlantis.battle.DuelMemory;

import atlantis.combat.cluster.ClusterBreach;
import atlantis.combat.cluster.ClusterRally;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.battle.kite.CombatTuning;
import atlantis.combat.kite.Kite;
import atlantis.combat.targeting.ASquadTargeting;
import atlantis.combat.twins.HoldTwin;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

/**
 * Formation hold (owner 2026-10-26, Stardust doctrine): engaging must not
 * dissolve the line. Stardust opens even its attack in an arc and breaks
 * it only for danger or weapon range ({@code attack()} forms the arc
 * while nobody is threatened or in range; {@code shouldStandGround}
 * holds while nobody has a target and nobody is threatened).
 *
 * <p>Our version, without a combat sim: while the enemy fields no ranged
 * units, the formation holds until contact - any our unit within
 * {@link #CONTACT_TILES} of an enemy, any our unit taking damage, or any
 * our ranged unit able to fire. Then the fight owns the unit as before.
 * With a ranged enemy the arc would march into fire, so the hold never
 * engages and micro runs as today. FLEE never holds: retreats own the
 * unit above this.</p>
 *
 * <p>Small groups are unaffected by construction: the hold marches at a
 * formation slot, and slots need three units - duels fall through to
 * the fight exactly as before.</p>
 */
public final class FormationHold {

    /** Contact breaks the hold: this close the phalanx becomes the fight. */
    public static final double CONTACT_TILES = 2.5;

    /**
     * A holder that marched within this distance of its slot counts as
     * arrived: standing your ground on the slot is the job, not a freeze.
     * Only a holder stuck FAR from its slot while not displacing trips
     * the unstick below.
     */
    public static final double WEDGE_ARRIVED_TILES = 1.0;

    /** Frames without displacement before a far-from-slot holder is declared wedged. */
    public static final int WEDGE_STILL_FRAMES = 48;

    /**
     * Hard veto (owner 2026-10-26): melee on top of the unit, no
     * discussion. Literally never hold formation with a melee enemy
     * within 3 tiles - no slot is worth standing into chews. Raw
     * distance on listed melee enemies only: no weapon, range,
     * visibility or cooldown plumbing that can fail open.
     */
    public static final double CLOSE_MELEE_TILES = 3.0;

    /**
     * Threat radius for a ghost contact (owner rule): anything that can
     * hit this unit inside these tiles counts as threatened, visible or
     * not - raw distance, same no-plumbing philosophy as the veto. A
     * threatened unit may fall back to the cluster; an unthreatened one
     * with only a ghost contact keeps hunting forward instead of
     * recalling to a slot.
     */
    public static final double SCOUT_THREAT_TILES = 3.2;

    /**
     * True when a listed melee enemy stands within {@link #CLOSE_MELEE_TILES}
     * of the unit. The only formation question that matters up close.
     */
    public static boolean hasCloseMeleeEnemy(AUnit unit) {
        try {
            if (unit == null || !unit.isAlive()) return false;

            for (AUnit foe : unit.enemiesNear().melee().list()) {
                if (foe == null || !foe.isAlive()) continue;
                if (unit.distTo(foe) <= CLOSE_MELEE_TILES) return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private FormationHold() {
    }

    /**
     * Why the hold was kept or broken - one value per decision, logged
     * by the HOLD twin instead of guessed from logs after the fact.
     */
    public enum HoldVerdict {
        /** March at the formation slot. */
        HOLD,
        /** Melee enemy within {@link #CLOSE_MELEE_TILES} - never stand into chews. */
        VETO,
        /** Any enemy within {@link #CONTACT_TILES} - the phalanx becomes the fight. */
        CONTACT,
        /** Hp dropped since the last battle frame (battle-hook latch, NEXT #183). */
        DAMAGE,
        /** A close enemy can hit this unit - kite outranks holding, unconditionally. */
        KITE,
        /**
         * Ghost contact and nothing can hit this unit: formation holds
         * nothing, the hunt owns it. Recalling to a slot on a fogged
         * memory parks the army while the enemy hides (live: a dragoon
         * marched at slots for 500f on a ghost 8 tiles out, UNSTICK x5).
         * Threatened units fall through to the normal gates below -
         * falling back to the cluster under pressure stays allowed.
         */
        GHOST,
        /** Anything else that declines the hold (tactic, ranged enemy, ...). */
        OTHER,
        /** Raiders in our base: no forming up, everybody attacks (owner doctrine). */
        BREACH,
        /** Hold decided, but no formation slot exists (duels fall through). */
        NO_SLOT,
        /** Hold decided, but the move order was refused. */
        MOVE_FAILED,
        /** Hold decided, but the unit never displaces: STOP once, fresh queue next frame. */
        UNSTICK
    }

    /**
     * Hold the line instead of engaging this target: true while the
     * enemy is melee-only, the tactic is FIGHT, and none of the contact
     * breaks fired (2.5-tile proximity, damage taken, own ranged able
     * to fire). The caller marches at the formation slot; a missing
     * slot falls through to the fight. Kiting outranks holding (owner
     * 2026-10-26): a chewed unit breaks to the fight instead of
     * holding its slot - safety of the single unit first.
     */
    public static boolean shouldHold(AUnit unit, AUnit target, FightOrFlight.Tactic tactic) {
        return decide(unit, target, tactic) == HoldVerdict.HOLD;
    }

    /** The ordered gate chain behind {@link #shouldHold}, naming the deciding gate. */
    static HoldVerdict decide(AUnit unit, AUnit target, FightOrFlight.Tactic tactic) {
        if (unit == null || target == null || !unit.isAlive()) return HoldVerdict.OTHER;
        if (hasCloseMeleeEnemy(unit)) return HoldVerdict.VETO;
        // Raiders in the base mean everybody attacks, no forming up
        // (owner: workers dying while the army holds its slots is the
        // breach itself being misread as a calm board). Reads after the
        // chew veto so an actually-chewed unit still names VETO first.
        if (ClusterBreach.breachForcesFight()) return HoldVerdict.BREACH;
        if (tactic != FightOrFlight.Tactic.FIGHT) return HoldVerdict.OTHER;
        if (!target.isVisibleUnitOnMap() && !isThreatened(unit)) return HoldVerdict.GHOST;

        try {
            if (!unit.isCombatUnit()) return HoldVerdict.OTHER;
            if (atlantis.units.select.Select.enemy().ranged().notEmpty()) return HoldVerdict.OTHER;
            if (DuelMemory.tookDamageSinceLastBattleFrame(unit)) return HoldVerdict.DAMAGE;
            if (unit.hasWeaponRangeToAttack(target, 0)) return HoldVerdict.OTHER;

            AUnit nearest = nearestEnemyOf(unit);
            if (nearest != null && unit.distTo(nearest) <= CONTACT_TILES) return HoldVerdict.CONTACT;
            if (fightWouldKite(unit)) return HoldVerdict.KITE;
            if (needsKite(unit)) return HoldVerdict.KITE;
        } catch (Exception ignored) {
            return HoldVerdict.OTHER;
        }
        return HoldVerdict.HOLD;
    }

    /**
     * Kite preemption (owner 2026-10-26): kiting outranks holding,
     * unconditionally. A close enemy that can hit this unit breaks
     * the hold. Damage itself is caught by the battle-hook latch
     * (DuelMemory.tookDamageSinceLastBattleFrame, NEXT #183), not by
     * the wound history read here: the margin below only decides
     * standoff shape. Safety of the single unit first; the
     * wound/cooldown nuances live inside the fight (margin scaling,
     * trade discipline), not in the hold gate.
     */
    public static boolean needsKite(AUnit unit) {
        try {
            return unit.enemiesThatCanAttackMe(3).count() >= 1;
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * The fight's own kite verdict, asked before the hold marches (owner:
     * FormHold never outranks kiting). Same threat the fight loop kites
     * from ({@code nearestEngageableEnemy} inside the kite envelope) and
     * the same predicate ({@code Kite.shouldKite}) - one membership source,
     * one decision, no second opinion that can fail open. The raw
     * {@link #needsKite} below stays as the backstop (it has no doctrine
     * exceptions, so the surrounded stand still reaches the fight instead
     * of marching to a slot). No behavior change wherever both agree;
     * the hold only declines in the margin band the raw 3-tile check
     * cannot see (wounded or hunted units the fight would already kite).
     */
    static boolean fightWouldKite(AUnit unit) {
        try {
            if (unit == null || !unit.isAlive()) return false;

            AUnit threat = ASquadTargeting.nearestEngageableEnemy(
                unit, CombatTuning.current().getKiteEnemyTiles());

            return threat != null && Kite.shouldKite(unit, threat);
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Real attackers close by (owner rule): anything that can hit this
     * unit inside {@link #SCOUT_THREAT_TILES} counts as threatened. Fail
     * closed toward the old behavior - on any doubt (or any exception)
     * the unit counts as threatened and the normal gates decide.
     */
    static boolean isThreatened(AUnit unit) {
        try {
            if (unit == null || !unit.isAlive()) return true;

            return unit.enemiesThatCanAttackMe(SCOUT_THREAT_TILES).count() > 0;
        } catch (Exception ignored) {
            return true;
        }
    }

    private static AUnit nearestEnemyOf(AUnit unit) {
        try {
            atlantis.units.select.Selection enemies = unit.enemiesNear();
            if (enemies == null || enemies.isEmpty()) return null;

            return enemies.nearestTo(unit);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Hold the line while calm (owner 2026-10-26, Stardust doctrine):
     * while the enemy is melee-only and nobody is in contact, the line
     * marches in formation instead of dissolving into a charge.
     * No slot means no formation (duels fall through untouched).
     */
    public static boolean holdIfCalm(
        AUnit unit, AUnit target, FightOrFlight.Tactic tactic, AUnit leader, HasPosition anchor
    ) {
        HoldVerdict verdict = decide(unit, target, tactic);
        if (verdict != HoldVerdict.HOLD) {
            HoldTwin.note(unit, target, tactic, verdict);

            return false;
        }

        HasPosition slot = ClusterRally.formationSlotFor(unit, leader, anchor, tactic);
        if (slot == null) {
            HoldTwin.note(unit, target, tactic, HoldVerdict.NO_SLOT);

            return false;
        }
        if (unstickHolderIfWedged(unit, slot, target, tactic)) {
            HoldTwin.note(unit, target, tactic, HoldVerdict.UNSTICK);

            return true;
        }
        if (!unit.move(slot, Actions.MOVE_ENGAGE, Behavior.HOLD_FORMATION.tag())) {
            HoldTwin.note(unit, target, tactic, HoldVerdict.MOVE_FAILED);

            return false;
        }
        FightTarget.markRepositioned(unit);
        HoldTwin.note(unit, target, tactic, HoldVerdict.HOLD);

        return true;
    }

    /**
     * A holder that marches at a slot it never reaches is not holding -
     * it is wedged, same as the kiting shape in FightTarget except the
     * kite loop never runs so its unstick can never trip. Same cadence
     * discipline: STOP once, fresh queue next frame; forget motion so the
     * watch restarts instead of firing every frame. Arrived holders
     * (within a tile of the slot) are left alone: standing your ground
     * on the slot is the job, not a freeze. Returns true when it acted.
     */
    private static boolean unstickHolderIfWedged(
        AUnit unit, HasPosition slot, AUnit target, FightOrFlight.Tactic tactic
    ) {
        try {
            if (unit == null || unit.position() == null || slot == null) return false;

            int now = A.now();
            Integer lastMoved = DuelMemory.lastMovedFrame(unit);
            APosition lastPos = DuelMemory.lastMovedPos(unit);
            if (lastMoved == null || lastPos == null || unit.distTo(lastPos) >= 0.5) {
                DuelMemory.noteMoved(unit, now, unit.position());

                return false;
            }
            if (now - lastMoved < WEDGE_STILL_FRAMES) return false;

            if (unit.distTo(slot) <= WEDGE_ARRIVED_TILES) {
                DuelMemory.noteMoved(unit, now, unit.position());

                return false;
            }
            unit.stop(Behavior.UNSTICK.tag());
            DuelMemory.forgetMotion(unit);

            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}
