package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.*;
import ch.epfl.ajul.gamestate.packed.*;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import org.junit.jupiter.api.Test;

import java.util.List;

import static ch.epfl.ajul.Game.PlayerDescription.PlayerKind.HUMAN;
import static org.junit.jupiter.api.Assertions.*;

class MyImmutableGameStateTest {

    private static Game game(int n) {
        var allPlayers = List.of(
                new Game.PlayerDescription(PlayerId.P1, "P1", HUMAN),
                new Game.PlayerDescription(PlayerId.P2, "P2", HUMAN),
                new Game.PlayerDescription(PlayerId.P3, "P3", HUMAN),
                new Game.PlayerDescription(PlayerId.P4, "P4", HUMAN));
        return new Game(allPlayers.subList(0, n));
    }

    // ===================== constructeur compact =====================

    @Test
    void immutableGameStateConstructorThrowsOnNullGame() {
        var g = game(2);
        var sources = ImmutableIntArray.copyOf(new int[g.tileSourcesCount()]);
        var playerStates = PkPlayerStates.initial(g);
        assertThrows(NullPointerException.class, () ->
                new ImmutableGameState(null, PkTileSet.EMPTY, sources,
                        PkIntSet32.EMPTY, playerStates, PlayerId.P1));
    }

    @Test
    void immutableGameStateConstructorThrowsOnNullPkTileSources() {
        var g = game(2);
        var playerStates = PkPlayerStates.initial(g);
        assertThrows(NullPointerException.class, () ->
                new ImmutableGameState(g, PkTileSet.EMPTY, null,
                        PkIntSet32.EMPTY, playerStates, PlayerId.P1));
    }

    @Test
    void immutableGameStateConstructorThrowsOnNullPkPlayerStates() {
        var g = game(2);
        var sources = ImmutableIntArray.copyOf(new int[g.tileSourcesCount()]);
        assertThrows(NullPointerException.class, () ->
                new ImmutableGameState(g, PkTileSet.EMPTY, sources,
                        PkIntSet32.EMPTY, null, PlayerId.P1));
    }

    @Test
    void immutableGameStateConstructorThrowsOnNullCurrentPlayerId() {
        var g = game(2);
        var sources = ImmutableIntArray.copyOf(new int[g.tileSourcesCount()]);
        var playerStates = PkPlayerStates.initial(g);
        assertThrows(NullPointerException.class, () ->
                new ImmutableGameState(g, PkTileSet.EMPTY, sources,
                        PkIntSet32.EMPTY, playerStates, null));
    }

    // ===================== initial =====================

    @Test
    void immutableGameStateInitialCurrentPlayerIsP1() {
        for (var n = 2; n <= 4; n += 1)
            assertEquals(PlayerId.P1, ImmutableGameState.initial(game(n)).currentPlayerId());
    }

    @Test
    void immutableGameStateInitialBagIsFullColored() {
        for (var n = 2; n <= 4; n += 1)
            assertEquals(PkTileSet.FULL_COLORED, ImmutableGameState.initial(game(n)).pkTileBag());
    }

    @Test
    void immutableGameStateInitialCentralAreaHasFirstPlayerMarker() {
        for (var n = 2; n <= 4; n += 1) {
            var state = ImmutableGameState.initial(game(n));
            assertEquals(1, PkTileSet.countOf(state.pkTileSources().get(0), TileKind.FIRST_PLAYER_MARKER));
        }
    }

    @Test
    void immutableGameStateInitialCentralAreaHasNoColoredTiles() {
        for (var n = 2; n <= 4; n += 1) {
            var state = ImmutableGameState.initial(game(n));
            for (var color : TileKind.Colored.ALL)
                assertEquals(0, PkTileSet.countOf(state.pkTileSources().get(0), color));
        }
    }

    @Test
    void immutableGameStateInitialFactoriesAreEmpty() {
        for (var n = 2; n <= 4; n += 1) {
            var state = ImmutableGameState.initial(game(n));
            for (var i = 1; i < state.pkTileSources().size(); i += 1)
                assertEquals(PkTileSet.EMPTY, state.pkTileSources().get(i));
        }
    }

    @Test
    void immutableGameStateInitialUniqueTileSourcesIsEmpty() {
        for (var n = 2; n <= 4; n += 1)
            assertEquals(PkIntSet32.EMPTY, ImmutableGameState.initial(game(n)).pkUniqueTileSources());
    }

    @Test
    void immutableGameStateInitialTileSourcesHasCorrectSize() {
        for (var n = 2; n <= 4; n += 1) {
            var g = game(n);
            assertEquals(g.tileSourcesCount(), ImmutableGameState.initial(g).pkTileSources().size());
        }
    }

    @Test
    void immutableGameStateInitialPlayerStatesAreAllZero() {
        for (var n = 2; n <= 4; n += 1) {
            var state = ImmutableGameState.initial(game(n));
            for (var i = 0; i < state.pkPlayerStates().size(); i += 1)
                assertEquals(0, state.pkPlayerStates().get(i));
        }
    }

    // ===================== immutable() =====================

    @Test
    void immutableGameStateImmutableReturnsSelf() {
        var state = ImmutableGameState.initial(game(2));
        assertSame(state, state.immutable());
    }

    // ===================== isRoundOver =====================

    @Test
    void immutableGameStateInitialIsRoundOver() {
        for (var n = 2; n <= 4; n += 1)
            assertTrue(ImmutableGameState.initial(game(n)).isRoundOver());
    }

    // ===================== isGameOver =====================

    @Test
    void immutableGameStateInitialIsNotGameOver() {
        for (var n = 2; n <= 4; n += 1)
            assertFalse(ImmutableGameState.initial(game(n)).isGameOver());
    }

    // ===================== pkDiscardedTiles =====================

    @Test
    void immutableGameStateInitialDiscardedTilesHasNoColoredTiles() {
        for (var n = 2; n <= 4; n += 1) {
            var discarded = ImmutableGameState.initial(game(n)).pkDiscardedTiles();
            for (var color : TileKind.Colored.ALL)
                assertEquals(0, PkTileSet.countOf(discarded, color));
        }
    }

    // ===================== accesseurs =====================

    @Test
    void immutableGameStateGameReturnsCorrectGame() {
        for (var n = 2; n <= 4; n += 1) {
            var g = game(n);
            assertEquals(g, ImmutableGameState.initial(g).game());
        }
    }

    @Test
    void immutableGameStatePlayerIdsReturnsCorrectList() {
        for (var n = 2; n <= 4; n += 1) {
            var g = game(n);
            assertEquals(g.playerIds(), ImmutableGameState.initial(g).playerIds());
        }
    }
}