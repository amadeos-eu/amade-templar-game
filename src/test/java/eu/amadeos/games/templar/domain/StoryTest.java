package eu.amadeos.games.templar.domain;

import eu.amadeos.commons.i18n.Messages;
import eu.amadeos.gameengine.adventure.Hotspot;
import eu.amadeos.gameengine.adventure.Room;
import eu.amadeos.games.templar.bootstrap.TemplarModule;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The story's data files fit together: every text exists, every door leads somewhere, every name is unique. */
class StoryTest {
    static final Messages MSG = Messages.load(TemplarModule.class, TemplarModule.MESSAGES, Locale.ENGLISH);
    final Story story = Story.load();

    /** All script lines, from the files story.txt names. */
    static List<String> scriptLines() throws IOException {
        List<String> lines = new ArrayList<>();
        for (String line : resource("story.txt").split("\n")) {
            if (line.startsWith("script ")) lines.addAll(List.of(resource("scripts/" + line.split("\\s+")[1]).split("\n")));
        }
        return lines;
    }

    static String resource(String name) throws IOException {
        try (InputStream in = Story.class.getResourceAsStream("/eu/amadeos/games/templar/" + name)) {
            assertNotNull(in, name);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    static TreeSet<String> missing(List<String> keys) {
        TreeSet<String> missing = new TreeSet<>();
        for (String key : keys) if (!MSG.has(key)) missing.add(key);
        return missing;
    }

    @Test
    void everyTextTheScriptsSayExists() throws IOException {
        List<String> keys = new ArrayList<>();
        Pattern say = Pattern.compile("^\\s*say \\S+ (\\S+)"), title = Pattern.compile("^\\s*title (\\S+)"),
                option = Pattern.compile("^option \\S+ \\S+ (\\S+)"), goal = Pattern.compile("set goal (\\d+)");
        for (String line : scriptLines()) {
            for (Pattern p : List.of(say, title, option)) {
                Matcher m = p.matcher(line);
                if (m.find()) keys.add(m.group(1));
            }
            Matcher m = goal.matcher(line);
            if (m.find()) {
                keys.add("goal." + m.group(1));
                keys.add("hint." + m.group(1));
            }
        }
        assertTrue(keys.size() > 300, "the scripts say a lot");
        assertEquals(new TreeSet<>(), missing(keys));
    }

    @Test
    void everyItemHasANameAndALook() throws IOException {
        List<String> keys = new ArrayList<>();
        TreeSet<String> items = new TreeSet<>();
        Pattern gain = Pattern.compile("^\\s*gain (\\S+)");
        for (String line : scriptLines()) {
            Matcher m = gain.matcher(line);
            if (m.find()) items.add(m.group(1));
        }
        List<String> script = scriptLines();
        TreeSet<String> unlooked = new TreeSet<>();
        for (String item : items) {
            keys.add("item." + item);
            if (!script.contains("on look " + item)) unlooked.add(item);
        }
        assertEquals(new TreeSet<>(), missing(keys));
        assertEquals(new TreeSet<>(), unlooked);
    }

    @Test
    void everyHotspotAndPersonHasAName() {
        List<String> keys = new ArrayList<>();
        for (Room room : story.rooms().values()) for (Hotspot h : room.hotspots()) keys.add("name." + h.id());
        for (Story.Person p : story.people()) keys.add("name." + p.id());
        assertEquals(new TreeSet<>(), missing(keys));
    }

    // --8<-- [start:unique]
    /** Scripts are written per thing, not per room: two rooms with a "window" would answer each other's clicks. */
    @Test
    void hotspotNamesAreUniqueAcrossRooms() {
        Map<String, String> seen = new HashMap<>();
        TreeSet<String> twice = new TreeSet<>();
        for (Room room : story.rooms().values()) {
            for (Hotspot h : room.hotspots()) {
                String other = seen.put(h.id(), room.id());
                if (other != null) twice.add(h.id() + " in " + other + " and " + room.id());
            }
        }
        for (Story.Person p : story.people()) {
            for (Room room : story.rooms().values()) if (room.has(p.id())) twice.add(p.id() + " is a person and a hotspot");
        }
        assertEquals(new TreeSet<>(), twice);
    }
    // --8<-- [end:unique]

    @Test
    void everyDoorLeadsToARoomAndASpot() throws IOException {
        TreeSet<String> broken = new TreeSet<>();
        for (Room room : story.rooms().values()) {
            for (Hotspot h : room.hotspots()) {
                if (!h.isExit()) continue;
                if (!story.rooms().containsKey(h.exitTo())) broken.add(room.id() + "/" + h.id() + " -> " + h.exitTo());
                else if (!hasSpot(story.room(h.exitTo()), h.exitSpot())) broken.add(room.id() + "/" + h.id() + " -> " + h.exitSpot());
            }
        }
        Pattern go = Pattern.compile("^\\s*goto (\\S+) (\\S+)");
        for (String line : scriptLines()) {
            Matcher m = go.matcher(line);
            if (!m.find()) continue;
            if (!story.rooms().containsKey(m.group(1)) || !hasSpot(story.room(m.group(1)), m.group(2))) broken.add(line.trim());
        }
        assertEquals(new TreeSet<>(), broken);
    }

    private static boolean hasSpot(Room room, String spot) {
        try {
            room.spot(spot);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Test
    void theStoryStartsIn1307AndEveryRoomHasAHero() {
        assertEquals("chapter", story.startRoom());
        assertEquals("aimery", story.hero("chapter"));
        assertEquals("maria", story.hero("workshop"));
        for (String room : story.rooms().keySet()) assertNotNull(story.hero(room), room);
    }
}
