package eu.amadeos.games.templar.ui.game;

import eu.amadeos.commons.i18n.Messages;
import eu.amadeos.gameengine.adventure.Hotspot;
import eu.amadeos.gameengine.adventure.ScriptRunner;
import eu.amadeos.gameengine.adventure.SpokenText;
import eu.amadeos.gameengine.math.Vector;
import eu.amadeos.games.templar.domain.Adventure;
import eu.amadeos.games.templar.ui.art.Items;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static eu.amadeos.games.templar.ui.art.Brush.c;

/**
 * Everything drawn over the room, sharp at any size: the inventory bar that slides down from the top, the name
 * of what the pointer is over, the spoken lines over the speakers, the answers of a dialog, title cards, the
 * notebook, the fades and the pointer itself.
 */
final class Hud {
    static final double W = 640, H = 400;
    static final double SLOT_W = 44, SLOT_H = 32, SLOT_GAP = 6, BAR_TOP = 4;
    static final Font SPEECH = new Font(Font.SANS_SERIF, Font.BOLD, 11);
    static final Font LABEL = new Font(Font.SERIF, Font.BOLD | Font.ITALIC, 13);
    static final Font CHOICE = new Font(Font.SANS_SERIF, Font.BOLD, 11);
    static final Font CARD = new Font(Font.SERIF, Font.BOLD | Font.ITALIC, 22);
    static final Font NOTE = new Font(Font.SERIF, Font.ITALIC, 13);
    static final Color GOLD = c(0xf2d48a);
    /** The colour of each speaker's lines. */
    static final Map<String, Color> VOICES = Map.ofEntries(
            Map.entry("lena", c(0xfff2c8)), Map.entry("aimery", c(0xc8f0a8)), Map.entry("gerard", c(0xf4f4f4)),
            Map.entry("guard", c(0xd8c0a0)), Map.entry("soldier", c(0x9ab8ff)), Map.entry("lefort", c(0xa8d8f0)),
            Map.entry("kev", c(0xffb060)), Map.entry("helene", c(0xe8a8d8)), Map.entry("driver", c(0xb8b8c0)),
            Map.entry("guide", c(0xfff07a)), Map.entry("cat", c(0xe0e0e0)), Map.entry("pigeon", c(0xd0d0e0)),
            Map.entry("ines", c(0xb8f0d0)));

    /** The colour of a speaker's lines; "kev-court" speaks in Kev's colour. */
    static Color voice(String actor) {
        Color voice = VOICES.get(actor);
        int dash = actor.indexOf('-');
        if (voice == null && dash > 0) voice = VOICES.get(actor.substring(0, dash));
        return voice == null ? Color.WHITE : voice;
    }

    private final Messages msg;
    private double bar;

    Hud(Messages msg) {
        this.msg = msg;
    }

    /** Slides the inventory bar down while the pointer is at the top of the screen and the player can act. */
    void update(double seconds, Vector pointer, boolean open) {
        double target = open && pointer != null && pointer.y() < (bar > 0.5 ? BAR_TOP + SLOT_H + 14 : 22) ? 1 : 0;
        bar += Math.signum(target - bar) * Math.min(Math.abs(target - bar), seconds * 6);
    }

    boolean barOpen() {
        return bar > 0.95;
    }

    private static double barY(double bar) {
        return BAR_TOP - (1 - bar) * (SLOT_H + 12);
    }

    private static Rectangle2D slot(int i, int count) {
        int shown = Math.max(8, count);
        double width = shown * SLOT_W + (shown - 1) * SLOT_GAP;
        double left = (W - width) / 2;
        return new Rectangle2D.Double(left + i * (SLOT_W + SLOT_GAP), 0, SLOT_W, SLOT_H);
    }

    /** The item in the slot under the point, while the bar is open. */
    String itemAt(Vector p, Adventure a) {
        if (!barOpen() || p == null) return null;
        List<String> items = a.inventory().items();
        for (int i = 0; i < items.size(); i++) {
            Rectangle2D r = slot(i, items.size());
            if (r.contains(p.x(), p.y() - barY(bar))) return items.get(i);
        }
        return null;
    }

    /** Whether the point is on the open bar, so a click there is not a click into the room. */
    boolean onBar(Vector p) {
        return bar > 0.2 && p != null && p.y() < barY(bar) + SLOT_H + 6;
    }

    private Rectangle2D choiceRow(int i, int count) {
        double rowH = 17, top = H - count * rowH - 14;
        return new Rectangle2D.Double(30, top + i * rowH, W - 60, rowH);
    }

    String choiceAt(Vector p, Adventure a) {
        if (p == null || !a.isChoosing()) return null;
        List<ScriptRunner.Choice> choices = a.choices();
        for (int i = 0; i < choices.size(); i++)
            if (choiceRow(i, choices.size()).contains(p.x(), p.y())) return choices.get(i).id();
        return null;
    }

    void draw(Graphics2D g, Adventure a, Vector pointer, Hotspot hover, String hoverItem, Notebook notebook, double time) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Rectangle2D screen = new Rectangle2D.Double(0, 0, W, H);
        Adventure.Line line = a.line();
        if (line != null) {
            Point2D head = RoomView.head(a, line.actor());
            Color voice = voice(line.actor());
            if (head == null) SpokenText.draw(g, line.text(), SPEECH, voice, W / 2, 60, 420, screen);
            else SpokenText.draw(g, line.text(), SPEECH, voice, head.getX(), head.getY() - 4, 300, screen);
        }
        if (a.isChoosing()) choices(g, a, pointer);
        else if (!a.isBusy()) label(g, a, hover, hoverItem);
        if (bar > 0) inventory(g, a, pointer);
        if (notebook.isOpen()) notebook.draw(g, a, msg);
        if (a.card() != null) card(g, a);
        double dark = a.darkness();
        if (dark > 0) {
            g.setColor(new Color(0, 0, 0, (int) Math.round(255 * dark)));
            g.fill(screen);
        }
        pointer(g, a, pointer, hover != null || hoverItem != null, time);
    }

    private void label(Graphics2D g, Adventure a, Hotspot hover, String hoverItem) {
        String held = a.held();
        String target = hover != null ? name(hover.id()) : hoverItem != null && !hoverItem.equals(held) ? item(hoverItem) : null;
        String text;
        if (held != null) text = target == null ? msg.format("label.use", item(held)) : msg.format("label.use-on", item(held), target);
        else text = target;
        if (text == null) return;
        g.setFont(LABEL);
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(text);
        g.setColor(c(0x000000, 150));
        g.fill(new RoundRectangle2D.Double(W / 2 - tw / 2.0 - 10, 378, tw + 20, 18, 8, 8));
        g.setColor(GOLD);
        g.drawString(text, (float) (W / 2 - tw / 2.0), 391.5f);
    }

    String name(String hotspot) {
        String key = "name." + hotspot;
        return msg.has(key) ? msg.get(key) : hotspot;
    }

    String item(String item) {
        String key = "item." + item;
        return msg.has(key) ? msg.get(key) : item;
    }

    private void choices(Graphics2D g, Adventure a, Vector pointer) {
        List<ScriptRunner.Choice> choices = a.choices();
        if (choices.isEmpty()) return;
        Rectangle2D first = choiceRow(0, choices.size());
        g.setColor(c(0x140c06, 215));
        g.fill(new RoundRectangle2D.Double(20, first.getY() - 7, W - 40, choices.size() * 17 + 14, 10, 10));
        g.setColor(c(0xb08a50));
        g.draw(new RoundRectangle2D.Double(20, first.getY() - 7, W - 40, choices.size() * 17 + 14, 10, 10));
        g.setFont(CHOICE);
        String over = choiceAt(pointer, a);
        for (int i = 0; i < choices.size(); i++) {
            Rectangle2D r = choiceRow(i, choices.size());
            boolean hot = choices.get(i).id().equals(over);
            g.setColor(hot ? c(0xffffff) : c(0xe8c890));
            g.drawString((hot ? "› " : "  ") + msg.get(choices.get(i).text()), (float) r.getX() + 8, (float) r.getY() + 12.5f);
        }
    }

    private void inventory(Graphics2D g, Adventure a, Vector pointer) {
        List<String> items = a.inventory().items();
        double y = barY(bar);
        int count = items.size();
        Rectangle2D firstSlot = slot(0, count), lastSlot = slot(Math.max(8, count) - 1, count);
        RoundRectangle2D frame = new RoundRectangle2D.Double(firstSlot.getX() - 8, y - 10, lastSlot.getMaxX() - firstSlot.getX() + 16, SLOT_H + 18, 12, 12);
        g.setColor(c(0x140c06, 220));
        g.fill(frame);
        g.setColor(c(0xb08a50));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(frame);
        String over = itemAt(pointer, a);
        for (int i = 0; i < Math.max(8, count); i++) {
            Rectangle2D r = slot(i, count);
            RoundRectangle2D box = new RoundRectangle2D.Double(r.getX(), y, r.getWidth(), r.getHeight(), 6, 6);
            String item = i < count ? items.get(i) : null;
            boolean hot = item != null && item.equals(over);
            g.setColor(hot ? c(0x4a3418) : c(0x2a1c10));
            g.fill(box);
            g.setColor(item != null && item.equals(a.held()) ? GOLD : c(0x6a5030));
            g.draw(box);
            if (item != null && !item.equals(a.held())) {
                var keep = g.getTransform();
                g.translate(r.getCenterX(), y + r.getHeight() / 2);
                Items.draw(g, item);
                g.setTransform(keep);
            }
        }
        g.setStroke(new BasicStroke(1f));
    }

    private void card(Graphics2D g, Adventure a) {
        double age = a.cardAge();
        double alpha = Math.min(1, Math.min(age / 0.6, (Adventure.CARD_SECONDS - age) / 0.6));
        alpha = Math.max(0, alpha);
        g.setColor(new Color(0, 0, 0, (int) (200 * Math.min(1, age / 0.4))));
        g.fill(new Rectangle2D.Double(0, 0, W, H));
        // the first part is the heading; the rest is wrapped to the width of the screen, as long as a language makes it
        String[] parts = a.card().text().split("\\|");
        List<String> lines = new ArrayList<>();
        lines.add(parts[0].strip());
        Font note = NOTE.deriveFont(15f);
        for (int i = 1; i < parts.length; i++) {
            lines.addAll(SpokenText.wrap(parts[i].strip(), note, g.getFontRenderContext(), W - 60));
        }
        double y = H / 2 - (lines.size() - 1) * 12;
        g.setColor(new Color(GOLD.getRed(), GOLD.getGreen(), GOLD.getBlue(), (int) (255 * alpha)));
        for (int i = 0; i < lines.size(); i++) {
            g.setFont(i == 0 ? CARD : note);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(lines.get(i), (float) (W / 2 - fm.stringWidth(lines.get(i)) / 2.0), (float) y);
            y += i == 0 ? 30 : 20;
        }
    }

    private void pointer(Graphics2D g, Adventure a, Vector p, boolean hot, double time) {
        if (p == null) return;
        double x = p.x(), y = p.y();
        if (a.held() != null) {
            var keep = g.getTransform();
            g.translate(x, y);
            Items.draw(g, a.held());
            g.setTransform(keep);
        }
        boolean waiting = a.isBusy() && !a.isChoosing();
        g.setStroke(new BasicStroke(1.6f));
        g.setColor(c(0x000000, 160));
        g.draw(new Ellipse2D.Double(x - 6, y - 6, 12, 12));
        g.setColor(waiting ? c(0x9a8a70) : hot ? c(0xffe080) : c(0xe8dcc0));
        double r = hot ? 5 + Math.sin(time * 6) * 0.6 : 5;
        g.draw(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
        if (!waiting) {
            g.drawLine((int) (x - 10), (int) y, (int) (x - 6), (int) y);
            g.drawLine((int) (x + 6), (int) y, (int) (x + 10), (int) y);
            g.drawLine((int) x, (int) (y - 10), (int) x, (int) (y - 6));
            g.drawLine((int) x, (int) (y + 6), (int) x, (int) (y + 10));
        }
        g.setStroke(new BasicStroke(1f));
    }
}
