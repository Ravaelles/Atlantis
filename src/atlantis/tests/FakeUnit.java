package atlantis.tests;

import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class FakeUnit extends AUnit {

    public static int firstFreeId = 1;
    public int id;
    public AUnitType type;
    public APosition position;
    public boolean enemy = false;
    public boolean neutral = false;

    // =========================================================

    public FakeUnit(AUnitType type, int tx, int ty) {
        super();
        this.id = firstFreeId++;
        this.type = type;
        this._lastType = type;
        this.position = APosition.create(tx, ty);
    }

    // =========================================================

    @Override
    public int id() {
        return id;
    }

    @Override
    public APosition position() {
        return position;
    }

    @Override
    public boolean isNeutral() {
        return neutral;
    }

    @Override
    public boolean isCompleted() {
        return true;
    }

    // =========================================================

    public FakeUnit setEnemy() {
        this.enemy = true;
        return this;
    }

    public FakeUnit setNeutral() {
        this.neutral = true;
        return this;
    }

    public FakeUnit setOur(boolean trueIfOurFalseIfEnemy) {
        if (!trueIfOurFalseIfEnemy) {
            this.enemy = true;
        }
        return this;
    }
}
