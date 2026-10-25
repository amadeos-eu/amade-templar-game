package eu.amadeos.games.templar.ui.art;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;

import static eu.amadeos.games.templar.ui.art.Brush.c;
import static eu.amadeos.games.templar.ui.art.Brush.poly;

/** The title screen: the Temple tower in Paris at night, and the seal of the order with two knights on one horse. */
public final class TitleArt {
    private TitleArt() {
    }

    public static void background(Graphics2D g, double time) {
        g.setPaint(new GradientPaint(0, 0, c(0x0c1028), 0, 400, c(0x2a2a48)));
        g.fill(new Rectangle2D.Double(0, 0, 640, 400));
        Random r = new Random(1307);
        for (int i = 0; i < 120; i++) {
            double x = r.nextDouble() * 640, y = r.nextDouble() * 230;
            int a = 60 + (int) (80 * (0.5 + 0.5 * Math.sin(time * (0.5 + r.nextDouble()) + i)));
            g.setColor(c(0xeef0ff, a));
            double d = 0.6 + r.nextDouble() * 0.8;
            g.fill(new Ellipse2D.Double(x, y, d, d));
        }
        Brush.glow(g, 520, 80, 60, c(0xc8d8ff, 50));
        g.setColor(c(0xeef2ff));
        g.fill(new Ellipse2D.Double(512, 72, 16, 16));
        // the Temple: the great keep with its four corner turrets, and the roofs of the enclosure
        Color tower = c(0x10121e);
        g.setColor(c(0x181a2a));
        g.fill(poly(0, 330, 120, 316, 200, 322, 300, 312, 420, 320, 520, 310, 640, 318, 640, 400, 0, 400));
        g.setColor(tower);
        double tx = 120, base = 330;
        g.fill(new Rectangle2D.Double(tx, base - 190, 70, 190));
        for (double cx : new double[]{tx - 10, tx + 62}) {
            g.fill(new Rectangle2D.Double(cx, base - 205, 18, 205));
            g.fill(poly(cx - 2, base - 205, cx + 9, base - 236, cx + 20, base - 205));
        }
        g.fill(poly(tx, base - 190, tx + 35, base - 240, tx + 70, base - 190));
        g.fill(new Rectangle2D.Double(tx + 80, base - 120, 40, 120));
        g.fill(poly(tx + 76, base - 120, tx + 100, base - 150, tx + 124, base - 120));
        g.setColor(c(0xffc860));
        for (double[] w : new double[][]{{tx + 30, base - 140}, {tx + 92, base - 90}, {tx + 34, base - 70}})
            g.fill(new Rectangle2D.Double(w[0], w[1], 4, 7));
        g.setColor(c(0x0a0c16));
        g.fill(poly(0, 360, 640, 352, 640, 400, 0, 400));
        seal(g, 450, 190, 70, c(0xd8b860, 46));
    }

    /** The seal of the Templars: two knights on one horse, inside a ring. */
    public static void seal(Graphics2D g, double x, double y, double r, Color color) {
        AffineTransform keep = g.getTransform();
        g.translate(x, y);
        g.scale(r / 50, r / 50);
        g.setColor(color);
        g.setStroke(new BasicStroke(3f));
        g.draw(new Ellipse2D.Double(-50, -50, 100, 100));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(new Ellipse2D.Double(-40, -40, 80, 80));
        // the horse
        Path2D horse = new Path2D.Double();
        horse.moveTo(-26, 8);
        horse.curveTo(-24, -4, 14, -6, 20, 0);
        horse.lineTo(26, -10);
        horse.lineTo(31, -8);
        horse.lineTo(27, 4);
        horse.lineTo(22, 8);
        horse.lineTo(20, 26);
        horse.lineTo(16, 26);
        horse.lineTo(15, 10);
        horse.lineTo(-14, 12);
        horse.lineTo(-18, 26);
        horse.lineTo(-22, 26);
        horse.lineTo(-22, 12);
        horse.lineTo(-30, 18);
        horse.closePath();
        g.fill(horse);
        // two riders with shields and lances
        for (double rx : new double[]{-12, 4}) {
            g.fill(poly(rx - 4, -2, rx + 4, -2, rx + 3, -18, rx - 3, -18));
            g.fill(new Ellipse2D.Double(rx - 3.5, -26, 7, 8));
            g.fill(poly(rx + 2, -14, rx + 9, -14, rx + 8, -4, rx + 5, -1, rx + 2, -4));
            g.setStroke(new BasicStroke(1.4f));
            g.drawLine((int) rx + 2, -10, (int) rx + 24, -32);
        }
        g.setTransform(keep);
    }

    /** The name in large gold letters with a dark outline, and a line under it, fading in with {@code alpha}. */
    public static void title(Graphics2D g, String name, String subtitle, double alpha) {
        int a = (int) Math.round(255 * Math.max(0, Math.min(1, alpha)));
        Font big = new Font(Font.SERIF, Font.BOLD | Font.ITALIC, 38);
        TextLayout layout = new TextLayout(name, big, g.getFontRenderContext());
        double w = layout.getAdvance();
        Shape outline = layout.getOutline(AffineTransform.getTranslateInstance(320 - w / 2, 200));
        g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(c(0x140a04, a));
        g.draw(outline);
        g.setPaint(new GradientPaint(0, 170, c(0xffe8a8, a), 0, 204, c(0xc89838, a)));
        g.fill(outline);
        g.setFont(new Font(Font.SERIF, Font.ITALIC, 15));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(c(0xe8dcc0, a));
        g.drawString(subtitle, (float) (320 - fm.stringWidth(subtitle) / 2.0), 232f);
    }
}
