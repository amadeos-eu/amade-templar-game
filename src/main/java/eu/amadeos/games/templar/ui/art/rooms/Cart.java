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
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

import static eu.amadeos.games.templar.ui.art.Brush.c;
import static eu.amadeos.games.templar.ui.art.Brush.poly;

/**
 * The Rue du Temple at dawn on Friday, 13 October 1307: the wall and the great tower of the Temple against a
 * pale sky, the King's lilies on the gate, timber houses with their shutters still closed, the prison cart
 * waiting for its horse, and a carter with a barrow of cabbages who is not a carter.
 */
public final class Cart extends RoomArt {
    static final double GROUND = 262;
    static final double BED = 300;

    @Override
    public String id() {
        return "cart";
    }

    @Override
    public Color tint() {
        return new Color(228, 214, 228);
    }

    @Override
    protected void paint(BufferedImage img, Graphics2D g) {
        sky(g, new Random(13));
        ink(img, Cart::tower);
        ink(img, l -> wall(l, new Random(1307)));
        ink(img, l -> houses(l, new Random(10)));
        street(g, new Random(7));
        ink(img, Cart::barrow);
        ink(img, Cart::cageBack);
        Graphics2D v = (Graphics2D) g.create();
        Brush.pool(v, 398, 330, 120, 36, c(0xffb860, 40));
        Workshop.vignette(v);
        v.dispose();
    }

    @Override
    protected Map<String, Consumer<Graphics2D>> layers() {
        return named("bars", (Consumer<Graphics2D>) Cart::cageFront);
    }

    @Override
    public void light(Graphics2D g, double time) {
        Lights.lamp(g, 396, 262, 110, c(0xffb860), 40);
    }

    // ---- the dawn and the Temple ----

    static void sky(Graphics2D g, Random r) {
        g.setPaint(new GradientPaint(0, 0, c(0x1e2448), 0, 150, c(0x9a7890)));
        g.fill(new Rectangle2D.Double(0, 0, W, 150));
        g.setPaint(new GradientPaint(0, 150, c(0x9a7890), 0, (float) GROUND, c(0xf0c49a)));
        g.fill(new Rectangle2D.Double(0, 150, W, GROUND - 150));
        for (int i = 0; i < 24; i++) {
            g.setColor(c(0xeef0ff, 40 + r.nextInt(80)));
            double d = 0.6 + r.nextDouble() * 0.7;
            g.fill(new Ellipse2D.Double(r.nextInt(W), r.nextInt(60), d, d));
        }
        // long thin clouds, lit pink from below by a sun not yet up
        for (int i = 0; i < 7; i++) {
            double x = r.nextInt(W) - 60, y = 40 + r.nextInt(100);
            g.setColor(c(0xf8c0b0, 60 + r.nextInt(40)));
            g.fill(new Ellipse2D.Double(x, y, 90 + r.nextInt(120), 5 + r.nextInt(4)));
        }
        Brush.glow(g, 520, GROUND, 160, c(0xffd8a0, 70));
    }

    static void tower(Graphics2D g) {
        // the great tower of the Temple, as in the courtyard, seen now from the street over the wall
        double x = 170, base = 160, w = 96;
        g.setPaint(new GradientPaint((float) x, 0, c(0x6a6070), (float) (x + w), 0, c(0x3a3444)));
        g.fill(new Rectangle2D.Double(x, 40, w, base - 40));
        for (double cx : new double[]{x - 12, x + w - 6}) {
            g.setPaint(new GradientPaint((float) cx, 0, c(0x746a7a), (float) cx + 18, 0, c(0x40384a)));
            g.fill(new Rectangle2D.Double(cx, 24, 18, base - 24));
            g.setColor(c(0x2e2838));
            g.fill(poly(cx - 3, 26, cx + 9, -6, cx + 21, 26));
        }
        g.setColor(c(0x2e2838));
        g.fill(poly(x - 2, 42, x + w / 2, 10, x + w + 2, 42));
        g.setColor(c(0x4a4252));
        for (double cx = x + 2; cx < x + w; cx += 9) g.fill(new Rectangle2D.Double(cx, 37, 5, 5));
        g.setColor(c(0x1e1a24));
        for (double[] s : new double[][]{{x + 26, 70}, {x + 64, 96}, {x + 44, 126}}) {
            g.fill(new Rectangle2D.Double(s[0], s[1], 3, 9));
        }
    }

    static void wall(Graphics2D g, Random r) {
        // the Temple's enclosure wall with its battlements, and the gate, shut, under the King's banner
        g.setPaint(new GradientPaint(0, 150, c(0x8a7a6e), 0, (float) GROUND, c(0x5a4e46)));
        g.fill(new Rectangle2D.Double(0, 150, 340, GROUND - 150));
        g.setColor(c(0x7a6a5e));
        for (double cx = 0; cx < 340; cx += 20) g.fill(new Rectangle2D.Double(cx, 140, 12, 12));
        Chapter.ashlar(g, 0, 154, 340, GROUND - 154, r, 0.1);
        // the gatehouse
        g.setPaint(new GradientPaint(40, 0, c(0x8e7e70), 160, 0, c(0x5a4e46)));
        g.fill(new Rectangle2D.Double(40, 128, 120, GROUND - 128));
        g.setColor(c(0x7e6e62));
        for (double cx = 38; cx < 162; cx += 16) g.fill(new Rectangle2D.Double(cx, 118, 10, 12));
        g.setColor(c(0x2a2018));
        g.fill(poly(66, GROUND, 66, 200, 100, 172, 134, 200, 134, GROUND));
        g.setPaint(new GradientPaint(66, 0, c(0x5a3a22), 134, 0, c(0x3a2414)));
        g.fill(poly(70, GROUND, 70, 202, 100, 177, 130, 202, 130, GROUND));
        g.setColor(c(0x2a1a0e));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new Line2D.Double(100, 178, 100, GROUND));
        for (double y = 212; y < GROUND; y += 16) g.draw(new Line2D.Double(70, y, 130, y));
        g.setColor(c(0x8a8e96));
        for (double y : new double[]{214, 246}) for (double x : new double[]{78, 122}) g.fill(new Ellipse2D.Double(x - 1.5, y - 1.5, 3, 3));
        // the banner of the King: gold lilies on blue, hung over the gate this morning
        g.setColor(c(0x22388a));
        g.fill(poly(84, 132, 116, 132, 116, 166, 100, 158, 84, 166));
        g.setColor(c(0xe8c860));
        for (double[] p : new double[][]{{92, 140}, {108, 140}, {100, 150}}) lily(g, p[0], p[1]);
        g.setStroke(new BasicStroke(1f));
    }

    static void lily(Graphics2D g, double x, double y) {
        g.fill(poly(x, y - 3, x + 1.2, y, x, y + 1.6, x - 1.2, y));
        g.fill(new Ellipse2D.Double(x - 3, y - 1, 2.2, 2.2));
        g.fill(new Ellipse2D.Double(x + 0.8, y - 1, 2.2, 2.2));
        g.fill(new Rectangle2D.Double(x - 2.4, y + 1.4, 4.8, 0.9));
    }

    static void houses(Graphics2D g, Random r) {
        // timber-framed houses with jettied upper floors, the shutters still shut
        double x = 344;
        while (x < W) {
            double w = 70 + r.nextInt(30), top = 60 + r.nextInt(40);
            Color plaster = Brush.mix(c(0xd8c8aa), c(0xc0a890), r.nextDouble());
            g.setColor(Brush.darker(plaster, 0.1));
            g.fill(new Rectangle2D.Double(x, top + 70, w, GROUND - top - 70));
            g.setColor(plaster);
            g.fill(new Rectangle2D.Double(x - 4, top, w + 8, 72));
            g.setColor(c(0x4a3020));
            g.fill(poly(x - 10, top + 2, x + w / 2, top - 46, x + w + 10, top + 2));
            g.setColor(c(0x5a3a24));
            g.setStroke(new BasicStroke(2.4f));
            g.draw(new Rectangle2D.Double(x - 4, top, w + 8, 72));
            g.draw(new Line2D.Double(x - 4, top + 36, x + w + 4, top + 36));
            for (double bx = x + 8; bx < x + w; bx += 22) {
                g.draw(new Line2D.Double(bx, top, bx, top + 72));
                g.draw(new Line2D.Double(bx, top + 36, bx + 11, top));
            }
            g.draw(new Line2D.Double(x, top + 72, x, GROUND));
            g.draw(new Line2D.Double(x + w, top + 72, x + w, GROUND));
            g.setStroke(new BasicStroke(1f));
            // shutters, one window lit
            for (double wx = x + 12; wx < x + w - 16; wx += 26) {
                boolean lit = r.nextInt(5) == 0;
                g.setColor(lit ? c(0xffc860) : c(0x3a4a3a));
                g.fill(new Rectangle2D.Double(wx, top + 44, 12, 16));
                if (lit) Brush.glow(g, wx + 6, top + 52, 16, c(0xffc060, 50));
            }
            // the shop front below, its board closed
            g.setColor(c(0x5a3a24));
            g.fill(new Rectangle2D.Double(x + 10, GROUND - 46, w - 20, 40));
            g.setColor(c(0x3a2414));
            for (double sx = x + 14; sx < x + w - 12; sx += 8) g.fill(new Rectangle2D.Double(sx, GROUND - 44, 1, 36));
            x += w + 12;
        }
    }

    static void street(Graphics2D g, Random r) {
        g.setPaint(new GradientPaint(0, (float) GROUND, c(0x5a4e44), 0, H, c(0x8a7a66)));
        g.fill(new Rectangle2D.Double(0, GROUND, W, H - GROUND));
        // cobbles, mud between them, the gutter down the middle
        for (double y = GROUND + 4; y < H; y += 5 + (y - GROUND) * 0.06) {
            double step = 8 + (y - GROUND) * 0.12;
            for (double x = r.nextDouble() * step; x < W; x += step) {
                g.setColor(Brush.c(Brush.mix(c(0x6a5e52), c(0x9a8a76), r.nextDouble()).getRGB() & 0xffffff, 150));
                g.fill(new Ellipse2D.Double(x, y, step * 0.8, 2 + (y - GROUND) * 0.03));
            }
        }
        g.setColor(c(0x3a3830, 120));
        g.fill(poly(0, 360, W, 340, W, 346, 0, 368));
        Brush.pool(g, 520, GROUND + 6, 180, 20, c(0xffd8a0, 50));
    }

    static void barrow(Graphics2D g) {
        // Raoul's barrow of cabbages, on the right
        double x = 570, y = 338;
        g.setColor(c(0x1a0e05, 80));
        g.fill(new Ellipse2D.Double(x - 8, y - 4, 80, 10));
        g.setPaint(new GradientPaint(0, (float) y - 30, c(0x7a5a3a), 0, (float) y, c(0x4a3420)));
        g.fill(poly(x, y - 34, x + 70, y - 34, x + 64, y - 12, x + 6, y - 12));
        g.setColor(c(0x3a2414));
        g.setStroke(new BasicStroke(2.2f));
        g.draw(new Line2D.Double(x + 6, y - 12, x + 10, y));
        g.draw(new Line2D.Double(x + 64, y - 12, x + 60, y));
        g.draw(new Line2D.Double(x, y - 30, x - 26, y - 20));
        g.setStroke(new BasicStroke(1f));
        Random r = new Random(3);
        for (int i = 0; i < 9; i++) {
            double cx = x + 8 + (i % 5) * 12 + r.nextInt(4), cy = y - 40 - (i / 5) * 8 + r.nextInt(3);
            g.setPaint(new GradientPaint((float) cx - 6, (float) cy - 6, c(0x9ac46a), (float) cx + 6, (float) cy + 6, c(0x4a7a2e)));
            g.fill(new Ellipse2D.Double(cx - 7, cy - 6, 14, 12));
            g.setColor(c(0x3a6a24));
            g.draw(new Arc2D.Double(cx - 5, cy - 4, 10, 8, 30, 120, Arc2D.OPEN));
        }
    }

    // ---- the prison cart ----

    static void cageBack(Graphics2D g) {
        // the far side of the cage and the floor of the cart, behind the prisoners
        g.setColor(c(0x3a2818));
        g.setStroke(new BasicStroke(3f));
        g.draw(new Line2D.Double(184, 190, 334, 190));
        for (double x = 186; x <= 334; x += 16) g.draw(new Line2D.Double(x, 190, x, BED - 2));
        g.setStroke(new BasicStroke(1f));
        g.setPaint(new GradientPaint(0, (float) BED - 6, c(0x6a4a2e), 0, (float) BED + 4, c(0x4a3420)));
        g.fill(poly(172, BED + 6, 330, BED + 6, 338, BED - 6, 180, BED - 6));
    }

    static void cageFront(Graphics2D g) {
        // the near side: the plank side of the cart, the bars, the near wheel and the shafts on the ground
        g.setColor(c(0x1a0e05, 90));
        g.fill(new Ellipse2D.Double(160, 352, 220, 18));
        g.setPaint(new GradientPaint(0, (float) BED, c(0x7a5634), 0, (float) BED + 22, c(0x4a3420)));
        g.fill(new Rectangle2D.Double(168, BED, 164, 22));
        g.setColor(c(0x3a2414));
        g.setStroke(new BasicStroke(0.8f));
        for (double y = BED + 7; y < BED + 22; y += 7) g.draw(new Line2D.Double(168, y, 332, y));
        g.setStroke(new BasicStroke(3.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(c(0x4a3220));
        g.draw(new Line2D.Double(172, 198, 330, 198));
        for (double x = 174; x <= 330; x += 17.5) g.draw(new Line2D.Double(x, 198, x, BED));
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(c(0x5a3c24));
        g.draw(new Line2D.Double(330, BED + 14, 470, 352));
        // the near wheel
        double cx = 300, cy = 330, rr = 32;
        g.setColor(c(0x3a2414));
        g.setStroke(new BasicStroke(5.5f));
        g.draw(new Ellipse2D.Double(cx - rr, cy - rr, rr * 2, rr * 2));
        g.setStroke(new BasicStroke(2.4f));
        for (int i = 0; i < 6; i++) {
            double a = i * Math.PI / 3 + 0.3;
            g.draw(new Line2D.Double(cx, cy, cx + Math.cos(a) * rr, cy + Math.sin(a) * rr));
        }
        g.setColor(c(0x5a3c24));
        g.fill(new Ellipse2D.Double(cx - 6, cy - 6, 12, 12));
        g.setColor(c(0x8a8e96));
        g.fill(new Ellipse2D.Double(cx - 2.5, cy - 2.5, 5, 5));
        g.setStroke(new BasicStroke(1f));
    }
}
