package eu.amadeos.games.templar.ui.game;

import eu.amadeos.gameengine.adventure.Hotspot;
import eu.amadeos.gameengine.adventure.Interaction;
import eu.amadeos.gameengine.adventure.Room;
import eu.amadeos.gameengine.math.Vector;
import eu.amadeos.games.templar.domain.Adventure;
import eu.amadeos.games.templar.ui.art.Cast;
import eu.amadeos.games.templar.ui.art.Figure;
import eu.amadeos.games.templar.ui.art.Ink;
import eu.amadeos.games.templar.ui.art.Look;
import eu.amadeos.games.templar.ui.art.Pose;
import eu.amadeos.games.templar.ui.art.rooms.Rooms;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * The screenshots of chapter 50 and of the README, drawn without a screen: the title, every room with its
 * people, a dialog, the notebook, what the game knows about a room, and the cast. {@code ./gradlew
 * screenshots -Pshots=<folder>}.
 */
public final class Screenshots {
    private Screenshots() {
    }

    public static void main(String[] args) throws Exception {
        File dir = new File(args.length > 0 ? args[0] : "screenshots");
        dir.mkdirs();

        Shot title = new Shot(Locale.ENGLISH);
        title.run(4);
        title.save(new File(dir, "title.png"));

        // The prologue: Brother Gérard has just handed over the page; Aimery asks him why.
        Shot chapter = room("chapter", "start");
        chapter.click(Interaction.LEFT, 470, 290);
        chapter.run(3);
        chapter.save(new File(dir, "chapter.png"));

        room("courtyard", "chapter", 1, 360, 330).save(new File(dir, "courtyard.png"));
        room("library", "door", 1, 430, 330).save(new File(dir, "library.png"));

        // The workshop: Lena behind the workbench, the README's picture.
        room("workshop", "start", 1, 330, 304).save(new File(dir, "amade-templar-game.png"));

        // The reading room with Lefort's dialog open.
        Shot reading = room("reading", "workshop-door", 1, 330, 330);
        reading.adventure.inventory().gain("page");
        reading.click(Interaction.LEFT, 522, 270);
        reading.run(4);
        reading.save(new File(dir, "reading.png"));

        room("square", "arrival", 1, 250, 340).save(new File(dir, "square.png"));
        room("bridge", "arrival", 1, 380, 350).save(new File(dir, "bridge.png"));

        // The first flashback: Aimery in the prison cart, singing the message to Raoul in Latin.
        Shot cart = room("cart", "start");
        settle(cart);
        cart.adventure.flags().set("may-sing");
        cart.click(Interaction.LEFT, 546, 300);
        cart.run(2);
        while (cart.adventure.skip()) cart.run(0.1);
        cart.run(0.5);
        cart.save(new File(dir, "cart.png"));

        room("harbour", "arrival", 1, 420, 340).save(new File(dir, "harbour.png"));
        room("court", "arrival", 1, 300, 330).save(new File(dir, "court.png"));
        room("quay", "start", 1, 200, 340).save(new File(dir, "quay.png"));

        // Act 3, Tomar: Dona Inês in the Charola, the archive with the pigeon on the sill, the chest in the cloister.
        Shot charola = room("charola", "arrival");
        settle(charola);
        charola.adventure.flags().set("may-ask");
        charola.click(Interaction.LEFT, 604, 300);
        charola.run(4);
        while (charola.adventure.skip()) charola.run(0.1);
        charola.run(0.5);
        charola.save(new File(dir, "charola.png"));

        Shot archive = room("archive", "arrival", 1, 360, 340);
        for (String hotspot : new String[]{"tower-window-open", "pigeon-archive", "lift-box"}) archive.adventure.show(hotspot, true);
        archive.adventure.show("tower-window", false);
        archive.run(0.5);
        archive.save(new File(dir, "archive.png"));

        room("cloister", "arrival", 1, 240, 360).save(new File(dir, "cloister.png"));

        // The epilogue, at its page about Portugal.
        Shot end = room("cloister", "arrival");
        settle(end);
        end.adventure.flags().set("finished");
        end.run(2);
        for (int i = 0; i < 4; i++) {
            end.click(Interaction.LEFT, 320, 200);
            end.run(1.5);
        }
        end.save(new File(dir, "epilogue.png"));

        // The notebook over the workshop.
        Shot notebook = room("workshop", "start", 1, 330, 304);
        notebook.key(KeyEvent.VK_N);
        notebook.run(0.5);
        notebook.key(KeyEvent.VK_N);
        notebook.run(0.5);
        notebook.save(new File(dir, "notebook.png"));

        walkArea(new File(dir, "walkarea.png"));
        cast(new File(dir, "cast.png"));
    }

    /** Plays a scene's opening to its end, clicking away the lines, so the player may act. */
    static void settle(Shot shot) {
        for (int i = 0; i < 6; i++) {
            shot.run(1);
            while (shot.adventure.skip()) shot.run(0.1);
        }
    }

    /** A new game, moved to a room, with the pointer clicked at (x, y) {@code clicks} times, a second apart. */
    static Shot room(String room, String spot, double... clicks) throws Exception {
        Shot shot = new Shot(Locale.ENGLISH);
        Adventure adventure = shot.start(Adventure.begin(eu.amadeos.games.templar.domain.Story.load(), shot.msg::get)).adventure();
        shot.run(1);
        while (adventure.skip()) shot.run(0.1);
        adventure.enter(room, spot);
        shot.run(2);
        while (adventure.skip()) shot.run(0.1);
        for (int i = 0; i + 2 < clicks.length + 1 && i + 2 <= clicks.length; i += 3) {
            for (int n = 0; n < 4; n++) {
                shot.click((int) clicks[i], clicks[i + 1], clicks[i + 2]);
                shot.run(1);
            }
        }
        while (adventure.skip()) shot.run(0.1);
        shot.run(0.5);
        return shot;
    }

    /** The workshop as the game sees it: the floor, its holes, the hotspots, the spots and the depth scale. */
    static void walkArea(File file) throws Exception {
        Room room = eu.amadeos.games.templar.domain.Story.load().room("workshop");
        BufferedImage image = new BufferedImage(1280, 800, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.scale(2, 2);
        Rooms.art("workshop").render().drawBackground(g);
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRect(0, 0, 640, 400);
        g.setColor(new Color(90, 220, 120, 90));
        g.fill(path(room.floor().outline()));
        g.setColor(new Color(220, 60, 60, 140));
        for (List<Vector> hole : room.floor().holes()) g.fill(path(hole));
        g.setStroke(new BasicStroke(1f));
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 7));
        for (Hotspot h : room.hotspots()) {
            g.setColor(h.isExit() ? new Color(120, 180, 255) : new Color(255, 220, 120));
            g.draw(path(h.area()));
            g.drawString(h.id(), (float) h.area().getFirst().x() + 2, (float) h.area().getFirst().y() + 8);
            g.fill(new Ellipse2D.Double(h.stand().x() - 1.5, h.stand().y() - 1.5, 3, 3));
        }
        g.setColor(Color.WHITE);
        for (double y = 270; y <= 400; y += 26) {
            double s = room.depth().at(y);
            g.drawString(String.format(Locale.ROOT, "%.2f", s), 4, (float) y);
        }
        g.dispose();
        ImageIO.write(image, "png", file);
    }

    static Path2D path(List<Vector> points) {
        Path2D p = new Path2D.Double();
        p.moveTo(points.getFirst().x(), points.getFirst().y());
        for (Vector v : points.subList(1, points.size())) p.lineTo(v.x(), v.y());
        p.closePath();
        return p;
    }

    /** The people of the prologue and Act 1, from the side, the front and the back. */
    static void cast(File file) throws Exception {
        Look[] looks = {Cast.AIMERY, Cast.GERARD, Cast.GUARD, Cast.SOLDIER, Cast.LENA, Cast.LENA_OUT, Cast.LEFORT,
                Cast.KEV, Cast.HELENE, Cast.DRIVER, Cast.GUIDE, Cast.TOURIST_A, Cast.TOURIST_B, Cast.RAOUL,
                Cast.ETIENNE, Cast.CAPTAIN, Cast.FISHERMAN, Cast.MORIN, Cast.INES};
        double scale = 2;
        int cw = 70, ch = 150, cols = looks.length;
        BufferedImage image = new BufferedImage((int) (cols * cw * scale), (int) (ch * scale), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bg = image.createGraphics();
        bg.setColor(new Color(0xe8dcc4));
        bg.fillRect(0, 0, image.getWidth(), image.getHeight());
        bg.dispose();
        for (int i = 0; i < looks.length; i++) {
            final int col = i;
            Ink.layer(image, scale, l -> {
                l.translate(col * cw + cw / 2.0, ch - 10);
                Figure.draw(l, looks[col], Figure.View.SIDE, Pose.STAND);
            });
        }
        ImageIO.write(image, "png", file);
    }
}
