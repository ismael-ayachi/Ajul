package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.*;
import ch.epfl.ajul.Game.PlayerDescription;
import ch.epfl.ajul.Game.PlayerDescription.PlayerKind;
import ch.epfl.ajul.gamestate.packed.PkIntSet32;
import ch.epfl.ajul.gamestate.packed.PkMove;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.gamestate.packed.PkTileSet;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import ch.epfl.ajul.intarray.MutableIntArray;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class ImmutableGameStateTest {
    @Test
    void immutableGameStateConstructorThrowsOnNullArguments() {
        var game = new Game(List.of(
                new PlayerDescription(PlayerId.P1, "P1", PlayerKind.HUMAN),
                new PlayerDescription(PlayerId.P2, "P2", PlayerKind.HUMAN)));

        var pkTileBag = PkTileSet.EMPTY;
        var pkUniqueTileSources = PkIntSet32.EMPTY;
        var tileSources = ImmutableIntArray.copyOf(new int[game.tileSourcesCount()]);
        var playerStates = PkPlayerStates.initial(game);

        assertThrows(NullPointerException.class, () -> {
            new ImmutableGameState(null, pkTileBag, tileSources, pkUniqueTileSources, playerStates, PlayerId.P1);
        });

        assertThrows(NullPointerException.class, () -> {
            new ImmutableGameState(game, pkTileBag, null, pkUniqueTileSources, playerStates, PlayerId.P1);
        });

        assertThrows(NullPointerException.class, () -> {
            new ImmutableGameState(game, pkTileBag, tileSources, pkUniqueTileSources, null, PlayerId.P1);
        });

        assertThrows(NullPointerException.class, () -> {
            new ImmutableGameState(game, pkTileBag, tileSources, pkUniqueTileSources, playerStates, null);
        });
    }

    @Test
    void immutableGameStateInitialWorks() {
        var players = List.of(
                new PlayerDescription(PlayerId.P1, "P1", PlayerKind.HUMAN),
                new PlayerDescription(PlayerId.P2, "P2", PlayerKind.HUMAN),
                new PlayerDescription(PlayerId.P3, "P3", PlayerKind.HUMAN),
                new PlayerDescription(PlayerId.P4, "P4", PlayerKind.HUMAN));

        for (var n = 2; n <= players.size(); n += 1) {
            var game = new Game(players.subList(0, n));
            var gameState = ImmutableGameState.initial(game);

            var expectedTileSources = new int[game.tileSourcesCount()];
            Arrays.fill(expectedTileSources, PkTileSet.EMPTY);
            expectedTileSources[TileSource.CENTER_AREA.index()] =
                    PkTileSet.of(1, TileKind.FIRST_PLAYER_MARKER);

            var expectedPlayerStates = PkPlayerStates.initial(game);

            assertSame(game, gameState.game());
            assertEquals(PkTileSet.FULL_COLORED, gameState.pkTileBag());
            assertArrayEquals(expectedTileSources, gameState.pkTileSources().toArray());
            assertEquals(PkIntSet32.EMPTY, gameState.pkUniqueTileSources());
            assertArrayEquals(expectedPlayerStates.toArray(), gameState.pkPlayerStates().toArray());
            assertEquals(PlayerId.P1, gameState.currentPlayerId());
        }
    }

    @Test
    void immutableGameStateImmutableReturnsThis() {
        var game = new Game(List.of(
                new PlayerDescription(PlayerId.P1, "P1", PlayerKind.HUMAN),
                new PlayerDescription(PlayerId.P2, "P2", PlayerKind.HUMAN)));
        var gameState = ImmutableGameState.initial(game);
        assertSame(gameState, gameState.immutable());
    }

    @Test
    void immutableGameStatePlayerIdsWorks() {
        var players = List.of(
                new PlayerDescription(PlayerId.P1, "P1", PlayerKind.HUMAN),
                new PlayerDescription(PlayerId.P2, "P2", PlayerKind.HUMAN),
                new PlayerDescription(PlayerId.P3, "P3", PlayerKind.HUMAN),
                new PlayerDescription(PlayerId.P4, "P4", PlayerKind.HUMAN));

        for (var n = 2; n <= players.size(); n += 1) {
            var game = new Game(players.subList(0, n));
            var gameState = ImmutableGameState.initial(game);

            assertEquals(game.playerIds(), gameState.playerIds());
        }
    }

    @Test
    void immutableGameStateIsRoundOverWorksOnKnownStates() {
        var trueStatesS = """
                4|a186147|0,0,0,0,0,0,0,0,0,0|0|146cb089,2a,4198,5,723281,122,400546,7,9100691,19,108402a,3,13281,22,2866,8|P4
                2|e30f24a|0,0,0,0,0,0|0|2293691,9236ef,48,2,21281291,4a,42210,4|P2
                2|0|0,0,0,0,0,0|0|12300861,29,4673ce,23,1c11b680,92526,8396f,11|P1
                2|5102087|0,0,0,0,0,0|0|2280689,5224af,69d9,6,1c261001,93,211ae,9|P2
                2|1245240b|0,0,0,0,0,0|0|2252099,0,0,0,2110b6a1,2a,0,0|P1
                2|b38b34b|0,0,0,0,0,0|0|24023481,12,44,0,1642281,491a27,202,0|P1
                3|928a1c8|0,0,0,0,0,0,0,0|0|54db291,22b,208,0,25021089,0,20081,1,68b299,8a,104,0|P2
                3|40c2085|0,0,0,0,0,0,0,0|0|1c50a081,0,100068c,2,23491,1d925,8200c3,8,14680689,4a,218c,9|P2
                3|9106247|0,0,0,0,0,0,0,0|0|1c24b889,21,10086cd,7,1503899,21,8209c7,b,14722081,3744,238e,d|P1
                3|3041041|0,0,0,0,0,0,0,0|0|1d253461,923,100a6ef,14,33006a1,6a,8299ef,1d,15123000,24b,123cf,12|P2
                2|14514500|40000000,0,0,0,0,0|0|103080,3,0,0,103000,1,0,0|P2
                """;

        var falseStatesS = """
                4|c35124d|40000081,2002,3000001,80041,1040080,1040041,1001080,2040001,400c0,0|1ff|19,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0|P2
                4|81051c4|2000001,0,0,0,0,0,0,0,0,0|1|1100691,142,441,3,30b8a1,a2,1108,2,b499,293,824,0,1b000489,da,444,0|P3
                3|71cb289|83080,0,1001080,82000,0,1040002,0,0|2d|23000,29,12101,4,1b003080,0,24,1,8c3019,0,90,0|P1
                2|123d140e|2002002,43,0,0,0,0|3|280,a43,0,0,700019,11,0,0|P1
                2|4105103|42040002,2041,1081000,0,2001001,0|17|1280480,0,264ee,c,c680009,0,15d9,4|P1
                4|3181048|1005,0,0,0,0,0,0,0,0,0|1|22240011,0,cf9a,10,95002a1,0,1072e,7,158cb889,24b,419cd,e,1d900011,29,10830da,c|P1
                2|1044f44f|84042,0,0,0,0,0|1|3251,6a,0,0,663021,0,0,0|P2
                4|81051c4|1,0,2001001,0,0,0,0,0,0,0|5|100691,142,441,3,30b8a1,21,1108,2,b499,293,824,0,1b000489,da,444,0|P1
                4|8143108|3100002,0,0,0,0,1041040,0,0,0,0|21|132a1,62,8502,5,971a009,0,24,0,11021081,29,40104,0,300099,0,2010,0|P2
                2|40c60c4|3004081,0,0,0,2000002,0|11|1280680,29,59cb,a,1d000001,0,211ae,6|P1
                """;

        for (var trueState : trueStatesS.split("\n")) {
            var gameState = parseGameState(trueState);
            assertTrue(gameState.isRoundOver());
        }

        for (var falseState : falseStatesS.split("\n")) {
            var gameState = parseGameState(falseState);
            assertFalse(gameState.isRoundOver());
        }
    }

    @Test
    void immutableGameStateIsGameOverWorksOnKnownStates() {
        var trueStatesS = """
                2|0|40000000,0,0,0,0,0|0|1a000,0,14a53ff,25,0,0,10843ff,15|P2
                3|3041041|40000000,0,0,0,0,0,0,0|0|240440,0,140e6ff,1d,3000000,0,8a9bff,28,0,0,252bcf,1b|P2
                4|5103000|40000000,0,0,0,0,0,0,0,0,0|0|11640000,0,1063cdf,22,0,0,159e7db,26,3640000,0,822bff,26,240,0,31cb5f,21|P2
                3|2043081|40000000,0,0,0,0,0,0,0|0|a000000,0,101adff,2e,b000000,0,105abff,2f,9000,0,d39bfe,30|P2
                2|0|40000000,0,0,0,0,0|0|c0000,0,39587f,10,0,0,c42fdf,19|P2
                2|0|40000000,0,0,0,0,0|0|3080000,0,2bff,9,9000000,0,186677f,20|P1
                4|2083001|40000000,0,0,0,0,0,0,0,0,0|0|80000,0,1313bff,2a,9889000,0,19cff,25,0,0,c4f3ff,35,12280000,0,914bff,26|P1
                2|0|40000000,0,0,0,0,0|0|23000000,0,dbadf,1e,0,0,229fff,2a|P2
                3|2001084|40000000,0,0,0,0,0,0,0|0|661000,0,18c67ff,39,8a000,0,234eff,29,2c0000,0,407bff,30|P3
                3|20820c4|40000000,0,0,0,0,0,0,0|0|3000000,0,450eff,19,640,0,8ca1ff,10,0,0,30e5ff,22|P1
                """;

        var falseStatesS = """
                3|3083104|40000000,0,0,0,0,0,0,0|0|14080000,0,15ace,1a,880000,0,108239a,d,21680000,0,21d8d,10|P2
                3|4103044|40000000,0,0,0,0,0,0,0|0|6e2000,0,18011da,12,4c0000,0,211996,10,192c0000,0,129ce,14|P3
                2|820a10a|40000000,0,0,0,0,0|0|1280000,0,69d9,6,1a000000,0,211ae,9|P2
                4|b145147|40000000,0,0,0,0,0,0,0,0,0|0|0,0,1079c,d,1b000000,0,655d5,8,11000000,0,86136,2,8c1000,0,9ce,11|P2
                3|a14724d|40000000,0,0,0,0,0,0,0|0|23000000,0,5022c,6,c440000,0,586,1,13682000,0,926,3|P1
                2|724814b|40000000,0,0,0,0,0|0|6c0000,0,200dd6,c,c2000,0,80239a,6|P2
                3|40c2085|40000000,0,0,0,0,0,0,0|0|1c00a000,0,10086cd,7,0,0,8209c7,b,14680000,0,238e,d|P2
                4|d38d30c|40000000,0,0,0,0,0,0,0,0,0|0|1a009000,0,88,0,480000,0,2044,3,0,0,20481,3,80000,0,904,1|P4
                2|d20e34c|40000000,0,0,0,0,0|0|b000000,0,2622a,9,21000,0,440249,2|P2
                3|e38c48e|40000000,0,0,0,0,0,0,0|0|2680000,0,1110,2,0,0,10902,4,22000,0,104,0|P2
                3|81c618b|101041,1041040,0,0,0,40081,0,1041040|23|2268a080,29,1041136,2,15000021,0,12b8e,b,249840,0,c009d5,a|P1
                3|5083045|c2040,0,0,0,0,0,0,40042|81|2400b491,0,184a0a,7,3000099,29,9185,a,11682889,0,e4c,5|P2
                4|3206107|40000000,1002001,2002000,1081,3001,1040080,1040041,82,1040002,2040040|3fe|4a2000,0,401138,5,22680000,0,20311,3,0,0,4330a,f,a00a000,0,84d2,8|P2
                3|113cf38b|10c0181,0,0,1041001,0,0,0,0|9|3100000,0,0,0,499,11,0,0,21011,29,0,0|P3
                2|b24830c|40000000,1002040,1080040,1040041,1041040,1041001|3e|0,0,30f77d,18,480000,0,8016ef,a|P1
                4|1146141|106,0,0,0,0,0,0,0,0,0|1|18d28a1,0,39cd,16,1d00b681,1,11c6e,15,2913841,0,1b8e,12,889,29,10cddc,f|P4
                4|0|0,0,40081,0,0,0,0,40081,0,0|4|214d3680,0,601dfe,1c,46a3091,243,8207bb,f,130222a1,29,c736f,26,a6e3641,1,4a5fe,1c|P4
                3|81c618b|1410c1,1041040,0,0,0,0,0,1041040|3|2268a081,29,1041136,2,15000021,0,12b8e,b,249840,0,c009d5,a|P2
                2|8184349|410410c1,0,1040080,0,2001001,0|15|15000000,0,530c,6,24459011,0,22a,0|P1
                2|1238f4ce|1000,0,0,0,0,0|1|13003699,d802e,0,0,19889,11,0,0|P2
                """;

        for (var trueState : trueStatesS.split("\n")) {
            var gameState = parseGameState(trueState);
            assertTrue(gameState.isGameOver());
        }

        for (var falseState : falseStatesS.split("\n")) {
            var gameState = parseGameState(falseState);
            assertFalse(gameState.isGameOver());
        }
    }

    @Test
    void immutableGameStatePkDiscardedTilesWorksOnKnownStates() {
        var testVectors = """
                4|6188107|40000000,0,0,0,0,0,0,0,0,0|0|0,0,8429cb,13,280000,0,80739a,10,1c000000,0,3727,c,13000840,0,11947,10|P3:7102144
                2|0|3001002,0,0,4000000,1002040,0|19|2009680,15a,859cf,f,2009880,0,4213d7,9|P2:738c308
                2|d410452|2003000,0,0,0,0,0|1|2300b019,0,0,0,201b891,29,0,0|P1:0
                2|61c9249|41000080,2040001,0,41041,1040080,81001|3b|24000000,0,42198,c,2091640,0,52c,0|P1:4105103
                3|c1082|2000,0,0,0,1081,0,0,0|11|2300699,0,425e7,10,8838a1,29,61cfcf,28,2500b680,6e3,609fe,17|P3:30c7085
                3|f34d3d0|2101041,2001040,0,0,42040,0,0,1000081|93|80440,29,0,0,1a011,0,0,0,11,0,0,0|P3:0
                3|918820d|1,0,0,0,0,0,0,0|1|2089289,e2,104,0,1371b089,2eb,400084,3,23921489,0,84,1|P3:21030c0
                2|40c50c5|40000000,0,0,0,0,0|0|12000000,0,8d2bdd,20,4009640,0,c453e,b|P2:b2c82c7
                4|71010c2|1002000,0,0,1002001,2040040,1001041,0,1003000,41041,80041|3b9|3000,29,8429cb,13,300000,0,80739a,10,1c000099,0,3727,c,13000859,0,11947,10|P4:0
                4|44042|2043080,0,0,0,40081,0,2000080,0,0,0|51|85b021,19,a439ef,24,123011,aa,8873db,1a,1c0002a1,0,13f6f,22,440021,0,21396f,25|P3:0
                3|f34d3d0|2,0,0,0,0,0,0,0|1|80491,4ab,0,0,1d01a291,9,0,0,23000291,122,0,0|P1:0
                4|e2cb34f|45040081,80080,80002,0,0,1080001,0,2080,0,3040|2a7|1,0,0,0,13000,0,0,0,680,0,0,0,11,0,0,0|P1:0
                3|7284246|1084,0,0,0,0,0,0,0|1|201b880,29,421c7,b,15013681,0,418dce,18,2408a8a1,21,209ce,11|P1:0
                4|6188107|41040000,1041040,1042,c0001,40042,1001080,2000080,2001001,0,1001041|2ff|280,0,840949,7,22280000,0,6312,a,1c000000,0,3305,6,0,0,10946,b|P2:0
                3|918820d|1000,1002040,0,2001040,1080040,1040041,1001002,c0040|fb|2080280,0,104,0,1b000,29,400084,3,23000000,0,84,1|P3:21030c0
                2|e28e20e|0,0,0,0,0,0|0|12a3299,6dba4f,4041,0,1a8c1491,42,82,0|P1:c20c1
                4|71010c2|42042,0,0,0,2040040,0,0,1003000,0,80041|291|3011,aa,8429cb,13,300001,0,80739a,10,1c023099,0,3727,c,15009859,0,11947,10|P2:0
                3|41020c3|1004100,43,0,0,0,0,0,0|3|2109081,1,186,4,13022499,29,4104c6,7,23063019,0,20186,5|P2:62c4142
                2|e28e20e|30c1000,0,0,0,0,42040|21|1280299,a4b,4041,0,1a8c1000,1,82,0|P2:c20c1
                2|0|40000000,0,0,0,0,0|0|842000,0,ad2bff,33,480000,0,10c673f,20|P1:d38c34c
                """;

        for (var testVector : testVectors.split("\n")) {
            var elements = testVector.split(Pattern.quote(":"));
            var gameState = parseGameState(elements[0]);
            var expectedPkDiscardedTiles = parseIntBase16(elements[1]);
            assertEquals(expectedPkDiscardedTiles, gameState.pkDiscardedTiles());
        }
    }

    @Test
    void immutableGameStateValidMovesWorksOnKnownStates() {
        var testVectors = """
                3|4003080|2004043,0,0,0,0,2001001,0,0|21|1d022019,56dc,673b7,1d,2a1,0,42b56f,22,d000440,0,84efb,17|P3:a00ac0b0000000000000000000000000000002802b92c
                3|1043082|41000100,1001041,1003,82,0,1002001,0,0|2f|12000019,19,53777,13,23022000,0,4a11fe,11,22000880,0,e7db,1b|P1:b00f0080000000000002a80003c020b00f2a82c000a80
                3|624a185|5040004,0,0,0,0,0,0,1001002|81|1280489,0,210adc,a,1c00b011,0,610f3,14,1284a099,29,81f96,d|P3:a00c00800000000000000000000000000000000000000000000028800020
                3|1043082|11c0,0,0,0,0,0,0,0|1|12480019,501c,53777,13,23023001,903,4a11fe,11,22003891,0,e7db,1b|P1:3c880
                2|930e38b|2,2001040,1080001,2001040,0,1000081|27|11000019,0,202,0,23009440,29,40004,0|P1:b8002eb80000000b80fae02eb0002eb80fae00000002e
                3|b149204|1002006,0,1040041,81001,0,0,0,0|d|1c013009,29,23395,13,1a002019,0,2a567,14,90002a1,9,80e6b,d|P2:3a800b20e80aac00000002002002c
                3|90c7249|40000000,1080001,81001,1003000,1001080,1040041,41002,82000|fe|11680000,0,43273,8,1b022000,0,a11ae,8,0,0,6793,f|P3:39e40000e7903efb9038fbe039e00f80e40000e7903efb9000f80000000
                3|b3ce3d1|1043,0,0,1040080,0,0,1041040,1002040|c9|6a1019,29,0,0,8c0021,0,0,0,12000021,0,0,0|P2:f80db603edb6d80000000000000000fb6036000000000000000000036db6
                3|52ca20a|4006041,0,0,0,0,0,0,0|1|1164b6a1,0,40021,0,19843899,12a,84,3,a2a1,21,502,1|P2:28020820
                3|b3ce3d1|3080,0,0,1040080,0,0,1041040,0|49|6a1019,29,0,0,900021,0,0,0,12003021,0,0,0|P1:36eb2c80000000000000000dba032000000000000000000032c80
                2|930e38b|1081040,0,0,2001040,0,1000081|29|11002899,0,202,0,23009441,29,40004,0|P2:e0002c800000000e00aac000000000000000038a2ab00
                2|0|40000000,80041,2001001,1042,1040041,42001|3e|880000,0,2053bb,7,682000,0,c4219d,f|P2:2ac00da2a80c76000c31da203003602a031d80000000
                3|d30c3d4|c10c0,0,43000,0,0,0,2002000,2080000|c5|4a1,0,0,0,880009,14a,0,0,680,0,0,0|P3:f7d00003d03d000000000000000000000000000f7d000000000000f7df40
                3|4003080|40000000,0,0,0,0,0,0,0|0|0,0,467bff,2b,251000,0,42b5ff,30,1440,0,18cefb,20|P1:0
                2|f3d04cf|40000000,1040002,1041001,1001041,2080000,42001|3e|0,0,0,0,0,0,0,0|P1:3ffc0ffffc0000fc0fffffffff03ffff000fc0000000
                3|b3ce3d1|1000,0,0,0,0,0,0,0|1|136a1299,29,0,0,9008a1,0,0,0,122c36a1,0,0,0|P1:30000
                3|f3d038c|41001000,2000080,0,1042,1000042,1041001,41041,c0040|fb|1,1,0,0,0,0,0,0,0,0,0,0|P2:3f03f000ffffffffffc0fff000fff000ffffc0000000fc003f03f03f000
                2|0|3000083,0,0,0,0,0|1|880091,5494,2053bb,7,703689,0,c4219d,f|P1:3c000d30
                3|420824f|40040040,42040,1000042,0,1040041,2000041,2041000,1001041|f7|1a000000,0,2110,4,900000,0,502,0,22000,0,208,0|P3:fc0efbeffe3b000fc003beffe00efb00000003f000efb038efb000e00ec0
                3|628a1cb|40041040,1080040,40081,41002,0,1000081,2000041,2041|ef|146a1021,0,88,1,0,0,20030,3,122c0000,0,1210,4|P2:fffff4000fffd0003ffc000000003ffc0fc0fc0fffd3f03f000ffffc0
                """;

        for (var testVector : testVectors.split("\n")) {
            var elements = testVector.split(Pattern.quote(":"));
            var gameState = parseGameState(elements[0]);

            var expectedMoves = parsePkMoves(elements[1]);
            Arrays.sort(expectedMoves);

            var actualMoves = new short[Move.MAX_MOVES];
            var movesCount = gameState.validMoves(actualMoves);
            actualMoves = Arrays.copyOf(actualMoves, movesCount);
            Arrays.sort(actualMoves);

            assertArrayEquals(expectedMoves, actualMoves);
        }
    }

    @Test
    void immutableGameStateUniqueValidMovesWorksOnKnownStates() {
        var testVectors = """
                4|4206145|40040041,2002000,1080001,1080001,80041,2000080,1003000,1040002,0,3000040|2f7|4c0021,0,504,1,9000000,0,1021,2,0,0,2108,5,0,0,100041,1|P2:b4003f000000000b6f000aad02f000b4003f000bc0fea00000002dbc002ab40bc0000bc0fea
                2|4104104|1040001,0,40081,2001001,0,2001001|d|1d000000,0,1118ee,e,15892080,29,38b,0|P1:a40b80a40980b29000000029980029
                4|4206145|400c0082,2002000,1080001,1080001,0,2000080,1003000,1040002,0,0|e7|4c0021,0,504,1,9023000,0,1021,2,9,0,2108,5,0,0,100041,1|P4:fff000f3f03f000fc002f00000000000000003ffc003cfc0fc0000fc0bfc
                3|82450ca|40000000,80041,2000080,40042,1041001,1041040,1041040,1001041|be|14000000,0,309ce,12,a48a000,0,405ba,7,840000,0,4050e7,11|P1:8c0f2cb400000008e6f2c0239bc02d02602cb63000b0002602cb40000000
                2|728520a|2040000,0,1041001,1041001,0,0|5|b009,29,2518c,c,1c900091,11,2312,3|P1:32eb803a000000032e80000
                4|4001081|40000000,1081000,41041,1040041,1001080,1040002,1002001,2040001,1081000,43|2fe|14c0000,0,4047de,18,9689000,0,219ef,13,0,0,863bce,23,0,0,18c6ef,1b|P1:24d400000009a0000d660280359a0000d660289009a0024d408289359a0a00000000000
                2|4104104|40041000,0,40081,2001001,1041001,2001001|1d|1c000000,0,1118ee,e,13892080,0,38b,0|P1:29dae029a40b80a40d80b29000000000dae000
                4|324518a|3042082,0,1002040,0,0,1002040,0,0,40081,2000041|305|8a3259,15a,401105,4,680001,0,2b06,7,11019,0,11126,3,11040880,0,12983,2|P2:c80032d80f00cb6000000000000000000000000000000000000032034c80000000032f34cb6
                4|10c1042|42083103,0,43,0,41041,1040080,0,1040080,0,0|35|8c0000,0,1c019bd,3,840,0,16bcf,1f,880040,0,157af,18,15041661,0,129af,9|P1:a260350009b4d62000000000000d620000000289b4d62
                4|324518a|40000000,2080000,1002040,3000001,2001001,1002040,1040041,41041,40081,2000041|3de|880240,0,401105,4,680000,0,2b06,7,11000,0,11126,3,11000000,0,12983,2|P4:ac0028b809c0a2e027f68bab9c0a2e00000002b03d02eac0000bab03da00ae7000000000000
                3|1002082|2042080,0,0,0,1001002,1001002,0,0|11|500080,0,4a738f,18,900680,21,4011ef,11,3001,142,104bde,11|P1:35030030000000000000000000000035930c00
                4|4206145|2000082,2002000,1080001,1080001,0,0,1003000,1040002,0,0|c7|4c02a1,0,504,1,9023000,0,1021,2,9,0,2108,5,1b000,29,100041,1|P2:a6b000aa902b000000000000000000000000029ac002aa40ac0029000eea
                4|80c5186|43101081,0,0,41041,0,41041,2001001,0,0,1041001|249|3000,0,400101,2,11,0,2202,2,6c0889,9,1104,0,11000480,0,882,2|P4:a6df40b4000000000000002903d02d00000000000000002df6cb40000000000000029b7db2d
                4|4001081|40040001,1081000,41041,1040041,1001080,1040002,1002001,0,1081000,43|27f|14c0880,0,4047de,18,9689000,0,219ef,13,0,0,863bce,23,0,0,18c6ef,1b|P2:3480000000000000002102002086a000821020d0086a034800aa0d2086a800000a80020
                4|1028d30d|40082040,0,1041001,40081,1040041,0,81001,1001041,81001,1041040|2dd|1,0,0,0,a000,0,0,0,0,0,0,0,0,0,0,0|P3:ffffff000000000fc0ffffc0fff03f00000003ffc0fff03f03fffffff03f000000000ffffc0
                4|80c5186|41082,0,0,41041,0,41041,2001001,0,0,0|49|3011,0,400101,2,23011,21,2202,2,6c0889,9,1104,0,11700480,29,882,2|P3:3403403000000000000000003cd34c00000000000000000f34d30
                4|10c1042|41041083,0,43,1001041,41041,1040080,0,1040080,1041040,0|13d|8c0000,0,1c019bd,3,840,0,16bcf,1f,880000,0,157af,18,15041640,0,129af,9|P3:3dc30d00000000000000000f70034000c30d32f40c34c80000d3200000003dc30d32
                4|4206145|82,0,1080001,1080001,0,0,1003000,1040002,0,0|c5|4c02a1,0,504,1,9023011,11,1021,2,900009,0,2108,5,1b000,29,100041,1|P4:efb000e3b03b00000000000000000000000003bec0038000000000000af8
                2|728520a|2,400c0,1041001,1041001,0,2002000|27|9,29,2518c,c,1c900000,0,2312,3|P2:980940000000000000000026d65027035022000000027
                4|1028d30d|1000042,0,1041001,0,1040041,0,81001,0,81001,1041040|255|1b000001,0,0,0,a009,0,0,0,491,29,0,0,9,9,0,0|P4:fbefbe000000000000000000fbe03e00000003ef80fbe00000003efbe03e00000003e000fbe
                """;

        for (var testVector : testVectors.split("\n")) {
            var elements = testVector.split(Pattern.quote(":"));
            var gameState = parseGameState(elements[0]);

            var expectedMoves = parsePkMoves(elements[1]);
            Arrays.sort(expectedMoves);

            var actualMoves = new short[Move.MAX_MOVES];
            var movesCount = gameState.uniqueValidMoves(actualMoves);
            actualMoves = Arrays.copyOf(actualMoves, movesCount);
            Arrays.sort(actualMoves);

            assertArrayEquals(expectedMoves, actualMoves);
        }
    }

    public static ImmutableGameState parseGameState(String string) {
        var allPlayers = List.of(
                new PlayerDescription(PlayerId.P1, "P1", PlayerKind.HUMAN),
                new PlayerDescription(PlayerId.P2, "P2", PlayerKind.HUMAN),
                new PlayerDescription(PlayerId.P3, "P3", PlayerKind.HUMAN),
                new PlayerDescription(PlayerId.P4, "P4", PlayerKind.HUMAN));

        var parts = string.split(Pattern.quote("|"));
        var i = 0;
        var game = new Game(allPlayers.subList(0, parseIntBase16(parts[i++])));
        var pkTileBag = parseIntBase16(parts[i++]);
        var pkTileSources = parseIntArray(parts[i++]).immutable();
        var pkUniqueTileSources = parseIntBase16(parts[i++]);
        var pkPlayerStates = parseIntArray(parts[i++]).immutable();
        var currentPlayerId = PlayerId.valueOf(parts[i++]);
        return new ImmutableGameState(
                game,
                pkTileBag,
                pkTileSources,
                pkUniqueTileSources,
                pkPlayerStates,
                currentPlayerId);
    }

    public static int parseIntBase16(String string) {
        return Integer.parseInt(string, 16);
    }

    public static ReadOnlyIntArray parseIntArray(String string) {
        return MutableIntArray.wrapping(
                Arrays.stream(string.split(Pattern.quote(",")))
                        .mapToInt(ImmutableGameStateTest::parseIntBase16)
                        .toArray());
    }

    public static short[] parsePkMoves(String string) {
        var bitSet = new BigInteger(string, 16);
        var pkMoves = new short[Move.MAX_MOVES];
        var movesCount = 0;
        var i = 0;
        for (var source : TileSource.ALL) {
            for (var color : TileKind.Colored.ALL) {
                for (var destination : TileDestination.ALL) {
                    if (bitSet.testBit(i)) pkMoves[movesCount++] = PkMove.pack(source, color, destination);
                    i += 1;
                }
            }
        }
        return Arrays.copyOf(pkMoves, movesCount);
    }
}