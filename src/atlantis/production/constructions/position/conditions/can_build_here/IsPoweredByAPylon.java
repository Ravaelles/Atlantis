package atlantis.production.constructions.position.conditions.can_build_here;

import atlantis.map.position.APosition;
import atlantis.map.position.PsiField;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

/**
 * Is this tile inside a finished Pylon's power field?
 *
 * <p>
 * <b>The answer is derived from our own Pylons, never asked of the engine.</b>
 * {@code Game.hasPowerPrecise} is not usable: it returned false for tiles a
 * finished Pylon clearly covered, so Forge and Cybernetics Core were placed in
 * unpowered spots while the Gateways beside them were fine (measured 2026-10-08,
 * see {@code _AI/POSITION-FINDER.md} point 4). That is why
 * {@code EnginePowerSource} derives it too, and why this class must agree with it
 * rather than keep its own idea of range.
 * </p>
 *
 * <p>
 * <b>This used to answer with a 3.2-tile radius measured from the position's
 * centre, which was wrong in two ways at once</b> - the engine's Pylon radius is
 * <b>6</b> tiles, measured from the Pylon's <b>footprint</b> (a 2x2 Pylon powers
 * from any of its tiles, so a tile diagonally off a corner is 6+1 away, not 6). So
 * the old check refused tiles that were genuinely powered, which is how a building
 * could look "unpowered" while sitting next to a Pylon.
 * </p>
 *
 * <p>
 * The radius and the footprint arithmetic are kept here in step with
 * {@code atlantis.placement.engine.EnginePowerSource}; if the two ever disagree
 * again, this comment is the place that says which one is authoritative (the
 * engine) and why.
 * </p>
 */
public class IsPoweredByAPylon {

    /**
     * Is this tile inside a finished Pylon's power field, by the engine's own
     * rule ({@link PsiField})? Completed Pylons only - a warping Pylon powers
     * nothing, and counting it would pass sites the engine refuses.
     */
    public static boolean check(APosition position) {
        if (position == null) return false;

        for (AUnit pylon : Select.ourOfType(AUnitType.Protoss_Pylon).list()) {
            if (PsiField.covers(pylon.tx(), pylon.ty(), position.tx(), position.ty())) return true;
        }

        return false;
    }
}
