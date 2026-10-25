package eu.amadeos.games.templar.ui.main;

import com.google.inject.Inject;
import eu.amadeos.commons.i18n.Messages;
import eu.amadeos.commons.i18n.Mnemonics;
import eu.amadeos.commons.swing.EdtExecutor;
import eu.amadeos.gameengine.input.Keyboard;
import eu.amadeos.gameengine.input.Mouse;
import eu.amadeos.gameengine.swing.FullScreen;
import eu.amadeos.gameengine.swing.GameCanvas;
import eu.amadeos.gameengine.swing.GameMenus;
import eu.amadeos.gameengine.swing.GameTexts;
import eu.amadeos.gameengine.swing.RenderMode;
import eu.amadeos.games.templar.bootstrap.AmadeTemplarApplication;
import eu.amadeos.games.templar.ui.game.TemplarGame;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

/**
 * The game window: a menu and the canvas the game loop draws the rooms on. The window can be resized or
 * switched to full screen with F11; the picture scales with it. The system pointer is hidden over the canvas:
 * the game draws its own, which lights up over things.
 */
public final class TemplarWindow extends JFrame implements TemplarView {
    private final Messages msg;
    private final GameMenus menus = new GameMenus(GameTexts.load());
    private final GameCanvas canvas;
    private final FullScreen fullScreen;
    private final JCheckBoxMenuItem sound;
    private JMenu graphics;
    private TemplarPresenter presenter;

    @Inject
    public TemplarWindow(Messages msg, TemplarGame game, Keyboard keyboard, Mouse mouse) {
        super(msg.get("window.title"));
        EdtExecutor.requireEdt();
        this.msg = msg;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        java.net.URL icon = getClass().getResource("/icons/amade-templar-game.png");
        if (icon != null) setIconImage(new ImageIcon(icon).getImage());

        // --8<-- [start:canvas]
        // The canvas is a heavyweight AWT component: lightweight menus would open behind it.
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
        // Two layers: the room with its people, and the overlay with the texts, the inventory and the pointer.
        canvas = new GameCanvas(640, 400, game::render, game::renderOverlay);
        keyboard.attach(canvas);
        mouse.attach(canvas);
        canvas.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(
                new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "none"));
        // --8<-- [end:canvas]
        getContentPane().add(canvas, BorderLayout.CENTER);
        fullScreen = new FullScreen(this, canvas);
        sound = Mnemonics.apply(new JCheckBoxMenuItem(), msg.get("menu.game.sound"));
        sound.setName("game-sound");
        sound.addActionListener(e -> {
            presenter.setSound(sound.isSelected());
            canvas.requestFocusInWindow();
        });
        setJMenuBar(menuBar());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                canvas.requestFocusInWindow();
            }
        });
        canvas.setPreferredSize(new java.awt.Dimension(1280, 800));
        pack();
    }

    /** Connects the window with the presenter; call once before {@link TemplarPresenter#start()}. */
    public void bind(TemplarPresenter presenter) {
        this.presenter = presenter;
    }

    public GameCanvas canvas() {
        return canvas;
    }

    /** Runs the action when the window closes, but not when it only switches to or from full screen. */
    public void whenClosed(Runnable action) {
        fullScreen.whenClosed(action);
    }

    // --8<-- [start:menu]
    private JMenuBar menuBar() {
        JMenu game = menus.game();
        game.add(focusing(menus.newGame(() -> presenter.newGame())));
        game.add(focusing(item("menu.game.continue", "game-continue", () -> presenter.continueGame())));
        game.add(focusing(item("menu.game.save", "game-save", () -> presenter.saveGame())));
        game.add(focusing(item("menu.game.title", "game-title", () -> presenter.titleScreen())));
        game.addSeparator();
        game.add(sound);
        graphics = menus.graphics(RenderMode.DIRECT, mode -> {
            presenter.setRenderMode(mode);
            canvas.requestFocusInWindow();
        });
        game.add(graphics);
        game.add(focusing(menus.frameTimes(canvas::setShowFrameTimes)));
        game.add(focusing(menus.fullScreen(fullScreen::toggle)));
        game.addSeparator();
        game.add(menus.exit(this::dispose));
        JMenu help = menus.help();
        help.add(item("menu.help.controls", "help-controls", () -> presenter.showControls()));
        help.add(menus.about(msg.get("window.title"), () -> presenter.showAbout()));
        JMenuBar bar = new JMenuBar();
        bar.add(game);
        bar.add(help);
        return bar;
    }
    // --8<-- [end:menu]

    private JMenuItem item(String key, String name, Runnable action) {
        JMenuItem item = Mnemonics.apply(new JMenuItem(), msg.get(key));
        item.setName(name);
        item.addActionListener(e -> action.run());
        return item;
    }

    /** After a menu command the keys belong to the game again. */
    private <T extends JMenuItem> T focusing(T item) {
        item.addActionListener(e -> canvas.requestFocusInWindow());
        return item;
    }

    // ---- TemplarView ------------------------------------------------------------------------------

    @Override
    public void showOptions(boolean soundOn, RenderMode mode) {
        sound.setSelected(soundOn);
        for (int i = 0; i < graphics.getItemCount(); i++) {
            JMenuItem item = graphics.getItem(i);
            item.setSelected(("game-graphics-" + mode.id()).equals(item.getName()));
        }
    }

    @Override
    public void useRenderMode(RenderMode mode) {
        canvas.setRenderMode(mode);
    }

    @Override
    public void showControls() {
        JOptionPane.showMessageDialog(this, "<html><body style='width: 340px'>" + msg.get("dialog.controls.text")
                        + "</body></html>", Mnemonics.strip(msg.get("menu.help.controls")),
                JOptionPane.INFORMATION_MESSAGE);
        canvas.requestFocusInWindow();
    }

    @Override
    public void showAbout() {
        // Swing's HTML only wraps lines with a fixed body width.
        JOptionPane.showMessageDialog(this,
                "<html><body style='width: 340px'><center><h2>"
                        + msg.get("window.title") + " " + AmadeTemplarApplication.version() + "</h2>"
                        + "<p>" + msg.get("dialog.about.text") + "</p><br>"
                        + "<p>" + msg.get("dialog.about.history") + "</p><br>"
                        + "<small>amade-templar-game &bull; &copy; 2026 datazuul.com &bull; EUPL-1.2</small>"
                        + "</center></html>",
                Mnemonics.strip(menus.texts().format("menu.help.about", msg.get("window.title"))),
                JOptionPane.INFORMATION_MESSAGE);
        canvas.requestFocusInWindow();
    }
}
