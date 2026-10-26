package eu.amadeos.games.templar.ui.main;

import com.google.inject.Inject;
import eu.amadeos.gameengine.swing.RenderMode;
import eu.amadeos.games.templar.repository.TemplarSettings;
import eu.amadeos.games.templar.ui.game.Arena;

/** Everything around the story, without Swing: the menu commands and the settings. All methods run on the Swing thread. */
public final class TemplarPresenter {
    private final TemplarView view;
    private final Arena arena;
    private final TemplarSettings settings;

    @Inject
    public TemplarPresenter(TemplarView view, Arena arena, TemplarSettings settings) {
        this.view = view;
        this.arena = arena;
        this.settings = settings;
    }

    public void start() {
        boolean sound = settings.sound();
        RenderMode mode = settings.renderMode();
        view.showOptions(sound, mode);
        view.useRenderMode(mode);
        arena.setSound(sound);
        arena.showTitle();
    }

    public void newGame() {
        arena.newGame();
    }

    public void continueGame() {
        arena.continueGame();
    }

    public void saveGame() {
        arena.saveGame();
    }

    public void titleScreen() {
        arena.showTitle();
    }

    public void setSound(boolean on) {
        settings.setSound(on);
        arena.setSound(on);
    }

    public void setRenderMode(RenderMode mode) {
        settings.setRenderMode(mode);
        view.useRenderMode(mode);
    }

    public void showControls() {
        view.showControls();
    }

    public void showAbout() {
        view.showAbout();
    }
}
