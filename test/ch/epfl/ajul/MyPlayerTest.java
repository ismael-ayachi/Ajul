package ch.epfl.ajul;

import ch.epfl.ajul.gamestate.*;
import ch.epfl.ajul.gamestate.packed.*;
import ch.epfl.ajul.mcts.HeuristicMoveSelector;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.random.RandomGeneratorFactory;

import static org.junit.jupiter.api.Assertions.*;

class MyPlayerTest {

    static Game game2P() {
        return new Game(List.of(
                new Game.PlayerDescription(PlayerId.P1, "P1", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P2, "P2", Game.PlayerDescription.PlayerKind.HUMAN)));
    }

    @Test
    void playerCanBeImplementedAsLambda() {
        Player player = (gameState) -> {
            var moves = new short[Move.MAX_MOVES];
            var count = gameState.validMoves(moves);
            return Move.ofPacked(moves[0]);
        };
        assertNotNull(player);
    }

    @Test
    void playerLambdaReturnsNonNullMove() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        var mgs = new MutableGameState(ImmutableGameState.initial(game2P()));
        mgs.fillFactories(rng);

        Player player = (gameState) -> {
            var moves = new short[Move.MAX_MOVES];
            var count = gameState.validMoves(moves);
            return Move.ofPacked(moves[0]);
        };

        var move = player.nextMove(mgs);
        assertNotNull(move);
        assertNotNull(move.source());
        assertNotNull(move.tileColor());
        assertNotNull(move.destination());
    }

    @Test
    void playerWithHeuristicReturnsValidMove() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        var mgs = new MutableGameState(ImmutableGameState.initial(game2P()));
        mgs.fillFactories(rng);

        Player player = (gameState) -> {
            var moves = new short[Move.MAX_MOVES];
            var count = gameState.uniqueValidMoves(moves);
            var index = HeuristicMoveSelector.selectMove(rng, gameState, moves, count);
            return Move.ofPacked(moves[index]);
        };

        assertNotNull(player.nextMove(mgs));
    }

    @Test
    void twoRandomPlayersCanPlayFullGame() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        var mgs = new MutableGameState(ImmutableGameState.initial(game2P()));

        Player p1 = (gameState) -> {
            var moves = new short[Move.MAX_MOVES];
            var count = gameState.validMoves(moves);
            return Move.ofPacked(moves[rng.nextInt(count)]);
        };
        Player p2 = (gameState) -> {
            var moves = new short[Move.MAX_MOVES];
            var count = gameState.validMoves(moves);
            return Move.ofPacked(moves[rng.nextInt(count)]);
        };
        Player[] players = {p1, p2};

        var maxRounds = 20;
        var roundsPlayed = 0;
        while (!mgs.isGameOver() && roundsPlayed < maxRounds) {
            mgs.fillFactories(rng);
            while (!mgs.isRoundOver()) {
                var current = players[mgs.currentPlayerId().ordinal()];
                var move = current.nextMove(mgs);
                assertNotNull(move);
                mgs.registerMove(move.packed());
            }
            mgs.endRound();
            roundsPlayed++;
        }
        mgs.endGame();

        assertTrue(PkPlayerStates.points(mgs.pkPlayerStates(), PlayerId.P1) >= 0);
        assertTrue(PkPlayerStates.points(mgs.pkPlayerStates(), PlayerId.P2) >= 0);
    }

    @Test
    void differentPlayerImplementationsReturnDifferentMoves() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        var mgs = new MutableGameState(ImmutableGameState.initial(game2P()));
        mgs.fillFactories(rng);

        Player firstPlayer = (gameState) -> {
            var moves = new short[Move.MAX_MOVES];
            var count = gameState.validMoves(moves);
            return Move.ofPacked(moves[0]);
        };
        Player lastPlayer = (gameState) -> {
            var moves = new short[Move.MAX_MOVES];
            var count = gameState.validMoves(moves);
            return Move.ofPacked(moves[count - 1]);
        };

        var moves = new short[Move.MAX_MOVES];
        var count = mgs.validMoves(moves);
        if (count > 1)
            assertNotEquals(firstPlayer.nextMove(mgs), lastPlayer.nextMove(mgs));
    }

    @Test
    void heuristicPlayersCompleteGameWithPositiveScores() {
        var rng = RandomGeneratorFactory.getDefault().create(42);
        var mgs = new MutableGameState(ImmutableGameState.initial(game2P()));

        Player heuristic = (gameState) -> {
            var moves = new short[Move.MAX_MOVES];
            var count = gameState.validMoves(moves);
            var index = HeuristicMoveSelector.selectMove(rng, gameState, moves, count);
            return Move.ofPacked(moves[index]);
        };
        Player[] players = {heuristic, heuristic};

        var maxRounds = 20;
        var roundsPlayed = 0;
        while (!mgs.isGameOver() && roundsPlayed < maxRounds) {
            mgs.fillFactories(rng);
            while (!mgs.isRoundOver()) {
                var current = players[mgs.currentPlayerId().ordinal()];
                mgs.registerMove(current.nextMove(mgs).packed());
            }
            mgs.endRound();
            roundsPlayed++;
        }
        mgs.endGame();

        var p1Pts = PkPlayerStates.points(mgs.pkPlayerStates(), PlayerId.P1);
        var p2Pts = PkPlayerStates.points(mgs.pkPlayerStates(), PlayerId.P2);
        assertTrue(p1Pts > 0 || p2Pts > 0,
                "At least one player should have positive points after a full game");
    }
}