package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.random.RandomGeneratorFactory;

import static ch.epfl.ajul.Game.PlayerDescription.PlayerKind.HUMAN;
import static org.junit.jupiter.api.Assertions.*;

class MyPkPlayerStatesTest {

    private final java.util.random.RandomGenerator seedGenerator =
            RandomGeneratorFactory.getDefault().create(2026);

    private static Game game2() {
        return new Game(List.of(
                new Game.PlayerDescription(PlayerId.P1, "P1", HUMAN),
                new Game.PlayerDescription(PlayerId.P2, "P2", HUMAN)));
    }

    private static Game game4() {
        return new Game(List.of(
                new Game.PlayerDescription(PlayerId.P1, "P1", HUMAN),
                new Game.PlayerDescription(PlayerId.P2, "P2", HUMAN),
                new Game.PlayerDescription(PlayerId.P3, "P3", HUMAN),
                new Game.PlayerDescription(PlayerId.P4, "P4", HUMAN)));
    }

    // ===================== initial =====================

    @Test
    void pkPlayerStatesInitialHasCorrectSize() {
        for (var n = 2; n <= 4; n += 1) {
            var players = List.of(
                    new Game.PlayerDescription(PlayerId.P1, "P1", HUMAN),
                    new Game.PlayerDescription(PlayerId.P2, "P2", HUMAN),
                    new Game.PlayerDescription(PlayerId.P3, "P3", HUMAN),
                    new Game.PlayerDescription(PlayerId.P4, "P4", HUMAN)).subList(0, n);
            var game = new Game(players);
            var state = PkPlayerStates.initial(game);
            assertEquals(4 * n, state.size());
        }
    }

    @Test
    void pkPlayerStatesInitialAllZero() {
        var game = game4();
        var state = PkPlayerStates.initial(game);
        for (var i = 0; i < state.size(); i += 1)
            assertEquals(0, state.get(i));
    }

    @Test
    void pkPlayerStatesInitialPatternsAreEmpty() {
        var game = game4();
        var state = PkPlayerStates.initial(game);
        for (var player : PlayerId.ALL.subList(0, 4))
            assertEquals(PkPatterns.EMPTY, PkPlayerStates.pkPatterns(state, player));
    }

    @Test
    void pkPlayerStatesInitialFloorIsEmpty() {
        var game = game4();
        var state = PkPlayerStates.initial(game);
        for (var player : PlayerId.ALL.subList(0, 4))
            assertEquals(PkFloor.EMPTY, PkPlayerStates.pkFloor(state, player));
    }

    @Test
    void pkPlayerStatesInitialWallIsEmpty() {
        var game = game4();
        var state = PkPlayerStates.initial(game);
        for (var player : PlayerId.ALL.subList(0, 4))
            assertEquals(PkWall.EMPTY, PkPlayerStates.pkWall(state, player));
    }

    @Test
    void pkPlayerStatesInitialPointsAreZero() {
        var game = game4();
        var state = PkPlayerStates.initial(game);
        for (var player : PlayerId.ALL.subList(0, 4))
            assertEquals(0, PkPlayerStates.points(state, player));
    }

    // ===================== pkPatterns =====================

    @Test
    void pkPlayerStatesPkPatternsReturnsCorrectValue() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        var game = game4();
        var remaining = 100;
        for (var i = 0; i < 100; i += 1) {
            var arr = new int[16];
            for (var j = 0; j < 16; j += 1) arr[j] = rng.nextInt();
            var state = ImmutableIntArray.copyOf(arr);
            for (var player : PlayerId.ALL.subList(0, 4))
                assertEquals(arr[player.ordinal() * 4], PkPlayerStates.pkPatterns(state, player));
            remaining -= 1;
        }
        assertEquals(0, remaining);
    }

    // ===================== pkFloor =====================

    @Test
    void pkPlayerStatesPkFloorReturnsCorrectValue() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        var remaining = 100;
        for (var i = 0; i < 100; i += 1) {
            var arr = new int[16];
            for (var j = 0; j < 16; j += 1) arr[j] = rng.nextInt();
            var state = ImmutableIntArray.copyOf(arr);
            for (var player : PlayerId.ALL.subList(0, 4))
                assertEquals(arr[1 + player.ordinal() * 4], PkPlayerStates.pkFloor(state, player));
            remaining -= 1;
        }
        assertEquals(0, remaining);
    }

    // ===================== pkWall =====================

    @Test
    void pkPlayerStatesPkWallReturnsCorrectValue() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        var remaining = 100;
        for (var i = 0; i < 100; i += 1) {
            var arr = new int[16];
            for (var j = 0; j < 16; j += 1) arr[j] = rng.nextInt();
            var state = ImmutableIntArray.copyOf(arr);
            for (var player : PlayerId.ALL.subList(0, 4))
                assertEquals(arr[2 + player.ordinal() * 4], PkPlayerStates.pkWall(state, player));
            remaining -= 1;
        }
        assertEquals(0, remaining);
    }

    // ===================== points =====================

    @Test
    void pkPlayerStatesPointsReturnsCorrectValue() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        var remaining = 100;
        for (var i = 0; i < 100; i += 1) {
            var arr = new int[16];
            for (var j = 0; j < 16; j += 1) arr[j] = rng.nextInt();
            var state = ImmutableIntArray.copyOf(arr);
            for (var player : PlayerId.ALL.subList(0, 4))
                assertEquals(arr[3 + player.ordinal() * 4], PkPlayerStates.points(state, player));
            remaining -= 1;
        }
        assertEquals(0, remaining);
    }

    // ===================== setPkPatterns =====================

    @Test
    void pkPlayerStatesSetPkPatternsWorks() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        var game = game4();
        var remaining = 100;
        for (var i = 0; i < 100; i += 1) {
            var arr = new int[16];
            var value = rng.nextInt();
            for (var player : PlayerId.ALL.subList(0, 4)) {
                PkPlayerStates.setPkPatterns(arr, player, value);
                assertEquals(value, arr[player.ordinal() * 4]);
            }
            remaining -= 1;
        }
        assertEquals(0, remaining);
    }

    @Test
    void pkPlayerStatesSetPkPatternsDoesNotAffectOtherFields() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        var remaining = 100;
        for (var i = 0; i < 100; i += 1) {
            var arr = new int[16];
            for (var j = 0; j < 16; j += 1) arr[j] = rng.nextInt();
            var copy = arr.clone();
            var value = rng.nextInt();
            PkPlayerStates.setPkPatterns(arr, PlayerId.P1, value);
            for (var j = 1; j < 16; j += 1)
                assertEquals(copy[j], arr[j]);
            remaining -= 1;
        }
        assertEquals(0, remaining);
    }

    // ===================== setPkFloor =====================

    @Test
    void pkPlayerStatesSetPkFloorWorks() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        var remaining = 100;
        for (var i = 0; i < 100; i += 1) {
            var arr = new int[16];
            var value = rng.nextInt();
            for (var player : PlayerId.ALL.subList(0, 4)) {
                PkPlayerStates.setPkFloor(arr, player, value);
                assertEquals(value, arr[1 + player.ordinal() * 4]);
            }
            remaining -= 1;
        }
        assertEquals(0, remaining);
    }

    // ===================== setPkWall =====================

    @Test
    void pkPlayerStatesSetPkWallWorks() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        var remaining = 100;
        for (var i = 0; i < 100; i += 1) {
            var arr = new int[16];
            var value = rng.nextInt();
            for (var player : PlayerId.ALL.subList(0, 4)) {
                PkPlayerStates.setPkWall(arr, player, value);
                assertEquals(value, arr[2 + player.ordinal() * 4]);
            }
            remaining -= 1;
        }
        assertEquals(0, remaining);
    }

    // ===================== addPoints =====================

    @Test
    void pkPlayerStatesAddPointsWorks() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        var remaining = 100;
        for (var i = 0; i < 100; i += 1) {
            var arr = new int[16];
            var initial = rng.nextInt(0, 100);
            var toAdd = rng.nextInt(-50, 100);
            arr[3] = initial;
            PkPlayerStates.addPoints(arr, PlayerId.P1, toAdd);
            assertEquals(initial + toAdd, arr[3]);
            remaining -= 1;
        }
        assertEquals(0, remaining);
    }

    @Test
    void pkPlayerStatesAddPointsWorksWithNegativeValues() {
        var arr = new int[16];
        arr[3] = 10;
        PkPlayerStates.addPoints(arr, PlayerId.P1, -5);
        assertEquals(5, arr[3]);
    }

    @Test
    void pkPlayerStatesAddPointsDoesNotAffectOtherFields() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        var remaining = 100;
        for (var i = 0; i < 100; i += 1) {
            var arr = new int[16];
            for (var j = 0; j < 16; j += 1) arr[j] = rng.nextInt();
            var copy = arr.clone();
            PkPlayerStates.addPoints(arr, PlayerId.P1, 42);
            for (var j = 0; j < 16; j += 1)
                if (j != 3) assertEquals(copy[j], arr[j]);
            remaining -= 1;
        }
        assertEquals(0, remaining);
    }

    // ===================== cohérence get/set =====================

    @Test
    void pkPlayerStatesGetAfterSetIsConsistent() {
        var rng = RandomGeneratorFactory.getDefault().create(seedGenerator.nextLong());
        var remaining = 100;
        for (var i = 0; i < 100; i += 1) {
            var arr = new int[16];
            var pkPatterns = rng.nextInt();
            var pkFloor = rng.nextInt();
            var pkWall = rng.nextInt();
            var points = rng.nextInt(0, 240);
            for (var player : PlayerId.ALL.subList(0, 4)) {
                PkPlayerStates.setPkPatterns(arr, player, pkPatterns);
                PkPlayerStates.setPkFloor(arr, player, pkFloor);
                PkPlayerStates.setPkWall(arr, player, pkWall);
                PkPlayerStates.addPoints(arr, player, points);
                var state = ImmutableIntArray.copyOf(arr);
                assertEquals(pkPatterns, PkPlayerStates.pkPatterns(state, player));
                assertEquals(pkFloor, PkPlayerStates.pkFloor(state, player));
                assertEquals(pkWall, PkPlayerStates.pkWall(state, player));
                assertEquals(points, PkPlayerStates.points(state, player));
                // reset
                PkPlayerStates.setPkPatterns(arr, player, 0);
                PkPlayerStates.setPkFloor(arr, player, 0);
                PkPlayerStates.setPkWall(arr, player, 0);
                PkPlayerStates.addPoints(arr, player, -points);
            }
            remaining -= 1;
        }
        assertEquals(0, remaining);
    }
}