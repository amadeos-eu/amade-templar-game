package eu.amadeos.games.templar.ui.art.rooms;

import eu.amadeos.games.templar.ui.art.Ink;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The picture of one room, painted in code: everything behind the people, the {@link #layers() layers} they
 * walk behind, the {@link #props() props} that can be taken away, and what moves by itself, a candle
 * flickering, dust in the light. Painted at {@link #S} times the logical size, so it stays sharp on a large
 * screen. A hand-painted picture could replace any part of it later; the room's data file does not change.
 */
public abstract class RoomArt {
    /** Image pixels per logical pixel. */
    public static final int S = 2;
    public static final int W = 640, H = 400;

    public abstract String id();

    /** Paints the background; use {@link #ink} for objects that get an ink line. */
    protected abstract void paint(BufferedImage image, Graphics2D g);

    /** The parts of the picture in front of people: by the name in the room file, a drawing in room coordinates. */
    protected Map<String, Consumer<Graphics2D>> layers() {
        return Map.of();
    }

    /**
     * Things that are there while their hotspot is shown, such as a tool that can be taken: by hotspot id, a
     * drawing in room coordinates and its baseline, as for a layer (a tool on the workbench lies just in front
     * of the bench).
     */
    protected Map<String, Prop> props() {
        return Map.of();
    }

    /** A drawing that belongs to a hotspot, and the height where it stands on the floor. */
    public record Prop(Consumer<Graphics2D> drawing, double baseline) {
    }

    /** The light on the people in this room, multiplied into their colours. */
    public Color tint() {
        return Color.WHITE;
    }

    /** What moves by itself, drawn over everything each frame in logical coordinates; {@code time} in seconds. */
    public void live(Graphics2D g, double time) {
    }

    /** What is drawn over the people, such as the light of a lamp; in logical coordinates. */
    public void light(Graphics2D g, double time) {
    }

    protected static void ink(BufferedImage image, Consumer<Graphics2D> drawing) {
        Ink.layer(image, S, drawing);
    }

    protected static Map<String, Consumer<Graphics2D>> named(Object... nameAndDrawing) {
        Map<String, Consumer<Graphics2D>> map = new LinkedHashMap<>();
        for (int i = 0; i < nameAndDrawing.length; i += 2) {
            @SuppressWarnings("unchecked")
            Consumer<Graphics2D> d = (Consumer<Graphics2D>) nameAndDrawing[i + 1];
            map.put((String) nameAndDrawing[i], d);
        }
        return map;
    }

    /** The painted room: the background, and the layers and props as images with their place. */
    public Painted render() {
        BufferedImage image = new BufferedImage(W * S, H * S, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = Ink.graphics(image, S);
        paint(image, g);
        g.dispose();
        Map<String, Painted.Part> layers = new LinkedHashMap<>();
        layers().forEach((name, drawing) -> layers.put(name, Painted.Part.of(drawing)));
        Map<String, Painted.Part> props = new LinkedHashMap<>();
        Map<String, Double> baselines = new LinkedHashMap<>();
        props().forEach((name, prop) -> {
            props.put(name, Painted.Part.of(prop.drawing()));
            baselines.put(name, prop.baseline());
        });
        return new Painted(this, image, layers, props, baselines);
    }
}
