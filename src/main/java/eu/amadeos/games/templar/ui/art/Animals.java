package eu.amadeos.games.templar.ui.art;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

import static eu.amadeos.games.templar.ui.art.Brush.c;
import static eu.amadeos.games.templar.ui.art.Brush.darker;
import static eu.amadeos.games.templar.ui.art.Brush.lighter;
import static eu.amadeos.games.templar.ui.art.Brush.poly;

/** The stable cat of the Temple and the Paris pigeon, seen from the side, looking right, feet at (0, 0). */
public final class Animals {
    private Animals() {
    }

    public static boolean isAnimal(String id) {
        return id.equals("cat") || id.equals("pigeon");
    }

    /** {@code walking} with {@code phase} 0 to 1 through a step cycle; otherwise sitting. */
    public static void draw(Graphics2D g, String id, boolean walking, double phase, boolean busy) {
        if (id.equals("cat")) cat(g, walking, phase);
        else pigeon(g, walking, phase, busy);
    }

    private static void cat(Graphics2D g, boolean walking, double phase) {
        Color fur = c(0x8a7a68), dark = c(0x4a3e34);
        if (!walking) {
            // sitting, tail curled round the paws
            g.setPaint(new GradientPaint(-10, -20, lighter(fur, 0.15), 8, 0, darker(fur, 0.3)));
            g.fill(Brush.blob(-11, 0, -12, -10, -6, -17, 2, -16, 5, -8, 4, 0));
            g.setColor(darker(fur, 0.1));
            g.fill(Brush.curve(-11, -1, 2, 2, 10, -1, 9, -3, 2, -1, -9, -3));
            head(g, 2, -18, fur, dark);
            g.setColor(dark);
            g.setStroke(Brush.line(0.7));
            for (int i = 0; i < 3; i++) g.draw(new Line2D.Double(-8 + i * 3, -14 + i, -6 + i * 3, -6 + i));
            g.setColor(lighter(fur, 0.35));
            g.fill(new Ellipse2D.Double(1, -3, 3.5, 3));
            return;
        }
        double s = Math.sin(phase * Math.PI * 2);
        g.setStroke(Brush.line(2.2));
        g.setColor(darker(fur, 0.25));
        g.draw(new Line2D.Double(-8, -8, -8 - 3 * s, 0));
        g.draw(new Line2D.Double(6, -8, 6 + 3 * s, 0));
        g.setPaint(new GradientPaint(0, -14, lighter(fur, 0.15), 0, -5, darker(fur, 0.25)));
        g.fill(Brush.blob(-12, -12, -4, -15, 6, -14, 9, -10, 5, -6, -10, -6));
        g.setColor(darker(fur, 0.1));
        g.draw(new Line2D.Double(-9, -8, -9 + 3 * s, 0));
        g.draw(new Line2D.Double(4, -8, 4 - 3 * s, 0));
        g.setStroke(Brush.line(1.8));
        g.setColor(fur);
        g.draw(Brush.curve(-12, -11, -18, -14, -19, -20 + 2 * s));
        head(g, 10, -14, fur, dark);
    }

    private static void head(Graphics2D g, double x, double y, Color fur, Color dark) {
        g.setPaint(new GradientPaint((float) x - 4, (float) y - 4, lighter(fur, 0.2), (float) x + 4, (float) y + 4, darker(fur, 0.2)));
        g.fill(new Ellipse2D.Double(x - 4.5, y - 4, 9, 7.5));
        g.fill(poly(x - 4, y - 2, x - 3.4, y - 7, x - 1, y - 3.4));
        g.fill(poly(x + 1, y - 3.4, x + 3, y - 7.2, x + 3.8, y - 2));
        g.setColor(c(0xd8c050));
        g.fill(new Ellipse2D.Double(x + 1.2, y - 1.6, 1.8, 1.4));
        g.setColor(dark);
        g.fill(new Ellipse2D.Double(x + 1.9, y - 1.6, 0.6, 1.4));
        g.setColor(c(0xc08080));
        g.fill(new Ellipse2D.Double(x + 3.6, y + 0.6, 1.2, 0.9));
    }

    private static void pigeon(Graphics2D g, boolean walking, double phase, boolean pecking) {
        double bob = walking ? Math.sin(phase * Math.PI * 4) * 0.8 : 0;
        double peck = pecking ? 3 : 0;
        Color grey = c(0x7d8194);
        g.setColor(c(0xc0605a));
        double s = walking ? Math.sin(phase * Math.PI * 2) : 0;
        g.setStroke(Brush.line(0.7));
        g.draw(new Line2D.Double(-1, -3, -1 - s, 0));
        g.draw(new Line2D.Double(1.5, -3, 1.5 + s, 0));
        g.setPaint(new GradientPaint(-6, -10, lighter(grey, 0.15), 4, -2, darker(grey, 0.25)));
        Path2D body = Brush.blob(-9, -6 + bob, -6, -9 + bob, 2, -9 + bob, 6, -6 + bob, 3, -2.5, -4, -3);
        g.fill(body);
        g.setColor(darker(grey, 0.2));
        g.fill(poly(-7, -7 + bob, -13, -4 + bob, -6, -4 + bob));
        g.setColor(c(0x5f6377));
        g.fill(Brush.blob(-6, -7 + bob, 0, -8.5 + bob, 2, -6 + bob, -3, -5 + bob));
        double hx = 4 + peck * 0.6, hy = -11 + bob + peck;
        g.setColor(c(0x6c7f7a));
        g.fill(new Ellipse2D.Double(hx - 2.6, hy - 2.6, 5.4, 5.6));
        g.fill(poly(hx - 2, hy + 1, hx + 2, hy + 1, hx + 1, hy + 5, hx - 3, hy + 4));
        g.setColor(c(0xe8a040));
        g.fill(poly(hx + 2.4, hy - 0.6, hx + 5, hy, hx + 2.4, hy + 0.6));
        g.setColor(c(0xf0e0b0));
        g.fill(new Ellipse2D.Double(hx + 0.4, hy - 1.2, 1.4, 1.4));
        g.setColor(c(0x101010));
        g.fill(new Ellipse2D.Double(hx + 0.8, hy - 0.8, 0.7, 0.7));
    }
}
