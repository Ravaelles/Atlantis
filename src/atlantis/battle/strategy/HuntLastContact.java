package atlantis.battle.strategy;

import atlantis.battle.FightTarget;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;

/**
 * Lost contact on a baseless map: march the last engaged point, or false to hold.
 *
 * Extracted from BattleCommander: the 9-tile envelope, the baseless gate and
 * the contact-memory rules are one reason to change (re-acquisition policy),
 * separate from the per-unit priority chain that calls it.
 */
public final class HuntLastContact {

    private HuntLastContact() {
    }

    public static boolean hunt(AUnit unit) {
        // No enemy in reach: on baseless maps, hunt the last engaged
        // point instead of sitting (1Drag_v_1Zeal: brief contact, then
        // 9 min idle 60 tiles apart - neither side re-acquires). The point
        // is snapshotted while visible, so a vanished enemy still leaves
        // an address. No UMS check on purpose: the gate is contact memory
        // plus baselessness, so melee frame 0 and based maps can never
        // reach here. Gated beyond the 9-tile squad envelope: inside it
        // the fight logic above already decided (focus refusals included
        // - never overruled from here).
        if (Count.bases() == 0) {
            APosition last = FightTarget.lastContact(unit);
            if (last != null && unit.distTo(last) > 9) {
                if (unit.move(last, Actions.MOVE_ENGAGE, "ClusterHunt")) {
                    FightTarget.markRepositioned(unit);
                    return true;
                }
            }
        }
        return false;
    }
}
