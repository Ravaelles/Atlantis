package tests.unit;

import atlantis.combat.retreating.protoss.should.ProtossShouldRetreat;
import atlantis.combat.retreating.protoss.small_scale.ProtossSmallScaleEvaluate;
import atlantis.combat.retreating.protoss.small_scale.ProtossSmallScaleRetreat;
import atlantis.game.AGame;
import atlantis.units.AUnitType;
import org.junit.Test;
import org.mockito.MockedStatic;

import static org.junit.Assert.assertEquals;

public class ProtossRetreatTest extends AbstractTestWithUnits {
    public MockedStatic<AGame> aGame;

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

//            ProtossSmallScaleRetreat.shouldSmallScaleRetreat(our, friends, enemies);

            assertEquals(true, (new ProtossSmallScaleRetreat(our)).applies());
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
            boolean shouldSmallScaleRetreatA = (new ProtossSmallScaleRetreat(zealotA)).shouldSmallScaleRetreat();
//            System.out.println("shouldSmallScaleRetreatA = " + shouldSmallScaleRetreatA);
//            System.out.println("smallScaleEval = " + zealotA.smallScaleEval());

//            System.out.println("-------- B");
            boolean shouldSmallScaleRetreatB = (new ProtossSmallScaleRetreat(zealotB)).shouldSmallScaleRetreat();
//            System.out.println("shouldSmallScaleRetreatB = " + shouldSmallScaleRetreatB);
//            System.out.println("smallScaleEval = " + zealotB.smallScaleEval());
//
//            System.out.println("-------- C");
            boolean shouldSmallScaleRetreatC = (new ProtossSmallScaleRetreat(zealotB)).shouldSmallScaleRetreat();
//            System.out.println("shouldSmallScaleRetreatC = " + shouldSmallScaleRetreatC);
//            System.out.println("smallScaleEval = " + zealotC.smallScaleEval());

            assertEquals(true, shouldSmallScaleRetreatA);
            assertEquals(false, shouldSmallScaleRetreatB);
            assertEquals(false, shouldSmallScaleRetreatC);

//            Selection friends = zealotA.friendsNear();
////            Selection enemies = zealotA.friendsNear();
//
//            assertEquals(false, ProtossSmallScaleRetreat.shouldSmallScaleRetreatA(zealotA, friends, enemies));
        });
    }
}
