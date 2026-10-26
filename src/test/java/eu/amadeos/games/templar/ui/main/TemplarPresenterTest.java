package eu.amadeos.games.templar.ui.main;

import eu.amadeos.gameengine.swing.RenderMode;
import eu.amadeos.games.templar.repository.TemplarSettings;
import eu.amadeos.games.templar.ui.game.Arena;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TemplarPresenterTest {
    @TempDir
    Path dir;

    private final List<String> log = new ArrayList<>();
    private TemplarPresenter presenter;
    private TemplarSettings settings;

    private final TemplarView view = new TemplarView() {
        @Override
        public void showOptions(boolean sound, RenderMode mode) {
            log.add("options " + sound + " " + mode);
        }

        @Override
        public void useRenderMode(RenderMode mode) {
            log.add("mode " + mode);
        }

        @Override
        public void showControls() {
            log.add("controls");
        }

        @Override
        public void showAbout() {
            log.add("about");
        }
    };

    private final Arena arena = new Arena() {
        @Override
        public void showTitle() {
            log.add("title");
        }

        @Override
        public void newGame() {
            log.add("new");
        }

        @Override
        public void continueGame() {
            log.add("continue");
        }

        @Override
        public boolean hasSavedGame() {
            return false;
        }

        @Override
        public void saveGame() {
            log.add("save");
        }

        @Override
        public void setSound(boolean on) {
            log.add("sound " + on);
        }
    };

    @BeforeEach
    void create() {
        settings = new TemplarSettings(dir.resolve("templar.properties"));
        presenter = new TemplarPresenter(view, arena, settings);
        presenter.start();
    }

    @Test
    void theSettingsAreAppliedAtStart() {
        assertEquals(List.of("options true DIRECT", "mode DIRECT", "sound true", "title"), log);
    }

    @Test
    void soundAndGraphicsAreKept() {
        log.clear();
        presenter.setRenderMode(RenderMode.PIXEL);
        presenter.setSound(false);
        assertEquals(List.of("mode PIXEL", "sound false"), log);
        assertEquals(RenderMode.PIXEL, settings.renderMode());
        assertFalse(settings.sound());
    }

    @Test
    void theMenuCommandsReachTheGame() {
        log.clear();
        presenter.newGame();
        presenter.saveGame();
        presenter.continueGame();
        presenter.titleScreen();
        presenter.showControls();
        presenter.showAbout();
        assertEquals(List.of("new", "save", "continue", "title", "controls", "about"), log);
    }
}
