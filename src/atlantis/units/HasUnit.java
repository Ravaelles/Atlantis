package atlantis.units;

import atlantis.combat.squad.Squad;

public abstract class HasUnit {
    protected AUnit unit;

    public HasUnit(AUnit unit) {
        this.unit = unit;
    }
}
