package eu.amadeos.games.templar.ui.art.rooms;

import eu.amadeos.games.templar.ui.art.Brush;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

import static eu.amadeos.games.templar.ui.art.Brush.c;
import static eu.amadeos.games.templar.ui.art.Brush.poly;

/**
 * The library of the Paris Temple, the same night: book chests and a cupboard of chained books, the copyist's
 * desk with knife and glue pot, and the psalter on its lectern under a candle.
 */
public final class Library extends RoomArt {
    static final double FLOOR = 270;

    @Override
    public String id() {
        return "library";
    }

    @Override
    public Color tint() {
        return new Color(232, 212, 200);
    }

    @Override
    protected void paint(BufferedImage img, Graphics2D g) {
        Random r = new Random(13);
        g.setPaint(new GradientPaint(0, 0, c(0x2e261e), 0, (float) FLOOR, c(0x6a5a48)));
        g.fill(new Rectangle2D.Double(0, 0, W, FLOOR));
        Chapter.ashlar(g, 0, 0, W, FLOOR, r, 0.1);
        g.setColor(c(0x2a1c10));
        for (double bx = 0; bx < W; bx += 80) g.fill(new Rectangle2D.Double(bx, 0, 14, 24));
        g.fill(new Rectangle2D.Double(0, 20, W, 10));
        Chapter.flags(g, FLOOR, new Random(3));
        ink(img, Library::door);
        ink(img, Library::window);
        ink(img, l -> cupboard(l, new Random(21)));
        ink(img, Library::chests);
        ink(img, Library::lectern);
        Graphics2D v = (Graphics2D) g.create();
        Brush.pool(v, 470, 300, 200, 70, c(0xffc870, 34));
        Workshop.vignette(v);
        v.dispose();
    }

    @Override
    protected Map<String, Consumer<Graphics2D>> layers() {
        return named("desk", (Consumer<Graphics2D>) Library::desk);
    }

    @Override
    protected Map<String, Prop> props() {
        return Map.of(
                "copy-knife", new Prop(l -> {
                    l.setColor(c(0xc8ccd4));
                    l.fill(poly(250, 280, 266, 278, 268, 280, 252, 282));
                    l.setColor(c(0x4a2c16));
                    l.fill(new RoundRectangle2D.Double(240, 279, 11, 3.6, 2, 2));
                }, 356),
                "glue-pot", new Prop(l -> {
                    l.setColor(c(0x6a4a2a));
                    l.fill(new RoundRectangle2D.Double(300, 270, 14, 11, 4, 4));
                    l.setColor(c(0xd8c088));
                    l.fill(new Ellipse2D.Double(301, 269, 12, 4));
                    l.setColor(c(0x8a6a4a));
                    l.fill(new Rectangle2D.Double(309, 258, 1.6, 13));
                }, 356));
    }

    @Override
    public void light(Graphics2D g, double time) {
        Lights.flame(g, 504, 196, time, 1.3);
        Lights.flame(g, 282, 254, time, 1.1);
        Lights.lamp(g, 470, 220, 120, c(0xffc870), 42);
    }

    // ---- the room ----

    static void door(Graphics2D g) {
        double x = 30, y = 136, w = 62, h = FLOOR - 136;
        g.setColor(c(0x1e1814));
        g.fill(new Arc2D.Double(x - 6, y - 6, w + 12, 66, 0, 180, Arc2D.CHORD));
        g.fill(new Rectangle2D.Double(x - 6, y + 27, w + 12, h - 27));
        g.setPaint(new GradientPaint((float) x, 0, c(0x6a4424), (float) (x + w), 0, c(0x3a2412)));
        g.fill(new Arc2D.Double(x, y, w, 56, 0, 180, Arc2D.CHORD));
        g.fill(new Rectangle2D.Double(x, y + 27, w, h - 27));
        g.setColor(c(0x2a1a0c, 150));
        for (double px = x + 10; px < x + w; px += 10) g.draw(new Line2D.Double(px, y + 4, px, y + h));
        g.setColor(c(0x2a2420));
        g.fill(new Rectangle2D.Double(x, y + 50, w, 4));
        g.fill(new Rectangle2D.Double(x, y + 100, w, 4));
        g.fill(new Ellipse2D.Double(x + w - 12, y + 74, 6, 6));
    }

    static void window(Graphics2D g) {
        double x = 140, y = 70, w = 40, h = 80;
        g.setColor(c(0x2a2018));
        g.fill(new Rectangle2D.Double(x - 6, y - 6, w + 12, h + 12));
        g.setPaint(new GradientPaint(0, (float) y, c(0x16224a), 0, (float) (y + h), c(0x2a3a6a)));
        g.fill(new Arc2D.Double(x, y, w, 40, 0, 180, Arc2D.CHORD));
        g.fill(new Rectangle2D.Double(x, y + 20, w, h - 20));
        g.setColor(c(0x2a2420));
        g.setStroke(new BasicStroke(1.4f));
        for (double ly = y + 16; ly < y + h; ly += 10) g.draw(new Line2D.Double(x, ly, x + w, ly));
        g.draw(new Line2D.Double(x + w / 2, y, x + w / 2, y + h));
        g.setStroke(new BasicStroke(1f));
    }

    static void cupboard(Graphics2D g, Random r) {
        // an open cupboard of books lying flat and chained, as books were kept
        double x = 210, y = 60, w = 160, h = 192;
        g.setPaint(new GradientPaint((float) x, 0, c(0x5a3a1e), (float) (x + w), 0, c(0x2e1e10)));
        g.fill(new Rectangle2D.Double(x, y, w, h));
        g.setColor(c(0x1e140a));
        g.fill(new Rectangle2D.Double(x + 6, y + 6, w - 12, h - 12));
        String[] covers = {"6a2a1a", "4a3a22", "7a5a2a", "3a2a1a", "8a3a2a", "5a4a3a"};
        for (int shelf = 0; shelf < 5; shelf++) {
            double sy = y + 10 + shelf * 36;
            double bx = x + 10;
            while (bx < x + w - 30) {
                double bw = 18 + r.nextInt(14), bh = 6 + r.nextInt(6);
                Color col = c(Integer.parseInt(covers[r.nextInt(covers.length)], 16));
                g.setPaint(new GradientPaint(0, (float) (sy + 26 - bh), col.brighter(), 0, (float) (sy + 26), col.darker()));
                g.fill(new Rectangle2D.Double(bx, sy + 26 - bh, bw, bh));
                g.setColor(c(0xb08a40, 140));
                g.fill(new Rectangle2D.Double(bx + bw / 2 - 1, sy + 26 - bh, 2, bh));
                g.setColor(c(0x8a8a90, 160));
                g.draw(new Line2D.Double(bx + bw / 2, sy + 26 - bh / 2, bx + bw / 2 + 4, sy + 30));
                bx += bw + 3;
            }
            g.setColor(c(0x5a3a1e));
            g.fill(new Rectangle2D.Double(x + 6, sy + 26, w - 12, 4));
        }
    }

    static void chests(Graphics2D g) {
        for (double[] k : new double[][]{{392, 230, 70}, {560, 236, 64}}) {
            double x = k[0], y = k[1], w = k[2];
            g.setPaint(new GradientPaint(0, (float) y, c(0x6a4424), 0, (float) (y + 40), c(0x3a2412)));
            g.fill(new Rectangle2D.Double(x, y, w, 40));
            g.setColor(c(0x3a3a40));
            g.fill(new Rectangle2D.Double(x, y + 8, w, 4));
            g.fill(new Rectangle2D.Double(x + w / 2 - 4, y, 8, 40));
            g.setColor(c(0x7a5230));
            g.fill(new Rectangle2D.Double(x - 2, y - 4, w + 4, 5));
        }
    }

    static void lectern(Graphics2D g) {
        // the lectern with the psalter, under a candle on an iron stand
        double x = 470, y = 230;
        g.setColor(c(0x1a0e05, 100));
        g.fill(new Ellipse2D.Double(x - 14, y + 64, 50, 10));
        g.setColor(c(0x4a2c16));
        g.fill(new Rectangle2D.Double(x + 6, y + 10, 8, 60));
        g.fill(new Rectangle2D.Double(x - 8, y + 64, 36, 6));
        g.setPaint(new GradientPaint(0, (float) y - 6, c(0x7a5230), 0, (float) y + 12, c(0x4a2c16)));
        g.fill(poly(x - 14, y + 12, x + 34, y + 12, x + 30, y - 6, x - 10, y - 10));
        // the psalter, open: red and blue initials
        g.setColor(c(0xeee2c0));
        g.fill(poly(x - 8, y + 8, x + 10, y + 8, x + 10, y - 7, x - 6, y - 9));
        g.fill(poly(x + 10, y + 8, x + 28, y + 8, x + 26, y - 5, x + 10, y - 7));
        g.setColor(c(0xb0201a));
        g.fill(new Rectangle2D.Double(x - 4, y - 5, 3, 3));
        g.setColor(c(0x2a4a8a));
        g.fill(new Rectangle2D.Double(x + 13, y - 4, 3, 3));
        g.setColor(c(0x5a4a3a, 160));
        for (int i = 0; i < 4; i++) {
            g.draw(new Line2D.Double(x, y - 4 + i * 2.6, x + 8, y - 4 + i * 2.6));
            g.draw(new Line2D.Double(x + 17, y - 3 + i * 2.6, x + 24, y - 3 + i * 2.6));
        }
        // the candle stand
        g.setColor(c(0x2a2420));
        g.fill(new Rectangle2D.Double(x + 33, 200, 2.5, 100));
        g.fill(new Rectangle2D.Double(x + 26, 298, 16, 3));
        g.fill(new Ellipse2D.Double(x + 28, 198, 12, 4));
        g.setColor(c(0xf2ead0));
        g.fill(new Rectangle2D.Double(x + 32, 186, 4, 12));
    }

    // ---- in front ----

    static void desk(Graphics2D g) {
        // the copyist's sloping desk with its bench
        double x = 214, y = 284, w = 120;
        g.setColor(c(0x1a0e05, 100));
        g.fill(new Ellipse2D.Double(x - 4, y + 60, w + 8, 12));
        g.setColor(c(0x3a2210));
        g.fill(new Rectangle2D.Double(x + 6, y + 4, 8, 62));
        g.fill(new Rectangle2D.Double(x + w - 14, y + 4, 8, 62));
        g.setPaint(new GradientPaint(0, (float) y - 10, c(0x8a5a32), 0, (float) y + 6, c(0x5a3a1e)));
        g.fill(poly(x + 6, y - 10, x + w - 6, y - 10, x + w, y + 2, x, y + 2));
        g.setColor(c(0x4a2c16));
        g.fill(new Rectangle2D.Double(x, y + 2, w, 7));
        // a sheet, an ink horn, a stub of candle
        g.setColor(c(0xe9dcb8));
        g.fill(poly(x + 62, y - 6, x + 84, y - 7, x + 86, y, x + 62, y));
        g.setColor(c(0x3a2a1a));
        g.fill(poly(x + 90, y - 2, x + 94, y - 14, x + 98, y - 13, x + 96, y - 2));
        g.setColor(c(0xa08040));
        g.fill(new Ellipse2D.Double(x + 64, y - 22, 8, 3));
        g.setColor(c(0xf2ead0));
        g.fill(new Rectangle2D.Double(x + 66, y - 30, 4, 9));
    }
}
