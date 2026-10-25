import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Draws the program icon of The Seal of the Templars: a red wax seal with two knights on one horse, pressed on
 * a folded page of parchment. Drawn as shapes on a 48 × 48 grid, so it is sharp at every size.
 *
 * <pre>
 * java tools/MakeIcon.java 48 src/main/resources/icons/amade-templar-game.png
 * java tools/MakeIcon.java 256 build/icon-256.png
 * python3 -c "from PIL import Image; Image.open('build/icon-256.png').save('packaging/windows/amade-templar-game.ico',
 *     sizes=[(16,16),(24,24),(32,32),(48,48),(64,64),(128,128),(256,256)])"
 * </pre>
 *
 * Run it in the repository's folder. The files are part of the repository; run this again only to change
 * the icon.
 */
public class MakeIcon {
    public static void main(String[] args) throws IOException {
        int size = Integer.parseInt(args[0]);
        File file = new File(args[1]);
        if (file.getParentFile() != null) file.getParentFile().mkdirs();
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.scale(size / 48.0, size / 48.0);
        paint(g);
        g.dispose();
        ImageIO.write(image, "png", file);
    }

    static void paint(Graphics2D g) {
        // The parchment, a rounded square with a fold and a few lines of writing.
        RoundRectangle2D page = new RoundRectangle2D.Double(2, 2, 44, 44, 8, 8);
        g.setPaint(new GradientPaint(0, 2, new Color(0xF2E4BE), 48, 46, new Color(0xC8A86A)));
        g.fill(page);
        g.setColor(new Color(0x7A5A2A));
        g.setStroke(new BasicStroke(0.9f));
        g.draw(page);
        g.setColor(new Color(0x8A6A3A, false));
        g.setStroke(new BasicStroke(0.7f));
        g.draw(new Line2D.Double(3, 16, 45, 16));
        g.setColor(new Color(0x5A4630));
        for (int i = 0; i < 3; i++) g.fill(new Rectangle2D.Double(7, 6 + i * 3, 30 - i * 6, 1));
        // The wax: an uneven red blob with a lighter rim.
        Path2D blob = new Path2D.Double();
        for (int i = 0; i <= 48; i++) {
            double a = i * Math.PI * 2 / 48, r = 15.5 + 0.9 * Math.sin(a * 5) + 0.5 * Math.sin(a * 9 + 1);
            double x = 25 + Math.cos(a) * r, y = 29 + Math.sin(a) * r;
            if (i == 0) blob.moveTo(x, y);
            else blob.lineTo(x, y);
        }
        blob.closePath();
        g.setPaint(new RadialGradientPaint(new Point2D.Double(21, 24), 20, new float[]{0f, 1f},
                new Color[]{new Color(0xD8402E), new Color(0x7A1A12)}));
        g.fill(blob);
        g.setColor(new Color(0x5A120C));
        g.setStroke(new BasicStroke(0.8f));
        g.draw(blob);
        // The seal pressed into it: a ring, and two knights riding one horse.
        g.setColor(new Color(0x6A1610));
        g.setStroke(new BasicStroke(1.1f));
        g.draw(new Ellipse2D.Double(14, 18, 22, 22));
        g.translate(24.5, 30);
        g.scale(0.26, 0.26);
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
        for (double rx : new double[]{-12, 4}) {
            g.fill(poly(rx - 4, -2, rx + 4, -2, rx + 3, -18, rx - 3, -18));
            g.fill(new Ellipse2D.Double(rx - 3.5, -26, 7, 8));
            g.fill(poly(rx + 2, -14, rx + 9, -14, rx + 8, -4, rx + 5, -1, rx + 2, -4));
            g.setStroke(new BasicStroke(3f));
            g.draw(new Line2D.Double(rx + 2, -10, rx + 22, -30));
        }
        // a highlight on the wax
        g.scale(1 / 0.26, 1 / 0.26);
        g.setColor(new Color(255, 220, 200, 90));
        g.fill(new Ellipse2D.Double(-10, -12, 6, 3));
    }

    static Path2D poly(double... xy) {
        Path2D p = new Path2D.Double();
        p.moveTo(xy[0], xy[1]);
        for (int i = 2; i < xy.length; i += 2) p.lineTo(xy[i], xy[i + 1]);
        p.closePath();
        return p;
    }
}
