package atlantis.units.attacked_by;

import atlantis.game.A;
import atlantis.units.AUnit;

public class UnderAttack {
    private final AUnit unit;
    private AUnit lastBy = null;
    private int lastByFrame = -1;

    public UnderAttack(AUnit unit) {
        this.unit = unit;
    }

    public AUnit lastBy() {
        return lastBy;
    }

    public void setLastAttackedBy(AUnit lastBy) {
        this.lastBy = lastBy;
        lastByFrame = A.now();
    }

    public int lastAgo() {
        return lastByFrame > 0 ? A.ago(lastByFrame) : 99999;
    }
}
