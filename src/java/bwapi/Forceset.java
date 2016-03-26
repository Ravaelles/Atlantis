package bwapi;

import bwapi.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

/**
A container that holds a group of Forces. See also BWAPI::Force
*/
public class Forceset {

/**
Retrieves the set of players that belong to this Force. Returns A Playerset object containing the players that are part of this Force. Example usage: // Get the enemy force, but make sure we have an enemy BWAPI::Force myEnemyForce = BWAPI::Broodwar->enemy() ? BWAPI::Broodwar->enemy()->getForce() : nullptr; if ( myEnemyForce != nullptr ) { Broodwar << "The allies of my enemy are..." << std::endl; for ( auto i = myEnemyForce.begin(); i != myEnemyForce.end(); ++i ) Broodwar << " - " << i->getName() << std::endl; }
*/
    public List<Player> getPlayers() {
        return getPlayers_native(pointer);
    }


    private static Map<Long, Forceset> instances = new HashMap<Long, Forceset>();

    private Forceset(long pointer) {
        this.pointer = pointer;
    }

    private static Forceset get(long pointer) {
        if (pointer == 0 ) {
            return null;
        }
        Forceset instance = instances.get(pointer);
        if (instance == null ) {
            instance = new Forceset(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;

    private native List<Player> getPlayers_native(long pointer);


}
