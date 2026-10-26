package eu.amadeos.games.templar.ui.art;

import java.awt.BasicStroke;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import static eu.amadeos.games.templar.ui.art.Brush.c;
import static eu.amadeos.games.templar.ui.art.Brush.poly;

/** The pictures of the things in the inventory, each about 36 × 26 pixels around (0, 0). */
public final class Items {
    private Items() {
    }

    public static void draw(Graphics2D g, String item) {
        AffineTransform keep = g.getTransform();
        switch (item) {
            case "phone" -> {
                g.setColor(c(0x202428));
                g.fill(new RoundRectangle2D.Double(-6, -11, 12, 22, 3, 3));
                g.setPaint(new GradientPaint(-5, -9, c(0x6a9ac0), 5, 8, c(0x2a4a6a)));
                g.fill(new Rectangle2D.Double(-5, -9, 10, 16));
            }
            case "brush", "damp-brush" -> {
                g.rotate(-0.5);
                g.setColor(c(0x8a5a2a));
                g.fill(new RoundRectangle2D.Double(-15, -1.6, 20, 3.2, 2, 2));
                g.setColor(c(0xb0b4bc));
                g.fill(new Rectangle2D.Double(4, -2.4, 3, 4.8));
                g.setColor(item.equals("damp-brush") ? c(0x9a8a6a) : c(0xe0d0a0));
                g.fill(poly(7, -2.6, 15, -2.2, 16, 0, 15, 2.2, 7, 2.6));
                if (item.equals("damp-brush")) {
                    g.setColor(c(0x8ab8e0, 200));
                    g.fill(new Ellipse2D.Double(15, 1, 2.4, 3.2));
                }
            }
            case "bonefolder" -> {
                g.rotate(-0.4);
                g.setPaint(new GradientPaint(0, -3, c(0xfaf2e0), 0, 3, c(0xd8ccb0)));
                g.fill(new RoundRectangle2D.Double(-16, -2.6, 32, 5.2, 5, 5));
            }
            case "page", "ledger" -> {
                g.setPaint(new GradientPaint(-12, -9, c(0xf0e2bc), 12, 9, c(0xc8b088)));
                g.fill(poly(-12, -9, 11, -10, 13, 8, -11, 10));
                g.setColor(c(0x5a4a3a, 170));
                g.setStroke(new BasicStroke(0.8f));
                for (int i = 0; i < 4; i++) g.draw(new Line2D.Double(-9, -6 + i * 3.4, 8, -6.6 + i * 3.4));
                if (item.equals("page") || item.equals("ledger")) {
                    g.setColor(c(0x8a1e14));
                    g.fill(new Ellipse2D.Double(3, 3, 6, 5));
                }
                g.setColor(c(0x8a7a5a));
                g.draw(new Line2D.Double(-12, -9, -11, 10));
            }
            case "coin" -> {
                g.setPaint(new GradientPaint(-6, -6, c(0xf0d890), 6, 6, c(0xa08030)));
                g.fill(new Ellipse2D.Double(-7, -7, 14, 14));
                g.setColor(c(0x7a6020));
                g.draw(new Ellipse2D.Double(-5, -5, 10, 10));
            }
            case "cup", "coffee" -> {
                g.setColor(c(0xf2efe8));
                g.fill(new RoundRectangle2D.Double(-7, -7, 12, 14, 4, 4));
                g.setStroke(new BasicStroke(2f));
                g.draw(new Arc2D.Double(2, -4, 8, 8, -90, 180, Arc2D.OPEN));
                g.setColor(c(0x2a5a8a));
                g.fill(new Rectangle2D.Double(-7, -1, 12, 2));
                g.setColor(c(0xc8c4b8));
                g.fill(poly(-7, -7, -4, -7, -6, -4));                 // the chip
                if (item.equals("coffee")) {
                    g.setColor(c(0x3a2010));
                    g.fill(new Ellipse2D.Double(-6, -8, 10, 3));
                    g.setColor(c(0xffffff, 120));
                    g.setStroke(new BasicStroke(1f));
                    g.draw(new Line2D.Double(-3, -10, -2, -14));
                    g.draw(new Line2D.Double(1, -10, 1, -15));
                }
            }
            case "sandwich" -> {
                g.setColor(c(0xd8a860));
                g.fill(new RoundRectangle2D.Double(-13, -5, 24, 10, 8, 8));
                g.setColor(c(0x7aa040));
                g.fill(new Rectangle2D.Double(-12, -1, 22, 2));
                g.setColor(c(0xd85040));
                g.fill(new Rectangle2D.Double(-11, 1, 18, 1.6));
                g.setColor(c(0x3a2414));
                g.fill(poly(8, -5, 12, -2, 12, 4, 9, 2, 11, -1));          // a beak-sized bite
            }
            case "key" -> {
                g.setColor(c(0x8a7a5a));
                g.setStroke(new BasicStroke(2.4f));
                g.draw(new Ellipse2D.Double(-13, -5, 9, 9));
                g.draw(new Line2D.Double(-4, 0, 12, 0));
                g.draw(new Line2D.Double(9, 0, 9, 5));
                g.draw(new Line2D.Double(12, 0, 12, 4));
            }
            case "seal" -> {
                g.setPaint(new GradientPaint(-8, -10, c(0xd8b860), 8, 10, c(0x8a6a20)));
                g.fill(new Ellipse2D.Double(-9, -9, 18, 18));
                g.setColor(c(0x5a4410));
                g.setStroke(new BasicStroke(0.9f));
                g.draw(new Ellipse2D.Double(-7, -7, 14, 14));
                // two riders on one horse
                g.fill(poly(-5, 3, 5, 3, 5, 1, 2, -1, -3, -1, -5, 1));
                g.fill(new Rectangle2D.Double(-2, -5, 1.6, 4));
                g.fill(new Rectangle2D.Double(1, -5, 1.6, 4));
            }
            case "wax", "soft-wax" -> {
                g.rotate(-0.3);
                g.setColor(item.equals("wax") ? c(0x8a1e14) : c(0xb02a1a));
                g.fill(new RoundRectangle2D.Double(-12, -3, 24, 6, 3, 3));
                if (item.equals("soft-wax")) {
                    g.setColor(c(0xd04030));
                    g.fill(new Ellipse2D.Double(9, -1, 5, 7));
                }
            }
            case "cheese" -> {
                g.setPaint(new GradientPaint(-10, -6, c(0xf0d070), 10, 6, c(0xc8a040)));
                g.fill(poly(-12, 6, 12, 6, 12, -2, -6, -8));
                g.setColor(c(0xb08a30));
                g.fill(new Ellipse2D.Double(-2, 0, 3, 3));
                g.fill(new Ellipse2D.Double(5, 1, 2.4, 2.4));
            }
            case "cat" -> {
                g.translate(0, 9);
                g.scale(0.85, 0.85);
                Animals.draw(g, "cat", false, 0, false);
            }
            case "knife" -> {
                g.rotate(-0.5);
                g.setColor(c(0x5a3a1a));
                g.fill(new RoundRectangle2D.Double(-14, -2, 12, 4, 2, 2));
                g.setColor(c(0xc8ccd4));
                g.fill(poly(-2, -1.6, 13, -1, 15, 0, -2, 1.6));
            }
            case "glue" -> {
                g.setColor(c(0x8a8a8a));
                g.fill(new RoundRectangle2D.Double(-6, -6, 12, 13, 3, 3));
                g.setColor(c(0xe8e0c0));
                g.fill(new Ellipse2D.Double(-5, -8, 10, 4));
            }
            case "half" -> {
                // the Tomar half: parchment with a wavy cut along its left edge
                g.setPaint(new GradientPaint(-12, -9, c(0xeadcb4), 12, 9, c(0xc0a880)));
                g.fill(poly(-10, -9, -12, -5, -9, -1, -12, 3, -9, 7, -11, 10, 12, 9, 11, -10));
                g.setColor(c(0x5a4a3a, 170));
                g.setStroke(new BasicStroke(0.8f));
                for (int i = 0; i < 4; i++) g.draw(new Line2D.Double(-6, -6 + i * 3.4, 8, -6.6 + i * 3.4));
            }
            case "chirograph" -> {
                // both halves, side by side, the wavy cut between them
                g.setPaint(new GradientPaint(-16, -9, c(0xf0e2bc), 16, 9, c(0xc8b088)));
                g.fill(poly(-16, -8, 16, -10, 17, 8, -15, 10));
                g.setColor(c(0x6a5a3a));
                g.setStroke(new BasicStroke(0.7f));
                g.draw(Brush.curve(1, -9, -1, -4, 2, 0, -1, 4, 1, 9));
                g.setColor(c(0x5a4a3a, 170));
                for (int i = 0; i < 4; i++) {
                    g.draw(new Line2D.Double(-13, -6 + i * 3.4, -3, -6.4 + i * 3.4));
                    g.draw(new Line2D.Double(4, -6.4 + i * 3.4, 14, -6.8 + i * 3.4));
                }
                g.setColor(c(0x8a1e14));
                g.fill(new Ellipse2D.Double(-12, 4, 5, 4));
            }
            case "napkin", "note" -> {
                g.setColor(c(0xf4f0e6));
                g.fill(poly(-11, -8, 11, -9, 12, 8, -10, 9));
                g.setColor(c(0x9a2a22));
                g.setStroke(new BasicStroke(0.6f));
                g.draw(new Rectangle2D.Double(-9, -7, 19, 14));
                if (item.equals("note")) {
                    g.setColor(c(0x3a3a3a));
                    g.setStroke(new BasicStroke(1f));
                    for (int i = 0; i < 3; i++) g.draw(new Line2D.Double(-6, -3 + i * 3.4, 6 - i * 2, -3 + i * 3.4));
                }
            }
            case "token" -> {
                g.setPaint(new GradientPaint(-7, -7, c(0xb0b4b8), 7, 7, c(0x5a5e66)));
                g.fill(new Ellipse2D.Double(-8, -8, 16, 16));
                g.setColor(c(0x3a3e44));
                g.setStroke(new BasicStroke(0.8f));
                g.draw(new Ellipse2D.Double(-6.5, -6.5, 13, 13));
                g.fill(poly(-3.5, 3, -3.5, -1, 0, -4, 3.5, -1, 3.5, 3));
            }
            case "lantern" -> {
                g.setColor(c(0x2a2a2e));
                g.fill(new Rectangle2D.Double(-5, -9, 10, 2));
                g.fill(new Rectangle2D.Double(-5, 7, 10, 2));
                g.setColor(c(0xffd070));
                g.fill(new Rectangle2D.Double(-4, -7, 8, 14));
                g.setColor(c(0x2a2a2e));
                g.setStroke(new BasicStroke(1f));
                g.draw(new Rectangle2D.Double(-4, -7, 8, 14));
                g.draw(new Arc2D.Double(-3, -13, 6, 8, 0, 180, Arc2D.OPEN));
            }
            case "pencil" -> {
                g.rotate(-0.45);
                g.setColor(c(0x2a5a3a));
                g.fill(new Rectangle2D.Double(-12, -1.8, 20, 3.6));
                g.setColor(c(0xe0c090));
                g.fill(poly(8, -1.8, 13, 0, 8, 1.8));
                g.setColor(c(0x2a2a2a));
                g.fill(poly(11.5, -0.6, 13, 0, 11.5, 0.6));
            }
            case "pastry" -> {
                g.setPaint(new GradientPaint(0, -4, c(0xe8b050), 0, 6, c(0xa86a24)));
                g.fill(new Ellipse2D.Double(-10, -5, 20, 11));
                g.setPaint(new GradientPaint(0, -3, c(0xfce48a), 0, 3, c(0xe8b040)));
                g.fill(new Ellipse2D.Double(-7, -3.5, 14, 6.5));
                g.setColor(c(0x5a2a0a));
                g.fill(new Ellipse2D.Double(-3, -2, 3, 2));
                g.fill(new Ellipse2D.Double(1.5, -0.5, 2.4, 1.6));
            }
            default -> {
                g.setColor(c(0xd8c8a0));
                g.fill(new Ellipse2D.Double(-6, -6, 12, 12));
            }
        }
        g.setTransform(keep);
    }
}
