package atlantis.combat;

import atlantis.AGame;
import atlantis.units.AUnit;

/**
 * Replaces combat attributes appended in JNIBWAPI.Unit in original Atlantis
 *
 * @author Anderson
 *
 */
public class ACombatInformation {

    private AUnit unit;
    private int _lastTimeCombatEval;
    private double _lastCombatEval;

    public ACombatInformation(AUnit u) {
        unit = u;
    }

    /**
     * Caches combat eval of this unit for the time of one frame.
     */
    public void updateCombatEval(double eval) {
        _lastTimeCombatEval = AGame.getTimeFrames();
        _lastCombatEval = eval;
    }

    public double getCombatEvalCachedValueIfNotExpired() {
        if (AGame.getTimeFrames() <= _lastTimeCombatEval) {
            return _lastCombatEval;
        } else {
            return (int) -123456;
        }
    }
}
