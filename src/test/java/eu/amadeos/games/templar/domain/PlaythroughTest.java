package eu.amadeos.games.templar.domain;

import eu.amadeos.commons.i18n.Messages;
import eu.amadeos.gameengine.adventure.Hotspot;
import eu.amadeos.gameengine.adventure.Interaction;
import eu.amadeos.gameengine.math.Vector;
import eu.amadeos.games.templar.bootstrap.TemplarModule;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Plays the story with clicks, as a player would, and checks that every puzzle can be solved. */
class PlaythroughTest {
    static final Messages MSG = Messages.load(TemplarModule.class, TemplarModule.MESSAGES, Locale.ENGLISH);

    final Adventure game = Adventure.begin(Story.load(), MSG::get);

    /** Lets the game run, clicking away every line, until the player may act again. */
    void settle() {
        for (int i = 0; i < 30 * 120; i++) {
            game.update(1.0 / 30);
            if (game.line() != null || game.card() != null) game.skip();
            if (game.isChoosing() || game.isIdle()) return;
        }
        throw new AssertionError("the game never came to rest");
    }

    Vector at(String id) {
        for (Adventure.Figure f : game.figures()) {
            if (f.id().equals(id)) return f.actor().position().plus(new Vector(0, -30));
        }
        Hotspot h = game.room().hotspot(id);
        double x = 0, y = 0;
        for (Vector v : h.area()) {
            x += v.x();
            y += v.y();
        }
        return new Vector(x / h.area().size(), y / h.area().size());
    }

    void use(String id) {
        settle();
        game.click(Interaction.LEFT, at(id));
        settle();
    }

    void use(String item, String id) {
        settle();
        game.clickItem(Interaction.LEFT, item);
        game.click(Interaction.LEFT, at(id));
        settle();
    }

    void choose(String option) {
        settle();
        assertTrue(game.isChoosing(), "a dialog should wait for " + option);
        game.choose(option);
        settle();
    }

    void workshop() {
        settle();
        assertEquals("workshop", game.room().id());
        use("coat");
        use("brush");
        use("bonefolder");
        use("brush", "jar");
        use("damp-brush", "psalter");
        use("bonefolder", "psalter");
        assertTrue(game.inventory().has("page"));
    }

    @Test
    void theWorkshopDoorWaitsForThePage() {
        settle();
        use("door");
        assertEquals("workshop", game.room().id());
    }

    @Test
    void lenaFindsThePageInTheBinding() {
        workshop();
        assertTrue(game.flags().is("found-page"));
        assertTrue(game.inventory().has("coin"));
        assertEquals(13, game.flags().get("goal"));
    }

    @Test
    void lefortReadsThePageAfterHisCoffee() {
        workshop();
        use("door");
        assertEquals("reading", game.room().id());
        use("street");
        assertEquals("reading", game.room().id(), "the street waits until the page is read");
        use("lefort");
        choose("page");
        choose("bye");
        assertEquals(1, game.flags().get("lefort"));
        use("coin", "machine");
        assertFalse(game.inventory().has("coffee"), "the machine gives no cups");
        use("lefort-cup");
        use("cup", "machine");
        assertTrue(game.inventory().has("coffee"));
        use("lefort");
        choose("give");
        choose("twenty");
        choose("all");
        assertTrue(game.flags().is("read-sum"));
        choose("bye");
        assertEquals(23, game.flags().get("goal"));
    }
}
