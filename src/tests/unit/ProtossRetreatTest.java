package tests.unit;

import atlantis.combat.retreating.protoss.should.ProtossShouldRetreat;
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
        FakeUnit our;
//        FakeUnit friend1, friend2;
        FakeUnit[] ours = fakeOurs(
            fake(AUnitType.Protoss_Zealot, 8),
            fake(AUnitType.Protoss_Zealot, 8.1),
            our = fake(AUnitType.Protoss_Zealot, 8.2),
            fake(AUnitType.Protoss_Zealot, 9.1)
        );

        FakeUnit[] enemies = fakeEnemies(
            fake(AUnitType.Protoss_Zealot, 10),
            fake(AUnitType.Protoss_Zealot, 10.1),
            fake(AUnitType.Protoss_Zealot, 10.2)
        );

        usingFakeOursAndFakeEnemies(ours, enemies, () -> {
            assertEquals(false, (new ProtossSmallScaleRetreat(our)).applies());

//            Selection friends = our.friendsNear();
////            Selection enemies = our.friendsNear();
//
//            assertEquals(false, ProtossSmallScaleRetreat.shouldSmallScaleRetreat(our, friends, enemies));
        });
    }
}
