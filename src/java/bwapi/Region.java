package bwapi;

import bwapi.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;
import bwapi.CenteredObject;

/**
Region objects are created by Starcraft: Broodwar to contain several tiles with the same properties, and create a node in pathfinding and other algorithms. Regions may not contain detailed information, but have a sufficient amount of data to identify general chokepoints, accessibility to neighboring terrain, be used in general pathing algorithms, and used as nodes to rally units to. Most parameters that are available are explicitly assigned by Broodwar itself. See also Game::getAllRegions, Game::getRegionAt, UnitInterface::getRegion
*/
public class Region extends CenteredObject 
{

/**
Retrieves a unique identifier for this region. Note This identifier is explicitly assigned by Broodwar. Returns An integer that represents this region. See also Game::getRegion
*/
    public int getID() {
        return getID_native(pointer);
    }

/**
Retrieves a unique identifier for a group of regions that are all connected and accessible by each other. That is, all accessible regions will have the same group ID. This function is generally used to check if a path is available between two points in constant time. Note This identifier is explicitly assigned by Broodwar. Returns An integer that represents the group of regions that this one is attached to.
*/
    public int getRegionGroupID() {
        return getRegionGroupID_native(pointer);
    }

/**
Retrieves the center of the region. This position is used as the node of the region. Returns A Position indicating the center location of the Region, in pixels.
*/
    public Position getCenter() {
        return getCenter_native(pointer);
    }

/**
Checks if this region is part of higher ground. Higher ground may be used in strategic placement of units and structures. Returns true if this region is part of strategic higher ground, and false otherwise.
*/
    public boolean isHigherGround() {
        return isHigherGround_native(pointer);
    }

/**
Retrieves a value that represents the strategic advantage of this region relative to other regions. A value of 2 may indicate a possible choke point, and a value of 3 indicates a signficant strategic position. Note This value is explicitly assigned by Broodwar. Returns An integer indicating this region's strategic potential.
*/
    public int getDefensePriority() {
        return getDefensePriority_native(pointer);
    }

/**
Retrieves the state of accessibility of the region. The region is considered accessible if it can be accessed by ground units. Returns true if ground units can traverse this region, and false if the tiles in this region are inaccessible or unwalkable.
*/
    public boolean isAccessible() {
        return isAccessible_native(pointer);
    }

/**
Retrieves the set of neighbor Regions that this one is connected to. Returns A reference to a Regionset containing the neighboring Regions.
*/
    public List<Region> getNeighbors() {
        return getNeighbors_native(pointer);
    }

/**
Retrieves the approximate left boundary of the region. Returns The x coordinate, in pixels, of the approximate left boundary of the region.
*/
    public int getBoundsLeft() {
        return getBoundsLeft_native(pointer);
    }

/**
Retrieves the approximate top boundary of the region. Returns The y coordinate, in pixels, of the approximate top boundary of the region.
*/
    public int getBoundsTop() {
        return getBoundsTop_native(pointer);
    }

/**
Retrieves the approximate right boundary of the region. Returns The x coordinate, in pixels, of the approximate right boundary of the region.
*/
    public int getBoundsRight() {
        return getBoundsRight_native(pointer);
    }

/**
Retrieves the approximate bottom boundary of the region. Returns The y coordinate, in pixels, of the approximate bottom boundary of the region.
*/
    public int getBoundsBottom() {
        return getBoundsBottom_native(pointer);
    }

/**
Retrieves the closest accessible neighbor region. Returns The closest Region that is accessible.
*/
    public Region getClosestAccessibleRegion() {
        return getClosestAccessibleRegion_native(pointer);
    }

/**
Retrieves the closest inaccessible neighbor region. Returns The closest Region that is inaccessible.
*/
    public Region getClosestInaccessibleRegion() {
        return getClosestInaccessibleRegion_native(pointer);
    }

/**
Retrieves the center-to-center distance between two regions. Parameters other The target Region to calculate distance to. Returns The integer distance from this Region to other.
*/
    public int getDistance(Region other) {
        return getDistance_native(pointer, other);
    }

/**
Retrieves a Unitset containing all the units that are in this region. Also has the ability to filter the units before the creation of the Unitset. Parameters pred (optional) If this parameter is used, it is a UnitFilter or function predicate that will retrieve only the units whose attributes match the given criteria. If omitted, then a default value of nullptr is used, in which case there is no filter. Returns A Unitset containing all units in this region that have met the requirements of pred. See also UnitFilter
*/
    public List<Unit> getUnits() {
        return getUnits_native(pointer);
    }


    private static Map<Long, Region> instances = new HashMap<Long, Region>();

    private Region(long pointer) {
        this.pointer = pointer;
    }

    private static Region get(long pointer) {
        if (pointer == 0 ) {
            return null;
        }
        Region instance = instances.get(pointer);
        if (instance == null ) {
            instance = new Region(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;

    private native int getID_native(long pointer);

    private native int getRegionGroupID_native(long pointer);

    private native Position getCenter_native(long pointer);

    private native boolean isHigherGround_native(long pointer);

    private native int getDefensePriority_native(long pointer);

    private native boolean isAccessible_native(long pointer);

    private native List<Region> getNeighbors_native(long pointer);

    private native int getBoundsLeft_native(long pointer);

    private native int getBoundsTop_native(long pointer);

    private native int getBoundsRight_native(long pointer);

    private native int getBoundsBottom_native(long pointer);

    private native Region getClosestAccessibleRegion_native(long pointer);

    private native Region getClosestInaccessibleRegion_native(long pointer);

    private native int getDistance_native(long pointer, Region other);

    private native List<Unit> getUnits_native(long pointer);


}
