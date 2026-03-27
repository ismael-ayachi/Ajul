package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.*;
import ch.epfl.ajul.gamestate.packed.*;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGeneratorFactory;

import static ch.epfl.ajul.Game.PlayerDescription.PlayerKind.HUMAN;
import static org.junit.jupiter.api.Assertions.*;

class MutableGameStateTest {

    private final java.util.random.RandomGenerator seedGenerator =
            RandomGeneratorFactory.getDefault().create(2026);

    private static Game game(int n) {
        var allPlayers = List.of(
                new Game.PlayerDescription(PlayerId.P1, "P1", HUMAN),
                new Game.PlayerDescription(PlayerId.P2, "P2", HUMAN),
                new Game.PlayerDescription(PlayerId.P3, "P3", HUMAN),
                new Game.PlayerDescription(PlayerId.P4, "P4", HUMAN));
        return new Game(allPlayers.subList(0, n));
    }

    private static MutableGameState initialState(int n) {
        return new MutableGameState(ImmutableGameState.initial(game(n)));
    }

    private static MutableGameState stateFrom(Game game, int pkTileBag, int[] sources,
                                              int pkUnique, int[] playerStates, PlayerId current) {
        return new MutableGameState(new ImmutableGameState(game, pkTileBag,
                ImmutableIntArray.copyOf(sources), pkUnique,
                ImmutableIntArray.copyOf(playerStates), current));
    }

    // ===================== constructeurs =====================

    @Test
    void mutableGameStateConstructorWithObserverDoesNotThrow() {
        assertDoesNotThrow(() -> new MutableGameState(
                ImmutableGameState.initial(game(2)), PointsObserver.EMPTY));
    }

    @Test
    void mutableGameStateConstructorWithoutObserverDoesNotThrow() {
        assertDoesNotThrow(() -> new MutableGameState(ImmutableGameState.initial(game(2))));
    }

    @Test
    void mutableGameStateAccessorsMatchInitialState() {
        for (var n = 2; n <= 4; n += 1) {
            var g = game(n);
            var initial = ImmutableGameState.initial(g);
            var mutable = new MutableGameState(initial);
            assertEquals(initial.game(), mutable.game());
            assertEquals(initial.pkTileBag(), mutable.pkTileBag());
            assertEquals(initial.pkUniqueTileSources(), mutable.pkUniqueTileSources());
            assertEquals(initial.currentPlayerId(), mutable.currentPlayerId());
            assertEquals(initial.pkTileSources().size(), mutable.pkTileSources().size());
        }
    }

    // ===================== fillFactories =====================

    @Test
    void mutableGameStateFillFactoriesReducesBagByCorrectAmount() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var n = 2; n <= 4; n += 1) {
            var gs = initialState(n);
            var tilesNeeded = gs.game().factoriesCount() * TileSource.Factory.TILES_PER_FACTORY;
            gs.fillFactories(rng);
            var remaining = 0;
            for (var color : TileKind.Colored.ALL)
                remaining += PkTileSet.countOf(gs.pkTileBag(), color);
            assertEquals(100 - tilesNeeded, remaining);
        }
    }

    @Test
    void mutableGameStateFillFactoriesEachFactoryHasExactly4Tiles() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var n = 2; n <= 4; n += 1) {
            var gs = initialState(n);
            gs.fillFactories(rng);
            for (var factory : gs.game().factories()) {
                var count = 0;
                for (var color : TileKind.Colored.ALL)
                    count += PkTileSet.countOf(gs.pkTileSources().get(factory.index()), color);
                assertEquals(TileSource.Factory.TILES_PER_FACTORY, count);
            }
        }
    }

    @Test
    void mutableGameStateFillFactoriesDoesNotPutTilesInCentralArea() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var n = 2; n <= 4; n += 1) {
            var gs = initialState(n);
            gs.fillFactories(rng);
            for (var color : TileKind.Colored.ALL)
                assertEquals(0, PkTileSet.countOf(gs.pkTileSources().get(0), color));
        }
    }

    @Test
    void mutableGameStateFillFactoriesCentralAreaPreservesMarker() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var n = 2; n <= 4; n += 1) {
            var gs = initialState(n);
            gs.fillFactories(rng);
            assertEquals(1, PkTileSet.countOf(gs.pkTileSources().get(0), TileKind.FIRST_PLAYER_MARKER));
        }
    }

    @Test
    void mutableGameStateFillFactoriesUpdatesPkUniqueTileSources() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        var gs = initialState(2);
        gs.fillFactories(rng);
        assertNotEquals(PkIntSet32.EMPTY, gs.pkUniqueTileSources());
    }

    @Test
    void mutableGameStateFillFactoriesUsesDiscardedTilesWhenBagTooSmall() {
        // sac avec seulement 4 tuiles A, 16 tuiles B sorties du jeu
        var g = game(2);
        var sources = new int[g.tileSourcesCount()];
        sources[0] = PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);
        var playerStates = PkPlayerStates.initial(g).toArray();
        // sac = 4 tuiles A seulement
        var smallBag = PkTileSet.of(4, TileKind.Colored.A);
        var gs = stateFrom(g, smallBag, sources, PkIntSet32.EMPTY,
                playerStates, PlayerId.P1);
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        gs.fillFactories(rng);
        // les fabriques doivent contenir au total 4*factoriesCount tuiles
        var total = 0;
        for (var factory : g.factories())
            for (var color : TileKind.Colored.ALL)
                total += PkTileSet.countOf(gs.pkTileSources().get(factory.index()), color);
        // fabriques count = 5, tiles needed = 20, bag had only 4 → uses discarded
        assertTrue(total > 0);
    }

    @Test
    void mutableGameStateFillFactoriesIsReproducibleWithSameSeed() {
        for (var n = 2; n <= 4; n += 1) {
            var gs1 = initialState(n);
            var gs2 = initialState(n);
            var rng1 = RandomGeneratorFactory.getDefault().create(42);
            var rng2 = RandomGeneratorFactory.getDefault().create(42);
            gs1.fillFactories(rng1);
            gs2.fillFactories(rng2);
            for (var i = 0; i < gs1.pkTileSources().size(); i += 1)
                assertEquals(gs1.pkTileSources().get(i), gs2.pkTileSources().get(i));
        }
    }

    // ===================== registerMove =====================

    @Test
    void mutableGameStateRegisterMoveChangesCurrentPlayer() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        var gs = initialState(2);
        gs.fillFactories(rng);
        var before = gs.currentPlayerId();
        var dest = new short[Move.MAX_MOVES];
        var count = gs.validMoves(dest);
        assertTrue(count > 0);
        gs.registerMove(dest[0]);
        assertNotEquals(before, gs.currentPlayerId());
    }

    @Test
    void mutableGameStateRegisterMoveCurrentPlayerCyclesCorrectly() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var n = 2; n <= 4; n += 1) {
            var gs = initialState(n);
            gs.fillFactories(rng);
            // jouer n coups → on doit revenir au premier joueur
            for (var i = 0; i < n; i += 1) {
                var dest = new short[Move.MAX_MOVES];
                gs.validMoves(dest);
                gs.registerMove(dest[0]);
            }
            assertEquals(PlayerId.P1, gs.currentPlayerId());
        }
    }

    @Test
    void mutableGameStateRegisterMoveRemovesColorFromSource() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        var gs = initialState(2);
        gs.fillFactories(rng);
        var dest = new short[Move.MAX_MOVES];
        var count = gs.validMoves(dest);
        assertTrue(count > 0);
        var move = Move.ofPacked(dest[0]);
        var sourceIndex = move.source().index();
        var color = move.tileColor();
        gs.registerMove(dest[0]);
        assertEquals(0, PkTileSet.countOf(gs.pkTileSources().get(sourceIndex), color));
    }

    @Test
    void mutableGameStateRegisterMoveFromFactoryMovesRemainingToCentralArea() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        var gs = initialState(2);
        gs.fillFactories(rng);
        var dest = new short[Move.MAX_MOVES];
        var count = gs.validMoves(dest);
        for (var i = 0; i < count; i += 1) {
            var move = Move.ofPacked(dest[i]);
            if (move.source() instanceof TileSource.Factory) {
                var sourceIndex = move.source().index();
                var color = move.tileColor();
                var sourceBefore = gs.pkTileSources().get(sourceIndex);
                var centralBefore = gs.pkTileSources().get(0);
                var otherColorCount = 0;
                for (var c : TileKind.Colored.ALL)
                    if (c != color) otherColorCount += PkTileSet.countOf(sourceBefore, c);
                gs.registerMove(dest[i]);
                // les tuiles restantes de la fabrique doivent être dans la zone centrale
                var addedToCenter = 0;
                for (var c : TileKind.Colored.ALL)
                    addedToCenter += PkTileSet.countOf(gs.pkTileSources().get(0), c)
                            - PkTileSet.countOf(centralBefore, c);
                assertEquals(otherColorCount, addedToCenter);
                return;
            }
        }
    }

    @Test
    void mutableGameStateRegisterMoveFromFactoryEmptiesFactory() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        var gs = initialState(2);
        gs.fillFactories(rng);
        var dest = new short[Move.MAX_MOVES];
        var count = gs.validMoves(dest);
        for (var i = 0; i < count; i += 1) {
            var move = Move.ofPacked(dest[i]);
            if (move.source() instanceof TileSource.Factory) {
                var sourceIndex = move.source().index();
                gs.registerMove(dest[i]);
                assertEquals(PkTileSet.EMPTY, gs.pkTileSources().get(sourceIndex));
                return;
            }
        }
    }

    @Test
    void mutableGameStateRegisterMoveFromCentralAreaTransfersMarkerToFloor() {
        var g = game(2);
        var sources = new int[g.tileSourcesCount()];
        sources[0] = PkTileSet.union(
                PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER),
                PkTileSet.of(2, TileKind.Colored.A));
        var uniqueSources = PkIntSet32.add(PkIntSet32.EMPTY, 0);
        var playerStates = PkPlayerStates.initial(g).toArray();
        var gs = stateFrom(g, PkTileSet.FULL_COLORED, sources, uniqueSources,
                playerStates, PlayerId.P1);
        // jouer depuis la zone centrale avec couleur A vers le plancher
        var dest = new short[Move.MAX_MOVES];
        var count = gs.validMoves(dest);
        for (var i = 0; i < count; i += 1) {
            var move = Move.ofPacked(dest[i]);
            if (move.source() instanceof TileSource.CenterArea
                    && move.destination() == TileDestination.FLOOR) {
                gs.registerMove(dest[i]);
                // le marqueur doit avoir quitté la zone centrale
                assertEquals(0, PkTileSet.countOf(gs.pkTileSources().get(0), TileKind.FIRST_PLAYER_MARKER));
                // le plancher de P1 doit contenir le marqueur
                assertTrue(PkFloor.containsFirstPlayerMarker(
                        PkPlayerStates.pkFloor(gs.pkPlayerStates(), PlayerId.P1)));
                return;
            }
        }
    }

    @Test
    void mutableGameStateRegisterMoveToPatternLineAddsExcessToFloor() {
        var g = game(2);
        var sources = new int[g.tileSourcesCount()];
        // fabrique 1 avec 4 tuiles A
        sources[1] = PkTileSet.of(4, TileKind.Colored.A);
        var uniqueSources = PkIntSet32.add(PkIntSet32.EMPTY, 1);
        var playerStates = PkPlayerStates.initial(g).toArray();
        var gs = stateFrom(g, PkTileSet.FULL_COLORED, sources, uniqueSources,
                playerStates, PlayerId.P1);
        // jouer 4 tuiles A sur la ligne PATTERN_1 (capacité 1) → 3 excédentaires sur le plancher
        var pkMove = PkMove.pack(TileSource.ALL.get(1), TileKind.Colored.A, TileDestination.Pattern.PATTERN_1);
        gs.registerMove(pkMove);
        // ligne de motif doit avoir 1 tuile A
        assertEquals(1, PkPatterns.size(
                PkPlayerStates.pkPatterns(gs.pkPlayerStates(), PlayerId.P1),
                TileDestination.Pattern.PATTERN_1));
        // plancher doit avoir 3 tuiles A
        assertEquals(3, PkFloor.size(
                PkPlayerStates.pkFloor(gs.pkPlayerStates(), PlayerId.P1)));
    }

    @Test
    void mutableGameStateRegisterMoveUpdatesPkUniqueTileSources() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        var gs = initialState(2);
        gs.fillFactories(rng);
        var before = gs.pkUniqueTileSources();
        var dest = new short[Move.MAX_MOVES];
        gs.validMoves(dest);
        gs.registerMove(dest[0]);
        // après un coup, les sources uniques peuvent avoir changé
        assertNotNull(gs.pkUniqueTileSources()); // toujours valide
        // la source utilisée ne doit plus être dans les sources uniques si elle est vide
        var move = Move.ofPacked(dest[0]);
        var sourceIndex = move.source().index();
        if (gs.pkTileSources().get(sourceIndex) == PkTileSet.EMPTY)
            assertFalse(PkIntSet32.contains(gs.pkUniqueTileSources(), sourceIndex));
    }

    // ===================== endRound =====================

    @Test
    void mutableGameStateEndRoundEmptiesFullPatternLines() {
        var g = game(2);
        var sources = new int[g.tileSourcesCount()];
        var playerStates = PkPlayerStates.initial(g).toArray();
        var pkPatterns = PkPatterns.withAddedTiles(PkPatterns.EMPTY,
                TileDestination.Pattern.PATTERN_1,
                TileDestination.Pattern.PATTERN_1.capacity(),
                TileKind.Colored.A);
        PkPlayerStates.setPkPatterns(playerStates, PlayerId.P1, pkPatterns);
        var gs = stateFrom(g, PkTileSet.FULL_COLORED, sources, PkIntSet32.EMPTY,
                playerStates, PlayerId.P1);
        gs.endRound();
        assertEquals(0, PkPatterns.size(
                PkPlayerStates.pkPatterns(gs.pkPlayerStates(), PlayerId.P1),
                TileDestination.Pattern.PATTERN_1));
    }

    @Test
    void mutableGameStateEndRoundPlacesFullLineTileOnWall() {
        var g = game(2);
        var sources = new int[g.tileSourcesCount()];
        var playerStates = PkPlayerStates.initial(g).toArray();
        var pkPatterns = PkPatterns.withAddedTiles(PkPatterns.EMPTY,
                TileDestination.Pattern.PATTERN_1,
                TileDestination.Pattern.PATTERN_1.capacity(),
                TileKind.Colored.A);
        PkPlayerStates.setPkPatterns(playerStates, PlayerId.P1, pkPatterns);
        var gs = stateFrom(g, PkTileSet.FULL_COLORED, sources, PkIntSet32.EMPTY,
                playerStates, PlayerId.P1);
        gs.endRound();
        assertTrue(PkWall.hasTileAt(
                PkPlayerStates.pkWall(gs.pkPlayerStates(), PlayerId.P1),
                TileDestination.Pattern.PATTERN_1, TileKind.Colored.A));
    }

    @Test
    void mutableGameStateEndRoundAddsPointsForFullLine() {
        var g = game(2);
        var sources = new int[g.tileSourcesCount()];
        var playerStates = PkPlayerStates.initial(g).toArray();
        var pkPatterns = PkPatterns.withAddedTiles(PkPatterns.EMPTY,
                TileDestination.Pattern.PATTERN_1,
                TileDestination.Pattern.PATTERN_1.capacity(),
                TileKind.Colored.A);
        PkPlayerStates.setPkPatterns(playerStates, PlayerId.P1, pkPatterns);
        var gs = stateFrom(g, PkTileSet.FULL_COLORED, sources, PkIntSet32.EMPTY,
                playerStates, PlayerId.P1);
        gs.endRound();
        assertTrue(PkPlayerStates.points(gs.pkPlayerStates(), PlayerId.P1) > 0);
    }

    @Test
    void mutableGameStateEndRoundDoesNotAddPointsForNonFullLine() {
        var g = game(2);
        var sources = new int[g.tileSourcesCount()];
        var playerStates = PkPlayerStates.initial(g).toArray();
        // ligne partiellement remplie (pas pleine)
        var pkPatterns = PkPatterns.withAddedTiles(PkPatterns.EMPTY,
                TileDestination.Pattern.PATTERN_2, 1, TileKind.Colored.B);
        PkPlayerStates.setPkPatterns(playerStates, PlayerId.P1, pkPatterns);
        var gs = stateFrom(g, PkTileSet.FULL_COLORED, sources, PkIntSet32.EMPTY,
                playerStates, PlayerId.P1);
        gs.endRound();
        assertEquals(0, PkPlayerStates.points(gs.pkPlayerStates(), PlayerId.P1));
    }

    @Test
    void mutableGameStateEndRoundDeductsFloorPenalty() {
        var g = game(2);
        var sources = new int[g.tileSourcesCount()];
        var playerStates = PkPlayerStates.initial(g).toArray();
        PkPlayerStates.addPoints(playerStates, PlayerId.P1, 10);
        var pkFloor = PkFloor.withAddedTiles(PkFloor.EMPTY, PkTileSet.of(3, TileKind.Colored.B));
        PkPlayerStates.setPkFloor(playerStates, PlayerId.P1, pkFloor);
        var gs = stateFrom(g, PkTileSet.FULL_COLORED, sources, PkIntSet32.EMPTY,
                playerStates, PlayerId.P1);
        gs.endRound();
        // pénalité pour 3 tuiles = 4 → 10 - 4 = 6
        assertEquals(6, PkPlayerStates.points(gs.pkPlayerStates(), PlayerId.P1));
    }

    @Test
    void mutableGameStateEndRoundScoreNeverNegative() {
        var g = game(2);
        var sources = new int[g.tileSourcesCount()];
        var playerStates = PkPlayerStates.initial(g).toArray();
        // 0 pts, 7 tuiles sur le plancher → pénalité de 14 → score = 0
        var pkFloor = PkFloor.withAddedTiles(PkFloor.EMPTY, PkTileSet.of(7, TileKind.Colored.C));
        PkPlayerStates.setPkFloor(playerStates, PlayerId.P1, pkFloor);
        var gs = stateFrom(g, PkTileSet.FULL_COLORED, sources, PkIntSet32.EMPTY,
                playerStates, PlayerId.P1);
        gs.endRound();
        assertEquals(0, PkPlayerStates.points(gs.pkPlayerStates(), PlayerId.P1));
    }

    @Test
    void mutableGameStateEndRoundEmptiesFloor() {
        var g = game(2);
        var sources = new int[g.tileSourcesCount()];
        var playerStates = PkPlayerStates.initial(g).toArray();
        PkPlayerStates.addPoints(playerStates, PlayerId.P1, 10);
        var pkFloor = PkFloor.withAddedTiles(PkFloor.EMPTY, PkTileSet.of(2, TileKind.Colored.A));
        PkPlayerStates.setPkFloor(playerStates, PlayerId.P1, pkFloor);
        var gs = stateFrom(g, PkTileSet.FULL_COLORED, sources, PkIntSet32.EMPTY,
                playerStates, PlayerId.P1);
        gs.endRound();
        assertEquals(PkFloor.EMPTY, PkPlayerStates.pkFloor(gs.pkPlayerStates(), PlayerId.P1));
    }

    @Test
    void mutableGameStateEndRoundReplacesMarkerInCentralArea() {
        var g = game(2);
        var sources = new int[g.tileSourcesCount()];
        // marqueur absent de la zone centrale
        sources[0] = PkTileSet.EMPTY;
        var playerStates = PkPlayerStates.initial(g).toArray();
        PkPlayerStates.addPoints(playerStates, PlayerId.P1, 10);
        // marqueur sur le plancher de P1
        var pkFloor = PkFloor.withAddedTiles(PkFloor.EMPTY,
                PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER));
        PkPlayerStates.setPkFloor(playerStates, PlayerId.P1, pkFloor);
        var gs = stateFrom(g, PkTileSet.FULL_COLORED, sources, PkIntSet32.EMPTY,
                playerStates, PlayerId.P1);
        gs.endRound();
        assertEquals(1, PkTileSet.countOf(gs.pkTileSources().get(0), TileKind.FIRST_PLAYER_MARKER));
    }

    @Test
    void mutableGameStateEndRoundSetsCurrentPlayerToMarkerHolder() {
        var g = game(2);
        var sources = new int[g.tileSourcesCount()];
        sources[0] = PkTileSet.EMPTY;
        var playerStates = PkPlayerStates.initial(g).toArray();
        PkPlayerStates.addPoints(playerStates, PlayerId.P2, 10);
        // marqueur sur le plancher de P2
        var pkFloor = PkFloor.withAddedTiles(PkFloor.EMPTY,
                PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER));
        PkPlayerStates.setPkFloor(playerStates, PlayerId.P2, pkFloor);
        var gs = stateFrom(g, PkTileSet.FULL_COLORED, sources, PkIntSet32.EMPTY,
                playerStates, PlayerId.P1);
        gs.endRound();
        assertEquals(PlayerId.P2, gs.currentPlayerId());
    }

    @Test
    void mutableGameStateEndRoundDeductsFloorPenaltyForAllPlayers() {
        var g = game(4);
        var sources = new int[g.tileSourcesCount()];
        var playerStates = PkPlayerStates.initial(g).toArray();
        PkPlayerStates.addPoints(playerStates, PlayerId.P1, 10);
        PkPlayerStates.addPoints(playerStates, PlayerId.P2, 10);
        var pkFloor1 = PkFloor.withAddedTiles(PkFloor.EMPTY, PkTileSet.of(2, TileKind.Colored.A));
        var pkFloor2 = PkFloor.withAddedTiles(PkFloor.EMPTY, PkTileSet.of(3, TileKind.Colored.B));
        PkPlayerStates.setPkFloor(playerStates, PlayerId.P1, pkFloor1);
        PkPlayerStates.setPkFloor(playerStates, PlayerId.P2, pkFloor2);
        var gs = stateFrom(g, PkTileSet.FULL_COLORED, sources, PkIntSet32.EMPTY,
                playerStates, PlayerId.P1);
        gs.endRound();
        assertEquals(10 - Points.totalFloorPenalty(2),
                PkPlayerStates.points(gs.pkPlayerStates(), PlayerId.P1));
        assertEquals(10 - Points.totalFloorPenalty(3),
                PkPlayerStates.points(gs.pkPlayerStates(), PlayerId.P2));
    }

    // ===================== endGame =====================

    @Test
    void mutableGameStateEndGameAddsFullRowBonus() {
        var g = game(2);
        var sources = new int[g.tileSourcesCount()];
        var playerStates = PkPlayerStates.initial(g).toArray();
        var pkWall = PkWall.EMPTY;
        for (var color : TileKind.Colored.ALL)
            pkWall = PkWall.withTileAt(pkWall, TileDestination.Pattern.PATTERN_1, color);
        PkPlayerStates.setPkWall(playerStates, PlayerId.P1, pkWall);
        var gs = stateFrom(g, PkTileSet.EMPTY, sources, PkIntSet32.EMPTY,
                playerStates, PlayerId.P1);
        gs.endGame();
        assertEquals(Points.FULL_ROW_BONUS_POINTS,
                PkPlayerStates.points(gs.pkPlayerStates(), PlayerId.P1));
    }

    @Test
    void mutableGameStateEndGameAddsFullRowBonusForEachFullRow() {
        var g = game(2);
        var sources = new int[g.tileSourcesCount()];
        var playerStates = PkPlayerStates.initial(g).toArray();
        var pkWall = PkWall.EMPTY;
        // remplir deux lignes
        for (var color : TileKind.Colored.ALL) {
            pkWall = PkWall.withTileAt(pkWall, TileDestination.Pattern.PATTERN_1, color);
            pkWall = PkWall.withTileAt(pkWall, TileDestination.Pattern.PATTERN_2, color);
        }
        PkPlayerStates.setPkWall(playerStates, PlayerId.P1, pkWall);
        var gs = stateFrom(g, PkTileSet.EMPTY, sources, PkIntSet32.EMPTY,
                playerStates, PlayerId.P1);
        gs.endGame();
        assertEquals(2 * Points.FULL_ROW_BONUS_POINTS,
                PkPlayerStates.points(gs.pkPlayerStates(), PlayerId.P1));
    }

    @Test
    void mutableGameStateEndGameAddsFullColumnBonus() {
        var g = game(2);
        var sources = new int[g.tileSourcesCount()];
        var playerStates = PkPlayerStates.initial(g).toArray();
        var pkWall = PkWall.EMPTY;
        for (var line : TileDestination.Pattern.ALL)
            pkWall = PkWall.withTileAt(pkWall, line, PkWall.colorAt(line, 0));
        PkPlayerStates.setPkWall(playerStates, PlayerId.P1, pkWall);
        var gs = stateFrom(g, PkTileSet.EMPTY, sources, PkIntSet32.EMPTY,
                playerStates, PlayerId.P1);
        gs.endGame();
        assertEquals(Points.FULL_COLUMN_BONUS_POINTS,
                PkPlayerStates.points(gs.pkPlayerStates(), PlayerId.P1));
    }

    @Test
    void mutableGameStateEndGameAddsFullColorBonus() {
        var g = game(2);
        var sources = new int[g.tileSourcesCount()];
        var playerStates = PkPlayerStates.initial(g).toArray();
        var pkWall = PkWall.EMPTY;
        for (var line : TileDestination.Pattern.ALL)
            pkWall = PkWall.withTileAt(pkWall, line, TileKind.Colored.A);
        PkPlayerStates.setPkWall(playerStates, PlayerId.P1, pkWall);
        var gs = stateFrom(g, PkTileSet.EMPTY, sources, PkIntSet32.EMPTY,
                playerStates, PlayerId.P1);
        gs.endGame();
        assertEquals(Points.FULL_COLOR_BONUS_POINTS,
                PkPlayerStates.points(gs.pkPlayerStates(), PlayerId.P1));
    }

    @Test
    void mutableGameStateEndGameAddsNoBonusForEmptyWall() {
        var g = game(2);
        var sources = new int[g.tileSourcesCount()];
        var playerStates = PkPlayerStates.initial(g).toArray();
        var gs = stateFrom(g, PkTileSet.EMPTY, sources, PkIntSet32.EMPTY,
                playerStates, PlayerId.P1);
        gs.endGame();
        assertEquals(0, PkPlayerStates.points(gs.pkPlayerStates(), PlayerId.P1));
    }

    @Test
    void mutableGameStateEndGameChecksAllPlayers() {
        var g = game(4);
        var sources = new int[g.tileSourcesCount()];
        var playerStates = PkPlayerStates.initial(g).toArray();
        // P1 : ligne complète
        var pkWall1 = PkWall.EMPTY;
        for (var color : TileKind.Colored.ALL)
            pkWall1 = PkWall.withTileAt(pkWall1, TileDestination.Pattern.PATTERN_1, color);
        PkPlayerStates.setPkWall(playerStates, PlayerId.P1, pkWall1);
        // P2 : colonne complète
        var pkWall2 = PkWall.EMPTY;
        for (var line : TileDestination.Pattern.ALL)
            pkWall2 = PkWall.withTileAt(pkWall2, line, PkWall.colorAt(line, 0));
        PkPlayerStates.setPkWall(playerStates, PlayerId.P2, pkWall2);
        // P3 : couleur complète
        var pkWall3 = PkWall.EMPTY;
        for (var line : TileDestination.Pattern.ALL)
            pkWall3 = PkWall.withTileAt(pkWall3, line, TileKind.Colored.B);
        PkPlayerStates.setPkWall(playerStates, PlayerId.P3, pkWall3);
        var gs = stateFrom(g, PkTileSet.EMPTY, sources, PkIntSet32.EMPTY,
                playerStates, PlayerId.P1);
        gs.endGame();
        assertEquals(Points.FULL_ROW_BONUS_POINTS,
                PkPlayerStates.points(gs.pkPlayerStates(), PlayerId.P1));
        assertEquals(Points.FULL_COLUMN_BONUS_POINTS,
                PkPlayerStates.points(gs.pkPlayerStates(), PlayerId.P2));
        assertEquals(Points.FULL_COLOR_BONUS_POINTS,
                PkPlayerStates.points(gs.pkPlayerStates(), PlayerId.P3));
        assertEquals(0, PkPlayerStates.points(gs.pkPlayerStates(), PlayerId.P4));
    }

    // ===================== pointsObserver =====================

    @Test
    void mutableGameStateObserverCalledForNewWallTile() {
        var g = game(2);
        var sources = new int[g.tileSourcesCount()];
        var playerStates = PkPlayerStates.initial(g).toArray();
        var pkPatterns = PkPatterns.withAddedTiles(PkPatterns.EMPTY,
                TileDestination.Pattern.PATTERN_1,
                TileDestination.Pattern.PATTERN_1.capacity(),
                TileKind.Colored.A);
        PkPlayerStates.setPkPatterns(playerStates, PlayerId.P1, pkPatterns);
        var calledPlayers = new ArrayList<PlayerId>();
        var observer = new PointsObserver() {
            @Override
            public void newWallTile(PlayerId p, TileDestination.Pattern l, TileKind.Colored c, int pts) {
                calledPlayers.add(p);
            }
        };
        var gs = new MutableGameState(new ImmutableGameState(g, PkTileSet.FULL_COLORED,
                ImmutableIntArray.copyOf(sources), PkIntSet32.EMPTY,
                ImmutableIntArray.copyOf(playerStates), PlayerId.P1), observer);
        gs.endRound();
        assertTrue(calledPlayers.contains(PlayerId.P1));
    }

    @Test
    void mutableGameStateObserverCalledForFloorPenalty() {
        var g = game(2);
        var sources = new int[g.tileSourcesCount()];
        var playerStates = PkPlayerStates.initial(g).toArray();
        PkPlayerStates.addPoints(playerStates, PlayerId.P1, 10);
        var pkFloor = PkFloor.withAddedTiles(PkFloor.EMPTY, PkTileSet.of(3, TileKind.Colored.B));
        PkPlayerStates.setPkFloor(playerStates, PlayerId.P1, pkFloor);
        var calledPenalties = new ArrayList<Integer>();
        var observer = new PointsObserver() {
            @Override
            public void floor(PlayerId p, int penalty) {
                calledPenalties.add(penalty);
            }
        };
        var gs = new MutableGameState(new ImmutableGameState(g, PkTileSet.FULL_COLORED,
                ImmutableIntArray.copyOf(sources), PkIntSet32.EMPTY,
                ImmutableIntArray.copyOf(playerStates), PlayerId.P1), observer);
        gs.endRound();
        assertFalse(calledPenalties.isEmpty());
        assertEquals(Points.totalFloorPenalty(3), calledPenalties.getFirst());
    }

    @Test
    void mutableGameStateObserverCalledForFullRow() {
        var g = game(2);
        var sources = new int[g.tileSourcesCount()];
        var playerStates = PkPlayerStates.initial(g).toArray();
        var pkWall = PkWall.EMPTY;
        for (var color : TileKind.Colored.ALL)
            pkWall = PkWall.withTileAt(pkWall, TileDestination.Pattern.PATTERN_1, color);
        PkPlayerStates.setPkWall(playerStates, PlayerId.P1, pkWall);
        var calledLines = new ArrayList<TileDestination.Pattern>();
        var observer = new PointsObserver() {
            @Override
            public void fullRow(PlayerId p, TileDestination.Pattern line, int pts) {
                calledLines.add(line);
            }
        };
        var gs = new MutableGameState(new ImmutableGameState(g, PkTileSet.EMPTY,
                ImmutableIntArray.copyOf(sources), PkIntSet32.EMPTY,
                ImmutableIntArray.copyOf(playerStates), PlayerId.P1), observer);
        gs.endGame();
        assertTrue(calledLines.contains(TileDestination.Pattern.PATTERN_1));
    }

    @Test
    void mutableGameStateObserverCalledForFullColumn() {
        var g = game(2);
        var sources = new int[g.tileSourcesCount()];
        var playerStates = PkPlayerStates.initial(g).toArray();
        var pkWall = PkWall.EMPTY;
        for (var line : TileDestination.Pattern.ALL)
            pkWall = PkWall.withTileAt(pkWall, line, PkWall.colorAt(line, 0));
        PkPlayerStates.setPkWall(playerStates, PlayerId.P1, pkWall);
        var calledColumns = new ArrayList<Integer>();
        var observer = new PointsObserver() {
            @Override
            public void fullColumn(PlayerId p, int col, int pts) {
                calledColumns.add(col);
            }
        };
        var gs = new MutableGameState(new ImmutableGameState(g, PkTileSet.EMPTY,
                ImmutableIntArray.copyOf(sources), PkIntSet32.EMPTY,
                ImmutableIntArray.copyOf(playerStates), PlayerId.P1), observer);
        gs.endGame();
        assertTrue(calledColumns.contains(0));
    }

    @Test
    void mutableGameStateObserverCalledForFullColor() {
        var g = game(2);
        var sources = new int[g.tileSourcesCount()];
        var playerStates = PkPlayerStates.initial(g).toArray();
        var pkWall = PkWall.EMPTY;
        for (var line : TileDestination.Pattern.ALL)
            pkWall = PkWall.withTileAt(pkWall, line, TileKind.Colored.A);
        PkPlayerStates.setPkWall(playerStates, PlayerId.P1, pkWall);
        var calledColors = new ArrayList<TileKind.Colored>();
        var observer = new PointsObserver() {
            @Override
            public void fullColor(PlayerId p, TileKind.Colored color, int pts) {
                calledColors.add(color);
            }
        };
        var gs = new MutableGameState(new ImmutableGameState(g, PkTileSet.EMPTY,
                ImmutableIntArray.copyOf(sources), PkIntSet32.EMPTY,
                ImmutableIntArray.copyOf(playerStates), PlayerId.P1), observer);
        gs.endGame();
        assertTrue(calledColors.contains(TileKind.Colored.A));
    }
}