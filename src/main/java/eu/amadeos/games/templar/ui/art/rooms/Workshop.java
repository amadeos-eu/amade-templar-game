package eu.amadeos.games.templar.ui.art.rooms;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

import static eu.amadeos.games.templar.ui.art.Brush.c;

/**
 * Maria's restoration workshop in a small Paris library, late in the evening: the arched window over the roofs,
 * the shelves, the workbench with the psalter under the lamp, the door to the reading room.
 */
public final class Workshop extends RoomArt {
    static final int BENCH_X = 214, BENCH_Y = 253, BENCH_W = 220;

    @Override
    public String id() {
        return "workshop";
    }

    @Override
    public Color tint() {
        return new Color(255, 238, 218);
    }

    @Override
    protected void paint(BufferedImage img, Graphics2D g) {
        Random rnd = new Random(1307);
        wall(g, rnd);
        floor(g);
        lightFromWindow(g);
        ink(img, Workshop::sconces);
        ink(img, Workshop::engraving);
        ink(img, l -> window(l, new Random(7)));
        ink(img, l -> shelves(l, new Random(11)));
        ink(img, l -> {
            door(l);
            doorGlass(l, new Random(3));
            coatHook(l);
        });
        Graphics2D v = (Graphics2D) g.create();
        vignette(v);
        v.dispose();
    }

    @Override
    protected Map<String, Consumer<Graphics2D>> layers() {
        return named("bench", (Consumer<Graphics2D>) Workshop::bench,
                "press", (Consumer<Graphics2D>) Workshop::press,
                "books", (Consumer<Graphics2D>) l -> books(l, new Random(5)));
    }

    @Override
    protected Map<String, Prop> props() {
        return Map.of(
                "bonefolder", new Prop(l -> {
                    l.setColor(c(0xefe6d0));
                    l.fill(AffineTransform.getRotateInstance(0.15, 364, 249).createTransformedShape(
                            new RoundRectangle2D.Double(362, 247, 22, 3.4, 3, 3)));
                }, 339),
                "brush", new Prop(l -> {
                    l.setColor(c(0x8a5a2a));
                    l.fill(AffineTransform.getRotateInstance(-0.1, 250, 246).createTransformedShape(
                            new RoundRectangle2D.Double(246, 245, 18, 2.6, 2, 2)));
                    l.setColor(c(0xd8c8a0));
                    l.fill(AffineTransform.getRotateInstance(-0.1, 250, 246).createTransformedShape(
                            new Rectangle2D.Double(263, 244, 7, 4.6)));
                }, 339),
                "flap", new Prop(Workshop::liftedFlap, 339));
    }

    @Override
    public void live(Graphics2D g, double time) {
        // dust turning in the light of the desk lamp
        double lx = 349, ly = 197;
        Random dust = new Random(13);
        for (int i = 0; i < 60; i++) {
            double a = dust.nextDouble() * Math.PI * 2, r = 10 + dust.nextDouble() * 60;
            double speed = 0.05 + dust.nextDouble() * 0.12;
            double ang = a + time * speed;
            double x = lx - 20 + Math.cos(ang) * r * 1.1, y = ly + 26 + Math.sin(ang * 1.3) * r * 0.45;
            int alpha = (int) (40 + 60 * (0.5 + 0.5 * Math.sin(time * 1.7 + i)));
            g.setColor(c(0xffeac0, alpha));
            double d = 0.5 + dust.nextDouble() * 0.6;
            g.fill(new Ellipse2D.Double(x, y, d, d));
        }
    }

    @Override
    public void light(Graphics2D g, double time) {
        Lights.lamp(g, 349, 197, 90, c(0xffd690), 80 + (int) (4 * Math.sin(time * 3)));
    }

    // ---- the room ----

    static void sconces(Graphics2D g) {
        for (int x : new int[]{230, 470}) {
            int y = 92;
            g.setColor(c(0x8a6a30));
            g.fill(new RoundRectangle2D.Double(x - 3, y, 6, 12, 3, 3));
            g.setStroke(new BasicStroke(1.4f));
            g.draw(new QuadCurve2D.Double(x, y + 6, x + 8, y + 2, x + 9, y - 6));
            g.setStroke(new BasicStroke(1f));
            g.setPaint(new RadialGradientPaint(new Point2D.Double(x + 9, y - 12), 9,
                    new float[]{0f, 1f}, new Color[]{c(0xfffbe8), c(0xf0dcb0)}));
            g.fill(new Arc2D.Double(x + 1, y - 22, 16, 18, 0, -180, Arc2D.CHORD));
            g.fill(new Rectangle2D.Double(x + 1, y - 14, 16, 2));
        }
    }

    static void coatHook(Graphics2D g) {
        int x = 446, y = 120;
        g.setColor(c(0x6a4a2a));
        g.fill(new RoundRectangle2D.Double(x - 8, y - 4, 16, 5, 2, 2));
        g.setPaint(new GradientPaint(x - 10, 0, c(0x7a2e28), x + 12, 0, c(0x4a1814)));
        g.fill(path(x - 3, y, x + 3, y, x + 12, y + 18, x + 13, y + 78, x - 12, y + 80, x - 10, y + 18));
        g.setColor(c(0x3a100c));
        g.draw(new Line2D.Double(x, y + 4, x + 1, y + 78));
        g.setColor(c(0xd8b860));
        for (int i = 0; i < 4; i++) g.fill(new Ellipse2D.Double(x + 2, y + 26 + i * 12, 2.4, 2.4));
        g.setColor(c(0x5a1e18));
        g.draw(new Line2D.Double(x - 8, y + 50, x - 2, y + 50));            // the pocket
        g.setPaint(new GradientPaint(x - 6, 0, c(0x2e5a6a), x + 6, 0, c(0x1e3e4a)));
        g.fill(path(x - 5, y + 2, x + 1, y + 2, x - 1, y + 96, x - 7, y + 96));
        g.setColor(c(0x9ac0c8));
        g.drawLine(x - 7, y + 90, x - 1, y + 90);
    }

    static void wall(Graphics2D g, Random rnd) {
        g.setPaint(new GradientPaint(0, 0, c(0xcbb08a), 0, 250, c(0xe9d3ad)));
        g.fillRect(0, 0, W, 262);
        for (int i = 0; i < 260; i++) {
            int x = rnd.nextInt(W), y = rnd.nextInt(250), r = 6 + rnd.nextInt(26);
            g.setColor(rnd.nextBoolean() ? c(0xf4e2c0, 6) : c(0x5a3f28, 5));
            g.fill(new Ellipse2D.Double(x - r, y - r / 2.0, r * 2, r));
        }
        g.setColor(c(0x3d2716));
        g.fillRect(0, 0, W, 18);
        for (int x = 20; x < W; x += 120) {
            g.setPaint(new GradientPaint(x, 0, c(0x4a2f1b), x + 26, 0, c(0x2c1a0d)));
            g.fillRect(x, 0, 26, 30);
            g.setColor(c(0x1e1108, 120));
            g.fillRect(x, 30, 26, 4);
        }
        g.setPaint(new GradientPaint(0, 222, c(0x5b3a20), 0, 262, c(0x3e2613)));
        g.fillRect(0, 222, W, 40);
        g.setColor(c(0x7a5230));
        g.fillRect(0, 220, W, 3);
        for (int x = 8; x < W; x += 46) {
            g.setColor(c(0x2a190b, 120));
            g.drawRect(x, 228, 38, 28);
        }
    }

    static void engraving(Graphics2D g) {
        int fx = 300, fy = 70;
        g.setColor(c(0x2e1c0e));
        g.fillRect(fx - 4, fy - 4, 66, 82);
        g.setColor(c(0xe8dcc0));
        g.fillRect(fx, fy, 58, 74);
        g.setColor(c(0x5b4a35));
        g.fillRect(fx + 20, fy + 22, 18, 44);
        g.fillRect(fx + 14, fy + 30, 6, 36);
        g.fillRect(fx + 38, fy + 30, 6, 36);
        g.fill(new Polygon(new int[]{fx + 20, fx + 29, fx + 38}, new int[]{fy + 22, fy + 8, fy + 22}, 3));
        g.fill(new Polygon(new int[]{fx + 14, fx + 17, fx + 20}, new int[]{fy + 30, fy + 22, fy + 30}, 3));
        g.fill(new Polygon(new int[]{fx + 38, fx + 41, fx + 44}, new int[]{fy + 30, fy + 22, fy + 30}, 3));
        g.setColor(c(0x8a7a60));
        g.drawLine(fx + 4, fy + 66, fx + 54, fy + 66);
    }

    static void window(Graphics2D g, Random rnd) {
        int x = 40, y = 44, w = 150, h = 170;
        Shape arch = archShape(x, y, w, h);
        g.setPaint(new GradientPaint(0, y, c(0x4a5a9a), 0, y + h, c(0xf2b07a)));
        g.fill(arch);
        Shape old = g.getClip();
        g.clip(arch);
        g.setPaint(new RadialGradientPaint(new Point2D.Double(x + 110, y + 140), 70,
                new float[]{0f, 1f}, new Color[]{c(0xffe2a0, 200), c(0xffe2a0, 0)}));
        g.fillRect(x, y, w, h);
        g.setColor(c(0x6c6f86));
        int[] fxs = {x, x + 20, x + 20, x + 46, x + 52, x + 80, x + 80, x + 112, x + 120, x + 150, x + 150, x};
        int[] fys = {y + 130, y + 118, y + 108, y + 108, y + 120, y + 120, y + 104, y + 112, y + 104, y + 116, y + h, y + h};
        g.fillPolygon(fxs, fys, fxs.length);
        g.setColor(c(0x4a4e63));
        g.fill(new Polygon(new int[]{x - 10, x + 40, x + 90, x + 90, x - 10}, new int[]{y + 150, y + 128, y + 150, y + h, y + h}, 5));
        g.setColor(c(0x565a72));
        g.fill(new Polygon(new int[]{x + 70, x + 125, x + 170, x + 170, x + 70}, new int[]{y + 156, y + 132, y + 152, y + h, y + h}, 5));
        g.setColor(c(0x8b5a44));
        for (int cx : new int[]{x + 24, x + 32, x + 106, x + 114, x + 122}) {
            g.fillRect(cx, y + 116 + (cx % 7), 6, 22);
            g.setColor(c(0xc77b5a));
            g.fillRect(cx + 1, y + 113 + (cx % 7), 2, 4);
            g.setColor(c(0x8b5a44));
        }
        g.setColor(c(0xffd27a));
        for (int i = 0; i < 9; i++) g.fillRect(x + 6 + rnd.nextInt(140), y + 150 + rnd.nextInt(14), 3, 4);
        g.setColor(c(0x5e6178));
        g.fill(new Polygon(new int[]{x + 60, x + 64, x + 68}, new int[]{y + 108, y + 70, y + 108}, 3));
        g.setClip(old);
        g.setStroke(new BasicStroke(4f));
        g.setColor(c(0x3a2414));
        g.draw(arch);
        g.setStroke(new BasicStroke(3f));
        g.drawLine(x + w / 2, y + 6, x + w / 2, y + h);
        g.drawLine(x, y + 90, x + w, y + 90);
        g.setStroke(new BasicStroke(1.2f));
        g.setColor(c(0x3a2414, 200));
        for (int i = 1; i < 4; i++) {
            g.drawLine(x + i * w / 4, y + 92, x + i * w / 4, y + h);
            g.drawLine(x, y + 90 + i * 20, x + w, y + 90 + i * 20);
        }
        g.setPaint(new GradientPaint(0, y + h, c(0xb98d5f), 0, y + h + 10, c(0x6b4628)));
        g.fillRect(x - 12, y + h, w + 24, 10);
        pigeon(g, x + 122, y + h - 1);
        g.setStroke(new BasicStroke(1f));
    }

    static Shape archShape(int x, int y, int w, int h) {
        Path2D p = new Path2D.Double();
        p.moveTo(x, y + h);
        p.lineTo(x, y + w / 2.0);
        p.append(new Arc2D.Double(x, y, w, w, 180, -180, Arc2D.OPEN), true);
        p.lineTo(x + w, y + h);
        p.closePath();
        return p;
    }

    static void pigeon(Graphics2D g, int x, int y) {
        g.setColor(c(0x7d8194));
        g.fill(new Ellipse2D.Double(x - 9, y - 10, 16, 10));
        g.setColor(c(0x5f6377));
        g.fill(new Polygon(new int[]{x - 9, x - 15, x - 6}, new int[]{y - 7, y - 4, y - 3}, 3));
        g.setColor(c(0x6c7f7a));
        g.fill(new Ellipse2D.Double(x + 2, y - 15, 7, 8));
        g.setColor(c(0xe8a040));
        g.fill(new Polygon(new int[]{x + 8, x + 12, x + 8}, new int[]{y - 13, y - 12, y - 11}, 3));
        g.setColor(Color.BLACK);
        g.fillRect(x + 6, y - 13, 1, 1);
        g.setColor(c(0xc0605a));
        g.drawLine(x - 2, y, x - 2, y - 1);
        g.drawLine(x + 1, y, x + 1, y - 1);
    }

    static void shelves(Graphics2D g, Random rnd) {
        int x = 500, y = 36, w = 140, h = 210;
        g.setPaint(new GradientPaint(x, 0, c(0x4a2c16), x + w, 0, c(0x2a170a)));
        g.fillRect(x, y, w, h);
        String[] spines = {"7a2a1e", "2f4a3a", "6b4a1c", "2b3550", "8a6a2c", "4e2638", "3d3d2a", "9a7a4a", "5a1f14", "1f3a3a"};
        for (int row = 0; row < 5; row++) {
            int sy = y + 8 + row * 40;
            int sx = x + 6;
            while (sx < x + w - 10) {
                int bw = 4 + rnd.nextInt(6), bh = 24 + rnd.nextInt(10);
                Color col = c(Integer.parseInt(spines[rnd.nextInt(spines.length)], 16));
                if (rnd.nextInt(9) == 0) {
                    g.setColor(col);
                    g.fill(AffineTransform.getRotateInstance(-0.25, sx, sy + 32).createTransformedShape(
                            new Rectangle(sx, sy + 32 - bh, bw + 2, bh)));
                    sx += bw + 8;
                    continue;
                }
                g.setPaint(new GradientPaint(sx, 0, col.brighter(), sx + bw, 0, col.darker()));
                g.fillRect(sx, sy + 32 - bh, bw, bh);
                g.setColor(c(0xd8b860, 140));
                g.fillRect(sx, sy + 32 - bh + 4, bw, 1);
                g.fillRect(sx, sy + 32 - 6, bw, 1);
                sx += bw + 1;
            }
            g.setColor(c(0x5e3a1e));
            g.fillRect(x, sy + 32, w, 5);
            g.setColor(c(0x1a0e05, 140));
            g.fillRect(x, sy + 37, w, 3);
        }
        g.setColor(c(0x3a2210));
        g.fillRect(x - 4, y - 6, 8, h + 30);
    }

    static void floor(Graphics2D g) {
        g.setPaint(new GradientPaint(0, 262, c(0x6b4526), 0, H, c(0x9a6a3e)));
        g.fillRect(0, 262, W, H - 262);
        double vx = 320, vy = 120;
        g.setStroke(new BasicStroke(0.8f));
        for (int i = -30; i <= 30; i++) {
            double bx = vx + i * 46;
            g.setColor(c(0x3c2412, 110));
            double t = (262 - vy) / (H - vy);
            g.draw(new Line2D.Double(vx + (bx - vx) * t, 262, bx, H));
        }
        for (int i = 0; i < 9; i++) {
            double y = 262 + Math.pow(i / 9.0, 1.6) * (H - 262);
            g.setColor(c(0x3c2412, 60));
            g.draw(new Line2D.Double(0, y, W, y));
        }
        g.setStroke(new BasicStroke(1f));
    }

    static void lightFromWindow(Graphics2D g) {
        Polygon beam = new Polygon(new int[]{52, 188, 330, 120}, new int[]{214, 214, 390, 390}, 4);
        g.setPaint(new GradientPaint(100, 214, c(0xffd890, 70), 200, 390, c(0xffd890, 0)));
        g.fill(beam);
    }

    /** The workbench with the psalter under the lamp; people behind it are hidden below the hips. */
    static void bench(Graphics2D g) {
        int x = BENCH_X, y = BENCH_Y, w = BENCH_W;
        g.setColor(c(0x3a2210));
        g.fillRect(x + 8, y + 10, 10, 81);
        g.fillRect(x + w - 18, y + 10, 10, 81);
        g.setColor(c(0x2a170a));
        g.fillRect(x + 18, y + 68, w - 36, 6);
        g.setColor(c(0x1a0e05, 90));
        g.fill(new Ellipse2D.Double(x - 10, y + 81, w + 20, 18));
        g.setPaint(new GradientPaint(0, y - 14, c(0xa8754a), 0, y + 10, c(0x6e4524)));
        g.fill(new Polygon(new int[]{x + 14, x + w - 14, x + w, x}, new int[]{y - 14, y - 14, y + 4, y + 4}, 4));
        g.setColor(c(0x5a3519));
        g.fillRect(x, y + 4, w, 8);
        g.setColor(c(0x2f5a46));
        g.fill(new Polygon(new int[]{x + 40, x + 168, x + 176, x + 34}, new int[]{y - 12, y - 12, y + 2, y + 2}, 4));
        g.setPaint(new RadialGradientPaint(new Point2D.Double(x + 104, y - 6), 70,
                new float[]{0f, 1f}, new Color[]{c(0xfff0c0, 120), c(0xfff0c0, 0)}));
        g.fill(new Ellipse2D.Double(x + 34, y - 40, 140, 70));
        // the lamp
        g.setColor(c(0x1f3a2e));
        g.setStroke(new BasicStroke(2.2f));
        g.drawLine(x + 190, y - 10, x + 182, y - 54);
        g.drawLine(x + 182, y - 54, x + 140, y - 66);
        g.fill(new Polygon(new int[]{x + 128, x + 152, x + 146, x + 120}, new int[]{y - 70, y - 64, y - 54, y - 58}, 4));
        g.fill(new Ellipse2D.Double(x + 182, y - 14, 18, 6));
        g.setColor(c(0xfff3c8));
        g.fill(new Ellipse2D.Double(x + 125, y - 58, 20, 5));
        g.setStroke(new BasicStroke(1f));
        // the open psalter, the pastedown of its front cover still in place, a little bulging
        int bx = x + 78, by = y - 14;
        g.setColor(c(0x4a1a12));
        g.fill(new Polygon(new int[]{bx - 4, bx + 62, bx + 66, bx - 8}, new int[]{by - 2, by - 2, by + 12, by + 12}, 4));
        g.setPaint(new GradientPaint(bx, 0, c(0xe9d9b0), bx + 29, 0, c(0xcbb486)));
        g.fill(new Polygon(new int[]{bx, bx + 29, bx + 29, bx - 4}, new int[]{by, by + 2, by + 11, by + 10}, 4));
        g.setPaint(new GradientPaint(bx + 29, 0, c(0xcbb486), bx + 58, 0, c(0xe9d9b0)));
        g.fill(new Polygon(new int[]{bx + 29, bx + 58, bx + 62, bx + 29}, new int[]{by + 2, by, by + 10, by + 11}, 4));
        g.setColor(c(0x7a2a1e));
        g.fillRect(bx + 34, by + 3, 3, 3);
        g.setColor(c(0x5a4a3a, 160));
        for (int i = 0; i < 3; i++) g.drawLine(bx + 39, by + 4 + i * 2, bx + 56, by + 4 + i * 2);
        g.setColor(c(0xb89a6a));
        g.fill(new Polygon(new int[]{bx + 2, bx + 27, bx + 27, bx + 1}, new int[]{by + 1, by + 2, by + 11, by + 10}, 4));
        g.setColor(c(0x8a6a3a, 120));
        g.fill(new Ellipse2D.Double(bx + 12, by + 3.5, 13, 5));          // the bulge
        // glue pot, water jar, thread spool and the coffee cup
        g.setColor(c(0x8a8a8a));
        g.fill(new Ellipse2D.Double(x + 44, y - 20, 14, 6));
        g.setColor(c(0x5a5a5a));
        g.fillRect(x + 44, y - 17, 14, 9);
        g.setPaint(new GradientPaint(x + 22, 0, c(0xd8e8f0, 220), x + 32, 0, c(0x8aa0b0, 220)));
        g.fill(new RoundRectangle2D.Double(x + 20, y - 26, 13, 18, 4, 4));
        g.setColor(c(0x6a8ab0, 180));
        g.fill(new Rectangle2D.Double(x + 21, y - 18, 11, 9));
        g.setColor(c(0x7a5a2a));
        g.fill(new RoundRectangle2D.Double(x + 4, y - 10, 10, 8, 2, 2));
        g.setColor(c(0xe0d0a0));
        g.fillRect(x + 4, y - 8, 10, 4);
        g.setColor(c(0xf2efe8));
        g.fill(new RoundRectangle2D.Double(x + 186, y - 30, 9, 10, 3, 3));
        g.drawArc(x + 192, y - 28, 6, 6, -90, 180);
    }

    /** After the discovery: the pastedown lifted, the hollow under it empty. */
    static void liftedFlap(Graphics2D g) {
        int bx = BENCH_X + 78, by = BENCH_Y - 14;
        g.setColor(c(0x6a4a2a));
        g.fill(new Polygon(new int[]{bx + 4, bx + 26, bx + 26, bx + 3}, new int[]{by + 2, by + 3, by + 10, by + 9}, 4));
        g.setPaint(new GradientPaint(bx + 14, 0, c(0xefe0b8), bx + 27, 0, c(0xd8c49a)));
        g.fill(new Polygon(new int[]{bx + 27, bx + 15, bx + 6, bx + 26}, new int[]{by + 2, by - 7, by - 4, by + 3}, 4));
    }

    static void press(Graphics2D g) {
        int px = 20, py = 400;
        g.setPaint(new GradientPaint(px, 0, c(0x4a3020), px + 90, 0, c(0x2a1a0e)));
        g.fillRect(px, py - 70, 90, 70);
        g.setColor(c(0x2a2018));
        g.fillRect(px + 6, py - 96, 12, 30);
        g.fillRect(px + 72, py - 96, 12, 30);
        g.fillRect(px + 2, py - 104, 86, 12);
        g.setColor(c(0x3a2e24));
        g.fillRect(px + 40, py - 128, 10, 30);
        g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(px + 14, py - 128, px + 76, py - 122);
        g.setStroke(new BasicStroke(1f));
        g.setColor(c(0xffd890, 40));
        g.drawLine(px + 2, py - 104, px + 88, py - 104);
    }

    static void books(Graphics2D g, Random rnd) {
        int bx = 560, by = 400;
        String[] cols = {"5a1f14", "2b3550", "6b4a1c", "2f4a3a", "4e2638"};
        for (int i = 0; i < 5; i++) {
            int w = 74 - i * 6 + rnd.nextInt(8), h = 12;
            int x = bx + rnd.nextInt(10) - i * 2;
            int y = by - (i + 1) * h;
            g.setColor(c(Integer.parseInt(cols[i], 16)).darker());
            g.fillRect(x, y, w, h);
            g.setColor(c(0xd8c9a0, 140));
            g.fillRect(x + w - 6, y + 2, 5, h - 4);
        }
    }

    static void door(Graphics2D g) {
        int x = 368, y = 104, w = 64, h = 158;
        g.setColor(c(0x24140a));
        g.fillRect(x - 6, y - 6, w + 12, h + 6);
        g.setPaint(new GradientPaint(x, 0, c(0x4a2c16), x + w, 0, c(0x36200f)));
        g.fillRect(x, y, w, h);
        g.setColor(c(0x22130a, 160));
        g.drawRect(x + 8, y + 92, w - 16, 54);
        g.drawRect(x + 10, y + 94, w - 20, 50);
        g.setColor(c(0xb08a40));
        g.fill(new Ellipse2D.Double(x + 6, y + 82, 5, 5));
    }

    static void doorGlass(Graphics2D g, Random rnd) {
        int x = 376, y = 112, w = 48, h = 74;
        g.setPaint(new GradientPaint(0, y, c(0xd9c89a), 0, y + h, c(0x9a8a66)));
        g.fillRect(x, y, w, h);
        for (int i = 0; i < 160; i++) {
            g.setColor(rnd.nextBoolean() ? c(0xffffff, 18) : c(0x000000, 14));
            g.fillRect(x + rnd.nextInt(w), y + rnd.nextInt(h), 2, 1);
        }
        g.setColor(c(0x3a2414));
        g.setStroke(new BasicStroke(2f));
        g.drawRect(x, y, w, h);
        g.drawLine(x + w / 2, y, x + w / 2, y + h);
        g.setStroke(new BasicStroke(1f));
        g.setPaint(new GradientPaint(0, 262, c(0xe8d098, 110), 0, 276, c(0xe8d098, 0)));
        g.fill(new Polygon(new int[]{368, 432, 446, 356}, new int[]{262, 262, 278, 278}, 4));
    }

    static void vignette(Graphics2D g) {
        g.setPaint(new RadialGradientPaint(new Point2D.Double(W / 2.0, H / 2.0 + 20), 420,
                new float[]{0.6f, 1f}, new Color[]{c(0x000000, 0), c(0x000000, 70)}));
        g.fillRect(0, 0, W, H);
    }

    static Path2D path(double... xy) {
        Path2D p = new Path2D.Double();
        p.moveTo(xy[0], xy[1]);
        for (int i = 2; i < xy.length; i += 2) p.lineTo(xy[i], xy[i + 1]);
        p.closePath();
        return p;
    }
}
