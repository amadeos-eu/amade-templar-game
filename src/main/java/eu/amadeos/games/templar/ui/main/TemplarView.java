package eu.amadeos.games.templar.ui.main;

import eu.amadeos.gameengine.swing.RenderMode;

/** What {@link TemplarPresenter} needs from the window; tests replace it. The story itself is drawn by the game. */
public interface TemplarView {
    /** Shows the settings in the menu. */
    void showOptions(boolean sound, RenderMode mode);

    /** Switches the canvas to a render mode. */
    void useRenderMode(RenderMode mode);

    /** The dialog with the mouse buttons and the keys. */
    void showControls();

    void showAbout();
}
