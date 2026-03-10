package ch.epfl.ajul;

import ch.epfl.ajul.Game.PlayerDescription;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.random.RandomGeneratorFactory;

import static ch.epfl.ajul.Game.PlayerDescription.PlayerKind.HUMAN;
import static org.junit.jupiter.api.Assertions.*;

class GameTest {
    @Test
    void gamePlayerDescriptionConstructorThrowsOnNullArgument() {
        assertThrows(NullPointerException.class, () -> {
            new PlayerDescription(null, "", HUMAN);
        });
        assertThrows(NullPointerException.class, () -> {
            new PlayerDescription(PlayerId.P1, null, HUMAN);
        });
        assertThrows(NullPointerException.class, () -> {
            new PlayerDescription(PlayerId.P1, "", null);
        });
    }

    @Test
    void gameConstructorThrowsOnTooFewPlayers() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Game(List.of());
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new Game(List.of(new PlayerDescription(PlayerId.P1, "", HUMAN)));
        });
    }

    @Test
    void gameConstructorThrowsOnIncorrectlySortedPlayers() {
        var players = Arrays.asList(
                new PlayerDescription(PlayerId.P1, "P1", HUMAN),
                new PlayerDescription(PlayerId.P2, "P2", HUMAN),
                new PlayerDescription(PlayerId.P3, "P3", HUMAN),
                new PlayerDescription(PlayerId.P4, "P4", HUMAN));
        var unmodifiablePlayers = Collections.unmodifiableList(players);

        // Note: we brute-force the generation of all possible permutations, to keep things simple.
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        var permutations = new HashSet<>(Set.of("P1P2P3P4"));
        assertDoesNotThrow(() -> { new Game(players); });
        while (permutations.size() != 24) {
            Collections.shuffle(players, rng);
            var permutationB = new StringBuilder();
            for (var p : players) permutationB.append(p.name());
            var permutation = permutationB.toString();
            if (!permutations.add(permutation)) continue;
            assertThrows(IllegalArgumentException.class, () -> {
                new Game(unmodifiablePlayers);
            });
        }
    }

    @Test
    void gamePlayerDescriptionsReturnsCorrectList() {
        var players = List.of(
                new PlayerDescription(PlayerId.P1, "P1", HUMAN),
                new PlayerDescription(PlayerId.P2, "P2", HUMAN),
                new PlayerDescription(PlayerId.P3, "P3", HUMAN),
                new PlayerDescription(PlayerId.P4, "P4", HUMAN));
        for (var n = 2; n <= 4; n += 1) {
            var subPlayers = players.subList(0, n);
            var game = new Game(subPlayers);
            assertEquals(subPlayers, game.playerDescriptions());
        }
    }

    @Test
    void gameIsImmutable() {
        var players = List.of(
                new PlayerDescription(PlayerId.P1, "P1", HUMAN),
                new PlayerDescription(PlayerId.P2, "P2", HUMAN),
                new PlayerDescription(PlayerId.P3, "P3", HUMAN),
                new PlayerDescription(PlayerId.P4, "P4", HUMAN));
        for (var n = 2; n <= 4; n += 1) {
            var subPlayers = players.subList(0, n);
            var modifiableSubPlayers = new ArrayList<>(subPlayers);
            var game = new Game(modifiableSubPlayers);
            modifiableSubPlayers.clear();
            assertEquals(subPlayers, game.playerDescriptions());

            try {
                game.playerDescriptions().clear();
            } catch (UnsupportedOperationException _) {
                // the returned list is unmodifiable, good!
            }
            assertEquals(subPlayers, game.playerDescriptions());
        }
    }

    @Test
    void gamePlayerIdsWorks() {
        var players = List.of(
                new PlayerDescription(PlayerId.P1, "P1", HUMAN),
                new PlayerDescription(PlayerId.P2, "P2", HUMAN),
                new PlayerDescription(PlayerId.P3, "P3", HUMAN),
                new PlayerDescription(PlayerId.P4, "P4", HUMAN));
        for (var n = 2; n <= 4; n += 1) {
            var subPlayers = players.subList(0, n);
            var subPlayerIds = PlayerId.ALL.subList(0, n);
            var game = new Game(subPlayers);
            assertEquals(subPlayerIds, game.playerIds());
        }
    }

    @Test
    void gamePlayersCountWorks() {
        var players = List.of(
                new PlayerDescription(PlayerId.P1, "P1", HUMAN),
                new PlayerDescription(PlayerId.P2, "P2", HUMAN),
                new PlayerDescription(PlayerId.P3, "P3", HUMAN),
                new PlayerDescription(PlayerId.P4, "P4", HUMAN));
        for (var n = 2; n <= 4; n += 1) {
            var subPlayers = players.subList(0, n);
            var game = new Game(subPlayers);
            assertEquals(n, game.playersCount());
        }
    }

    @Test
    void gameFactoriesWorks() {
        var players = List.of(
                new PlayerDescription(PlayerId.P1, "P1", HUMAN),
                new PlayerDescription(PlayerId.P2, "P2", HUMAN),
                new PlayerDescription(PlayerId.P3, "P3", HUMAN),
                new PlayerDescription(PlayerId.P4, "P4", HUMAN));
        for (var n = 2; n <= 4; n += 1) {
            var subPlayers = players.subList(0, n);
            var subFactories = TileSource.Factory.ALL.subList(0, 2 * n + 1);
            var game = new Game(subPlayers);
            assertEquals(subFactories, game.factories());
        }
    }

    @Test
    void gameFactoriesCountWorks() {
        var players = List.of(
                new PlayerDescription(PlayerId.P1, "P1", HUMAN),
                new PlayerDescription(PlayerId.P2, "P2", HUMAN),
                new PlayerDescription(PlayerId.P3, "P3", HUMAN),
                new PlayerDescription(PlayerId.P4, "P4", HUMAN));
        for (var n = 2; n <= 4; n += 1) {
            var subPlayers = players.subList(0, n);
            var game = new Game(subPlayers);
            assertEquals(2 * n + 1, game.factoriesCount());
        }
    }

    @Test
    void gameTileSourcesWorks() {
        var players = List.of(
                new PlayerDescription(PlayerId.P1, "P1", HUMAN),
                new PlayerDescription(PlayerId.P2, "P2", HUMAN),
                new PlayerDescription(PlayerId.P3, "P3", HUMAN),
                new PlayerDescription(PlayerId.P4, "P4", HUMAN));
        for (var n = 2; n <= 4; n += 1) {
            var subPlayers = players.subList(0, n);
            var subSources = TileSource.ALL.subList(0, 2 * (n + 1));
            var game = new Game(subPlayers);
            assertEquals(subSources, game.tileSources());
        }
    }

    @Test
    void gameTileSourcesCountWorks() {
        var players = List.of(
                new PlayerDescription(PlayerId.P1, "P1", HUMAN),
                new PlayerDescription(PlayerId.P2, "P2", HUMAN),
                new PlayerDescription(PlayerId.P3, "P3", HUMAN),
                new PlayerDescription(PlayerId.P4, "P4", HUMAN));
        for (var n = 2; n <= 4; n += 1) {
            var subPlayers = players.subList(0, n);
            var game = new Game(subPlayers);
            assertEquals(2 * (n + 1), game.tileSourcesCount());
        }
    }

    @Test
    void gameCentralAreaMaxSizeWorks() {
        var players = List.of(
                new PlayerDescription(PlayerId.P1, "P1", HUMAN),
                new PlayerDescription(PlayerId.P2, "P2", HUMAN),
                new PlayerDescription(PlayerId.P3, "P3", HUMAN),
                new PlayerDescription(PlayerId.P4, "P4", HUMAN));
        assertEquals(16, new Game(players.subList(0, 2)).centralAreaMaxSize());
        assertEquals(22, new Game(players.subList(0, 3)).centralAreaMaxSize());
        assertEquals(28, new Game(players.subList(0, 4)).centralAreaMaxSize());
    }
}