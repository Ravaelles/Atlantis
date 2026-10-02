package atlantis.core.world;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.CappedList;
import bwapi.TechType;

import java.io.Serializable;

/**
 * Stage E (see _AI/REVIEW.md §16): accumulated derived per-unit state,
 * progressively migrated out of {@code AUnit}'s {@code public _last*} fields.
 *
 * <p>Plain data holder with an explicit API — no behaviour, no engine calls.
 * Owned one-per-unit by {@code AUnit} for now; the {@code World} registry
 * takes over ownership as Stage E progresses.</p>
 */
public class UnitState implements Serializable {

    private CappedList<Integer> lastHitPoints = new CappedList<>(20);
    private int lastAttackOrder = -9999;
    private int lastAttackFrame = -9999;
    private int lastCommandIssued = -9877;
    private int lastCooldown;
    private int lastFrameOfStartingAttack = -9999;
    private int lastRetreat = -9998;
    private int lastStartedRunning = -999;
    private int lastStoppedRunning = -999;
    private int lastRunningPositionChange = -999;
    private int lastStartedAttack = -999;
    private AUnit lastTarget = null;
    private AUnitType lastTargetType = null;
    private int lastTargetToAttackAcquired = -999;
    private TechType lastTech;
    private APosition lastTechPosition;
    private AUnit lastTechUnit;
    private int lastUnderAttack = -999;
    private int lastX = -1;
    private int lastY = -1;
    private int lastPositionChanged = -999;
    private HasPosition lastPositionRunInAnyDir = null;

    public CappedList<Integer> getLastHitPoints() {
        return lastHitPoints;
    }

    public void setLastHitPoints(CappedList<Integer> lastHitPoints) {
        this.lastHitPoints = lastHitPoints;
    }

    public int getLastAttackOrder() {
        return lastAttackOrder;
    }

    public void setLastAttackOrder(int lastAttackOrder) {
        this.lastAttackOrder = lastAttackOrder;
    }

    public int getLastAttackFrame() {
        return lastAttackFrame;
    }

    public void setLastAttackFrame(int lastAttackFrame) {
        this.lastAttackFrame = lastAttackFrame;
    }

    public int getLastCommandIssued() {
        return lastCommandIssued;
    }

    public void setLastCommandIssued(int lastCommandIssued) {
        this.lastCommandIssued = lastCommandIssued;
    }

    public int getLastCooldown() {
        return lastCooldown;
    }

    public void setLastCooldown(int lastCooldown) {
        this.lastCooldown = lastCooldown;
    }

    public int getLastFrameOfStartingAttack() {
        return lastFrameOfStartingAttack;
    }

    public void setLastFrameOfStartingAttack(int lastFrameOfStartingAttack) {
        this.lastFrameOfStartingAttack = lastFrameOfStartingAttack;
    }

    public int getLastRetreat() {
        return lastRetreat;
    }

    public void setLastRetreat(int lastRetreat) {
        this.lastRetreat = lastRetreat;
    }

    public int getLastStartedRunning() {
        return lastStartedRunning;
    }

    public void setLastStartedRunning(int lastStartedRunning) {
        this.lastStartedRunning = lastStartedRunning;
    }

    public int getLastStoppedRunning() {
        return lastStoppedRunning;
    }

    public void setLastStoppedRunning(int lastStoppedRunning) {
        this.lastStoppedRunning = lastStoppedRunning;
    }

    public int getLastRunningPositionChange() {
        return lastRunningPositionChange;
    }

    public void setLastRunningPositionChange(int lastRunningPositionChange) {
        this.lastRunningPositionChange = lastRunningPositionChange;
    }

    public int getLastStartedAttack() {
        return lastStartedAttack;
    }

    public void setLastStartedAttack(int lastStartedAttack) {
        this.lastStartedAttack = lastStartedAttack;
    }

    public AUnit getLastTarget() {
        return lastTarget;
    }

    public void setLastTarget(AUnit lastTarget) {
        this.lastTarget = lastTarget;
    }

    public AUnitType getLastTargetType() {
        return lastTargetType;
    }

    public void setLastTargetType(AUnitType lastTargetType) {
        this.lastTargetType = lastTargetType;
    }

    public int getLastTargetToAttackAcquired() {
        return lastTargetToAttackAcquired;
    }

    public void setLastTargetToAttackAcquired(int lastTargetToAttackAcquired) {
        this.lastTargetToAttackAcquired = lastTargetToAttackAcquired;
    }

    public TechType getLastTech() {
        return lastTech;
    }

    public void setLastTech(TechType lastTech) {
        this.lastTech = lastTech;
    }

    public APosition getLastTechPosition() {
        return lastTechPosition;
    }

    public void setLastTechPosition(APosition lastTechPosition) {
        this.lastTechPosition = lastTechPosition;
    }

    public AUnit getLastTechUnit() {
        return lastTechUnit;
    }

    public void setLastTechUnit(AUnit lastTechUnit) {
        this.lastTechUnit = lastTechUnit;
    }

    public int getLastUnderAttack() {
        return lastUnderAttack;
    }

    public void setLastUnderAttack(int lastUnderAttack) {
        this.lastUnderAttack = lastUnderAttack;
    }

    public int getLastX() {
        return lastX;
    }

    public void setLastX(int lastX) {
        this.lastX = lastX;
    }

    public int getLastY() {
        return lastY;
    }

    public void setLastY(int lastY) {
        this.lastY = lastY;
    }

    public int getLastPositionChanged() {
        return lastPositionChanged;
    }

    public void setLastPositionChanged(int lastPositionChanged) {
        this.lastPositionChanged = lastPositionChanged;
    }

    public HasPosition getLastPositionRunInAnyDir() {
        return lastPositionRunInAnyDir;
    }

    public void setLastPositionRunInAnyDir(HasPosition lastPositionRunInAnyDir) {
        this.lastPositionRunInAnyDir = lastPositionRunInAnyDir;
    }
}
