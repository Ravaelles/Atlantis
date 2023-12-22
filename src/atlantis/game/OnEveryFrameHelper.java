package atlantis.game;

import atlantis.debug.painter.AAdvancedPainter;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.path.OurClosestBaseToEnemy;
import atlantis.map.path.PathToEnemyBase;
import atlantis.map.position.APosition;

import atlantis.map.region.ARegion;
import atlantis.map.region.MainRegion;
import atlantis.map.wall.GetWallIn;
import atlantis.map.wall.Structure;
import atlantis.production.constructing.position.terran.SupplyDepotPositionFinder;
import atlantis.production.constructing.position.terran.TerranPositionFinder;
import atlantis.production.orders.production.queue.Queue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.interrupt.DontInterruptShootingUnits;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
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
    }

    private static void printFirstCombatUnitStatus() {
        if (A.now() <= 21) return;

        AUnit unit = Select.ourCombatUnits().first();
        if (unit == null) return;

        double enemyDist = unit.nearestEnemyDist();
        System.err.println("@ " + A.now() + ": " + unit.manager()
            + " \\ dist: " + A.digit(enemyDist)
            + " \\ cool:" + unit.cooldown());

//        unit.setTooltip("Cooldown:" + unit.cooldown());

        if (unit.isAttacking())  {
            AAdvancedPainter.paintCircle(unit, 12, Color.Orange);
            AAdvancedPainter.paintCircle(unit, 11, Color.Orange);
            AAdvancedPainter.paintCircle(unit, 8, Color.Orange);
            AAdvancedPainter.paintCircle(unit, 7, Color.Orange);
        }

        if ((new DontInterruptShootingUnits(unit)).applies())  {
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
