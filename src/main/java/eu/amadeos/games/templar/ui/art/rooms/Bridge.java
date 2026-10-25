package eu.amadeos.games.templar.ui.art.rooms;

import eu.amadeos.games.templar.ui.art.Animals;
import eu.amadeos.games.templar.ui.art.Brush;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
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
 * The Pont Neuf at night: the Seine, the quays with their lamps, the bronze king on his horse, and on the stone
 * wall by the steps the plaque for Jacques de Molay, burned on the little island below on 18 March 1314.
 */
public final class Bridge extends RoomArt {
    static final double PAVEMENT = 286;

    @Override
    public String id() {
        return "bridge";
    }

    @Override
    public Color tint() {
        return new Color(200, 206, 236);
    }

    @Override
    protected void paint(BufferedImage img, Graphics2D g) {
        sky(g, new Random(1314));
        river(g, new Random(18));
        ink(img, Bridge::statue);
        ink(img, Bridge::parapet);
        ink(img, Bridge::wall);
        ink(img, l -> lamppost(l, 252));
        ink(img, l -> lamppost(l, 432));
        pavement(g);
        Graphics2D v = (Graphics2D) g.create();
        Workshop.vignette(v);
        v.dispose();
    }

    @Override
    protected Map<String, Consumer<Graphics2D>> layers() {
        return named("bollard", (Consumer<Graphics2D>) Bridge::bollard);
    }

    @Override
    protected Map<String, Prop> props() {
        return Map.of("swarm", new Prop(Bridge::swarm, 346));
    }

    @Override
    public void live(Graphics2D g, double time) {
        // the lights of the quays tremble on the water
        Random r = new Random(4);
        for (int i = 0; i < 26; i++) {
            double x = r.nextInt(W), y = 214 + r.nextInt(30);
            double a = 0.5 + 0.5 * Math.sin(time * (1.5 + r.nextDouble() * 2) + i);
            g.setColor(c(0xffd890, (int) (30 + 60 * a)));
            g.fill(new Rectangle2D.Double(x, y, 6 + r.nextInt(8), 1.2));
        }
    }

    @Override
    public void light(Graphics2D g, double time) {
        Lights.lamp(g, 252, 112, 110, c(0xffd890), 64);
        Lights.lamp(g, 432, 112, 110, c(0xffd890), 64);
    }

    // ---- the night ----

    static void sky(Graphics2D g, Random r) {
        g.setPaint(new GradientPaint(0, 0, c(0x0e1430), 0, 200, c(0x3a3a6a)));
        g.fill(new Rectangle2D.Double(0, 0, W, 210));
        for (int i = 0; i < 60; i++) {
            g.setColor(c(0xeef0ff, 60 + r.nextInt(100)));
            double d = 0.6 + r.nextDouble() * 0.8;
            g.fill(new Ellipse2D.Double(r.nextInt(W), r.nextInt(120), d, d));
        }
        Brush.glow(g, 560, 50, 40, c(0xd8e0ff, 50));
        g.setColor(c(0xeef2ff));
        g.fill(new Ellipse2D.Double(553, 43, 14, 14));
        // the far bank: the long front of the Louvre and the roofs, with lit windows
        g.setColor(c(0x22243e));
        g.fill(poly(0, 196, 0, 150, 60, 150, 60, 140, 170, 140, 170, 150, 330, 150, 340, 132, 350, 150, 470, 150, 470, 144,
                640, 144, 640, 200));
        for (int i = 0; i < 70; i++) {
            g.setColor(r.nextInt(3) == 0 ? c(0xffcc70) : c(0x6a6a8a, 140));
            g.fill(new Rectangle2D.Double(r.nextInt(W), 156 + r.nextInt(32), 2, 3));
        }
        g.setColor(c(0x2a2a40));
        g.fill(new Rectangle2D.Double(0, 196, W, 8));
        for (double x = 10; x < W; x += 34) {
            g.setColor(c(0xffd890));
            g.fill(new Ellipse2D.Double(x, 190, 3, 3));
            Brush.glow(g, x + 1.5, 191, 10, c(0xffc060, 50));
        }
    }

    static void river(Graphics2D g, Random r) {
        g.setPaint(new GradientPaint(0, 204, c(0x1a2440), 0, 250, c(0x2a3658)));
        g.fill(new Rectangle2D.Double(0, 204, W, 50));
        g.setColor(c(0x8a9ac8, 40));
        for (int i = 0; i < 40; i++) g.fill(new Rectangle2D.Double(r.nextInt(W), 206 + r.nextInt(44), 10 + r.nextInt(20), 0.8));
    }

    static void statue(Graphics2D g) {
        // the bronze king on his horse, on a high pedestal on the island's point, seen from behind the parapet
        double x = 120, base = 238;
        g.setPaint(new GradientPaint(0, 140, c(0x5a5a6a), 0, (float) base, c(0x34343e)));
        g.fill(poly(x - 34, base, x - 30, 150, x + 30, 150, x + 34, base));
        g.setColor(c(0x6a6a7a));
        g.fill(new Rectangle2D.Double(x - 36, 146, 72, 6));
        g.setPaint(new GradientPaint((float) x - 30, 0, c(0x4a6a5a), (float) x + 30, 0, c(0x1e2e28)));
        // the horse walking to the right: body, neck and head, four legs, the tail
        Path2D horse = new Path2D.Double();
        horse.moveTo(x - 24, 112);
        horse.curveTo(x - 22, 102, x + 4, 100, x + 14, 104);
        horse.curveTo(x + 18, 96, x + 22, 88, x + 28, 84);
        horse.lineTo(x + 31, 80);
        horse.lineTo(x + 33, 84);
        horse.curveTo(x + 38, 88, x + 40, 92, x + 38, 95);
        horse.lineTo(x + 33, 94);
        horse.curveTo(x + 28, 96, x + 24, 104, x + 22, 112);
        horse.curveTo(x + 22, 120, x + 14, 124, x + 6, 124);
        horse.lineTo(x - 14, 124);
        horse.curveTo(x - 22, 124, x - 26, 120, x - 24, 112);
        horse.closePath();
        g.fill(horse);
        g.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(poly(x + 14, 122, x + 24, 132, x + 26, 146, x + 26, 146, x + 24, 132));      // the raised front leg
        g.draw(new Line2D.Double(x + 6, 123, x + 8, 146));
        g.draw(new Line2D.Double(x - 14, 122, x - 18, 134));
        g.draw(new Line2D.Double(x - 18, 134, x - 14, 146));
        g.draw(new Line2D.Double(x - 20, 120, x - 26, 146));
        g.setStroke(new BasicStroke(1f));
        g.fill(poly(x - 24, 108, x - 34, 116, x - 36, 132, x - 30, 130, x - 26, 116));       // the tail
        // the king: legs along the flank, the body upright, head with a hat, the baton held forward
        g.fill(poly(x - 4, 104, x + 6, 104, x + 6, 116, x + 2, 118, x - 2, 112));
        g.fill(poly(x - 6, 104, x + 6, 104, x + 5, 84, x - 4, 84));
        g.fill(new Ellipse2D.Double(x - 3.5, 75, 7, 9));
        g.fill(new RoundRectangle2D.Double(x - 6, 73, 12, 3.5, 2, 2));
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(poly(x + 4, 88, x + 12, 92, x + 18, 86, x + 18, 86, x + 12, 92));
        g.setStroke(new BasicStroke(1f));
        // pigeons on the pedestal
        for (double[] p : new double[][]{{x - 22, 146}, {x + 14, 146}, {x + 2, 146}}) {
            AffineTransform keep = g.getTransform();
            g.translate(p[0], p[1]);
            g.scale(0.55, 0.55);
            Animals.draw(g, "pigeon", false, 0, false);
            g.setTransform(keep);
        }
    }

    static void parapet(Graphics2D g) {
        // the stone parapet of the bridge with its round bays, and the cornice of grotesque heads below it
        g.setPaint(new GradientPaint(0, 236, c(0xb0a690), 0, (float) PAVEMENT, c(0x7a705e)));
        g.fill(new Rectangle2D.Double(0, 240, W, PAVEMENT - 240));
        g.setColor(c(0xc8bea6));
        g.fill(new Rectangle2D.Double(0, 236, W, 6));
        g.setColor(c(0x5a5040, 120));
        for (double x = 0; x < W; x += 40) g.draw(new Line2D.Double(x, 242, x, PAVEMENT));
        for (double y = 254; y < PAVEMENT; y += 14) g.draw(new Line2D.Double(0, y, W, y));
        // a round bay with its stone bench, in the middle
        g.setPaint(new GradientPaint(0, 250, c(0x9a907a), 0, (float) PAVEMENT, c(0x6a604e)));
        g.fill(new Rectangle2D.Double(296, 262, 100, 22));
        g.setColor(c(0xc8bea6));
        g.fill(new RoundRectangle2D.Double(292, 258, 108, 6, 3, 3));
    }

    static void wall(Graphics2D g) {
        // the pier on the right where the steps go down to the island, with the plaque
        double x = 470, w = 170, top = 150;
        g.setPaint(new GradientPaint((float) x, 0, c(0xa89c84), (float) (x + w), 0, c(0x6a604e)));
        g.fill(new Rectangle2D.Double(x, top, w, PAVEMENT - top + 4));
        g.setColor(c(0xc8bea6));
        g.fill(new Rectangle2D.Double(x - 4, top - 6, w + 8, 8));
        g.setColor(c(0x5a5040, 110));
        Random r = new Random(9);
        for (double y = top + 14; y < PAVEMENT; y += 16) {
            g.draw(new Line2D.Double(x, y, x + w, y));
            for (double bx = x + r.nextInt(30); bx < x + w; bx += 34 + r.nextInt(10)) g.draw(new Line2D.Double(bx, y - 16, bx, y));
        }
        // the plaque: dark stone with gold letters
        double px = 506, py = 196;
        g.setColor(c(0x2a2a30));
        g.fill(new Rectangle2D.Double(px - 3, py - 3, 66, 42));
        g.setColor(c(0x3e3e48));
        g.fill(new Rectangle2D.Double(px, py, 60, 36));
        g.setColor(c(0xd8b860));
        g.setFont(new Font(Font.SERIF, Font.BOLD, 4));
        g.drawString("A CET ENDROIT", (float) px + 14, (float) py + 7);
        g.drawString("JACQUES DE MOLAY", (float) px + 10, (float) py + 13);
        g.setFont(new Font(Font.SERIF, Font.PLAIN, 3));
        for (int i = 0; i < 4; i++) g.fill(new Rectangle2D.Double(px + 8, py + 18 + i * 3.6, 44 - (i % 2) * 8, 0.8));
        g.drawString("LE 18 MARS 1314", (float) px + 18, (float) py + 33);
        // the steps down, at the far right
        g.setColor(c(0x3a342a));
        for (int i = 0; i < 6; i++) g.fill(new Rectangle2D.Double(600 + i * 4, 256 + i * 6, 40 - i * 4, 2.5));
        g.setColor(c(0x2a2a30));
        g.setStroke(new BasicStroke(2f));
        g.draw(new Line2D.Double(596, 236, 640, 280));
        g.setStroke(new BasicStroke(1f));
    }

    static void lamppost(Graphics2D g, double x) {
        // a Pont Neuf lamp: a dark green post with a cluster of lanterns
        g.setColor(c(0x1e2a24));
        g.fill(poly(x - 4, 290, x + 4, 290, x + 2, 132, x - 2, 132));
        g.fill(new Rectangle2D.Double(x - 6, 284, 12, 8));
        g.setStroke(new BasicStroke(2f));
        g.draw(new Line2D.Double(x - 18, 126, x + 18, 126));
        g.setStroke(new BasicStroke(1f));
        for (double lx : new double[]{x - 18, x, x + 18}) {
            double ly = lx == x ? 98 : 112;
            g.setColor(c(0x1e2a24));
            g.fill(poly(lx - 6, ly + 14, lx + 6, ly + 14, lx + 4, ly, lx - 4, ly));
            g.fill(poly(lx - 5, ly, lx, ly - 6, lx + 5, ly));
            g.setColor(c(0xffe8a8));
            g.fill(poly(lx - 4.5, ly + 12, lx + 4.5, ly + 12, lx + 3, ly + 2, lx - 3, ly + 2));
            if (lx != x) g.draw(new Line2D.Double(lx, ly + 14, lx, 126));
        }
    }

    static void pavement(Graphics2D g) {
        g.setPaint(new GradientPaint(0, (float) PAVEMENT, c(0x4a4650), 0, H, c(0x6a6470)));
        g.fill(new Rectangle2D.Double(0, PAVEMENT, W, H - PAVEMENT));
        g.setColor(c(0x8a8478));
        g.fill(new Rectangle2D.Double(0, PAVEMENT, W, 4));
        g.setColor(c(0x2a2830, 90));
        for (double y = PAVEMENT + 12; y < H; y += 6 + (y - PAVEMENT) * 0.12) g.draw(new Line2D.Double(0, y, W, y));
        Brush.pool(g, 252, 320, 140, 40, c(0xffd890, 40));
        Brush.pool(g, 432, 320, 140, 40, c(0xffd890, 40));
    }

    // ---- in front ----

    static void bollard(Graphics2D g) {
        double x = 40, y = 380;
        g.setColor(c(0x1a0e05, 90));
        g.fill(new Ellipse2D.Double(x - 12, y - 4, 24, 8));
        g.setPaint(new GradientPaint((float) x - 8, 0, c(0x4a5a4a), (float) x + 8, 0, c(0x1e2a24)));
        g.fill(new RoundRectangle2D.Double(x - 7, y - 40, 14, 40, 6, 6));
        g.fill(new Ellipse2D.Double(x - 9, y - 46, 18, 10));
    }

    static void swarm(Graphics2D g) {
        // the pigeons, all over the sandwich at the driver's feet
        Random r = new Random(7);
        for (int i = 0; i < 9; i++) {
            AffineTransform keep = g.getTransform();
            g.translate(500 + r.nextInt(70), 332 + r.nextInt(14));
            if (r.nextBoolean()) g.scale(-1, 1);
            g.scale(0.9, 0.9);
            Animals.draw(g, "pigeon", false, r.nextDouble(), r.nextBoolean());
            g.setTransform(keep);
        }
    }
}
