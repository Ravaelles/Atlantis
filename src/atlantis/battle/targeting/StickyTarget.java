package atlantis.battle.targeting;

import atlantis.battle.kite.CombatTuning;
import atlantis.units.AUnit;

/**
 * Committed-target stickiness (extracted from BattleCommander): keep
 * shooting the committed target while it is alive, hittable and inside
 * weapon range plus a short chase allowance - never re-pick every frame
 * just because another enemy stepped marginally nearer. Per-frame
 * re-picking restarts the attack animation faster than it completes
 * (Wine ZealDrag: 519 acquisitions for 19 kills), the same churn both
 * reference bots refuse: Stardust only allows the switch when the old
 * target is gone/invalid/too far at the predicted frame
 * (MyDragoon::isReady), PurpleWave issues only on a new or overdue
 * target (Commander.attack). Sits between the squad pick (shared
 * focus in small fights stays untouched) and the nearest re-pick
 * (still the answer when commitment ends). Public as a seam.
 */
public final class StickyTarget {

    private StickyTarget() {
    }

    public static AUnit currentTarget(AUnit unit) {
        if (unit == null) return null;
        AUnit current = unit.target();
        if (current == null || !current.isAlive()) return null;
        if (!unit.hasWeaponToAttackThisUnit(current)) return null;
        if (unit.distTo(current) > unit.groundWeaponRange() + CombatTuning.current().getKiteStickyChaseTiles()) return null;
        return current;
    }
}
