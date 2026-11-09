package atlantis.combat.targeting;

import atlantis.battle.cluster.Missions;
import atlantis.map.position.Proximity;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.workers.defence.fight.WorkerDefenceFightCombatUnits;

/**
 * The zealot chase veto (owner 2026-10-30, in words): a zealot holding
 * home does not march out after a far target. Chasing is allowed while
 * it stays home business - toe-to-toe contact, inside the base zone,
 * within close of our buildings, an already-started fight, falling back
 * onto an enemy that sits between the unit and the base. Everything
 * else is a donation march: the caller forgets the contact (so the
 * ghost creep cannot re-chase it) and falls through to regroup.
 *
 * <p>Thresholds read as words ({@link Proximity}, the shared leash),
 * never as literals - tuning a number never renames the rule. The
 * small-army generalization is open (this gate is zealot-only). No home
 * base, no veto: baseless duels chase freely.</p>
 */
public final class ChaseVeto {

    private ChaseVeto() {
    }

    public static boolean veto(AUnit unit, AUnit target) {
        try {
            if (unit == null || !unit.isZealot() || target == null) return false;

            // Sieges are never chases.
            if (target.isABuilding()) return false;

            // The attack marches; the veto guards home. Reads the
            // decided posture from the shared frame note, else the
            // mirror it has always read (untouched fallback).
            atlantis.combat.cluster.ClusterMission.Posture noted =
                atlantis.combat.cluster.DecidedPosture.currentOrNull();
            boolean isAttack = noted != null
                ? noted == atlantis.combat.cluster.ClusterMission.Posture.ATTACK
                : Missions.isGlobalMissionAttack();
            if (isAttack) return false;

            AUnit main = Select.main();
            if (main == null) return false;

            // An ongoing fight persists - the veto refuses new chases,
            // it never interrupts a brawl.
            if (target.equals(unit.target())) return false;

            // Toe-to-toe is fighting, wherever it stands.
            if (Proximity.CONTACT.covers(target, unit)) return false;

            // Inside the base zone threats are engaged, not vetted -
            // the breaching raider at the door is home defence.
            if (Proximity.BASE_ZONE.covers(target, main)) return false;

            // Base-ward of the unit: falling back onto it on the way
            // home, not marching out past the line (owner order).
            try {
                if (target.distToMain() < unit.distToMain()) return false;
            } catch (Exception ignored) {
            }

            // Home defence, here or at the natural: inside close of any
            // standing building the chase stays.
            for (AUnit base : Select.ourBuildings().list()) {
                if (Proximity.CLOSE_TO_BUILDING.covers(target, base)) return false;
            }

            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}
