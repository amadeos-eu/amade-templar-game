package eu.amadeos.games.templar.ui.art.rooms;

import eu.amadeos.games.templar.ui.art.Brush;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
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
 * The Square du Temple at dusk, where the enclosure of the Templars stood: the houses of the Marais behind the
 * railing, chestnut trees, a playground, the information board with the old engraving of the tower, a bench,
 * and the steps down to the metro on the right.
 */
public final class Square extends RoomArt {
    static final double GROUND = 262;

    @Override
    public String id() {
        return "square";
    }

    @Override
    public Color tint() {
        return new Color(246, 226, 222);
    }

    @Override
    protected void paint(BufferedImage img, Graphics2D g) {
        sky(g);
        houses(g, new Random(1808));
        ground(g);
        ink(img, Square::railing);
        ink(img, Square::playground);
        ink(img, l -> trees(l, new Random(3)));
        ink(img, Square::board);
        ink(img, Square::lamppost);
        ink(img, Square::metro);
        Graphics2D v = (Graphics2D) g.create();
        Workshop.vignette(v);
        v.dispose();
    }

    @Override
    protected Map<String, Consumer<Graphics2D>> layers() {
        return named("bench", (Consumer<Graphics2D>) Square::bench);
    }

    @Override
    protected Map<String, Prop> props() {
        return Map.of(
                "kev-sandwich", new Prop(l -> sandwich(l, 372, 301, 1), 340),
                "half-sandwich", new Prop(l -> sandwich(l, 286, 350, 0.6), 352));
    }

    @Override
    public void light(Graphics2D g, double time) {
        Lights.lamp(g, 204, 128, 90, c(0xffd890), 70);
        Lights.lamp(g, 600, 150, 50, c(0xffe0a0), 50);
    }

    // ---- the town ----

    static void sky(Graphics2D g) {
        g.setPaint(new GradientPaint(0, 0, c(0x34407a), 0, 210, c(0xe89a74)));
        g.fill(new Rectangle2D.Double(0, 0, W, 240));
        Random r = new Random(5);
        g.setColor(c(0xf0d0c0, 70));
        for (int i = 0; i < 6; i++) {
            double x = r.nextInt(W), y = 30 + r.nextInt(90);
            g.fill(new Ellipse2D.Double(x, y, 80 + r.nextInt(90), 6 + r.nextInt(5)));
        }
    }

    static void houses(Graphics2D g, Random r) {
        double x = -20;
        while (x < W) {
            double w = 70 + r.nextInt(50), top = 70 + r.nextInt(40);
            Color stone = Brush.mix(c(0xd8c4a4), c(0xb8a0a8), r.nextDouble() * 0.6);
            g.setPaint(new GradientPaint(0, (float) top, Brush.lighter(stone, 0.05), 0, 250, Brush.darker(stone, 0.2)));
            g.fill(new Rectangle2D.Double(x, top, w, 250 - top));
            // the zinc mansard roof with its dormers and chimneys
            g.setColor(c(0x5c6478));
            g.fill(poly(x - 2, top, x + 8, top - 22, x + w - 8, top - 22, x + w + 2, top));
            g.setColor(c(0x7a5a4a));
            g.fill(new Rectangle2D.Double(x + 12, top - 34, 10, 14));
            g.fill(new Rectangle2D.Double(x + w - 26, top - 30, 8, 10));
            for (double dx = x + 16; dx < x + w - 14; dx += 22) {
                g.setColor(c(0x4a5264));
                g.fill(new Rectangle2D.Double(dx, top - 16, 10, 12));
                g.setColor(r.nextInt(3) == 0 ? c(0xffd27a) : c(0x2a3048));
                g.fill(new Rectangle2D.Double(dx + 2, top - 13, 6, 8));
            }
            // windows, some lit, and the iron balcony of the second floor
            for (double wy = top + 14; wy < 226; wy += 30) {
                for (double wx = x + 10; wx < x + w - 14; wx += 20) {
                    boolean lit = r.nextInt(4) == 0;
                    g.setColor(lit ? c(0xffcc70) : c(0x3a3a58));
                    g.fill(new Rectangle2D.Double(wx, wy, 9, 18));
                    if (lit) Brush.glow(g, wx + 4.5, wy + 9, 14, c(0xffc060, 40));
                    g.setColor(c(0x8a7a6a));
                    g.fill(new Rectangle2D.Double(wx - 1, wy + 18, 11, 2));
                }
            }
            g.setColor(c(0x2a2a30));
            g.fill(new Rectangle2D.Double(x + 4, top + 44, w - 8, 1.5));
            for (double bx = x + 6; bx < x + w - 6; bx += 3) g.fill(new Rectangle2D.Double(bx, top + 44, 0.6, 7));
            g.setColor(c(0x3a2a2a, 60));
            g.fill(new Rectangle2D.Double(x + w - 2, top, 2, 250 - top));
            x += w;
        }
        // the street behind the railing
        g.setColor(c(0x4a4850));
        g.fill(new Rectangle2D.Double(0, 236, W, 20));
    }

    static void ground(Graphics2D g) {
        g.setPaint(new GradientPaint(0, (float) GROUND - 8, c(0x9a8a6a), 0, H, c(0xc8b088)));
        g.fill(new Rectangle2D.Double(0, GROUND - 8, W, H - GROUND + 8));
        // lawns on both sides, behind little hoop fences
        g.setPaint(new GradientPaint(0, (float) GROUND, c(0x3a5a2a), 0, 300, c(0x4e7a34)));
        g.fill(poly(0, GROUND - 6, 150, GROUND - 6, 120, 296, 0, 312));
        g.fill(poly(430, GROUND - 6, 640, GROUND - 6, 640, 300, 470, 290));
        g.setColor(c(0x2a2a24));
        g.setStroke(new BasicStroke(1f));
        for (double hx = 4; hx < 120; hx += 12) g.draw(new java.awt.geom.Arc2D.Double(hx, 296 - hx * 0.1, 12, 10, 0, 180, java.awt.geom.Arc2D.OPEN));
        // gravel
        Random r = new Random(8);
        for (int i = 0; i < 900; i++) {
            double x = r.nextInt(W), y = GROUND + r.nextDouble() * (H - GROUND);
            g.setColor(r.nextBoolean() ? c(0xe8d8b0, 70) : c(0x6a5a40, 60));
            g.fill(new Ellipse2D.Double(x, y, 1.4, 0.9));
        }
    }

    static void railing(Graphics2D g) {
        g.setColor(c(0x1e2a24));
        g.fill(new Rectangle2D.Double(0, 228, W, 2.5));
        g.fill(new Rectangle2D.Double(0, 252, W, 2.5));
        for (double x = 2; x < W; x += 7) {
            g.fill(new Rectangle2D.Double(x, 222, 1.6, 34));
            g.fill(poly(x - 1, 223, x + 0.8, 218, x + 2.6, 223));
        }
        for (double x = 0; x < W; x += 112) {
            g.fill(new Rectangle2D.Double(x, 214, 5, 44));
            g.fill(new Ellipse2D.Double(x - 1, 209, 7, 7));
        }
    }

    static void playground(Graphics2D g) {
        // a climbing frame and a slide, red and yellow, on the lawn at the back
        g.setColor(c(0xc83a2a));
        g.setStroke(new BasicStroke(3f));
        g.draw(poly(470, 262, 488, 206, 506, 262));
        g.draw(poly(520, 262, 538, 206, 556, 262));
        g.draw(new Line2D.Double(488, 206, 538, 206));
        g.setColor(c(0xe8b830));
        g.setStroke(new BasicStroke(2f));
        for (int i = 1; i < 4; i++) g.draw(new Line2D.Double(470 + i * 4.5, 262 - i * 14, 506 - i * 4.5, 262 - i * 14));
        g.setColor(c(0x2a7aa8));
        g.fill(poly(556, 230, 562, 228, 600, 268, 594, 270));
        g.setColor(c(0x5a4030));
        g.fill(new Rectangle2D.Double(548, 228, 12, 4));
        g.setStroke(new BasicStroke(1f));
    }

    static void trees(Graphics2D g, Random r) {
        for (double[] t : new double[][]{{30, 250, 1.2}, {612, 246, 1.1}, {372, 240, 0.8}}) {
            double x = t[0], foot = t[1], k = t[2];
            g.setPaint(new GradientPaint((float) x - 6, 0, c(0x5a4434), (float) x + 8, 0, c(0x2e2218)));
            g.fill(poly(x - 7 * k, foot, x - 4 * k, foot - 120 * k, x + 4 * k, foot - 120 * k, x + 8 * k, foot));
            for (int i = 0; i < 26; i++) {
                double a = r.nextDouble() * Math.PI * 2, d = r.nextDouble() * 60 * k;
                double cx = x + Math.cos(a) * d * 1.4, cy = foot - 150 * k + Math.sin(a) * d * 0.8;
                double s = (22 + r.nextInt(18)) * k;
                g.setColor(Brush.mix(c(0x2a4a2a), c(0x6a8a4a), r.nextDouble() * 0.5));
                g.fill(new Ellipse2D.Double(cx - s / 2, cy - s / 2, s, s * 0.85));
                g.setColor(c(0xf0a070, 40));
                g.fill(new Ellipse2D.Double(cx - s / 2 + 2, cy - s / 2 + 1, s * 0.6, s * 0.4));
            }
        }
    }

    static void board(Graphics2D g) {
        // the information board of the city of Paris, with the engraving of the Temple tower
        double x = 64, y = 168, w = 84, h = 70;
        g.setColor(c(0x1e3a2e));
        g.fill(new Rectangle2D.Double(x + 8, y + h, 5, 50));
        g.fill(new Rectangle2D.Double(x + w - 13, y + h, 5, 50));
        g.fill(new RoundRectangle2D.Double(x - 4, y - 4, w + 8, h + 8, 4, 4));
        g.setColor(c(0xe8dcc0));
        g.fill(new Rectangle2D.Double(x, y, w, h));
        g.setColor(c(0x2a3a5a));
        g.fill(new Rectangle2D.Double(x, y, w, 10));
        g.setColor(c(0xf2ead8));
        g.setFont(new Font(Font.SERIF, Font.BOLD, 6));
        g.drawString("HISTOIRE DE PARIS", (float) x + 14, (float) y + 7.5f);
        // the engraving: the keep with four corner turrets
        double ex = x + 6, ey = y + 14;
        g.setColor(c(0xc8b48a));
        g.fill(new Rectangle2D.Double(ex, ey, 40, 50));
        g.setColor(c(0x5a4428));
        double tx = ex + 12, base = ey + 46;
        g.fill(new Rectangle2D.Double(tx, base - 30, 16, 30));
        g.fill(new Rectangle2D.Double(tx - 3, base - 33, 4, 33));
        g.fill(new Rectangle2D.Double(tx + 15, base - 33, 4, 33));
        g.fill(poly(tx - 4, base - 33, tx - 1, base - 40, tx + 2, base - 33));
        g.fill(poly(tx + 14, base - 33, tx + 17, base - 40, tx + 20, base - 33));
        g.fill(poly(tx, base - 30, tx + 8, base - 40, tx + 16, base - 30));
        g.fill(new Rectangle2D.Double(tx + 20, base - 18, 8, 18));
        g.setColor(c(0x5a4428, 120));
        for (double hy = ey + 4; hy < ey + 48; hy += 2) g.draw(new Line2D.Double(ex + 1, hy, ex + 10, hy));
        // lines of text on the right
        g.setColor(c(0x6a6050));
        for (int i = 0; i < 9; i++) g.fill(new Rectangle2D.Double(x + 50, y + 16 + i * 5.5, 28 - (i % 3) * 4, 1.4));
    }

    static void lamppost(Graphics2D g) {
        double x = 204;
        g.setColor(c(0x1e2a24));
        g.fill(poly(x - 5, 290, x + 5, 290, x + 2.5, 270, x + 1.6, 140, x - 1.6, 140, x - 2.5, 270));
        g.fill(new Rectangle2D.Double(x - 6, 284, 12, 6));
        g.fill(poly(x - 9, 136, x + 9, 136, x + 6, 112, x - 6, 112));
        g.fill(poly(x - 8, 112, x, 102, x + 8, 112));
        g.setColor(c(0xffe8a8));
        g.fill(poly(x - 7, 133, x + 7, 133, x + 4.8, 115, x - 4.8, 115));
    }

    static void metro(Graphics2D g) {
        // the steps down to the metro: a railing around the stairwell and the lit sign on its pole
        g.setColor(c(0x3a3630));
        g.fill(poly(560, 296, 640, 290, 640, 330, 568, 330));
        g.setColor(c(0x2a2622));
        for (int i = 0; i < 5; i++) g.fill(new Rectangle2D.Double(566 + i * 2, 300 + i * 6, 74 - i * 2, 2));
        g.setColor(c(0x1e2a24));
        g.setStroke(new BasicStroke(2.2f));
        g.draw(new Line2D.Double(556, 276, 640, 270));
        g.draw(new Line2D.Double(556, 276, 560, 300));
        for (double x = 562; x < 640; x += 10) g.draw(new Line2D.Double(x, 276 - (x - 556) * 0.07, x, 296 - (x - 556) * 0.07));
        g.setStroke(new BasicStroke(1f));
        g.fill(new Rectangle2D.Double(598, 160, 4, 116));
        g.setColor(c(0xe8d8a0));
        g.fill(new RoundRectangle2D.Double(578, 140, 44, 20, 4, 4));
        g.setColor(c(0x1e3a2e));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 8));
        g.drawString("METRO", 586, 151);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 5));
        g.drawString("Temple", 591, 157.5f);
    }

    // ---- in front ----

    static void bench(Graphics2D g) {
        double x = 300, y = 300, w = 104;
        g.setColor(c(0x1a0e05, 80));
        g.fill(new Ellipse2D.Double(x - 4, y + 28, w + 8, 10));
        g.setColor(c(0x1e2a24));
        g.fill(new Rectangle2D.Double(x + 6, y + 8, 4, 26));
        g.fill(new Rectangle2D.Double(x + w - 10, y + 8, 4, 26));
        g.fill(new Rectangle2D.Double(x + 4, y - 22, 3, 30));
        g.fill(new Rectangle2D.Double(x + w - 7, y - 22, 3, 30));
        for (int i = 0; i < 3; i++) {
            g.setPaint(new GradientPaint(0, (float) (y - 20 + i * 6), c(0x3a6a3a), 0, (float) (y - 16 + i * 6), c(0x24482a)));
            g.fill(new Rectangle2D.Double(x, y - 20 + i * 6, w, 4));
        }
        for (int i = 0; i < 3; i++) {
            g.setPaint(new GradientPaint(0, (float) (y + i * 3), c(0x4a7a44), 0, (float) (y + 3 + i * 3), c(0x2a502e)));
            g.fill(new Rectangle2D.Double(x - 2 + i, y + i * 3, w + 4 - 2 * i, 2.6));
        }
    }

    static void sandwich(Graphics2D g, double x, double y, double k) {
        Path2D bread = poly(x - 12 * k, y, x - 10 * k, y - 5 * k, x + 10 * k, y - 5 * k, x + 12 * k, y);
        g.setPaint(new GradientPaint(0, (float) (y - 5 * k), c(0xe8b870), 0, (float) y, c(0xb07a3a)));
        g.fill(bread);
        g.setColor(c(0x7aa04a));
        g.fill(new Rectangle2D.Double(x - 10 * k, y - 2.2 * k, 20 * k, 1.2 * k));
        g.setColor(c(0xd85a4a));
        g.fill(new Rectangle2D.Double(x - 8 * k, y - 3.2 * k, 16 * k, 1 * k));
        g.setColor(c(0xf0e2c0));
        g.fill(new RoundRectangle2D.Double(x - 14 * k, y, 28 * k, 2 * k, 2, 2));
    }
}
