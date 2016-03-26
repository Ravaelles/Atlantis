package bwapi;

import bwapi.Position;

/**
 * Interrmediate class used to translate getPoint() calls to getCenter() calls.
 */
public abstract class CenteredObject extends AbstractPoint<Position> {

    public Position getPoint(){
        return getCenter();
    }

    public abstract Position getCenter();
}