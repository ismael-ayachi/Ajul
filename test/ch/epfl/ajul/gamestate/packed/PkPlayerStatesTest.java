package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.intarray.MutableIntArray;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.random.RandomGeneratorFactory;

import static org.junit.jupiter.api.Assertions.*;

class PkPlayerStatesTest {
    @Test
    void pkPlayerStatesInitialWorks() {
        var playerDescriptions = List.of(
                new Game.PlayerDescription(PlayerId.P1, "P1", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P2, "P2", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P3, "P3", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P4, "P4", Game.PlayerDescription.PlayerKind.HUMAN));

        for (var n = 2; n <= playerDescriptions.size(); n += 1) {
            var game = new Game(playerDescriptions.subList(0, n));
            var actualInitialState = PkPlayerStates.initial(game);
            var expectedInitialState = new int[4 * n];
            assertArrayEquals(expectedInitialState, actualInitialState.toArray());
        }
    }

    @Test
    void pkPlayerStatesPkPatternsWorks() {
        var pkPatterns = 0b010_010_001_001;
        for (var n = 2; n <= 4; n += 1) {
            for (var playerId : PlayerId.ALL.subList(0, n)) {
                var pkPlayerStates = new int[4 * n];
                pkPlayerStates[4 * playerId.ordinal()] = pkPatterns;
                var pkPlayerStatesA = MutableIntArray.wrapping(pkPlayerStates);
                assertEquals(pkPatterns, PkPlayerStates.pkPatterns(pkPlayerStatesA, playerId));
            }
        }
    }

    @Test
    void pkPlayerStatesPkFloorWorks() {
        var pkFloor = 0b111;
        for (var n = 2; n <= 4; n += 1) {
            for (var playerId : PlayerId.ALL.subList(0, n)) {
                var pkPlayerStates = new int[4 * n];
                pkPlayerStates[4 * playerId.ordinal() + 1] = pkFloor;
                var pkPlayerStatesA = MutableIntArray.wrapping(pkPlayerStates);
                assertEquals(pkFloor, PkPlayerStates.pkFloor(pkPlayerStatesA, playerId));
            }
        }
    }

    @Test
    void pkPlayerStatesPkWallWorks() {
        var pkWall = 0b10100_00110_01110_11111_00110;
        for (var n = 2; n <= 4; n += 1) {
            for (var playerId : PlayerId.ALL.subList(0, n)) {
                var pkPlayerStates = new int[4 * n];
                pkPlayerStates[4 * playerId.ordinal() + 2] = pkWall;
                var pkPlayerStatesA = MutableIntArray.wrapping(pkPlayerStates);
                assertEquals(pkWall, PkPlayerStates.pkWall(pkPlayerStatesA, playerId));
            }
        }
    }

    @Test
    void pkPlayerStatesPointsWorks() {
        var points = 26;
        for (var n = 2; n <= 4; n += 1) {
            for (var playerId : PlayerId.ALL.subList(0, n)) {
                var pkPlayerStates = new int[4 * n];
                pkPlayerStates[4 * playerId.ordinal() + 3] = points;
                var pkPlayerStatesA = MutableIntArray.wrapping(pkPlayerStates);
                assertEquals(points, PkPlayerStates.points(pkPlayerStatesA, playerId));
            }
        }
    }

    @Test
    void pkPlayerStatesSetPkPatternsWorks() {
        var pkPatterns = 0b010_010_001_001;
        for (var n = 2; n <= 4; n += 1) {
            for (var playerId : PlayerId.ALL.subList(0, n)) {
                var expectedPkPlayerStates = new int[4 * n];
                expectedPkPlayerStates[4 * playerId.ordinal()] = pkPatterns;

                var actualPkPlayerStates = new int[4 * n];
                PkPlayerStates.setPkPatterns(actualPkPlayerStates, playerId, pkPatterns);

                assertArrayEquals(expectedPkPlayerStates, actualPkPlayerStates);
            }
        }
    }

    @Test
    void pkPlayerStatesSetPkFloorWorks() {
        var pkFloor = 0b111;
        for (var n = 2; n <= 4; n += 1) {
            for (var playerId : PlayerId.ALL.subList(0, n)) {
                var expectedPkPlayerStates = new int[4 * n];
                expectedPkPlayerStates[4 * playerId.ordinal() + 1] = pkFloor;

                var actualPkPlayerStates = new int[4 * n];
                PkPlayerStates.setPkFloor(actualPkPlayerStates, playerId, pkFloor);

                assertArrayEquals(expectedPkPlayerStates, actualPkPlayerStates);
            }
        }
    }

    @Test
    void pkPlayerStatesSetPkWallWorks() {
        var pkWall = 0b10100_00110_01110_11111_00110;
        for (var n = 2; n <= 4; n += 1) {
            for (var playerId : PlayerId.ALL.subList(0, n)) {
                var expectedPkPlayerStates = new int[4 * n];
                expectedPkPlayerStates[4 * playerId.ordinal() + 2] = pkWall;

                var actualPkPlayerStates = new int[4 * n];
                PkPlayerStates.setPkWall(actualPkPlayerStates, playerId, pkWall);

                assertArrayEquals(expectedPkPlayerStates, actualPkPlayerStates);
            }
        }
    }

    @Test
    void pkPlayerStatesAddPointsWorks() {
        var rng = RandomGeneratorFactory.getDefault().create(2026);
        var pkWall = 0;
        for (var n = 2; n <= 4; n += 1) {
            for (var playerId : PlayerId.ALL.subList(0, n)) {
                var expectedPkPlayerStates = new int[4 * n];
                expectedPkPlayerStates[4 * playerId.ordinal() + 2] = pkWall;

                var actualPkPlayerStates = new int[4 * n];
                PkPlayerStates.setPkWall(actualPkPlayerStates, playerId, pkWall);

                for (var i = rng.nextInt(10); i >= 0; i -= 1) {
                    var points = rng.nextInt(10);
                    expectedPkPlayerStates[4 * playerId.ordinal() + 3] += points;
                    PkPlayerStates.addPoints(actualPkPlayerStates, playerId, points);
                }

                assertArrayEquals(expectedPkPlayerStates, actualPkPlayerStates);
            }
        }
    }
}