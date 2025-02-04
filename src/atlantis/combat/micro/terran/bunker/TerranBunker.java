package atlantis.combat.micro.terran.bunker;

import atlantis.information.enemy.EnemyInfo;
import atlantis.production.requests.AntiLandBuildingCommander;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;
import atlantis.game.player.Enemy;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

public class TerranBunker extends AntiLandBuildingCommander {
    @Override
    public AUnitType type() {
        return AUnitType.Terran_Bunker;
    }

    @Override
    public int expected() {
        if (EnemyInfo.isDoingEarlyGamePush()) {
            return (Enemy.zerg() || Enemy.protoss()) ? 2 : 1;
        }

        return 1;
    }

    @Override
    public boolean shouldBuildNew() {
        if (!We.terran()) {
            ErrorLog.printMaxOncePerMinute("shouldBuildNew (Bunker) called for non-terran");
            return false;
        }

        if (!Have.barracks()) return false;

        return (new ShouldBuildNewBunker()).shouldBuild();
    }

    @Override
    public boolean requestToBuildNewAntiLandCombatBuilding() {
        if (!shouldBuildNew()) return false;

//        System.err.println("@ " + A.now() + " - REQUESTED NEW BUNKER, NOW DEFINE POS");
        return (new NewBunker()).requestNewAndAutomaticallyDecidePosition();
    }

}
