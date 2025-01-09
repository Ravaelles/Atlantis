package tests.unit.retreat;

import atlantis.combat.retreating.protoss.small_scale.ProtossMeleeSmallScaleRetreat;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;
import tests.unit.AbstractTestWithUnits;

import static org.junit.jupiter.api.Assertions.*;

public class ProtossSmallRetreatTest extends AbstractTestWithUnits {
    @Test
    public void retreatWhenNoMeleeAdvantage() {
        FakeUnit our;
//        FakeUnit friend1, friend2;
        FakeUnit[] ours = fakeOurs(
            fake(AUnitType.Protoss_Zealot, 8),
            fake(AUnitType.Protoss_Zealot, 8.1),
            our = fake(AUnitType.Protoss_Zealot, 8.2)
        );

        FakeUnit[] enemies = fakeEnemies(
            fake(AUnitType.Protoss_Zealot, 10),
            fake(AUnitType.Protoss_Zealot, 10.1),
            fake(AUnitType.Protoss_Zealot, 10.2)
        );

        usingFakeOursAndFakeEnemies(ours, enemies, () -> {
//            Selection friends = our.friendsNear();
//            Selection enemies = our.friendsNear();

//            ProtossMeleeSmallScaleRetreat.shouldSmallScaleRetreat(our, friends, enemies);

            assertEquals(true, (new ProtossMeleeSmallScaleRetreat(our)).applies());
        });
    }

    @Test
    public void noRetreatWhenMeleeAdvantage() {
        FakeUnit zealotA, zealotB, zealotC;
//        FakeUnit friend1, friend2;
        FakeUnit[] ours = fakeOurs(
            zealotC = fake(AUnitType.Protoss_Zealot, 7),
            fake(AUnitType.Protoss_Zealot, 7.1),
            zealotB = fake(AUnitType.Protoss_Zealot, 8.1),
            fake(AUnitType.Protoss_Zealot, 8.2),
            zealotA = fake(AUnitType.Protoss_Zealot, 9.9)
        );

        FakeUnit[] enemies = fakeEnemies(
            fake(AUnitType.Protoss_Zealot, 10),
            fake(AUnitType.Protoss_Zealot, 10.1),
            fake(AUnitType.Protoss_Zealot, 10.2)
        );

        usingFakeOursAndFakeEnemies(ours, enemies, () -> {
            boolean shouldSmallScaleRetreatA = (new ProtossMeleeSmallScaleRetreat(zealotA)).shouldSmallScaleRetreat();
            System.out.println("shouldSmallScaleRetreatA = " + shouldSmallScaleRetreatA);
            System.out.println("smallScaleEval = " + zealotA.smallScaleEval());
            System.out.println("Logs");
            zealotA.log().print();
            System.out.println("ManagerLogs");
            zealotA.managerLogs().print();

//            System.out.println("-------- B");
            boolean shouldSmallScaleRetreatB = (new ProtossMeleeSmallScaleRetreat(zealotB)).shouldSmallScaleRetreat();
//            System.out.println("shouldSmallScaleRetreatB = " + shouldSmallScaleRetreatB);
//            System.out.println("smallScaleEval = " + zealotB.smallScaleEval());
//
//            System.out.println("-------- C");
            boolean shouldSmallScaleRetreatC = (new ProtossMeleeSmallScaleRetreat(zealotB)).shouldSmallScaleRetreat();
//            System.out.println("shouldSmallScaleRetreatC = " + shouldSmallScaleRetreatC);
//            System.out.println("smallScaleEval = " + zealotC.smallScaleEval());

            assertEquals(true, shouldSmallScaleRetreatA);
            assertEquals(false, shouldSmallScaleRetreatB);
            assertEquals(false, shouldSmallScaleRetreatC);

//            Selection friends = zealotA.friendsNear();
////            Selection enemies = zealotA.friendsNear();
//
//            assertEquals(false, ProtossMeleeSmallScaleRetreat.shouldSmallScaleRetreatA(zealotA, friends, enemies));
        });
    }
}
