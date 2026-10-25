package eu.amadeos.games.templar.ui.art;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.function.Consumer;

/**
 * The look of hand-drawn adventure pictures: every object gets a dark ink line around it. An object is drawn on
 * a transparent layer of its own; where the layer is solid, a slightly larger dark shape is put underneath,
 * so a line of {@code width} pixels shows all around. Soft things, a glow or a translucent shadow, get no line.
 */
public final class Ink {
    /** The colour of the lines: a very dark brown, warmer than black. */
    public static final int COLOR = 0x2a1a10;
    /** How solid a pixel must be to get a line: lights and shadows stay soft. */
    private static final int SOLID = 230;

    private Ink() {
    }

    /** A graphics for drawing at {@code scale} with good quality, as all pictures of the game are drawn. */
    public static Graphics2D graphics(BufferedImage image, double scale) {
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.scale(scale, scale);
        return g;
    }

    /**
     * Draws {@code drawing} onto {@code target} with an ink line around it; {@code scale} is the size of a
     * logical pixel in the target's pixels, and the line is one logical pixel wide.
     */
    public static void layer(BufferedImage target, double scale, Consumer<Graphics2D> drawing) {
        layer(target, scale, scale, drawing);
    }

    // --8<-- [start:layer]
    /** As {@link #layer(BufferedImage, double, Consumer)}, with a line {@code width} target pixels wide. */
    public static void layer(BufferedImage target, double scale, double width, Consumer<Graphics2D> drawing) {
        BufferedImage layer = new BufferedImage(target.getWidth(), target.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D lg = graphics(layer, scale);
        drawing.accept(lg);
        lg.dispose();
        BufferedImage ink = outline(layer, width);
        Graphics2D g = target.createGraphics();
        g.setComposite(AlphaComposite.SrcOver);
        g.drawImage(ink, 0, 0, null);
        g.drawImage(layer, 0, 0, null);
        g.dispose();
    }
    // --8<-- [end:layer]

    /** Puts an ink line around what is drawn on {@code image} itself; for sprites drawn on their own image. */
    public static BufferedImage outlined(BufferedImage image, double width) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        g.drawImage(outline(image, width), 0, 0, null);
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return result;
    }

    /** The ink under a layer: its solid pixels, grown by {@code width} in every direction. */
    static BufferedImage outline(BufferedImage layer, double width) {
        int w = layer.getWidth(), h = layer.getHeight();
        int[] src = layer.getRGB(0, 0, w, h, null, 0, w);
        boolean[] solid = new boolean[w * h];
        for (int i = 0; i < src.length; i++) solid[i] = (src[i] >>> 24) > SOLID;
        int r = (int) Math.ceil(width);
        double r2 = width * width + 0.5;
        // Grow across, then down, by a disc: for each row offset the half width of the disc at that offset.
        BufferedImage ink = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int[] out = ((DataBufferInt) ink.getRaster().getDataBuffer()).getData();
        int[][] across = new int[r + 1][];
        for (int dy = 0; dy <= r; dy++) {
            int half = (int) Math.floor(Math.sqrt(Math.max(0, r2 - dy * dy)));
            across[dy] = grow(solid, w, h, half);
        }
        int color = 0xff000000 | COLOR;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int i = y * w + x;
                if (solid[i]) continue;
                for (int dy = -r; dy <= r; dy++) {
                    int yy = y + dy;
                    if (yy < 0 || yy >= h) continue;
                    if (across[Math.abs(dy)][yy * w + x] != 0) {
                        out[i] = color;
                        break;
                    }
                }
            }
        }
        return ink;
    }

    /** For each pixel, 1 if a solid pixel lies within {@code half} pixels on its row. */
    private static int[] grow(boolean[] solid, int w, int h, int half) {
        int[] grown = new int[w * h];
        for (int y = 0; y < h; y++) {
            int row = y * w;
            int last = -100000;
            for (int x = 0; x < w; x++) {
                if (solid[row + x]) last = x;
                if (x - last <= half) grown[row + x] = 1;
            }
            last = 100000;
            for (int x = w - 1; x >= 0; x--) {
                if (solid[row + x]) last = x;
                if (last - x <= half) grown[row + x] = 1;
            }
        }
        return grown;
    }
}
