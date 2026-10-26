package eu.amadeos.games.templar.ui.art.rooms;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** The painters of all rooms, by the room's id. */
public final class Rooms {
    private static final Map<String, Supplier<RoomArt>> ART = new LinkedHashMap<>();

    static {
        ART.put("chapter", Chapter::new);
        ART.put("courtyard", Courtyard::new);
        ART.put("library", Library::new);
        ART.put("workshop", Workshop::new);
        ART.put("reading", ReadingRoom::new);
        ART.put("square", Square::new);
        ART.put("bridge", Bridge::new);
        ART.put("cart", Cart::new);
        ART.put("harbour", Harbour::new);
        ART.put("court", Court::new);
        ART.put("quay", Quay::new);
        ART.put("charola", Charola::new);
        ART.put("archive", Archive::new);
        ART.put("cloister", Cloister::new);
    }

    private Rooms() {
    }

    public static List<String> ids() {
        return List.copyOf(ART.keySet());
    }

    public static RoomArt art(String id) {
        Supplier<RoomArt> art = ART.get(id);
        if (art == null) throw new IllegalArgumentException("no picture for room " + id);
        return art.get();
    }
}
