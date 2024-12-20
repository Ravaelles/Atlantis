package atlantis.combat.micro.avoid;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.ForceAttackNearestEnemy;
import atlantis.combat.running.stop_running.protoss.ProtossShouldStopRunning;
import atlantis.units.AUnit;
import atlantis.util.We;

public class DetectUnitsNotRunning extends Manager {
    public DetectUnitsNotRunning(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        return (unit.isRunning() || unit.action().name().startsWith("RUN_"))
            && (!unit.isMoving() || unit.isStopped())
            && unit.lastStartedRunningMoreThanAgo(6);
    }

    @Override
    public Manager handle() {
//        System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - DetectUnitsNotRunning");
//        unit.paintCircleFilled(20, Color.Teal);
//        CameraCommander.centerCameraOn(unit);

        if (We.protoss()) {
            ProtossShouldStopRunning.decisionStopRunning(unit);
        }

//        if (unit.isCombatUnit() && unit.lastOrderWasFramesAgo() >= 2 && unit.hp() >= 23) {
////            if (new AttackNearbyEnemies(unit).invoked(this)) {
//            if (new ForceAttackNearestEnemy(unit).forceHandle() != null) {
////                System.err.println("ATTTT @ " + A.now() + " - " + unit.typeWithUnitId()
////                    + " - \n" + unit.target()
////                );
//                return usedManager(this);
//            }
////            else {
////                System.err.println("Ehmmm why? " + unit + " / " + unit.enemiesNearInRadius(4));
////            }
//        }

        return null;
    }
}
