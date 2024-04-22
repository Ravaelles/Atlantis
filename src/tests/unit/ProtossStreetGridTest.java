package tests.unit;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.production.constructing.position.protoss.ProtossForbiddenByStreetGrid;
import atlantis.units.AUnitType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ProtossStreetGridTest extends AbstractTestWithUnits {
    @Test
    public void testGatewayAndPylonPlacement() {
        int xRadius = 20;
        int yRadius = 20;

        for (int ty = 1; ty < yRadius * 2; ty++) {
            for (int tx = 1; tx < xRadius * 2; tx++) {
                APosition position = APosition.create(tx, ty);

                boolean isOkForPylon = !ProtossForbiddenByStreetGrid.isForbiddenByStreetGrid(
                    null, AUnitType.Protoss_Pylon, position
                );

//                if (isOkForPylon) A.print("█");
//                else {
//                    boolean isOkForGateway = !ProtossForbiddenByStreetGrid.isForbiddenByStreetGrid(
//                        null, AUnitType.Protoss_Gateway, position
//                    );
//                    if (isOkForGateway) A.print("░"); // ░ ▒
//                    else A.print("◦");
//                }
            }
            A.println();
        }
    }

    @Test
    public void testGatewayPlacement() {
        int xRadius = 20;
        int yRadius = 20;

        for (int ty = 1; ty < yRadius * 2; ty++) {
            for (int tx = 1; tx < xRadius * 2; tx++) {
                APosition position = APosition.create(tx, ty);
                String string = gatewayPositionToString(position);

//                A.print(string);
            }
//            A.println();
        }
    }

    @Test
    public void testPylonPlacement() {
        int xRadius = 20;
        int yRadius = 20;

        for (int ty = 1; ty < yRadius * 2; ty++) {
            for (int tx = 1; tx < xRadius * 2; tx++) {
                APosition position = APosition.create(tx, ty);
                String string = pylonPositionToString(position);

//                A.print(string);
            }
//            A.println();
        }
    }

    private static String gatewayPositionToString(APosition position) {
        boolean isOk = !ProtossForbiddenByStreetGrid.isForbiddenByStreetGrid(
            null, AUnitType.Protoss_Gateway, position
        );
        String string = isOk ? "▒" : "◦"; //"▓"

        return string;
    }

    private static String pylonPositionToString(APosition position) {
        boolean isOk = !ProtossForbiddenByStreetGrid.isForbiddenByStreetGrid(
            null, AUnitType.Protoss_Pylon, position
        );
        String string = isOk ? "█" : "◦";

        return string;
    }
}
