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
 * The archive in the tower of the convent: a vaulted room with shelves of registers, the register of 1319 on a
 * lectern in the middle, a reading desk with a green lamp, the hatch of the book lift in the wall, the heavy door,
 * and a barred window high over the cloister.
 */
public final class Archive extends RoomArt {
    static final double GROUND = 294;

    @Override
    public String id() {
        return "archive";
    }

    @Override
    public Color tint() {
        return new Color(240, 228, 212);
    }

    @Override
    protected void paint(BufferedImage img, Graphics2D g) {
        walls(g, new Random(1357));
        ink(img, l -> shelves(l, new Random(1319)));
        ink(img, Archive::door);
        ink(img, Archive::bookLift);
        ink(img, Archive::rules);
        ink(img, Archive::windowFrame);
        floor(g, new Random(3));
        Graphics2D v = (Graphics2D) g.create();
        Brush.pool(v, 300, 340, 200, 40, c(0xffe0a0, 36));
        Brush.pool(v, 560, 320, 90, 26, c(0xf0f4ff, 40));
        Workshop.vignette(v);
        v.dispose();
    }

    @Override
    protected Map<String, Consumer<Graphics2D>> layers() {
        return named("lectern", (Consumer<Graphics2D>) Archive::lectern, "desk", (Consumer<Graphics2D>) Archive::desk);
    }

    @Override
    protected Map<String, Prop> props() {
        return Map.of(
                "desk-pencil", new Prop(Archive::pencil, 335),
                "lift-box", new Prop(Archive::liftBox, 260),
                "lunch-bag", new Prop(Archive::lunchBag, 261),
                "tower-window", new Prop(Archive::glass, 240),
                "tower-window-open", new Prop(Archive::openCasement, 240));
    }

    @Override
    public void light(Graphics2D g, double time) {
        Lights.lamp(g, 446, 266, 70, c(0xd8f0a0), 46);
        g.setPaint(new GradientPaint(580, 160, c(0xf0f4ff, 40), 520, 330, c(0xf0f4ff, 0)));
        g.fill(poly(544, 120, 618, 120, 600, 330, 470, 330));
    }

    // ---- behind ----

    static void walls(Graphics2D g, Random r) {
        g.setPaint(new GradientPaint(0, 0, c(0x5a4a3c), 0, (float) GROUND, c(0x9a8670)));
        g.fill(new Rectangle2D.Double(0, 0, W, GROUND));
        // ashlar stones
        for (double y = 4; y < GROUND; y += 18) {
            double off = ((int) (y / 18) % 2) * 20;
            for (double x = -off; x < W; x += 40) {
                g.setColor(c(Brush.mix(c(0x7a6a58), c(0xb0a088), r.nextDouble()).getRGB() & 0xffffff, 70));
                g.fill(new RoundRectangle2D.Double(x + 1, y + 1, 38, 16, 3, 3));
            }
        }
        // the vault: two ribs meeting over the lectern
        g.setColor(c(0x3a2e24));
        g.fill(poly(0, 0, W, 0, W, 30, 0, 30));
        g.setColor(c(0x6a5644));
        g.setStroke(new BasicStroke(5f));
        Path2D rib = new Path2D.Double();
        rib.moveTo(0, 70);
        rib.quadTo(280, -30, 640, 70);
        g.draw(rib);
        g.setStroke(new BasicStroke(1f));
    }

    static void shelves(Graphics2D g, Random r) {
        // tall wooden shelves with registers bound in vellum and leather, labels on the spines
        double x0 = 152, x1 = 410, top = 52;
        g.setColor(c(0x3a2414));
        g.fill(new Rectangle2D.Double(x0, top, x1 - x0, GROUND - top));
        Color[] bindings = {c(0xe8dcc0), c(0xd8c8a0), c(0x8a5a32), c(0x6a3a22), c(0xc8b490), c(0x7a2a1e)};
        for (double y = top + 6; y < GROUND - 30; y += 46) {
            for (double x = x0 + 6; x < x1 - 14; ) {
                double w = 9 + r.nextInt(8), h = 34 + r.nextInt(6);
                Color b = bindings[r.nextInt(bindings.length)];
                g.setPaint(new GradientPaint((float) x, 0, Brush.lighter(b, 0.15), (float) (x + w), 0, Brush.darker(b, 0.25)));
                g.fill(new Rectangle2D.Double(x, y + 40 - h, w, h));
                g.setColor(c(0xf4eedc));
                g.fill(new Rectangle2D.Double(x + 2, y + 44 - h, w - 4, 5));
                g.setColor(c(0x2a1a10, 120));
                g.fill(new Rectangle2D.Double(x + 1, y + 40 - h + 16, w - 2, 1));
                x += w + 1;
            }
            g.setColor(c(0x6a4426));
            g.fill(new Rectangle2D.Double(x0, y + 40, x1 - x0, 6));
        }
        g.setColor(c(0x5a3a22));
        g.fill(new Rectangle2D.Double(x0 - 4, top - 6, x1 - x0 + 8, 8));
        g.fill(new Rectangle2D.Double(x0 - 4, top, 6, GROUND - top));
        g.fill(new Rectangle2D.Double(x1 - 2, top, 6, GROUND - top));
    }

    static void door(Graphics2D g) {
        // the heavy door, studded, with an iron ring
        double x = 10, y = 162, w = 44, h = GROUND - 162;
        g.setColor(c(0x4a3a2c));
        g.fill(new RoundRectangle2D.Double(x - 6, y - 8, w + 12, h + 8, 24, 24));
        g.setPaint(new GradientPaint((float) x, 0, c(0x6a4426), (float) (x + w), 0, c(0x3a2414)));
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 20, 20));
        g.setColor(c(0x1a1a1e));
        for (double sy = y + 16; sy < GROUND; sy += 22)
            for (double sx = x + 6; sx < x + w; sx += 10) g.fill(new Ellipse2D.Double(sx, sy, 2.4, 2.4));
        g.setStroke(new BasicStroke(1.6f));
        g.draw(new Ellipse2D.Double(x + w - 14, y + 64, 9, 9));
        g.fill(new RoundRectangle2D.Double(x + w - 13, y + 54, 7, 10, 2, 2));
        g.setStroke(new BasicStroke(1f));
    }

    static void bookLift(Graphics2D g) {
        // the hatch of the book lift: a dark shaft with two ropes, and the crank wheel beside it
        double x = 98, y = 196, w = 50, h = 60;
        g.setColor(c(0x5a4232));
        g.fill(new Rectangle2D.Double(x - 4, y - 4, w + 8, h + 8));
        g.setPaint(new GradientPaint(0, (float) y, c(0x0e0a08), 0, (float) (y + h), c(0x2a2018)));
        g.fill(new Rectangle2D.Double(x, y, w, h));
        g.setColor(c(0x8a7a5a));
        g.setStroke(new BasicStroke(1f));
        g.draw(new Line2D.Double(x + 12, y, x + 12, y + h));
        g.draw(new Line2D.Double(x + w - 12, y, x + w - 12, y + h));
        g.setColor(c(0x2a2a2e));
        g.setStroke(new BasicStroke(2f));
        g.draw(new Ellipse2D.Double(x - 28, y + 18, 18, 18));
        g.draw(new Line2D.Double(x - 19, y + 27, x - 28, y + 38));
        g.fill(new Ellipse2D.Double(x - 31, y + 36, 5, 5));
        g.setStroke(new BasicStroke(1f));
        g.setColor(c(0xf0ead8));
        g.fill(new Rectangle2D.Double(x + 8, y - 16, 34, 9));
        g.setColor(c(0x3a3a3a));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 5));
        g.drawString("ELEVADOR", (float) x + 11, (float) y - 9.8f);
    }

    static void rules(Graphics2D g) {
        // the rules of the reading room, framed on the wall
        double x = 432, y = 172;
        g.setColor(c(0x3a2a1c));
        g.fill(new Rectangle2D.Double(x - 2, y - 2, 54, 34));
        g.setColor(c(0xf4f0e4));
        g.fill(new Rectangle2D.Double(x, y, 50, 30));
        g.setColor(c(0x2a2a2a));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 4));
        g.drawString("SALA DE LEITURA", (float) x + 6, (float) y + 7);
        g.setColor(c(0x5a5a5a));
        for (int i = 0; i < 4; i++) g.fill(new Rectangle2D.Double(x + 6, y + 11 + i * 4.5, 32 - i * 4, 1.2));
        g.setColor(c(0xb0201a));
        g.fill(new Rectangle2D.Double(x + 6, y + 22.6, 20, 0.8));
        g.fill(new Rectangle2D.Double(x + 6, y + 24, 20, 0.8));
    }

    static void windowFrame(Graphics2D g) {
        // the deep window opening, the sky and the hills beyond the cloister, and the iron bars
        double x = 540, y = 100, w = 80, h = 136;
        g.setColor(c(0x7a6650));
        g.fill(poly(x - 10, y + h + 6, x - 10, y - 6, x + w + 10, y - 6, x + w + 10, y + h + 6));
        g.setPaint(new GradientPaint(0, (float) y, c(0x8ab8e8), 0, (float) (y + h), c(0xe0e8f0)));
        g.fill(new Rectangle2D.Double(x, y, w, h));
        g.setColor(c(0x6a8a5a));
        g.fill(Brush.blob(x - 4, y + h - 30, x + 30, y + h - 52, x + 60, y + h - 40, x + w + 6, y + h - 56, x + w + 6, y + h,
                x - 4, y + h));
        g.setColor(c(0xb0705a));
        g.fill(poly(x, y + h - 16, x + w, y + h - 22, x + w, y + h, x, y + h));
        g.setColor(c(0xc8b8a0));
        g.fill(new Rectangle2D.Double(x - 12, y + h, w + 24, 8));
        g.setColor(c(0x2a2a2e));
        g.setStroke(new BasicStroke(2.6f));
        for (double bx = x + 16; bx < x + w; bx += 16) g.draw(new Line2D.Double(bx, y, bx, y + h));
        g.draw(new Line2D.Double(x, y + h / 2, x + w, y + h / 2));
        g.setStroke(new BasicStroke(1f));
    }

    static void floor(Graphics2D g, Random r) {
        g.setPaint(new GradientPaint(0, (float) GROUND, c(0x5a4232), 0, H, c(0x8a6a4a)));
        g.fill(new Rectangle2D.Double(0, GROUND, W, H - GROUND));
        // wide old floorboards running into the room
        g.setColor(c(0x3a2818, 120));
        for (int i = -12; i <= 12; i++) {
            double xb = 320 + i * 30, xf = 320 + i * 70;
            g.draw(new Line2D.Double(xb, GROUND, xf, H));
        }
        for (int i = 0; i < 40; i++) {
            g.setColor(c(0x2a1a0e, 40));
            double y = GROUND + r.nextDouble() * (H - GROUND);
            g.fill(new Rectangle2D.Double(r.nextDouble() * W, y, 6 + r.nextDouble() * 20, 0.8));
        }
    }

    // ---- props ----

    static void glass(Graphics2D g) {
        // the closed casement: leaded glass inside the bars
        double x = 540, y = 100, w = 80, h = 136;
        g.setColor(c(0xd8e8f0, 110));
        g.fill(new Rectangle2D.Double(x + 2, y + 2, w - 4, h - 4));
        g.setColor(c(0x4a4a4a, 160));
        g.setStroke(new BasicStroke(0.6f));
        for (double gy = y + 10; gy < y + h; gy += 12)
            for (double gx = x + 6; gx < x + w; gx += 12) g.draw(poly(gx, gy - 6, gx + 6, gy, gx, gy + 6, gx - 6, gy));
        g.setStroke(new BasicStroke(1f));
        g.setColor(c(0x5a4232));
        g.fill(new Rectangle2D.Double(x, y, w, 3));
        g.fill(new Rectangle2D.Double(x, y + h - 3, w, 3));
    }

    static void openCasement(Graphics2D g) {
        // the casement swung into the room, seen edge-on against the wall
        double x = 540, y = 100, h = 136;
        g.setColor(c(0x5a4232));
        g.fill(poly(x - 2, y, x - 26, y + 10, x - 26, y + h - 10, x - 2, y + h));
        g.setColor(c(0xd8e8f0, 120));
        g.fill(poly(x - 4, y + 4, x - 22, y + 12, x - 22, y + h - 12, x - 4, y + h - 4));
    }

    static void liftBox(Graphics2D g) {
        // the box of the book lift, up and open
        double x = 102, y = 210;
        g.setPaint(new GradientPaint((float) x, 0, c(0x8a5a32), (float) x + 42, 0, c(0x5a3a1e)));
        g.fill(new Rectangle2D.Double(x, y, 42, 44));
        g.setColor(c(0x2a1a10));
        g.fill(new Rectangle2D.Double(x + 4, y + 4, 34, 34));
        g.setColor(c(0x8a7a5a));
        g.draw(new Line2D.Double(x + 8, 196, x + 8, y));
        g.draw(new Line2D.Double(x + 34, 196, x + 34, y));
    }

    static void lunchBag(Graphics2D g) {
        // a brown paper bag from a bakery, folded over
        double x = 112, y = 248;
        g.setPaint(new GradientPaint((float) x, (float) y - 18, c(0xc8a070), (float) x + 22, (float) y, c(0x9a7448)));
        g.fill(poly(x, y, x + 2, y - 16, x + 20, y - 16, x + 22, y));
        g.setColor(c(0xd8b888));
        g.fill(poly(x + 2, y - 16, x + 20, y - 16, x + 18, y - 20, x + 4, y - 20));
        g.setColor(c(0x7a2a22));
        g.fill(new Ellipse2D.Double(x + 8, y - 10, 6, 4));
    }

    static void pencil(Graphics2D g) {
        g.setColor(c(0x2a5a3a));
        g.fill(poly(488, 294.4, 504, 292.4, 504.4, 294.2, 488.4, 296));
        g.setColor(c(0xe0c090));
        g.fill(poly(504, 292.4, 508, 293, 504.4, 294.2));
    }

    // ---- in front ----

    static void lectern(Graphics2D g) {
        // a wooden lectern with a slanted top, and the great register lying open on it
        double cx = 278, base = 338;
        g.setColor(c(0x1a0e05, 90));
        g.fill(new Ellipse2D.Double(cx - 30, base - 4, 60, 8));
        g.setPaint(new GradientPaint((float) cx - 8, 0, c(0x6a4426), (float) cx + 8, 0, c(0x3a2414)));
        g.fill(new Rectangle2D.Double(cx - 6, 296, 12, base - 296));
        g.fill(poly(cx - 26, base, cx + 26, base, cx + 20, base - 6, cx - 20, base - 6));
        g.setColor(c(0x5a3a22));
        g.fill(poly(cx - 36, 300, cx + 36, 300, cx + 32, 286, cx - 32, 286));
        // the register: two pages, columns of text, a red initial
        g.setPaint(new GradientPaint(0, 276, c(0xf4ead0), 0, 296, c(0xd8c8a0)));
        g.fill(poly(cx - 34, 296, cx, 298, cx, 280, cx - 32, 276));
        g.fill(poly(cx + 34, 296, cx, 298, cx, 280, cx + 32, 276));
        g.setColor(c(0x8a5a32));
        g.fill(poly(cx - 36, 297, cx + 36, 297, cx + 36, 300, cx - 36, 300));
        g.setColor(c(0x5a4a3a, 160));
        for (int i = 0; i < 5; i++) {
            g.fill(new Rectangle2D.Double(cx - 28, 281 + i * 3, 24, 0.7));
            g.fill(new Rectangle2D.Double(cx + 4, 281 + i * 3, 24, 0.7));
        }
        g.setColor(c(0xb0201a));
        g.fill(new Rectangle2D.Double(cx - 29, 280, 3, 4));
    }

    static void desk(Graphics2D g) {
        // the reading desk: a green lamp, foam wedges, a pad of paper
        double x = 420, w = 104, top = 296, base = 334;
        g.setColor(c(0x1a0e05, 90));
        g.fill(new Ellipse2D.Double(x - 4, base - 4, w + 8, 8));
        g.setColor(c(0x4a2e1c));
        g.fill(new Rectangle2D.Double(x + 4, top + 4, 5, base - top - 4));
        g.fill(new Rectangle2D.Double(x + w - 9, top + 4, 5, base - top - 4));
        g.setPaint(new GradientPaint(0, (float) top, c(0x7a5032), 0, (float) top + 8, c(0x4a2e1c)));
        g.fill(new Rectangle2D.Double(x, top, w, 8));
        g.setColor(c(0x5a7a3a));
        g.fill(poly(x + 40, top, x + 60, top, x + 56, top - 6, x + 44, top - 6));
        g.fill(poly(x + 64, top, x + 82, top, x + 78, top - 6, x + 68, top - 6));
        // the lamp
        g.setColor(c(0xb89a5a));
        g.fill(new Rectangle2D.Double(x + 24, top - 30, 2.4, 30));
        g.fill(new Ellipse2D.Double(x + 18, top - 3, 14, 4));
        g.setPaint(new GradientPaint(0, (float) top - 36, c(0x4a8a4a), 0, (float) top - 26, c(0x1e4a2a)));
        g.fill(poly(x + 12, top - 26, x + 40, top - 26, x + 34, top - 36, x + 18, top - 36));
        g.setColor(c(0xfff0b0));
        g.fill(new Rectangle2D.Double(x + 14, top - 26, 24, 1.6));
    }
}
