package atlantis.combat.micro.avoid;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.dont.DontAvoidEnemy;
import atlantis.combat.micro.avoid.terran.ShouldNotAvoidSingleEnemyBunker;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.log.ErrorLog;

public class AvoidSingleEnemy extends Manager {
    protected final RunError runError;
    private final AUnit enemy;

    public AvoidSingleEnemy(AUnit unit, AUnit enemy) {
        super(unit);
        this.enemy = enemy;
        runError = new RunError(unit);
    }

    //    public Manager avoid() {
    public Manager avoid() {
        if (this.enemy == null) {
//            ErrorLog.printMaxOncePerMinute("AvoidSingleEnemy - enemy is NULL for " + unit);
            return processDontAvoid();
        }

        if (enemy.hasBiggerWeaponRangeThan(unit)) {
            unit.addLog("EnemyHasLongerDick");
            return null;
        }

        if (ShouldNotAvoidSingleEnemyBunker.check(unit, enemy)) {
            unit.addLog("SafeOwningBunker");
            return null;
        }

//        System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - @D@ Avoiding single " + enemy);

        if (unit.runningManager().runFrom(
            enemy, calculateRunDistance(enemy), Actions.RUN_ENEMY, allowedToNotifyNearUnitsToMakeSpace()
        )) {
            return this;
        }

        return runError.handleErrorRun(unit, -345);
    }

    private boolean allowedToNotifyNearUnitsToMakeSpace() {
        return unit.isDragoon() && unit.hp() <= 61;
    }

    private Manager processDontAvoid() {
        unit.runningManager().stopRunning();
        if (unit.isMoving()) unit.holdPosition("AvoidHold");

        return null;
    }

    private boolean doNotAvoid() {
        if (this.enemy == null) return true;
        if (unit.effUndetected()) return true;
//        if (unit.hp() >= 33 && unit.isRepairerOfAnyKind()) return true;
//        if (isEnemyFacingOtherWayAndWeLookSafe()) return true;
//
//        APainter.paintCircle(enemy, 16, Color.Orange);

        if (enemy.position() == null) {
            System.err.println("enemy.position() is NULL for " + enemy);
            return true;
        }

        if ((new DontAvoidEnemy(unit)).applies()) return true;

        return false;
    }

    private boolean isEnemyFacingOtherWayAndWeLookSafe() {
        return enemy.isMelee()
            && unit.hp() >= 18
            && enemy.distTo(unit) >= 1.4
            && !unit.isOtherUnitFacingThisUnit(enemy);
    }

    protected double calculateRunDistance(AUnit enemy) {
        if (enemy.isCombatBuilding()) {
            return 0.25;
        }

        if (unit.isVulture()) {
            return 4.5;
        }
        else if (unit.isInfantry()) {
            return 2.7;
        }
        else {
            return 3.5;
        }
    }

    @Override
    public String toString() {
        String target = unit.runningFromUnit() == null
            ? (unit.runningFromPosition() != null ? unit.runningFromPosition().toString() : "NoNe")
            : unit.runningFromUnit().type().name();
        return super.toString() + "(" + target + ")";
    }
}
