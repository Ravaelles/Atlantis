package bwapi;

import bwapi.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

public class Client {

    public boolean isConnected() {
        return isConnected_native(pointer);
    }

    public boolean connect() {
        return connect_native(pointer);
    }

    public void disconnect() {
        disconnect_native(pointer);
    }

    public void update() {
        update_native(pointer);
    }


    private static Map<Long, Client> instances = new HashMap<Long, Client>();

    private Client(long pointer) {
        this.pointer = pointer;
    }

    private static Client get(long pointer) {
        if (pointer == 0 ) {
            return null;
        }
        Client instance = instances.get(pointer);
        if (instance == null ) {
            instance = new Client(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;

    private native boolean isConnected_native(long pointer);

    private native boolean connect_native(long pointer);

    private native void disconnect_native(long pointer);

    private native void update_native(long pointer);


}
