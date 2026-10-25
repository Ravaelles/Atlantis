package atlantis.combat.behaviors;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

/**
 * Reavers brew scarabs instead of standing around without ammo (NEXT
 * #198, port of the old ReaverProduceScarab gate): scarabs at or below
 * three, minerals for one more, not training already, not mid-swing
 * (an order now would cancel the shot).
 */
public final class ProduceScarabs {

    private ProduceScarabs() {
    }

    /** Full magazines and non-Reavers are not this behavior's business. */
    public static boolean applies(AUnit unit) {
        if (unit == null || !unit.isAlive()) return false;
        if (!unit.isReaver()) return false;
        if (unit.isStartingAttack() || unit.isAttackFrame()) return false;

        return unit.scarabCount() <= 3 && A.hasMinerals(20) && !unit.isTrainingAnyUnit();
    }

    /**
     * Queue one scarab; true when the order went out. Training is not
     * a move and never preempts kiting - the caller decides placement.
     */
    public static boolean act(AUnit unit) {
        if (!applies(unit)) return false;

        return unit.orderSink().train(unit, AUnitType.Protoss_Scarab.ut());
    }
}
