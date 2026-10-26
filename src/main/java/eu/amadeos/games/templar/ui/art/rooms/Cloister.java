package eu.amadeos.games.templar.ui.art.rooms;

import eu.amadeos.games.templar.ui.art.Brush;

import java.awt.BasicStroke;
import java.awt.Color;
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

import static eu.amadeos.games.templar.ui.art.Brush.c;
import static eu.amadeos.games.templar.ui.art.Brush.poly;

/**
 * The cemetery cloister of the convent in the late afternoon: two storeys of pointed arches round a small garden
 * with an orange tree, the gate to the main cloister on the left, and in the paving in front a gravestone with a
 * cross, levered up, over the pit with the chest.
 */
public final class Cloister extends RoomArt {
    static final double GROUND = 290;

    @Override
    public String id() {
        return "cloister";
    }

    @Override
    public Color tint() {
        return new Color(255, 240, 218);
    }

    @Override
    protected void paint(BufferedImage img, Graphics2D g) {
        sky(g);
        ink(img, l -> arcades(l, new Random(1420)));
        ink(img, l -> garden(l, new Random(9)));
        ink(img, Cloister::gate);
        paving(g, new Random(4));
        ink(img, Cloister::pit);
        Graphics2D v = (Graphics2D) g.create();
        Brush.pool(v, 380, 340, 300, 50, c(0xffe8b0, 40));
        Workshop.vignette(v);
        v.dispose();
    }

    @Override
    protected Map<String, Prop> props() {
        return Map.of(
                "iron-chest", new Prop(Cloister::chest, 346),
                "iron-chest-open", new Prop(Cloister::chestOpen, 346));
    }

    @Override
    public void live(Graphics2D g, double time) {
        // swallows over the cloister
        for (int i = 0; i < 3; i++) {
            double t = (time * 0.08 + i * 0.37) % 1;
            double x = -20 + t * 700, y = 18 + 10 * Math.sin(t * 9 + i) + i * 6;
            double flap = Math.sin(time * 14 + i) * 2.4;
            g.setColor(c(0x1e1e26));
            g.setStroke(new BasicStroke(1.2f));
            g.draw(Brush.curve(x - 5, y - flap, x - 2, y, x, y + 1, x + 2, y, x + 5, y - flap));
        }
        g.setStroke(new BasicStroke(1f));
    }

    // ---- behind ----

    static void sky(Graphics2D g) {
        g.setPaint(new GradientPaint(0, 0, c(0x7aa8d8), 0, 60, c(0xd8e4ec)));
        g.fill(new Rectangle2D.Double(0, 0, W, 60));
    }

    static void arcades(Graphics2D g, Random r) {
        // the roof, then the upper gallery with small pointed arches, then the lower one, deep in shadow
        g.setPaint(new GradientPaint(0, 30, c(0xb0604a), 0, 52, c(0x8a4a38)));
        g.fill(poly(0, 50, 0, 36, W, 36, W, 50));
        g.setColor(c(0x6a3a2a, 120));
        for (double x = 0; x < W; x += 6) g.fill(new Rectangle2D.Double(x, 36, 1, 14));
        g.setPaint(new GradientPaint(0, 50, c(0xe8d8bc), 0, (float) GROUND, c(0xc8b49a)));
        g.fill(new Rectangle2D.Double(0, 50, W, GROUND - 50));
        arches(g, 64, 132, 34, 14, c(0x5a4a3c));
        g.setColor(c(0xd8c6a8));
        g.fill(new Rectangle2D.Double(0, 134, W, 12));
        g.setColor(c(0xb8a688));
        g.fill(new Rectangle2D.Double(0, 144, W, 3));
        arches(g, 160, GROUND, 64, 30, c(0x3a2e24));
        // weathering on the stone
        for (int i = 0; i < 60; i++) {
            g.setColor(c(0x8a7a5a, 30 + r.nextInt(30)));
            g.fill(new Ellipse2D.Double(r.nextDouble() * W, 50 + r.nextDouble() * (GROUND - 60), 4 + r.nextDouble() * 10,
                    2 + r.nextDouble() * 4));
        }
    }

    /** A row of pointed arches from {@code top} to {@code bottom}, {@code span} apart, with piers {@code pier} wide. */
    private static void arches(Graphics2D g, double top, double bottom, double span, double pier, Color dark) {
        for (double x = pier / 2; x < W; x += span) {
            double l = x + pier / 2, rgt = x + span - pier / 2, w = rgt - l;
            Path2D a = new Path2D.Double();
            a.moveTo(l, bottom);
            a.lineTo(l, top + w * 0.7);
            a.quadTo(l, top, l + w / 2, top);
            a.quadTo(rgt, top, rgt, top + w * 0.7);
            a.lineTo(rgt, bottom);
            a.closePath();
            g.setPaint(new GradientPaint(0, (float) top, dark, 0, (float) bottom, Brush.lighter(dark, 0.25)));
            g.fill(a);
            g.setColor(c(0xf0e4cc));
            g.setStroke(new BasicStroke(1.4f));
            g.draw(a);
            g.setStroke(new BasicStroke(1f));
            // a slim column between each pair of piers
            g.setPaint(new GradientPaint((float) x - 3, 0, c(0xf0e4cc), (float) x + 3, 0, c(0xb8a688)));
            g.fill(new Rectangle2D.Double(x - 2.4, top + 6, 4.8, bottom - top - 6));
        }
    }

    static void garden(Graphics2D g, Random r) {
        // a low wall, the grass of the garden behind it, and the orange tree
        g.setColor(c(0x6a8a4a));
        g.fill(new Rectangle2D.Double(0, 262, W, 26));
        g.setColor(c(0xd8c8aa));
        g.fill(new Rectangle2D.Double(0, 278, W, 12));
        g.setColor(c(0xb8a688));
        g.fill(new Rectangle2D.Double(0, 278, W, 2));
        double cx = 310;
        g.setPaint(new GradientPaint((float) cx - 6, 0, c(0x7a5a3a), (float) cx + 6, 0, c(0x4a3422)));
        g.fill(poly(cx - 6, 280, cx - 3, 200, cx + 3, 200, cx + 6, 280));
        g.setColor(c(0x2e5a2a));
        g.fill(Brush.blob(cx - 60, 200, cx - 54, 140, cx - 10, 116, cx + 40, 124, cx + 62, 168, cx + 50, 214, cx, 226));
        g.setColor(c(0x3e6e32));
        g.fill(Brush.blob(cx - 40, 190, cx - 30, 144, cx + 10, 130, cx + 40, 160, cx + 30, 200));
        for (int i = 0; i < 18; i++) {
            double a = r.nextDouble() * Math.PI * 2, d = 10 + r.nextDouble() * 42;
            double x = cx + Math.cos(a) * d, y = 172 + Math.sin(a) * d * 0.7;
            g.setColor(c(0xf0902a));
            g.fill(new Ellipse2D.Double(x - 3, y - 3, 6, 6));
        }
    }

    static void gate(Graphics2D g) {
        // the arch to the main cloister, bright beyond
        double w = 30, top = 150;
        Path2D arch = new Path2D.Double();
        arch.moveTo(-10, GROUND);
        arch.lineTo(-10, top + 20);
        arch.quadTo(-10, top, w / 2 - 5, top - 6);
        arch.quadTo(w, top, w, top + 20);
        arch.lineTo(w, GROUND);
        arch.closePath();
        g.setColor(c(0x8a7a62));
        g.setStroke(new BasicStroke(8f));
        g.draw(arch);
        g.setStroke(new BasicStroke(1f));
        g.setPaint(new GradientPaint(0, (float) top, c(0xfff4dc), 0, (float) GROUND, c(0xe8d4b0)));
        g.fill(arch);
        g.setColor(c(0xa8c87a));
        g.fill(new Rectangle2D.Double(-10, GROUND - 30, w + 10, 30));
    }

    static void paving(Graphics2D g, Random r) {
        g.setPaint(new GradientPaint(0, (float) GROUND, c(0xa89678), 0, H, c(0xd0bc98)));
        g.fill(new Rectangle2D.Double(0, GROUND, W, H - GROUND));
        for (double y = GROUND + 4; y < H; y += 8 + (y - GROUND) * 0.12) {
            double step = 30 + (y - GROUND) * 0.5;
            g.setColor(c(0x6a5a44, 90));
            g.fill(new Rectangle2D.Double(0, y, W, 0.8));
            for (double x = r.nextDouble() * step; x < W; x += step)
                g.fill(new Rectangle2D.Double(x, y, 0.8, 8 + (y - GROUND) * 0.12));
        }
        // other gravestones, flush with the paving, worn smooth
        for (double[] s : new double[][]{{90, 356}, {500, 370}, {170, 384}}) {
            g.setColor(c(0x8a7a62, 120));
            g.fill(new RoundRectangle2D.Double(s[0], s[1], 56, 14, 4, 4));
            g.setColor(c(0x5a4a3a, 90));
            g.fill(new Rectangle2D.Double(s[0] + 26, s[1] + 2, 3, 10));
            g.fill(new Rectangle2D.Double(s[0] + 21, s[1] + 5, 13, 2.4));
        }
    }

    static void pit(Graphics2D g) {
        // the open grave: the dark pit, the slab with its cross leaning on the edge, and the crowbar
        double x = 304, y = 330, w = 78, h = 18;
        g.setColor(c(0x6a5a44));
        g.fill(new RoundRectangle2D.Double(x - 3, y - 3, w + 6, h + 6, 3, 3));
        g.setPaint(new GradientPaint(0, (float) y, c(0x140e08), 0, (float) (y + h), c(0x3a2a1a)));
        g.fill(new Rectangle2D.Double(x, y, w, h));
        // the slab, propped up behind the pit
        g.setPaint(new GradientPaint(0, 300, c(0xd8c8a8), 0, 330, c(0x9a8a6c)));
        g.fill(poly(x + 2, y - 2, x + w - 2, y - 2, x + w - 8, y - 30, x + 8, y - 30));
        g.setColor(c(0x6a5a44));
        g.fill(new Rectangle2D.Double(x + w / 2 - 2, y - 27, 4, 22));
        g.fill(new Rectangle2D.Double(x + w / 2 - 10, y - 21, 20, 4));
        // the crowbar
        g.setColor(c(0x3a3a40));
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(x + w + 6, y + 22, x + w + 40, y + 12));
        g.draw(Brush.curve(x + w + 6, y + 22, x + w + 2, y + 20, x + w + 3, y + 16));
        g.setStroke(new BasicStroke(1f));
    }

    // ---- props ----

    static void chest(Graphics2D g) {
        double x = 318, y = 334;
        g.setPaint(new GradientPaint(0, (float) y - 10, c(0x6a4a2a), 0, (float) y + 12, c(0x3a2414)));
        g.fill(new RoundRectangle2D.Double(x, y - 8, 50, 20, 6, 6));
        g.setColor(c(0x2a2a2e));
        g.fill(new Rectangle2D.Double(x + 8, y - 8, 3, 20));
        g.fill(new Rectangle2D.Double(x + 39, y - 8, 3, 20));
        g.setColor(c(0xb89a5a));
        g.fill(new Rectangle2D.Double(x + 22, y - 3, 6, 6));
    }

    static void chestOpen(Graphics2D g) {
        // the chest, open: the lid thrown back, rolls and sheets of parchment with seals on cords
        double x = 318, y = 334;
        g.setColor(c(0x4a3020));
        g.fill(poly(x, y - 8, x + 50, y - 8, x + 46, y - 26, x + 4, y - 26));
        g.setColor(c(0x2a2a2e));
        g.fill(new Rectangle2D.Double(x + 8, y - 26, 3, 18));
        g.fill(new Rectangle2D.Double(x + 39, y - 26, 3, 18));
        g.setPaint(new GradientPaint(0, (float) y - 8, c(0x6a4a2a), 0, (float) y + 12, c(0x3a2414)));
        g.fill(new Rectangle2D.Double(x, y - 4, 50, 16));
        g.setColor(c(0xeee2c4));
        for (int i = 0; i < 6; i++) g.fill(new RoundRectangle2D.Double(x + 3 + i * 7.6, y - 9 - (i % 2) * 2, 7, 8, 3, 3));
        Color[] wax = {c(0x2a7a3a), c(0xb0201a), c(0x2a7a3a), c(0xb0201a)};
        for (int i = 0; i < 4; i++) {
            double sx = x + 8 + i * 11;
            g.setColor(c(0x8a7a5a));
            g.draw(new Line2D.Double(sx, y - 4, sx, y + 4));
            g.setColor(wax[i]);
            g.fill(new Ellipse2D.Double(sx - 3, y + 3, 6, 6));
        }
        g.setColor(c(0xfff4c8, 70));
        g.fill(new Ellipse2D.Double(x + 4, y - 14, 42, 10));
    }
}
