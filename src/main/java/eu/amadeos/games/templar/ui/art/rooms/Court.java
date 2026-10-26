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
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

import static eu.amadeos.games.templar.ui.art.Brush.c;
import static eu.amadeos.games.templar.ui.art.Brush.poly;

/**
 * The Cour du Temple in La Rochelle at night: an old wall on the left, older than the houses, with a worn stone
 * of two riders on one horse behind a café's menu board; the café "Le Templier" on the right with its terrace,
 * a string of lights across the courtyard, and the street sign on the house at the back.
 */
public final class Court extends RoomArt {
    static final double GROUND = 284;

    @Override
    public String id() {
        return "court";
    }

    @Override
    public Color tint() {
        return new Color(214, 206, 230);
    }

    @Override
    protected void paint(BufferedImage img, Graphics2D g) {
        sky(g, new Random(1139));
        ink(img, l -> houses(l, new Random(21)));
        ink(img, l -> oldWall(l, new Random(1307)));
        ink(img, Court::cafe);
        ground(g, new Random(2));
        ink(img, Court::wires);
        Graphics2D v = (Graphics2D) g.create();
        Brush.pool(v, 500, 320, 200, 50, c(0xffc870, 46));
        Brush.pool(v, 150, 300, 90, 24, c(0xffd890, 30));
        Workshop.vignette(v);
        v.dispose();
    }

    @Override
    protected Map<String, Consumer<Graphics2D>> layers() {
        return named("table", (Consumer<Graphics2D>) Court::table);
    }

    @Override
    protected Map<String, Prop> props() {
        return Map.of(
                "relief", new Prop(Court::relief, 200),
                "hollow", new Prop(Court::hollow, 200),
                "chalkboard", new Prop(l -> chalkboard(l, 144, 292), 292),
                "moved-board", new Prop(l -> chalkboard(l, 253, 292), 292),
                "loose-stone", new Prop(Court::looseStone, 300),
                "napkin", new Prop(Court::napkin, 341));
    }

    @Override
    public void live(Graphics2D g, double time) {
        // the bulbs of the string of lights, each flickering a little on its own
        for (int i = 0; i < 18; i++) {
            double t = i / 17.0, x = 250 + t * 390, y = 124 + 20 * Math.sin(t * Math.PI) - t * 6;
            double a = 0.8 + 0.2 * Math.sin(time * 3 + i * 1.7);
            Brush.glow(g, x, y + 3, 7, c(0xffd080, (int) (90 * a)));
            g.setColor(c(0xfff0c0));
            g.fill(new Ellipse2D.Double(x - 1.3, y + 1.6, 2.6, 3));
        }
    }

    @Override
    public void light(Graphics2D g, double time) {
        Lights.lamp(g, 488, 200, 120, c(0xffc870), 50);
        Lights.lamp(g, 210, 128, 70, c(0xffd890), 50);
        Lights.flame(g, 210, 136, time, 0.9);
    }

    // ---- the night ----

    static void sky(Graphics2D g, Random r) {
        g.setPaint(new GradientPaint(0, 0, c(0x101634), 0, 120, c(0x2a2c52)));
        g.fill(new Rectangle2D.Double(0, 0, W, GROUND));
        for (int i = 0; i < 50; i++) {
            g.setColor(c(0xeef0ff, 50 + r.nextInt(110)));
            double d = 0.6 + r.nextDouble() * 0.8;
            g.fill(new Ellipse2D.Double(r.nextInt(W), r.nextInt(60), d, d));
        }
    }

    static void houses(Graphics2D g, Random r) {
        // the house at the back of the courtyard, pale stone, a big arched doorway, shutters, the street sign
        double x = 230, w = 230, top = 50;
        g.setPaint(new GradientPaint(0, (float) top, c(0xc8b8a0), 0, (float) GROUND, c(0x8a7a6a)));
        g.fill(new Rectangle2D.Double(x, top, w, GROUND - top));
        g.setColor(c(0x4a4658));
        g.fill(poly(x - 6, top + 2, x + 20, top - 26, x + w - 20, top - 26, x + w + 6, top + 2));
        g.setColor(c(0x2a1e18));
        Path2D arch = new Path2D.Double();
        arch.moveTo(x + 90, GROUND);
        arch.lineTo(x + 90, 228);
        arch.curveTo(x + 90, 196, x + 150, 196, x + 150, 228);
        arch.lineTo(x + 150, GROUND);
        arch.closePath();
        g.fill(arch);
        g.setPaint(new GradientPaint((float) x + 92, 0, c(0x5a3a24), (float) x + 148, 0, c(0x3a2414)));
        Path2D door = new Path2D.Double();
        door.moveTo(x + 94, GROUND);
        door.lineTo(x + 94, 230);
        door.curveTo(x + 94, 202, x + 146, 202, x + 146, 230);
        door.lineTo(x + 146, GROUND);
        door.closePath();
        g.fill(door);
        for (double wy : new double[]{78, 124}) {
            for (double wx : new double[]{x + 24, x + 96, x + 168}) {
                boolean lit = r.nextInt(4) == 0;
                g.setColor(lit ? c(0xffcc70) : c(0x2a2e44));
                g.fill(new Rectangle2D.Double(wx, wy, 22, 30));
                if (lit) Brush.glow(g, wx + 11, wy + 15, 24, c(0xffc060, 50));
                g.setColor(c(0x5a6a7a));
                g.fill(new Rectangle2D.Double(wx - 8, wy, 7, 30));
                g.fill(new Rectangle2D.Double(wx + 23, wy, 7, 30));
            }
        }
        // the blue street sign
        g.setColor(c(0x1e3a7a));
        g.fill(new RoundRectangle2D.Double(282, 156, 56, 18, 3, 3));
        g.setColor(c(0xf0f0f0));
        g.setStroke(new BasicStroke(0.7f));
        g.draw(new RoundRectangle2D.Double(284, 158, 52, 14, 2, 2));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 5));
        g.drawString("COUR DU", 300, 164.5f);
        g.drawString("TEMPLE", 301, 170.5f);
        g.setStroke(new BasicStroke(1f));
    }

    static void oldWall(Graphics2D g, Random r) {
        // the old wall on the left: rough, dark, much older than the houses, with a wall lantern
        g.setPaint(new GradientPaint(0, 0, c(0x8a7a6a), 240, 0, c(0x5a4c42)));
        g.fill(poly(0, 60, 236, 80, 236, GROUND, 0, GROUND));
        for (double by = 82; by < GROUND; by += 14) {
            double off = ((int) by / 14 % 2) * 14;
            for (double bx = -off; bx < 236; bx += 28 + r.nextInt(6)) {
                Color stone = Brush.mix(c(0x9a8876), c(0x6a5a4c), r.nextDouble());
                g.setColor(Brush.c(stone.getRGB() & 0xffffff, 120));
                g.fill(new RoundRectangle2D.Double(bx + 1, by + 1, 26, 12, 4, 4));
            }
        }
        g.setColor(c(0x4a3c32));
        g.fill(poly(0, 56, 236, 76, 236, 82, 0, 64));
        // ivy at the top of the wall
        g.setColor(c(0x2e4a2a));
        for (int i = 0; i < 40; i++) {
            double x = r.nextInt(236), y = 70 + r.nextInt(30) + (x < 60 ? 40 : 0);
            g.fill(new Ellipse2D.Double(x, y, 7, 5));
        }
        // the wall lantern
        g.setColor(c(0x1e1e22));
        g.fill(new Rectangle2D.Double(204, 120, 12, 2));
        g.fill(poly(203, 146, 217, 146, 215, 128, 205, 128));
        g.fill(poly(204, 128, 210, 120, 216, 128));
        g.setColor(c(0xffe0a0));
        g.fill(poly(205, 144, 215, 144, 213.5, 130, 206.5, 130));
    }

    static void relief(Graphics2D g) {
        // a worn stone set in the wall: two knights on one horse, shields and lances, as on the seal
        double x = 114, y = 180, w = 66, h = 48;
        g.setPaint(new GradientPaint((float) x, (float) y, c(0xb8a890), (float) (x + w), (float) (y + h), c(0x7a6a5a)));
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 4, 4));
        g.setColor(c(0x5a4a3c));
        g.setStroke(new BasicStroke(0.9f));
        g.draw(new RoundRectangle2D.Double(x + 3, y + 3, w - 6, h - 6, 3, 3));
        double cx = x + 33, cy = y + 30;
        g.setColor(c(0x6a5a4a));
        Path2D horse = new Path2D.Double();
        horse.moveTo(cx - 16, cy);
        horse.curveTo(cx - 14, cy - 6, cx + 8, cy - 7, cx + 14, cy - 4);
        horse.lineTo(cx + 20, cy - 12);
        horse.lineTo(cx + 22, cy - 9);
        horse.curveTo(cx + 18, cy - 2, cx + 16, cy + 4, cx + 10, cy + 5);
        horse.lineTo(cx - 12, cy + 5);
        horse.closePath();
        g.fill(horse);
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(cx + 8, cy + 4, cx + 12, cy + 13));
        g.draw(new Line2D.Double(cx - 10, cy + 4, cx - 14, cy + 13));
        g.draw(new Line2D.Double(cx + 2, cy + 4, cx + 2, cy + 13));
        g.draw(new Line2D.Double(cx - 4, cy + 4, cx - 6, cy + 13));
        for (double kx : new double[]{cx - 6, cx + 3}) {
            g.fill(new RoundRectangle2D.Double(kx - 2.5, cy - 15, 5, 10, 2, 2));
            g.fill(new Ellipse2D.Double(kx - 2.2, cy - 19.5, 4.4, 4.8));
            g.draw(new Line2D.Double(kx + 1, cy - 12, kx + 14, cy - 22));
        }
        g.fill(poly(cx - 12, cy - 14, cx - 6, cy - 14, cx - 6, cy - 7, cx - 9, cy - 4, cx - 12, cy - 7));
        g.setStroke(new BasicStroke(1f));
        // cracks and lichen
        g.setColor(c(0x4a3c30, 140));
        g.draw(new Line2D.Double(x + 50, y + 4, x + 56, y + 20));
        g.setColor(c(0x8a9a5a, 120));
        g.fill(new Ellipse2D.Double(x + 4, y + 36, 9, 6));
        g.fill(new Ellipse2D.Double(x + 54, y + 8, 6, 4));
    }

    static void hollow(Graphics2D g) {
        // where the stone was: a dark hollow in the wall, with the old mortar crumbled round it
        double x = 116, y = 182;
        g.setColor(c(0x6a5a4c));
        g.fill(new RoundRectangle2D.Double(x - 2, y - 2, 66, 48, 6, 6));
        g.setPaint(new GradientPaint((float) x, (float) y, c(0x0e0a08), (float) x, (float) y + 44, c(0x2a2018)));
        g.fill(new RoundRectangle2D.Double(x + 4, y + 4, 54, 36, 4, 4));
        g.setColor(c(0xc8c0b0, 90));
        g.setStroke(new BasicStroke(0.5f));
        g.draw(new QuadCurve2D.Double(x + 8, y + 6, x + 16, y + 14, x + 12, y + 22));
        g.setStroke(new BasicStroke(1f));
    }

    static void looseStone(Graphics2D g) {
        // the stone, lifted out and leaning on the wall below the hollow
        double x = 188, y = 298;
        g.setColor(c(0x1a0e05, 90));
        g.fill(new Ellipse2D.Double(x - 4, y - 4, 44, 7));
        g.setPaint(new GradientPaint((float) x, (float) y - 14, c(0xb8a890), (float) x + 36, (float) y, c(0x7a6a5a)));
        g.fill(poly(x, y, x + 2, y - 14, x + 36, y - 16, x + 38, y));
        g.setColor(c(0x6a5a4a));
        g.fill(new Ellipse2D.Double(x + 12, y - 12, 14, 6));
    }

    static void chalkboard(Graphics2D g, double cx, double foot) {
        // the café's A-frame menu board, the menu written in a cipher of squares, angles and dots
        double w = 52, h = 70, top = foot - h - 12;
        g.setColor(c(0x1a0e05, 90));
        g.fill(new Ellipse2D.Double(cx - 32, foot - 4, 64, 8));
        g.setColor(c(0x5a3a22));
        g.setStroke(new BasicStroke(2.4f));
        g.draw(new Line2D.Double(cx - w / 2 + 2, top + 4, cx - w / 2 - 4, foot));
        g.draw(new Line2D.Double(cx + w / 2 - 2, top + 4, cx + w / 2 + 4, foot));
        g.setStroke(new BasicStroke(1f));
        g.setColor(c(0x6a4a2e));
        g.fill(new RoundRectangle2D.Double(cx - w / 2 - 3, top - 3, w + 6, h + 6, 3, 3));
        g.setColor(c(0x22282a));
        g.fill(new Rectangle2D.Double(cx - w / 2, top, w, h));
        g.setColor(c(0xe8e4d8));
        g.setFont(new Font(Font.SERIF, Font.BOLD | Font.ITALIC, 6));
        g.drawString("Le Templier", (float) (cx - 16), (float) (top + 9));
        g.setStroke(new BasicStroke(0.6f));
        Random r = new Random(12);
        for (int row = 0; row < 5; row++) {
            double y = top + 16 + row * 10;
            int n = 4 + r.nextInt(3);
            for (int i = 0; i < n; i++) {
                double x = cx - w / 2 + 5 + i * 7.4;
                int shape = r.nextInt(5);
                switch (shape) {
                    case 0 -> g.draw(new Rectangle2D.Double(x, y, 5, 5));
                    case 1 -> g.draw(poly(x, y, x + 5, y, x + 5, y + 5, x + 5, y, x, y));
                    case 2 -> g.draw(new Line2D.Double(x, y, x + 2.5, y + 5));
                    case 3 -> g.draw(poly(x, y, x + 2.5, y + 5, x + 5, y, x + 2.5, y + 5));
                    default -> g.draw(new Arc2D.Double(x, y - 2, 5, 7, 180, 180, Arc2D.OPEN));
                }
                if (r.nextBoolean()) g.fill(new Ellipse2D.Double(x + 1.8, y + 1.6, 1.4, 1.4));
            }
        }
        g.setStroke(new BasicStroke(1f));
    }

    static void cafe(Graphics2D g) {
        // the café on the right: warm window, a red awning, the door, a lamp over the sign
        double x = 444, top = 40;
        g.setPaint(new GradientPaint(0, (float) top, c(0xd0c0a8), 0, (float) GROUND, c(0x9a8a76)));
        g.fill(new Rectangle2D.Double(x, top, W - x, GROUND - top));
        g.setColor(c(0x4a4658));
        g.fill(poly(x - 4, top + 2, x + 20, top - 22, W, top - 22, W, top + 2));
        for (double wx : new double[]{x + 24, x + 96, x + 160}) {
            g.setColor(c(0x2a2e44));
            g.fill(new Rectangle2D.Double(wx, 70, 22, 30));
            g.setColor(c(0x5a6a7a));
            g.fill(new Rectangle2D.Double(wx - 7, 70, 6, 30));
            g.fill(new Rectangle2D.Double(wx + 23, 70, 6, 30));
        }
        // the sign and the awning
        g.setColor(c(0x2a1e18));
        g.fill(new Rectangle2D.Double(x + 10, 118, 150, 18));
        g.setColor(c(0xe8c870));
        g.setFont(new Font(Font.SERIF, Font.BOLD, 11));
        g.drawString("LE TEMPLIER", (float) x + 40, 131f);
        g.setColor(c(0x9a2a22));
        g.fill(poly(x + 4, 140, x + 176, 140, x + 186, 158, x - 6, 158));
        g.setColor(c(0xe8e0d0));
        for (double sx = x + 4; sx < x + 176; sx += 20) g.fill(poly(sx + 8, 140, sx + 16, 140, sx + 18, 158, sx + 10, 158));
        // the window: the warm room inside, tables, a bar
        double wx = 452, wy = 162;
        g.setPaint(new GradientPaint(0, (float) wy, c(0xffd890), 0, (float) wy + 78, c(0xd89850)));
        g.fill(new Rectangle2D.Double(wx, wy, 70, 78));
        g.setColor(c(0x8a5a30));
        g.fill(new Rectangle2D.Double(wx, wy + 52, 70, 26));
        g.setColor(c(0x6a3a1e));
        for (double bx = wx + 8; bx < wx + 70; bx += 18) g.fill(new Ellipse2D.Double(bx, wy + 44, 10, 4));
        g.setColor(c(0x3a2a1e));
        g.setStroke(new BasicStroke(2.4f));
        g.draw(new Rectangle2D.Double(wx, wy, 70, 78));
        g.draw(new Line2D.Double(wx + 35, wy, wx + 35, wy + 78));
        g.setStroke(new BasicStroke(1f));
        // the door, open, light falling out
        g.setPaint(new GradientPaint(0, 170, c(0xffd890), 0, (float) GROUND, c(0xc88040)));
        g.fill(new Rectangle2D.Double(534, 170, 34, GROUND - 170));
        g.setColor(c(0x3a2a1e));
        g.fill(poly(534, 170, 546, 176, 546, GROUND + 6, 534, GROUND));
        // a planter by the door
        g.setColor(c(0x6a3a2a));
        g.fill(new Rectangle2D.Double(590, 252, 36, 30));
        g.setColor(c(0x3a5a2a));
        g.fill(Brush.blob(586, 254, 596, 226, 610, 232, 624, 222, 632, 254));
    }

    static void wires(Graphics2D g) {
        // the string of lights across the courtyard, from the old wall to the café
        g.setColor(c(0x1a1a1e));
        g.setStroke(new BasicStroke(0.8f));
        Path2D wire = new Path2D.Double();
        wire.moveTo(250, 124);
        for (int i = 1; i <= 34; i++) {
            double t = i / 34.0;
            wire.lineTo(250 + t * 390, 124 + 20 * Math.sin(t * Math.PI) - t * 6);
        }
        g.draw(wire);
        g.setStroke(new BasicStroke(1f));
    }

    static void ground(Graphics2D g, Random r) {
        g.setPaint(new GradientPaint(0, (float) GROUND, c(0x4a4448), 0, H, c(0x7a6e66)));
        g.fill(new Rectangle2D.Double(0, GROUND, W, H - GROUND));
        for (double y = GROUND + 4; y < H; y += 6 + (y - GROUND) * 0.08) {
            double step = 10 + (y - GROUND) * 0.16;
            for (double x = r.nextDouble() * step; x < W; x += step) {
                g.setColor(Brush.c(Brush.mix(c(0x5a5254), c(0x8a7e74), r.nextDouble()).getRGB() & 0xffffff, 140));
                g.fill(new RoundRectangle2D.Double(x, y, step * 0.86, 2.4 + (y - GROUND) * 0.04, 2, 2));
            }
        }
    }

    // ---- in front ----

    static void table(Graphics2D g) {
        // a round terrace table with two chairs, a glass and a basket of bread
        double cx = 420, top = 312;
        g.setColor(c(0x1a0e05, 90));
        g.fill(new Ellipse2D.Double(cx - 40, 334, 80, 10));
        g.setColor(c(0x2a2a2e));
        g.setStroke(new BasicStroke(2.4f));
        g.draw(new Line2D.Double(cx, top + 4, cx, 338));
        g.draw(new Line2D.Double(cx - 12, 338, cx + 12, 338));
        g.draw(new Line2D.Double(cx - 34, 300, cx - 34, 340));
        g.draw(new Line2D.Double(cx - 46, 318, cx - 46, 340));
        g.draw(new Line2D.Double(cx - 46, 318, cx - 28, 318));
        g.setStroke(new BasicStroke(1f));
        g.setPaint(new GradientPaint((float) cx - 30, 0, c(0x9aa0a8), (float) cx + 30, 0, c(0x5a6068)));
        g.fill(new Ellipse2D.Double(cx - 30, top - 4, 60, 10));
        g.setColor(c(0xd8e8f0, 170));
        g.fill(new RoundRectangle2D.Double(cx + 10, top - 14, 6, 10, 1.5, 1.5));
        g.setColor(c(0xa06a30));
        g.fill(new Ellipse2D.Double(cx - 20, top - 8, 16, 6));
        g.setColor(c(0xe0b070));
        g.fill(new Ellipse2D.Double(cx - 18, top - 10, 12, 4));
    }

    static void napkin(Graphics2D g) {
        // a printed paper napkin on the table
        double x = 400, y = 311;
        g.setColor(c(0xf4f0e6));
        g.fill(poly(x, y + 2, x + 18, y, x + 22, y + 4, x + 4, y + 6));
        g.setColor(c(0x9a2a22));
        g.setStroke(new BasicStroke(0.4f));
        g.draw(new Line2D.Double(x + 4, y + 3, x + 16, y + 1.6));
        g.draw(new Line2D.Double(x + 6, y + 4.4, x + 18, y + 3));
        g.setStroke(new BasicStroke(1f));
    }
}
