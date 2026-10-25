package eu.amadeos.games.templar.bootstrap;

import eu.amadeos.commons.app.AmadeApplication;
import eu.amadeos.commons.app.AppInfo;
import eu.amadeos.gameengine.audio.Mixer;
import eu.amadeos.games.templar.ui.game.TemplarGame;
import eu.amadeos.games.templar.ui.main.TemplarPresenter;
import eu.amadeos.games.templar.ui.main.TemplarWindow;

public final class AmadeTemplarApplication {
    public static final AppInfo APP = new AppInfo("amade-templar-game", "The Seal of the Templars", "templar");

    private AmadeTemplarApplication() {
    }

    /** The version from the jar's manifest; "dev" when run from the IDE. */
    public static String version() {
        String version = AmadeTemplarApplication.class.getPackage().getImplementationVersion();
        return version == null ? "dev" : version;
    }

    // --8<-- [start:main]
    public static void main(String[] args) {
        AmadeApplication.launch(APP, injector -> {
            Mixer mixer = injector.getInstance(Mixer.class);
            mixer.start();
            TemplarWindow window = injector.getInstance(TemplarWindow.class);
            TemplarPresenter presenter = injector.getInstance(TemplarPresenter.class);
            TemplarGame game = injector.getInstance(TemplarGame.class);
            window.bind(presenter);
            presenter.start();
            window.whenClosed(() -> {
                game.stop();
                mixer.close();
                System.exit(0);
            });
            window.setLocationByPlatform(true);
            window.setVisible(true);
            game.start(window.canvas());
        }, new TemplarModule());
    }
    // --8<-- [end:main]
}
