package eu.amadeos.games.templar.ui.art.rooms;

import eu.amadeos.games.templar.ui.art.Brush;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import static eu.amadeos.games.templar.ui.art.Brush.c;

/** Light drawn over everything, people included: lamps, candles, torches, and the dark of a night room. */
public final class Lights {
    private Lights() {
    }

    /** A warm glow around a lamp, {@code radius} wide, {@code alpha} strong in the middle. */
    public static void lamp(Graphics2D g, double x, double y, double radius, Color color, int alpha) {
        Brush.glow(g, x, y, radius, c(color.getRGB() & 0xffffff, alpha));
    }

    /** A candle flame that flickers with the time; its glow is part of the painted room. */
    public static void flame(Graphics2D g, double x, double y, double time, double size) {
        double f = 0.85 + 0.15 * Math.sin(time * 11 + x) * Math.sin(time * 7.3 + y);
        double sway = Math.sin(time * 5 + x) * 0.6 * size;
        Brush.glow(g, x, y - 3 * size, 26 * size * f, c(0xffc060, 50));
        g.setColor(c(0xffa830));
        g.fill(Brush.blob(x + sway, y - 7 * size * f, x + 2 * size, y - 2 * size, x, y, x - 2 * size, y - 2 * size));
        g.setColor(c(0xfff2b0));
        g.fill(new Ellipse2D.Double(x - 0.8 * size, y - 3.4 * size, 1.6 * size, 2.6 * size));
    }

    /** Night: everything darkens to blue, except round pools of light at (x, y, radius) triples. */
    public static void night(Graphics2D g, int width, int height, Color dark, int alpha, double... pools) {
        java.awt.geom.Area shade = new java.awt.geom.Area(new java.awt.Rectangle(0, 0, width, height));
        g.setColor(c(dark.getRGB() & 0xffffff, alpha));
        g.fill(shade);
        for (int i = 0; i < pools.length; i += 3) {
            g.setPaint(new RadialGradientPaint(new Point2D.Double(pools[i], pools[i + 1]), (float) pools[i + 2],
                    new float[]{0f, 1f}, new Color[]{c(0xffd890, 60), c(0xffd890, 0)}));
            g.fill(new Ellipse2D.Double(pools[i] - pools[i + 2], pools[i + 1] - pools[i + 2], pools[i + 2] * 2, pools[i + 2] * 2));
        }
    }
}
