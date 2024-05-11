package atlantis.game.events;

import atlantis.config.AtlantisRaceConfig;
import atlantis.debug.painter.AAdvancedPainter;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.game.CameraCommander;
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
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.constructing.position.PositionFulfillsAllConditions;
import atlantis.production.constructing.position.base.NextBasePosition;
import atlantis.production.constructing.position.protoss.GatewayPosition;
import atlantis.production.constructing.position.protoss.PylonPosition;
import atlantis.production.constructing.position.terran.BarracksPosition;
import atlantis.production.constructing.position.terran.TerranPositionFinder;
import atlantis.production.orders.production.queue.Queue;
import atlantis.terran.chokeblockers.ChokeToBlock;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.attacked_by.Bullets;
import atlantis.units.interrupt.ContinueOldBroklenShootingOld;
import atlantis.units.select.Select;
import atlantis.units.workers.FreeWorkers;
import atlantis.util.Vector;
import bwapi.Bullet;
import bwapi.Color;
import jbweb.Block;
import jbweb.Blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Auxiliary class, helpful when there's need to do something every frame and not spam other classes.
 */
public class OnEveryFrameHelper {
    public static void handle() {
//        SaveUnitsAndBulletsInfoToFile.save();

//        paintMissionAttackFocusPoint();
//        pathToEnemyBase();

//        paintJbwebBlocks();

//        CurrentProductionQueue.print();

//        if (A.supplyUsed() >= 41 && A.now() % 30 == 0) {
//            Queue.get().readyToProduceOrders().print("All orders");
//        }

//        combatUnitInfo();

//        paintMyOwnWallInSolution(); // <<<<<<<<<<<<<<<<<

//        testScvMoveAwayFrom();

//        paintNearestBaseToEnemy();

//        paintMainChokeDetails();

//        printQueue();

//        validateNextDepotPosition();

//        printFirstCombatUnitStatus();

//        printMarineManagers();

//        paintAllConstructions();

//        printObserverStatus();

//        paintAttackTargetsForOur();

//        paintNextBasePosition();

//        printCombatUnitStatus();

//        paintChokeBlock();

//        printNextGateway();
//        printNextBarracks();

//        paintWalkableTiles();
//        paintAllowedGatewayPositions();

//        printUnitManagers();

//        printBullets();

//        printLastActions();

        paintBullets();
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
//            System.err.println(unit.typeWithUnitId() + " - " + unit.distToNearestChoke());
        }
    }

    private static void printBullets() {
//        Bullets.updateKnown();

        List<Bullet> bullets = AGame.get().getBullets();
        if (bullets.isEmpty()) return;

        System.err.println("@ " + A.now() + " - BULLETS");
        for (Bullet b : bullets) {
            System.out.println(b.getType() + " from " + b.getSource()
                    + " to " + b.getTarget()
                    + ", pos:" + b.getPosition()
                    + ", dist:" + b.getPosition().getApproxDistance(b.getTargetPosition())
//                + ", :" + b.
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
//        System.out.println("@ " + A.now() + " - ");

        for (AUnit unit : Select.ourCombatUnits().list()) {
            unit.paintLine(unit.position().translateByTiles(0, 2), Color.Black);
            unit.paintTextCentered(
                unit.position().translateByTiles(0, 2),
                unit.idWithHash(), // + " (" + unit.squad() + ")",
                Color.Black
            );

//            System.out.println(
//                unit.typeWithUnitId() + " - " + unit.manager() + " - " + unit.lastCommandName()
//                    + " / target:" + unit.target() + " / orderTarget=" + unit.orderTarget()
//            );
        }
    }

    private static void paintNextBasePosition() {
        APosition nextBase = NextBasePosition.nextBasePosition();

//        System.out.println("NextBasePosition = " + nextBase);

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
            System.out.println("@ " + A.now() + " - Marine#" + unit.id() + " - " + unit.manager());
            System.out.println(unit.managerLogs().toString());
//            System.out.println(unit.target());
        }
    }

    private static void printFirstCombatUnitStatus() {
        if (A.now() <= 21) return;

        AUnit unit = Select.ourCombatUnits().first();
        if (unit == null) return;

//        double enemyDist = unit.nearestEnemyDist();
//        System.err.println("@ " + A.now() + ": " + unit.manager()
//            + " \\ dist: " + A.digit(enemyDist)
//            + " \\ cool:" + unit.cooldown());

        System.err.println("@ " + A.now() + " - " + unit.managerLogs().toString());

//        unit.setTooltip("Cooldown:" + unit.cooldown());

//        if (unit.isAttacking()) {
//            AAdvancedPainter.paintCircle(unit, 12, Color.Orange);
//            AAdvancedPainter.paintCircle(unit, 11, Color.Orange);
//            AAdvancedPainter.paintCircle(unit, 8, Color.Orange);
//            AAdvancedPainter.paintCircle(unit, 7, Color.Orange);
//        }

        if ((new ContinueOldBroklenShootingOld(unit)).applies()) {
            AAdvancedPainter.paintCircle(unit, 10, Color.Teal);
            AAdvancedPainter.paintCircle(unit, 9, Color.Teal);
            AAdvancedPainter.paintCircle(unit, 6, Color.Teal);
            AAdvancedPainter.paintCircle(unit, 5, Color.Teal);
            AAdvancedPainter.paintCircle(unit, 4, Color.Teal);
        }
    }

//    private static void validateNextDepotPosition() {
//        SupplyDepotPositionFinder.findPosition(null, null, null);
//    }

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

        AAdvancedPainter.paintCircleFilled(point1, 4, Color.Yellow);
        AAdvancedPainter.paintCircleFilled(point2, 4, Color.Orange);

        ARegion regionA = choke.firstRegion();
        ARegion regionB = choke.secondRegion();
//        System.err.println(regionA + " / " + regionB + " / " + Select.main().position().region());

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
//        if (position2 != null) AAdvancedPainter.paintRectangle(position2, 2 * 32, 2 * 32, Color.Teal);

//        sendWorkersToBlockChoke(point1, point2);
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

        A.println(first.typeWithHash() + " / " + first.manager() + " / " + first.combatEvalRelative());

//        if (first.combatEvalRelative() < 1 && first.isActiveManager(AttackNearbyEnemies.class)) {
//            A.printStackTrace("Why is this unit attacking?");
//        }
    }

    private static void paintJbwebBlocks() {
//        Blocks.draw();

        for (Block block : Blocks.getAllBlocks()) {
            AAdvancedPainter.paintRectangle(
                APosition.create(block.getTilePosition().toPosition()),
                block.width() * 32,
                block.height() * 32,
                Color.Grey
            );
        }
    }

    private static void pathToEnemyBase() {
        paintPathToEnemyBase(PathToEnemyBase.chokesLeadingToEnemyBase());
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

//    private static void paintMissionAttackFocusPoint() {
//        AFocusPoint focusPoint = Alpha.get().mission().focusPoint();
//        AUnit unit = Alpha.get().first();
//
//        if (focusPoint != null) {
//            APainter.paintLine(unit, focusPoint, Color.Cyan);
//        }

//        AAdvancedPainter.paintSideMessage("Focus: x:" + focusPoint.x() + ", y:" + focusPoint.y(), Color.Yellow);
//    }

}
