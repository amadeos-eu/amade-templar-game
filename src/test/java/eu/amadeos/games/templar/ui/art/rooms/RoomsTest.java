package eu.amadeos.games.templar.ui.art.rooms;

import eu.amadeos.gameengine.adventure.Room;
import eu.amadeos.games.templar.domain.Story;
import org.junit.jupiter.api.Test;

import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomsTest {
    final Story story = Story.load();

    @Test
    void everyRoomOfTheStoryIsPainted() {
        assertEquals(new TreeSet<>(story.rooms().keySet()), new TreeSet<>(Rooms.ids()));
        for (String id : Rooms.ids()) assertEquals(id, Rooms.art(id).id());
    }

    /** The layers a room file names are painted, and every prop belongs to a hotspot that can show or hide it. */
    @Test
    void layersAndPropsMatchTheRoomFiles() {
        TreeSet<String> problems = new TreeSet<>();
        for (Room room : story.rooms().values()) {
            RoomArt art = Rooms.art(room.id());
            for (Room.Layer layer : room.layers()) {
                if (!art.layers().containsKey(layer.image())) problems.add(room.id() + ": layer " + layer.image() + " not painted");
            }
            for (String layer : art.layers().keySet()) {
                if (room.layers().stream().noneMatch(l -> l.image().equals(layer))) problems.add(room.id() + ": layer " + layer + " not in the room file");
            }
            for (String prop : art.props().keySet()) {
                if (!room.has(prop)) problems.add(room.id() + ": prop " + prop + " has no hotspot");
            }
        }
        assertEquals(new TreeSet<>(), problems);
    }

    @Test
    void aRoomRendersAtTwiceTheLogicalSize() {
        Painted painted = Rooms.art("bridge").render();
        assertEquals(RoomArt.W * RoomArt.S, painted.background().getWidth());
        assertEquals(RoomArt.H * RoomArt.S, painted.background().getHeight());
        assertTrue(painted.props().containsKey("swarm"));
    }
}
