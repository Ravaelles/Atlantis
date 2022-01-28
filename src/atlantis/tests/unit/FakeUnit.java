package atlantis.tests.unit;

import atlantis.position.APosition;
import atlantis.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.UnitAction;
import bwapi.TechType;
import bwapi.UnitType;

public class FakeUnit extends AUnit {

    public static int firstFreeId = 1;
    public int id;
    public AUnitType rawType;
    public APosition position;
    public boolean enemy = false;
    public boolean neutral = false;
    public boolean detected = true;
    public boolean completed = true;
    public boolean loaded = false;
    public boolean stimmed = false;
    public int energy = 0;
    public FakeUnit target = null;
    public APosition targetPosition = null;
    public TechType lastTechUsed = null;
    public String lastCommand = "None";

    // =========================================================

    public FakeUnit(AUnitType type, double tx, double ty) {
        super();
        this.id = firstFreeId++;
        this.rawType = type;
        this._lastType = type;
        this.position = APosition.createFromPixels((int) tx * 32, (int) ty * 32);
    }

    // =========================================================

    @Override
    public String toString() {
        return "Fake " + super.toString() + " " + position();
    }

    public String lastCommand() {
        if (lastCommand.equals("AttackUnit")) {
            return "Attack:" + target;
        }
        else if (lastCommand.equals("Move")) {
            return "Move:" + targetPosition.toString();
        }
        return lastCommand;
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
    public UnitType bwapiType() {
        return rawType.ut();
    }

    @Override
    public boolean hasPathTo(HasPosition point) {
        return true;
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
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public boolean isUnderStorm() {
        return false;
    }

    @Override
    public int hp() {
        return maxHp();
    }

    @Override
    public int maxHp() {
        return type().maxHp();
    }

    @Override
    public int energy() {
        return energy;
    }

    @Override
    public int groundWeaponCooldown() {
        return 0;
    }

    @Override
    public int airWeaponCooldown() {
        return 0;
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
    public boolean isAttackFrame() {
        return false;
    }

    @Override
    public boolean isStartingAttack() {
        return false;
    }

    @Override
    public boolean isStimmed() {
        return stimmed;
    }

    @Override
    public boolean isMoving() {
        return lastCommand.equals("Move");
    }

    public boolean isAttacking() {
        return lastCommand.equals("AttackUnit");
    }

    @Override
    public boolean isPatrolling() {
        return lastCommand.equals("Patrolling");
    }

    @Override
    public boolean isHoldingPosition() {
        return lastCommand.equals("Hold");
    }

    @Override
    public boolean isUnderDarkSwarm() {
        return false;
    }

    @Override
    public AUnit target() {
        return target;
    }

    @Override
    public APosition targetPosition() {
        return targetPosition;
    }

    // =========================================================

    @Override
    protected void cacheType() {
        _lastType = rawType;
    }

    @Override
    public double distTo(AUnit otherUnit) {
//        System.out.println("this = " + this.position + " // " + this);
//        System.out.println("otherUnit = " + ((FakeUnit) otherUnit).position);
        int dx = otherUnit.x() - position.x();
        int dy = otherUnit.y() - position.y();
//        System.out.println("X =  " + otherUnit.x() + " // " + position.x() + " // " + dx);
//        System.out.println("Y =  " + otherUnit.y() + " // " + position.y() + " // " + dy);
//        System.out.println("Length = " + Math.sqrt(dx * dx + dy * dy) / 32.0);
        return Math.sqrt(dx * dx + dy * dy) / 32.0;
    }

    @Override
    public double distTo(HasPosition otherPosition) {
        int dx = otherPosition.x() - position.x();
        int dy = otherPosition.y() - position.y();
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

    @Override
    public boolean holdPosition(String tooltip, boolean strategicLevel) {
        lastCommand = "Hold";
        target = null;
        targetPosition = null;
        return true;
    }

    @Override
    public boolean attackUnit(AUnit target) {
        lastCommand = "AttackUnit";
        this.target = (FakeUnit) target;
//        System.out.println("### ATTACK");
//        System.out.println("target = " + target);
//        System.out.println("target.targetPosition() = " + target.position());
//        System.out.println("### End of ATTACK");
        targetPosition = target.position();
        return true;
    }

//    @Override
//    public boolean move(AUnit target, UnitAction unitAction, String tooltip) {
//        lastCommand = "Move";
//        this.target = (FakeUnit) target;
//        targetPosition = target.targetPosition();
//        return true;
//    }

    @Override
    public boolean move(HasPosition target, UnitAction unitAction, String tooltip, boolean strategicLevel) {
        if (target == null) {
            throw new RuntimeException("FakeUnit move got null");
        }

        lastCommand = "Move";
        if (target instanceof FakeUnit) {
            this.target = (FakeUnit) target;
        } else {
            this.target = null;
        }
        targetPosition = target.position();
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

    public void changeRawUnitType(AUnitType newType) {
        rawType = newType;
    }
}
