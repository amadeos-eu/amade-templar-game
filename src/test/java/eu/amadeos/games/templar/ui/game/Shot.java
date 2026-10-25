package eu.amadeos.games.templar.ui.game;

import eu.amadeos.commons.i18n.Messages;
import eu.amadeos.gameengine.audio.Mixer;
import eu.amadeos.gameengine.input.Keyboard;
import eu.amadeos.gameengine.input.Mouse;
import eu.amadeos.gameengine.math.Vector;
import eu.amadeos.gameengine.save.SaveFile;
import eu.amadeos.games.templar.bootstrap.TemplarModule;
import eu.amadeos.games.templar.domain.Adventure;
import eu.amadeos.games.templar.domain.Story;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.Locale;

/**
 * For designing: {@code java ... Shot out.png [seconds] [x y]...} starts a new game, plays it that long, moving
 * the pointer to x y and clicking there, and saves a picture.
 */
public final class Shot {
    final Mouse mouse = new Mouse();
    final TemplarGame game;

    Shot(Locale locale) throws Exception {
        Messages msg = Messages.load(TemplarModule.class, TemplarModule.MESSAGES, locale);
        Mixer mixer = new Mixer();
        mixer.setMuted(true);
        game = new TemplarGame(new Keyboard(() -> 0), mouse, msg, mixer,
                new SaveFile(Files.createTempDirectory("templar").resolve("game.save")));
    }

    TemplarGame.PlayScene start(Adventure adventure) {
        return game.play(adventure);
    }

    void run(double seconds) {
        for (int i = 0; i < seconds * TemplarGame.UPDATES_PER_SECOND; i++) game.update(1.0 / TemplarGame.UPDATES_PER_SECOND);
    }

    void point(double x, double y) {
        mouse.moved(x, y);
    }

    void click(int button, double x, double y) {
        mouse.moved(x, y);
        mouse.pressed(button);
        mouse.released(button);
        run(1.0 / 30);
    }

    void save(File file) throws Exception {
        BufferedImage image = new BufferedImage(1280, 800, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.scale(2, 2);
        game.render(g, 1);
        game.renderOverlay(g, 1);
        g.dispose();
        ImageIO.write(image, "png", file);
    }

    public static void main(String[] args) throws Exception {
        Shot shot = new Shot(Locale.ENGLISH);
        Messages msg = Messages.load(TemplarModule.class, TemplarModule.MESSAGES, Locale.ENGLISH);
        Adventure adventure = Adventure.begin(Story.load(), msg::get);
        shot.start(adventure);
        double seconds = args.length > 1 ? Double.parseDouble(args[1]) : 1;
        String room = System.getProperty("templar.room");        // e.g. reading/workshop-door
        if (room != null) {
            shot.run(1);
            while (adventure.skip()) shot.run(0.1);
            adventure.enter(room.split("/")[0], room.split("/")[1]);
        }
        shot.run(seconds);
        for (int i = 2; i + 2 < args.length + 1 && i + 1 < args.length; i += 3) {
            int button = Integer.parseInt(args[i]);
            shot.click(button, Double.parseDouble(args[i + 1]), Double.parseDouble(args[i + 2]));
            shot.run(1);
        }
        shot.save(new File(args[0]));
    }

    static Vector v(double x, double y) {
        return new Vector(x, y);
    }
}
