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
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

import static eu.amadeos.games.templar.ui.art.Brush.c;
import static eu.amadeos.games.templar.ui.art.Brush.poly;

/**
 * The courtyard of the Paris Temple in the night before 13 October 1307: the great tower against the stars,
 * the cloister on the left, the library door, the well, and the gate to the street with its torch and guard.
 */
public final class Courtyard extends RoomArt {
    static final double GROUND = 268;

    @Override
    public String id() {
        return "courtyard";
    }

    @Override
    public Color tint() {
        return new Color(206, 206, 232);
    }

    @Override
    protected void paint(BufferedImage img, Graphics2D g) {
        sky(g, new Random(1307));
        ink(img, Courtyard::tower);
        ink(img, l -> walls(l, new Random(4)));
        ink(img, Courtyard::cloister);
        ink(img, Courtyard::libraryDoor);
        ink(img, Courtyard::chapterDoor);
        ink(img, Courtyard::gate);
        ground(g, new Random(9));
        Graphics2D v = (Graphics2D) g.create();
        Brush.pool(v, 560, 300, 160, 60, c(0xffb860, 40));
        Workshop.vignette(v);
        v.dispose();
    }

    @Override
    protected Map<String, Consumer<Graphics2D>> layers() {
        return named("well", (Consumer<Graphics2D>) Courtyard::well);
    }

    @Override
    public void light(Graphics2D g, double time) {
        Lights.flame(g, 528, 176, time, 1.8);
        Lights.flame(g, 92, 190, time, 1.3);
        Lights.lamp(g, 528, 190, 140, c(0xffb860), 46);
    }

    // ---- the night ----

    static void sky(Graphics2D g, Random r) {
        g.setPaint(new GradientPaint(0, 0, c(0x0a0e24), 0, 220, c(0x2a2c4c)));
        g.fill(new Rectangle2D.Double(0, 0, W, GROUND));
        for (int i = 0; i < 90; i++) {
            g.setColor(c(0xeef0ff, 50 + r.nextInt(120)));
            double d = 0.6 + r.nextDouble() * 0.9;
            g.fill(new Ellipse2D.Double(r.nextInt(W), r.nextInt(150), d, d));
        }
        Brush.glow(g, 90, 46, 40, c(0xd8e0ff, 50));
        g.setColor(c(0xeef2ff));
        g.fill(new Ellipse2D.Double(83, 39, 14, 14));
    }

    static void tower(Graphics2D g) {
        // the keep of the Temple: square, four round corner turrets with pointed roofs, a few lit slits
        double x = 236, base = 210, w = 120;
        g.setPaint(new GradientPaint((float) x, 0, c(0x4a4658), (float) (x + w), 0, c(0x242232)));
        g.fill(new Rectangle2D.Double(x, 40, w, base - 40));
        for (double cx : new double[]{x - 14, x + w - 6}) {
            g.setPaint(new GradientPaint((float) cx, 0, c(0x54506a), (float) cx + 20, 0, c(0x2a283a)));
            g.fill(new Rectangle2D.Double(cx, 20, 20, base - 20));
            g.setColor(c(0x1e1c2a));
            g.fill(poly(cx - 3, 22, cx + 10, -14, cx + 23, 22));
        }
        g.setColor(c(0x1e1c2a));
        g.fill(poly(x - 2, 42, x + w / 2, 4, x + w + 2, 42));
        g.setColor(c(0x3a3848));
        for (double cx = x + 2; cx < x + w; cx += 10) g.fill(new Rectangle2D.Double(cx, 36, 6, 6));
        g.setColor(c(0xffc860));
        for (double[] s : new double[][]{{x + 30, 80}, {x + 80, 120}, {x + 52, 150}, {x - 6, 100}}) {
            g.fill(new Rectangle2D.Double(s[0], s[1], 3, 9));
        }
    }

    static void walls(Graphics2D g, Random r) {
        // the buildings round the courtyard: the cloister range on the left, the gatehouse on the right
        g.setPaint(new GradientPaint(0, 150, c(0x5a5048), 0, (float) GROUND, c(0x3a342e)));
        g.fill(poly(0, 120, 200, 140, 200, GROUND, 0, GROUND));
        g.fill(poly(400, 130, 640, 104, 640, GROUND, 400, GROUND));
        g.setColor(c(0x2a2420));
        g.fill(poly(0, 120, 200, 140, 200, 132, 0, 108));
        g.fill(poly(400, 130, 640, 104, 640, 94, 400, 122));
        Chapter.ashlar(g, 400, 140, 240, GROUND - 140, r, 0.3);
        g.setColor(c(0x3a342e));
        g.fill(new Rectangle2D.Double(200, 190, 200, GROUND - 190));
        Chapter.ashlar(g, 200, 194, 200, GROUND - 194, r, 0.3);
    }

    static void cloister(Graphics2D g) {
        // round arches of the cloister walk, dark inside, with a torch
        g.setColor(c(0x1a1614));
        for (double ax = 10; ax < 190; ax += 46) {
            g.fill(new Arc2D.Double(ax, 160, 36, 36, 0, 180, Arc2D.CHORD));
            g.fill(new Rectangle2D.Double(ax, 178, 36, GROUND - 178));
        }
        g.setColor(c(0x6a6050));
        for (double px = 4; px < 200; px += 46) g.fill(new Rectangle2D.Double(px, 172, 6, GROUND - 172));
        g.setColor(c(0x2a2420));
        g.fill(new Rectangle2D.Double(88, 194, 8, 3));
        g.setColor(c(0x5a3a1e));
        g.fill(poly(89, 190, 95, 190, 93, 206, 91, 206));
    }

    static void libraryDoor(Graphics2D g) {
        // the library door in the low range under the tower, with an old lamp niche
        double x = 284, y = 206, w = 36;
        g.setColor(c(0x1e1814));
        g.fill(new Arc2D.Double(x - 5, y - 5, w + 10, 36, 0, 180, Arc2D.CHORD));
        g.fill(new Rectangle2D.Double(x - 5, y + 13, w + 10, GROUND - y - 13));
        g.setPaint(new GradientPaint((float) x, 0, c(0x5a3a1e), (float) (x + w), 0, c(0x2e1e10)));
        g.fill(new Arc2D.Double(x, y, w, 26, 0, 180, Arc2D.CHORD));
        g.fill(new Rectangle2D.Double(x, y + 13, w, GROUND - y - 13));
        g.setColor(c(0x2a2420));
        g.fill(new Ellipse2D.Double(x + w - 9, y + 34, 4, 4));
        g.setColor(c(0xe8d8b0, 140));
        g.fill(new Rectangle2D.Double(x + 6, y - 14, 24, 6));
    }

    static void chapterDoor(Graphics2D g) {
        // the way back into the treasurer's chamber, at the left edge
        g.setColor(c(0x1e1814));
        g.fill(new Rectangle2D.Double(0, 196, 26, GROUND - 196));
        g.setColor(c(0xffb860, 70));
        g.fill(new Rectangle2D.Double(2, 200, 18, GROUND - 200));
    }

    static void gate(Graphics2D g) {
        // the great gate to the street: two heavy leaves under a pointed arch, a bar across, the torch beside it
        double x = 556, y = 150, w = 76, h = GROUND - 150;
        g.setColor(c(0x2a2420));
        g.fill(new Rectangle2D.Double(x - 8, y + 20, w + 16, h - 20));
        Path2D arch = new Path2D.Double();
        arch.moveTo(x - 8, y + 24);
        arch.quadTo(x - 8, y - 14, x + w / 2, y - 24);
        arch.quadTo(x + w + 8, y - 14, x + w + 8, y + 24);
        arch.closePath();
        g.fill(arch);
        g.setPaint(new GradientPaint((float) x, 0, c(0x6a4424), (float) (x + w), 0, c(0x3a2412)));
        Path2D leaves = new Path2D.Double();
        leaves.moveTo(x, y + h);
        leaves.lineTo(x, y + 20);
        leaves.quadTo(x, y - 8, x + w / 2, y - 16);
        leaves.quadTo(x + w, y - 8, x + w, y + 20);
        leaves.lineTo(x + w, y + h);
        leaves.closePath();
        g.fill(leaves);
        g.setColor(c(0x2a1a0c, 150));
        for (double px = x + 8; px < x + w; px += 9) g.draw(new Line2D.Double(px, y - 8, px, y + h));
        g.setColor(c(0x2a1a0c));
        g.fill(new Rectangle2D.Double(x + w / 2 - 1, y - 14, 2, h + 14));
        g.setColor(c(0x3a3a40));
        g.fill(new Rectangle2D.Double(x - 4, y + 60, w + 8, 7));
        for (double by : new double[]{y + 24, y + 92}) g.fill(new Rectangle2D.Double(x, by, w, 3));
        // the torch on the wall to the left of the gate
        g.setColor(c(0x2a2420));
        g.fill(new Rectangle2D.Double(522, 190, 12, 4));
        g.setColor(c(0x5a3a1e));
        g.fill(poly(524, 178, 532, 178, 530, 206, 526, 206));
    }

    static void ground(Graphics2D g, Random r) {
        g.setPaint(new GradientPaint(0, (float) GROUND, c(0x3a3630), 0, H, c(0x6a6050)));
        g.fill(new Rectangle2D.Double(0, GROUND, W, H - GROUND));
        // cobbles
        for (double y = GROUND + 4; y < H; y += 5 + (y - GROUND) * 0.06) {
            double s = 4 + (y - GROUND) * 0.08;
            for (double x = r.nextDouble() * s; x < W; x += s * 1.2) {
                g.setColor(Brush.mix(c(0x7a7060), c(0x4a4238), r.nextDouble()));
                g.fill(new Ellipse2D.Double(x, y, s, s * 0.5));
            }
        }
        g.setColor(c(0x8a7a5a, 60));
        g.fill(poly(250, GROUND, 330, GROUND, 520, H, 180, H));
    }

    // ---- in front ----

    static void well(Graphics2D g) {
        double x = 150, y = 318, w = 76;
        g.setColor(c(0x1a0e05, 100));
        g.fill(new Ellipse2D.Double(x - 6, y + 22, w + 12, 12));
        g.setPaint(new GradientPaint((float) x, 0, c(0x8a8070), (float) (x + w), 0, c(0x4a4238)));
        g.fill(new Rectangle2D.Double(x, y - 8, w, 38));
        g.setColor(c(0x2a2420, 120));
        for (double by = y - 2; by < y + 30; by += 8) g.draw(new Line2D.Double(x, by, x + w, by));
        g.setColor(c(0x9a9080));
        g.fill(new Ellipse2D.Double(x - 2, y - 14, w + 4, 12));
        g.setColor(c(0x0a0806));
        g.fill(new Ellipse2D.Double(x + 6, y - 12, w - 12, 8));
        g.setColor(c(0x4a2c16));
        g.fill(new Rectangle2D.Double(x + 4, y - 66, 5, 56));
        g.fill(new Rectangle2D.Double(x + w - 9, y - 66, 5, 56));
        g.fill(new Rectangle2D.Double(x, y - 70, w, 6));
        g.setColor(c(0x8a7a5a));
        g.draw(new Line2D.Double(x + w / 2, y - 64, x + w / 2, y - 30));
        g.setColor(c(0x5a3a1e));
        g.fill(poly(x + w / 2 - 6, y - 30, x + w / 2 + 6, y - 30, x + w / 2 + 5, y - 20, x + w / 2 - 5, y - 20));
    }
}
