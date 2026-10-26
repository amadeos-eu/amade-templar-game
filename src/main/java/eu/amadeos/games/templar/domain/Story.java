package eu.amadeos.games.templar.domain;

import eu.amadeos.gameengine.adventure.Room;
import eu.amadeos.gameengine.adventure.Script;
import eu.amadeos.gameengine.data.TextData;
import eu.amadeos.gameengine.math.Vector;
import eu.amadeos.gameengine.tile.Direction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * The whole adventure as data: the rooms with their heroes, the people in them, the script and where a new
 * game begins. {@code story.txt} lists it all:
 *
 * <pre>
 * start chapter start                       # a new game: room and spot
 * room chapter aimery                       # a room, and who is the hero there
 * person gerard chapter 470 300 left        # someone in a room: x y facing [height h] [hidden]
 * script prologue.txt                       # a script file; all of them make one script
 * </pre>
 *
 * Each room has its own file, {@code rooms/<id>.txt}, read by {@link Room#parse}.
 */
public final class Story {
    /** Where a person stands in a room at the start, how tall they are at scale 1, and whether they are there yet. */
    public record Person(String id, String room, Vector position, Direction facing, double height, boolean hidden) {
    }

    private static final String BASE = "/eu/amadeos/games/templar/";

    private final String startRoom;
    private final String startSpot;
    private final Map<String, Room> rooms;
    private final Map<String, String> heroes;
    private final List<Person> people;
    private final Script script;

    Story(String startRoom, String startSpot, Map<String, Room> rooms, Map<String, String> heroes, List<Person> people,
          Script script) {
        this.startRoom = Objects.requireNonNull(startRoom, "start");
        this.startSpot = startSpot;
        this.rooms = Collections.unmodifiableMap(rooms);
        this.heroes = Collections.unmodifiableMap(heroes);
        this.people = List.copyOf(people);
        this.script = script;
        if (!rooms.containsKey(startRoom)) throw new TextData.DataException("story.txt: no room " + startRoom);
    }

    /** Reads the story from the game's resources, fresh: rooms remember what was hidden, so each game needs its own. */
    public static Story load() {
        List<TextData.Line> lines = TextData.load(Story.class, BASE + "story.txt");
        String start = null, spot = null;
        Map<String, Room> rooms = new LinkedHashMap<>();
        Map<String, String> heroes = new LinkedHashMap<>();
        List<Person> people = new ArrayList<>();
        List<TextData.Line> script = new ArrayList<>();
        for (TextData.Line line : lines) {
            switch (line.keyword()) {
                case "start" -> {
                    start = line.word(1);
                    spot = line.word(2);
                }
                case "room" -> {
                    String id = line.word(1);
                    Room room = Room.parse(TextData.load(Story.class, BASE + "rooms/" + id + ".txt"));
                    if (!room.id().equals(id)) throw line.error("rooms/" + id + ".txt describes room " + room.id());
                    rooms.put(id, room);
                    heroes.put(id, line.word(2));
                }
                case "person" -> people.add(person(line, rooms));
                case "script" -> script.addAll(TextData.load(Story.class, BASE + "scripts/" + line.word(1)));
                default -> throw line.error("unknown keyword " + line.keyword());
            }
        }
        if (start == null) throw new TextData.DataException("story.txt: no start line");
        return new Story(start, spot, rooms, heroes, people, Script.parse(script));
    }

    private static Person person(TextData.Line line, Map<String, Room> rooms) {
        String room = line.word(2);
        if (!rooms.containsKey(room)) throw line.error("no room " + room + " (rooms come before their people)");
        Direction facing;
        try {
            facing = Direction.valueOf(line.word(5).toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw line.error('"' + line.word(5) + "\" is no direction");
        }
        double height = 120;
        boolean hidden = false;
        for (int i = 6; i < line.size(); i++) {
            switch (line.word(i)) {
                case "height" -> height = line.number(++i);
                case "hidden" -> hidden = true;
                default -> throw line.error("expected height or hidden, found " + line.word(i));
            }
        }
        return new Person(line.word(1), room, new Vector(line.number(3), line.number(4)), facing, height, hidden);
    }

    public String startRoom() {
        return startRoom;
    }

    public String startSpot() {
        return startSpot;
    }

    public Map<String, Room> rooms() {
        return rooms;
    }

    public Room room(String id) {
        Room room = rooms.get(id);
        if (room == null) throw new IllegalArgumentException("no room " + id);
        return room;
    }

    /** Who the player is in a room: Aimery in 1307, Maria today. */
    public String hero(String room) {
        return heroes.get(room);
    }

    public List<Person> people() {
        return people;
    }

    public Script script() {
        return script;
    }
}
