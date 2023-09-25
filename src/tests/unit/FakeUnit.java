package tests.unit;

import atlantis.combat.missions.Mission;
import atlantis.combat.missions.Missions;
import atlantis.config.env.Env;
import atlantis.game.APlayer;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Action;
import bwapi.TechType;
import bwapi.UnitType;
import bwapi.UpgradeType;
import tests.fakes.FakePlayer;

public class FakeUnit extends AUnit {
    public static int firstFreeId = 10;
    public int id;
    public AUnitType rawType;
    public APosition position;
    public APlayer player;
    public boolean enemy = false;
    public boolean neutral = false;
    public APosition targetPosition = null;
    public boolean burrowed = false;
    public boolean cloaked = false;
    public boolean completed = true;
    public boolean detected = true;
    public boolean effCloaked = false;
    public boolean effVisible = true;
    public boolean idle = false;
    public boolean isVisibleUnitOnMap = true;
    public boolean loaded = false;
    public boolean lockedDown = false;
    public TechType researching = null;
    public UpgradeType upgrading = null;
    public boolean stasised = false;
    public boolean stimmed = false;
    public FakeUnit target = null;
    public int energy = 0;
    public String lastCommand = "None";
    public TechType lastTechUsed = null;

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
        return "Fake " + super.toString() + (isEnemy() ? " (Enemy)" : "");
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
    public FakePlayer player() {
        if (isOur()) {
            return FakePlayer.OUR;
        }
        else if (isEnemy()) {
            return FakePlayer.ENEMY;
        }
        return FakePlayer.NEUTRAL;
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
    public boolean effUndetected() {
        return effCloaked;
    }

    @Override
    public boolean isAccelerating() {
        return false;
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
    public boolean hasAddon() {
        return false;
    }

    @Override
    public boolean isConstructing() {
        return false;
    }

    @Override
    public boolean isGatheringMinerals() {
        return false;
    }

    @Override
    public boolean isGatheringGas() {
        return false;
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
    public boolean isCloaked() {
        return cloaked || isDT() || isObserver();
    }

    @Override
    public boolean isResearching() {
        return researching != null;
    }

    @Override
    public TechType whatIsResearching() {
        return researching;
    }

    @Override
    public boolean isUpgrading() {
        return upgrading != null;
    }

    @Override
    public UpgradeType whatIsUpgrading() {
        return upgrading;
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
    protected AUnitType cacheType() {
        _lastType = rawType;
        return null;
    }

    @Override
    public double distTo(AUnit otherUnit) {
//        System.err.println("FakeUnit::distTo (AUnit)");

        int dx = otherUnit.x() - position.x();
        int dy = otherUnit.y() - position.y();

//        System.err.println("this = " + this.position + " // " + this);
//        System.err.println("otherUnit = " + ((FakeUnit) otherUnit).position);
//        System.err.println("dx = " + dx + ", dy = " + dy);
//        System.err.println("Fake dist = " + (Math.sqrt(dx * dx + dy * dy) / 32.0));

//        System.err.println("X =  " + otherUnit.x() + " // " + position.x() + " // " + dx);
//        System.err.println("Y =  " + otherUnit.y() + " // " + position.y() + " // " + dy);
//        System.err.println("Length = " + Math.sqrt(dx * dx + dy * dy) / 32.0);

        return Math.sqrt(dx * dx + dy * dy) / 32.0;
    }

    @Override
    public double distTo(HasPosition otherPosition) {
//        System.err.println("FakeUnit::distTo (HasPosition)");

        int dx = otherPosition.x() - position.x();
        int dy = otherPosition.y() - position.y();
        return Math.sqrt(dx * dx + dy * dy) / 32.0;
    }

    @Override
    public double groundDist(HasPosition other) {
        return distTo(other);
    }

    @Override
    public boolean isFacing(AUnit otherUnit) {
        return true;
    }

    // =========================================================
    // Orders

    @Override
    public Mission mission() {
        return Missions.ATTACK;
    }

    @Override
    public boolean useTech(TechType tech, AUnit target) {
        this.lastTechUsed = tech;
        this.target = (FakeUnit) target;
        return true;
    }

    @Override
    public boolean holdPosition(String tooltip) {
        lastCommand = "Hold";
        target = null;
        targetPosition = null;
        return true;
    }

    @Override
    public boolean attackUnit(AUnit target) {
        lastCommand = "AttackUnit";
        this.target = (FakeUnit) target;
//        System.err.println("### ATTACK");
//        System.err.println("target = " + target);
//        System.err.println("target.targetPosition() = " + target.position());
//        System.err.println("### End of ATTACK");
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
    public boolean move(HasPosition target, Action unitAction, String tooltip, boolean strategicLevel) {
        if (target == null) {
            throw new RuntimeException("FakeUnit move got null");
        }

        lastCommand = "Move";
        if (target instanceof FakeUnit) {
            this.target = (FakeUnit) target;
        }
        else {
            this.target = null;
        }

        setAction(unitAction);
        targetPosition = target.position();
        return true;
    }

    // =========================================================

    public void changeRawUnitType(AUnitType newType) {
        rawType = newType;
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

    public FakeUnit setBurrowed(boolean burrowed) {
        this.burrowed = burrowed;
        return this;
    }

    public FakeUnit setCloaked(boolean cloaked) {
        this.cloaked = cloaked;
        return this;
    }

    public FakeUnit setEffCloaked(boolean effCloaked) {
        this.effCloaked = effCloaked;
        return this;
    }

    public FakeUnit setEffVisible(boolean effVisible) {
        this.effVisible = effVisible;
        return this;
    }

    @Override
    public boolean effVisible() {
        return effVisible;
    }

    @Override
    public boolean isVisibleUnitOnMap() {
        return isVisibleUnitOnMap;
    }

    @Override
    public boolean isBurrowed() {
        return burrowed;
    }

    @Override
    public boolean isIdle() {
        return idle;
    }

    @Override
    public boolean isLockedDown() {
        return lockedDown;
    }

    public FakeUnit setLockedDown(boolean lockedDown) {
        this.lockedDown = lockedDown;
        return this;
    }

    @Override
    public boolean isStasised() {
        return stasised;
    }

    public FakeUnit setStasised(boolean stasised) {
        this.stasised = stasised;
        return this;
    }

    @Override
    public int shields() {
        return 0;
    }

    @Override
    public boolean canLift() {
        return true;
    }
}
