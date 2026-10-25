package eu.amadeos.games.templar.ui.game;

import eu.amadeos.gameengine.adventure.Actor;
import eu.amadeos.gameengine.adventure.Room;
import eu.amadeos.games.templar.domain.Adventure;
import eu.amadeos.games.templar.ui.art.Brush;
import eu.amadeos.games.templar.ui.art.Cast;
import eu.amadeos.games.templar.ui.art.Look;
import eu.amadeos.games.templar.ui.art.Sprites;
import eu.amadeos.games.templar.ui.art.rooms.Painted;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Draws a room as the adventure stands: the painted picture, then everything that stands on the floor from the
 * back to the front, people, props and the layers they walk behind, each at its baseline, then the light.
 */
final class RoomView {
    /** How far a person walks in one walk cycle (two steps), in heights of the person. */
    static final double CYCLE = 0.78;

    private record Drawable(double baseline, int order, Runnable draw) {
    }

    private final Sprites sprites = new Sprites();

    void draw(Graphics2D g, Adventure adventure, Painted painted, double time) {
        Room room = adventure.room();
        painted.drawBackground(g);
        List<Drawable> items = new ArrayList<>();
        for (Room.Layer layer : room.layers()) {
            Painted.Part part = painted.layers().get(layer.image());
            if (part != null) items.add(new Drawable(layer.baseline(), 1, () -> part.draw(g)));
        }
        painted.props().forEach((id, part) -> {
            if (room.has(id) && !room.isHidden(id))
                items.add(new Drawable(painted.propBaselines().get(id), 2, () -> part.draw(g)));
        });
        List<Adventure.Figure> people = new ArrayList<>(adventure.figures());
        Actor hero = adventure.hero();
        for (Adventure.Figure f : people) {
            Actor a = f.actor();
            items.add(new Drawable(a.position().y(), 0, () -> person(g, adventure, painted, f.id(), a, time)));
        }
        items.add(new Drawable(hero.position().y(), 0, () -> person(g, adventure, painted, hero.id(), hero, time)));
        items.sort(Comparator.comparingDouble(Drawable::baseline).thenComparingInt(Drawable::order));
        for (Drawable d : items) d.draw().run();
        painted.art().light(g, time);
        painted.art().live(g, time);
    }

    /** Where the top of a person's head is, for the line they speak. */
    static Point2D head(Adventure adventure, String id) {
        Actor actor = id.equals(adventure.hero().id()) ? adventure.hero() : null;
        double height = 0;
        if (actor == null) {
            for (Adventure.Figure f : adventure.figures())
                if (f.id().equals(id)) {
                    actor = f.actor();
                    height = f.height();
                }
        } else {
            Look look = Cast.of(id, adventure.room().id());
            height = look == null ? 30 : look.height();
        }
        if (actor == null) return null;
        double scale = adventure.room().depth().at(actor.position().y());
        return new Point2D.Double(actor.position().x(), actor.position().y() - height * scale);
    }

    private void person(Graphics2D g, Adventure adventure, Painted painted, String id, Actor actor, double time) {
        Room room = adventure.room();
        Look look = Cast.of(id, room.id());
        double scale = room.depth().at(actor.position().y());
        double height = look == null ? 20 : look.height();
        double x = actor.position().x(), y = actor.position().y();
        Brush.pool(g, x, y, height * 0.17 * scale, height * 0.035 * scale, Brush.c(0x140a04, 110));
        String pose = "stand";
        double t = 0;
        String animation = adventure.animation(id);
        Adventure.Line line = adventure.line();
        if (animation != null) {
            pose = animation;
            t = adventure.animationProgress(id);
        } else if (actor.isWalking()) {
            pose = "walk";
            t = actor.stride() / (height * CYCLE);
        } else if (line != null && line.actor().equals(id)) {
            pose = "talk";
            t = Math.sin(time * 26) > 0.1 ? 1 : 0;
        }
        sprites.draw(g, id, look, actor.facing(), pose, t, x, y, scale, painted.art().tint());
    }
}
