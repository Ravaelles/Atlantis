package tests.acceptance;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.production.constructions.position.protoss.ProtossForbiddenByStreetGrid;
import atlantis.units.AUnitType;
import atlantis.util.AConsole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProtossStreetGridTest extends WorldStubForTests {
    @Test
    public void testGatewayAndPylonPlacement() {
        world(1, () -> {
        int xRadius = 20;
        int yRadius = 20;

        for (int ty = 1; ty < yRadius * 2; ty++) {
            for (int tx = 1; tx < xRadius * 2; tx++) {
                APosition position = APosition.create(tx, ty);

                boolean isOkForPylon = !ProtossForbiddenByStreetGrid.isForbiddenByStreetGrid(
                    null, AUnitType.Protoss_Pylon, position
                );

                //                if (isOkForPylon) AConsole.print("█");
                //                else {
                //                    boolean isOkForGateway = !ProtossForbiddenByStreetGrid.isForbiddenByStreetGrid(
                //                        null, AUnitType.Protoss_Gateway, position
                //                    );
                //                    if (isOkForGateway) AConsole.print("░"); // ░ ▒
                //                    else AConsole.print("◦");
                //                }
            }
            AConsole.println();
        }
        });
    }

    @Test
    public void testGatewayPlacement() {
        int xRadius = 20;
        int yRadius = 20;

        for (int ty = 1; ty < yRadius * 2; ty++) {
            for (int tx = 1; tx < xRadius * 2; tx++) {
                APosition position = APosition.create(tx, ty);
                String string = gatewayPositionToString(position);

                AConsole.print(string);
            }
            AConsole.println();
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

                AConsole.print(string);
            }
            AConsole.println();
        }
    }

    @Test
    public void testBothPylonAndGatewayPlacement() {
        int xRadius = 20;
        int yRadius = 20;

        for (int ty = 1; ty < yRadius * 2; ty++) {
            for (int tx = 1; tx < xRadius * 2; tx++) {
                APosition position = APosition.create(tx, ty);
                String string = pylonAndGatewayPositionToString(position);

                AConsole.print(string);
            }
            AConsole.println();
        }
    }

    private String pylonAndGatewayPositionToString(APosition position) {
        if (true) return "";

        boolean isGatewayOk = !ProtossForbiddenByStreetGrid.isForbiddenByStreetGrid(
            null, AUnitType.Protoss_Gateway, position
        );
        boolean isPylonOk = !ProtossForbiddenByStreetGrid.isForbiddenByStreetGrid(
            null, AUnitType.Protoss_Pylon, position
        );

        String string = "_";

        if (!isGatewayOk && !isPylonOk) {
            string = "◦";
        }
        else if (isGatewayOk && !isPylonOk) {
            string = "▒";
        }
        else if (!isGatewayOk && isPylonOk) {
            string = "█";
        }
        else if (isGatewayOk && isPylonOk) {
            string = "X";
        }

        return string;
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
