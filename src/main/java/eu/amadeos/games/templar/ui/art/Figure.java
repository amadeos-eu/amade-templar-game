package eu.amadeos.games.templar.ui.art;

import eu.amadeos.games.templar.ui.art.Look.Beard;
import eu.amadeos.games.templar.ui.art.Look.Extra;
import eu.amadeos.games.templar.ui.art.Look.Garment;
import eu.amadeos.games.templar.ui.art.Look.Hair;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import static eu.amadeos.games.templar.ui.art.Brush.c;
import static eu.amadeos.games.templar.ui.art.Brush.darker;
import static eu.amadeos.games.templar.ui.art.Brush.lighter;
import static eu.amadeos.games.templar.ui.art.Brush.poly;

/**
 * Draws a person of any {@link Look} in any {@link Pose}, from the side (looking right), from the front or from
 * behind, with the soles at (0, 0). Bodies are built on a skeleton of a person 118 pixels tall, about seven
 * heads, and scaled to the look's height: hips at 60, knees at 32, shoulders at 96. Limbs are tapered shapes
 * lit from the upper left; {@link Ink} adds the outline afterwards.
 */
public final class Figure {
    public enum View { SIDE, FRONT, BACK }

    static final double H = 118;
    private static final double THIGH = 28, SHIN = 28, UPPER_ARM = 20, FOREARM = 17;

    private Figure() {
    }

    // --8<-- [start:draw]
    public static void draw(Graphics2D g, Look look, View view, Pose pose) {
        AffineTransform keep = g.getTransform();
        g.scale(look.height() / H, look.height() / H);
        if (view == View.SIDE) side(g, look, pose);
        else straight(g, look, pose, view == View.FRONT);
        g.setTransform(keep);
    }
    // --8<-- [end:draw]

    // ---- from the side ---------------------------------------------------------------------------------

    private record Leg(Point2D hip, Point2D knee, Point2D ankle, double footAngle, double sole) {
    }

    private static Leg leg(double hx, double hy, double thigh, double bend) {
        Point2D hip = new Point2D.Double(hx, hy);
        Point2D knee = new Point2D.Double(hx + THIGH * Math.sin(thigh), hy + THIGH * Math.cos(thigh));
        double shin = thigh - bend;
        Point2D ankle = new Point2D.Double(knee.getX() + SHIN * Math.sin(shin), knee.getY() + SHIN * Math.cos(shin));
        double foot = shin < 0 ? Math.min(0.9, -shin * 0.75) : -Math.min(0.35, shin * 0.45);
        double sole = Double.NEGATIVE_INFINITY;
        for (double[] corner : new double[][]{{-3.5, 4}, {9.5, 4}}) {
            double y = ankle.getY() + corner[0] * Math.sin(foot) + corner[1] * Math.cos(foot);
            sole = Math.max(sole, y);
        }
        return new Leg(hip, knee, ankle, foot, sole);
    }

    private static void side(Graphics2D g, Look look, Pose p) {
        double hipY = -60 + p.crouch();
        Leg near = leg(1, hipY, p.nearHip(), p.nearKnee());
        Leg far = leg(-1, hipY, p.farHip(), p.farKnee());
        double ground = Math.max(near.sole(), far.sole());
        g.translate(0, -ground);

        double lean = p.lean();
        Torso t = new Torso(0, hipY, lean);
        Color legs = look.legs(), legsFar = darker(legs, 0.25);
        Color sleeve = sleeve(look);
        Color sleeveFar = darker(sleeve, 0.25);
        Point2D shoulder = t.at(0, 32.5);
        Point2D farShoulder = t.at(-1.5, 32);

        if (look.garment() == Garment.MANTLE) cloakBehind(g, look, t, near, far);
        // the far arm and leg, in shadow
        arm(g, farShoulder, p.farShoulder(), p.farElbow(), sleeveFar, darker(look.skin(), 0.3), 6.2);
        legSide(g, far, legsFar, darker(look.shoes(), 0.2), look);
        // pelvis between the legs
        g.setColor(legs);
        g.fill(new Ellipse2D.Double(-8.5, hipY - 5, 17, 11));
        if (look.female()) {                         // the round of the seat, behind the hip
            Point2D seat = t.at(-4.2, -2.5);
            double w = 17 * look.build();
            g.setPaint(new GradientPaint((float) (seat.getX() - w / 2), 0, lighter(legs, 0.12),
                    (float) (seat.getX() + w / 2), 0, darker(legs, 0.1)));
            g.fill(new Ellipse2D.Double(seat.getX() - w / 2, seat.getY() - 7.5, w, 15));
        }
        legSide(g, near, legs, look.shoes(), look);
        skirtSide(g, look, t, near, far);
        torsoSide(g, look, t);
        headSide(g, look, t, p);
        if (look.has(Extra.HANDBAG)) handbag(g, shoulder, p, look);
        Point2D hand = arm(g, shoulder, p.nearShoulder(), p.nearElbow(), sleeve, look.skin(), 6.8);
        held(g, look, hand, p.nearShoulder() + p.nearElbow(), true);
    }

    /** The upper body's own coordinates: {@code v} forward, {@code u} up from the hip joint, leaning. */
    private record Torso(double x, double y, double lean) {
        Point2D at(double v, double u) {
            return new Point2D.Double(x + v * Math.cos(lean) + u * Math.sin(lean), y + v * Math.sin(lean) - u * Math.cos(lean));
        }

        Path2D shape(double... vu) {
            Path2D p = new Path2D.Double();
            for (int i = 0; i < vu.length; i += 2) {
                Point2D q = at(vu[i], vu[i + 1]);
                if (i == 0) p.moveTo(q.getX(), q.getY());
                else p.lineTo(q.getX(), q.getY());
            }
            p.closePath();
            return p;
        }

        /** A closed, rounded outline through the points (Catmull-Rom), for soft body curves. */
        Path2D smooth(double... vu) {
            int n = vu.length / 2;
            Point2D[] q = new Point2D[n];
            for (int i = 0; i < n; i++) q[i] = at(vu[2 * i], vu[2 * i + 1]);
            return through(q);
        }
    }

    /** A closed, rounded outline through the points x, y, x, y, ... (Catmull-Rom), for hair and soft shapes. */
    static Path2D curve(double... xy) {
        Point2D[] q = new Point2D[xy.length / 2];
        for (int i = 0; i < q.length; i++) q[i] = new Point2D.Double(xy[2 * i], xy[2 * i + 1]);
        return through(q);
    }

    private static Path2D through(Point2D[] q) {
        int n = q.length;
        Path2D p = new Path2D.Double();
        p.moveTo(q[0].getX(), q[0].getY());
        for (int i = 0; i < n; i++) {
            Point2D a = q[(i - 1 + n) % n], b = q[i], c = q[(i + 1) % n], d = q[(i + 2) % n];
            p.curveTo(b.getX() + (c.getX() - a.getX()) / 6, b.getY() + (c.getY() - a.getY()) / 6,
                    c.getX() - (d.getX() - b.getX()) / 6, c.getY() - (d.getY() - b.getY()) / 6, c.getX(), c.getY());
        }
        p.closePath();
        return p;
    }

    private static Color sleeve(Look look) {
        return switch (look.garment()) {
            case COAT, JACKET -> look.garmentColor();
            default -> look.sleeve();
        };
    }

    /** One arm from the shoulder; returns where the hand is. */
    private static Point2D arm(Graphics2D g, Point2D shoulder, double angle, double bend, Color sleeve, Color skin,
                               double width) {
        Point2D elbow = new Point2D.Double(shoulder.getX() + UPPER_ARM * Math.sin(angle),
                shoulder.getY() + UPPER_ARM * Math.cos(angle));
        double fore = angle + bend;
        Point2D wrist = new Point2D.Double(elbow.getX() + FOREARM * Math.sin(fore), elbow.getY() + FOREARM * Math.cos(fore));
        limb(g, shoulder, elbow, width, width * 0.86, sleeve);
        limb(g, elbow, wrist, width * 0.86, width * 0.7, sleeve);
        Point2D hand = new Point2D.Double(wrist.getX() + 3.2 * Math.sin(fore), wrist.getY() + 3.2 * Math.cos(fore));
        g.setPaint(new GradientPaint((float) hand.getX() - 3, 0, lighter(skin, 0.15), (float) hand.getX() + 3, 0, darker(skin, 0.15)));
        g.fill(new Ellipse2D.Double(hand.getX() - 2.8, hand.getY() - 3.4, 5.6, 6.8));
        return hand;
    }

    private static void legSide(Graphics2D g, Leg leg, Color color, Color shoe, Look look) {
        limb(g, leg.hip(), leg.knee(), (look.female() ? 13.4 : 12) * look.build(), 8.2, color);
        limb(g, leg.knee(), leg.ankle(), 8.2, 5.4, color);
        AffineTransform keep = g.getTransform();
        g.translate(leg.ankle().getX(), leg.ankle().getY());
        g.rotate(leg.footAngle());
        g.setPaint(new GradientPaint(0, -1, lighter(shoe, 0.2), 0, 4, shoe));
        if (look.female() && look.has(Extra.EARRINGS)) {           // heels
            g.fill(poly(-3, -1, 3, -1.5, 9.5, 3.4, 9.5, 4, 4, 4, 0, 1.5, -2.6, 1.6, -2.6, 4, -3.6, 4, -3.6, 0));
        } else {
            g.fill(new RoundRectangle2D.Double(-3.6, -1.2, 13.2, 5.2, 3.5, 3.5));
            g.setColor(darker(shoe, 0.4));
            g.fill(new Rectangle2D.Double(-3.6, 3.1, 13.2, 0.9));
        }
        g.setTransform(keep);
    }

    /** A tapered limb from a to b, with round ends, lit from the left. */
    static void limb(Graphics2D g, Point2D a, Point2D b, double wa, double wb, Color color) {
        double dx = b.getX() - a.getX(), dy = b.getY() - a.getY(), len = Math.max(0.001, Math.hypot(dx, dy));
        double nx = -dy / len, ny = dx / len;
        Area shape = new Area(poly(a.getX() + nx * wa / 2, a.getY() + ny * wa / 2, b.getX() + nx * wb / 2, b.getY() + ny * wb / 2,
                b.getX() - nx * wb / 2, b.getY() - ny * wb / 2, a.getX() - nx * wa / 2, a.getY() - ny * wa / 2));
        shape.add(new Area(new Ellipse2D.Double(a.getX() - wa / 2, a.getY() - wa / 2, wa, wa)));
        shape.add(new Area(new Ellipse2D.Double(b.getX() - wb / 2, b.getY() - wb / 2, wb, wb)));
        double side = nx <= 0 ? 1 : -1;             // the edge towards the left gets the light
        g.setPaint(new GradientPaint((float) (a.getX() + side * nx * wa / 2), (float) (a.getY() + side * ny * wa / 2),
                lighter(color, 0.16), (float) (a.getX() - side * nx * wa / 2), (float) (a.getY() - side * ny * wa / 2),
                darker(color, 0.22)));
        g.fill(shape);
    }

    /** The x of a leg at height y, between hip, knee and ankle. */
    private static double legX(Leg leg, double y) {
        Point2D a = leg.hip(), b = leg.knee();
        if (y > b.getY()) {
            a = leg.knee();
            b = leg.ankle();
        }
        double t = (y - a.getY()) / Math.max(0.001, b.getY() - a.getY());
        t = Math.max(0, Math.min(1, t));
        return a.getX() + (b.getX() - a.getX()) * t;
    }

    /** Skirts of coats, tunics and habits, over the legs, swinging with them. */
    private static void skirtSide(Graphics2D g, Look look, Torso t, Leg near, Leg far) {
        Garment gm = look.garment();
        if (gm == Garment.NONE || gm == Garment.JACKET && look.hem() > 54) return;
        double hemY = -look.hem();
        double lo = Math.min(legX(near, hemY), legX(far, hemY)), hi = Math.max(legX(near, hemY), legX(far, hemY));
        Color color = switch (gm) {
            case ROBE, MANTLE -> look.top();
            default -> look.garmentColor();
        };
        double flare = gm == Garment.ROBE || gm == Garment.MANTLE ? 9 : 8;
        Point2D wb = t.at(-7.5, 10), wf = t.at(7, 10), hb = t.at(look.female() ? -11 : -8.5, -2), hf = t.at(8, -2);
        if (gm == Garment.APRON) {
            Path2D apron = poly(wf.getX() - 3, wf.getY(), wf.getX() + 1.2, wf.getY(), hi + 7.5, hemY, hi + 1, hemY + 0.5);
            Brush.horizontal(g, apron, wf.getX() - 3, lighter(color, 0.1), hi + 8, darker(color, 0.25));
            return;
        }
        Path2D s = new Path2D.Double();
        s.moveTo(wb.getX(), wb.getY());
        s.lineTo(hb.getX() - 0.5, hb.getY());
        s.quadTo(lo - flare - 2, (hb.getY() + hemY) / 2, lo - flare, hemY);
        s.quadTo((lo + hi) / 2, hemY + 2.2, hi + flare, hemY);
        s.quadTo(hi + flare - 1, (hf.getY() + hemY) / 2, hf.getX(), hf.getY());
        s.lineTo(wf.getX(), wf.getY());
        s.closePath();
        Brush.horizontal(g, s, lo - flare, lighter(color, 0.12), hi + flare, darker(color, 0.25));
        g.setColor(darker(color, 0.35));
        g.setStroke(Brush.line(0.55));
        double mid = (lo + hi) / 2;
        g.draw(new QuadCurve2D.Double((hb.getX() + hf.getX()) / 2, hb.getY() + 2, mid + 2, (hb.getY() + hemY) / 2, mid, hemY));
        if (gm == Garment.COAT) {
            g.draw(new Line2D.Double(hf.getX() - 1, hf.getY(), hi + flare - 1.5, hemY - 0.5));
        }
        if (gm == Garment.TUNIC || gm == Garment.SURCOAT) {
            g.setColor(darker(color, 0.15));
            g.draw(new Line2D.Double(lo - flare + 1, hemY - 1.5, hi + flare - 1, hemY - 1.5));
        }
    }

    private static void torsoSide(Graphics2D g, Look look, Torso t) {
        boolean f = look.female();
        double b = look.build() * 1.1;
        // a woman: a rounded seat behind, a narrow waist, and the bust standing out in front
        Path2D body = f
                ? t.smooth(-9.4 * b, -3, -9.2 * b, 3, -6 * b, 12, -6.6 * b, 24, -5.8 * b, 34, 0, 37, 4.6, 34.5, 6.8 * b, 29.6,
                7.7 * b, 27.9, 8.3 * b, 24.8, 7.9 * b, 21.6, 6.5 * b, 19, 5.4 * b, 17.4, 4.9 * b, 12, 6.8 * b, 4, 7.4 * b, -3)
                : t.shape(-7.5 * b, -1, -7.5 * b, 12, -8 * b, 26, -6.5 * b, 35, 4, 36, 6.5 * b, 30, 7 * b, 22, 6.5 * b, 12, 7 * b, -1);
        Color top = look.top();
        Garment gm = look.garment();
        Point2D back = t.at(-8, 20), front = t.at(8, 20);
        Brush.horizontal(g, body, back.getX(), lighter(top, 0.14), front.getX(), darker(top, 0.24));
        if (look.has(Extra.ROLL_NECK) || look.has(Extra.HOOD)) {
            g.setColor(darker(top, 0.15));
            g.setStroke(Brush.line(0.45));
            for (double u = 2; u < 33; u += 2.4) g.draw(new Line2D.Double(t.at(-1, u), t.at(4, u + 0.3)));
        }
        switch (gm) {
            case APRON -> {
                Color a = look.garmentColor();
                Path2D bib = f
                        ? t.smooth(1.5, 28, 5.6, 29, 6.8 * b, 28.6, 8.0 * b, 27.5, 8.5 * b, 24.5, 8.1 * b, 21.3,
                        6.6 * b, 18.7, 5.6 * b, 17,
                        5.1 * b, 11.5, 7 * b, 3.5, 7.6 * b, 2, 1, 2)
                        : t.shape(1.5, 26, 5.2, 27, 6.6 * b, 18, 6.5 * b, 11, 7.6 * b, 2, 1, 2);
                Brush.horizontal(g, bib, t.at(1, 10).getX(), lighter(a, 0.1), t.at(8, 10).getX(), darker(a, 0.25));
                g.setColor(darker(a, 0.4));
                g.setStroke(Brush.line(0.8));
                g.draw(new Line2D.Double(t.at(-7, 12), t.at(6.5, 12)));
                g.draw(new Line2D.Double(t.at(2, 26.5), t.at(-2, 35)));
            }
            case COAT, JACKET -> {
                Color a = look.garmentColor();
                double low = gm == Garment.COAT ? -2 : -6;
                Path2D coat = f
                        ? t.smooth(-10.4 * b, low - 1, -9.8 * b, 4, -7 * b, 13, -7.4 * b, 25, -6.4 * b, 35.5, 2.5, 36.5,
                        3.8, 31, 3, 18, 4.5, low)
                        : t.shape(-8 * b, low, -8 * b, 12, -8.5 * b, 26, -6.5 * b, 35.5, 2.5, 36.5, 3.8, 31, 3, 18, 4.5, low);
                Brush.horizontal(g, coat, back.getX(), lighter(a, 0.12), front.getX(), darker(a, 0.25));
                g.setColor(darker(a, 0.4));
                g.setStroke(Brush.line(0.6));
                g.draw(new Line2D.Double(t.at(3.5, 31), t.at(1, 22)));       // the lapel
                if (gm == Garment.JACKET) {
                    g.setColor(c(0xd8c8a0, 160));
                    g.fill(new Ellipse2D.Double(t.at(0, 25).getX() - 1, t.at(0, 25).getY() - 1, 2, 2));
                }
            }
            case SURCOAT -> {
                Color a = look.garmentColor();
                Path2D coat = t.shape(-7.8 * b, -2, -7.8 * b, 14, -7.2 * b, 30, -5, 35.5, 2, 36, 6.8 * b, 28, 6.8 * b, 12, 7.2 * b, -2);
                Brush.horizontal(g, coat, back.getX(), lighter(a, 0.1), front.getX(), darker(a, 0.25));
                if (look.has(Extra.LILIES)) lilies(g, t.at(2, 20), 1.0);
            }
            case MANTLE, ROBE, TUNIC -> {
                if (look.has(Extra.BELT) || gm == Garment.TUNIC) {
                    g.setColor(c(0x3a2414));
                    g.fill(t.shape(-7.6 * b, 9.5, 7.2 * b, 9.5, 7.2 * b, 12, -7.6 * b, 12));
                    if (look.has(Extra.POUCH)) {
                        g.setColor(c(0x6a4428));
                        Point2D q = t.at(-3, 8);
                        g.fill(new RoundRectangle2D.Double(q.getX() - 3, q.getY(), 6, 7, 2, 2));
                    }
                }
            }
            default -> {
            }
        }
        if (look.has(Extra.CROSS)) cross(g, t.at(3.5, 26), 0.9);
        if (look.has(Extra.HOOD)) {
            Path2D hood = t.shape(-9, 36, -2, 38.5, 0, 34, -3, 27, -8.5, 28);
            Brush.horizontal(g, hood, t.at(-9, 30).getX(), lighter(look.top(), 0.05), t.at(0, 30).getX(), darker(look.top(), 0.3));
        }
        if (look.has(Extra.SCARF)) {
            Color s = look.accent();
            Path2D scarf = t.shape(-5, 33, 4, 34.5, 5.5, 31, 4.5, 14, 1.5, 14, 1, 29, -5, 30);
            Brush.horizontal(g, scarf, t.at(-5, 30).getX(), lighter(s, 0.15), t.at(5, 30).getX(), darker(s, 0.2));
        }
        if (look.has(Extra.BOWTIE)) {
            g.setColor(look.accent());
            Point2D q = t.at(4.5, 34);
            g.fill(poly(q.getX() - 1, q.getY(), q.getX() + 1.6, q.getY() - 1.5, q.getX() + 1.6, q.getY() + 1.5));
        }
    }

    private static void cloakBehind(Graphics2D g, Look look, Torso t, Leg near, Leg far) {
        Color a = look.garmentColor();
        double hemY = -look.hem() + 1;
        double lo = Math.min(legX(near, hemY), legX(far, hemY));
        Point2D s1 = t.at(-6.5, 36), s2 = t.at(2, 37);
        Path2D cloak = new Path2D.Double();
        cloak.moveTo(s2.getX(), s2.getY());
        cloak.lineTo(s1.getX(), s1.getY());
        cloak.quadTo(s1.getX() - 9, (s1.getY() + hemY) / 2, lo - 15, hemY);
        cloak.lineTo(lo + 6, hemY + 1);
        cloak.lineTo(t.at(2, 6).getX(), t.at(2, 6).getY());
        cloak.closePath();
        Brush.horizontal(g, cloak, lo - 15, lighter(a, 0.08), s2.getX(), darker(a, 0.3));
        g.setColor(darker(a, 0.3));
        g.setStroke(Brush.line(0.55));
        g.draw(new QuadCurve2D.Double(s1.getX() - 2, s1.getY() + 6, s1.getX() - 8, (s1.getY() + hemY) / 2, lo - 9, hemY));
    }

    private static void cross(Graphics2D g, Point2D at, double k) {
        g.setColor(c(0xb0201a));
        double x = at.getX(), y = at.getY();
        g.fill(new Rectangle2D.Double(x - 1.1 * k, y - 4 * k, 2.2 * k, 8 * k));
        g.fill(new Rectangle2D.Double(x - 3.6 * k, y - 1.1 * k, 7.2 * k, 2.2 * k));
    }

    private static void lilies(Graphics2D g, Point2D at, double k) {
        g.setColor(c(0xe8c450));
        for (int i = -1; i <= 1; i++) {
            double x = at.getX() + i * 4 * k, y = at.getY() + (i == 0 ? -4 : 2) * k;
            g.fill(new Ellipse2D.Double(x - 0.7 * k, y - 2 * k, 1.4 * k, 3 * k));
            g.fill(new Ellipse2D.Double(x - 1.8 * k, y - 0.8 * k, 1.2 * k, 2 * k));
            g.fill(new Ellipse2D.Double(x + 0.6 * k, y - 0.8 * k, 1.2 * k, 2 * k));
        }
    }

    /** The head from the side, looking right: neck, face, ear, eye, hair. */
    private static void headSide(Graphics2D g, Look look, Torso t, Pose p) {
        Color skin = look.skin();
        Point2D n1 = t.at(0.5, 33.5), n2 = t.at(1.8, 40.5);
        limb(g, n1, n2, 6, 5.4, darker(skin, 0.12));
        if (look.has(Extra.ROLL_NECK)) {
            Point2D q = t.at(0.3, 36);
            g.setPaint(new GradientPaint((float) q.getX() - 5, 0, lighter(look.top(), 0.15), (float) q.getX() + 5, 0, darker(look.top(), 0.2)));
            g.fill(new RoundRectangle2D.Double(q.getX() - 5.2, q.getY() - 2.6, 10.6, 5.6, 4, 4));
        }
        if (look.has(Extra.COIF)) {
            Point2D q = t.at(1, 41);
            g.setColor(c(0x8a8e96));
            g.fill(new Ellipse2D.Double(q.getX() - 8, q.getY() - 14, 15.5, 19));
        }
        Point2D c = t.at(2.2, 48.3);
        AffineTransform keep = g.getTransform();
        g.translate(c.getX(), c.getY());
        g.rotate(t.lean() * 0.6 + p.headTilt());
        g.scale(-0.82, 0.82);
        profile(g, look, p.mouth() > 0.5);
        g.setTransform(keep);
    }

    /** A head in profile looking left, centred on (0, 0), about 22 pixels high: the head of the workshop mockup. */
    private static void profile(Graphics2D g, Look look, boolean open) {
        Color skinLit = lighter(look.skin(), 0.1), skinMid = look.skin(), skinDark = darker(look.skin(), 0.35);
        Color hair = look.hair();
        Hair style = look.hairStyle();
        if (style == Hair.LONG) {
            // the hair behind the head and the neck, falling to the shoulders
            g.setPaint(new GradientPaint(2, -10, hair, 9, 20, darker(hair, 0.45)));
            g.fill(curve(1, -11.8, 6.6, -9, 9.2, -2, 9.8, 6, 10.4, 13, 9.6, 19.5, 6.4, 21.2, 3.6, 18, 2.6, 11, 1.2, 4));
        }
        if (style == Hair.CHIGNON) {
            g.setColor(hair);
            g.fill(new Ellipse2D.Double(5.5, -6, 7.5, 7));
        }
        Path2D face = poly(2, -10, -5, -8, -7, -4, -7.4, -1.6, -8, -0.8, -10.1, 2.3, -8.4, 3.3, -8.9, 4.7, -8.1, 5.4,
                -8.6, 6.4, -7.4, 8.8, -4, 10.2, 2.5, 7, 6.5, 1, 6.2, -7);
        if (!look.female()) face = poly(2, -10, -5, -8, -7, -4, -7.6, -1.6, -8.2, -0.8, -10.5, 2.6, -8.6, 3.5, -9, 4.8,
                -8.3, 5.5, -8.8, 6.6, -8, 9.4, -3.6, 10.8, 2.8, 7.4, 6.8, 1, 6.4, -7);
        g.setPaint(new GradientPaint(-9, 0, skinLit, 6, 0, skinDark));
        g.fill(face);
        g.setPaint(new GradientPaint(0, 4, c(0, 0), 0, 10, c(0x4a2a1a, 70)));
        g.fill(face);
        g.setColor(skinMid);
        g.fill(new Ellipse2D.Double(1.2, -1.5, 3.2, 5));
        g.setColor(skinDark);
        g.setStroke(Brush.line(0.45));
        g.draw(new Arc2D.Double(1.8, -0.8, 2, 3.6, 90, 180, Arc2D.OPEN));
        // eye, lid, brow
        g.setColor(c(0xf4eee6));
        g.fill(new Ellipse2D.Double(-6.6, -1.3, 2.6, 1.5));
        g.setColor(look.name().equals("maria") ? c(0x3a5a4a) : c(0x4a3a2a));
        g.fill(new Ellipse2D.Double(-6.5, -1.3, 1.5, 1.5));
        g.setColor(c(0x1a0e08));
        g.fill(new Ellipse2D.Double(-6.3, -1.0, 0.8, 0.8));
        g.setStroke(Brush.line(0.5));
        g.setColor(c(0x5a3424));
        g.draw(new Line2D.Double(-6.8, -1.5, -3.9, -1.2));
        g.setStroke(Brush.line(0.8));
        g.setColor(style == Hair.BALD || style == Hair.FRINGE ? darker(hair, 0.2) : darker(hair, 0.15));
        g.draw(new QuadCurve2D.Double(-7.3, -2.9, -5.5, -4, -3.4, -3.1));
        g.setStroke(Brush.line(0.5));
        g.setColor(c(0x9a6448, 160));
        g.draw(new QuadCurve2D.Double(-8, -0.5, -7.6, 2, -8.4, 3.2));
        // lips
        g.setColor(look.female() ? c(0xb0605a) : c(0xa0685a));
        g.fill(poly(-8.8, 4.7, -6.6, 4.9, -6.8, 5.6, -8.5, 6.3, -8.2, 5.4));
        g.setColor(c(0x5a2420));
        if (open) g.fill(new Ellipse2D.Double(-8.6, 4.9, 1.9, 1.6));
        else g.draw(new Line2D.Double(-8.2, 5.4, -6.6, 5.3));
        g.setColor(c(0xe08a7a, look.female() ? 40 : 18));
        g.fill(new Ellipse2D.Double(-6.5, 1.2, 4, 2.6));
        beardProfile(g, look);
        hairProfile(g, look);
        if (look.has(Extra.GLASSES) || look.has(Extra.ROUND_GLASSES)) {
            g.setStroke(Brush.line(0.55));
            g.setColor(c(0x1a1410));
            if (look.has(Extra.ROUND_GLASSES)) g.draw(new Ellipse2D.Double(-7.9, -2.4, 3.6, 3.6));
            else g.draw(new RoundRectangle2D.Double(-7.7, -2.2, 4.1, 3.0, 1.4, 1.4));
            g.draw(new Line2D.Double(-3.6, -1.2, 1.6, -1));
            g.setColor(c(0xffffff, 90));
            g.draw(new Line2D.Double(-7.3, -1.8, -6.4, -2));
        }
        if (look.has(Extra.SUNGLASSES)) {
            g.setColor(c(0x101418));
            g.fill(new RoundRectangle2D.Double(-8, -2.4, 4.6, 3, 1.2, 1.2));
            g.setStroke(Brush.line(0.6));
            g.draw(new Line2D.Double(-3.6, -1.4, 1.6, -1.2));
        }
        if (look.has(Extra.EARRINGS)) {
            g.setColor(c(0xe8d070));
            g.fill(new Ellipse2D.Double(2.2, 3.3, 1.4, 1.4));
        }
        if (look.has(Extra.CAP)) {
            g.setColor(look.accent());
            g.fill(poly(-6.5, -6.5, -4, -11.5, 2, -13, 6.5, -10, 7.4, -5.5, 3, -6.5));
            g.fill(poly(-5.2, -7.4, -11.6, -3, -11.2, -1.6, -5.6, -5.2));  // the peak, tipped down over the eyes
            g.setColor(darker(look.accent(), 0.3));
            g.draw(new Line2D.Double(-6, -6.6, 6.8, -5.8));
        }
        if (look.has(Extra.HELMET)) {
            g.setPaint(new GradientPaint(-6, -12, c(0xb8bcc4), 6, -4, c(0x5a5e66)));
            g.fill(new Arc2D.Double(-8.5, -15.5, 17, 17, 0, 180, Arc2D.CHORD));
            g.fill(new Ellipse2D.Double(-13, -8.6, 26, 3.8));            // the brim of a kettle hat
        }
    }

    private static void hairProfile(Graphics2D g, Look look) {
        Color hair = look.hair();
        Color dark = darker(hair, 0.45);
        switch (look.hairStyle()) {
            case LONG -> {
                // over the top and down over the ear, in soft waves
                Path2D h = curve(-7.3, -4.4, -6.2, -9.8, -1, -12.8, 5, -11.8, 8.6, -6.6, 9.3, 0.5, 8.6, 7, 6.2, 10.4,
                        3, 9.6, 0.8, 6.6, 0.1, 1.5, -0.8, -3.4, -3.8, -6.4);
                g.setPaint(new GradientPaint(-6, -12, lighter(hair, 0.16), 8, 8, dark));
                g.fill(h);
                g.setStroke(Brush.line(0.45));
                g.setColor(lighter(hair, 0.3));
                for (int i = 0; i < 5; i++) {
                    double o = i * 1.5;
                    g.draw(new CubicCurve2D.Double(-4.5 + o, -9.5 - o * 0.3, 1 + o * 0.6, -11, 4 + o * 0.5, -2 + o, 2.2 + o * 0.9, 6 + o * 0.4));
                }
                g.setColor(darker(hair, 0.25));
                g.draw(new CubicCurve2D.Double(0.6, -2, 0.2, 2, 1.4, 5, 0.9, 7.4));                     // the edge over the ear
            }
            case CHIGNON -> {
                Path2D h = poly(-6.8, -5, -5.5, -9.5, -1, -12, 4.5, -11, 7.6, -6.5, 7.8, 1, 6, 4, 4.6, -1, 1.5, -3, -3, -6.2);
                g.setPaint(new GradientPaint(-6, -12, lighter(hair, 0.15), 7, 2, dark));
                g.fill(h);
                g.setStroke(Brush.line(0.45));
                g.setColor(lighter(hair, 0.3));
                for (int i = 0; i < 6; i++) {
                    double o = i * 1.3;
                    g.draw(new QuadCurve2D.Double(-5 + o, -8.5 - o * 0.4, o * 0.6, -11.5, 5 + o * 0.4, -6 + o));
                }
                g.setColor(darker(hair, 0.2));
                g.draw(new CubicCurve2D.Double(-6.4, -5, -8.2, -1, -6.2, 2, -7.4, 5));
            }
            case SHORT, CROPPED -> {
                double top = look.hairStyle() == Hair.CROPPED ? -10.8 : -11.8;
                Path2D h = poly(-6.6, -5.5, -5.6, -9.4, -1, top, 4.5, top + 0.6, 7.4, -6.5, 7.5, 0, 5.8, 2.5, 4.6, -1.5, 2, -3.6, -2.6, -6.6);
                g.setPaint(new GradientPaint(-6, -12, lighter(hair, 0.12), 7, 2, dark));
                g.fill(h);
                g.setStroke(Brush.line(0.4));
                g.setColor(lighter(hair, 0.25));
                for (int i = 0; i < 4; i++) g.draw(new Line2D.Double(-4 + i * 2.4, -9.5 - i * 0.2, -2 + i * 2.6, -7 + i * 0.4));
            }
            case BOWL -> {
                Path2D h = poly(-7.4, -3.2, -6, -9.8, -1, -12.4, 4.8, -11.4, 7.8, -6.8, 7.8, 1.6, 4.4, 1.8, 4.2, -2, 1, -3, -7, -3);
                g.setPaint(new GradientPaint(-6, -12, lighter(hair, 0.12), 7, 2, dark));
                g.fill(h);
                g.setColor(darker(hair, 0.25));
                g.setStroke(Brush.line(0.4));
                for (int i = 0; i < 5; i++) g.draw(new Line2D.Double(-6 + i * 2.3, -3.3, -5 + i * 2.6, -9));
            }
            case FRINGE -> {
                g.setPaint(new GradientPaint(0, -4, lighter(hair, 0.1), 6, 4, dark));
                g.fill(poly(1, -4.5, 5, -6.5, 7.6, -3, 7.2, 2.5, 5.2, 4, 4.4, 0, 2.2, -1.6));
                g.setColor(c(0xffffff, 60));
                g.fill(new Ellipse2D.Double(-3, -10.5, 5, 2.5));                 // the shine of the scalp
            }
            case BALD -> {
                g.setColor(c(0xffffff, 60));
                g.fill(new Ellipse2D.Double(-3, -10.5, 5, 2.5));
            }
        }
    }

    private static void beardProfile(Graphics2D g, Look look) {
        Color beard = look.hair();
        switch (look.beard()) {
            case FULL -> {
                g.setColor(beard);
                g.fill(poly(-7.6, 3, -9.2, 5.6, -8.6, 9, -6, 12.4, -1.5, 12, 3, 7.5, 4.6, 2.4, 2.4, 3.6, -1, 4.2, -5.5, 3.6));
                g.setColor(lighter(beard, 0.25));
                g.setStroke(Brush.line(0.4));
                for (int i = 0; i < 4; i++) g.draw(new Line2D.Double(-6 + i * 2, 6 + i * 0.3, -5.4 + i * 2, 10.5));
                g.setColor(c(0x5a2420));
                g.draw(new Line2D.Double(-8.6, 5.4, -7, 5.3));
            }
            case MOUSTACHE -> {
                g.setColor(beard);
                g.fill(poly(-9.2, 3.6, -6.2, 3.5, -5.4, 4.6, -7.6, 4.8, -9.4, 5.2));
            }
            case STUBBLE -> {
                g.setColor(Brush.c(beard.getRGB() & 0xffffff, 70));
                g.fill(poly(-8.5, 3.6, -8.8, 7, -7, 9.6, -3.6, 10.5, 2.5, 7, 3.5, 2.5, -2, 3.5));
            }
            case NONE -> {
            }
        }
    }

    // ---- from the front and from behind ---------------------------------------------------------------

    private static void straight(Graphics2D g, Look look, Pose p, boolean front) {
        double b = look.build();
        boolean f = look.female();
        double hipY = -60 + p.crouch();
        double perspective = front ? 0.16 : -0.16;
        double[] nearFoot = footStraight(p.nearHip(), p.nearKnee() * 0.55 + p.crouch() * 0.02, hipY, perspective);
        double[] farFoot = footStraight(p.farHip(), p.farKnee() * 0.55 + p.crouch() * 0.02, hipY, perspective);
        double ground = Math.max(nearFoot[2], farFoot[2]);
        g.translate(0, -ground);
        // From the front the near side (the person's left) is on the right of the picture.
        double nearSide = front ? 1 : -1;
        double spreadKnees = p.crouch() * 0.3;
        double hip = 4.8 * b;
        double sh = (f ? 11.4 : 13) * b, waist = (f ? 7 : 9) * b, hips = (f ? 12.2 : 9.6) * b;
        double shoulderY = -95.5 + p.crouch() * 0.95 + Math.max(0, p.lean()) * 4;
        double waistY = shoulderY + 24, hipsY = hipY - 1;

        Garment gm = look.garment();
        if (gm == Garment.MANTLE) {
            Color a = look.garmentColor();
            double hemY = -look.hem();
            Path2D cloak = poly(-sh - 1, shoulderY + 1, sh + 1, shoulderY + 1, sh + 9, hemY, -sh - 9, hemY);
            Brush.horizontal(g, cloak, -sh - 9, lighter(a, 0.1), sh + 9, darker(a, 0.28));
        }
        // legs
        Color legs = look.legs();
        for (int side = -1; side <= 1; side += 2) {
            boolean isNear = side == nearSide;
            double[] foot = isNear ? nearFoot : farFoot;
            double x = side * hip, kx = side * (hip + 0.4 + spreadKnees), ax = side * (hip - 0.4);
            Point2D h = new Point2D.Double(x, hipY), k = new Point2D.Double(kx, foot[0]), a = new Point2D.Double(ax, foot[1]);
            limb(g, h, k, (f ? 11.8 : 10.4) * b, 7.6, legs);
            limb(g, k, a, 7.6, 5.2, legs);
            shoeStraight(g, look, ax, foot[1], front, side);
        }
        // pelvis
        g.setColor(legs);
        g.fill(poly(-hips, hipsY - 4, hips, hipsY - 4, hip + 5, hipY + 4, 0, hipY + 6, -hip - 5, hipY + 4));
        if (f && !front) seatBack(g, legs, hipY, b);
        skirtStraight(g, look, p, hipY, nearFoot, farFoot, hips, front);
        // the upper body
        Path2D body = f
                ? poly(-sh, shoulderY + 0.5, -sh + 0.4, shoulderY + 8, -sh + 0.6, shoulderY + 14, -waist - 0.8, shoulderY + 19,
                -waist, waistY, -hips + 1.5, waistY + 7, -hips, hipsY, -hips + 0.5, hipsY + 4,
                hips - 0.5, hipsY + 4, hips, hipsY, hips - 1.5, waistY + 7, waist, waistY, waist + 0.8, shoulderY + 19,
                sh - 0.6, shoulderY + 14, sh - 0.4, shoulderY + 8, sh, shoulderY + 0.5,
                sh - 3.5, shoulderY - 2, 3.8, shoulderY - 3.2, -3.8, shoulderY - 3.2, -sh + 3.5, shoulderY - 2)
                : poly(-sh, shoulderY + 0.5, -sh + 0.6, shoulderY + 8, -waist, waistY, -hips, hipsY, -hips + 0.5, hipsY + 4,
                hips - 0.5, hipsY + 4, hips, hipsY, waist, waistY, sh - 0.6, shoulderY + 8, sh, shoulderY + 0.5,
                sh - 3.5, shoulderY - 2, 3.8, shoulderY - 3.2, -3.8, shoulderY - 3.2, -sh + 3.5, shoulderY - 2);
        Brush.horizontal(g, body, -sh, lighter(look.top(), 0.14), sh, darker(look.top(), 0.22));
        if (look.has(Extra.ROLL_NECK) && front) {
            g.setColor(darker(look.top(), 0.12));
            g.setStroke(Brush.line(0.4));
            for (double x = -sh + 3; x < sh - 2; x += 2.4) g.draw(new Line2D.Double(x, shoulderY + 4, x * 0.92, waistY + 4));
        }
        garmentStraight(g, look, sh, waist, hips, shoulderY, waistY, hipsY, front, b);
        if (f && front && gm != Garment.COAT && gm != Garment.APRON) bustFront(g, look.top(), shoulderY, b);
        // arms
        Color sleeve = sleeve(look);
        Point2D[] hands = new Point2D[2];
        for (int side = -1; side <= 1; side += 2) {
            boolean isNear = side == nearSide;
            double s = isNear ? p.nearShoulder() : p.farShoulder(), e = isNear ? p.nearElbow() : p.farElbow();
            double f1 = UPPER_ARM * Math.sin(s), d1 = UPPER_ARM * Math.cos(s);
            double f2 = FOREARM * Math.sin(s + e), d2 = FOREARM * Math.cos(s + e);
            Point2D shoulder = new Point2D.Double(side * (sh - 1.5), shoulderY + 2.5);
            Point2D elbow = new Point2D.Double(side * (sh + 0.8 - f1 * 0.2 + p.spread() * 6), shoulderY + 2.5 + d1 * 0.92);
            Point2D wrist = new Point2D.Double(side * (sh + 1.2 - (f1 + f2) * 0.36 + p.spread() * 15), elbow.getY() + d2);
            limb(g, shoulder, elbow, 6.8, 6, side == -1 ? lighter(sleeve, 0.02) : darker(sleeve, 0.04));
            limb(g, elbow, wrist, 6, 4.8, side == -1 ? lighter(sleeve, 0.02) : darker(sleeve, 0.04));
            double hx = wrist.getX() + (wrist.getX() - elbow.getX()) * 0.12, hy = wrist.getY() + 3;
            g.setPaint(new GradientPaint((float) hx - 3, 0, lighter(look.skin(), 0.1), (float) hx + 3, 0, darker(look.skin(), 0.2)));
            g.fill(new Ellipse2D.Double(hx - 2.7, hy - 3.4, 5.4, 6.6));
            hands[side < 0 ? 0 : 1] = new Point2D.Double(hx, hy);
        }
        headStraight(g, look, p, shoulderY, front);
        if (look.has(Extra.HANDBAG)) {
            Point2D h = hands[front ? 0 : 1];
            g.setColor(c(0x5a1a1a));
            g.setStroke(Brush.line(0.6));
            g.draw(new Line2D.Double(h.getX(), h.getY() - 2, h.getX() - 2, h.getY() + 4));
            g.fill(new RoundRectangle2D.Double(h.getX() - 6, h.getY() + 3, 9, 7, 2, 2));
        }
        Point2D nearHand = hands[nearSide < 0 ? 0 : 1];
        held(g, look, nearHand, p.nearShoulder() + p.nearElbow(), false);
    }

    /** A woman's bust from the front: two soft rounds, lit from the left, with a shadow underneath. */
    private static void bustFront(Graphics2D g, Color cloth, double shoulderY, double b) {
        double w = 6.6 * b, h = 5.8 * b, y = shoulderY + 10;
        for (int side = -1; side <= 1; side += 2) {
            double x = side * 3.8 * b;
            g.setPaint(new GradientPaint((float) (x - w / 2), (float) y, lighter(cloth, side < 0 ? 0.1 : 0.04),
                    (float) (x + w / 2), (float) (y + h), darker(cloth, side < 0 ? 0.04 : 0.12)));
            g.fill(new Ellipse2D.Double(x - w / 2, y, w, h));
            g.setColor(Brush.c(darker(cloth, 0.45).getRGB() & 0xffffff, 150));
            g.setStroke(Brush.line(0.6));
            g.draw(new Arc2D.Double(x - w / 2, y, w, h, 215, 110, Arc2D.OPEN));
        }
        g.setColor(darker(cloth, 0.2));
        g.setStroke(Brush.line(0.45));
        g.draw(new Line2D.Double(0, y + h * 0.35, 0, y + h * 0.8));
    }

    /** A woman's seat from behind: two rounds below the waist, with the fold between them. */
    private static void seatBack(Graphics2D g, Color legs, double hipY, double b) {
        double w = 11.4 * b, h = 12, y = hipY - 6;
        for (int side = -1; side <= 1; side += 2) {
            double x = side * 5.2 * b;
            g.setPaint(new GradientPaint((float) (x - w / 2), (float) y, lighter(legs, side > 0 ? 0.14 : 0.06),
                    (float) (x + w / 2), (float) (y + h), darker(legs, side > 0 ? 0.08 : 0.2)));
            g.fill(new Ellipse2D.Double(x - w / 2, y, w, h));
        }
        g.setColor(darker(legs, 0.35));
        g.setStroke(Brush.line(0.6));
        g.draw(new Line2D.Double(0, y + 2, 0, y + h - 0.5));
    }

    /** Knee y, ankle y and sole y of a leg seen from the front; a leg reaching forward comes lower (nearer). */
    private static double[] footStraight(double thigh, double bend, double hipY, double perspective) {
        double kneeY = hipY + THIGH * Math.cos(thigh);
        double shin = thigh - bend;
        double ankleY = kneeY + SHIN * Math.cos(shin);
        double forward = THIGH * Math.sin(thigh) + SHIN * Math.sin(shin);
        ankleY += forward * perspective;
        kneeY += THIGH * Math.sin(thigh) * perspective;
        return new double[]{kneeY, ankleY, ankleY + 4};
    }

    private static void shoeStraight(Graphics2D g, Look look, double x, double ankleY, boolean front, int side) {
        Color shoe = look.shoes();
        g.setPaint(new GradientPaint(0, (float) ankleY - 1, lighter(shoe, 0.2), 0, (float) ankleY + 4, shoe));
        if (front) g.fill(new RoundRectangle2D.Double(x - 3.2 + side * 0.6, ankleY - 1.5, 6.4, 5.8, 4, 4));
        else g.fill(new RoundRectangle2D.Double(x - 3, ankleY - 1.2, 6, 5.2, 3, 3));
    }

    private static void skirtStraight(Graphics2D g, Look look, Pose p, double hipY, double[] nearFoot, double[] farFoot,
                                      double hips, boolean front) {
        Garment gm = look.garment();
        if (gm == Garment.NONE || gm == Garment.JACKET) return;
        double hemY = -look.hem();
        Color color = gm == Garment.ROBE || gm == Garment.MANTLE ? look.top() : look.garmentColor();
        if (gm == Garment.APRON && !front) return;
        double width = switch (gm) {
            case APRON -> 9;
            case COAT -> hips + 4.5;
            case ROBE, MANTLE -> hips + 3.5;
            default -> hips + 2.5;
        };
        double sway = (nearFoot[1] - farFoot[1]) * 0.15;
        Path2D s = new Path2D.Double();
        double topY = gm == Garment.APRON ? hipY - 12 : hipY - 6;
        double topW = gm == Garment.APRON ? 7.5 : hips + 0.5;
        s.moveTo(-topW, topY);
        s.quadTo(-width - 0.5, (topY + hemY) / 2, -width, hemY + sway);
        s.quadTo(0, hemY + 2.4, width, hemY - sway);
        s.quadTo(width + 0.5, (topY + hemY) / 2, topW, topY);
        s.closePath();
        Brush.horizontal(g, s, -width, lighter(color, 0.12), width, darker(color, 0.25));
        g.setColor(darker(color, 0.3));
        g.setStroke(Brush.line(0.55));
        if (gm == Garment.COAT && front) {
            g.setColor(darker(color, 0.45));
            g.draw(new Line2D.Double(0.5, topY, 0.8, hemY + 1));
        } else if (gm != Garment.APRON) {
            g.draw(new QuadCurve2D.Double(-width * 0.45, topY + 6, -width * 0.5, (topY + hemY) / 2, -width * 0.55, hemY));
            g.draw(new QuadCurve2D.Double(width * 0.45, topY + 6, width * 0.5, (topY + hemY) / 2, width * 0.55, hemY));
        }
    }

    private static void garmentStraight(Graphics2D g, Look look, double sh, double waist, double hips, double shoulderY,
                                        double waistY, double hipsY, boolean front, double b) {
        Garment gm = look.garment();
        Color a = look.garmentColor();
        switch (gm) {
            case APRON -> {
                if (!front) {
                    g.setColor(darker(a, 0.25));
                    g.setStroke(Brush.line(1));
                    g.draw(new Line2D.Double(-waist, waistY + 1, waist, waistY + 1));
                    g.draw(new Line2D.Double(-3, shoulderY - 1, -sh + 3, waistY));
                    g.draw(new Line2D.Double(3, shoulderY - 1, sh - 3, waistY));
                    return;
                }
                boolean f = look.female();
                Path2D bib = f
                        ? poly(-5.5, shoulderY + 6, 5.5, shoulderY + 6, 7.6 * b, shoulderY + 10.5, 8.2 * b, shoulderY + 15,
                        waist - 0.5, waistY + 2, -waist + 0.5, waistY + 2, -8.2 * b, shoulderY + 15, -7.6 * b, shoulderY + 10.5)
                        : poly(-5.5, shoulderY + 6, 5.5, shoulderY + 6, waist - 0.5, waistY + 2, -waist + 0.5, waistY + 2);
                Brush.horizontal(g, bib, -6, lighter(a, 0.1), 6, darker(a, 0.2));
                if (f) {                                 // the bib lies over the bust
                    Shape keep = g.getClip();
                    g.clip(bib);
                    bustFront(g, a, shoulderY, b);
                    g.setClip(keep);
                }
                g.setColor(darker(a, 0.3));
                g.setStroke(Brush.line(0.7));
                g.draw(new Line2D.Double(-5, shoulderY + 6, -3.5, shoulderY - 2.5));
                g.draw(new Line2D.Double(5, shoulderY + 6, 3.5, shoulderY - 2.5));
                double py = f ? shoulderY + 18 : shoulderY + 12;
                g.setColor(darker(a, 0.25));
                g.fill(new Rectangle2D.Double(-3.5, py, 7, f ? 4.5 : 5));            // pocket
                g.setColor(c(0x3a3020));
                g.draw(new Line2D.Double(1.5, py - 2.5, 2, py + 1));
            }
            case COAT, JACKET -> {
                double low = gm == Garment.COAT ? hipsY + 4 : hipsY + 6;
                double open = front ? (gm == Garment.JACKET ? 3.2 : 3.6) : 0;
                for (int side = -1; side <= 1; side += 2) {
                    if (!front && side == 1) break;
                    Path2D half = front
                            ? poly(side * open, low, side * (hips + 1), low, side * (hips + 0.5), hipsY, side * (waist + 1), waistY,
                            side * (sh - 0.4), shoulderY + 8, side * (sh + 0.5), shoulderY + 0.5, side * (sh - 3.5), shoulderY - 2,
                            side * 3.4, shoulderY - 2.8, side * 1.8, shoulderY + 5, side * open, waistY - 3)
                            : poly(-hips - 1, low, hips + 1, low, hips + 0.5, hipsY, waist + 1, waistY, sh - 0.4, shoulderY + 8,
                            sh + 0.5, shoulderY + 0.5, sh - 3.5, shoulderY - 2, -sh + 3.5, shoulderY - 2, -sh - 0.5, shoulderY + 0.5,
                            -sh + 0.4, shoulderY + 8, -waist - 1, waistY, -hips - 0.5, hipsY);
                    Brush.horizontal(g, half, -sh, lighter(a, 0.12), sh, darker(a, 0.25));
                    if (front) {
                        g.setColor(darker(a, 0.4));
                        g.setStroke(Brush.line(0.55));
                        g.draw(new Line2D.Double(side * 3.4, shoulderY - 2.5, side * (open + 2.5), waistY - 6));
                    }
                }
                if (front && gm == Garment.JACKET) {
                    g.setColor(c(0xd8c8a0, 170));
                    for (int i = 0; i < 3; i++) g.fill(new Ellipse2D.Double(-0.8, shoulderY + 12 + i * 4, 1.6, 1.6));
                }
            }
            case SURCOAT -> {
                Path2D coat = poly(-sh + 2.5, shoulderY - 0.5, sh - 2.5, shoulderY - 0.5, sh - 1.5, waistY, hips + 1, hipsY + 4,
                        -hips - 1, hipsY + 4, -sh + 1.5, waistY);
                Brush.horizontal(g, coat, -sh, lighter(a, 0.1), sh, darker(a, 0.25));
                if (look.has(Extra.LILIES)) lilies(g, new Point2D.Double(0, shoulderY + 14), 1.15);
            }
            case MANTLE -> {
                Color m = look.garmentColor();
                for (int side = -1; side <= 1; side += 2) {
                    Path2D flap = poly(side * (sh - 3.5), shoulderY - 2, side * (sh + 1.2), shoulderY + 0.5, side * (sh + 3), waistY + 4,
                            side * (sh + 2.6), hipsY + 18, side * (sh - 2.6), hipsY + 18, side * (sh - 3.2), waistY);
                    Brush.horizontal(g, flap, -sh - 3, lighter(m, 0.08), sh + 3, darker(m, 0.25));
                }
                if (!front) {
                    Path2D back = poly(-sh - 1, shoulderY + 0.5, sh + 1, shoulderY + 0.5, sh + 3, hipsY + 18, -sh - 3, hipsY + 18);
                    Brush.horizontal(g, back, -sh, lighter(m, 0.08), sh, darker(m, 0.25));
                    if (look.has(Extra.CROSS)) cross(g, new Point2D.Double(0, shoulderY + 14), 1.3);
                }
            }
            default -> {
            }
        }
        if ((gm == Garment.TUNIC || gm == Garment.MANTLE || gm == Garment.ROBE) && (look.has(Extra.BELT) || gm == Garment.TUNIC)) {
            g.setColor(c(0x3a2414));
            g.fill(new Rectangle2D.Double(-waist - 0.6, waistY + 1, waist * 2 + 1.2, 2.4));
            g.setColor(c(0xb08a40));
            g.fill(new Rectangle2D.Double(-1.2, waistY + 1, 2.4, 2.4));
            if (look.has(Extra.POUCH)) {
                g.setColor(c(0x6a4428));
                g.fill(new RoundRectangle2D.Double(front ? waist - 4 : -waist + 1, waistY + 3, 5, 6, 2, 2));
            }
        }
        if (front && look.has(Extra.CROSS) && gm != Garment.MANTLE) cross(g, new Point2D.Double(sh * 0.4, shoulderY + 9), 0.95);
        if (front && look.has(Extra.CROSS) && gm == Garment.MANTLE) cross(g, new Point2D.Double(sh * 0.45, shoulderY + 9), 0.95);
        if (look.has(Extra.HOOD)) {
            Color h = darker(look.top(), front ? 0.15 : 0.05);
            g.setColor(h);
            if (front) g.fill(new RoundRectangle2D.Double(-6.5, shoulderY - 3.5, 13, 4.5, 4, 4));
            else g.fill(Brush.blob(-8, shoulderY - 3, 8, shoulderY - 3, 6, shoulderY + 11, 0, shoulderY + 14, -6, shoulderY + 11));
        }
        if (look.has(Extra.SCARF)) {
            Color s = look.accent();
            if (front) {
                Path2D scarf = poly(-5.5, shoulderY - 2.5, 5.5, shoulderY - 2.5, 5, shoulderY + 3, 3.2, shoulderY + 4, 3.8, shoulderY + 22,
                        0.6, shoulderY + 22, 0.8, shoulderY + 4, -5, shoulderY + 3);
                Brush.horizontal(g, scarf, -5, lighter(s, 0.15), 5, darker(s, 0.2));
            } else {
                Path2D scarf = poly(-5.5, shoulderY - 2.5, 5.5, shoulderY - 2.5, 5.5, shoulderY + 2.5, -5.5, shoulderY + 2.5);
                Brush.horizontal(g, scarf, -5, lighter(s, 0.15), 5, darker(s, 0.2));
            }
        }
        if (front && look.has(Extra.BOWTIE)) {
            g.setColor(look.accent());
            g.fill(poly(-3, shoulderY - 2.5, 0, shoulderY - 1, 3, shoulderY - 2.5, 3, shoulderY + 0.8, 0, shoulderY - 0.6, -3, shoulderY + 0.8));
        }
    }

    private static void headStraight(Graphics2D g, Look look, Pose p, double shoulderY, boolean front) {
        Color skin = look.skin();
        double cy = shoulderY - 12.6 + Math.max(0, p.lean()) * 2;
        double tilt = p.headTilt() * 0.4;
        double neckY = look.has(Extra.ROLL_NECK) ? shoulderY - 3 : shoulderY + 1;   // a roll neck hides the base
        limb(g, new Point2D.Double(0, neckY), new Point2D.Double(0, cy + 6), 6, 5.6, darker(skin, 0.12));
        if (look.has(Extra.ROLL_NECK)) {
            g.setPaint(new GradientPaint(-5, 0, lighter(look.top(), 0.15), 5, 0, darker(look.top(), 0.2)));
            g.fill(new RoundRectangle2D.Double(-5.4, shoulderY - 5, 10.8, 5.6, 4, 4));
        }
        if (look.has(Extra.COIF)) {
            g.setColor(c(0x8a8e96));
            g.fill(new Ellipse2D.Double(-9, cy - 11, 18, 22));
            g.fill(new RoundRectangle2D.Double(-10, shoulderY - 5, 20, 8, 4, 4));
        }
        AffineTransform keep = g.getTransform();
        g.translate(0, cy);
        g.rotate(tilt);
        if (front) faceFront(g, look, p.mouth() > 0.5);
        else headBack(g, look);
        g.setTransform(keep);
    }

    private static void faceFront(Graphics2D g, Look look, boolean open) {
        Color skin = look.skin(), hair = look.hair();
        boolean f = look.female();
        Hair style = look.hairStyle();
        double w = f ? 6.3 : 6.7;
        if (style == Hair.CHIGNON) {
            g.setColor(darker(hair, 0.35));
            g.fill(new Ellipse2D.Double(-w - 0.8, -10.5, w * 2 + 1.6, 14));
        }
        if (style == Hair.LONG) {
            // the hair behind the face, down to the shoulders
            g.setPaint(new GradientPaint(0, -12, darker(hair, 0.2), 0, 16, darker(hair, 0.5)));
            g.fill(curve(-w - 1.8, -8, 0, -13.4, w + 1.8, -8, w + 3, 0, w + 3.6, 8, w + 3.2, 14.5, w + 0.6, 16,
                    w - 1.6, 9, 0, 6.5, -w + 1.6, 9, -w - 0.6, 16, -w - 3.2, 14.5, -w - 3.6, 8, -w - 3, 0));
        } else {
            // ears
            g.setColor(darker(skin, 0.1));
            g.fill(new Ellipse2D.Double(-w - 1.6, -2.2, 2.8, 4.6));
            g.fill(new Ellipse2D.Double(w - 1.2, -2.2, 2.8, 4.6));
        }
        Path2D face = new Path2D.Double();
        face.moveTo(-w, -4);
        face.curveTo(-w, -11.5, w, -11.5, w, -4);
        face.curveTo(w, 1.5, w * 0.62, 6.6, 0, f ? 8.6 : 9.2);
        face.curveTo(-w * 0.62, 6.6, -w, 1.5, -w, -4);
        face.closePath();
        g.setPaint(new GradientPaint((float) -w, 0, lighter(skin, 0.12), (float) w, 0, darker(skin, 0.2)));
        g.fill(face);
        g.setPaint(new GradientPaint(0, 3, c(0, 0), 0, 9, c(0x4a2a1a, 60)));
        g.fill(face);
        // eyes
        for (int s = -1; s <= 1; s += 2) {
            double ex = s * 2.6;
            g.setColor(c(0xf4eee6));
            g.fill(new Ellipse2D.Double(ex - 1.4, -1.4, 2.8, 1.6));
            g.setColor(look.name().equals("maria") ? c(0x3a5a4a) : c(0x4a3a2a));
            g.fill(new Ellipse2D.Double(ex - 0.8, -1.4, 1.6, 1.6));
            g.setColor(c(0x1a0e08));
            g.fill(new Ellipse2D.Double(ex - 0.4, -1.1, 0.8, 0.8));
            g.setStroke(Brush.line(0.45));
            g.setColor(c(0x5a3424));
            g.draw(new Line2D.Double(ex - 1.5, -1.5, ex + 1.5, -1.5));
            g.setStroke(Brush.line(0.75));
            g.setColor(darker(hair, 0.15));
            g.draw(new QuadCurve2D.Double(ex - 1.8, -3.1, ex, -4.1, ex + 1.8, -3.2));
        }
        g.setStroke(Brush.line(0.5));
        g.setColor(c(0x9a6448, 150));
        g.draw(new QuadCurve2D.Double(0.4, -1, 1.3, 1.6, 0, 2.5));
        g.setColor(f ? c(0xb0605a) : c(0xa06a5a));
        g.fill(new Ellipse2D.Double(-1.8, 4.3, 3.6, 1.3));
        g.setColor(c(0x5a2420));
        if (open) g.fill(new Ellipse2D.Double(-1.1, 4.4, 2.2, 1.9));
        else g.draw(new Line2D.Double(-1.5, 4.9, 1.5, 4.9));
        g.setColor(c(0xe08a7a, f ? 36 : 16));
        g.fill(new Ellipse2D.Double(-5.2, 1.2, 3.2, 2));
        g.fill(new Ellipse2D.Double(2, 1.2, 3.2, 2));
        switch (look.beard()) {
            case FULL -> {
                Path2D beard = new Path2D.Double();
                beard.moveTo(-w + 0.3, -0.5);
                beard.curveTo(-w + 0.5, 6, -3, 11.5, 0, 12);
                beard.curveTo(3, 11.5, w - 0.5, 6, w - 0.3, -0.5);
                beard.lineTo(w - 1.6, 2.6);
                beard.curveTo(2.5, 3.2, 1.5, 3.3, 0, 3.4);
                beard.curveTo(-1.5, 3.3, -2.5, 3.2, -w + 1.6, 2.6);
                beard.closePath();
                g.setPaint(new GradientPaint((float) -w, 0, lighter(hair, 0.1), (float) w, 0, darker(hair, 0.25)));
                g.fill(beard);
                g.setColor(c(0x5a2420));
                g.draw(new Line2D.Double(-1.4, 5, 1.4, 5));
            }
            case MOUSTACHE -> {
                g.setColor(hair);
                g.fill(poly(-3, 4.3, 0, 3.2, 3, 4.3, 2.4, 3.2, 0, 2.6, -2.4, 3.2));
            }
            case STUBBLE -> {
                g.setColor(Brush.c(hair.getRGB() & 0xffffff, 60));
                Path2D s = new Path2D.Double();
                s.moveTo(-w + 0.6, 0);
                s.curveTo(-w + 0.6, 4, -3, 8.5, 0, 9);
                s.curveTo(3, 8.5, w - 0.6, 4, w - 0.6, 0);
                s.lineTo(w - 2, 2.5);
                s.lineTo(-w + 2, 2.5);
                s.closePath();
                g.fill(s);
            }
            case NONE -> {
            }
        }
        // hair
        Color dark = darker(hair, 0.45);
        switch (style) {
            case LONG -> {
                // parted on the right, falling over both ears to below the chin
                Path2D h = curve(-w - 1.6, 9, -w - 2, 0, -w - 0.8, -8, 0, -12.2, w + 0.8, -8, w + 2, 0, w + 1.6, 9,
                        w - 0.2, 4, w - 0.4, -2, w - 1.6, -5.8, 1.6, -6.8,      // the parting on the right
                        -1.6, -7.2, -w + 0.6, -5.4, -w + 0.4, -1, -w + 0.2, 4);
                g.setPaint(new GradientPaint(-6, -11, lighter(hair, 0.06), 6, 8, dark));
                g.fill(h);
                g.setStroke(Brush.line(0.4));
                g.setColor(lighter(hair, 0.14));
                for (int i = 0; i < 3; i++) {
                    g.draw(new QuadCurve2D.Double(0.6 - i * 0.8, -9.6 - i * 0.6, -w - 1.6 + i * 0.3, -9 + i * 0.8, -w - 0.8 + i * 0.2, 7 - i));
                    g.draw(new QuadCurve2D.Double(2.6 + i * 0.8, -9.6 - i * 0.6, w + 1.6 - i * 0.3, -9 + i * 0.8, w + 0.8 - i * 0.2, 7 - i));
                }
            }
            case CHIGNON -> {
                Path2D h = new Path2D.Double();
                h.moveTo(-w - 0.6, -1.5);
                h.curveTo(-w - 1.2, -13, w + 1.2, -13, w + 0.6, -1.5);
                h.curveTo(w - 0.6, -5, 3, -7.8, -0.6, -8);          // the parting on the right
                h.curveTo(-3, -7, -w + 0.6, -5, -w - 0.6, -1.5);
                h.closePath();
                g.setPaint(new GradientPaint(-6, -11, lighter(hair, 0.15), 6, -1, dark));
                g.fill(h);
                g.setStroke(Brush.line(0.4));
                g.setColor(lighter(hair, 0.3));
                for (int i = 0; i < 4; i++) g.draw(new QuadCurve2D.Double(-1 + i * 0.5, -8.5, -4 - i, -8 + i * 0.6, -w + 0.2 + i * 0.4, -2.5 + i));
                g.setColor(darker(hair, 0.2));
                g.draw(new CubicCurve2D.Double(-w + 0.4, -3, -w - 0.6, 0, -w + 0.8, 2, -w, 4.5));       // the loose strand
            }
            case SHORT, CROPPED -> {
                double top = style == Hair.CROPPED ? -11 : -12;
                Path2D h = new Path2D.Double();
                h.moveTo(-w - 0.4, -1.5);
                h.curveTo(-w - 1, top, w + 1, top, w + 0.4, -1.5);
                h.lineTo(w - 0.6, -4.5);
                h.curveTo(3, -7.2, -3, -7.6, -w + 0.6, -4.5);
                h.closePath();
                g.setPaint(new GradientPaint(-6, -11, lighter(hair, 0.12), 6, -1, dark));
                g.fill(h);
            }
            case BOWL -> {
                Path2D h = new Path2D.Double();
                h.moveTo(-w - 1, 0);
                h.curveTo(-w - 1.4, -14, w + 1.4, -14, w + 1, 0);
                h.lineTo(w - 0.6, -2.2);
                h.lineTo(w - 0.8, -4);
                h.lineTo(-w + 0.8, -4);
                h.lineTo(-w + 0.6, -2.2);
                h.closePath();
                g.setPaint(new GradientPaint(-6, -11, lighter(hair, 0.12), 6, -1, dark));
                g.fill(h);
                g.setColor(darker(hair, 0.25));
                g.setStroke(Brush.line(0.4));
                for (int i = -2; i <= 2; i++) g.draw(new Line2D.Double(i * 2.2, -4, i * 2.4, -9));
            }
            case FRINGE -> {
                g.setPaint(new GradientPaint(-6, -4, lighter(hair, 0.1), 6, 2, dark));
                g.fill(poly(-w - 0.6, 1, -w - 0.8, -5, -w + 1.2, -6.5, -w + 1.2, -2));
                g.fill(poly(w + 0.6, 1, w + 0.8, -5, w - 1.2, -6.5, w - 1.2, -2));
                g.setColor(c(0xffffff, 60));
                g.fill(new Ellipse2D.Double(-3.5, -10.2, 4.5, 2));
            }
            case BALD -> {
                g.setColor(c(0xffffff, 60));
                g.fill(new Ellipse2D.Double(-3.5, -10.2, 4.5, 2));
            }
        }
        if (look.has(Extra.GLASSES) || look.has(Extra.ROUND_GLASSES)) {
            g.setStroke(Brush.line(0.5));
            g.setColor(c(0x1a1410));
            for (int s = -1; s <= 1; s += 2) {
                if (look.has(Extra.ROUND_GLASSES)) g.draw(new Ellipse2D.Double(s * 2.6 - 1.9, -2.3, 3.8, 3.8));
                else g.draw(new RoundRectangle2D.Double(s * 2.6 - 2.1, -2.2, 4.2, 2.9, 1.3, 1.3));
            }
            g.draw(new Line2D.Double(-0.5, -1.3, 0.5, -1.3));
            g.setColor(c(0xffffff, 80));
            g.draw(new Line2D.Double(-3.8, -1.6, -3, -1.9));
        }
        if (look.has(Extra.SUNGLASSES)) {
            g.setColor(c(0x101418));
            for (int s = -1; s <= 1; s += 2) g.fill(new RoundRectangle2D.Double(s * 2.6 - 2.3, -2.4, 4.6, 3, 1.3, 1.3));
            g.setStroke(Brush.line(0.6));
            g.draw(new Line2D.Double(-0.4, -1.4, 0.4, -1.4));
        }
        if (look.has(Extra.EARRINGS)) {
            g.setColor(c(0xe8d070));
            g.fill(new Ellipse2D.Double(-w - 1, 2.4, 1.4, 1.4));
            g.fill(new Ellipse2D.Double(w - 0.4, 2.4, 1.4, 1.4));
        }
        headwearFront(g, look, w, true);
    }

    private static void headBack(Graphics2D g, Look look) {
        Color skin = look.skin(), hair = look.hair();
        double w = look.female() ? 6.3 : 6.7;
        if (look.hairStyle() != Hair.LONG) {
            g.setColor(darker(skin, 0.15));
            g.fill(new Ellipse2D.Double(-w - 1.6, -2.2, 2.8, 4.6));
            g.fill(new Ellipse2D.Double(w - 1.2, -2.2, 2.8, 4.6));
        }
        Path2D skull = new Path2D.Double();
        skull.moveTo(-w, -3);
        skull.curveTo(-w, -11.8, w, -11.8, w, -3);
        skull.curveTo(w, 2.5, w * 0.6, 7, 0, 7.6);
        skull.curveTo(-w * 0.6, 7, -w, 2.5, -w, -3);
        skull.closePath();
        Hair style = look.hairStyle();
        boolean bald = style == Hair.BALD || style == Hair.FRINGE;
        g.setPaint(new GradientPaint((float) -w, -8, bald ? lighter(skin, 0.05) : lighter(hair, 0.12), (float) w, 4,
                bald ? darker(skin, 0.25) : darker(hair, 0.4)));
        g.fill(skull);
        if (style == Hair.FRINGE) {
            g.setPaint(new GradientPaint(-6, 0, lighter(hair, 0.1), 6, 4, darker(hair, 0.35)));
            g.fill(poly(-w - 0.6, -2.5, w + 0.6, -2.5, w * 0.8, 2.5, 0, 3.4, -w * 0.8, 2.5));
        }
        if (style == Hair.LONG) {
            // one full mass from the crown over the ears to the shoulders
            g.setPaint(new GradientPaint((float) -w, -10, lighter(hair, 0.1), (float) w, 16, darker(hair, 0.45)));
            g.fill(curve(0, -12.6, w + 0.6, -10, w + 2, -3, w + 2.8, 5, w + 3.2, 12, w + 1.6, 16.4, 0, 17.4,
                    -w - 1.6, 16.4, -w - 3.2, 12, -w - 2.8, 5, -w - 2, -3, -w - 0.6, -10));
            g.setStroke(Brush.line(0.45));
            g.setColor(darker(hair, 0.32));
            for (int i = -3; i <= 3; i++) g.draw(new CubicCurve2D.Double(i * 0.5, -11.5, i * 2.6, -5, i * 2.4, 6, i * 2.2, 16));
        }
        if (style == Hair.CHIGNON) {
            g.setPaint(new GradientPaint(-3, -6, lighter(hair, 0.1), 3, 1, darker(hair, 0.4)));
            g.fill(new Ellipse2D.Double(-3.8, -6, 7.6, 7));
        }
        if (style != Hair.BALD && style != Hair.FRINGE && style != Hair.LONG) {
            g.setStroke(Brush.line(0.4));
            g.setColor(darker(hair, 0.3));
            for (int i = -2; i <= 2; i++) g.draw(new QuadCurve2D.Double(i * 0.6, -10, i * 2.4, -4, i * 2.2, 3));
        } else {
            g.setColor(c(0xffffff, 50));
            g.fill(new Ellipse2D.Double(-3.5, -10.2, 4.5, 2));
        }
        headwearFront(g, look, w, false);
    }

    private static void headwearFront(Graphics2D g, Look look, double w, boolean front) {
        if (look.has(Extra.CAP)) {
            g.setPaint(new GradientPaint((float) -w, -12, lighter(look.accent(), 0.1), (float) w, -4, darker(look.accent(), 0.25)));
            Path2D cap = new Path2D.Double();
            cap.moveTo(-w - 0.8, -4.6);
            cap.curveTo(-w - 1, -14.5, w + 1, -14.5, w + 0.8, -4.6);
            cap.closePath();
            g.fill(cap);
            if (front) {
                Path2D peak = new Path2D.Double();                                         // seen from below, tipped down
                peak.moveTo(-w - 1.6, -5.2);
                peak.curveTo(-w, -6.2, w, -6.2, w + 1.6, -5.2);
                peak.curveTo(w + 0.6, -2, -w - 0.6, -2, -w - 1.6, -5.2);
                g.setColor(darker(look.accent(), 0.25));
                g.fill(peak);
            } else {
                g.setColor(c(0xd8d0c0));
                g.fill(new RoundRectangle2D.Double(-2.6, -6.6, 5.2, 2, 1, 1));            // the strap at the back
            }
        }
        if (look.has(Extra.HELMET)) {
            g.setPaint(new GradientPaint((float) -w, -14, c(0xc0c4cc), (float) w, -4, c(0x5a5e66)));
            g.fill(new Arc2D.Double(-w - 1.8, -15.5, w * 2 + 3.6, 18, 0, 180, Arc2D.CHORD));
            g.fill(new Ellipse2D.Double(-w - 6, -8, w * 2 + 12, 4));
            g.setColor(c(0xe8ecf0, 120));
            g.draw(new Arc2D.Double(-w, -14, w * 2, 14, 100, 60, Arc2D.OPEN));
        }
    }

    /** What a person holds in the near hand: a spear, a selfie stick, an umbrella, a torch. */
    private static void held(Graphics2D g, Look look, Point2D hand, double angle, boolean side) {
        double x = hand.getX(), y = hand.getY();
        if (look.has(Extra.SPEAR)) {
            g.setColor(c(0x6a4a2a));
            g.setStroke(Brush.line(1.6));
            g.draw(new Line2D.Double(x + 1, y + 52, x - 1, y - 62));
            g.setColor(c(0xb0b4bc));
            g.fill(poly(x - 1, y - 62, x - 3, y - 64, x - 1, y - 74, x + 1, y - 64));
        }
        if (look.has(Extra.SELFIE_STICK)) {
            double ex = x + (side ? 18 : 10), ey = y - 26;
            g.setColor(c(0x22252a));
            g.setStroke(Brush.line(1.2));
            g.draw(new Line2D.Double(x, y, ex, ey));
            g.setColor(c(0x101214));
            g.fill(new RoundRectangle2D.Double(ex - 3, ey - 5, 6, 9, 1.5, 1.5));
            g.setColor(c(0xff3a3a));
            g.fill(new Ellipse2D.Double(ex - 0.8, ey - 4, 1.6, 1.6));
        }
        if (look.has(Extra.UMBRELLA)) {
            // furled and held up high, so the group can follow it through the crowd
            g.setColor(c(0x3a2a20));
            g.setStroke(Brush.line(1.1));
            g.draw(new Arc2D.Double(x - 1.6, y + 1, 3.2, 4, 180, 180, Arc2D.OPEN));
            g.draw(new Line2D.Double(x + 1.6, y + 3, x + 1.6, y - 40));
            Path2D canopy = curve(x + 1.6, y - 40, x + 3.6, y - 30, x + 4.4, y - 20, x + 3.2, y - 13,
                    x + 1.6, y - 12, x, y - 13, x - 1.2, y - 20, x - 0.4, y - 30);
            g.setColor(look.accent());
            g.fill(canopy);
            g.setColor(look.accent().darker());
            g.setStroke(Brush.line(0.5));
            g.draw(canopy);
            g.draw(new Line2D.Double(x + 1.6, y - 38, x + 2.6, y - 14));
            g.draw(new Line2D.Double(x + 1.6, y - 38, x + 0.6, y - 14));
            g.fill(new Ellipse2D.Double(x + 0.4, y - 18, 2.4, 1.8));
            g.setColor(c(0x8a8e94));
            g.setStroke(Brush.line(0.8));
            g.draw(new Line2D.Double(x + 1.6, y - 40, x + 1.6, y - 44));
        }
        if (look.has(Extra.LANTERN)) {
            // a shuttered lantern of horn and iron, hanging from the hand by its ring
            g.setColor(c(0x2a2420));
            g.setStroke(Brush.line(0.7));
            g.draw(new Arc2D.Double(x - 2, y - 1, 4, 4, 0, 180, Arc2D.OPEN));
            g.fill(poly(x - 4.4, y + 3, x + 4.4, y + 3, x + 3.4, y + 1.4, x - 3.4, y + 1.4));
            g.setColor(c(0xf0c060));
            g.fill(new RoundRectangle2D.Double(x - 3.6, y + 3, 7.2, 8, 1.5, 1.5));
            g.setColor(c(0xfff0b0));
            g.fill(new Ellipse2D.Double(x - 1.2, y + 4.8, 2.4, 3.6));
            g.setColor(c(0x2a2420));
            g.draw(new Line2D.Double(x, y + 3, x, y + 11));
            g.fill(new Rectangle2D.Double(x - 4.2, y + 11, 8.4, 1.6));
        }
        if (look.has(Extra.TORCH)) {
            g.setColor(c(0x5a3a1a));
            g.setStroke(Brush.line(2));
            g.draw(new Line2D.Double(x, y + 4, x + 2, y - 12));
            g.setColor(c(0xffb040));
            g.fill(Brush.blob(x + 2, y - 22, x + 5, y - 13, x + 2, y - 11, x - 1, y - 13));
            g.setColor(c(0xfff0a0));
            g.fill(new Ellipse2D.Double(x, y - 16, 3, 4));
        }
    }

    private static void handbag(Graphics2D g, Point2D shoulder, Pose p, Look look) {
        double x = shoulder.getX() + 2, y = shoulder.getY();
        g.setColor(c(0x5a1a1a));
        g.setStroke(Brush.line(0.6));
        g.draw(new Line2D.Double(x, y, x + 3, y + 24));
        g.draw(new Line2D.Double(x, y, x - 4, y + 24));
        g.fill(new RoundRectangle2D.Double(x - 6, y + 23, 11, 9, 2.5, 2.5));
    }

    /** The space a figure takes, relative to its soles, at height 118: for sizing the sprite images. */
    public static Rectangle2D bounds() {
        return new Rectangle2D.Double(-46, -128, 92, 134);
    }
}
