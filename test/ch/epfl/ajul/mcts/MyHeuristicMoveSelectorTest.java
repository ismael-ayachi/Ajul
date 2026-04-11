package ch.epfl.ajul.mcts;

import ch.epfl.ajul.*;
import ch.epfl.ajul.gamestate.*;
import ch.epfl.ajul.gamestate.packed.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.random.RandomGeneratorFactory;

import static org.junit.jupiter.api.Assertions.*;

class MyHeuristicMoveSelectorTest {

    static Game game2P() {
        return new Game(List.of(
                new Game.PlayerDescription(PlayerId.P1, "P1", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P2, "P2", Game.PlayerDescription.PlayerKind.HUMAN)));
    }

    static Game game4P() {
        return new Game(List.of(
                new Game.PlayerDescription(PlayerId.P1, "P1", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P2, "P2", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P3, "P3", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P4, "P4", Game.PlayerDescription.PlayerKind.HUMAN)));
    }

    @Test
    void selectMoveReturnsValidIndex2Players() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        var mgs = new MutableGameState(ImmutableGameState.initial(game2P()));
        mgs.fillFactories(rng);

        var moves = new short[Move.MAX_MOVES];
        var count = mgs.validMoves(moves);
        assertTrue(count > 0);

        var index = HeuristicMoveSelector.selectMove(rng, mgs, moves, count);
        assertTrue(index >= 0 && index < count);
    }

    @Test
    void selectMoveReturnsValidIndex4Players() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        var mgs = new MutableGameState(ImmutableGameState.initial(game4P()));
        mgs.fillFactories(rng);

        var moves = new short[Move.MAX_MOVES];
        var count = mgs.validMoves(moves);
        assertTrue(count > 0);

        var index = HeuristicMoveSelector.selectMove(rng, mgs, moves, count);
        assertTrue(index >= 0 && index < count);
    }

    @Test
    void selectMoveWithSingleMoveAlwaysReturns0() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        var mgs = new MutableGameState(ImmutableGameState.initial(game2P()));
        mgs.fillFactories(rng);

        var allMoves = new short[Move.MAX_MOVES];
        var count = mgs.validMoves(allMoves);
        assertTrue(count > 0);

        var singleMove = new short[Move.MAX_MOVES];
        singleMove[0] = allMoves[0];
        for (int i = 0; i < 100; i++)
            assertEquals(0, HeuristicMoveSelector.selectMove(rng, mgs, singleMove, 1));
    }

    @Test
    void selectMoveNeverReturnsOutOfBoundsOver500Calls() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        var mgs = new MutableGameState(ImmutableGameState.initial(game2P()));
        mgs.fillFactories(rng);

        var moves = new short[Move.MAX_MOVES];
        var count = mgs.validMoves(moves);
        assertTrue(count > 0);

        for (int i = 0; i < 500; i++) {
            var index = HeuristicMoveSelector.selectMove(rng, mgs, moves, count);
            assertTrue(index >= 0, "Index should be >= 0, got " + index);
            assertTrue(index < count, "Index should be < " + count + ", got " + index);
        }
    }

    @Test
    void selectMoveIsDeterministicWithSameSeed() {
        var rng1 = RandomGeneratorFactory.getDefault().create(2026);
        var rng2 = RandomGeneratorFactory.getDefault().create(2026);
        var mgs1 = new MutableGameState(ImmutableGameState.initial(game2P()));
        var mgs2 = new MutableGameState(ImmutableGameState.initial(game2P()));
        mgs1.fillFactories(rng1);
        mgs2.fillFactories(rng2);

        var moves1 = new short[Move.MAX_MOVES];
        var moves2 = new short[Move.MAX_MOVES];
        var count1 = mgs1.validMoves(moves1);
        var count2 = mgs2.validMoves(moves2);
        assertEquals(count1, count2);

        var idx1 = HeuristicMoveSelector.selectMove(rng1, mgs1, moves1, count1);
        var idx2 = HeuristicMoveSelector.selectMove(rng2, mgs2, moves2, count2);
        assertEquals(idx1, idx2);
    }

    @Test
    void selectMovePrefersPatternLinesOverFloor() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        var mgs = new MutableGameState(ImmutableGameState.initial(game2P()));
        mgs.fillFactories(rng);

        var moves = new short[Move.MAX_MOVES];
        var count = mgs.validMoves(moves);
        assertTrue(count > 0);

        var patternCount = 0;
        var trials = 1000;
        for (int t = 0; t < trials; t++) {
            var index = HeuristicMoveSelector.selectMove(rng, mgs, moves, count);
            var move = Move.ofPacked(moves[index]);
            if (move.destination() instanceof TileDestination.Pattern)
                patternCount++;
        }
        assertTrue(patternCount > trials / 2,
                "Expected pattern moves to be preferred, got " + patternCount + "/" + trials);
    }

    @Test
    void selectMoveWorksCorrectlyDuringMultiRoundGame() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        var mgs = new MutableGameState(ImmutableGameState.initial(game2P()));
        var moves = new short[Move.MAX_MOVES];

        for (int round = 0; round < 3 && !mgs.isGameOver(); round++) {
            mgs.fillFactories(rng);
            while (!mgs.isRoundOver()) {
                var count = mgs.validMoves(moves);
                assertTrue(count > 0);
                var index = HeuristicMoveSelector.selectMove(rng, mgs, moves, count);
                assertTrue(index >= 0 && index < count);
                mgs.registerMove(moves[index]);
            }
            mgs.endRound();
        }
    }

    @Test
    void selectMoveWorksWithUniqueValidMoves() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        var mgs = new MutableGameState(ImmutableGameState.initial(game2P()));
        mgs.fillFactories(rng);

        var moves = new short[Move.MAX_MOVES];
        var count = mgs.uniqueValidMoves(moves);
        assertTrue(count > 0);

        var index = HeuristicMoveSelector.selectMove(rng, mgs, moves, count);
        assertTrue(index >= 0 && index < count);
    }

    @Test
    void selectMoveProducesVariedResultsAcrossSeeds() {
        var mgs = new MutableGameState(ImmutableGameState.initial(game2P()));
        var rngSetup = RandomGeneratorFactory.getDefault().create(2026);
        mgs.fillFactories(rngSetup);

        var moves = new short[Move.MAX_MOVES];
        var count = mgs.validMoves(moves);
        if (count <= 1) return;

        var seenIndices = new java.util.HashSet<Integer>();
        for (int seed = 0; seed < 100; seed++) {
            var rng = RandomGeneratorFactory.getDefault().create(seed);
            seenIndices.add(HeuristicMoveSelector.selectMove(rng, mgs, moves, count));
        }
        assertTrue(seenIndices.size() > 1,
                "Expected varied results across seeds, got only " + seenIndices.size() + " distinct indices");
    }
}