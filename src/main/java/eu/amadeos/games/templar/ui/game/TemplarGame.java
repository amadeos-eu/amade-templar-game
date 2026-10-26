package eu.amadeos.games.templar.ui.game;

import com.google.inject.Inject;
import eu.amadeos.commons.i18n.Messages;
import eu.amadeos.gameengine.adventure.Hotspot;
import eu.amadeos.gameengine.adventure.Interaction;
import eu.amadeos.gameengine.audio.Mixer;
import eu.amadeos.gameengine.audio.Sound;
import eu.amadeos.gameengine.input.Keyboard;
import eu.amadeos.gameengine.input.Mouse;
import eu.amadeos.gameengine.loop.GameLoop;
import eu.amadeos.gameengine.math.Vector;
import eu.amadeos.gameengine.save.SaveFile;
import eu.amadeos.gameengine.scene.Scene;
import eu.amadeos.gameengine.scene.SceneStack;
import eu.amadeos.gameengine.swing.GameCanvas;
import eu.amadeos.games.templar.domain.Adventure;
import eu.amadeos.games.templar.domain.Story;
import eu.amadeos.games.templar.ui.art.TitleArt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.Optional;

import static eu.amadeos.games.templar.ui.art.Brush.c;

/**
 * The running game: the loop, the mouse and the keys, and the scenes, the title, the story being played, and
 * the end of what is written so far. Everything here runs on the game loop thread, except the {@link Arena}
 * methods, which post commands to it.
 */
public final class TemplarGame implements Arena {
    private static final Logger LOG = LoggerFactory.getLogger(TemplarGame.class);
    static final int UPDATES_PER_SECOND = 60;
    static final int FRAMES_PER_SECOND = 60;
    /** The version of the saved games' format. */
    static final int SAVE_VERSION = 1;
    /** How long a short notice, such as "Game saved", stays on the screen. */
    static final double NOTICE_SECONDS = 2.2;

    private final Keyboard keyboard;
    private final Mouse mouse;
    private final Messages msg;
    private final Mixer mixer;
    private final SaveFile saveFile;
    private final Gallery gallery = new Gallery();
    private final SoundBank sounds = new SoundBank();
    private final Hud hud;
    private final RoomView view = new RoomView();
    private final Notebook notebook = new Notebook();
    private final SceneStack scenes = new SceneStack();
    private GameLoop loop;
    private double time;
    private String notice;
    private double noticeLeft;

    @Inject
    public TemplarGame(Keyboard keyboard, Mouse mouse, Messages msg, Mixer mixer, SaveFile saveFile) {
        this.keyboard = keyboard;
        this.mouse = mouse;
        this.msg = msg;
        this.mixer = mixer;
        this.saveFile = saveFile;
        this.hud = new Hud(msg);
        scenes.push(new TitleScene());
    }

    /** Starts painting the rooms and the loop; the canvas draws the scenes' worlds and then their overlays. */
    public void start(GameCanvas canvas) {
        gallery.prepare(Story.load().startRoom());
        loop = new GameLoop(UPDATES_PER_SECOND, FRAMES_PER_SECOND, this::update, canvas::render);
        loop.start();
    }

    public void stop() {
        if (loop != null) loop.stop();
        Scene top = scenes.top();
        if (top instanceof PlayScene play) play.autosave();
    }

    void update(double seconds) {
        time += seconds;
        keyboard.poll();
        mouse.poll();
        if (noticeLeft > 0) noticeLeft -= seconds;
        scenes.update(seconds);
    }

    public void render(Graphics2D g, double alpha) {
        scenes.render(g, alpha);
    }

    public void renderOverlay(Graphics2D g, double alpha) {
        scenes.renderOverlay(g, alpha);
        if (noticeLeft > 0 && notice != null) {
            g.setFont(Hud.LABEL);
            FontMetrics fm = g.getFontMetrics();
            int w = fm.stringWidth(notice);
            g.setColor(c(0x000000, 170));
            g.fillRoundRect(320 - w / 2 - 10, 46, w + 20, 20, 8, 8);
            g.setColor(Hud.GOLD);
            g.drawString(notice, 320 - w / 2, 60);
        }
    }

    private void notice(String key) {
        notice = msg.get(key);
        noticeLeft = NOTICE_SECONDS;
    }

    private Vector pointer() {
        return mouse.isInside() ? new Vector(mouse.x(), mouse.y()) : null;
    }

    // ---- Arena: called on the Swing thread, carried out on the loop thread -----------------------

    @Override
    public void showTitle() {
        run(() -> {
            if (scenes.top() instanceof PlayScene play) play.autosave();
            scenes.replace(new TitleScene());
        });
    }

    @Override
    public void newGame() {
        run(() -> play(Adventure.begin(Story.load(), msg::get)));
    }

    @Override
    public void continueGame() {
        run(() -> load().ifPresentOrElse(this::play, () -> notice("notice.nothing-saved")));
    }

    @Override
    public boolean hasSavedGame() {
        return saveFile.load().isPresent();
    }

    @Override
    public void saveGame() {
        run(() -> {
            if (scenes.top() instanceof PlayScene play) play.save(true);
        });
    }

    @Override
    public void setSound(boolean on) {
        mixer.setMuted(!on);
    }

    private void run(Runnable command) {
        if (loop != null && loop.running()) loop.post(command);
        else command.run();
    }

    private Optional<Adventure> load() {
        Optional<SaveFile.Saved> saved = saveFile.load();
        if (saved.isEmpty()) return Optional.empty();
        try {
            return Optional.of(Adventure.restore(Story.load(), msg::get, saved.get().values()));
        } catch (RuntimeException e) {
            LOG.warn("Cannot continue the saved game: {}", e.getMessage());
            notice("notice.cannot-load");
            return Optional.empty();
        }
    }

    // ---- for tests -------------------------------------------------------------------------------

    Scene top() {
        return scenes.top();
    }

    /** Plays an adventure at once, without the title. */
    PlayScene play(Adventure adventure) {
        PlayScene play = new PlayScene(adventure);
        scenes.replace(play);
        return play;
    }

    // ---- the scenes ------------------------------------------------------------------------------

    /** The seal of the order, the title, and two choices: a new game or the saved one. */
    final class TitleScene implements Scene {
        private final boolean saved = hasSavedGame();
        private double age;

        private Rectangle2D option(int i) {
            return new Rectangle2D.Double(220, 296 + i * 24, 200, 20);
        }

        @Override
        public void update(double seconds) {
            age += seconds;
            Vector p = pointer();
            if (keyboard.wasPressed(KeyEvent.VK_ENTER) || keyboard.wasPressed(KeyEvent.VK_SPACE)) {
                if (saved) continueGame();
                else newGame();
                return;
            }
            if (mouse.wasPressed(Interaction.LEFT) && p != null) {
                if (option(0).contains(p.x(), p.y())) newGame();
                else if (saved && option(1).contains(p.x(), p.y())) continueGame();
            }
        }

        @Override
        public void render(Graphics2D g, double alpha) {
            TitleArt.background(g, time);
        }

        @Override
        public void renderOverlay(Graphics2D g, double alpha) {
            TitleArt.title(g, msg.get("title.name"), msg.get("title.subtitle"), Math.min(1, age / 1.2));
            Vector p = pointer();
            String[] options = saved ? new String[]{"title.new", "title.continue"} : new String[]{"title.new"};
            g.setFont(new Font(Font.SERIF, Font.BOLD, 15));
            FontMetrics fm = g.getFontMetrics();
            for (int i = 0; i < options.length; i++) {
                Rectangle2D r = option(i);
                boolean hot = p != null && r.contains(p.x(), p.y());
                String s = msg.get(options[i]);
                g.setColor(hot ? Color.WHITE : Hud.GOLD);
                g.drawString(s, (float) (320 - fm.stringWidth(s) / 2.0), (float) r.getY() + 15);
            }
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            g.setColor(c(0xc8b890));
            String keys = msg.get("title.keys");
            g.drawString(keys, 320 - g.getFontMetrics().stringWidth(keys) / 2, 380);
            if (p != null) {
                g.setColor(Hud.GOLD);
                g.drawOval((int) p.x() - 5, (int) p.y() - 5, 10, 10);
            }
        }
    }

    /** The story being played. */
    final class PlayScene implements Scene {
        private final Adventure adventure;
        private Hotspot hover;
        private String hoverItem;

        PlayScene(Adventure adventure) {
            this.adventure = adventure;
            notebook.close();
        }

        Adventure adventure() {
            return adventure;
        }

        @Override
        public void update(double seconds) {
            Vector p = pointer();
            boolean ready = !adventure.isBusy();
            hud.update(seconds, p, ready || adventure.held() != null);
            keys();
            if (!notebook.isOpen()) mouse(p);
            else if (mouse.wasPressed(Interaction.LEFT) || mouse.wasPressed(Interaction.RIGHT)) notebook.close();
            adventure.update(seconds);
            for (String name : adventure.takeSounds()) {
                Sound sound = sounds.get(name);
                if (sound != null) mixer.play(sound);
            }
            hover = p != null && !hud.onBar(p) && !adventure.isBusy() ? adventure.hotspotAt(p) : null;
            hoverItem = p != null ? hud.itemAt(p, adventure) : null;
            if (adventure.takeSaveWanted()) save(false);
            if (adventure.flags().is("finished") && !adventure.isBusy()) {
                save(false);
                scenes.replace(new EndScene());
            }
        }

        private void keys() {
            if (keyboard.wasPressed(KeyEvent.VK_N)) notebook.press();
            if (keyboard.wasPressed(KeyEvent.VK_ESCAPE)) {
                if (notebook.isOpen()) notebook.close();
                else adventure.skip();
            }
            if (keyboard.wasPressed(KeyEvent.VK_PERIOD)) adventure.skip();
            if (keyboard.wasPressed(KeyEvent.VK_F5)) save(true);
            if (keyboard.wasPressed(KeyEvent.VK_F9)) continueGame();
        }

        private void mouse(Vector p) {
            for (int button : new int[]{Interaction.LEFT, Interaction.RIGHT}) {
                if (!mouse.wasPressed(button) || p == null) continue;
                if (adventure.isChoosing()) {
                    String choice = hud.choiceAt(p, adventure);
                    if (choice != null) adventure.choose(choice);
                    continue;
                }
                String item = hud.itemAt(p, adventure);
                if (item != null) adventure.clickItem(button, item);
                else if (!hud.onBar(p)) adventure.click(button, p);
            }
        }

        /** Saves the game if nothing is playing; {@code tell} shows whether it worked. */
        void save(boolean tell) {
            if (!adventure.isIdle()) {
                if (tell) notice("notice.not-now");
                return;
            }
            try {
                Map<String, String> values = adventure.save();
                saveFile.store(SAVE_VERSION, values);
                if (tell) notice("notice.saved");
            } catch (RuntimeException e) {
                LOG.warn("Cannot save the game: {}", e.getMessage());
                if (tell) notice("notice.cannot-save");
            }
        }

        void autosave() {
            save(false);
        }

        @Override
        public void render(Graphics2D g, double alpha) {
            view.draw(g, adventure, gallery.room(adventure.room().id()), time);
        }

        @Override
        public void renderOverlay(Graphics2D g, double alpha) {
            hud.draw(g, adventure, pointer(), hover, hoverItem, notebook, time);
        }
    }

    /**
     * The end of the story: "The End", then what really happened, page by page, and what the game made up.
     * A click turns the page; after the last one comes the title screen.
     */
    final class EndScene implements Scene {
        static final int PAGES = 7;
        private double age;
        private int page;

        @Override
        public void update(double seconds) {
            age += seconds;
            if (age > 1.2 && (mouse.wasPressed(Interaction.LEFT) || keyboard.wasPressed(KeyEvent.VK_ENTER)
                    || keyboard.wasPressed(KeyEvent.VK_SPACE))) {
                age = 0;
                if (++page > PAGES) scenes.replace(new TitleScene());
            }
            if (keyboard.wasPressed(KeyEvent.VK_ESCAPE)) scenes.replace(new TitleScene());
        }

        @Override
        public void render(Graphics2D g, double alpha) {
            TitleArt.background(g, time);
        }

        @Override
        public void renderOverlay(Graphics2D g, double alpha) {
            double fade = Math.min(1, age / 0.8);
            if (page == 0) TitleArt.title(g, msg.get("end.title"), msg.get("end.subtitle"), fade);
            else fact(g, page, fade);
            g.setFont(new Font(Font.SERIF, Font.ITALIC, 13));
            g.setColor(c(0xe8dcc0));
            String s = msg.get("end.back");
            g.drawString(s, 320 - g.getFontMetrics().stringWidth(s) / 2, 370);
        }

        // --8<-- [start:epilogue]
        /** One page of "What really happened": a heading and a few lines, on a dark panel over the title picture. */
        private void fact(Graphics2D g, int n, double fade) {
            int a = (int) (255 * fade);
            g.setColor(new Color(12, 8, 4, (int) (190 * fade)));
            g.fill(new java.awt.geom.RoundRectangle2D.Double(60, 70, 520, 270, 14, 14));
            g.setFont(new Font(Font.SERIF, Font.ITALIC, 14));
            g.setColor(c(0xc8a868, a));
            String over = msg.get("epilogue.title") + "  ·  " + n + " / " + PAGES;
            g.drawString(over, 320 - g.getFontMetrics().stringWidth(over) / 2, 100);
            g.setFont(new Font(Font.SERIF, Font.BOLD | Font.ITALIC, 24));
            g.setColor(c(0xf2d48a, a));
            String head = msg.get("epilogue." + n + ".head");
            g.drawString(head, 320 - g.getFontMetrics().stringWidth(head) / 2, 136);
            Font body = new Font(Font.SERIF, Font.PLAIN, 15);
            g.setFont(body);
            g.setColor(c(0xeee4cc, a));
            double y = 172;
            for (String line : eu.amadeos.gameengine.adventure.SpokenText.wrap(msg.get("epilogue." + n + ".text"), body,
                    g.getFontRenderContext(), 470)) {
                g.drawString(line, 85f, (float) y);
                y += 21;
            }
        }
        // --8<-- [end:epilogue]
    }
}
