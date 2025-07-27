# CherryVis Integration for Java StarCraft Bot

This package provides Java 8 integration with the CherryVis replay enhancer for StarCraft: Brood War bots. CherryVis allows adding logs to specific units and events that can be viewed when watching replays.

## Overview

The CherryVis system creates additional files alongside regular replay files that contain logs and data about specific units and events during a game. This information can then be viewed in the CherryVis replay viewer.

## Usage

### Initialization

```java
// Initialize CherryVis with the directory to store enhancement files
CherryVis.initialize("replays/cherryvis_data");
```

### Frame Updates

You need to update the current frame on each game frame:

```java
// In your game loop
CherryVis.setFrame(bwapi.getFrameCount());
```

### Logging Unit Data

```java
// Log various unit data types
CherryVis.logUnit(unit.id(), "position_x", unit.getPosition().getX());
CherryVis.logUnit(unit.id(), "position_y", unit.getPosition().getY());
CherryVis.logUnit(unit.id(), "health", unit.getHitPoints());
CherryVis.logUnit(unit.id(), "target", targetUnitId);
CherryVis.logUnit(unit.id(), "isAttacking", unit.isAttacking());
```

### Logging Events

```java
// Log an event with JSON data
CherryVis.log("UnitCreated", "{\"unitId\": " + unit.id() + ", \"type\": \"" + unit.getType() + "\"}" );
```

### Using CVLog for Unit Logs

```java
// Create a log for a unit
CVLog unitLog = new CVLog(300, 10); // expire after 300 frames, keep max 10 messages
unitLog.setUnitId(unit.id());

// Add messages to the log
unitLog.addMessage("Unit started attacking target " + target.id(), unit);
```

### Finalization

Make sure to finalize CherryVis data when the game ends:

```java
// In your game end callback
CherryVis.finalize();
```

## Integration with Existing Code

To integrate with your existing `Log` class, you can either:

1. Replace your existing Log class with CVLog
2. Modify your existing Log class to call CherryVis methods
3. Create a wrapper that uses both systems

## Files Created

CherryVis will create the following files in the specified directory:

- `frames.csv` - List of all frames in the game
- `unit_[id].json` - Data for each unit
- `[category].json` - Event logs for each category

These files can then be used by the CherryVis replay viewer to enhance the replay viewing experience.
