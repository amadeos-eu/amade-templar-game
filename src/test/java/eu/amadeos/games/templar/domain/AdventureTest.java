package eu.amadeos.games.templar.domain;

import eu.amadeos.gameengine.adventure.Interaction;
import eu.amadeos.gameengine.math.Vector;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The rules of play, apart from any particular puzzle. */
class AdventureTest {
    final PlaythroughTest play = new PlaythroughTest();
    final Adventure game = play.game;

    @Test
    void anItemInHandIsPutBackWithTheRightButton() {
        play.settle();
        game.clickItem(Interaction.LEFT, "ledger");
        assertEquals("ledger", game.held());
        game.click(Interaction.RIGHT, new Vector(320, 380));
        assertNull(game.held());
    }

    @Test
    void lookingAtSomethingWithoutAScriptGivesTheDefaultLine() {
        play.settle();
        game.clickItem(Interaction.LEFT, "ledger");
        game.clickItem(Interaction.LEFT, "key");
        assertNotNull(game.line());
        assertEquals("default.combine", game.line().key());
    }

    @Test
    void aSavedGameComesBackTheSame() {
        play.use("key", "chest");
        play.use("supper");
        Map<String, String> saved = game.save();
        Adventure again = Adventure.restore(Story.load(), PlaythroughTest.MSG::get, saved);
        assertEquals(game.room().id(), again.room().id());
        assertEquals(game.inventory().items(), again.inventory().items());
        assertEquals(game.flags().all(), again.flags().all());
        assertTrue(again.room().isHidden("supper"));
        assertFalse(again.room().isHidden("chest-open"));
        assertEquals(game.hero().position(), again.hero().position());
    }

    @Test
    void aDialogOptionMarkedOnceIsOfferedOnce() {
        play.settle();
        play.use("gerard");
        assertTrue(game.choices().stream().anyMatch(c -> c.id().equals("why")));
        play.choose("why");
        assertFalse(game.choices().stream().anyMatch(c -> c.id().equals("why")));
        play.choose("bye");
        assertFalse(game.isChoosing());
    }

    @Test
    void walkingStaysOnTheFloor() {
        play.settle();
        game.click(Interaction.LEFT, new Vector(330, 344));      // under the table
        play.settle();
        assertTrue(game.room().floor().contains(game.hero().position()));
    }
}
