package eu.amadeos.games.templar.ui.game;

import eu.amadeos.games.templar.ui.art.rooms.Painted;
import eu.amadeos.games.templar.ui.art.rooms.RoomArt;
import eu.amadeos.games.templar.ui.art.rooms.Rooms;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The painted rooms. Painting one takes a moment, so all of them are painted in the background as soon as the
 * game starts, the first room first; a room needed before it is ready is waited for.
 */
final class Gallery {
    private final Map<String, CompletableFuture<Painted>> rooms = new ConcurrentHashMap<>();
    private final ExecutorService painter = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "room-painter");
        t.setDaemon(true);
        t.setPriority(Thread.MIN_PRIORITY);
        return t;
    });

    /** Starts painting every room, {@code first} before the others. */
    void prepare(String first) {
        submit(first);
        for (String id : Rooms.ids()) submit(id);
    }

    private CompletableFuture<Painted> submit(String id) {
        return rooms.computeIfAbsent(id, r -> {
            RoomArt art = Rooms.art(r);
            return CompletableFuture.supplyAsync(art::render, painter);
        });
    }

    /** The painted room, waiting for it if it is not ready yet. */
    Painted room(String id) {
        return submit(id).join();
    }

    boolean isReady(String id) {
        return submit(id).isDone();
    }
}
