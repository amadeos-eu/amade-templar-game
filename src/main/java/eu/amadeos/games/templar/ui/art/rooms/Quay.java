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
import java.util.function.Consumer;

import static eu.amadeos.games.templar.ui.art.Brush.c;
import static eu.amadeos.games.templar.ui.art.Brush.poly;

/**
 * La Rochelle, a night at the end of October 1307: a wooden quay in thick fog, the town walls a shadow behind
 * it, no towers yet at the harbour mouth, and a ship, a single-masted cog with its sail furled, waiting at the
 * end of the quay with no light.
 */
public final class Quay extends RoomArt {
    static final double DECK = 290;

    @Override
    public String id() {
        return "quay";
    }

    @Override
    public Color tint() {
        return new Color(190, 198, 214);
    }

    @Override
    protected void paint(BufferedImage img, Graphics2D g) {
        sky(g);
        town(g, new Random(1307));
        water(g, new Random(3));
        ink(img, Quay::ship);
        planks(g, new Random(5));
        ink(img, Quay::gangplank);
        fogBanks(g, new Random(9));
        Graphics2D v = (Graphics2D) g.create();
        Workshop.vignette(v);
        v.dispose();
    }

    @Override
    protected Map<String, Consumer<Graphics2D>> layers() {
        return named("post", (Consumer<Graphics2D>) Quay::post);
    }

    @Override
    protected Map<String, Prop> props() {
        return Map.of(
                "ship-light", new Prop(Quay::shipLight, 100),
                "sea-chest", new Prop(Quay::chest, 331));
    }

    @Override
    public void live(Graphics2D g, double time) {
        // fog drifting slowly across, in long soft bands
        for (int i = 0; i < 6; i++) {
            double y = 120 + i * 46, speed = 6 + i * 2.5;
            double x = ((time * speed + i * 210) % (W + 400)) - 300;
            g.setColor(c(0xc8d0dc, 22));
            g.fill(new Ellipse2D.Double(x, y, 360, 34));
            g.fill(new Ellipse2D.Double(x - 420, y + 10, 300, 26));
        }
    }

    // ---- the fog ----

    static void sky(Graphics2D g) {
        g.setPaint(new GradientPaint(0, 0, c(0x1e2434), 0, 240, c(0x5a6272)));
        g.fill(new Rectangle2D.Double(0, 0, W, 240));
        Brush.glow(g, 180, 60, 90, c(0xc8d0e0, 50));
    }

    static void town(Graphics2D g, Random r) {
        // the town walls and roofs, a church tower, all grey in the fog
        g.setColor(c(0x4a5262));
        g.fill(poly(0, 232, 0, 170, 40, 170, 40, 160, 90, 160, 90, 172, 150, 172, 150, 150, 168, 150, 168, 120,
                176, 100, 184, 120, 184, 150, 200, 150, 200, 174, 300, 178, 300, 232));
        g.setColor(c(0x545c6c));
        for (double x = 0; x < 300; x += 12) g.fill(new Rectangle2D.Double(x, 196, 8, 8));
        g.setColor(c(0xffc870, 120));
        for (int i = 0; i < 4; i++) g.fill(new Rectangle2D.Double(10 + r.nextInt(280), 180 + r.nextInt(30), 2, 3));
    }

    static void water(Graphics2D g, Random r) {
        g.setPaint(new GradientPaint(0, 230, c(0x3a4252), 0, (float) DECK, c(0x22283a)));
        g.fill(new Rectangle2D.Double(0, 228, W, H - 228));
        g.setColor(c(0xa8b0c0, 40));
        for (int i = 0; i < 40; i++) g.fill(new Rectangle2D.Double(r.nextInt(W), 232 + r.nextInt(56), 8 + r.nextInt(18), 0.8));
    }

    static void ship(Graphics2D g) {
        // a cog: a round, high hull of overlapping planks, a castle at the stern, one mast with the sail furled
        double x = 440, water = 272;
        g.setColor(c(0x1a1a24, 80));
        g.fill(new Ellipse2D.Double(x - 10, water - 4, 230, 12));
        Path2D hull = new Path2D.Double();
        hull.moveTo(x, 196);
        hull.curveTo(x + 6, 250, x + 30, water, x + 80, water);
        hull.lineTo(x + 170, water);
        hull.curveTo(x + 200, water, x + 210, 230, x + 214, 186);
        hull.lineTo(x + 214, 180);
        hull.lineTo(x, 190);
        hull.closePath();
        g.setPaint(new GradientPaint((float) x, 0, c(0x5a4a3a), (float) x + 210, 0, c(0x2a2218)));
        g.fill(hull);
        g.setColor(c(0x2a2018, 160));
        g.setStroke(new BasicStroke(0.8f));
        for (double y = 202; y < water; y += 10) g.draw(new Line2D.Double(x + (y - 196) * 0.3, y, x + 214 - (y - 186) * 0.2, y - 6));
        g.setStroke(new BasicStroke(1f));
        // the stern castle on the right
        g.setColor(c(0x4a3a2c));
        g.fill(new Rectangle2D.Double(x + 160, 150, 56, 34));
        g.setColor(c(0x3a2c20));
        for (double cx = x + 160; cx < x + 216; cx += 10) g.fill(new Rectangle2D.Double(cx, 144, 6, 7));
        // the mast, the yard with the furled sail, and the stays
        g.setColor(c(0x3a2c20));
        g.setStroke(new BasicStroke(3f));
        g.draw(new Line2D.Double(x + 104, 192, x + 104, 40));
        g.setStroke(new BasicStroke(2f));
        g.draw(new Line2D.Double(x + 54, 66, x + 154, 66));
        g.setStroke(new BasicStroke(1f));
        g.setColor(c(0xb8ae98));
        g.fill(new RoundRectangle2D.Double(x + 56, 64, 96, 7, 5, 5));
        g.setColor(c(0x2a2218));
        g.setStroke(new BasicStroke(0.6f));
        g.draw(new Line2D.Double(x + 104, 42, x, 190));
        g.draw(new Line2D.Double(x + 104, 42, x + 214, 180));
        g.setStroke(new BasicStroke(1f));
        // the lantern on its pole at the stern, dark until it answers
        g.setColor(c(0x2a2218));
        g.fill(new Rectangle2D.Double(x + 118, 104, 2, 46));
        g.fill(new RoundRectangle2D.Double(x + 113, 96, 12, 12, 2, 2));
    }

    static void shipLight(Graphics2D g) {
        Brush.glow(g, 559, 102, 40, c(0xffc860, 120));
        g.setColor(c(0xffe8a0));
        g.fill(new RoundRectangle2D.Double(555, 98, 8, 8, 2, 2));
    }

    static void planks(Graphics2D g, Random r) {
        // the wooden quay, planks running away from us, wet in the fog
        g.setPaint(new GradientPaint(0, (float) DECK, c(0x3a3028), 0, H, c(0x5a4a3c)));
        g.fill(poly(0, DECK, 604, DECK - 2, 604, H, 0, H));
        g.setColor(c(0x22180e, 150));
        for (double y = DECK + 6; y < H; y += 6 + (y - DECK) * 0.12) g.draw(new Line2D.Double(0, y, 604, y - 1));
        for (int i = 0; i < 40; i++) {
            double y = DECK + 6 + r.nextInt((int) (H - DECK - 8));
            g.draw(new Line2D.Double(r.nextInt(600), y, r.nextInt(600), y));
        }
        g.setColor(c(0x8a9aaa, 30));
        for (int i = 0; i < 12; i++) g.fill(new Ellipse2D.Double(r.nextInt(600), DECK + 10 + r.nextInt(100), 30, 3));
        g.setColor(c(0x2a2018));
        g.fill(new Rectangle2D.Double(0, DECK - 4, 604, 5));
        for (double x = 20; x < 604; x += 60) g.fill(new Rectangle2D.Double(x, DECK - 10, 8, 10));
    }

    static void gangplank(Graphics2D g) {
        g.setColor(c(0x4a3a2c));
        g.fill(poly(560, DECK + 10, 590, DECK + 14, 640, 200, 624, 196));
        g.setColor(c(0x2a2018));
        g.setStroke(new BasicStroke(0.8f));
        for (double t = 0.1; t < 1; t += 0.12) {
            double ax = 560 + (624 - 560) * t, ay = DECK + 10 + (196 - DECK - 10) * t;
            g.draw(new Line2D.Double(ax, ay, ax + 26, ay + 3));
        }
        g.setStroke(new BasicStroke(1f));
    }

    static void fogBanks(Graphics2D g, Random r) {
        for (int i = 0; i < 14; i++) {
            double x = r.nextInt(W) - 100, y = 90 + r.nextInt(200);
            g.setColor(c(0xc0c8d4, 26));
            g.fill(new Ellipse2D.Double(x, y, 260 + r.nextInt(200), 30 + r.nextInt(20)));
        }
    }

    static void chest(Graphics2D g) {
        // an iron-bound chest, small, sealed
        double x = 402, y = 330;
        g.setColor(c(0x1a0e05, 90));
        g.fill(new Ellipse2D.Double(x - 4, y - 4, 50, 8));
        g.setPaint(new GradientPaint((float) x, 0, c(0x6a4a2e), (float) x + 42, 0, c(0x3a2818)));
        g.fill(new Rectangle2D.Double(x, y - 22, 42, 22));
        g.fill(new RoundRectangle2D.Double(x - 1, y - 30, 44, 12, 8, 8));
        g.setColor(c(0x5a5e66));
        g.fill(new Rectangle2D.Double(x + 8, y - 30, 3, 30));
        g.fill(new Rectangle2D.Double(x + 31, y - 30, 3, 30));
        g.setColor(c(0xb0201a));
        g.fill(new Ellipse2D.Double(x + 18, y - 18, 6, 6));
    }

    // ---- in front ----

    static void post(Graphics2D g) {
        double x = 263, y = 360;
        g.setColor(c(0x1a0e05, 90));
        g.fill(new Ellipse2D.Double(x - 14, y - 4, 28, 8));
        g.setPaint(new GradientPaint((float) x - 10, 0, c(0x5a4a3a), (float) x + 10, 0, c(0x2a2018)));
        g.fill(new RoundRectangle2D.Double(x - 9, y - 40, 18, 40, 4, 4));
        g.setColor(c(0x8a7a5a));
        g.setStroke(new BasicStroke(2.2f));
        g.draw(new Line2D.Double(x - 9, y - 30, x + 9, y - 26));
        g.draw(new Line2D.Double(x + 8, y - 28, x + 60, y - 60));
        g.setStroke(new BasicStroke(1f));
    }
}
