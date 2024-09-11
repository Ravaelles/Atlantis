package atlantis.production.dynamic.reinforce.terran.turrets.offensive;

import atlantis.architecture.Commander;
import atlantis.combat.missions.Missions;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.A;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;
import atlantis.util.We;

import static atlantis.units.AUnitType.Terran_Missile_Turret;

public class TurretsToContainEnemy extends Commander {

    private static AChoke enemyMainChoke;
    private static APosition alphaCenter;

    @Override
    public boolean applies() {
        return turretNeeded();
    }

    private static boolean turretNeeded() {
        return We.terran()
            && Missions.isGlobalMissionAttack()
//            && !Have.scienceVessel()
            && alphaIsCloseToEnemy()
            && !haveAlreadyTurretCoveringThisPlace();
    }

    private static boolean haveAlreadyTurretCoveringThisPlace() {
        return Count.existingOrPlannedBuildingsNear(Terran_Missile_Turret, 8, positionForNext()) <= 0;
    }

    private static boolean alphaIsCloseToEnemy() {
        alphaCenter = Alpha.get().center();
        enemyMainChoke = Chokes.enemyMainChoke();

        return enemyMainChoke != null && alphaCenter != null && alphaCenter.distTo(enemyMainChoke) <= 15;
    }

    private static HasPosition positionForNext() {
        return alphaCenter.translateTilesTowards(enemyMainChoke, 3);
    }

    @Override
    protected void handle() {
        if ((new OffensiveTurrets()).buildIfNeeded()) return;

        HasPosition position = (new AnyOffensiveTurretNeeded()).getTurretNeededHere();

        if (position != null) {
            System.err.println("@ " + A.now() + " - OffensiveTurretAdded");
            AddToQueue.withHighPriority(Terran_Missile_Turret, position.position());
        }

    }
}
