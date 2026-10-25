package atlantis.combat.strategy;

import atlantis.map.choke.AChoke;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

/**
 * SquadScout bait (NEXT #170): when the line holds a choke with no
 * contact, detach one unit as bait - outside the formation - to pull the
 * enemy into the funnel while the line stands formed. Works on open
 * ground too (a formed line shoots first) and gives observable range
 * without committing the army. The bait is deterministic (lowest id), so
 * every holder picks the same unit and exactly one detaches.
 */
public final class SquadScout {

    private SquadScout() {
    }

    public static boolean applies(AUnit unit, HasPosition anchor) {
        if (unit == null) return false;
        if (Count.bases() == 0) return false;
        if (!ChokeStandoff.applies(unit, anchor)) return false;
        if (BattleSetup.combatants() < 3) return false;
        // No contact: baiting mid-fight splits the line that is already
        // trading. The bait goes out only while nobody shoots back.
        if (!unit.enemiesNear().isEmpty()) return false;

        AChoke choke = BattleSetup.chokeNear(unit);
        if (choke == null || choke.center() == null) return false;
        if (BattleSetup.enemiesNear(choke.center(), ChokeStandoff.CONTEST_TILES) == 0) return false;

        Selection ours = Select.ourCombatUnits();
        if (ours == null || ours.count() < 3) return false;

        AUnit bait = pickBait(ours);

        return bait != null && bait.id() == unit.id();
    }

    /** Deterministic pick: the lowest-id combat unit, so the whole line agrees. Pure. */
    public static AUnit pickBait(Selection ours) {
        if (ours == null) return null;

        AUnit bait = null;
        for (AUnit candidate : ours.list()) {
            if (candidate == null || !candidate.isAlive()) continue;
            if (bait == null || candidate.id() < bait.id()) bait = candidate;
        }
        return bait;
    }

    /** Pure geometry: the bait stands in the funnel mouth, toward the enemy. */
    public static APosition baitPoint(APosition chokeCenter, HasPosition enemySide) {
        if (chokeCenter == null || enemySide == null) return null;

        return chokeCenter.translateTilesTowards(enemySide, 2);
    }

    /** Guarded detection: the bait point for this unit, or null when there is no held choke. */
    public static APosition baitPointFor(AUnit unit) {
        try {
            atlantis.map.choke.AChoke choke = BattleSetup.chokeNear(unit);
            if (choke == null || choke.center() == null) return null;

            AUnit foe = Select.enemy().combatUnits().nearestTo(choke.center());
            if (foe == null || foe.position() == null) return null;

            return baitPoint(choke.center(), foe.position());
        } catch (Exception ignored) {
            return null;
        }
    }
}
