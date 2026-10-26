package eu.amadeos.games.templar.ui.art;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

/** Small helpers for drawing in code: colours, outlines from points, gradients, soft glows. */
public final class Brush {
    private Brush() {
    }

    public static Color c(int rgb) {
        return new Color(rgb);
    }

    public static Color c(int rgb, int alpha) {
        return new Color((rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255, Math.max(0, Math.min(255, alpha)));
    }

    /** A colour between two others: {@code t} 0 is {@code a}, 1 is {@code b}. */
    public static Color mix(Color a, Color b, double t) {
        t = Math.max(0, Math.min(1, t));
        return new Color((int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t),
                (int) Math.round(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t));
    }

    public static Color darker(Color c, double amount) {
        return mix(c, new Color(20, 12, 8, c.getAlpha()), amount);
    }

    public static Color lighter(Color c, double amount) {
        return mix(c, new Color(255, 246, 228, c.getAlpha()), amount);
    }

    /** A closed outline through the points x0 y0 x1 y1 ... */
    public static Path2D poly(double... xy) {
        Path2D p = new Path2D.Double();
        p.moveTo(xy[0], xy[1]);
        for (int i = 2; i < xy.length; i += 2) p.lineTo(xy[i], xy[i + 1]);
        p.closePath();
        return p;
    }

    /** An open line through the points, rounded: each inner point pulls a quadratic curve towards it. */
    public static Path2D curve(double... xy) {
        Path2D p = new Path2D.Double();
        p.moveTo(xy[0], xy[1]);
        int n = xy.length / 2;
        if (n == 2) {
            p.lineTo(xy[2], xy[3]);
            return p;
        }
        for (int i = 1; i < n - 1; i++) {
            double cx = xy[2 * i], cy = xy[2 * i + 1];
            double ex = i == n - 2 ? xy[2 * i + 2] : (cx + xy[2 * i + 2]) / 2;
            double ey = i == n - 2 ? xy[2 * i + 3] : (cy + xy[2 * i + 3]) / 2;
            p.quadTo(cx, cy, ex, ey);
        }
        return p;
    }

    /** A closed, rounded outline through the points (each point is a control point of the curve). */
    public static Path2D blob(double... xy) {
        Path2D p = new Path2D.Double();
        int n = xy.length / 2;
        double sx = (xy[0] + xy[2 * (n - 1)]) / 2, sy = (xy[1] + xy[2 * (n - 1) + 1]) / 2;
        p.moveTo(sx, sy);
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            p.quadTo(xy[2 * i], xy[2 * i + 1], (xy[2 * i] + xy[2 * j]) / 2, (xy[2 * i + 1] + xy[2 * j + 1]) / 2);
        }
        p.closePath();
        return p;
    }

    public static void vertical(Graphics2D g, Shape s, double top, Color a, double bottom, Color b) {
        g.setPaint(new GradientPaint(0, (float) top, a, 0, (float) bottom, b));
        g.fill(s);
    }

    public static void horizontal(Graphics2D g, Shape s, double left, Color a, double right, Color b) {
        g.setPaint(new GradientPaint((float) left, 0, a, (float) right, 0, b));
        g.fill(s);
    }

    /** A soft round light or shadow fading to nothing at {@code radius}. */
    public static void glow(Graphics2D g, double x, double y, double radius, Color color) {
        Color none = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);
        g.setPaint(new RadialGradientPaint(new Point2D.Double(x, y), (float) radius, new float[]{0f, 1f},
                new Color[]{color, none}));
        g.fill(new Ellipse2D.Double(x - radius, y - radius, radius * 2, radius * 2));
    }

    /** An oval glow, wider than high, as light falls on a floor. */
    public static void pool(Graphics2D g, double x, double y, double rx, double ry, Color color) {
        var keep = g.getTransform();
        g.translate(x, y);
        g.scale(1, ry / rx);
        glow(g, 0, 0, rx, color);
        g.setTransform(keep);
    }

    public static Stroke line(double width) {
        return new BasicStroke((float) width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }

    public static void stroke(Graphics2D g, Shape s, double width, Color color) {
        Stroke keep = g.getStroke();
        g.setStroke(line(width));
        g.setColor(color);
        g.draw(s);
        g.setStroke(keep);
    }
}
