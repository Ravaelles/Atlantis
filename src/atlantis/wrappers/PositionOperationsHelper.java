package atlantis.wrappers;

import atlantis.units.AUnit;
import atlantis.units.Units;
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
    
}
