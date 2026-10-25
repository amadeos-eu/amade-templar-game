package eu.amadeos.games.templar.ui.game;

/** What the window asks of the game; each call is carried out on the game loop thread. */
public interface Arena {
    void showTitle();

    void newGame();

    /** Plays on from the saved game, if there is one. */
    void continueGame();

    boolean hasSavedGame();

    /** Saves now, if nothing is playing; the answer shows on the screen. */
    void saveGame();

    void setSound(boolean on);
}
