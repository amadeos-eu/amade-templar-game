package eu.amadeos.games.templar.ui.game;

import eu.amadeos.commons.i18n.Messages;
import eu.amadeos.gameengine.adventure.SpokenText;
import eu.amadeos.games.templar.domain.Adventure;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

import static eu.amadeos.games.templar.ui.art.Brush.c;

/**
 * Maria's notebook, opened with N: what she wants to do next, in one line, from the story's flag {@code goal}.
 * Pressing N again while it is open adds a hint, {@code hint.<goal>}; a third time closes it.
 */
final class Notebook {
    private int pressed;

    void press() {
        pressed = (pressed + 1) % 3;
    }

    void close() {
        pressed = 0;
    }

    boolean isOpen() {
        return pressed > 0;
    }

    void draw(Graphics2D g, Adventure a, Messages msg) {
        int goal = a.flags().get("goal");
        double x = 150, y = 80, w = 340, h = pressed > 1 ? 170 : 120;
        RoundRectangle2D page = new RoundRectangle2D.Double(x, y, w, h, 8, 8);
        g.setColor(c(0x000000, 90));
        g.fill(new RoundRectangle2D.Double(x + 4, y + 5, w, h, 8, 8));
        g.setColor(c(0xf2e6c8));
        g.fill(page);
        g.setColor(c(0x7a2a1e));
        g.fill(new RoundRectangle2D.Double(x, y, 10, h, 6, 6));
        g.setColor(c(0xb8c8d8, 140));
        for (double ly = y + 34; ly < y + h - 6; ly += 15) g.drawLine((int) x + 16, (int) ly, (int) (x + w - 10), (int) ly);
        g.setFont(new Font(Font.SERIF, Font.BOLD, 14));
        g.setColor(c(0x3a2414));
        g.drawString(msg.get("notebook.title"), (float) x + 22, (float) y + 24);
        Font hand = new Font(Font.SERIF, Font.ITALIC, 13);
        String key = "goal." + goal;
        double ty = text(g, msg.has(key) ? msg.get(key) : msg.get("goal.none"), hand, c(0x1a2a4a), x + 22, y + 46, w - 40);
        if (pressed > 1) {
            String hint = "hint." + goal;
            text(g, msg.has(hint) ? msg.get(hint) : msg.get("hint.none"), hand, c(0x7a2a1e), x + 22, ty + 10, w - 40);
        }
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
        g.setColor(c(0x6a5a40));
        g.drawString(msg.get(pressed > 1 ? "notebook.close" : "notebook.more"), (float) x + 22, (float) (y + h - 8));
    }

    private static double text(Graphics2D g, String text, Font font, Color color, double x, double y, double width) {
        g.setFont(font);
        g.setColor(color);
        FontMetrics fm = g.getFontMetrics();
        List<String> lines = SpokenText.wrap(text, font, g.getFontRenderContext(), width);
        for (String line : lines) {
            g.drawString(line, (float) x, (float) y);
            y += fm.getHeight();
        }
        return y;
    }
}
