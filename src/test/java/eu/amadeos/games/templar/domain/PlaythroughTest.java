package eu.amadeos.games.templar.domain;

import eu.amadeos.commons.i18n.Messages;
import eu.amadeos.gameengine.adventure.Hotspot;
import eu.amadeos.gameengine.adventure.Interaction;
import eu.amadeos.gameengine.math.Vector;
import eu.amadeos.games.templar.bootstrap.TemplarModule;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Plays the story with clicks, as a player would, and checks that every puzzle can be solved. */
class PlaythroughTest {
    static final Messages MSG = Messages.load(TemplarModule.class, TemplarModule.MESSAGES, Locale.ENGLISH);

    final Adventure game = Adventure.begin(Story.load(), MSG::get);

    // --8<-- [start:settle]
    /** Lets the game run, clicking away every line, until the player may act again. */
    void settle() {
        for (int i = 0; i < 30 * 120; i++) {
            game.update(1.0 / 30);
            if (game.line() != null || game.card() != null) game.skip();
            if (game.isChoosing() || game.isIdle()) return;
        }
        throw new AssertionError("the game never came to rest");
    }
    // --8<-- [end:settle]

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

    void combine(String item, String other) {
        settle();
        game.clickItem(Interaction.LEFT, item);
        game.clickItem(Interaction.LEFT, other);
        settle();
    }

    void choose(String option) {
        settle();
        assertTrue(game.isChoosing(), "a dialog should wait for " + option);
        game.choose(option);
        settle();
    }

    // --8<-- [start:prologue]
    void prologue() {
        settle();
        assertEquals("chapter", game.room().id());
        assertTrue(game.inventory().has("ledger"));
        use("key", "chest");
        use("wax-stick");
        use("supper");
        use("wax", "candles");
        combine("soft-wax", "ledger");
        combine("seal", "ledger");
        assertTrue(game.flags().is("sealed"));
        use("courtyard-door");
        assertEquals("courtyard", game.room().id());
        use("cheese", "guard");
        assertTrue(game.flags().is("guard-gone"));
        use("gate");
        assertTrue(game.flags().is("soldiers"));
        use("library");
        assertEquals("library", game.room().id());
        use("copy-knife");
        use("glue-pot");
        use("knife", "temple-psalter");
        use("ledger", "temple-psalter");
        use("glue", "temple-psalter");
    }
    // --8<-- [end:prologue]

    void workshop() {
        prologue();
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
    void theChamberDoorWaitsForTheSeal() {
        settle();
        use("courtyard-door");
        assertEquals("chapter", game.room().id());
    }

    @Test
    void aimeryHidesThePageAndLenaWakesUpInTheWorkshop() {
        prologue();
        settle();
        assertEquals("workshop", game.room().id());
        assertEquals("lena", game.hero().id());
        assertEquals(List.of("phone"), game.inventory().items());
    }

    @Test
    void theWorkshopDoorWaitsForThePage() {
        prologue();
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

    void readingRoom() {
        use("door");
        use("lefort");
        choose("page");
        choose("bye");
        use("lefort-cup");
        use("coin", "machine");
        use("lefort");
        choose("give");
        choose("all");
        choose("bye");
        use("street");
    }

    @Test
    void theSquareGivesTheWayToThePontNeuf() {
        workshop();
        readingRoom();
        assertEquals("square", game.room().id());
        use("metro");
        assertEquals("square", game.room().id(), "the metro waits until Lena knows where to go");
        use("board");
        assertTrue(game.isChoosing(), "Hélène makes her offer");
        choose("offer");
        choose("no");
        assertTrue(game.flags().is("met-helene"));
        use("kev");
        choose("film");
        choose("curse");
        choose("bye");
        assertEquals(32, game.flags().get("goal"));
        use("half-sandwich");
        assertTrue(game.inventory().has("sandwich"));
        assertEquals(33, game.flags().get("goal"));
    }

    void square() {
        use("board");
        choose("no");
        use("kev");
        choose("film");
        choose("curse");
        choose("bye");
        use("half-sandwich");
        use("metro");
    }

    @Test
    void thePigeonsClearTheWayToThePlaqueAndActOneEnds() {
        workshop();
        readingRoom();
        square();
        assertEquals("bridge", game.room().id());
        use("plaque");
        assertTrue(game.isChoosing(), "the driver stands in front of the plaque, a click on it talks to him");
        choose("no");
        assertFalse(game.flags().is("read-plaque"));
        use("sandwich", "driver");
        assertTrue(game.flags().is("driver-gone"));
        use("plaque");
        assertTrue(game.flags().is("read-plaque"));
        use("phone", "plaque");
        assertEquals("cart", game.room().id(), "act 1 ends and the first flashback begins");
        assertEquals("aimery", game.hero().id());
        assertTrue(game.inventory().items().isEmpty(), "Lena's things wait for her");
    }

    void actOne() {
        workshop();
        readingRoom();
        square();
        use("sandwich", "driver");
        use("plaque");
        use("phone", "plaque");
    }

    // --8<-- [start:cart]
    void cart() {
        use("gerard-cart");
        choose("how");
        choose("bye");
        use("soldier-cart");
        choose("pray");
        choose("bye");
        use("raoul");
        choose("hear");
        choose("where");
        choose("half");
    }
    // --8<-- [end:cart]

    @Test
    void shoutingInFrenchMakesTheSoldierChaseRaoulAwayButNeverEndsTheGame() {
        actOne();
        for (int i = 0; i < 3; i++) {
            use("raoul");
            choose("page");
        }
        assertEquals(0, game.flags().get("suspicion"), "after the third shout the soldier sends Raoul off, and he comes back");
        assertTrue(game.figures().stream().anyMatch(f -> f.id().equals("raoul")));
        cart();
        assertEquals("harbour", game.room().id());
    }

    @Test
    void aimerySingsTheMessageInLatin() {
        actOne();
        use("raoul");
        assertTrue(game.isChoosing());
        assertTrue(game.choices().stream().noneMatch(c -> c.id().equals("hear")), "no psalm before the soldier allows prayer");
        choose("bye");
        cart();
        assertEquals("harbour", game.room().id());
        assertEquals("lena", game.hero().id());
        assertTrue(game.inventory().has("phone"));
        assertTrue(game.inventory().has("page"));
        assertTrue(game.inventory().has("bonefolder"));
    }

    void harbour() {
        use("fisherman");
        choose("templars");
        choose("bye");
        use("phone", "barometer");
        use("fisherman");
        choose("barometer");
        choose("bye");
        use("old-town");
    }

    @Test
    void theFishermanTellsTheWayForAForecast() {
        actOne();
        cart();
        use("old-town");
        assertEquals("harbour", game.room().id(), "the old town waits until Lena knows where to look");
        use("phone", "barometer");
        use("fisherman");
        choose("templars");
        assertEquals(62, game.flags().get("goal"), "the barometer read first counts as soon as he asks");
        choose("barometer");
        choose("bye");
        assertTrue(game.flags().is("fisher-done"));
        use("old-town");
        assertEquals("court", game.room().id());
    }

    void court() {
        use("napkin");
        use("napkin", "chalkboard");
        use("morin");
        choose("menu");
        choose("bye");
        use("chalkboard");
        use("bonefolder", "relief");
        use("kev-court");
        choose("lift");
        use("hollow");
    }

    @Test
    void theBoardStaysUntilMadameMorinAllowsIt() {
        actOne();
        cart();
        harbour();
        use("chalkboard");
        assertFalse(game.flags().is("board-moved"));
        use("morin");
        choose("board");
        choose("bye");
        assertFalse(game.flags().is("allowed"));
    }

    @Test
    void theTokenFromTomarLeadsToTheShipInTheFogAndActTwoEnds() {
        actOne();
        cart();
        harbour();
        court();
        assertEquals("quay", game.room().id());
        assertEquals("raoul", game.hero().id());
        assertEquals(List.of("lantern"), game.inventory().items());
        assertTrue(game.figures().stream().noneMatch(f -> f.id().equals("etienne")),
                "Étienne waits in the fog until the lantern signals");
        use("lantern", "ship");
        assertTrue(game.flags().is("signalled"));
        use("etienne");
        choose("page");
        assertTrue(game.flags().is("fog-seen"));
        settle();
        assertEquals("court", game.room().id());
        assertTrue(game.flags().is("finished"));
        assertTrue(game.inventory().has("token"));
        assertTrue(game.inventory().has("page"));
        assertFalse(game.inventory().has("lantern"));
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
