package atlantis.tests.unit;

import atlantis.information.AFoggedUnit;
import atlantis.information.FoggedUnit;
import atlantis.position.APosition;

public class FakeFoggedUnit extends AFoggedUnit {

//    public int _id;
//    public AUnitType type;
//    public APosition position;
    public boolean enemy = false;
    public boolean neutral = false;
    public boolean detected = true;
    public boolean completed = true;

    // =========================================================

    public static FakeFoggedUnit fromFake(FakeUnit unit) {
        _lastAUnit = unit;

        FakeFoggedUnit foggedUnit = (FakeFoggedUnit) all.get(unit.id());
        if (foggedUnit != null) {
            return foggedUnit;
        }

        return new FakeFoggedUnit(unit);
    }

    private FakeFoggedUnit(FakeUnit unit) {
        this._id = unit.id();
        this.aUnit = unit;
        this.update(unit);

        all.put(unit.id(), this);
    }

    // =========================================================

    @Override
    public String toString() {
        return "FakeFoggedUnit{#" + _id + " " + _lastType + " at " + _position + "}";
    }

    // =========================================================

    @Override
    public int id() {
        return _id;
    }

    @Override
    public APosition position() {
        return _position;
    }

    @Override
    public int x() {
        return _position.x;
    }

    @Override
    public int y() {
        return _position.y;
    }

    @Override
    public boolean isNeutral() {
        return neutral;
    }

    @Override
    public boolean isOur() {
        return !enemy;
    }

    @Override
    public boolean isEnemy() {
        return enemy;
    }

    @Override
    public boolean isDetected() {
        return detected;
    }

    @Override
    public int hp() {
        return _id * 10;
    }

    public int maxHp() {
        return hp() + _id * 10;
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

}
