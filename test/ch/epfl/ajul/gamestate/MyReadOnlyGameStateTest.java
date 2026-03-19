package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.*;
import ch.epfl.ajul.gamestate.packed.*;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import org.junit.jupiter.api.Test;

import java.util.List;

import static ch.epfl.ajul.Game.PlayerDescription.PlayerKind.HUMAN;
import static org.junit.jupiter.api.Assertions.*;

class MyReadOnlyGameStateTest {

    private static Game game(int n) {
        var allPlayers = List.of(
                new Game.PlayerDescription(PlayerId.P1, "P1", HUMAN),
                new Game.PlayerDescription(PlayerId.P2, "P2", HUMAN),
                new Game.PlayerDescription(PlayerId.P3, "P3", HUMAN),
                new Game.PlayerDescription(PlayerId.P4, "P4", HUMAN));
        return new Game(allPlayers.subList(0, n));
    }

    // Calcule pkUniqueTileSources à partir des sources selon la définition de l'énoncé
    private static int computeUniqueSources(int[] sources) {
        var unique = PkIntSet32.EMPTY;
        for (var i = 0; i < sources.length; i++) {
            // doit contenir au moins une tuile colorée
            var hasColored = false;
            for (var color : TileKind.Colored.ALL) {
                if (PkTileSet.countOf(sources[i], color) > 0) {
                    hasColored = true;
                    break;
                }
            }
            if (!hasColored) continue;
            // ne doit pas avoir le même contenu qu'une source précédente
            var isDuplicate = false;
            for (var j = 0; j < i; j++) {
                if (sources[j] == sources[i]) {
                    isDuplicate = true;
                    break;
                }
            }
            if (!isDuplicate)
                unique = PkIntSet32.add(unique, i);
        }
        return unique;
    }

    private static ImmutableGameState stateWithSources(Game game, int[] sources) {
        return new ImmutableGameState(
                game,
                PkTileSet.FULL_COLORED,
                ImmutableIntArray.copyOf(sources),
                computeUniqueSources(sources),
                PkPlayerStates.initial(game),
                PlayerId.P1);
    }

    // ===================== isRoundOver =====================

    @Test
    void readOnlyGameStateIsRoundOverReturnsTrueWhenOnlyMarkerInCentralArea() {
        for (var n = 2; n <= 4; n += 1) {
            var g = game(n);
            var sources = new int[g.tileSourcesCount()];
            sources[0] = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);
            assertTrue(stateWithSources(g, sources).isRoundOver());
        }
    }

    @Test
    void readOnlyGameStateIsRoundOverReturnsFalseWhenColoredTilePresent() {
        for (var n = 2; n <= 4; n += 1) {
            var g = game(n);
            var sources = new int[g.tileSourcesCount()];
            sources[1] = PkTileSet.of(1, TileKind.Colored.A);
            assertFalse(stateWithSources(g, sources).isRoundOver());
        }
    }

    @Test
    void readOnlyGameStateIsRoundOverReturnsTrueWhenAllSourcesEmpty() {
        var g = game(2);
        var sources = new int[g.tileSourcesCount()];
        assertTrue(stateWithSources(g, sources).isRoundOver());
    }

    @Test
    void readOnlyGameStateIsRoundOverReturnsFalseWhenMultipleSourcesHaveColoredTiles() {
        var g = game(4);
        var sources = new int[g.tileSourcesCount()];
        sources[1] = PkTileSet.of(2, TileKind.Colored.A);
        sources[3] = PkTileSet.of(3, TileKind.Colored.B);
        assertFalse(stateWithSources(g, sources).isRoundOver());
    }

    @Test
    void readOnlyGameStateIsRoundOverReturnsFalseWhenCentralAreaHasColoredTile() {
        var g = game(2);
        var sources = new int[g.tileSourcesCount()];
        sources[0] = PkTileSet.of(2, TileKind.Colored.A);
        assertFalse(stateWithSources(g, sources).isRoundOver());
    }

    // ===================== isGameOver =====================

    @Test
    void readOnlyGameStateIsGameOverReturnsFalseWhenRoundNotOver() {
        var g = game(2);
        var sources = new int[g.tileSourcesCount()];
        sources[1] = PkTileSet.of(4, TileKind.Colored.A);
        assertFalse(stateWithSources(g, sources).isGameOver());
    }

    @Test
    void readOnlyGameStateIsGameOverReturnsFalseWhenRoundOverButNoFullRow() {
        var g = game(2);
        var sources = new int[g.tileSourcesCount()];
        assertTrue(stateWithSources(g, sources).isRoundOver());
        assertFalse(stateWithSources(g, sources).isGameOver());
    }

    @Test
    void readOnlyGameStateIsGameOverReturnsTrueWhenRoundOverAndPlayerHasFullRow() {
        var g = game(2);
        var sources = new int[g.tileSourcesCount()];
        var pkWall = PkWall.EMPTY;
        for (var color : TileKind.Colored.ALL)
            pkWall = PkWall.withTileAt(pkWall, TileDestination.Pattern.PATTERN_1, color);
        var playerStates = PkPlayerStates.initial(g).toArray();
        PkPlayerStates.setPkWall(playerStates, PlayerId.P1, pkWall);
        var state = new ImmutableGameState(g, PkTileSet.EMPTY,
                ImmutableIntArray.copyOf(sources), PkIntSet32.EMPTY,
                ImmutableIntArray.copyOf(playerStates), PlayerId.P1);
        assertTrue(state.isGameOver());
    }

    @Test
    void readOnlyGameStateIsGameOverChecksAllPlayers() {
        var g = game(4);
        var sources = new int[g.tileSourcesCount()];
        var pkWall = PkWall.EMPTY;
        for (var color : TileKind.Colored.ALL)
            pkWall = PkWall.withTileAt(pkWall, TileDestination.Pattern.PATTERN_3, color);
        var playerStates = PkPlayerStates.initial(g).toArray();
        PkPlayerStates.setPkWall(playerStates, PlayerId.P4, pkWall);
        var state = new ImmutableGameState(g, PkTileSet.EMPTY,
                ImmutableIntArray.copyOf(sources), PkIntSet32.EMPTY,
                ImmutableIntArray.copyOf(playerStates), PlayerId.P1);
        assertTrue(state.isGameOver());
    }

    // ===================== pkDiscardedTiles =====================

    @Test
    void readOnlyGameStateDiscardedTilesIsEmptyWhenBagIsFullAndPlateauxEmpty() {
        var g = game(2);
        var sources = new int[g.tileSourcesCount()];
        var discarded = stateWithSources(g, sources).pkDiscardedTiles();
        for (var color : TileKind.Colored.ALL)
            assertEquals(0, PkTileSet.countOf(discarded, color));
    }

    @Test
    void readOnlyGameStateDiscardedTilesAccountsForAllPlayers() {
        var g = game(2);
        var sources = new int[g.tileSourcesCount()];
        var pkWallP1 = PkWall.EMPTY;
        pkWallP1 = PkWall.withTileAt(pkWallP1, TileDestination.Pattern.PATTERN_1, TileKind.Colored.A);
        pkWallP1 = PkWall.withTileAt(pkWallP1, TileDestination.Pattern.PATTERN_2, TileKind.Colored.A);
        pkWallP1 = PkWall.withTileAt(pkWallP1, TileDestination.Pattern.PATTERN_3, TileKind.Colored.A);
        var pkWallP2 = PkWall.EMPTY;
        pkWallP2 = PkWall.withTileAt(pkWallP2, TileDestination.Pattern.PATTERN_1, TileKind.Colored.B);
        pkWallP2 = PkWall.withTileAt(pkWallP2, TileDestination.Pattern.PATTERN_2, TileKind.Colored.B);
        var playerStates = PkPlayerStates.initial(g).toArray();
        PkPlayerStates.setPkWall(playerStates, PlayerId.P1, pkWallP1);
        PkPlayerStates.setPkWall(playerStates, PlayerId.P2, pkWallP2);
        var state = new ImmutableGameState(g, PkTileSet.EMPTY,
                ImmutableIntArray.copyOf(sources), PkIntSet32.EMPTY,
                ImmutableIntArray.copyOf(playerStates), PlayerId.P1);
        var discarded = state.pkDiscardedTiles();
        assertEquals(17, PkTileSet.countOf(discarded, TileKind.Colored.A));
        assertEquals(18, PkTileSet.countOf(discarded, TileKind.Colored.B));
    }

    @Test
    void readOnlyGameStateDiscardedTilesAccountsForTileSources() {
        var g = game(2);
        var sources = new int[g.tileSourcesCount()];
        sources[1] = PkTileSet.of(3, TileKind.Colored.C);
        var state = new ImmutableGameState(g, PkTileSet.EMPTY,
                ImmutableIntArray.copyOf(sources), PkIntSet32.EMPTY,
                PkPlayerStates.initial(g), PlayerId.P1);
        var discarded = state.pkDiscardedTiles();
        assertEquals(17, PkTileSet.countOf(discarded, TileKind.Colored.C));
    }

    // ===================== validMoves =====================

    @Test
    void readOnlyGameStateValidMovesReturnsZeroWhenNoColoredTiles() {
        var g = game(2);
        var sources = new int[g.tileSourcesCount()];
        var destination = new short[Move.MAX_MOVES];
        assertEquals(0, stateWithSources(g, sources).validMoves(destination));
    }

    @Test
    void readOnlyGameStateValidMovesReturnsPositiveWhenSourceHasTile() {
        var g = game(2);
        var sources = new int[g.tileSourcesCount()];
        sources[1] = PkTileSet.of(4, TileKind.Colored.A);
        var destination = new short[Move.MAX_MOVES];
        assertTrue(stateWithSources(g, sources).validMoves(destination) > 0);
    }

    @Test
    void readOnlyGameStateValidMovesAlwaysIncludesFloor() {
        var g = game(2);
        var sources = new int[g.tileSourcesCount()];
        sources[1] = PkTileSet.of(4, TileKind.Colored.A);
        var destination = new short[Move.MAX_MOVES];
        var count = stateWithSources(g, sources).validMoves(destination);
        var hasFloor = false;
        for (var i = 0; i < count; i += 1) {
            if (Move.ofPacked(destination[i]).destination() == TileDestination.FLOOR) {
                hasFloor = true;
                break;
            }
        }
        assertTrue(hasFloor);
    }

    @Test
    void readOnlyGameStateValidMovesExcludesFullPatternLine() {
        var g = game(2);
        var sources = new int[g.tileSourcesCount()];
        sources[1] = PkTileSet.of(4, TileKind.Colored.A);
        var pkPatterns = PkPatterns.withAddedTiles(PkPatterns.EMPTY,
                TileDestination.Pattern.PATTERN_1,
                TileDestination.Pattern.PATTERN_1.capacity(),
                TileKind.Colored.A);
        var playerStates = PkPlayerStates.initial(g).toArray();
        PkPlayerStates.setPkPatterns(playerStates, PlayerId.P1, pkPatterns);
        var state = new ImmutableGameState(g, PkTileSet.FULL_COLORED,
                ImmutableIntArray.copyOf(sources), computeUniqueSources(sources),
                ImmutableIntArray.copyOf(playerStates), PlayerId.P1);
        var destination = new short[Move.MAX_MOVES];
        var count = state.validMoves(destination);
        for (var i = 0; i < count; i += 1)
            assertNotEquals(TileDestination.Pattern.PATTERN_1, Move.ofPacked(destination[i]).destination());
    }

    // ===================== uniqueValidMoves =====================

    @Test
    void readOnlyGameStateUniqueValidMovesIsSubsetOfValidMoves() {
        var g = game(4);
        var sources = new int[g.tileSourcesCount()];
        sources[1] = PkTileSet.of(2, TileKind.Colored.A);
        sources[2] = PkTileSet.of(2, TileKind.Colored.A);
        sources[3] = PkTileSet.of(3, TileKind.Colored.B);
        var uniqueSources = computeUniqueSources(sources);
        var state = new ImmutableGameState(g, PkTileSet.FULL_COLORED,
                ImmutableIntArray.copyOf(sources), uniqueSources,
                PkPlayerStates.initial(g), PlayerId.P1);
        var allDest = new short[Move.MAX_MOVES];
        var uniqueDest = new short[Move.MAX_MOVES];
        var allCount = state.validMoves(allDest);
        var uniqueCount = state.uniqueValidMoves(uniqueDest);
        assertTrue(uniqueCount <= allCount);
    }

    @Test
    void readOnlyGameStateUniqueValidMovesReturnsZeroWhenNoUniqueSources() {
        var g = game(2);
        var sources = new int[g.tileSourcesCount()];
        sources[1] = PkTileSet.of(4, TileKind.Colored.A);
        var state = new ImmutableGameState(g, PkTileSet.FULL_COLORED,
                ImmutableIntArray.copyOf(sources), PkIntSet32.EMPTY,
                PkPlayerStates.initial(g), PlayerId.P1);
        var destination = new short[Move.MAX_MOVES];
        assertEquals(0, state.uniqueValidMoves(destination));
    }

    @Test
    void readOnlyGameStateUniqueValidMovesExcludesDuplicateSources() {
        var g = game(4);
        var sources = new int[g.tileSourcesCount()];
        // sources 1 et 2 ont le même contenu → seule la source 1 est unique
        sources[1] = PkTileSet.of(2, TileKind.Colored.A);
        sources[2] = PkTileSet.of(2, TileKind.Colored.A);
        var uniqueSources = computeUniqueSources(sources);
        var state = new ImmutableGameState(g, PkTileSet.FULL_COLORED,
                ImmutableIntArray.copyOf(sources), uniqueSources,
                PkPlayerStates.initial(g), PlayerId.P1);
        var destination = new short[Move.MAX_MOVES];
        var count = state.uniqueValidMoves(destination);
        // tous les coups doivent venir de la source 1, pas de la source 2
        for (var i = 0; i < count; i += 1)
            assertNotEquals(TileSource.ALL.get(2), Move.ofPacked(destination[i]).source());
    }
}