package eu.amadeos.games.templar.ui.art.rooms;

import eu.amadeos.games.templar.ui.art.Ink;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.function.Consumer;

/** A room as pictures, ready to draw: made once by {@link RoomArt#render()}. */
public record Painted(RoomArt art, BufferedImage background, Map<String, Part> layers, Map<String, Part> props,
                      Map<String, Double> propBaselines) {

    /** A piece of the picture cut to the size of what is drawn on it, and where it goes, in image pixels. */
    public record Part(BufferedImage image, int x, int y) {
        static Part of(Consumer<Graphics2D> drawing) {
            BufferedImage full = new BufferedImage(RoomArt.W * RoomArt.S, RoomArt.H * RoomArt.S, BufferedImage.TYPE_INT_ARGB);
            Ink.layer(full, RoomArt.S, drawing);
            int w = full.getWidth(), h = full.getHeight();
            int[] px = full.getRGB(0, 0, w, h, null, 0, w);
            int x0 = w, y0 = h, x1 = -1, y1 = -1;
            for (int y = 0; y < h; y++)
                for (int x = 0; x < w; x++)
                    if ((px[y * w + x] >>> 24) != 0) {
                        x0 = Math.min(x0, x);
                        y0 = Math.min(y0, y);
                        x1 = Math.max(x1, x);
                        y1 = Math.max(y1, y);
                    }
            if (x1 < 0) return new Part(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), 0, 0);
            return new Part(full.getSubimage(x0, y0, x1 - x0 + 1, y1 - y0 + 1), x0, y0);
        }

        /** Draws the part in logical coordinates. */
        public void draw(Graphics2D g) {
            double k = 1.0 / RoomArt.S;
            g.drawImage(image, new java.awt.geom.AffineTransform(k, 0, 0, k, x * k, y * k), null);
        }
    }

    /** Draws the background in logical coordinates. */
    public void drawBackground(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(background, 0, 0, RoomArt.W, RoomArt.H, null);
    }
}
