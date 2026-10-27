package tests.unit;

import atlantis.combat.cluster.FirstDragoonGate;
import atlantis.production.orders.build.ABuildOrder;
import atlantis.production.orders.build.CurrentBuildOrder;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * First-Dragoon gate (owner order 2026-10-26, vs Zerg on a
 * Dragoon-based build): the line defending home stays at the main base
 * until the first Dragoon is finished - the Zealot phase of a dragoon
 * build is when the march used to step out and uncover the miners.
 *
 * <p>Three things must stay independent: the matchup (Zerg only), the
 * build (a Zealot build must still stage out), and the army (one
 * finished Dragoon opens the gate for good). The build's intent itself
 * is read from the active build order rows, which is the game root's
 * job - combat holds only the flag.</p>
 */
@Tag("combat")
public class FirstDragoonGateTest {

    private static ABuildOrder buildOrderPlanning(ProductionOrder... rows) {
        ABuildOrder buildOrder = Mockito.mock(ABuildOrder.class);
        Mockito.when(buildOrder.productionOrders())
            .thenReturn(new ArrayList<>(Arrays.asList(rows)));

        return buildOrder;
    }

    @Test
    public void zergDragoonBuildWithNoDragoonYetHoldsTheMain() {
        assertTrue(
            FirstDragoonGate.holds(true, 0, true),
            "owner: vs Zerg on a dragoon build, no Dragoon yet = hold the main"
        );
    }

    @Test
    public void oneFinishedDragoonOpensTheGate() {
        assertFalse(
            FirstDragoonGate.holds(true, 1, true),
            "owner: the first Dragoon is exactly when the line may leave the main"
        );
    }

    @Test
    public void aBuildThatNeverTrainsDragoonsIsNotGated() {
        assertFalse(
            FirstDragoonGate.holds(true, 0, false),
            "a Zealot build must keep staging out as before - only dragoon builds gate"
        );
    }

    @Test
    public void onlyZergIsGated() {
        assertFalse(
            FirstDragoonGate.holds(false, 0, true),
            "the owner scoped the order to Zerg, other matchups keep today's point"
        );
    }

    @Test
    public void aFinishedDragoonAndAZealotBuildTogetherStillMarchOut() {
        assertFalse(
            FirstDragoonGate.holds(true, 3, false),
            "two independent conditions, each enough to keep the gate open"
        );
    }

    @Test
    public void everyFreshGameStartsWithAnUngatedBuild() {
        FirstDragoonGate.setBuildPlansDragoons(true);
        assertTrue(FirstDragoonGate.buildPlansDragoons(), "the game root writes it");

        FirstDragoonGate.clearForTests();
        assertFalse(
            FirstDragoonGate.buildPlansDragoons(),
            "a stale flag would hold a Zealot build at home in the next game"
        );
    }

    @Test
    public void aBuildOrderRowForADragoonMarksTheBuild() {
        ABuildOrder previous = CurrentBuildOrder.get();

        try {
            CurrentBuildOrder.set(buildOrderPlanning(
                new ProductionOrder(AUnitType.Protoss_Dragoon, 20)));
            assertTrue(
                CurrentBuildOrder.plansDragoon(),
                "a build that trains a Dragoon is a dragoon build"
            );

            CurrentBuildOrder.set(buildOrderPlanning(
                new ProductionOrder(AUnitType.Protoss_Zealot, 12),
                new ProductionOrder(AUnitType.Protoss_Cybernetics_Core, 14)));
            assertFalse(
                CurrentBuildOrder.plansDragoon(),
                "no Dragoon row = not a dragoon build, the gate must stay open"
            );
        } finally {
            if (previous != null) CurrentBuildOrder.set(previous);
            else CurrentBuildOrder.clear();
        }
    }

    @Test
    public void anUnknownBuildIsNeverGated() {
        ABuildOrder previous = CurrentBuildOrder.get();

        try {
            CurrentBuildOrder.clear();
            assertFalse(
                CurrentBuildOrder.plansDragoon(),
                "no build loaded answers false - an unknown build holds nothing back"
            );
        } finally {
            if (previous != null) CurrentBuildOrder.set(previous);
        }
    }
}
