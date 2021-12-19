package atlantis.tests.unit;

import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import bwapi.TechType;

public class FakeUnit extends AUnit {

    public static int firstFreeId = 1;
    public int id;
    public AUnitType type;
    public APosition position;
    public boolean enemy = false;
    public boolean neutral = false;
    public boolean detected = true;
    public boolean completed = true;
    public int energy = 0;
    public FakeUnit target = null;
    public TechType lastTechUsed = null;

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
    public String toString() {
        return "Fake " + super.toString();
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
    public int x() {
        return position.x;
    }

    @Override
    public int y() {
        return position.y;
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
        return id * 10;
    }

    @Override
    public int energy() {
        return energy;
    }

    public int maxHp() {
        return hp() + id * 10;
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public boolean isPowered() {
        return true;
    }

    @Override
    public boolean isMoving() {
        return false;
    }

    @Override
    public boolean isUnderDarkSwarm() {
        return false;
    }

    @Override
    public AUnit target() {
        return target;
    }

    // =========================================================

    @Override
    public double distTo(AUnit otherUnit) {
        int dx = otherUnit.x() - position.x();
        int dy = otherUnit.y() - position.y();
        return Math.sqrt(dx * dx + dy * dy) / 32.0;
    }

    // =========================================================
    // Orders

    @Override
    public boolean useTech(TechType tech, AUnit target) {
        this.lastTechUsed = tech;
        this.target = (FakeUnit) target;
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

    public FakeUnit setDetected(boolean detected) {
        this.detected = detected;
        return this;
    }

    public FakeUnit setCompleted(boolean completed) {
        this.completed = completed;
        return this;
    }

    public FakeUnit setEnergy(int energy) {
        this.energy = energy;
        return this;
    }

    public FakeUnit setOur(boolean trueIfOurFalseIfEnemy) {
        if (!trueIfOurFalseIfEnemy) {
            this.enemy = true;
        }
        return this;
    }
}
