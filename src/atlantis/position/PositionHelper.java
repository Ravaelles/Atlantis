package atlantis.position;

import atlantis.units.AUnit;
import atlantis.units.Units;
import bwapi.Point;
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
public class PositionHelper {
    
    // === Median ========================================
    /**
     * Returns median PX and median PY for all passed units.
     */
    public static APosition medianPosition(Collection<AUnit> units) {
        if (units.isEmpty()) {
            return null;
        }

        ArrayList<Integer> xCoordinates = new ArrayList<>();
        ArrayList<Integer> yCoordinates = new ArrayList<>();
        for (AUnit unit : units) {
            xCoordinates.add(unit.getPosition().getX());	//TODO: check whether position is in Pixels
            yCoordinates.add(unit.getPosition().getX());
        }
        Collections.sort(xCoordinates);
        Collections.sort(yCoordinates);

        return new APosition(
                xCoordinates.get(xCoordinates.size() / 2),
                yCoordinates.get(yCoordinates.size() / 2)
        );
    }
    
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
    
    public static APosition getPositionAverage(Units units) {
        int totalX = 0;
        int totalY = 0;
        
        for (Iterator<AUnit> iterator = units.iterator(); iterator.hasNext();) {
            AUnit unit = iterator.next();
            totalX += unit.getX();
            totalY += unit.getY();
        }

        return new APosition(totalX / units.size(), totalY / units.size());
    }
    
    public static APosition getPositionAverageDistanceWeightedTo(AUnit unit, Units units, double power) {
        int totalX = 0;
        int totalY = 0;
        double totalFactor = 0;
        
        for (Iterator<AUnit> iterator = units.iterator(); iterator.hasNext();) {
            AUnit otherUnit = iterator.next();
            double distanceToUnit = unit.distanceTo(otherUnit);
            double factor = Math.pow(distanceToUnit, power);
            totalFactor += factor;
            totalX += otherUnit.getX() * factor;
            totalY += otherUnit.getY() * factor;
        }

        return new APosition((int) (totalX / totalFactor), (int) (totalY / totalFactor));
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
    
    public static APosition getPositionMovedPercentTowards(HasPosition from, HasPosition movedToward, double percent) {
        int finalX = (int) ((100 - percent) * from.getX() + percent * movedToward.getX()) / 100;
        int finalY = (int) ((100 - percent) * from.getY() + percent * movedToward.getY()) / 100;
        return new APosition(finalX, finalY);
    }
    
    public static APosition getPositionMovedTilesTowards(APosition from, Point<Position> to, double tiles) {
        int dirX = to.getX() - from.getX();
        int dirY = to.getY() - from.getY();
        
//        double hyp = Math.sqrt(dirX * dirX + dirY * dirY);
        double hyp = tiles * 32;
        dirX /= hyp;
        dirY /= hyp;
        
        int finalX = from.getX() + dirX;
        int finalY = from.getY() + dirY;
        
        return new APosition(finalX, finalY);
    }

    // === Translate position ============================
    
    /**
     * Returns a <b>new</b> Position that represents the effect of moving this position by 
     * [deltaTileX, deltaTileY].
     */
    public static APosition translateByTiles(APosition position, int deltaTileX, int deltaTileY) {
        return new APosition(position.getX() + deltaTileX * 32, position.getY() + deltaTileY * 32);
    }
    
    /**
     * Returns a <b>new</b> Position that represents the effect of moving this position by [deltaX, deltaY].
     */
    public static APosition translateByPixels(APosition position, int deltaPixelX, int deltaPixelY) {
        return new APosition(position.getX() + deltaPixelX, position.getY() + deltaPixelY);
    }
    
    // === Other method ==================================

    /**
     * Returns average PX and average PY for all passed units.
     */
    public static APosition averagePosition(Collection<AUnit> units) {
        if (units.isEmpty()) {
            return null;
        }

        int totalX = 0;
        int totalY = 0;
        for (AUnit unit : units) {
            totalX += unit.getPosition().getX();
            totalY += unit.getPosition().getY();
        }
        return new APosition(
            totalX / units.size(),
            totalY / units.size()
        );
    }
    
}
