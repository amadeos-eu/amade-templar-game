package eu.amadeos.games.templar.ui.art;

import eu.amadeos.gameengine.tile.Direction;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.HashMap;
import java.util.Map;

/**
 * The pictures of the people, drawn once and kept: each look, view, pose and frame becomes an image with its
 * ink line, at {@link #RES} image pixels per logical pixel, so it stays sharp on a large screen. A room's
 * light tints them, so a figure in a night scene is as dark and blue as the room around it.
 */
public final class Sprites {
    /** Image pixels per logical pixel at depth scale 1. */
    public static final double RES = 2.5;
    /** Frames of a walk cycle (two steps). */
    public static final int WALK_FRAMES = 10;
    /** Frames of an animation from start to end. */
    public static final int ANIM_FRAMES = 12;

    private record Key(String look, Direction facing, String pose, int frame, int tint) {
    }

    private record Sprite(BufferedImage image, double originX, double originY) {
    }

    private final Map<Key, Sprite> cache = new HashMap<>();

    /**
     * Draws a person with the soles at (x, y), {@code scale} times the size of the look.
     *
     * @param pose  "stand", "walk", "talk" or an animation such as "reach"
     * @param t     the walk phase, 0 to 1; the progress of an animation, 0 to 1; for "talk" 0 or 1 (mouth)
     * @param tint  the light of the room, multiplied into the colours; white leaves them
     */
    public void draw(Graphics2D g, String id, Look look, Direction facing, String pose, double t, double x, double y,
                     double scale, Color tint) {
        int frame = switch (pose) {
            case "stand" -> 0;
            case "walk" -> Math.floorMod((int) Math.floor(t * WALK_FRAMES), WALK_FRAMES);
            case "talk" -> t >= 0.5 ? 1 : 0;
            default -> Math.min(ANIM_FRAMES, (int) Math.round(t * ANIM_FRAMES));
        };
        String name = look == null ? id : look.name() + "/" + look.garment() + look.extras().size();
        Key key = new Key(name, facing, pose, frame, tint.getRGB());
        Sprite sprite = cache.computeIfAbsent(key, k -> render(id, look, facing, pose, frame, tint));
        double k = scale / RES;
        Object keep = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(sprite.image(), (int) Math.round(x - sprite.originX() * k), (int) Math.round(y - sprite.originY() * k),
                (int) Math.round(sprite.image().getWidth() * k), (int) Math.round(sprite.image().getHeight() * k), null);
        if (keep != null) g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, keep);
    }

    /** Forgets the images, as after a change of language or when a game ends. */
    public void clear() {
        cache.clear();
    }

    private static Sprite render(String id, Look look, Direction facing, String pose, int frame, Color tint) {
        double height = look == null ? 30 : look.height();
        Rectangle2D b = Figure.bounds();
        double k = height / Figure.H;
        int w = (int) Math.ceil(b.getWidth() * k * RES) + 8, h = (int) Math.ceil(b.getHeight() * k * RES) + 8;
        double ox = w / 2.0, oy = -b.getY() * k * RES + 4;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = Ink.graphics(image, 1);
        g.translate(ox, oy);
        g.scale(RES, RES);
        boolean mirror = facing == Direction.LEFT;
        if (mirror) g.scale(-1, 1);
        if (look == null) {
            Animals.draw(g, id, pose.equals("walk"), frame / (double) WALK_FRAMES, !pose.equals("stand") && !pose.equals("walk"));
        } else {
            Figure.View view = switch (facing) {
                case UP -> Figure.View.BACK;
                case DOWN -> Figure.View.FRONT;
                default -> Figure.View.SIDE;
            };
            Pose p = switch (pose) {
                case "stand" -> Pose.STAND;
                case "walk" -> Pose.walk(frame / (double) WALK_FRAMES);
                case "talk" -> Pose.STAND.talking(frame == 1);
                default -> Pose.of(pose, frame / (double) ANIM_FRAMES);
            };
            Figure.draw(g, look, view, p);
        }
        g.dispose();
        BufferedImage inked = Ink.outlined(image, RES * 0.8);
        if (tint.getRGB() != Color.WHITE.getRGB()) {
            float[] scales = {tint.getRed() / 255f, tint.getGreen() / 255f, tint.getBlue() / 255f, 1f};
            inked = new RescaleOp(scales, new float[4], null).filter(inked, null);
        }
        return new Sprite(inked, ox, oy);
    }
}
