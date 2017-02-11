package atlantis.wrappers;

import atlantis.units.AUnit;
import atlantis.units.Units;
import bwapi.AbstractPoint;
import bwapi.Position;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class PositionOperationsHelper {
    
    // === Median ========================================
    
    public static APosition getPositionMedian(Units units) {
        ArrayList<Integer> xCoordinates = new ArrayList<>();
        ArrayList<Integer> yCoordinates = new ArrayList<>();
        
        for (Iterator<AUnit> iterator = units.iterator(); iterator.hasNext();) {
            AUnit unit = iterator.next();
            xCoordinates.add(unit.getX());
            yCoordinates.add(unit.getY());
        }

        return new APosition(
                getPositionMedian(xCoordinates),
                getPositionMedian(yCoordinates)
        );
    }
    
    public static int getPositionMedian(List<Integer> collection) {
        Collections.sort(collection);
        return collection.get(collection.size() / 2);
    }
    
    public static APosition getPositionMedian(Collection<APosition> positions) {
        ArrayList<Integer> xCoordinates = new ArrayList<>();
        ArrayList<Integer> yCoordinates = new ArrayList<>();

        for (APosition position : positions) {
            xCoordinates.add(position.getX());
            yCoordinates.add(position.getY());
        }

        return new APosition(
                getPositionMedian(xCoordinates),
                getPositionMedian(yCoordinates)
        );
    }
    
    // === Position between A and B ======================
    
    public static APosition getPositionMovedPercentTowards(Position from, AbstractPoint<Position> movedToward, double percent) {
        int finalX = (int) ((100 - percent) * from.getX() + percent * movedToward.getX()) / 100;
        int finalY = (int) ((100 - percent) * from.getY() + percent * movedToward.getY()) / 100;
        return new APosition(finalX, finalY);
    }
    
}
