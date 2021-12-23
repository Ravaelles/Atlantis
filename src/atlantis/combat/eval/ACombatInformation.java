package atlantis.combat.eval;

import atlantis.AGame;
import atlantis.units.AUnit;

/**
 * Replaces combat attributes appended in JNIBWAPI.Unit in original Atlantis
 *
 * @author Anderson
 *
 */
public class ACombatInformation {

    private final AUnit unit;
    private int _lastTimeCombatEval;
    private double _lastCombatEval;

    public ACombatInformation(AUnit u) {
        unit = u;
    }

    /**
     * Caches combat eval of this unit for the time of one frame.
     */
    public void updateCombatEval(double eval) {
        _lastTimeCombatEval = AGame.now();
        _lastCombatEval = eval;
    }

    public double getCombatEvalCachedValueIfNotExpired() {
        if (AGame.now() <= _lastTimeCombatEval) {
            return _lastCombatEval;
        } else {
            return -123456;
        }
    }
}
