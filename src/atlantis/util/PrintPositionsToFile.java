package atlantis.util;

import atlantis.game.A;
import atlantis.map.base.ABaseLocation;
import atlantis.map.position.HasPosition;

import java.util.List;

public class PrintPositionsToFile {
    public static void printToFile(String file, List<? extends HasPosition> positions, HasPosition highlightThisOne) {
        A.saveToFile(file, string(positions, highlightThisOne), true);
    }

    private static String string(List<? extends HasPosition> positions, HasPosition highlightThisOne) {
        StringBuilder builder = new StringBuilder();

        int txMax = positions.stream().mapToInt(HasPosition::tx).max().orElse(0);
        int tyMax = positions.stream().mapToInt(HasPosition::ty).max().orElse(0);

        for (int ty = 1; ty <= tyMax; ty++) {
            for (int tx = 1; tx <= txMax; tx++) {
                HasPosition position = getPositionHereIfPresent((List<HasPosition>) positions, tx, ty);

                builder.append(
                    position != null ? positionToString(position, highlightThisOne) : "  "
                );
            }
            builder.append("\n");
        }

        return builder.toString();
    }

    private static String positionToString(HasPosition position, HasPosition highlightThisOne) {
        String extra = "";

        if (position instanceof ABaseLocation) {
            ABaseLocation baseLocation = (ABaseLocation) position;
            extra += baseLocation.isStartLocation() ? "(S)" : "";
        }

        if (position.equals(highlightThisOne)) {
            extra += "<<<";
        }

//        return "[" + position.tx() + "," + position.ty() + "]";
        return position.tx() + "," + position.ty() + extra;
    }

    private static HasPosition getPositionHereIfPresent(List<HasPosition> positions, int tx, int ty) {
        for (HasPosition position : positions) {
            if (position.tx() == tx && position.ty() == ty) {
                return position;
            }
        }
        return null;
    }
}
