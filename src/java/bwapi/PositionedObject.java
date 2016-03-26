package bwapi;

import bwapi.Position;

/**
 * Interrmediate class used to translate getPoint() calls to getPosition() calls.
 */
//public abstract class PositionedObject extends AbstractPoint<Position> {
public abstract class PositionedObject extends Position {

    public Position getPoint(){
        return getPosition();
    }

    public abstract Position getPosition();
    
}