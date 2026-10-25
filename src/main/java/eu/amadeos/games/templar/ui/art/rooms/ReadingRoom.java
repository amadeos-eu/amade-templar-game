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
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

import static eu.amadeos.games.templar.ui.art.Brush.c;
import static eu.amadeos.games.templar.ui.art.Brush.poly;

/**
 * The reading room of the library after closing time: tall shelves with a ladder, a window at dusk, green
 * reading lamps, Monsieur Lefort's desk on the right and, in the corner, the coffee machine he is not on
 * speaking terms with.
 */
public final class ReadingRoom extends RoomArt {
    static final double WALL = 270;

    @Override
    public String id() {
        return "reading";
    }

    @Override
    public Color tint() {
        return new Color(255, 240, 222);
    }

    @Override
    protected void paint(BufferedImage img, Graphics2D g) {
        wall(g);
        floor(g);
        ink(img, ReadingRoom::window);
        ink(img, l -> shelves(l, 228, 34, 196, 232, new Random(21)));
        ink(img, ReadingRoom::ladder);
        ink(img, ReadingRoom::door);
        ink(img, ReadingRoom::clock);
        ink(img, ReadingRoom::archway);
        ink(img, ReadingRoom::machine);
        ink(img, ReadingRoom::rack);
        ink(img, l -> shelves(l, 440, 214, 100, 52, new Random(4)));
        Graphics2D v = (Graphics2D) g.create();
        Brush.pool(v, 300, 300, 260, 90, c(0xffd890, 26));
        Workshop.vignette(v);
        v.dispose();
    }

    @Override
    protected Map<String, Consumer<Graphics2D>> layers() {
        return named("desk", (Consumer<Graphics2D>) ReadingRoom::desk,
                "table", (Consumer<Graphics2D>) ReadingRoom::table);
    }

    @Override
    protected Map<String, Prop> props() {
        return Map.of("lefort-cup", new Prop(l -> cup(l, 138, 236), 293));
    }

    @Override
    public void light(Graphics2D g, double time) {
        Lights.lamp(g, 318, 296, 70, c(0xffe6a0), 60);
        Lights.lamp(g, 470, 236, 60, c(0xffe6a0), 50);
    }

    // ---- the room ----

    static void wall(Graphics2D g) {
        g.setPaint(new GradientPaint(0, 0, c(0x8a6a48), 0, (float) WALL, c(0xb89a72)));
        g.fill(new Rectangle2D.Double(0, 0, W, WALL));
        Random r = new Random(9);
        for (int i = 0; i < 200; i++) {
            double x = r.nextInt(W), y = r.nextInt((int) WALL), s = 8 + r.nextInt(24);
            g.setColor(r.nextBoolean() ? c(0xf4e2c0, 7) : c(0x3a2410, 6));
            g.fill(new Ellipse2D.Double(x - s, y - s / 2, s * 2, s));
        }
        // the cornice and the wainscot
        g.setColor(c(0x5a3a20));
        g.fill(new Rectangle2D.Double(0, 0, W, 22));
        g.setColor(c(0xd8b888));
        g.fill(new Rectangle2D.Double(0, 22, W, 3));
        g.setPaint(new GradientPaint(0, 226, c(0x5b3a20), 0, (float) WALL, c(0x3e2613)));
        g.fill(new Rectangle2D.Double(0, 226, W, WALL - 226));
        g.setColor(c(0x7a5230));
        g.fill(new Rectangle2D.Double(0, 224, W, 3));
        g.setColor(c(0x2a190b, 110));
        for (int x = 6; x < W; x += 52) g.draw(new Rectangle2D.Double(x, 232, 44, 30));
    }

    static void floor(Graphics2D g) {
        g.setPaint(new GradientPaint(0, (float) WALL, c(0x6a4224), 0, H, c(0xa06c3c)));
        g.fill(new Rectangle2D.Double(0, WALL, W, H - WALL));
        // herringbone parquet in perspective
        g.setStroke(new BasicStroke(0.7f));
        double vx = 320, vy = 90;
        for (int row = 0; row < 14; row++) {
            double t0 = row / 14.0, t1 = (row + 1) / 14.0;
            double y0 = WALL + Math.pow(t0, 1.5) * (H - WALL), y1 = WALL + Math.pow(t1, 1.5) * (H - WALL);
            g.setColor(c(0x3c2412, 70));
            g.draw(new Line2D.Double(0, y0, W, y0));
            double k0 = (y0 - vy) / (H - vy), k1 = (y1 - vy) / (H - vy);
            for (int i = -20; i <= 20; i++) {
                double bx = vx + i * 38;
                double x0 = vx + (bx - vx) * k0, x1 = vx + (bx - vx) * k1;
                double xm = (x0 + x1) / 2 + (row % 2 == 0 ? 6 : -6) * k1;
                g.setColor(c(0x3c2412, 60));
                g.draw(new Line2D.Double(x0, y0, xm, y1));
            }
        }
        g.setStroke(new BasicStroke(1f));
        // a long carpet down the middle
        Path2D carpet = poly(250, WALL + 8, 390, WALL + 8, 470, H, 170, H);
        g.setPaint(new GradientPaint(0, (float) WALL, c(0x5a1a18), 0, H, c(0x8a2a22)));
        g.fill(carpet);
        g.setColor(c(0xc89a50, 120));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(poly(258, WALL + 12, 382, WALL + 12, 456, H - 4, 184, H - 4));
        g.setStroke(new BasicStroke(1f));
    }

    static void window(Graphics2D g) {
        double x = 440, y = 46, w = 110, h = 160;
        Path2D arch = new Path2D.Double();
        arch.moveTo(x, y + h);
        arch.lineTo(x, y + w / 2);
        arch.append(new Arc2D.Double(x, y, w, w, 180, -180, Arc2D.OPEN), true);
        arch.lineTo(x + w, y + h);
        arch.closePath();
        g.setPaint(new GradientPaint(0, (float) y, c(0x2a3a7a), 0, (float) (y + h), c(0xd88a6a)));
        g.fill(arch);
        var clip = g.getClip();
        g.clip(arch);
        g.setColor(c(0x3a3a58));
        g.fill(poly(x, y + 130, x + 30, y + 118, x + 30, y + 100, x + 52, y + 100, x + 52, y + 120, x + 80, y + 112, x + 110, y + 126, x + 110, y + h, x, y + h));
        g.setColor(c(0xffcf6a));
        Random r = new Random(2);
        for (int i = 0; i < 7; i++) g.fill(new Rectangle2D.Double(x + 6 + r.nextInt(98), y + 132 + r.nextInt(22), 2.5, 3.5));
        g.setColor(c(0xfff4d8));
        g.fill(new Ellipse2D.Double(x + 76, y + 34, 9, 9));
        g.setClip(clip);
        g.setColor(c(0x3a2414));
        g.setStroke(new BasicStroke(3.5f));
        g.draw(arch);
        g.setStroke(new BasicStroke(2.2f));
        g.draw(new Line2D.Double(x + w / 2, y + 4, x + w / 2, y + h));
        g.draw(new Line2D.Double(x, y + 80, x + w, y + 80));
        g.setStroke(new BasicStroke(1f));
        // heavy green curtains
        for (int side = 0; side < 2; side++) {
            double cx = side == 0 ? x - 18 : x + w - 6;
            Path2D curtain = poly(cx, y - 14, cx + 24, y - 14, cx + 20 - side * 6, y + 70, cx + 26 - side * 10, y + h + 20, cx - 2, y + h + 20);
            g.setPaint(new GradientPaint((float) cx, 0, c(0x2e5a3a), (float) cx + 24, 0, c(0x16301e)));
            g.fill(curtain);
            g.setColor(c(0x0e2014, 140));
            g.draw(new Line2D.Double(cx + 8, y - 10, cx + 7, y + h + 18));
            g.draw(new Line2D.Double(cx + 15, y - 10, cx + 16, y + h + 18));
        }
        g.setColor(c(0x8a6a30));
        g.fill(new RoundRectangle2D.Double(x - 26, y - 18, w + 52, 5, 3, 3));
    }

    static void shelves(Graphics2D g, double x, double y, double w, double h, Random rnd) {
        g.setPaint(new GradientPaint((float) x, 0, c(0x4a2c16), (float) (x + w), 0, c(0x2a170a)));
        g.fill(new Rectangle2D.Double(x, y, w, h));
        String[] spines = {"7a2a1e", "2f4a3a", "6b4a1c", "2b3550", "8a6a2c", "4e2638", "3d3d2a", "9a7a4a", "5a1f14", "1f3a3a"};
        int rows = (int) (h / 38);
        for (int row = 0; row < rows; row++) {
            double sy = y + 6 + row * 38;
            double sx = x + 5;
            while (sx < x + w - 8) {
                double bw = 3.5 + rnd.nextInt(5), bh = 22 + rnd.nextInt(9);
                Color col = c(Integer.parseInt(spines[rnd.nextInt(spines.length)], 16));
                g.setPaint(new GradientPaint((float) sx, 0, col.brighter(), (float) (sx + bw), 0, col.darker()));
                g.fill(new Rectangle2D.Double(sx, sy + 30 - bh, bw, bh));
                g.setColor(c(0xd8b860, 120));
                g.fill(new Rectangle2D.Double(sx, sy + 30 - bh + 3, bw, 0.8));
                sx += bw + 0.8;
            }
            g.setColor(c(0x5e3a1e));
            g.fill(new Rectangle2D.Double(x, sy + 30, w, 4));
            g.setColor(c(0x1a0e05, 120));
            g.fill(new Rectangle2D.Double(x, sy + 34, w, 2.5));
        }
        g.setColor(c(0x3a2210));
        g.fill(new Rectangle2D.Double(x - 3, y - 5, w + 6, 6));
        g.fill(new Rectangle2D.Double(x - 3, y, 5, h));
        g.fill(new Rectangle2D.Double(x + w - 2, y, 5, h));
    }

    static void ladder(Graphics2D g) {
        g.setColor(c(0x6a4424));
        g.setStroke(new BasicStroke(3f));
        g.draw(new Line2D.Double(372, 30, 392, 266));
        g.draw(new Line2D.Double(392, 30, 412, 266));
        g.setStroke(new BasicStroke(2f));
        for (int i = 0; i < 11; i++) {
            double t = (i + 0.5) / 11, y = 30 + t * 236;
            g.draw(new Line2D.Double(372 + t * 20, y, 392 + t * 20, y));
        }
        g.setColor(c(0xb08a40));
        g.fill(new Rectangle2D.Double(228, 28, 196, 3));
        g.setStroke(new BasicStroke(1f));
    }

    static void door(Graphics2D g) {
        double x = 156, y = 112, w = 62, h = 158;
        g.setColor(c(0x24140a));
        g.fill(new Rectangle2D.Double(x - 6, y - 6, w + 12, h + 6));
        g.setPaint(new GradientPaint((float) x, 0, c(0x4a2c16), (float) (x + w), 0, c(0x36200f)));
        g.fill(new Rectangle2D.Double(x, y, w, h));
        g.setPaint(new GradientPaint(0, (float) y + 8, c(0xd9c89a), 0, (float) y + 80, c(0x9a8a66)));
        g.fill(new Rectangle2D.Double(x + 8, y + 8, w - 16, 70));
        g.setColor(c(0x3a2414));
        g.setStroke(new BasicStroke(2f));
        g.draw(new Rectangle2D.Double(x + 8, y + 8, w - 16, 70));
        g.draw(new Line2D.Double(x + w / 2, y + 8, x + w / 2, y + 78));
        g.setStroke(new BasicStroke(1f));
        g.setColor(c(0x22130a, 160));
        g.draw(new Rectangle2D.Double(x + 8, y + 92, w - 16, 54));
        g.setColor(c(0xb08a40));
        g.fill(new Ellipse2D.Double(x + w - 11, y + 84, 5, 5));
        // a brass plate: ATELIER
        g.fill(new Rectangle2D.Double(x + 18, y - 20, 26, 8));
        g.setColor(c(0x5a4010));
        g.fill(new Rectangle2D.Double(x + 21, y - 17, 20, 1.5));
    }

    static void clock(Graphics2D g) {
        double cx = 330, cy = 0;
        g.setColor(c(0x3a2414));
        g.fill(new RoundRectangle2D.Double(cx - 8, cy + 22, 16, 8, 3, 3));
        g.setPaint(new GradientPaint(0, 0, c(0xb08a40), 0, 0, c(0x8a6a30)));
        g.fill(new Ellipse2D.Double(cx - 14, cy + 26, 28, 28));
        g.setColor(c(0xf2ead8));
        g.fill(new Ellipse2D.Double(cx - 11, cy + 29, 22, 22));
        g.setColor(c(0x1a1410));
        g.setStroke(new BasicStroke(1.4f));
        g.draw(new Line2D.Double(cx, cy + 40, cx + 6, cy + 39));       // ten to eight
        g.draw(new Line2D.Double(cx, cy + 40, cx - 4, cy + 32));
        g.setStroke(new BasicStroke(1f));
        for (int i = 0; i < 12; i++) {
            double a = i * Math.PI / 6;
            g.fill(new Ellipse2D.Double(cx + Math.sin(a) * 9 - 0.6, cy + 40 - Math.cos(a) * 9 - 0.6, 1.2, 1.2));
        }
    }

    static void archway(Graphics2D g) {
        // the way out to the street, on the right
        g.setPaint(new GradientPaint(580, 0, c(0x2a1a10), 640, 0, c(0x4a3020)));
        g.fill(poly(588, 120, 640, 104, 640, 312, 588, 286));
        g.setPaint(new GradientPaint(0, 120, c(0x3a4a6a), 0, 300, c(0x8a7a6a)));
        g.fill(poly(596, 132, 640, 120, 640, 300, 596, 280));
        g.setColor(c(0xd8b878, 90));
        g.fill(poly(602, 150, 632, 142, 632, 200, 602, 204));
        g.setColor(c(0x5a3a20));
        g.setStroke(new BasicStroke(3f));
        g.draw(poly(588, 120, 640, 104, 640, 312, 588, 286));
        g.setStroke(new BasicStroke(1f));
        g.setColor(c(0xe8e0c8));
        g.fill(new RoundRectangle2D.Double(594, 90, 40, 12, 3, 3));
        g.setColor(c(0x2a6a3a));
        g.fill(poly(600, 93, 612, 93, 612, 90.5, 618, 96, 612, 101.5, 612, 99, 600, 99));     // a green exit sign
    }

    static void machine(Graphics2D g) {
        double x = 30, y = 136, w = 70, h = 156;
        g.setPaint(new GradientPaint((float) x, 0, c(0x9a2a24), (float) (x + w), 0, c(0x5a1410)));
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 8, 8));
        g.setColor(c(0x1a1414));
        g.fill(new Rectangle2D.Double(x + 8, y + 12, w - 16, 54));
        g.setColor(c(0xf2e8c8));
        String[] rows = {"CAFE", "CAFE LAIT", "CHOCOLAT", "THE"};
        g.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, 5));
        for (int i = 0; i < rows.length; i++) {
            g.fill(new Rectangle2D.Double(x + 12, y + 18 + i * 12, 6, 6));
            g.drawString(rows[i], (float) x + 22, (float) y + 23 + i * 12);
        }
        g.setColor(c(0x6a9a6a));
        g.fill(new Rectangle2D.Double(x + 12, y + 72, 26, 8));        // the little display
        g.setColor(c(0xd8d0c0));
        g.fill(new RoundRectangle2D.Double(x + 46, y + 72, 10, 16, 2, 2));   // the coin slot
        g.setColor(c(0x1a1414));
        g.fill(new Rectangle2D.Double(x + 50, y + 75, 2, 10));
        g.fill(new Rectangle2D.Double(x + 14, y + 100, w - 28, 40));    // the cup niche
        g.setColor(c(0x8a8a90));
        g.fill(new Rectangle2D.Double(x + 16, y + 134, w - 32, 4));
        g.setColor(c(0xe8e0d0));
        g.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, 4));
        g.drawString("PIECES SEULEMENT", (float) x + 14, (float) y + 96);
        g.setColor(c(0x3a0a08));
        g.fill(new Rectangle2D.Double(x + 6, y + h - 6, 6, 8));
        g.fill(new Rectangle2D.Double(x + w - 12, y + h - 6, 6, 8));
    }

    static void rack(Graphics2D g) {
        // a little side table with a drying rack of cups; Lefort's is the one with the blue band and the chip
        double x = 106, y = 244;
        g.setColor(c(0x5a3a1e));
        g.fill(new Rectangle2D.Double(x, y, 46, 5));
        g.fill(new Rectangle2D.Double(x + 4, y + 5, 4, 44));
        g.fill(new Rectangle2D.Double(x + 38, y + 5, 4, 44));
        g.setColor(c(0xb8b8b8));
        g.setStroke(new BasicStroke(1f));
        g.draw(new Rectangle2D.Double(x + 4, y - 14, 38, 14));
        for (int i = 0; i < 3; i++) {
            double cx = x + 8 + i * 12;
            if (i == 2) continue;
            g.setColor(i == 0 ? c(0xf2efe8) : c(0xd8e0c8));
            g.fill(new RoundRectangle2D.Double(cx, y - 12, 8, 10, 3, 3));
        }
    }

    static void cup(Graphics2D g, double x, double y) {
        g.setColor(c(0xf2efe8));
        g.fill(new RoundRectangle2D.Double(x, y - 4, 8, 10, 3, 3));
        g.setColor(c(0x2a5a8a));
        g.fill(new Rectangle2D.Double(x, y, 8, 1.6));
        g.setColor(c(0xc8c4b8));
        g.fill(poly(x, y - 4, x + 2.5, y - 4, x + 1, y - 2));
    }

    static void desk(Graphics2D g) {
        double x = 432, y = 250, w = 176;
        g.setPaint(new GradientPaint(0, (float) y, c(0x6a3e1e), 0, (float) y + 84, c(0x3a2210)));
        g.fill(new Rectangle2D.Double(x + 6, y + 10, w - 12, 76));
        g.setColor(c(0x2a1608, 140));
        g.draw(new Rectangle2D.Double(x + 18, y + 22, 54, 52));
        g.draw(new Rectangle2D.Double(x + w - 72, y + 22, 54, 52));
        g.setColor(c(0xb08a40));
        g.fill(new Ellipse2D.Double(x + 42, y + 44, 4, 4));
        g.fill(new Ellipse2D.Double(x + w - 46, y + 44, 4, 4));
        g.setPaint(new GradientPaint(0, (float) y - 10, c(0x9a6a40), 0, (float) y + 10, c(0x6a4224)));
        g.fill(poly(x + 10, y - 10, x + w - 10, y - 10, x + w, y + 4, x, y + 4));
        g.setColor(c(0x4a2a12));
        g.fill(new Rectangle2D.Double(x, y + 4, w, 7));
        g.setColor(c(0x1a0e05, 90));
        g.fill(new Ellipse2D.Double(x - 6, y + 78, w + 12, 14));
        // the green leather top, papers, the stamp, a telephone with a dial, the green lamp
        g.setColor(c(0x2e5a3e));
        g.fill(poly(x + 30, y - 8, x + w - 30, y - 8, x + w - 24, y + 2, x + 24, y + 2));
        g.setColor(c(0xefe6d0));
        g.fill(poly(x + 40, y - 6, x + 66, y - 7, x + 70, y + 1, x + 42, y + 2));
        g.fill(poly(x + 46, y - 9, x + 70, y - 8, x + 72, y - 2, x + 47, y - 2));
        g.setColor(c(0x1a1a1a));
        g.fill(new RoundRectangle2D.Double(x + 82, y - 12, 18, 10, 4, 4));
        g.fill(new RoundRectangle2D.Double(x + 80, y - 17, 22, 5, 3, 3));
        // the conversion tables: a fat brown volume
        g.setPaint(new GradientPaint((float) x + 124, 0, c(0x8a5a2a), (float) x + 150, 0, c(0x5a3418)));
        g.fill(poly(x + 124, y - 14, x + 152, y - 14, x + 154, y - 4, x + 122, y - 4));
        g.setColor(c(0xe8dcc0));
        g.fill(new Rectangle2D.Double(x + 124, y - 6, 28, 2));
        g.setColor(c(0xd8b860));
        g.fill(new Rectangle2D.Double(x + 134, y - 12, 8, 1.5));
        // the green banker's lamp
        g.setColor(c(0xb08a40));
        g.fill(new Rectangle2D.Double(x + 37, y - 30, 2, 20));
        g.fill(new Ellipse2D.Double(x + 30, y - 12, 16, 4));
        g.setPaint(new GradientPaint(0, (float) y - 38, c(0x3a8a5a), 0, (float) y - 28, c(0x1a4a2a)));
        g.fill(new Arc2D.Double(x + 24, y - 42, 30, 20, 0, 180, Arc2D.CHORD));
        g.setColor(c(0xfff0b8));
        g.fill(new Ellipse2D.Double(x + 28, y - 33, 22, 3));
    }

    static void table(Graphics2D g) {
        double x = 214, y = 300, w = 212;
        g.setColor(c(0x3a2210));
        g.fill(new Rectangle2D.Double(x + 10, y + 8, 10, 64));
        g.fill(new Rectangle2D.Double(x + w - 20, y + 8, 10, 64));
        g.setColor(c(0x1a0e05, 90));
        g.fill(new Ellipse2D.Double(x - 6, y + 64, w + 12, 14));
        g.setPaint(new GradientPaint(0, (float) y - 12, c(0xa8754a), 0, (float) y + 6, c(0x6e4524)));
        g.fill(poly(x + 12, y - 12, x + w - 12, y - 12, x + w, y + 2, x, y + 2));
        g.setColor(c(0x5a3519));
        g.fill(new Rectangle2D.Double(x, y + 2, w, 7));
        // two open books and the green lamp of the reader's place
        g.setColor(c(0xe9d9b0));
        g.fill(poly(x + 40, y - 8, x + 76, y - 9, x + 80, y - 2, x + 38, y - 1));
        g.fill(poly(x + 130, y - 9, x + 162, y - 8, x + 166, y - 1, x + 128, y - 2));
        g.setColor(c(0x5a4a3a, 150));
        for (int i = 0; i < 2; i++) {
            g.draw(new Line2D.Double(x + 44, y - 6 + i * 2, x + 74, y - 6.5 + i * 2));
            g.draw(new Line2D.Double(x + 134, y - 6.5 + i * 2, x + 160, y - 6 + i * 2));
        }
        double lx = x + 104;
        g.setColor(c(0xb08a40));
        g.fill(new Rectangle2D.Double(lx - 1, y - 26, 2, 18));
        g.fill(new Ellipse2D.Double(lx - 8, y - 10, 16, 4));
        g.setPaint(new GradientPaint(0, (float) y - 34, c(0x3a8a5a), 0, (float) y - 24, c(0x1a4a2a)));
        g.fill(new Arc2D.Double(lx - 15, y - 38, 30, 20, 0, 180, Arc2D.CHORD));
        g.setColor(c(0xfff0b8));
        g.fill(new Ellipse2D.Double(lx - 11, y - 29, 22, 3));
    }
}
