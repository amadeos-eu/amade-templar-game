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
 * The Charola of Tomar, the Templars' round church, seen from the walk around it: in the middle the painted and
 * gilded octagon with the high altar behind a red rope, the outer wall with its paintings and a tall window,
 * the small door to the tower stairs on the left, and the showcase of this month's exhibition on the right.
 */
public final class Charola extends RoomArt {
    static final double GROUND = 292;

    @Override
    public String id() {
        return "charola";
    }

    @Override
    public Color tint() {
        return new Color(246, 230, 206);
    }

    @Override
    protected void paint(BufferedImage img, Graphics2D g) {
        vault(g);
        ink(img, l -> outerWall(l, new Random(1160)));
        ink(img, Charola::window);
        ink(img, Charola::towerDoor);
        ink(img, l -> octagon(l, new Random(1319)));
        floor(g, new Random(7));
        ink(img, Charola::rope);
        Graphics2D v = (Graphics2D) g.create();
        Brush.pool(v, 320, 330, 260, 60, c(0xffd890, 40));
        Brush.pool(v, 470, 312, 70, 18, c(0xfff0c0, 50));
        Workshop.vignette(v);
        v.dispose();
    }

    @Override
    protected Map<String, Consumer<Graphics2D>> layers() {
        return named("showcase", (Consumer<Graphics2D>) Charola::showcase);
    }

    @Override
    public void live(Graphics2D g, double time) {
        // dust turning slowly in the beam of the window
        Random r = new Random(5);
        for (int i = 0; i < 24; i++) {
            double t = (time * (0.02 + r.nextDouble() * 0.03) + r.nextDouble()) % 1;
            double x = 446 + r.nextDouble() * 60 - t * 50, y = 150 + t * 140 + Math.sin(time + i) * 3;
            g.setColor(c(0xfff4d0, (int) (90 * Math.sin(t * Math.PI))));
            g.fill(new Ellipse2D.Double(x, y, 1.2, 1.2));
        }
    }

    @Override
    public void light(Graphics2D g, double time) {
        // the beam of the window falling across the walk, and the candles on the altar
        g.setPaint(new GradientPaint(470, 140, c(0xfff4d0, 46), 420, 300, c(0xfff4d0, 0)));
        g.fill(poly(446, 146, 500, 146, 470, 320, 380, 320));
        Lights.lamp(g, 320, 190, 70, c(0xffc870), 40);
        Lights.flame(g, 302, 196, time, 0.7);
        Lights.flame(g, 338, 196, time + 0.4, 0.7);
    }

    // ---- behind ----

    static void vault(Graphics2D g) {
        // the dark ribbed vault of the walk around the octagon
        g.setPaint(new GradientPaint(0, 0, c(0x2a1c14), 0, 120, c(0x5a4232)));
        g.fill(new Rectangle2D.Double(0, 0, W, GROUND));
        g.setColor(c(0x7a5a3a, 160));
        g.setStroke(new BasicStroke(3f));
        for (double x : new double[]{-60, 110, 530, 700}) {
            Path2D rib = new Path2D.Double();
            rib.moveTo(x, 40);
            rib.quadTo((x + 320) / 2, -10, 320, 20);
            g.draw(rib);
        }
        g.setStroke(new BasicStroke(1f));
    }

    static void outerWall(Graphics2D g, Random r) {
        // the outer wall, curving away: painted panels between pilasters, gold borders
        g.setPaint(new GradientPaint(0, 30, c(0xb89a74), 0, (float) GROUND, c(0x7a5e44)));
        g.fill(poly(0, 34, 230, 50, 230, GROUND, 0, GROUND));
        g.fill(poly(410, 50, 640, 34, 640, GROUND, 410, GROUND));
        double[][] panels = {{100, 60, 92, 140}, {550, 60, 84, 140}};
        Color[] robes = {c(0x8a2a22), c(0x2a4a7a), c(0x3a6a3a), c(0xc89a3a)};
        for (double[] p : panels) {
            double x = p[0], y = p[1], w = p[2], h = p[3];
            g.setColor(c(0xd8b050));
            g.fill(new Rectangle2D.Double(x - 4, y - 4, w + 8, h + 8));
            g.setPaint(new GradientPaint(0, (float) y, c(0x4a5a7a), 0, (float) (y + h), c(0x8a7a5a)));
            g.fill(new Rectangle2D.Double(x, y, w, h));
            // a painted scene: figures in robes under arches, with haloes
            for (int i = 0; i < 4; i++) {
                double fx = x + 10 + i * (w - 20) / 3.0, fy = y + h - 12;
                g.setColor(robes[(i + r.nextInt(2)) % robes.length]);
                g.fill(poly(fx - 8, fy, fx - 5, fy - 50, fx + 5, fy - 50, fx + 8, fy));
                g.setColor(c(0xe8c49a));
                g.fill(new Ellipse2D.Double(fx - 4, fy - 60, 8, 10));
                g.setColor(c(0xf0d070, 200));
                g.setStroke(new BasicStroke(1.2f));
                g.draw(new Ellipse2D.Double(fx - 7, fy - 64, 14, 14));
            }
            g.setStroke(new BasicStroke(1f));
            g.setColor(c(0x2a1a10, 90));
            g.fill(new Rectangle2D.Double(x, y, w, 14));
        }
        // pilasters and the wall below the paintings, with a painted curtain pattern
        for (double x : new double[]{6, 88, 204, 420, 536, 634}) {
            g.setPaint(new GradientPaint((float) x, 0, c(0xc8aa80), (float) x + 10, 0, c(0x8a6a4a)));
            g.fill(new Rectangle2D.Double(x - 5, 40, 10, GROUND - 40));
        }
        g.setColor(c(0x6a2a20, 160));
        for (double x = 0; x < W; x += 12) {
            if (x > 226 && x < 412) continue;
            g.fill(poly(x, 216, x + 12, 216, x + 9, GROUND, x + 3, GROUND));
        }
        g.setColor(c(0xd8b050));
        g.fill(new Rectangle2D.Double(0, 212, 230, 4));
        g.fill(new Rectangle2D.Double(410, 212, 230, 4));
    }

    static void window(Graphics2D g) {
        // a tall round-headed window in the outer wall, bright with the afternoon
        double x = 446, y = 50, w = 50, h = 98;
        Path2D frame = new Path2D.Double();
        frame.moveTo(x - 6, y + h + 6);
        frame.lineTo(x - 6, y + 20);
        frame.quadTo(x + w / 2, y - 26, x + w + 6, y + 20);
        frame.lineTo(x + w + 6, y + h + 6);
        frame.closePath();
        g.setColor(c(0x8a6a4a));
        g.fill(frame);
        Path2D glass = new Path2D.Double();
        glass.moveTo(x, y + h);
        glass.lineTo(x, y + 22);
        glass.quadTo(x + w / 2, y - 16, x + w, y + 22);
        glass.lineTo(x + w, y + h);
        glass.closePath();
        g.setPaint(new GradientPaint(0, (float) y, c(0xfffbe8), 0, (float) (y + h), c(0xd8e0d0)));
        g.fill(glass);
        g.setColor(c(0x6a5a4a, 160));
        g.setStroke(new BasicStroke(0.7f));
        for (double gy = y + 12; gy < y + h; gy += 10) g.draw(new Line2D.Double(x, gy, x + w, gy));
        g.draw(new Line2D.Double(x + w / 2, y, x + w / 2, y + h));
        g.setStroke(new BasicStroke(1f));
    }

    static void towerDoor(Graphics2D g) {
        // the small arched door to the tower stairs, with a step
        double x = 40, y = 180, w = 44, h = GROUND - 180;
        g.setColor(c(0x5a4232));
        g.fill(new RoundRectangle2D.Double(x - 6, y - 8, w + 12, h + 8, 30, 30));
        g.setPaint(new GradientPaint((float) x, 0, c(0x6a4426), (float) (x + w), 0, c(0x3a2414)));
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 26, 26));
        g.setColor(c(0x2a1a0e));
        g.setStroke(new BasicStroke(0.8f));
        for (double bx = x + 11; bx < x + w; bx += 11) g.draw(new Line2D.Double(bx, y + 4, bx, GROUND));
        g.setStroke(new BasicStroke(1f));
        g.setColor(c(0x1a1a1e));
        g.fill(new Rectangle2D.Double(x + 4, y + 40, w - 8, 3));
        g.fill(new Rectangle2D.Double(x + 4, y + 80, w - 8, 3));
        g.setColor(c(0xb89a5a));
        g.fill(new Ellipse2D.Double(x + w - 10, y + 60, 5, 5));
        g.setColor(c(0x8a7a6a));
        g.fill(new Rectangle2D.Double(x - 8, GROUND - 3, w + 16, 5));
    }

    static void octagon(Graphics2D g, Random r) {
        // the octagon in the middle: three faces seen, an open arch in each, painted and gilded; a drum above
        double l = 228, rgt = 412, mid = 320;
        double[] xs = {l, 268, 372, rgt};
        Color[] faces = {c(0x8a3a2a), c(0xa84a32), c(0x8a3a2a)};
        for (int f = 0; f < 3; f++) {
            double x0 = xs[f], x1 = xs[f + 1];
            g.setPaint(new GradientPaint((float) x0, 0, Brush.lighter(faces[f], f == 1 ? 0.15 : 0),
                    (float) x1, 0, Brush.darker(faces[f], f == 1 ? 0 : 0.25)));
            g.fill(new Rectangle2D.Double(x0, 92, x1 - x0, GROUND - 92));
            // gold borders and a painted diaper pattern
            g.setColor(c(0xe0b850));
            g.fill(new Rectangle2D.Double(x0, 92, x1 - x0, 5));
            g.fill(new Rectangle2D.Double(x0, 150, x1 - x0, 3));
            g.setColor(c(0xf0d070, 120));
            for (double py = 104; py < 148; py += 9)
                for (double px = x0 + 4 + ((int) py % 2) * 4; px < x1 - 4; px += 9)
                    g.fill(poly(px, py - 3, px + 3, py, px, py + 3, px - 3, py));
            // the arch, opening onto the altar
            double aw = (x1 - x0) * 0.62, ax = (x0 + x1) / 2 - aw / 2, top = 168;
            Path2D arch = new Path2D.Double();
            arch.moveTo(ax, GROUND);
            arch.lineTo(ax, top + aw / 2);
            arch.quadTo(ax + aw / 2, top - aw * 0.2, ax + aw, top + aw / 2);
            arch.lineTo(ax + aw, GROUND);
            arch.closePath();
            g.setColor(c(0xe0b850));
            g.setStroke(new BasicStroke(4f));
            g.draw(arch);
            g.setStroke(new BasicStroke(1f));
            g.setPaint(new GradientPaint(0, (float) top, c(0x3a2414), 0, (float) GROUND, c(0x6a4a2a)));
            g.fill(arch);
            if (f == 1) {
                // the altar inside, with its candles and a gilded panel
                g.setColor(c(0xc8a050));
                g.fill(new Rectangle2D.Double(mid - 30, 214, 60, 40));
                g.setColor(c(0xe8e0d0));
                g.fill(new Rectangle2D.Double(mid - 34, 246, 68, 26));
                g.setColor(c(0xb0201a));
                g.fill(poly(mid - 4, 220, mid + 4, 220, mid + 4, 228, mid + 12, 228, mid + 12, 236, mid + 4, 236,
                        mid + 4, 250, mid - 4, 250, mid - 4, 236, mid - 12, 236, mid - 12, 228, mid - 4, 228));
                g.setColor(c(0xf0e8d0));
                g.fill(new Rectangle2D.Double(mid - 19, 198, 3, 14));
                g.fill(new Rectangle2D.Double(mid + 17, 198, 3, 14));
            }
            // gilded columns at the corners
            g.setPaint(new GradientPaint((float) x0 - 4, 0, c(0xf0d070), (float) x0 + 4, 0, c(0x9a7020)));
            g.fill(new Rectangle2D.Double(x0 - 4, 92, 8, GROUND - 92));
        }
        g.setPaint(new GradientPaint((float) rgt - 4, 0, c(0xf0d070), (float) rgt + 4, 0, c(0x9a7020)));
        g.fill(new Rectangle2D.Double(rgt - 4, 92, 8, GROUND - 92));
        // the drum above, with small windows, and the start of the dome
        g.setPaint(new GradientPaint(0, 40, c(0x9a5a3a), 0, 92, c(0x7a3a28)));
        g.fill(poly(244, 92, 244, 46, 396, 46, 396, 92));
        for (double wx = 262; wx < 390; wx += 34) {
            g.setColor(c(0xfff0c0));
            g.fill(new RoundRectangle2D.Double(wx, 56, 12, 26, 12, 12));
            g.setColor(c(0xe0b850));
            g.setStroke(new BasicStroke(1.4f));
            g.draw(new RoundRectangle2D.Double(wx, 56, 12, 26, 12, 12));
        }
        g.setStroke(new BasicStroke(1f));
        g.setColor(c(0xe0b850));
        g.fill(new Rectangle2D.Double(240, 42, 160, 5));
        g.setPaint(new GradientPaint(0, 0, c(0x5a3a28), 0, 42, c(0x8a5a3a)));
        Path2D dome = new Path2D.Double();
        dome.moveTo(240, 42);
        dome.quadTo(320, -30, 400, 42);
        dome.closePath();
        g.fill(dome);
        // the cross of the Order of Christ, red with a white cross inside, painted over the middle arch
        double cx = mid, cy = 122;
        g.setColor(c(0xb0201a));
        g.fill(poly(cx - 5, cy - 14, cx + 5, cy - 14, cx + 3, cy - 4, cx + 14, cy - 5, cx + 14, cy + 5, cx + 3, cy + 4,
                cx + 5, cy + 14, cx - 5, cy + 14, cx - 3, cy + 4, cx - 14, cy + 5, cx - 14, cy - 5, cx - 3, cy - 4));
        g.setColor(c(0xf4f0e6));
        g.fill(new Rectangle2D.Double(cx - 1, cy - 10, 2, 20));
        g.fill(new Rectangle2D.Double(cx - 10, cy - 1, 20, 2));
        g.setColor(c(0x000000, (int) (20 + r.nextInt(10))));
        g.fill(new Rectangle2D.Double(l, 270, rgt - l, GROUND - 270));
    }

    static void floor(Graphics2D g, Random r) {
        g.setPaint(new GradientPaint(0, (float) GROUND, c(0x6a5a4a), 0, H, c(0xa8927a)));
        g.fill(new Rectangle2D.Double(0, GROUND, W, H - GROUND));
        for (double y = GROUND + 3; y < H; y += 7 + (y - GROUND) * 0.12) {
            double step = 24 + (y - GROUND) * 0.4;
            g.setColor(c(0x4a3a2c, 90));
            g.fill(new Rectangle2D.Double(0, y, W, 0.8));
            for (double x = r.nextDouble() * step; x < W; x += step) {
                g.fill(new Rectangle2D.Double(x, y, 0.8, 7 + (y - GROUND) * 0.12));
                g.setColor(c(Brush.mix(c(0x8a7a6a), c(0xc0a888), r.nextDouble()).getRGB() & 0xffffff, 40));
                g.fill(new Rectangle2D.Double(x + 2, y + 1.5, step - 4, 4 + (y - GROUND) * 0.1));
                g.setColor(c(0x4a3a2c, 90));
            }
        }
    }

    static void rope(Graphics2D g) {
        // brass posts and a red rope in front of the octagon
        double[] posts = {238, 320, 402};
        for (double x : posts) {
            g.setColor(c(0x1a0e05, 70));
            g.fill(new Ellipse2D.Double(x - 7, 296, 14, 4));
            g.setPaint(new GradientPaint((float) x - 2, 0, c(0xf0d070), (float) x + 2, 0, c(0x9a7020)));
            g.fill(new Rectangle2D.Double(x - 1.6, 272, 3.2, 26));
            g.fill(new Ellipse2D.Double(x - 3, 268, 6, 6));
            g.fill(new Ellipse2D.Double(x - 5, 295, 10, 4));
        }
        g.setColor(c(0x9a1a18));
        g.setStroke(new BasicStroke(2.2f));
        for (int i = 0; i < 2; i++) {
            Path2D rope = new Path2D.Double();
            rope.moveTo(posts[i], 273);
            rope.quadTo((posts[i] + posts[i + 1]) / 2, 290, posts[i + 1], 273);
            g.draw(rope);
        }
        g.setStroke(new BasicStroke(1f));
    }

    // ---- in front ----

    static void showcase(Graphics2D g) {
        // a glass showcase on a dark wooden stand: three stone moulds and a label
        double x = 470, w = 96, top = 250, base = 338;
        g.setColor(c(0x1a0e05, 90));
        g.fill(new Ellipse2D.Double(x - 6, base - 5, w + 12, 9));
        g.setPaint(new GradientPaint((float) x, 0, c(0x4a2e1c), (float) (x + w), 0, c(0x2a1a0e)));
        g.fill(new Rectangle2D.Double(x + 4, 298, w - 8, base - 298));
        g.setColor(c(0x5a3a24));
        g.fill(new Rectangle2D.Double(x, 294, w, 6));
        // the moulds: grey stone slabs with carved hollows, one of them a round church
        double[] mx = {x + 8, x + 38, x + 66};
        for (int i = 0; i < 3; i++) {
            g.setPaint(new GradientPaint((float) mx[i], 270, c(0xa8a49a), (float) mx[i] + 22, 292, c(0x6a6660)));
            g.fill(new RoundRectangle2D.Double(mx[i], 272, 22, 20, 3, 3));
            g.setColor(c(0x4a4640));
            if (i == 1) {
                g.fill(new Ellipse2D.Double(mx[i] + 4, 275, 14, 14));
                g.setColor(c(0x8a867e));
                g.fill(poly(mx[i] + 7, 286, mx[i] + 7, 281, mx[i] + 11, 278, mx[i] + 15, 281, mx[i] + 15, 286));
            } else {
                g.fill(poly(mx[i] + 11, 275, mx[i] + 18, 286, mx[i] + 4, 286));
            }
        }
        g.setColor(c(0xf0ead8));
        g.fill(new Rectangle2D.Double(x + 30, 288, 30, 5));
        g.setColor(c(0x3a3a3a));
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 3));
        g.drawString("TOMAR · SÉC. XIV", (float) x + 31, 291.6f);
        // the glass box, with a reflection
        g.setColor(c(0xd8ecf0, 50));
        g.fill(new Rectangle2D.Double(x, top, w, 44));
        g.setColor(c(0xc8a050));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new Rectangle2D.Double(x, top, w, 44));
        g.setStroke(new BasicStroke(1f));
        g.setColor(c(0xffffff, 90));
        g.fill(poly(x + 10, top + 2, x + 22, top + 2, x + 8, top + 42, x - 4 + 2, top + 42));
    }
}
