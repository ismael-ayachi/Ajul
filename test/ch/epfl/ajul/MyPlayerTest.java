package ch.epfl.ajul;

import ch.epfl.ajul.gamestate.*;
import ch.epfl.ajul.gamestate.packed.*;
import ch.epfl.ajul.mcts.HeuristicMoveSelector;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.*;

public class MyPlayerTest {

    private static Game game2Players() {
        return new Game(List.of(
                new Game.PlayerDescription(PlayerId.P1, "Alice", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P2, "Bob", Game.PlayerDescription.PlayerKind.AI)
        ));
    }

    @Test
    void playerCanBeImplementedAsLambda() {
        Player player = (gameState) -> {
            short[] moves = new short[Move.MAX_MOVES];
            int count = gameState.validMoves(moves);
            return Move.ofPacked(moves[0]);
        };
        assertNotNull(player);
    }

    @Test
    void playerLambdaReturnsValidMove() {
        Game game = game2Players();
        RandomGenerator rng = RandomGenerator.of("L64X128MixRandom");
        MutableGameState mgs = new MutableGameState(ImmutableGameState.initial(game));
        mgs.fillFactories(rng);

        Player player = (gameState) -> {
            short[] moves = new short[Move.MAX_MOVES];
            int count = gameState.validMoves(moves);
            return Move.ofPacked(moves[0]);
        };

        Move move = player.nextMove(mgs);
        assertNotNull(move);
        assertNotNull(move.source());
        assertNotNull(move.tileColor());
        assertNotNull(move.destination());
    }

    @Test
    void playerWithHeuristicReturnsValidMove() {
        Game game = game2Players();
        RandomGenerator rng = RandomGenerator.of("L64X128MixRandom");
        MutableGameState mgs = new MutableGameState(ImmutableGameState.initial(game));
        mgs.fillFactories(rng);

        Player heuristicPlayer = (gameState) -> {
            short[] moves = new short[Move.MAX_MOVES];
            int count = gameState.uniqueValidMoves(moves);
            int index = HeuristicMoveSelector.selectMove(rng, gameState, moves, count);
            return Move.ofPacked(moves[index]);
        };

        Move move = heuristicPlayer.nextMove(mgs);
        assertNotNull(move);
    }

    @Test
    void playerCanPlayFullGame() {
        Game game = game2Players();
        RandomGenerator rng = RandomGenerator.of("L64X128MixRandom");
        MutableGameState mgs = new MutableGameState(ImmutableGameState.initial(game));

        Player p1 = (gameState) -> {
            short[] moves = new short[Move.MAX_MOVES];
            int count = gameState.validMoves(moves);
            return Move.ofPacked(moves[rng.nextInt(count)]);
        };

        Player p2 = (gameState) -> {
            short[] moves = new short[Move.MAX_MOVES];
            int count = gameState.validMoves(moves);
            return Move.ofPacked(moves[rng.nextInt(count)]);
        };

        Player[] players = {p1, p2};
        int maxRounds = 20;
        int roundsPlayed = 0;

        while (!mgs.isGameOver() && roundsPlayed < maxRounds) {
            mgs.fillFactories(rng);
            while (!mgs.isRoundOver()) {
                Player currentPlayer = players[mgs.currentPlayerId().ordinal()];
                Move move = currentPlayer.nextMove(mgs);
                assertNotNull(move);
                mgs.registerMove(move.packed());
            }
            mgs.endRound();
            roundsPlayed++;
        }
        mgs.endGame();

        int p1Points = PkPlayerStates.points(mgs.pkPlayerStates(), PlayerId.P1);
        int p2Points = PkPlayerStates.points(mgs.pkPlayerStates(), PlayerId.P2);
        assertTrue(p1Points >= 0);
        assertTrue(p2Points >= 0);
    }

    @Test
    void differentPlayerImplementationsReturnDifferentMoves() {
        Game game = game2Players();
        RandomGenerator rng = RandomGenerator.of("L64X128MixRandom");
        MutableGameState mgs = new MutableGameState(ImmutableGameState.initial(game));
        mgs.fillFactories(rng);

        // Player that always picks first move
        Player firstMovePlayer = (gameState) -> {
            short[] moves = new short[Move.MAX_MOVES];
            int count = gameState.validMoves(moves);
            return Move.ofPacked(moves[0]);
        };

        // Player that always picks last move
        Player lastMovePlayer = (gameState) -> {
            short[] moves = new short[Move.MAX_MOVES];
            int count = gameState.validMoves(moves);
            return Move.ofPacked(moves[count - 1]);
        };

        Move firstMove = firstMovePlayer.nextMove(mgs);
        Move lastMove = lastMovePlayer.nextMove(mgs);

        // With multiple valid moves, first and last should differ
        short[] moves = new short[Move.MAX_MOVES];
        int count = mgs.validMoves(moves);
        if (count > 1) {
            assertNotEquals(firstMove, lastMove);
        }
    }
}