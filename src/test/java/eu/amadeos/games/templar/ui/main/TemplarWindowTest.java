package eu.amadeos.games.templar.ui.main;

import eu.amadeos.commons.i18n.Messages;
import eu.amadeos.gameengine.audio.Mixer;
import eu.amadeos.gameengine.input.Keyboard;
import eu.amadeos.gameengine.input.Mouse;
import eu.amadeos.gameengine.save.SaveFile;
import eu.amadeos.gameengine.swing.RenderMode;
import eu.amadeos.games.templar.bootstrap.TemplarModule;
import eu.amadeos.games.templar.repository.TemplarSettings;
import eu.amadeos.games.templar.ui.game.TemplarGame;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.JMenuItem;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** The real window, game and loop: the menu reaches the canvas and the settings. */
class TemplarWindowTest {
    @TempDir
    Path dir;

    private TemplarWindow window;
    private TemplarGame game;
    private TemplarSettings settings;

    @BeforeEach
    void open() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "needs a display");
        Messages msg = Messages.load(TemplarModule.class, TemplarModule.MESSAGES, Locale.ENGLISH);
        settings = new TemplarSettings(dir.resolve("templar.properties"));
        Keyboard keyboard = new Keyboard();
        Mouse mouse = new Mouse();
        Mixer mixer = new Mixer();
        onEdt(() -> {
            game = new TemplarGame(keyboard, mouse, msg, mixer, new SaveFile(dir.resolve("game.save")));
            window = new TemplarWindow(msg, game, keyboard, mouse);
            TemplarPresenter presenter = new TemplarPresenter(window, game, settings);
            window.bind(presenter);
            presenter.start();
            window.setVisible(true);
            game.start(window.canvas());
        });
    }

    @AfterEach
    void close() throws Exception {
        if (game != null) game.stop();
        if (window != null) onEdt(window::dispose);
    }

    private static void onEdt(Runnable code) throws Exception {
        try {
            SwingUtilities.invokeAndWait(code);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Error error) throw error;
            throw (Exception) e.getCause();
        }
    }

    private JMenuItem item(String name) {
        return find(window.getJMenuBar(), name);
    }

    private static JMenuItem find(MenuElement element, String name) {
        for (MenuElement child : element.getSubElements()) {
            if (child instanceof JMenuItem item && name.equals(item.getName())) return item;
            JMenuItem found = find(child, name);
            if (found != null) return found;
        }
        return null;
    }

    @Test
    void theGraphicsMenuReachesTheCanvas() throws Exception {
        onEdt(() -> {
            assertEquals(RenderMode.DIRECT, window.canvas().renderMode());
            JMenuItem pixel = item("game-graphics-pixel");
            assertNotNull(pixel);
            pixel.doClick(0);
            assertEquals(RenderMode.PIXEL, window.canvas().renderMode());
        });
        assertEquals(RenderMode.PIXEL, settings.renderMode());
    }

    @Test
    void theSoundMenuKeepsTheSetting() throws Exception {
        onEdt(() -> {
            JMenuItem sound = item("game-sound");
            assertTrue(sound.isSelected());
            sound.doClick(0);
        });
        assertFalse(settings.sound());
    }

    @Test
    void theMenuHasTheSavedGameCommands() throws Exception {
        onEdt(() -> {
            assertNotNull(item("game-continue"));
            assertNotNull(item("game-save"));
            assertNotNull(item("game-title"));
            assertNotNull(item("help-controls"));
        });
    }
}
