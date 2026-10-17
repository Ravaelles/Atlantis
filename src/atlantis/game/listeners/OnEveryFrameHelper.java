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

    public static void handle() {


        if (true) return;

        Selection units = Select.ourCombatUnits().havingWeapon();
        for (AUnit unit : units.list()) {
            unit.paintTextCentered(unit.attackState().toString(), Color.Green, -1.4);

            if (unit.lastPositionChangedAgo() <= 10) continue;
            if (unit.attackState().none()) continue;







        }













































    }

    private static void enemyOnCloseIslandDebug() {
        HasPosition enemyOnCloseIsland = EnemyOnCloseIsland.get();
        if (enemyOnCloseIsland != null) {
            if (A.everyNthGameFrame(60)) {
            }
            enemyOnCloseIsland.paintCircleFilled(8, Color.Purple);
        }

    }

    private static void debugDragoonHoldToShoot() {
        AUnit unit = Select.ourOfType(AUnitType.Protoss_Dragoon).first();

        AUnit enemy = unit.enemiesThatCanAttackMe(100).nearestTo(unit);
        if (enemy == null) return;

        String lastCommand = unit.lastCommandName();
        if (!"".equals(lastCommand)) {
            System.err.println("@" + A.now() + ":  " + unit.action().name()
                + (unit.isAttackFrame() ? " (" + unit.cooldown() + ")" : "")
                + ", dist: " + A.digit(unit.distTo(enemy))
            );
        }
    }

    private static void updateTooltips() {
        for (AUnit unit : Select.ourCombatUnits().list()) {
            unit.paintTextCentered(unit.runningManager().lastRunMode(), Color.Orange, -1);
        }
    }

    private static void paintAllUnitEvals() {
        for (AUnit unit : Select.enemyUnits()) {
            unit.paintTextCentered(unit, A.digit(unit.eval()), Color.Orange);
        }
        for (AUnit unit : Select.our().list()) {
            unit.paintTextCentered(unit, A.digit(unit.eval()), Color.Blue);
        }
    }

    private static void paintEnemiesTargets() {
        AUnit our = Select.ourCombatUnits().first();
        if (our == null) return;

        if (our.isAttacking()) {
            our.paintLine(our.target(), Color.Orange);
        }


        for (AUnit enemy : our.enemiesNear().list()) {
            enemy.paintTextCentered(enemy.idWithHash() + " (" + enemy.hp() + ")", Color.Orange, -1);
        }

    }

    private static void paintEnemiesFacingOurDirection() {
        AUnit our = Select.ourCombatUnits().first();
        if (our == null) return;

        our.paintCircle(our.groundWeaponRange() * 32, 1, Color.Orange);

        for (AUnit enemy : our.enemiesNear().list()) {
            enemy.paintCircleFilled(8, our.isOtherFacingThisUnit(enemy) ? Color.Red : Color.Green);
        }

    }

    private static void paintBasePositionAndBuildCoordinates() {
        ABaseLocation base = BaseLocations.main();
        AUnit main = Select.main();
        APosition mainPosition = Select.mainOrAnyBuildingPosition();

        AAdvancedPainter.paintCircle(base, 10, Color.Orange);
        AAdvancedPainter.paintCircle(base.position(), 8, Color.Red);
        AAdvancedPainter.paintCircle(main, 6, Color.Green);
        AAdvancedPainter.paintCircle(main.position(), 4, Color.Blue);

        System.err.println("base = " + base);
        System.err.println("main = " + main);
        System.err.println("mainPosition = " + mainPosition);
    }

    private static void printBaseLocationsDistancesForTests() {
        BaseLocations.main().paintTextCentered("Main BAZE", Color.Orange, 1);

        ABaseLocation mainBase = BaseLocations.main();
        System.out.println("mainBase = " + mainBase);
        for (ABaseLocation location : BaseLocations.baseLocations()) {
            System.out.println(
                location.tx() + ", " + location.ty() + ": "
                    + ", GD: " + A.digit(location.groundDist(mainBase))
            );
        }
        System.out.println("-------------------");

        A.quit();
    }

    private static void printChokesForTests() {

        for (AChoke choke : Chokes.chokes()) {
        }
        System.err.println("-----------------");
    }

    private static void paintOverUnits() {
        for (AUnit unit : Select.ourCombatUnits().list()) {
            unit.paintTextCentered(unit, A.digit(unit.eval()), Color.Orange);
        }
    }

    private static void paintUnitTargets() {
        for (AUnit unit : Select.ourCombatUnits().list()) {
            if (unit.target() != null) {
                unit.paintLine(unit.target(), Color.Orange);
            }
            else if (unit.targetPosition() != null) {
                unit.paintLine(unit.target(), Color.Cyan);
            }
        }
    }

    private static void paintUnitSpeeding() {
        AUnit unit = Select.ourCombatUnits().first();

        System.err.println("@ " + A.now() + " --------------------------------- ");
        System.err.println("AttackFrame() = " + unit.lastAttackFrameAgo());
        System.err.println("StartedAttack() = " + unit.lastStartedAttackAgo());

        if (unit.isAttackFrame()) unit.paintCircleFilled(10, Color.Red);
        if (unit.isAccelerating()) unit.paintCircle(11, 3, Color.Green);
    }

    private static void paintBullets() {
        for (ABullet bullet : Bullets.knownBullets()) {
            AAdvancedPainter.paintLine(
                bullet.position(), bullet.position().translateByTiles(0, -2), Color.Orange
            );
        }
    }

    private static void printLastActions() {
        System.err.println("@ " + A.now());
        for (AUnit unit : Select.ourCombatUnits().dragoons().list()) {
            System.err.println(unit.typeWithUnitId() + " - " + unit.manager() + " / " + unit.orderTarget());
        }
    }

    private static void printBullets() {

        List<Bullet> bullets = AGame.get().getBullets();
        if (bullets.isEmpty()) return;

        System.err.println("@ " + A.now() + " - BULLETS");
        for (Bullet b : bullets) {
            System.out.println(b.getType() + " from " + b.getSource()
                    + " to " + b.getTarget()
                    + ", pos:" + b.getPosition()
                    + ", dist:" + b.getPosition().getApproxDistance(b.getTargetPosition())
            );
        }
    }

    private static void printUnitManagers() {
        System.out.println("@ " + A.now());
        for (AUnit unit : Select.ourCombatUnits().list()) {
            System.out.println(unit.typeWithUnitId() + " - " + unit.manager());
        }
    }

    private static void paintAllowedGatewayPositions() {
        HasPosition nearTo = Select.ourOfType(AUnitType.Protoss_Pylon).last();
        if (nearTo == null) return;

        int x0 = nearTo.tx();
        int y0 = nearTo.ty();
        int delta = 23;

        for (int tx = x0 - delta; tx <= x0 + delta; tx++) {
            for (int ty = y0 - delta; ty <= y0 + delta; ty++) {
                APosition position = APosition.create(tx, ty);
                Color color = canBuildGatewayHere(position, nearTo) ? Color.Teal : Color.Orange;
                AAdvancedPainter.paintRectangle(position, 32, 32, color);
            }
        }
    }

    private static boolean canBuildGatewayHere(APosition constructionPosition, HasPosition nearTo) {
        AUnit builder = FreeWorkers.getOne();
        AUnitType building = AUnitType.Protoss_Gateway;

        return PositionFulfillsAllConditions.doesPositionFulfillAllConditions(
            builder, building, constructionPosition, nearTo
        );
    }

    private static void paintWalkableTiles() {
        int x0 = Select.main().tx();
        int y0 = Select.main().ty();
        int delta = 23;

        for (int tx = x0 - delta; tx <= x0 + delta; tx++) {
            for (int ty = y0 - delta; ty <= y0 + delta; ty++) {
                APosition position = APosition.create(tx, ty);
                Color color = position.isWalkable() ? Color.Teal : Color.Orange;
                AAdvancedPainter.paintRectangle(position, 32, 32, color);
            }
        }
    }

    private static void printNextBarracks() {
        if (A.seconds() <= 2) return;
        if (A.now() % 30 != 0) return;

        HasPosition barracks = BarracksPosition.nextPosition();
        System.err.println("---------------------------");
        System.err.println("barracks = " + barracks);
    }

    private static void printNextGateway() {
        if (A.now() % 30 != 0) return;

        HasPosition nextGateway = GatewayPosition.nextPosition();
        HasPosition nextPylon = PylonPosition.nextPosition();
        System.err.println("---------------------------");
        System.err.println("nextPylon = " + nextPylon);
        System.err.println("nextGateway = " + nextGateway);
    }

    private static void paintChokeBlock() {
        AChoke choke = ChokeToBlock.get();
        if (choke == null) return;

        AAdvancedPainter.forcePainting();

        AAdvancedPainter.paintCircleFilled(choke.pointA(), 5, Color.Orange);
        AAdvancedPainter.paintCircleFilled(choke.pointB(), 5, Color.Yellow);

        Vector vector = ChokeToBlock.defineTranslationVector(choke);

        APosition position = choke.center().translateByVector(vector);
        AAdvancedPainter.paintCircleFilled(
            position,
            10, Color.Green
        );

        CameraCommander.centerCameraOn(position);
    }

    private static void printCombatUnitStatus() {

        for (AUnit unit : Select.ourCombatUnits().list()) {
            unit.paintLine(unit.position().translateByTiles(0, 2), Color.Black);
            unit.paintTextCentered(
                unit.position().translateByTiles(0, 2),
                unit.idWithHash(), // + " (" + unit.squad() + ")",
                Color.Black
            );

        }
    }

    private static void paintNextBasePosition() {
        APosition nextBase = NextBasePosition.nextBasePosition();


        if (nextBase == null) return;

        AAdvancedPainter.paintBuildingPosition(nextBase, "Next_BASE", AtlantisRaceConfig.BASE);
    }

    private static void paintAttackTargetsForOur() {
        for (AUnit unit : Select.ourCombatUnits().list()) {
            if (unit.noTarget()) continue;

            unit.paintLine(unit.target(), Color.Orange);
        }
    }

    private static void printObserverStatus() {
        AUnit first = Select.our().ofType(AUnitType.Protoss_Observer).first();
        if (first == null) return;

        System.out.println("@ " + A.now() + " - " + first + " - " + first.manager());
    }

    private static void paintAllConstructions() {
        if (!A.everyNthGameFrame(117)) return;

        System.out.println("@ " + A.now() + " - ");
        for (Construction construction : ConstructionRequests.all()) {
            System.out.println("construction = " + construction);
        }
    }

    private static void printMarineManagers() {
        for (AUnit unit : Select.ourOfType(AUnitType.Terran_Marine).list()) {
        }
    }

    private static void printFirstCombatUnitStatus() {
        if (A.now() <= 21) return;

        AUnit unit = Select.ourCombatUnits().first();
        if (unit == null) return;


        System.err.println("@ " + A.now() + " - " + unit.managerLogs().toString());



    }


    private static void printQueue() {
        if (A.everyNthGameFrame(30 * 9)) {
            Queue.get().allOrders().print("All orders");
        }
    }

    private static void paintMainChokeDetails() {
        AChoke choke = Chokes.mainChoke();

        if (choke == null) return;

        if (A.now() <= 1) {
            CameraCommander.centerCameraOn(choke.center());
            AAdvancedPainter.paintingMode = AAdvancedPainter.MODE_FULL_PAINTING;
        }

        // === Define regions ======================================

        APosition point1 = choke.firstPoint();
        APosition point2 = choke.lastPoint();


        ARegion regionA = choke.firstRegion();
        ARegion regionB = choke.secondRegion();

        boolean regionAIsMain = MainRegion.isMainRegion(regionA);
        boolean regionBIsMain = MainRegion.isMainRegion(regionB);

        AAdvancedPainter.paintPosition(regionA, regionAIsMain ? Color.Green : Color.Red, "A");
        AAdvancedPainter.paintPosition(regionB, regionBIsMain ? Color.Green : Color.Red, "B");

        APosition point3 = null;
        if (regionAIsMain) point3 = point1.translateTilesTowards(-1, regionB.center());
        else point3 = point1.translateTilesTowards(-1, regionA.center());

        AAdvancedPainter.paintCircleFilled(point3, 8, Color.Purple);

        // =========================================================

        APosition position1 = TerranPositionFinder.findStandardPositionFor(
            null, AUnitType.Terran_Supply_Depot, point3, 6
        );
        APosition position2 = TerranPositionFinder.findStandardPositionFor(
            null, AUnitType.Terran_Supply_Depot, point3, 6
        );

        if (position1 != null) AAdvancedPainter.paintRectangle(position1, 2 * 32, 2 * 32, Color.Cyan);

    }

    private static void sendWorkersToBlockChoke(APosition point1, APosition point2) {


    }

    private static void paintNearestBaseToEnemy() {
        AUnit nearestToEnemy = OurClosestBaseToEnemy.get();

        if (nearestToEnemy == null) return;

        AAdvancedPainter.paintCircleFilled(nearestToEnemy, 9, Color.Orange);
        AAdvancedPainter.paintTextCentered(nearestToEnemy, "NearestBaseToEnemy", Color.Orange);
    }

    private static void testScvMoveAwayFrom() {
        List<AUnit> workers = Select.ourWorkers().list();
        for (AUnit unit : workers) {
            if (unit.id() % 3 != 0) continue;

            AUnit avoid = Select.ourWorkers().exclude(unit).nearestTo(unit);

            if (avoid != null && avoid.distTo(unit) <= 5) {
                unit.moveAwayFrom(avoid, 1, Actions.MOVE_AVOID, "TestAvoid");
                AAdvancedPainter.paintLine(unit, unit.targetPosition(), Color.Cyan);
            }
        }
    }

    private static void paintMyOwnWallInSolution() {
        Set<Structure> structures = GetWallIn.get();
        GetWallIn.paint(structures);
    }

    private static void combatUnitInfo() {
        AUnit first = Select.ourCombatUnits().nonBuildings().first();
        if (first == null) return;

        AConsole.println(first.typeWithHash() + " / " + first.manager() + " / " + first.eval());

    }



    private static void pathToEnemyBase() {
        paintPathToEnemyBase(PathToEnemyBase.chokesLeadingToEnemyBase());

        // BATTLE CUTOVER: the focus-choke paint (deleted focus logic) is gone.
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
