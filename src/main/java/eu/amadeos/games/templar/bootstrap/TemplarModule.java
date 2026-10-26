package eu.amadeos.games.templar.bootstrap;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import eu.amadeos.commons.app.AppInfo;
import eu.amadeos.commons.config.XdgDirectories;
import eu.amadeos.commons.i18n.Messages;
import eu.amadeos.gameengine.audio.Mixer;
import eu.amadeos.gameengine.input.Keyboard;
import eu.amadeos.gameengine.input.Mouse;
import eu.amadeos.gameengine.save.SaveFile;
import eu.amadeos.games.templar.repository.TemplarSettings;
import eu.amadeos.games.templar.ui.game.Arena;
import eu.amadeos.games.templar.ui.game.TemplarGame;
import eu.amadeos.games.templar.ui.main.TemplarPresenter;
import eu.amadeos.games.templar.ui.main.TemplarView;
import eu.amadeos.games.templar.ui.main.TemplarWindow;

import java.util.Locale;

/**
 * amade-templar-game bindings. Theme, language and the XDG directories come from
 * {@link eu.amadeos.commons.app.CommonsModule}.
 */
public final class TemplarModule extends AbstractModule {
    /** The texts, {@code messages.properties} and its translations: the menus and every line of the story. */
    public static final String MESSAGES = "eu.amadeos.games.templar.messages";

    @Override
    protected void configure() {
        bind(TemplarSettings.class).in(Scopes.SINGLETON);
        bind(Keyboard.class).in(Scopes.SINGLETON);
        bind(Mouse.class).in(Scopes.SINGLETON);
        bind(Mixer.class).in(Scopes.SINGLETON);
        bind(TemplarGame.class).in(Scopes.SINGLETON);
        bind(Arena.class).to(TemplarGame.class);
        bind(TemplarWindow.class).in(Scopes.SINGLETON);
        bind(TemplarView.class).to(TemplarWindow.class);
        bind(TemplarPresenter.class).in(Scopes.SINGLETON);
    }

    /** The game in progress, {@code ~/.local/share/amadeos/games/templar/game.save}. */
    @Provides
    @Singleton
    SaveFile saveFile(XdgDirectories xdg, AppInfo app) {
        return SaveFile.of(xdg, app.configName(), "game");
    }

    /** The texts in the language {@code AmadeApplication} chose. */
    @Provides
    @Singleton
    Messages messages(Locale locale) {
        return Messages.load(TemplarModule.class, MESSAGES, locale);
    }
}
