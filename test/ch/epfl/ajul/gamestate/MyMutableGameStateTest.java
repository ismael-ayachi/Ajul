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

class MyMutableGameStateTest {

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

    // ===================== constructeurs =====================

    @Test
    void mutableGameStateConstructorWithObserverDoesNotThrow() {
        assertDoesNotThrow(() -> new MutableGameState(ImmutableGameState.initial(game(2)),
                PointsObserver.EMPTY));
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
        }
    }

    // ===================== fillFactories =====================

    @Test
    void mutableGameStateFillFactoriesEmptiesBag() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        for (var n = 2; n <= 4; n += 1) {
            var gs = initialState(n);
            gs.fillFactories(rng);
            // le sac doit avoir perdu exactement factoriesCount * 4 tuiles colorées
            var tilesUsed = gs.game().factoriesCount() * TileSource.Factory.TILES_PER_FACTORY;
            var remaining = 0;
            for (var color : TileKind.Colored.ALL)
                remaining += PkTileSet.countOf(gs.pkTileBag(), color);
            assertEquals(100 - tilesUsed, remaining);
        }
    }

    @Test
    void mutableGameStateFillFactoriesEachFactoryHas4Tiles() {
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
    void mutableGameStateFillFactoriesUpdatesPkUniqueTileSources() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        var gs = initialState(2);
        gs.fillFactories(rng);
        // après le remplissage des fabriques, pkUniqueTileSources ne doit plus être vide
        // car les fabriques contiennent des tuiles colorées
        assertTrue(gs.pkUniqueTileSources() != PkIntSet32.EMPTY);
    }

    @Test
    void mutableGameStateFillFactoriesCentralAreaPreservesMarker() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        var gs = initialState(2);
        gs.fillFactories(rng);
        // la zone centrale doit toujours contenir le marqueur
        assertEquals(1, PkTileSet.countOf(gs.pkTileSources().get(0), TileKind.FIRST_PLAYER_MARKER));
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
    void mutableGameStateRegisterMoveEmptiesSourceOfColoredTiles() {
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
        // trouver un coup depuis une fabrique
        for (var i = 0; i < count; i++) {
            var move = Move.ofPacked(dest[i]);
            if (move.source() instanceof TileSource.Factory) {
                var sourceIndex = move.source().index();
                var color = move.tileColor();
                var centralBefore = gs.pkTileSources().get(0);
                var sourceBefore = gs.pkTileSources().get(sourceIndex);
                var otherTiles = PkTileSet.difference(sourceBefore,
                        PkTileSet.of(PkTileSet.countOf(sourceBefore, color), color));
                gs.registerMove(dest[i]);
                // les tuiles restantes doivent être dans la zone centrale
                for (var c : TileKind.Colored.ALL) {
                    var expected = PkTileSet.countOf(centralBefore, c) + PkTileSet.countOf(otherTiles, c);
                    assertEquals(expected, PkTileSet.countOf(gs.pkTileSources().get(0), c));
                }
                return;
            }
        }
    }

    // ===================== endRound =====================

    @Test
    void mutableGameStateEndRoundEmptiesFullPatternLines() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        var gs = initialState(2);
        gs.fillFactories(rng);
        // remplir manuellement la première ligne de motif de P1
        var pkPatterns = PkPatterns.withAddedTiles(PkPatterns.EMPTY,
                TileDestination.Pattern.PATTERN_1,
                TileDestination.Pattern.PATTERN_1.capacity(),
                TileKind.Colored.A);
        var playerStates = gs.pkPlayerStates().toArray();
        PkPlayerStates.setPkPatterns(playerStates, PlayerId.P1, pkPatterns);
        var gsModified = new MutableGameState(new ImmutableGameState(
                gs.game(), gs.pkTileBag(),
                ImmutableIntArray.copyOf(gs.pkTileSources().toArray()),
                gs.pkUniqueTileSources(),
                ImmutableIntArray.copyOf(playerStates),
                gs.currentPlayerId()));
        gsModified.endRound();
        assertEquals(0, PkPatterns.size(
                PkPlayerStates.pkPatterns(gsModified.pkPlayerStates(), PlayerId.P1),
                TileDestination.Pattern.PATTERN_1));
    }

    @Test
    void mutableGameStateEndRoundAddsPointsForFullLine() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        var gs = initialState(2);
        gs.fillFactories(rng);
        var pkPatterns = PkPatterns.withAddedTiles(PkPatterns.EMPTY,
                TileDestination.Pattern.PATTERN_1,
                TileDestination.Pattern.PATTERN_1.capacity(),
                TileKind.Colored.A);
        var playerStates = gs.pkPlayerStates().toArray();
        PkPlayerStates.setPkPatterns(playerStates, PlayerId.P1, pkPatterns);
        var gsModified = new MutableGameState(new ImmutableGameState(
                gs.game(), gs.pkTileBag(),
                ImmutableIntArray.copyOf(gs.pkTileSources().toArray()),
                gs.pkUniqueTileSources(),
                ImmutableIntArray.copyOf(playerStates),
                gs.currentPlayerId()));
        var pointsBefore = PkPlayerStates.points(gsModified.pkPlayerStates(), PlayerId.P1);
        gsModified.endRound();
        assertTrue(PkPlayerStates.points(gsModified.pkPlayerStates(), PlayerId.P1) > pointsBefore);
    }

    @Test
    void mutableGameStateEndRoundDeductsFloorPenalty() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        var gs = initialState(2);
        gs.fillFactories(rng);
        // donner 10 points à P1 et mettre 3 tuiles sur son plancher
        var playerStates = gs.pkPlayerStates().toArray();
        PkPlayerStates.addPoints(playerStates, PlayerId.P1, 10);
        var pkFloor = PkFloor.withAddedTiles(PkFloor.EMPTY, PkTileSet.of(3, TileKind.Colored.B));
        PkPlayerStates.setPkFloor(playerStates, PlayerId.P1, pkFloor);
        var gsModified = new MutableGameState(new ImmutableGameState(
                gs.game(), gs.pkTileBag(),
                ImmutableIntArray.copyOf(gs.pkTileSources().toArray()),
                gs.pkUniqueTileSources(),
                ImmutableIntArray.copyOf(playerStates),
                gs.currentPlayerId()));
        gsModified.endRound();
        // pénalité pour 3 tuiles = 4 points → 10 - 4 = 6
        assertEquals(6, PkPlayerStates.points(gsModified.pkPlayerStates(), PlayerId.P1));
    }

    @Test
    void mutableGameStateEndRoundScoreNeverNegative() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        var gs = initialState(2);
        gs.fillFactories(rng);
        // P1 a 0 points et 7 tuiles sur son plancher
        var playerStates = gs.pkPlayerStates().toArray();
        var pkFloor = PkFloor.withAddedTiles(PkFloor.EMPTY, PkTileSet.of(7, TileKind.Colored.C));
        PkPlayerStates.setPkFloor(playerStates, PlayerId.P1, pkFloor);
        var gsModified = new MutableGameState(new ImmutableGameState(
                gs.game(), gs.pkTileBag(),
                ImmutableIntArray.copyOf(gs.pkTileSources().toArray()),
                gs.pkUniqueTileSources(),
                ImmutableIntArray.copyOf(playerStates),
                gs.currentPlayerId()));
        gsModified.endRound();
        assertTrue(PkPlayerStates.points(gsModified.pkPlayerStates(), PlayerId.P1) >= 0);
    }

    @Test
    void mutableGameStateEndRoundEmptiesFloor() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        var gs = initialState(2);
        gs.fillFactories(rng);
        var playerStates = gs.pkPlayerStates().toArray();
        PkPlayerStates.addPoints(playerStates, PlayerId.P1, 10);
        var pkFloor = PkFloor.withAddedTiles(PkFloor.EMPTY, PkTileSet.of(2, TileKind.Colored.A));
        PkPlayerStates.setPkFloor(playerStates, PlayerId.P1, pkFloor);
        var gsModified = new MutableGameState(new ImmutableGameState(
                gs.game(), gs.pkTileBag(),
                ImmutableIntArray.copyOf(gs.pkTileSources().toArray()),
                gs.pkUniqueTileSources(),
                ImmutableIntArray.copyOf(playerStates),
                gs.currentPlayerId()));
        gsModified.endRound();
        assertEquals(PkFloor.EMPTY, PkPlayerStates.pkFloor(gsModified.pkPlayerStates(), PlayerId.P1));
    }

    @Test
    void mutableGameStateEndRoundReplacesMarkerInCentralArea() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        var gs = initialState(2);
        gs.fillFactories(rng);
        // mettre le marqueur sur le plancher de P1
        var playerStates = gs.pkPlayerStates().toArray();
        PkPlayerStates.addPoints(playerStates, PlayerId.P1, 10);
        var pkFloor = PkFloor.withAddedTiles(PkFloor.EMPTY,
                PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER));
        PkPlayerStates.setPkFloor(playerStates, PlayerId.P1, pkFloor);
        // retirer le marqueur de la zone centrale
        var sources = gs.pkTileSources().toArray();
        sources[0] = PkTileSet.remove(sources[0], TileKind.FIRST_PLAYER_MARKER);
        var gsModified = new MutableGameState(new ImmutableGameState(
                gs.game(), gs.pkTileBag(),
                ImmutableIntArray.copyOf(sources),
                gs.pkUniqueTileSources(),
                ImmutableIntArray.copyOf(playerStates),
                gs.currentPlayerId()));
        gsModified.endRound();
        assertEquals(1, PkTileSet.countOf(gsModified.pkTileSources().get(0), TileKind.FIRST_PLAYER_MARKER));
    }

    // ===================== endGame =====================

    @Test
    void mutableGameStateEndGameAddsFullRowBonus() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        var gs = initialState(2);
        gs.fillFactories(rng);
        var pkWall = PkWall.EMPTY;
        for (var color : TileKind.Colored.ALL)
            pkWall = PkWall.withTileAt(pkWall, TileDestination.Pattern.PATTERN_1, color);
        var playerStates = gs.pkPlayerStates().toArray();
        PkPlayerStates.setPkWall(playerStates, PlayerId.P1, pkWall);
        var sources = new int[gs.game().tileSourcesCount()];
        var gsModified = new MutableGameState(new ImmutableGameState(
                gs.game(), PkTileSet.EMPTY,
                ImmutableIntArray.copyOf(sources),
                PkIntSet32.EMPTY,
                ImmutableIntArray.copyOf(playerStates),
                gs.currentPlayerId()));
        var pointsBefore = PkPlayerStates.points(gsModified.pkPlayerStates(), PlayerId.P1);
        gsModified.endGame();
        assertEquals(pointsBefore + Points.FULL_ROW_BONUS_POINTS,
                PkPlayerStates.points(gsModified.pkPlayerStates(), PlayerId.P1));
    }

    @Test
    void mutableGameStateEndGameAddsFullColumnBonus() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        var gs = initialState(2);
        gs.fillFactories(rng);
        var pkWall = PkWall.EMPTY;
        for (var line : TileDestination.Pattern.ALL)
            pkWall = PkWall.withTileAt(pkWall, line, PkWall.colorAt(line, 0));
        var playerStates = gs.pkPlayerStates().toArray();
        PkPlayerStates.setPkWall(playerStates, PlayerId.P1, pkWall);
        var sources = new int[gs.game().tileSourcesCount()];
        var gsModified = new MutableGameState(new ImmutableGameState(
                gs.game(), PkTileSet.EMPTY,
                ImmutableIntArray.copyOf(sources),
                PkIntSet32.EMPTY,
                ImmutableIntArray.copyOf(playerStates),
                gs.currentPlayerId()));
        var pointsBefore = PkPlayerStates.points(gsModified.pkPlayerStates(), PlayerId.P1);
        gsModified.endGame();
        assertEquals(pointsBefore + Points.FULL_COLUMN_BONUS_POINTS,
                PkPlayerStates.points(gsModified.pkPlayerStates(), PlayerId.P1));
    }

    @Test
    void mutableGameStateEndGameAddsFullColorBonus() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        var gs = initialState(2);
        gs.fillFactories(rng);
        var pkWall = PkWall.EMPTY;
        for (var line : TileDestination.Pattern.ALL)
            pkWall = PkWall.withTileAt(pkWall, line, TileKind.Colored.A);
        var playerStates = gs.pkPlayerStates().toArray();
        PkPlayerStates.setPkWall(playerStates, PlayerId.P1, pkWall);
        var sources = new int[gs.game().tileSourcesCount()];
        var gsModified = new MutableGameState(new ImmutableGameState(
                gs.game(), PkTileSet.EMPTY,
                ImmutableIntArray.copyOf(sources),
                PkIntSet32.EMPTY,
                ImmutableIntArray.copyOf(playerStates),
                gs.currentPlayerId()));
        var pointsBefore = PkPlayerStates.points(gsModified.pkPlayerStates(), PlayerId.P1);
        gsModified.endGame();
        assertEquals(pointsBefore + Points.FULL_COLOR_BONUS_POINTS,
                PkPlayerStates.points(gsModified.pkPlayerStates(), PlayerId.P1));
    }

    @Test
    void mutableGameStateEndGameChecksAllPlayers() {
        var gs = initialState(4);
        var playerStates = gs.pkPlayerStates().toArray();
        // P1 : ligne complète, P2 : colonne complète, P3 : couleur complète
        var pkWall1 = PkWall.EMPTY;
        for (var color : TileKind.Colored.ALL)
            pkWall1 = PkWall.withTileAt(pkWall1, TileDestination.Pattern.PATTERN_1, color);
        PkPlayerStates.setPkWall(playerStates, PlayerId.P1, pkWall1);

        var pkWall2 = PkWall.EMPTY;
        for (var line : TileDestination.Pattern.ALL)
            pkWall2 = PkWall.withTileAt(pkWall2, line, PkWall.colorAt(line, 0));
        PkPlayerStates.setPkWall(playerStates, PlayerId.P2, pkWall2);

        var pkWall3 = PkWall.EMPTY;
        for (var line : TileDestination.Pattern.ALL)
            pkWall3 = PkWall.withTileAt(pkWall3, line, TileKind.Colored.B);
        PkPlayerStates.setPkWall(playerStates, PlayerId.P3, pkWall3);

        var sources = new int[gs.game().tileSourcesCount()];
        var gsModified = new MutableGameState(new ImmutableGameState(
                gs.game(), PkTileSet.EMPTY,
                ImmutableIntArray.copyOf(sources),
                PkIntSet32.EMPTY,
                ImmutableIntArray.copyOf(playerStates),
                gs.currentPlayerId()));
        gsModified.endGame();

        assertEquals(Points.FULL_ROW_BONUS_POINTS,
                PkPlayerStates.points(gsModified.pkPlayerStates(), PlayerId.P1));
        assertEquals(Points.FULL_COLUMN_BONUS_POINTS,
                PkPlayerStates.points(gsModified.pkPlayerStates(), PlayerId.P2));
        assertEquals(Points.FULL_COLOR_BONUS_POINTS,
                PkPlayerStates.points(gsModified.pkPlayerStates(), PlayerId.P3));
    }

    // ===================== pointsObserver =====================

    @Test
    void mutableGameStatePointsObserverIsCalledOnNewWallTile() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        var gs = initialState(2);
        gs.fillFactories(rng);
        var calledPlayers = new ArrayList<PlayerId>();
        var observer = new PointsObserver() {
            @Override
            public void newWallTile(PlayerId playerId, TileDestination.Pattern line,
                                    TileKind.Colored color, int points) {
                calledPlayers.add(playerId);
            }
        };
        var pkPatterns = PkPatterns.withAddedTiles(PkPatterns.EMPTY,
                TileDestination.Pattern.PATTERN_1,
                TileDestination.Pattern.PATTERN_1.capacity(),
                TileKind.Colored.A);
        var playerStates = gs.pkPlayerStates().toArray();
        PkPlayerStates.setPkPatterns(playerStates, PlayerId.P1, pkPatterns);
        var gsModified = new MutableGameState(new ImmutableGameState(
                gs.game(), gs.pkTileBag(),
                ImmutableIntArray.copyOf(gs.pkTileSources().toArray()),
                gs.pkUniqueTileSources(),
                ImmutableIntArray.copyOf(playerStates),
                gs.currentPlayerId()), observer);
        gsModified.endRound();
        assertTrue(calledPlayers.contains(PlayerId.P1));
    }

    @Test
    void mutableGameStatePointsObserverIsCalledOnFloorPenalty() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        var gs = initialState(2);
        gs.fillFactories(rng);
        var calledPenalties = new ArrayList<Integer>();
        var observer = new PointsObserver() {
            @Override
            public void floor(PlayerId playerId, int penalty) {
                calledPenalties.add(penalty);
            }
        };
        var playerStates = gs.pkPlayerStates().toArray();
        PkPlayerStates.addPoints(playerStates, PlayerId.P1, 10);
        var pkFloor = PkFloor.withAddedTiles(PkFloor.EMPTY, PkTileSet.of(3, TileKind.Colored.B));
        PkPlayerStates.setPkFloor(playerStates, PlayerId.P1, pkFloor);
        var gsModified = new MutableGameState(new ImmutableGameState(
                gs.game(), gs.pkTileBag(),
                ImmutableIntArray.copyOf(gs.pkTileSources().toArray()),
                gs.pkUniqueTileSources(),
                ImmutableIntArray.copyOf(playerStates),
                gs.currentPlayerId()), observer);
        gsModified.endRound();
        assertFalse(calledPenalties.isEmpty());
        assertEquals(Points.totalFloorPenalty(3), calledPenalties.getFirst());
    }
}