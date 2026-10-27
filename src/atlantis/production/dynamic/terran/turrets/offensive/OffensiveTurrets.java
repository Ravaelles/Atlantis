package atlantis.production.dynamic.terran.turrets.offensive;

import atlantis.battle.cluster.Alpha;
import atlantis.battle.cluster.Missions;
import atlantis.battle.cluster.Squad;
import atlantis.map.position.HasPosition;
import atlantis.production.dynamic.terran.turrets.TurretsForNonMain;
import atlantis.units.select.Have;

public class OffensiveTurrets extends TurretsForNonMain {
    public boolean buildIfNeeded() {
        if (!Have.engBay()) return false;

        return handleReinforceMissionContain();
    }

    // =========================================================

//    protected boolean handleReinforceMissionAttack() {
//        HasPosition squadCenter = Squad.alphaCenter();
//        if (squadCenter == null) {
//            return false;
//        }
//
//        return TerranMissileTurret.get().handleReinforcePosition(squadCenter, 14);
//    }

    protected boolean handleReinforceMissionContain() {
        HasPosition focusPoint = Missions.globalMission().focusPoint();
        if (focusPoint == null || !Have.main()) return false;

        if (Alpha.count() <= 12) return false;

        return handleReinforcePosition(containReinforcePoint(focusPoint), 9);
    }

    protected HasPosition containReinforcePoint(HasPosition focusPoint) {
        HasPosition point = focusPoint;
//        FoggedUnit enemyBuilding = EnemyUnits.nearestEnemyBuilding();

        HasPosition alphaCenter = Squad.alphaCenter();
        if (alphaCenter != null) {
//            point = point.translateTilesTowards(-6, enemyBuilding);
//            AChoke choke = Chokes.nearestChoke(enemyBuilding);
//            if (choke != null) {
            point = point.translatePercentTowards(113, alphaCenter);
//            }
        }

        return point;
    }

}
