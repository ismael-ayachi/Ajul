package ch.epfl.ajul.mcts;

import ch.epfl.ajul.*;
import ch.epfl.ajul.gamestate.*;
import ch.epfl.ajul.gamestate.packed.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.*;

public class MyHeuristicMoveSelectorTest {

    private static Game game2Players() {
        return new Game(List.of(
                new Game.PlayerDescription(PlayerId.P1, "Alice", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P2, "Bob", Game.PlayerDescription.PlayerKind.AI)
        ));
    }

    private static Game game4Players() {
        return new Game(List.of(
                new Game.PlayerDescription(PlayerId.P1, "Alice", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P2, "Bob", Game.PlayerDescription.PlayerKind.AI),
                new Game.PlayerDescription(PlayerId.P3, "Charlie", Game.PlayerDescription.PlayerKind.AI),
                new Game.PlayerDescription(PlayerId.P4, "Diana", Game.PlayerDescription.PlayerKind.AI)
        ));
    }

    private static MutableGameState setupGameWithFactories(Game game, RandomGenerator rng) {
        ImmutableGameState state = ImmutableGameState.initial(game);
        MutableGameState mgs = new MutableGameState(state);
        mgs.fillFactories(rng);
        return mgs;
    }

    @Test
    void selectMoveReturnsValidIndex2Players() {
        Game game = game2Players();
        RandomGenerator rng = RandomGenerator.of("L64X128MixRandom");
        MutableGameState mgs = setupGameWithFactories(game, rng);

        short[] moves = new short[Move.MAX_MOVES];
        int movesCount = mgs.validMoves(moves);

        if (movesCount > 0) {
            int index = HeuristicMoveSelector.selectMove(rng, mgs, moves, movesCount);
            assertTrue(index >= 0 && index < movesCount);
        }
    }

    @Test
    void selectMoveReturnsValidIndex4Players() {
        Game game = game4Players();
        RandomGenerator rng = RandomGenerator.of("L64X128MixRandom");
        MutableGameState mgs = setupGameWithFactories(game, rng);

        short[] moves = new short[Move.MAX_MOVES];
        int movesCount = mgs.validMoves(moves);

        if (movesCount > 0) {
            int index = HeuristicMoveSelector.selectMove(rng, mgs, moves, movesCount);
            assertTrue(index >= 0 && index < movesCount);
        }
    }

    @Test
    void selectMoveWithSingleMoveReturns0() {
        Game game = game2Players();
        RandomGenerator rng = RandomGenerator.of("L64X128MixRandom");
        MutableGameState mgs = setupGameWithFactories(game, rng);

        short[] moves = new short[Move.MAX_MOVES];
        int movesCount = mgs.validMoves(moves);

        if (movesCount > 0) {
            short[] singleMove = new short[Move.MAX_MOVES];
            singleMove[0] = moves[0];
            int index = HeuristicMoveSelector.selectMove(rng, mgs, singleMove, 1);
            assertEquals(0, index);
        }
    }

    @Test
    void selectMoveNeverReturnsOutOfBounds() {
        Game game = game2Players();
        RandomGenerator rng = RandomGenerator.of("L64X128MixRandom");
        MutableGameState mgs = setupGameWithFactories(game, rng);

        short[] moves = new short[Move.MAX_MOVES];
        int movesCount = mgs.validMoves(moves);

        if (movesCount > 0) {
            for (int i = 0; i < 500; i++) {
                int index = HeuristicMoveSelector.selectMove(rng, mgs, moves, movesCount);
                assertTrue(index >= 0, "Index should be >= 0, got " + index);
                assertTrue(index < movesCount, "Index should be < " + movesCount + ", got " + index);
            }
        }
    }

    @Test
    void selectMoveDeterministicWithSameSeed() {
        Game game = game2Players();

        MutableGameState mgs1 = setupGameWithFactories(game, RandomGenerator.of("L64X128MixRandom"));
        MutableGameState mgs2 = setupGameWithFactories(game, RandomGenerator.of("L64X128MixRandom"));

        short[] moves1 = new short[Move.MAX_MOVES];
        short[] moves2 = new short[Move.MAX_MOVES];
        int count1 = mgs1.validMoves(moves1);
        int count2 = mgs2.validMoves(moves2);

        assertEquals(count1, count2);

        if (count1 > 0) {
            RandomGenerator rng1 = RandomGenerator.of("L64X128MixRandom");
            RandomGenerator rng2 = RandomGenerator.of("L64X128MixRandom");

            int idx1 = HeuristicMoveSelector.selectMove(rng1, mgs1, moves1, count1);
            int idx2 = HeuristicMoveSelector.selectMove(rng2, mgs2, moves2, count2);
            assertEquals(idx1, idx2);
        }
    }

    @Test
    void selectMovePrefersPatternLinesOverFloor() {
        Game game = game2Players();
        RandomGenerator rng = RandomGenerator.of("L64X128MixRandom");
        MutableGameState mgs = setupGameWithFactories(game, rng);

        short[] moves = new short[Move.MAX_MOVES];
        int movesCount = mgs.validMoves(moves);

        if (movesCount > 0) {
            int patternCount = 0;
            int trials = 1000;
            for (int t = 0; t < trials; t++) {
                int index = HeuristicMoveSelector.selectMove(rng, mgs, moves, movesCount);
                Move move = Move.ofPacked(moves[index]);
                if (move.destination() instanceof TileDestination.Pattern)
                    patternCount++;
            }
            assertTrue(patternCount > trials / 2,
                    "Expected pattern moves to be preferred, got " + patternCount + "/" + trials);
        }
    }

    @Test
    void selectMoveWorksAfterSeveralRounds() {
        Game game = game2Players();
        RandomGenerator rng = RandomGenerator.of("L64X128MixRandom");
        ImmutableGameState state = ImmutableGameState.initial(game);
        MutableGameState mgs = new MutableGameState(state);
        short[] moves = new short[Move.MAX_MOVES];

        // Play a few rounds
        for (int round = 0; round < 3 && !mgs.isGameOver(); round++) {
            mgs.fillFactories(rng);
            while (!mgs.isRoundOver()) {
                int movesCount = mgs.validMoves(moves);
                if (movesCount == 0) break;
                int index = HeuristicMoveSelector.selectMove(rng, mgs, moves, movesCount);
                assertTrue(index >= 0 && index < movesCount);
                mgs.registerMove(moves[index]);
            }
            mgs.endRound();
        }
    }

    @Test
    void selectMoveWithUniqueValidMoves() {
        Game game = game2Players();
        RandomGenerator rng = RandomGenerator.of("L64X128MixRandom");
        MutableGameState mgs = setupGameWithFactories(game, rng);

        short[] moves = new short[Move.MAX_MOVES];
        int movesCount = mgs.uniqueValidMoves(moves);

        if (movesCount > 0) {
            int index = HeuristicMoveSelector.selectMove(rng, mgs, moves, movesCount);
            assertTrue(index >= 0 && index < movesCount);
        }
    }
}