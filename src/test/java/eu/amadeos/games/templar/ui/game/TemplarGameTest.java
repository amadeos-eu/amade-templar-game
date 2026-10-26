package eu.amadeos.games.templar.ui.game;

import eu.amadeos.commons.i18n.Messages;
import eu.amadeos.gameengine.audio.Mixer;
import eu.amadeos.gameengine.input.Keyboard;
import eu.amadeos.gameengine.input.Mouse;
import eu.amadeos.gameengine.save.SaveFile;
import eu.amadeos.games.templar.bootstrap.TemplarModule;
import eu.amadeos.games.templar.domain.Adventure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The game without a window and without the loop thread: the test calls the steps itself, in order. */
class TemplarGameTest {
    static final double STEP = 1.0 / TemplarGame.UPDATES_PER_SECOND;

    @TempDir
    Path dir;

    private final long[] now = {0};
    private final Keyboard keyboard = new Keyboard(() -> now[0]);
    private final Mouse mouse = new Mouse();
    private TemplarGame game;

    @BeforeEach
    void create() {
        Messages msg = Messages.load(TemplarModule.class, TemplarModule.MESSAGES, Locale.ENGLISH);
        Mixer mixer = new Mixer();
        mixer.setMuted(true);
        game = new TemplarGame(keyboard, mouse, msg, mixer, new SaveFile(dir.resolve("game.save")));
    }

    private void run(double seconds) {
        for (int i = 0; i < seconds / STEP; i++) {
            now[0] += (long) (STEP * 1e9);
            game.update(STEP);
        }
    }

    private Adventure adventure() {
        return assertInstanceOf(TemplarGame.PlayScene.class, game.top()).adventure();
    }

    /** Clicks lines and titles away until the game waits for the player. */
    private void settle() {
        for (int i = 0; i < 6000 && !adventure().isIdle(); i++) {
            adventure().skip();
            run(STEP);
        }
    }

    private void tap(int key) {
        keyboard.pressed(key, now[0]);
        keyboard.released(key, now[0] + 1_000_000);
        run(STEP);
    }

    @Test
    void theGameStartsOnTheTitleAndANewGameIn1307() {
        assertInstanceOf(TemplarGame.TitleScene.class, game.top());
        assertFalse(game.hasSavedGame());
        game.newGame();
        assertEquals("chapter", adventure().room().id());
        assertEquals("aimery", adventure().hero().id());
    }

    @Test
    void aSavedGameContinuesWhereItWasSaved() {
        game.newGame();
        settle();
        adventure().inventory().gain("cheese");
        adventure().flags().set("sealed");
        tap(KeyEvent.VK_F5);
        assertTrue(game.hasSavedGame());
        game.newGame();
        assertFalse(adventure().inventory().has("cheese"));
        game.continueGame();
        assertTrue(adventure().inventory().has("cheese"));
        assertTrue(adventure().flags().is("sealed"));
        assertEquals("chapter", adventure().room().id());
    }

    @Test
    void continuingWithoutASavedGameStaysOnTheTitle() {
        game.continueGame();
        assertInstanceOf(TemplarGame.TitleScene.class, game.top());
    }

    @Test
    void theEndOfTheActShowsTheEndScene() {
        game.newGame();
        settle();
        adventure().flags().set("finished");
        run(STEP);
        assertInstanceOf(TemplarGame.EndScene.class, game.top());
        assertTrue(game.hasSavedGame(), "the end is saved, too");
    }

    @Test
    void theNotebookOpensWithN() {
        game.newGame();
        settle();
        tap(KeyEvent.VK_N);
        BufferedImage image = new BufferedImage(640, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        game.render(g, 1);
        game.renderOverlay(g, 1);
        g.dispose();
        tap(KeyEvent.VK_ESCAPE);
    }

    @Test
    void theTitleIsDrawn() {
        run(1);
        BufferedImage image = new BufferedImage(640, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        game.render(g, 1);
        game.renderOverlay(g, 1);
        g.dispose();
        assertTrue((image.getRGB(320, 200) & 0xffffff) != 0, "something is painted in the middle");
    }
}
