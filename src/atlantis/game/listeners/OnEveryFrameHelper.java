package atlantis.game.listeners;

import atlantis.config.AtlantisRaceConfig;
import atlantis.debug.painter.AAdvancedPainter;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.game.CameraCommander;
import atlantis.information.enemy.EnemyOnCloseIsland;
import atlantis.map.base.ABaseLocation;
import atlantis.map.base.BaseLocations;
import atlantis.map.bullets.ABullet;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.path.OurClosestBaseToEnemy;
import atlantis.map.path.PathToEnemyBase;
import atlantis.map.position.APosition;

import atlantis.map.position.HasPosition;
import atlantis.map.region.ARegion;
import atlantis.map.region.MainRegion;
import atlantis.map.wall.GetWallIn;
import atlantis.map.wall.Structure;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.position.PositionFulfillsAllConditions;
import atlantis.production.constructions.position.base.NextBasePosition;
import atlantis.production.constructions.position.protoss.GatewayPosition;
import atlantis.production.constructions.position.protoss.PylonPosition;
import atlantis.production.constructions.position.terran.BarracksPosition;
import atlantis.production.constructions.position.terran.TerranPositionFinder;
import atlantis.production.orders.production.queue.Queue;
import atlantis.terran.chokeblockers.ChokeToBlock;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.attacked_by.Bullets;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.units.workers.FreeWorkers;
import atlantis.util.AConsole;
import atlantis.util.Vector;
import bwapi.Bullet;
import bwapi.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Auxiliary class, helpful when there's need to do something every frame and not spam other classes.
 */
public class OnEveryFrameHelper {

    // Aux code to be executed at every frame
    public static void handle() {
    }

    private static void paintPathToEnemyBase(ArrayList<AChoke> chokes) {
        int chokeIndex = 0;
        APosition prevPoint = null;

        for (AChoke choke : chokes) {
            AAdvancedPainter.paintChoke(choke, Color.Orange, "Milestone=" + chokeIndex);

            if (prevPoint != null) {
                AAdvancedPainter.paintLine(prevPoint, choke.center(), Color.Orange);
            }
            prevPoint = choke.center();

            chokeIndex++;
        }
    }
}
