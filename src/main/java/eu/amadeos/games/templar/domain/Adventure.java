package eu.amadeos.games.templar.domain;

import eu.amadeos.gameengine.adventure.Actor;
import eu.amadeos.gameengine.adventure.Flags;
import eu.amadeos.gameengine.adventure.Hotspot;
import eu.amadeos.gameengine.adventure.Interaction;
import eu.amadeos.gameengine.adventure.Inventory;
import eu.amadeos.gameengine.adventure.ReadingTime;
import eu.amadeos.gameengine.adventure.Room;
import eu.amadeos.gameengine.adventure.ScriptRunner;
import eu.amadeos.gameengine.math.Vector;
import eu.amadeos.gameengine.save.Snapshot;
import eu.amadeos.gameengine.tile.Direction;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

/**
 * A game being played, without pictures: the room the hero is in, the people there, what the hero carries,
 * what the story remembers, and the script that plays when the player clicks. It is the script's
 * {@link ScriptRunner.Stage}: a {@code say} becomes the {@link #line()} the screen shows over the speaker, a
 * {@code goto} a fade to another room, a {@code title} a {@link #card()} across the screen.
 *
 * <p>The game calls {@link #click} with what the mouse did and {@link #update} once per step; the screen asks
 * what to draw. Everything here runs on the game loop thread and can be tested without a screen: the tests
 * play the whole story with clicks.
 */
public final class Adventure implements ScriptRunner.Stage {
    /** How fast people walk, in pixels per second at scale 1. */
    public static final double SPEED = 100;
    /** How long a fade to black takes, and the same again back. */
    public static final double FADE_SECONDS = 0.5;
    /** How long a title card stays, unless clicked away. */
    public static final double CARD_SECONDS = 4.5;
    /** How long the animations of the scripts take; the screen shows their poses meanwhile. */
    public static final Map<String, Double> ANIMATIONS = Map.of(
            "reach", 1.0, "pickup", 1.1, "kneel", 1.4, "shrug", 0.9, "point", 1.0, "phone", 1.6,
            "startle", 1.0, "flee", 0.6);

    /** A line being spoken: by whom, its key in the texts, and the text. */
    public record Line(String actor, String key, String text) {
    }

    /** A title across the screen, such as a place and a date. */
    public record Card(String key, String text) {
    }

    /** A person in a room, as the screen draws them. */
    public record Figure(String id, Actor actor, double height) {
    }

    private enum Fade { NONE, OUT, IN }

    private final Story story;
    private final Function<String, String> texts;
    private final Flags flags = new Flags();
    private final Inventory inventory = new Inventory();
    private final ScriptRunner runner;
    /** The people of each room, by room and then by id. */
    private final Map<String, Map<String, Figure>> people = new LinkedHashMap<>();
    /** Who is not to be seen yet or any more, as {@code room/id}. */
    private final Set<String> hiddenPeople = new HashSet<>();
    private final Map<String, Actor> heroes = new HashMap<>();
    private final Map<String, String> animation = new HashMap<>();
    private final Map<String, Double> animationLeft = new HashMap<>();
    private final Map<String, Double> animationLength = new HashMap<>();
    private final Deque<List<String>> queued = new ArrayDeque<>();
    private final List<String> sounds = new ArrayList<>();
    private Room room;
    private Actor hero;
    private String held;
    private Line line;
    private double lineLeft;
    private Card card;
    private double cardAge;
    private Fade fade = Fade.NONE;
    private double fadeTime;
    private String nextRoom;
    private String nextSpot;
    private boolean saveWanted;

    /**
     * A new game of the story; {@code texts} gives the text of a key in the player's language, so that a line
     * stays as long as it takes to read.
     */
    public Adventure(Story story, Function<String, String> texts) {
        this.story = story;
        this.texts = texts;
        this.runner = new ScriptRunner(story.script(), this, flags, inventory);
        for (Story.Person p : story.people()) {
            Actor actor = new Actor(p.id(), p.position(), p.facing(), SPEED);
            people.computeIfAbsent(p.room(), r -> new LinkedHashMap<>()).put(p.id(), new Figure(p.id(), actor, p.height()));
            if (p.hidden()) hiddenPeople.add(p.room() + "/" + p.id());
        }
        Room start = story.room(story.startRoom());
        Room.Spot spot = start.spot(story.startSpot());
        arrive(start, spot.position(), spot.facing());
    }

    /** Starts a new game: the story's {@code on start} block, if it has one, plays first. */
    public static Adventure begin(Story story, Function<String, String> texts) {
        Adventure adventure = new Adventure(story, texts);
        adventure.queue("start");
        adventure.queue("enter", story.startRoom());
        return adventure;
    }

    // ---- what the player does ------------------------------------------------------------------------

    /**
     * A click at a point of the room. While someone speaks, a click ends the line; while a script plays
     * otherwise, clicks do nothing. The right button puts back an item in hand, or looks; the left button
     * walks, uses, talks or leaves.
     */
    public void click(int button, Vector p) {
        if (skip()) return;
        if (isBusy()) return;
        if (button == Interaction.RIGHT && held != null) {
            held = null;
            return;
        }
        Hotspot spot = hotspotAt(p);
        if (spot == null) {
            if (button == Interaction.LEFT) hero.walkTo(room.floor(), p, null);
            return;
        }
        List<String> trigger = Interaction.trigger(button, spot, held, this::isPerson);
        held = null;
        if (button == Interaction.RIGHT) {
            hero.stop();
            hero.face(toward(hero.position(), center(spot)));
            act(trigger, spot);
            return;
        }
        hero.walkTo(room.floor(), spot.stand(), () -> {
            hero.face(spot.facing());
            act(trigger, spot);
        });
    }

    /**
     * A click on an item in the inventory bar: the right button looks at it; the left button takes it in hand,
     * or uses the item in hand on it.
     */
    public void clickItem(int button, String item) {
        if (skip() || isBusy() || !inventory.has(item)) return;
        if (button == Interaction.RIGHT) {
            held = null;
            act(List.of("look", item), null);
        } else if (held == null || held.equals(item)) {
            held = held == null ? item : null;
        } else {
            String used = held;
            held = null;
            if (!runner.start("use", used, item) && !runner.start("use", item, used)) speak(hero.id(), "default.combine");
        }
    }

    /** Ends the line being spoken or the title being shown, as a click does; true if there was one. */
    public boolean skip() {
        if (fade != Fade.NONE) return true;
        if (card != null) {
            card = null;
            return true;
        }
        if (line != null) {
            line = null;
            return true;
        }
        return false;
    }

    /** The player picks one of the {@link #choices()} of a dialog. */
    public void choose(String id) {
        if (runner.isChoosing() && line == null) runner.choose(id);
    }

    public List<ScriptRunner.Choice> choices() {
        return line == null ? runner.choices() : List.of();
    }

    private void act(List<String> trigger, Hotspot spot) {
        if (trigger.getFirst().equals("talk")) {
            Figure person = person(trigger.get(1));
            if (person != null) person.actor().face(toward(person.actor().position(), hero.position()));
        }
        if (runner.start(trigger.toArray(String[]::new))) return;
        switch (trigger.getFirst()) {
            case "look" -> speak(hero.id(), "default.look");
            case "talk" -> speak(hero.id(), "default.talk");
            case "exit" -> go(spot.exitTo(), spot.exitSpot());
            default -> speak(hero.id(), trigger.size() == 3 ? "default.use-on" : "default.use");
        }
    }

    /** Lets the hero say a line outside a script, such as an answer to something that does not work. */
    private void speak(String actor, String key) {
        line = new Line(actor, key, texts.apply(key));
        lineLeft = ReadingTime.of(line.text());
    }

    /** Plays the script's block for the trigger as soon as nothing else plays, if the script has one. */
    public void queue(String... trigger) {
        if (story.script().find(trigger) != null) queued.add(List.of(trigger));
    }

    // ---- the clock -------------------------------------------------------------------------------------

    public void update(double seconds) {
        if (fade != Fade.NONE) {
            fadeTime += seconds;
            if (fade == Fade.OUT && fadeTime >= FADE_SECONDS) {
                Room to = story.room(nextRoom);
                Room.Spot spot = to.spot(nextSpot);
                arrive(to, spot.position(), spot.facing());
                fade = Fade.IN;
                fadeTime = 0;
                queue("enter", to.id());
            } else if (fade == Fade.IN && fadeTime >= FADE_SECONDS) {
                fade = Fade.NONE;
                saveWanted = true;
            }
            if (fade != Fade.NONE) return;
        }
        if (card != null) {
            cardAge += seconds;
            if (cardAge >= CARD_SECONDS) card = null;
        }
        if (line != null) {
            lineLeft -= seconds;
            if (lineLeft <= 0) line = null;
        }
        animationLeft.replaceAll((actor, left) -> left - seconds);
        animationLeft.entrySet().removeIf(e -> {
            if (e.getValue() > 0) return false;
            animation.remove(e.getKey());
            return true;
        });
        hero.update(seconds, room.depth());
        for (Figure f : figures()) f.actor().update(seconds, room.depth());
        runner.update(seconds);
        while (!isBusy() && !queued.isEmpty()) runner.start(queued.removeFirst().toArray(String[]::new));
    }

    /** Whether a script, a line, a title or a fade is playing: the player cannot act meanwhile. */
    public boolean isBusy() {
        return runner.isBusy() || line != null || card != null || fade != Fade.NONE;
    }

    /** Whether the game may be saved now: nothing plays and the hero stands still. */
    public boolean isIdle() {
        return !isBusy() && queued.isEmpty() && !hero.isWalking();
    }

    /** True once after the hero arrived in a room, and the game should save itself. */
    public boolean takeSaveWanted() {
        boolean wanted = saveWanted && isIdle();
        if (wanted) saveWanted = false;
        return wanted;
    }

    // ---- Stage: what the script makes happen -----------------------------------------------------------

    @Override
    public BooleanSupplier say(String actor, String text) {
        speak(actor, text);
        Line said = line;
        return () -> line != said;
    }

    @Override
    public BooleanSupplier walk(String actor, Vector to) {
        Actor a = actor(actor);
        a.walkTo(room.floor(), to, null);
        return () -> !a.isWalking();
    }

    @Override
    public BooleanSupplier walkTo(String actor, String place) {
        return walk(actor, place(place));
    }

    @Override
    public void face(String actor, Direction direction) {
        actor(actor).face(direction);
    }

    @Override
    public BooleanSupplier animate(String actor, String name) {
        Actor a = actor(actor);
        double seconds = ANIMATIONS.getOrDefault(name, 0.8);
        animation.put(a.id(), name);
        animationLeft.put(a.id(), seconds);
        animationLength.put(a.id(), seconds);
        return () -> !animation.containsKey(a.id());
    }

    @Override
    public void show(String hotspot, boolean visible) {
        if (people.getOrDefault(room.id(), Map.of()).containsKey(hotspot)) {
            if (visible) hiddenPeople.remove(room.id() + "/" + hotspot);
            else hiddenPeople.add(room.id() + "/" + hotspot);
        } else if (visible) {
            room.show(hotspot);
        } else {
            room.hide(hotspot);
        }
    }

    @Override
    public BooleanSupplier enter(String roomId, String spot) {
        go(roomId, spot);
        return () -> fade == Fade.NONE;
    }

    /** Fades to another room; the hero there arrives at its spot. */
    private void go(String roomId, String spot) {
        story.room(roomId).spot(spot);              // fails now, not after the fade, for a wrong name
        nextRoom = roomId;
        nextSpot = spot;
        fade = Fade.OUT;
        fadeTime = 0;
        held = null;
    }

    @Override
    public BooleanSupplier title(String key) {
        card = new Card(key, texts.apply(key));
        cardAge = 0;
        Card shown = card;
        return () -> card != shown;
    }

    @Override
    public void sound(String name) {
        sounds.add(name);
    }

    // ---- the room and the people -------------------------------------------------------------------------

    private void arrive(Room to, Vector at, Direction facing) {
        room = to;
        String id = story.hero(to.id());
        hero = heroes.computeIfAbsent(id, h -> new Actor(h, at, facing, SPEED));
        hero.place(at, facing);
    }

    /** The topmost thing under the point: a person, else a hotspot of the room; null for none. */
    public Hotspot hotspotAt(Vector p) {
        List<Figure> nearFirst = new ArrayList<>(figures());
        nearFirst.sort(Comparator.comparingDouble((Figure f) -> f.actor().position().y()).reversed());
        for (Figure f : nearFirst) {
            Hotspot h = personSpot(f);
            if (h.contains(p)) return h;
        }
        return room.hotspotAt(p);
    }

    /** A person as a hotspot: the outline around them, and a place beside them, on the hero's side. */
    private Hotspot personSpot(Figure f) {
        Vector at = f.actor().position();
        double scale = room.depth().at(at.y());
        double h = f.height() * scale, w = Math.max(16, h * 0.34);
        double side = hero.position().x() <= at.x() ? -1 : 1;
        double gap = Math.max(18, Math.min(44, h * 0.4)) + 8 * scale;
        Vector stand = room.floor().nearest(new Vector(at.x() + side * gap, at.y() + 2));
        if (stand.distance(at) < 6) stand = room.floor().nearest(new Vector(at.x() - side * gap, at.y() + 2));
        return new Hotspot(f.id(), Hotspot.rectangle(at.x() - w / 2, at.y() - h, w, h), stand,
                side < 0 ? Direction.RIGHT : Direction.LEFT);
    }

    private Vector place(String place) {
        Figure person = person(place);
        if (person != null) return personSpot(person).stand();
        if (room.has(place)) return room.hotspot(place).stand();
        return room.spot(place).position();
    }

    private boolean isPerson(String id) {
        return person(id) != null;
    }

    private Figure person(String id) {
        Figure f = people.getOrDefault(room.id(), Map.of()).get(id);
        return f == null || hiddenPeople.contains(room.id() + "/" + id) ? null : f;
    }

    private Actor actor(String id) {
        if (id.equals("hero") || id.equals(hero.id())) return hero;
        Figure f = people.getOrDefault(room.id(), Map.of()).get(id);
        if (f == null) throw new IllegalArgumentException("nobody called " + id + " in room " + room.id());
        return f.actor();
    }

    private static Vector center(Hotspot h) {
        double x = 0, y = 0;
        for (Vector v : h.area()) {
            x += v.x();
            y += v.y();
        }
        return new Vector(x / h.area().size(), y / h.area().size());
    }

    private static Direction toward(Vector from, Vector to) {
        Vector d = to.minus(from);
        if (Math.abs(d.x()) * 0.6 >= Math.abs(d.y())) return d.x() < 0 ? Direction.LEFT : Direction.RIGHT;
        return d.y() < 0 ? Direction.UP : Direction.DOWN;
    }

    // ---- what the screen shows ---------------------------------------------------------------------

    public Room room() {
        return room;
    }

    public Actor hero() {
        return hero;
    }

    /** The visible people of the room, the hero not among them. */
    public List<Figure> figures() {
        List<Figure> visible = new ArrayList<>();
        for (Figure f : people.getOrDefault(room.id(), Map.of()).values())
            if (!hiddenPeople.contains(room.id() + "/" + f.id())) visible.add(f);
        return visible;
    }

    /** The animation an actor plays now, such as {@code reach}, or null. */
    public String animation(String actor) {
        return animation.get(actor);
    }

    /** How far an actor's animation is, 0 at its start to 1 at its end. */
    public double animationProgress(String actor) {
        Double left = animationLeft.get(actor), length = animationLength.get(actor);
        if (left == null || length == null || length <= 0) return 0;
        return Math.max(0, Math.min(1, 1 - left / length));
    }

    public Line line() {
        return line;
    }

    public Card card() {
        return card;
    }

    /** How old the title card is, in seconds, for fading it in and out. */
    public double cardAge() {
        return cardAge;
    }

    /** How dark the screen is in a fade, 0 to 1. */
    public double darkness() {
        return switch (fade) {
            case NONE -> 0;
            case OUT -> Math.min(1, fadeTime / FADE_SECONDS);
            case IN -> Math.max(0, 1 - fadeTime / FADE_SECONDS);
        };
    }

    /** The item in hand, or null. */
    public String held() {
        return held;
    }

    public Inventory inventory() {
        return inventory;
    }

    public Flags flags() {
        return flags;
    }

    public boolean isChoosing() {
        return runner.isChoosing() && line == null;
    }

    /** The sounds the script asked for since the last call. */
    public List<String> takeSounds() {
        List<String> taken = List.copyOf(sounds);
        sounds.clear();
        return taken;
    }

    public Story story() {
        return story;
    }

    // ---- saving ---------------------------------------------------------------------------------------

    /** Everything a saved game needs; only meaningful while {@link #isIdle()}. */
    public Map<String, String> save() {
        Snapshot.Writer w = Snapshot.writer();
        w.put("room", room.id());
        w.put("x", hero.position().x());
        w.put("y", hero.position().y());
        w.put("facing", hero.facing());
        w.put("items", inventory.save());
        w.list("flag").list("hidden").list("person");
        flags.all().forEach((name, value) -> w.add("flag").put("name", name).put("value", value));
        for (Room r : story.rooms().values())
            for (String h : r.hidden()) w.add("hidden").put("room", r.id()).put("hotspot", h);
        people.forEach((roomId, figures) -> figures.values().forEach(f -> w.add("person")
                .put("room", roomId).put("id", f.id())
                .put("x", f.actor().position().x()).put("y", f.actor().position().y())
                .put("facing", f.actor().facing())
                .put("hidden", hiddenPeople.contains(roomId + "/" + f.id()))));
        return w.values();
    }

    /** A saved game, played on from where it was saved. */
    public static Adventure restore(Story story, Function<String, String> texts, Map<String, String> values) {
        Adventure a = new Adventure(story, texts);
        Snapshot.Reader r = Snapshot.reader(values);
        Map<String, Integer> saved = new HashMap<>();
        for (Snapshot.Reader f : r.list("flag")) saved.put(f.text("name"), f.integer("value"));
        a.flags.restore(saved);
        a.inventory.restore(r.text("items", ""));
        for (Room room : story.rooms().values())
            for (var h : room.hotspots()) room.show(h.id());
        for (Snapshot.Reader h : r.list("hidden")) story.room(h.text("room")).hide(h.text("hotspot"));
        a.hiddenPeople.clear();
        for (Snapshot.Reader p : r.list("person")) {
            Figure f = a.people.getOrDefault(p.text("room"), Map.of()).get(p.text("id"));
            if (f == null) continue;                    // someone a later version no longer has
            f.actor().place(new Vector(p.number("x"), p.number("y")), p.choice("facing", Direction.class));
            if (p.bool("hidden")) a.hiddenPeople.add(p.text("room") + "/" + p.text("id"));
        }
        a.arrive(story.room(r.text("room")), new Vector(r.number("x"), r.number("y")),
                r.choice("facing", Direction.class));
        return a;
    }
}
