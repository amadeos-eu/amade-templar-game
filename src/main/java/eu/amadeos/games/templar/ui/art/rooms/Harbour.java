package eu.amadeos.games.templar.ui.art.rooms;

import eu.amadeos.games.templar.ui.art.Brush;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
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
 * The old harbour of La Rochelle at dusk: the sun going down between the Saint-Nicolas tower and the Chain
 * tower, the clock gate of the old town on the left, the harbour office with its barometer in the window, a
 * fishing boat at the quay, gulls, and a lamp just lit.
 */
public final class Harbour extends RoomArt {
    static final double QUAY = 266;

    @Override
    public String id() {
        return "harbour";
    }

    @Override
    public Color tint() {
        return new Color(250, 224, 210);
    }

    @Override
    protected void paint(BufferedImage img, Graphics2D g) {
        sky(g, new Random(1345));
        town(g, new Random(17));
        ink(img, Harbour::saintNicolas);
        ink(img, Harbour::chainTower);
        water(g, new Random(4));
        ink(img, Harbour::clockGate);
        ink(img, Harbour::boat);
        quay(g, new Random(8));
        ink(img, Harbour::office);
        ink(img, Harbour::nets);
        ink(img, Harbour::lamp);
        gulls(g);
        Graphics2D v = (Graphics2D) g.create();
        Workshop.vignette(v);
        v.dispose();
    }

    @Override
    protected Map<String, Consumer<Graphics2D>> layers() {
        return named("bollard", (Consumer<Graphics2D>) Harbour::bollard);
    }

    @Override
    public void live(Graphics2D g, double time) {
        // the low sun glitters on the water of the harbour mouth
        Random r = new Random(6);
        for (int i = 0; i < 30; i++) {
            double x = 250 + r.nextInt(160), y = 196 + r.nextInt(64);
            double a = 0.5 + 0.5 * Math.sin(time * (1.2 + r.nextDouble() * 2) + i);
            g.setColor(c(0xffe0a0, (int) (30 + 80 * a)));
            g.fill(new Rectangle2D.Double(x + Math.sin(time + i) * 2, y, 5 + r.nextInt(9), 1.1));
        }
    }

    @Override
    public void light(Graphics2D g, double time) {
        Lights.lamp(g, 615, 140, 80, c(0xffd890), 60);
        Lights.lamp(g, 330, 180, 200, c(0xffb070), 22);
    }

    // ---- the evening ----

    static void sky(Graphics2D g, Random r) {
        g.setPaint(new GradientPaint(0, 0, c(0x2c3a6c), 0, 196, c(0xf2a46c)));
        g.fill(new Rectangle2D.Double(0, 0, W, 200));
        for (int i = 0; i < 8; i++) {
            double x = r.nextInt(W) - 40, y = 30 + r.nextInt(110);
            g.setColor(c(0xffc8a0, 50 + r.nextInt(50)));
            g.fill(new Ellipse2D.Double(x, y, 100 + r.nextInt(140), 5 + r.nextInt(5)));
        }
        Brush.glow(g, 328, 188, 140, c(0xffe0a0, 120));
        g.setColor(c(0xfff0c0));
        g.fill(new Ellipse2D.Double(316, 176, 24, 24));
        // the open sea beyond the towers
        g.setPaint(new GradientPaint(0, 192, c(0x7a6a8a), 0, 214, c(0x3a4a6a)));
        g.fill(new Rectangle2D.Double(0, 192, W, 24));
    }

    static void town(Graphics2D g, Random r) {
        // the houses along the quays, far away, in the warm dusk
        for (double x = 60; x < W; x += 26 + r.nextInt(20)) {
            if (x > 250 && x < 400) continue;
            double top = 150 + r.nextInt(30);
            g.setColor(Brush.mix(c(0x9a7a80), c(0x7a6a7a), r.nextDouble()));
            g.fill(new Rectangle2D.Double(x, top, 30, 212 - top));
            g.setColor(c(0x5a4a5a));
            g.fill(poly(x - 2, top, x + 15, top - 10, x + 32, top));
            for (double wy = top + 6; wy < 206; wy += 10) {
                g.setColor(r.nextInt(3) == 0 ? c(0xffd080) : c(0x5a4a5a));
                g.fill(new Rectangle2D.Double(x + 6 + r.nextInt(14), wy, 3, 4));
            }
        }
    }

    static void saintNicolas(Graphics2D g) {
        // the great tower on the left of the harbour mouth: massive, battlemented, a turret on its corner
        double x = 152, w = 98, top = 62, base = 214;
        g.setPaint(new GradientPaint((float) x, 0, c(0xb89a86), (float) (x + w), 0, c(0x6a5458)));
        g.fill(poly(x, base, x + 4, top, x + w - 4, top, x + w, base));
        g.setColor(c(0xa88a78));
        for (double cx = x + 2; cx < x + w - 4; cx += 10) g.fill(new Rectangle2D.Double(cx, top - 8, 6, 9));
        // the corner turret with its little roof, and the pointed roof of the stair tower
        g.setPaint(new GradientPaint((float) x + 64, 0, c(0xb09482), (float) x + 90, 0, c(0x6a5458)));
        g.fill(new Rectangle2D.Double(x + 64, 30, 26, top - 30));
        g.setColor(c(0x4a3a40));
        g.fill(poly(x + 60, 32, x + 77, 4, x + 94, 32));
        g.setColor(c(0x5a4448, 140));
        g.setStroke(new BasicStroke(0.6f));
        for (double y = top + 12; y < base; y += 12) g.draw(new Line2D.Double(x + 3, y, x + w - 3, y));
        g.setStroke(new BasicStroke(1f));
        g.setColor(c(0x2a2028));
        for (double[] s : new double[][]{{x + 24, 90}, {x + 60, 110}, {x + 36, 150}, {x + 74, 70}}) {
            g.fill(new RoundRectangle2D.Double(s[0], s[1], 4, 10, 3, 3));
        }
        g.setColor(c(0xffd080));
        g.fill(new RoundRectangle2D.Double(x + 60, 110, 4, 10, 3, 3));
        g.setColor(c(0x5a4a4a));
        g.fill(new Rectangle2D.Double(x - 30, base - 6, w + 60, 8));
    }

    static void chainTower(Graphics2D g) {
        // the round Chain tower on the right, lower, with its crown of battlements and a little roof
        double cx = 438, r = 34, top = 118, base = 214;
        g.setPaint(new GradientPaint((float) (cx - r), 0, c(0xc0a28e), (float) (cx + r), 0, c(0x6a5458)));
        g.fill(new Rectangle2D.Double(cx - r, top, r * 2, base - top));
        g.fill(new Ellipse2D.Double(cx - r, base - 8, r * 2, 14));
        g.setColor(c(0xb09482));
        g.fill(new Rectangle2D.Double(cx - r - 3, top - 6, r * 2 + 6, 8));
        for (double x = cx - r - 2; x < cx + r; x += 9) g.fill(new Rectangle2D.Double(x, top - 13, 5, 8));
        g.setColor(c(0x4a3a40));
        g.fill(poly(cx - 12, top - 12, cx, top - 30, cx + 12, top - 12));
        g.setColor(c(0x2a2028));
        g.fill(new RoundRectangle2D.Double(cx - 6, 150, 4, 10, 3, 3));
        g.fill(new RoundRectangle2D.Double(cx + 14, 170, 4, 10, 3, 3));
        g.setColor(c(0x5a4a4a));
        g.fill(new Rectangle2D.Double(cx - r - 20, base - 4, r * 2 + 220, 8));
    }

    static void water(Graphics2D g, Random r) {
        g.setPaint(new GradientPaint(0, 212, c(0x4a5276), 0, (float) QUAY, c(0x2a3456)));
        g.fill(new Rectangle2D.Double(0, 212, W, QUAY - 212));
        g.setColor(c(0xffd0a0, 50));
        for (int i = 0; i < 50; i++) g.fill(new Rectangle2D.Double(r.nextInt(W), 214 + r.nextInt(50), 8 + r.nextInt(20), 0.8));
        // the reflections of the towers
        g.setColor(c(0x8a6a6a, 50));
        g.fill(new Rectangle2D.Double(156, 216, 90, 40));
        g.fill(new Rectangle2D.Double(406, 216, 64, 30));
        Brush.glow(g, 328, 222, 60, c(0xffd0a0, 60));
    }

    static void clockGate(Graphics2D g) {
        // the clock gate of the old town on the far left: a square tower with an arch, the clock, a lantern top
        double x = 4, w = 62, base = 214;
        g.setPaint(new GradientPaint((float) x, 0, c(0xc8b09a), (float) (x + w), 0, c(0x8a7068)));
        g.fill(new Rectangle2D.Double(x, 70, w, base - 70));
        g.setColor(c(0x2a2028));
        g.fill(poly(x + 16, base, x + 16, 178, x + 31, 164, x + 46, 178, x + 46, base));
        g.setColor(c(0xf0e8d8));
        g.fill(new Ellipse2D.Double(x + 21, 92, 20, 20));
        g.setColor(c(0x2a2028));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new Ellipse2D.Double(x + 21, 92, 20, 20));
        g.draw(new Line2D.Double(x + 31, 102, x + 31, 95));
        g.draw(new Line2D.Double(x + 31, 102, x + 36, 104));
        g.setStroke(new BasicStroke(1f));
        g.setColor(c(0x6a5a5a));
        g.fill(new Rectangle2D.Double(x - 2, 64, w + 4, 8));
        g.setColor(c(0x3a3a4a));
        Path2D dome = new Path2D.Double();
        dome.moveTo(x + 10, 64);
        dome.curveTo(x + 10, 44, x + 52, 44, x + 52, 64);
        dome.closePath();
        g.fill(dome);
        g.fill(new Rectangle2D.Double(x + 26, 30, 10, 18));
        g.fill(poly(x + 24, 32, x + 31, 20, x + 38, 32));
    }

    static void boat(Graphics2D g) {
        // a small blue fishing boat at the quay: hull, wheelhouse, mast, the name on the bow
        double x = 382, w = 136, deck = 232, water = 262;
        g.setColor(c(0x1a1a2a, 70));
        g.fill(new Ellipse2D.Double(x, water - 3, w, 8));
        Path2D hull = new Path2D.Double();
        hull.moveTo(x, deck - 4);
        hull.lineTo(x + w, deck - 8);
        hull.curveTo(x + w - 6, water - 10, x + w - 20, water, x + w - 34, water);
        hull.lineTo(x + 18, water);
        hull.curveTo(x + 8, water - 6, x + 2, deck + 10, x, deck - 4);
        hull.closePath();
        g.setPaint(new GradientPaint(0, (float) deck, c(0x3a6aa8), 0, (float) water, c(0x1e3a68)));
        g.fill(hull);
        g.setColor(c(0xf0ece0));
        g.fill(poly(x, deck - 4, x + w, deck - 8, x + w - 1, deck - 4, x + 1, deck));
        g.setColor(c(0xb0302a));
        g.fill(poly(x + 18, water - 3, x + w - 34, water - 3, x + w - 30, water - 7, x + 12, water - 7));
        g.setColor(c(0xf0ece0));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 6));
        g.drawString("MARIE-JO", (float) (x + w - 50), (float) (deck + 8));
        // the wheelhouse
        g.setPaint(new GradientPaint(0, 200, c(0xf0ece0), 0, (float) deck, c(0xb8b0a0)));
        g.fill(new Rectangle2D.Double(x + 70, 202, 36, deck - 204));
        g.setColor(c(0x3a3a4a));
        g.fill(poly(x + 66, 204, x + 110, 204, x + 106, 198, x + 70, 198));
        g.setColor(c(0x6a8aaa));
        g.fill(new Rectangle2D.Double(x + 76, 208, 10, 8));
        g.fill(new Rectangle2D.Double(x + 90, 208, 10, 8));
        // the mast with its stays and a little flag
        g.setColor(c(0x3a3030));
        g.setStroke(new BasicStroke(2f));
        g.draw(new Line2D.Double(x + 36, deck - 4, x + 36, 130));
        g.setStroke(new BasicStroke(0.6f));
        g.draw(new Line2D.Double(x + 36, 132, x + 4, deck - 4));
        g.draw(new Line2D.Double(x + 36, 132, x + 70, 200));
        g.setStroke(new BasicStroke(1f));
        g.setColor(c(0xd84a3a));
        g.fill(poly(x + 37, 132, x + 50, 136, x + 37, 140));
    }

    static void quay(Graphics2D g, Random r) {
        // the quay: its stone edge, then the wide pavement, warm in the last light
        g.setPaint(new GradientPaint(0, (float) QUAY, c(0x8a7a6e), 0, H, c(0xb8a690)));
        g.fill(new Rectangle2D.Double(0, QUAY, W, H - QUAY));
        g.setColor(c(0xc8b8a0));
        g.fill(new Rectangle2D.Double(0, QUAY - 2, W, 6));
        g.setColor(c(0x5a4a40, 110));
        for (double x = 0; x < W; x += 30) g.draw(new Line2D.Double(x, QUAY - 2, x, QUAY + 4));
        for (double y = QUAY + 14; y < H; y += 8 + (y - QUAY) * 0.14) {
            g.setColor(c(0x6a5a50, 70));
            g.draw(new Line2D.Double(0, y, W, y));
            double step = 30 + (y - QUAY) * 0.4;
            for (double x = r.nextDouble() * step; x < W; x += step) g.draw(new Line2D.Double(x, y, x - 2, y + 8));
        }
        Brush.pool(g, 615, 310, 90, 26, c(0xffd890, 50));
        Brush.pool(g, 330, 290, 260, 30, c(0xffc890, 30));
    }

    static void office(Graphics2D g) {
        // the harbour office: a little stone house with a blue door and a window with the barometer
        double x = 72, w = 74, top = 132, base = QUAY;
        g.setPaint(new GradientPaint((float) x, 0, c(0xe0d0b8), (float) (x + w), 0, c(0xa8947e)));
        g.fill(new Rectangle2D.Double(x, top, w, base - top));
        g.setColor(c(0x5a4a5a));
        g.fill(poly(x - 4, top + 2, x + w / 2, top - 18, x + w + 4, top + 2));
        g.setColor(c(0x2a4a7a));
        g.fill(new Rectangle2D.Double(x + 50, base - 56, 18, 56));
        g.setColor(c(0xd8c070));
        g.fill(new Ellipse2D.Double(x + 52, base - 30, 2.5, 2.5));
        g.setColor(c(0x2a3a5a));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 5));
        g.drawString("CAPITAINERIE", (float) x + 6, (float) top + 12);
        // the window, dark inside, and the barometer on the wall behind the glass
        double wx = 82, wy = 180;
        g.setColor(c(0x22263a));
        g.fill(new Rectangle2D.Double(wx, wy, 44, 40));
        g.setColor(c(0x5a4a3a));
        g.fill(new Ellipse2D.Double(wx + 15, wy + 7, 22, 22));
        g.setColor(c(0x8a8478));
        g.fill(new Ellipse2D.Double(wx + 17, wy + 9, 18, 18));
        g.setColor(c(0x2a2a2a));
        g.setStroke(new BasicStroke(0.9f));
        g.draw(new Line2D.Double(wx + 26, wy + 18, wx + 31, wy + 23));
        g.setStroke(new BasicStroke(1f));
        g.setColor(c(0xffffff, 40));
        g.fill(poly(wx + 4, wy + 36, wx + 18, wy + 4, wx + 24, wy + 4, wx + 10, wy + 36));
        g.setColor(c(0xe8e0d0));
        g.setStroke(new BasicStroke(2f));
        g.draw(new Rectangle2D.Double(wx, wy, 44, 40));
        g.draw(new Line2D.Double(wx + 22, wy, wx + 22, wy + 40));
        g.setStroke(new BasicStroke(1f));
    }

    static void nets(Graphics2D g) {
        // the fisherman's nets in a heap on the quay, with floats and a crate
        double x = 504, y = 314;
        g.setColor(c(0x1a0e05, 80));
        g.fill(new Ellipse2D.Double(x - 4, y - 6, 74, 12));
        g.setColor(c(0x8a3a2a));
        g.fill(new RoundRectangle2D.Double(x + 40, y - 30, 30, 26, 2, 2));
        g.setColor(c(0x6a2a1e));
        g.fill(new Rectangle2D.Double(x + 40, y - 20, 30, 2));
        g.setPaint(new GradientPaint(0, (float) y - 24, c(0x5a7a6a), 0, (float) y, c(0x2a4a3a)));
        g.fill(Brush.blob(x, y - 2, x + 6, y - 22, x + 26, y - 28, x + 46, y - 18, x + 52, y - 2));
        g.setColor(c(0x8aaa9a, 120));
        g.setStroke(new BasicStroke(0.5f));
        for (double nx = x + 4; nx < x + 50; nx += 4) g.draw(new Line2D.Double(nx, y - 2, nx + 6, y - 22));
        g.setStroke(new BasicStroke(1f));
        g.setColor(c(0xe86a2a));
        for (double[] f : new double[][]{{x + 10, y - 14}, {x + 24, y - 22}, {x + 38, y - 12}}) g.fill(new Ellipse2D.Double(f[0], f[1], 6, 5));
    }

    static void lamp(Graphics2D g) {
        // an old cast-iron quay lamp on the right
        double x = 615;
        g.setColor(c(0x22262a));
        g.fill(poly(x - 3, 296, x + 3, 296, x + 2, 150, x - 2, 150));
        g.fill(new Rectangle2D.Double(x - 6, 290, 12, 7));
        g.fill(poly(x - 8, 152, x + 8, 152, x + 6, 134, x - 6, 134));
        g.fill(poly(x - 7, 134, x, 124, x + 7, 134));
        g.setColor(c(0xffe8a8));
        g.fill(poly(x - 6, 150, x + 6, 150, x + 4.5, 136, x - 4.5, 136));
    }

    static void gulls(Graphics2D g) {
        // gulls, all sitting on the quay edge, facing the wind
        double[][] at = {{226, 276}, {244, 280}, {262, 274}, {280, 282}, {296, 277}};
        for (double[] p : at) {
            g.setColor(c(0x1a0e05, 60));
            g.fill(new Ellipse2D.Double(p[0] - 5, p[1] + 3, 10, 2.4));
            g.setColor(c(0xf2f0ea));
            g.fill(new Ellipse2D.Double(p[0] - 5, p[1] - 4, 10, 7));
            g.fill(new Ellipse2D.Double(p[0] + 2, p[1] - 8, 5, 5));
            g.setColor(c(0x8a8e9a));
            g.fill(new Arc2D.Double(p[0] - 6, p[1] - 5, 10, 7, 90, 180, Arc2D.CHORD));
            g.setColor(c(0xe8b020));
            g.fill(poly(p[0] + 7, p[1] - 6, p[0] + 10, p[1] - 5, p[0] + 7, p[1] - 4.6));
            g.setColor(c(0x1a1a1a));
            g.fill(new Ellipse2D.Double(p[0] + 4.6, p[1] - 6.8, 1.2, 1.2));
        }
    }

    // ---- in front ----

    static void bollard(Graphics2D g) {
        double x = 326, y = 360;
        g.setColor(c(0x1a0e05, 90));
        g.fill(new Ellipse2D.Double(x - 14, y - 4, 28, 8));
        g.setPaint(new GradientPaint((float) x - 10, 0, c(0x4a4a52), (float) x + 10, 0, c(0x1e1e24)));
        g.fill(new RoundRectangle2D.Double(x - 9, y - 26, 18, 26, 6, 6));
        g.fill(new Ellipse2D.Double(x - 12, y - 32, 24, 10));
        g.setColor(c(0xc8b8a0));
        g.setStroke(new BasicStroke(2f));
        g.draw(new Line2D.Double(x - 8, y - 18, x - 40, y - 6));
        g.setStroke(new BasicStroke(1f));
    }
}
