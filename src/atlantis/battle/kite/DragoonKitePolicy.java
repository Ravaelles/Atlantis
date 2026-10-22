package atlantis.battle.kite;

import atlantis.battle.escape.PinchRoom;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;

/**
 * When a Dragoon steps back and when it holds, in decision order.
 *
 * <p>Two reasons, asked in this order - the split the ladder is built
 * on, so a future gun (Terran included) reuses the shape instead of
 * growing another if-chain:
 * <ol>
 * <li>cooldown reason: the weapon is spent, so stepping back costs no
 * DPS - decided first, whatever the health;</li>
 * <li>health reason: hurt guns yield pockets and dying guns trade.</li>
 * </ol>
 *
 * <p>Zero behavior lives here that {@link Kite#shouldKite} did not
 * already have: this is the same four rules in the same order, only
 * named. A rule that needs a comment explaining <i>what</i> it is gets
 * its own method instead.
 */
public final class DragoonKitePolicy {

    private DragoonKitePolicy() {
    }

    public static KiteVerdict decide(AUnit unit, AUnit threat) {
        KiteVerdict cooling = coolingVerdict(unit);
        if (cooling != KiteVerdict.DEFER) return cooling;

        return healthVerdict(unit, threat);
    }

    // =========================================================
    // Cooldown reason: the weapon is spent.

    private static KiteVerdict coolingVerdict(AUnit unit) {
        // No pure cooldown rule yet - the projected-contact kite (kite
        // while cooling inside melee reach, health plays no role) lands
        // here, ahead of the health rules below.
        return KiteVerdict.DEFER;
    }

    // =========================================================
    // Health reason: hurt guns yield, dying guns trade.

    private static KiteVerdict healthVerdict(AUnit unit, AUnit threat) {
        boolean lowHp = unit.hp() + unit.shields() < CombatTuning.current().getDragoonLowHp();

        if (pocketClosing(unit)) return KiteVerdict.KITE;
        if (escapeImpossible(unit, threat)) return KiteVerdict.STAND;
        if (imminentShotWorthTaking(unit, lowHp)) return KiteVerdict.STAND;
        if (hurtAndCooling(unit, threat, lowHp)) return KiteVerdict.KITE;

        return KiteVerdict.DEFER;
    }

    // Pinch retreat (owner): two or more melee enemies inside 2
    // tiles on a hurt Dragoon (hp + shields < 80) means the pocket
    // is closing - yield ground instead of standing into it.
    // Overrides the surrounded-stand below: standing trades only
    // when escape already failed, never while steps still open.
    // A step that finds nothing still falls through to the
    // glue/corner trading below, so a fully pinched Dragoon
    // shoots instead of idling.
    private static boolean pocketClosing(AUnit unit) {
        return unit.hp() + unit.shields() < CombatTuning.current().getDragoonPinchHp()
            && unit.enemiesNear().melee().inRadius(CombatTuning.current().getDragoonPinchMeleeTiles(), unit).size()
                >= CombatTuning.current().getDragoonPinchMeleeCount()
            && PinchRoom.hasRoom(unit.position(), (x, y) -> {
                try {
                    return APosition.create(x, y).isWalkable();
                } catch (Exception | Error ignored) {
                    return true;
                }
            });
    }

    // Surrounded: three or more melee enemies on top of the Dragoon
    // means escape is impossible - stand and shoot instead of
    // donating the retreat (owner). Either the shot is almost
    // ready or the nearest enemy is far enough to trade with.
    // A chewer already on the guns voids even this: standing
    // through contact is never trading (owner order: kiting has
    // absolute precedence, measured 348 chewed sim frames).
    private static boolean escapeImpossible(AUnit unit, AUnit threat) {
        return !Kite.isBeingChewed(unit)
            && unit.enemiesNear().melee().inRadius(3, unit).size() >= CombatTuning.current().getDragoonSurroundedMeleeCount()
            && (unit.cooldown() <= CombatTuning.current().getDragoonSurroundedStandCooldown()
                || unit.distTo(threat) >= CombatTuning.current().getDragoonSurroundedStandDistTiles());
    }

    // Low-HP Dragoon holds fire unless the shot is safe: at least
    // 3.2 tiles of room or an imminent weapon (owner, converging
    // with Stardust: cooldown <= latency + 2 attacks instead of
    // stepping). Below half health the threatened step would only
    // delay the one shot that still matters. Chewed low-hp guns
    // kite anyway: the imminent shot is not worth contact.
    private static boolean imminentShotWorthTaking(AUnit unit, boolean lowHp) {
        return lowHp && !Kite.isBeingChewed(unit)
            && unit.cooldown() <= CombatTuning.current().getDragoonLowHpFireCooldown();
    }

    // ...otherwise it yields ground early: holding fire means
    // stepping, never standing through the cooldown. Robust to
    // margin tuning - the margin gate alone already kites here at
    // default constants, this keeps it true whatever they become.
    // (Owner order: the former meat-screen stand above this line
    // was withdrawn - screened dragoons kite by the same margin,
    // health plays no role. Pinch and surrounded above still
    // yield, except chewed, which always kites: a closing pocket
    // is dying, not trading.)
    private static boolean hurtAndCooling(AUnit unit, AUnit threat, boolean lowHp) {
        return lowHp
            && unit.distTo(threat) < CombatTuning.current().getDragoonLowHpHoldTiles()
            && unit.cooldown() > CombatTuning.current().getDragoonLowHpFireCooldown();
    }
}
