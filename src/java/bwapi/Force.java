package bwapi;

import bwapi.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

/**
The Force class is used to get information about each force in a match. Normally this is considered a team. Note It is not called a team because players on the same force do not necessarily need to be allied at the beginning of a match.
*/
public class Force {

/**
Retrieves the unique ID that represents this Force. Returns An integer containing the ID for the Force.
*/
    public int getID() {
        return getID_native(pointer);
    }

/**
Retrieves the name of the Force. Returns A std::string object containing the name of the force. Example usage: BWAPI::Force myForce = BWAPI::Broodwar->self()->getForce(); if ( myForce->getName() == "Observers" ) BWAPI::Broodwar << "Looks like we're observing a match." << std::endl; Note Don't forget to use std::string::c_str() when passing this parameter to Game::sendText and other variadic functions.
*/
    public String getName() {
        return getName_native(pointer);
    }

/**
Retrieves the set of players that belong to this Force. Returns A Playerset object containing the players that are part of this Force. Example usage: // Get the enemy force, but make sure we have an enemy BWAPI::Force myEnemyForce = BWAPI::Broodwar->enemy() ? BWAPI::Broodwar->enemy()->getForce() : nullptr; if ( myEnemyForce != nullptr ) { Broodwar << "The allies of my enemy are..." << std::endl; for ( auto i = myEnemyForce.begin(); i != myEnemyForce.end(); ++i ) Broodwar << " - " << i->getName() << std::endl; }
*/
    public List<Player> getPlayers() {
        return getPlayers_native(pointer);
    }


    private static Map<Long, Force> instances = new HashMap<Long, Force>();

    private Force(long pointer) {
        this.pointer = pointer;
    }

    private static Force get(long pointer) {
        if (pointer == 0 ) {
            return null;
        }
        Force instance = instances.get(pointer);
        if (instance == null ) {
            instance = new Force(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;

    private native int getID_native(long pointer);

    private native String getName_native(long pointer);

    private native List<Player> getPlayers_native(long pointer);


}
