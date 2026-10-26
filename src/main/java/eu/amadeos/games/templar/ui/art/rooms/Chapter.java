package eu.amadeos.games.templar.ui.art.rooms;

import eu.amadeos.games.templar.ui.art.Brush;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

import static eu.amadeos.games.templar.ui.art.Brush.c;
import static eu.amadeos.games.templar.ui.art.Brush.poly;

/**
 * The treasurer's chamber in the Paris Temple, 12 October 1307, at night: stone vaults, the black-and-white
 * banner of the order, a table with candles, account books and Brother Gérard's supper, and the iron-bound
 * chest with the seal.
 */
public final class Chapter extends RoomArt {
    static final double FLOOR = 272;

    @Override
    public String id() {
        return "chapter";
    }

    @Override
    public Color tint() {
        return new Color(236, 214, 200);
    }

    @Override
    protected void paint(BufferedImage img, Graphics2D g) {
        vault(g, new Random(1307));
        flags(g, FLOOR, new Random(12));
        ink(img, Chapter::window);
        ink(img, Chapter::banner);
        ink(img, Chapter::door);
        ink(img, Chapter::chest);
        ink(img, Chapter::lectern);
        Graphics2D v = (Graphics2D) g.create();
        Brush.pool(v, 330, 300, 240, 80, c(0xffc870, 30));
        Workshop.vignette(v);
        v.dispose();
    }

    @Override
    protected Map<String, Consumer<Graphics2D>> layers() {
        return named("table", (Consumer<Graphics2D>) Chapter::table);
    }

    @Override
    protected Map<String, Prop> props() {
        return Map.of(
                "supper", new Prop(l -> {
                    l.setPaint(new GradientPaint(0, 266, c(0xf2d878), 0, 274, c(0xc89a3a)));
                    l.fill(poly(398, 274, 400, 266, 414, 264, 418, 274));
                    l.setColor(c(0xa87a2a));
                    l.fill(new Ellipse2D.Double(406, 268, 2.5, 2));
                }, 353),
                "wax-stick", new Prop(l -> {
                    l.setColor(c(0xb0201a));
                    l.fill(new RoundRectangle2D.Double(264, 270, 16, 4, 2, 2));
                }, 353),
                "chest-open", new Prop(Chapter::chestOpen, 330));
    }

    @Override
    public void light(Graphics2D g, double time) {
        Lights.flame(g, 312, 252, time, 1.2);
        Lights.flame(g, 352, 250, time, 1.2);
        Lights.flame(g, 130, 160, time, 1.6);
        Lights.lamp(g, 330, 250, 120, c(0xffc870), 40);
    }

    // ---- the chamber ----

    /** Stone walls of the Temple: ashlar blocks with soft light and shadow, and ribs of the vault. */
    static void vault(Graphics2D g, Random r) {
        g.setPaint(new GradientPaint(0, 0, c(0x3a3028), 0, (float) FLOOR, c(0x7a6a58)));
        g.fill(new Rectangle2D.Double(0, 0, W, FLOOR));
        ashlar(g, 0, 40, W, FLOOR - 40, r, 0.0);
        // the ribbed vault: two bays
        g.setColor(c(0x2a2018));
        g.fill(new Rectangle2D.Double(0, 0, W, 34));
        for (double cx : new double[]{170, 490}) {
            g.setPaint(new GradientPaint(0, 0, c(0x5a4a3a), 0, 60, c(0x3a3028)));
            g.fill(new Arc2D.Double(cx - 160, -60, 320, 160, 180, 180, Arc2D.CHORD));
            g.setColor(c(0x8a7a64));
            g.setStroke(new BasicStroke(5f));
            g.draw(new Arc2D.Double(cx - 160, -60, 320, 160, 180, 180, Arc2D.OPEN));
            g.setStroke(new BasicStroke(1f));
        }
        // pillars between the bays and at the sides
        for (double px : new double[]{4, 318, 616}) {
            g.setPaint(new GradientPaint((float) px, 0, c(0x9a8a72), (float) px + 22, 0, c(0x4a3e30)));
            g.fill(new Rectangle2D.Double(px, 30, 22, FLOOR - 26));
            g.setColor(c(0x5a4a3a));
            g.fill(new Rectangle2D.Double(px - 3, 30, 28, 6));
            g.fill(new Rectangle2D.Double(px - 3, FLOOR - 8, 28, 8));
        }
    }

    static void ashlar(Graphics2D g, double x, double y, double w, double h, Random r, double shade) {
        for (double by = y; by < y + h; by += 18) {
            double off = ((int) ((by - y) / 18) % 2) * 22;
            for (double bx = x - off; bx < x + w; bx += 44) {
                Color stone = Brush.mix(c(0x8a7a66), c(0x6a5a4a), r.nextDouble() * 0.7 + shade);
                g.setColor(Brush.c(stone.getRGB() & 0xffffff, 90));
                g.fill(new Rectangle2D.Double(bx + 1, by + 1, 42, 16));
                g.setColor(c(0x2a2018, 70));
                g.draw(new Rectangle2D.Double(bx, by, 44, 18));
            }
        }
    }

    /** A floor of big stone flags in perspective. */
    static void flags(Graphics2D g, double top, Random r) {
        g.setPaint(new GradientPaint(0, (float) top, c(0x4a3e32), 0, H, c(0x8a7a62)));
        g.fill(new Rectangle2D.Double(0, top, W, H - top));
        double vx = W / 2.0, vy = top - 160;
        g.setColor(c(0x2a2018, 110));
        for (int row = 0; row < 7; row++) {
            double y = top + Math.pow(row / 7.0, 1.4) * (H - top);
            g.draw(new Line2D.Double(0, y, W, y));
        }
        for (int i = -12; i <= 12; i++) {
            double bx = vx + i * 70;
            g.draw(new Line2D.Double(vx + (bx - vx) * (top - vy) / (H - vy), top, bx, H));
        }
        for (int i = 0; i < 60; i++) {
            g.setColor(r.nextBoolean() ? c(0xc8b89a, 25) : c(0x1a120a, 30));
            double x = r.nextInt(W), y = top + r.nextDouble() * (H - top);
            g.fill(new Ellipse2D.Double(x, y, 10 + r.nextInt(30), 3 + r.nextInt(5)));
        }
    }

    static void window(Graphics2D g) {
        // a tall lancet window with the moon behind the glass
        double x = 400, y = 60, w = 46, h = 130;
        Path2D lancet = new Path2D.Double();
        lancet.moveTo(x, y + h);
        lancet.lineTo(x, y + 30);
        lancet.quadTo(x, y, x + w / 2, y - 8);
        lancet.quadTo(x + w, y, x + w, y + 30);
        lancet.lineTo(x + w, y + h);
        lancet.closePath();
        g.setColor(c(0x3a3028));
        g.fill(new Rectangle2D.Double(x - 8, y - 14, w + 16, h + 22));
        g.setPaint(new GradientPaint(0, (float) y, c(0x1a2a5a), 0, (float) (y + h), c(0x3a4a7a)));
        g.fill(lancet);
        g.setColor(c(0xe8eeff));
        g.fill(new Ellipse2D.Double(x + 26, y + 22, 10, 10));
        g.setColor(c(0x2a2420));
        g.setStroke(new BasicStroke(1.4f));
        for (double ly = y + 10; ly < y + h; ly += 12) g.draw(new Line2D.Double(x, ly, x + w, ly));
        for (double lx = x + 11.5; lx < x + w; lx += 11.5) g.draw(new Line2D.Double(lx, y, lx, y + h));
        g.setStroke(new BasicStroke(3f));
        g.draw(lancet);
        g.setStroke(new BasicStroke(1f));
        g.setColor(c(0xb0c0ff, 24));
        g.fill(poly(x, y + h, x + w, y + h, x + w + 70, FLOOR + 40, x + 10, FLOOR + 40));
    }

    static void banner(Graphics2D g) {
        // the Beauséant, the black-and-white banner of the order, with a red cross above, on the back wall
        double x = 200, y = 64, w = 64, h = 120;
        g.setColor(c(0x3a2414));
        g.fill(new Rectangle2D.Double(x - 6, y - 6, w + 12, 4));
        g.setColor(c(0x1a1614));
        g.fill(new Rectangle2D.Double(x, y, w, h / 2));
        g.setColor(c(0xeee8da));
        g.fill(poly(x, y + h / 2, x + w, y + h / 2, x + w, y + h, x + w / 2, y + h - 12, x, y + h));
        g.setColor(c(0xb0201a));
        g.fill(new Rectangle2D.Double(x + w / 2 - 4, y + 12, 8, 30));
        g.fill(new Rectangle2D.Double(x + w / 2 - 15, y + 23, 30, 8));
        g.setColor(c(0x000000, 40));
        g.fill(new Rectangle2D.Double(x + w - 10, y, 10, h - 4));
        // a torch in an iron ring on the wall beside the door
        g.setColor(c(0x2a2420));
        g.fill(new Rectangle2D.Double(124, 166, 12, 4));
        g.setColor(c(0x5a3a1e));
        g.fill(poly(126, 162, 134, 162, 132, 190, 128, 190));
    }

    static void door(Graphics2D g) {
        double x = 40, y = 128, w = 64, h = 146;
        g.setColor(c(0x2a2018));
        g.fill(new Arc2D.Double(x - 8, y - 8, w + 16, 70, 0, 180, Arc2D.CHORD));
        g.fill(new Rectangle2D.Double(x - 8, y + 27, w + 16, h - 27));
        Path2D leaf = new Path2D.Double();
        leaf.moveTo(x, y + h);
        leaf.lineTo(x, y + 30);
        leaf.append(new Arc2D.Double(x, y, w, 60, 180, -180, Arc2D.OPEN), true);
        leaf.lineTo(x + w, y + h);
        leaf.closePath();
        g.setPaint(new GradientPaint((float) x, 0, c(0x6a4424), (float) (x + w), 0, c(0x3a2412)));
        g.fill(leaf);
        g.setColor(c(0x2a1a0c, 150));
        for (double px = x + 10; px < x + w; px += 11) g.draw(new Line2D.Double(px, y + 4, px, y + h));
        g.setColor(c(0x2a2420));
        for (double by : new double[]{y + 48, y + 112}) g.fill(new Rectangle2D.Double(x, by, w, 4));
        g.fill(new Ellipse2D.Double(x + w - 14, y + 80, 6, 6));
    }

    static void chest(Graphics2D g) {
        double x = 512, y = 274, w = 92, h = 52;
        g.setColor(c(0x1a0e05, 90));
        g.fill(new Ellipse2D.Double(x - 6, y + h - 4, w + 12, 12));
        g.setPaint(new GradientPaint(0, (float) y, c(0x6a4424), 0, (float) (y + h), c(0x3a2412)));
        g.fill(new Rectangle2D.Double(x, y, w, h));
        g.setPaint(new GradientPaint(0, (float) y - 18, c(0x7a5230), 0, (float) y, c(0x4a2c16)));
        g.fill(new Arc2D.Double(x, y - 18, w, 36, 0, 180, Arc2D.CHORD));
        g.setColor(c(0x3a3a40));
        for (double bx : new double[]{x + 8, x + w - 16}) {
            g.fill(new Rectangle2D.Double(bx, y - 16, 8, h + 16));
        }
        g.fill(new Rectangle2D.Double(x, y + 18, w, 5));
        g.setColor(c(0x8a8a90));
        g.fill(new RoundRectangle2D.Double(x + w / 2 - 7, y - 2, 14, 16, 3, 3));
        g.setColor(c(0x1a1a1a));
        g.fill(new Rectangle2D.Double(x + w / 2 - 1, y + 3, 2, 6));
    }

    static void chestOpen(Graphics2D g) {
        // the lid thrown back and the inside of the chest: a leather pouch and the brass seal matrix
        double x = 512, y = 274, w = 92;
        g.setColor(c(0x1a0e05));
        g.fill(new Arc2D.Double(x, y - 18, w, 36, 0, 180, Arc2D.CHORD));
        g.setPaint(new GradientPaint(0, (float) y - 56, c(0x7a5230), 0, (float) y - 18, c(0x4a2c16)));
        g.fill(poly(x + 2, y - 4, x + w - 2, y - 4, x + w - 6, y - 50, x + 6, y - 50));
        g.setColor(c(0x3a3a40));
        g.fill(new Rectangle2D.Double(x + 10, y - 50, 8, 46));
        g.fill(new Rectangle2D.Double(x + w - 18, y - 50, 8, 46));
        g.setColor(c(0x5a3a1e));
        g.fill(new Ellipse2D.Double(x + 24, y - 10, 20, 9));
    }

    static void lectern(Graphics2D g) {
        // a reading desk by the window, with an open account book
        double x = 452, y = 214;
        g.setColor(c(0x4a2c16));
        g.fill(new Rectangle2D.Double(x + 14, y + 10, 6, 60));
        g.fill(new Rectangle2D.Double(x + 4, y + 66, 26, 5));
        g.setPaint(new GradientPaint(0, (float) y, c(0x7a5230), 0, (float) y + 14, c(0x4a2c16)));
        g.fill(poly(x - 4, y + 14, x + 38, y + 14, x + 34, y, x, y - 4));
        g.setColor(c(0xe9dcb8));
        g.fill(poly(x + 2, y + 10, x + 32, y + 10, x + 30, y + 1, x + 3, y - 1));
        g.setColor(c(0x6a5a4a, 140));
        for (int i = 0; i < 3; i++) g.draw(new Line2D.Double(x + 5, y + 3 + i * 2.2, x + 28, y + 3.5 + i * 2.2));
    }

    // ---- in front ----

    static void table(Graphics2D g) {
        double x = 230, y = 276, w = 204;
        g.setColor(c(0x1a0e05, 100));
        g.fill(new Ellipse2D.Double(x - 6, y + 66, w + 12, 14));
        g.setColor(c(0x3a2210));
        g.fill(new Rectangle2D.Double(x + 10, y + 6, 10, 66));
        g.fill(new Rectangle2D.Double(x + w - 20, y + 6, 10, 66));
        g.setPaint(new GradientPaint(0, (float) y - 10, c(0x8a5a32), 0, (float) y + 6, c(0x5a3a1e)));
        g.fill(poly(x + 10, y - 10, x + w - 10, y - 10, x + w, y + 2, x, y + 2));
        g.setColor(c(0x4a2c16));
        g.fill(new Rectangle2D.Double(x, y + 2, w, 8));
        // account books, an inkwell with a quill, two candlesticks, bread and a jug
        g.setColor(c(0x5a2a1a));
        g.fill(poly(x + 16, y - 6, x + 54, y - 7, x + 56, y, x + 14, y));
        g.setColor(c(0xe9dcb8));
        g.fill(new Rectangle2D.Double(x + 16, y - 3, 38, 2));
        g.setColor(c(0x2a2a3a));
        g.fill(new Ellipse2D.Double(x + 62, y - 6, 8, 6));
        g.setColor(c(0xe8e0d0));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new Line2D.Double(x + 66, y - 4, x + 74, y - 20));
        g.setStroke(new BasicStroke(1f));
        for (double cx : new double[]{x + 82, x + 122}) {
            g.setColor(c(0xa08040));
            g.fill(new Ellipse2D.Double(cx - 5, y - 4, 10, 4));
            g.fill(new Rectangle2D.Double(cx - 1.5, y - 14, 3, 12));
            g.setColor(c(0xf2ead0));
            g.fill(new Rectangle2D.Double(cx - 2, y - 24, 4, 11));
        }
        g.setColor(c(0xc89858));
        g.fill(new Ellipse2D.Double(x + 140, y - 8, 22, 8));
        g.setColor(c(0x8a6a4a));
        g.fill(poly(x + 176, y - 2, x + 174, y - 18, x + 178, y - 22, x + 186, y - 22, x + 190, y - 18, x + 188, y - 2));
    }
}
