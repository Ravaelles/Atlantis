package atlantis.units;

import atlantis.units.select.Select;

/**
 * Is the enemy attacking us at home, right now?
 *
 * <p>Placed in {@code atlantis.units} rather than next to the managers that ask:
 * the worker managers live under {@code atlantis.units.workers..}, which the core
 * rule treats as a consumer, so a shared decision class there would add six frozen
 * violations on every call site. This class answers a question about units, so it
 * belongs with them.</p>
 *
 * <p>The worker run/fight arbitration was field-shaped only: a packed melee
 * rush made every worker flee, and the 300-frame run lockout plus the
 * modulo skips then kept them out of the fight for good - in a base defence
 * that meant nobody ever supported the cannon while it died three tiles
 * away (_AI/BUGS.md B-19, measured on the 4pool and 9pool scenarios:
 * nexus down ~880/~631, probes never engaged).</p>
 *
 * <p>Fleeing is right in the field and fatal at home: once the army is dead
 * there is nothing left between the attackers and the base. This class is
 * the one place that decides "home under attack", so {@code WorkerDefenceRun}
 * suppresses itself here, the run lockouts lift here, and the modulo skips
 * stop skipping here - and nowhere else.</p>
 */
public class BaseUnderAttack {

    /**
     * Matches the distance {@code WorkerDefenceFightCombatUnits} and
     * {@code WorkerHelpCombatUnitsFight} already use: a worker within this
     * distance of its base is defending it, not visiting it.
     */
    private static final double WORKER_NEAR_BASE = 8;

    /** Attackers this close to a base count as an attack on the base. */
    private static final double ATTACK_RADIUS = 8;

    /**
     * Enemy combat units are attacking right next to one of our bases.
     */
    public static boolean check() {
        AUnit base = Select.mainOrAnyBuilding();
        return base != null
            && base.enemiesNear().combatUnits().inRadius(ATTACK_RADIUS, base).notEmpty();
    }

    /**
     * This worker should hold ground instead of fleeing: the attack is at
     * home, the worker is close enough to help, and the defence still has
     * teeth - something friendly is fighting (or shooting) near the base for
     * the worker to support. Once that is gone the fight is lost and saving
     * workers is the right answer, so running is no longer suppressed.
     * Attackers that kill from beyond worker range (Reaver/Tank/Lurker)
     * always outrule holding ground: standing under artillery is not
     * defence.
     */
    public static boolean workerShouldHoldGround(AUnit worker) {
        if (worker.distToBase() > WORKER_NEAR_BASE) return false;
        if (!check()) return false;
        if (outrangesWorkers(worker)) return false;

        AUnit base = Select.mainOrAnyBuilding();
        return base != null
            && base.friendsNear().combatUnits().inRadius(ATTACK_RADIUS, base).notEmpty();
    }

    /**
     * Attackers a worker cannot trade with even at home: they strike from
     * beyond worker range (or in one splash), so flee instead of hold.
     */
    private static boolean outrangesWorkers(AUnit worker) {
        return worker.enemiesNear().ofType(
            AUnitType.Protoss_Reaver,
            AUnitType.Terran_Siege_Tank_Tank_Mode,
            AUnitType.Terran_Siege_Tank_Siege_Mode,
            AUnitType.Zerg_Lurker
        ).inRadius(12, worker).notEmpty();
    }
}
