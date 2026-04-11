package ch.epfl.ajul;

import ch.epfl.ajul.gamestate.*;
import ch.epfl.ajul.gamestate.packed.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.random.RandomGeneratorFactory;

import static org.junit.jupiter.api.Assertions.*;

class MyRankComputerTest {

    static Game game2P() {
        return new Game(List.of(
                new Game.PlayerDescription(PlayerId.P1, "P1", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P2, "P2", Game.PlayerDescription.PlayerKind.HUMAN)));
    }

    static Game game3P() {
        return new Game(List.of(
                new Game.PlayerDescription(PlayerId.P1, "P1", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P2, "P2", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P3, "P3", Game.PlayerDescription.PlayerKind.HUMAN)));
    }

    static Game game4P() {
        return new Game(List.of(
                new Game.PlayerDescription(PlayerId.P1, "P1", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P2, "P2", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P3, "P3", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P4, "P4", Game.PlayerDescription.PlayerKind.HUMAN)));
    }

    @Test
    void allPlayersExAequo2PlayersAllRank0() {
        var mgs = new MutableGameState(ImmutableGameState.initial(game2P()));
        var ranks = new int[2];
        RankComputer.playersRank(mgs, ranks);
        assertEquals(0, ranks[0]);
        assertEquals(0, ranks[1]);
    }

    @Test
    void allPlayersExAequo4PlayersAllRank0() {
        var mgs = new MutableGameState(ImmutableGameState.initial(game4P()));
        var ranks = new int[4];
        RankComputer.playersRank(mgs, ranks);
        for (var rank : ranks)
            assertEquals(0, rank);
    }

    @Test
    void ranksAreBetween0AndPlayerCountExclusive2Players() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        var mgs = new MutableGameState(ImmutableGameState.initial(game2P()));
        playFullGame(mgs, rng);

        var ranks = new int[2];
        RankComputer.playersRank(mgs, ranks);
        for (var rank : ranks)
            assertTrue(rank >= 0 && rank < 2);
    }

    @Test
    void ranksAreBetween0AndPlayerCountExclusive4Players() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        var mgs = new MutableGameState(ImmutableGameState.initial(game4P()));
        playFullGame(mgs, rng);

        var ranks = new int[4];
        RankComputer.playersRank(mgs, ranks);
        for (var rank : ranks)
            assertTrue(rank >= 0 && rank < 4);
    }

    @Test
    void atLeastOnePlayerHasRank0After2PlayerGame() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        var mgs = new MutableGameState(ImmutableGameState.initial(game2P()));
        playFullGame(mgs, rng);

        var ranks = new int[2];
        RankComputer.playersRank(mgs, ranks);
        assertTrue(ranks[0] == 0 || ranks[1] == 0);
    }

    @Test
    void atLeastOnePlayerHasRank0After4PlayerGame() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        var mgs = new MutableGameState(ImmutableGameState.initial(game4P()));
        playFullGame(mgs, rng);

        var ranks = new int[4];
        RankComputer.playersRank(mgs, ranks);
        var hasRank0 = false;
        for (var rank : ranks)
            if (rank == 0) hasRank0 = true;
        assertTrue(hasRank0);
    }

    @Test
    void playerWithMorePointsHasBetterRank() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        var mgs = new MutableGameState(ImmutableGameState.initial(game2P()));
        playFullGame(mgs, rng);

        var p1Pts = PkPlayerStates.points(mgs.pkPlayerStates(), PlayerId.P1);
        var p2Pts = PkPlayerStates.points(mgs.pkPlayerStates(), PlayerId.P2);

        var ranks = new int[2];
        RankComputer.playersRank(mgs, ranks);

        if (p1Pts > p2Pts) {
            assertTrue(ranks[0] < ranks[1]);
        } else if (p2Pts > p1Pts) {
            assertTrue(ranks[1] < ranks[0]);
        }
    }

    @Test
    void exAequoPlayersShareSameRank() {
        // Initial state: all 0 points, 0 full rows
        var mgs = new MutableGameState(ImmutableGameState.initial(game4P()));
        var ranks = new int[4];
        RankComputer.playersRank(mgs, ranks);
        assertEquals(ranks[0], ranks[1]);
        assertEquals(ranks[1], ranks[2]);
        assertEquals(ranks[2], ranks[3]);
    }

    @Test
    void noRank1WhenAllPlayersExAequo() {
        var mgs = new MutableGameState(ImmutableGameState.initial(game4P()));
        var ranks = new int[4];
        RankComputer.playersRank(mgs, ranks);
        for (var rank : ranks)
            assertNotEquals(1, rank);
    }

    @Test
    void ranksConsistentAcrossDifferentSeeds() {
        for (int seed = 0; seed < 10; seed++) {
            var rng = RandomGeneratorFactory.getDefault().create(seed);
            var mgs = new MutableGameState(ImmutableGameState.initial(game2P()));
            playFullGame(mgs, rng);

            var ranks = new int[2];
            RankComputer.playersRank(mgs, ranks);

            assertTrue(ranks[0] >= 0 && ranks[0] < 2);
            assertTrue(ranks[1] >= 0 && ranks[1] < 2);
            assertTrue(ranks[0] == 0 || ranks[1] == 0);
        }
    }

    @Test
    void ranksWorkFor3PlayerGame() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        var mgs = new MutableGameState(ImmutableGameState.initial(game3P()));
        playFullGame(mgs, rng);

        var ranks = new int[3];
        RankComputer.playersRank(mgs, ranks);

        for (var rank : ranks)
            assertTrue(rank >= 0 && rank < 3);

        var hasRank0 = false;
        for (var rank : ranks)
            if (rank == 0) hasRank0 = true;
        assertTrue(hasRank0);
    }

    @Test
    void winnerHasRank0AndLoserHasRank1In2PlayerGame() {
        var rng = RandomGeneratorFactory.getDefault().create(42);
        var mgs = new MutableGameState(ImmutableGameState.initial(game2P()));
        playFullGame(mgs, rng);

        var p1Pts = PkPlayerStates.points(mgs.pkPlayerStates(), PlayerId.P1);
        var p2Pts = PkPlayerStates.points(mgs.pkPlayerStates(), PlayerId.P2);

        var ranks = new int[2];
        RankComputer.playersRank(mgs, ranks);

        if (p1Pts != p2Pts) {
            var winnerRank = p1Pts > p2Pts ? ranks[0] : ranks[1];
            var loserRank = p1Pts > p2Pts ? ranks[1] : ranks[0];
            assertEquals(0, winnerRank);
            assertEquals(1, loserRank);
        }
    }

    private static void playFullGame(MutableGameState mgs, java.util.random.RandomGenerator rng) {
        var moves = new short[Move.MAX_MOVES];
        var maxRounds = 20;
        var roundsPlayed = 0;
        while (!mgs.isGameOver() && roundsPlayed < maxRounds) {
            mgs.fillFactories(rng);
            while (!mgs.isRoundOver()) {
                var count = mgs.validMoves(moves);
                if (count == 0) break;
                mgs.registerMove(moves[rng.nextInt(count)]);
            }
            mgs.endRound();
            roundsPlayed++;
        }
        mgs.endGame();
    }
}