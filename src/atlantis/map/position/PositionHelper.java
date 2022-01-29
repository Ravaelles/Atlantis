package atlantis.map.position;

import atlantis.units.AUnit;
import atlantis.units.Units;

import java.util.*;


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
            xCoordinates.add(unit.position().getX());	//TODO: check whether position is in Pixels
            yCoordinates.add(unit.position().getX());
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
            xCoordinates.add(unit.x());
            yCoordinates.add(unit.y());
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
            totalX += unit.x();
            totalY += unit.y();
        }

        return new APosition(totalX / units.size(), totalY / units.size());
    }
    
    public static APosition getPositionAverageDistanceWeightedTo(AUnit unit, Units units, double power) {
        int totalX = 0;
        int totalY = 0;
        double totalFactor = 0;
        
        for (Iterator<AUnit> iterator = units.iterator(); iterator.hasNext();) {
            AUnit otherUnit = iterator.next();
            double distanceToUnit = unit.distTo(otherUnit);
            double factor = Math.pow(distanceToUnit, power);
            totalFactor += factor;
            totalX += otherUnit.x() * factor;
            totalY += otherUnit.y() * factor;
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
        int finalX = (int) ((100 - percent) * from.x() + percent * movedToward.x()) / 100;
        int finalY = (int) ((100 - percent) * from.y() + percent * movedToward.y()) / 100;
        return new APosition(finalX, finalY);
    }
    
    public static APosition getPositionMovedTilesTowards(HasPosition from, HasPosition to, double tiles) {
        double modifier = tiles / from.distTo(to);
        int dirX = (int) ((to.x() - from.x()) * modifier);
        int dirY = (int) ((to.y() - from.y()) * modifier);

//        double hyp = Math.sqrt(dirX * dirX + dirY * dirY);

        return new APosition(from.x() + dirX, from.y() + dirY);
    }

    // === Translate position ============================
    
    /**
     * Returns a <b>new</b> Position that represents the effect of moving this position by 
     * [deltaTileX, deltaTileY].
     */
//    public static APosition translateByTiles(APosition position, int deltaTileX, int deltaTileY) {
//        return new APosition(position.getX() + deltaTileX * 32, position.getY() + deltaTileY * 32);
//    }
//
//    /**
//     * Returns a <b>new</b> Position that represents the effect of moving this position by [deltaX, deltaY].
//     */
//    public static APosition translateByPixels(HasPosition position, int deltaPixelX, int deltaPixelY) {
//        return new APosition(position.x() + deltaPixelX, position.y() + deltaPixelY);
//    }
    
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
            totalX += unit.position().getX();
            totalY += unit.position().getY();
        }
        return new APosition(
            totalX / units.size(),
            totalY / units.size()
        );
    }
    
}
