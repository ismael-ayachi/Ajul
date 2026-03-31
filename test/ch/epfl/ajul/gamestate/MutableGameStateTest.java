package ch.epfl.ajul.gamestate;

import ch.epfl.ajul.*;
import ch.epfl.ajul.gamestate.packed.PkFloor;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.gamestate.packed.PkTileSet;
import ch.epfl.ajul.intarray.MutableIntArray;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.random.RandomGeneratorFactory;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class MutableGameStateTest {
    @Test
    void mutableGameStateConstructorCorrectlyCopiesState() {
        var testVectors = """
                3|20c5045|40000000,0,0,0,0,0,0,0|0|1000000,0,30dd3,11,14000000,0,5297c,8,148c0040,0,10120e,9|P2
                4|60830c5|40000000,0,0,0,0,0,0,0,0,0|0|1b000000,0,10aede,1a,1b000000,0,a5aef,1f,11000000,0,5b9de,25,24000000,0,4e66f,1a|P1
                4|71811c6|40000000,0,0,0,0,0,0,0,0,0|0|9000000,0,b4ce,d,14000000,0,168cb,c,1c000000,0,219d9,d,440,0,210867,13|P3
                4|40c7083|40000000,0,0,0,0,0,0,0,0,0|0|0,0,10b4ee,17,0,0,2368db,11,0,0,4239fb,20,440,0,1210c6f,17|P2
                4|4205146|40000000,0,0,0,0,0,0,0,0,0|0|94c1000,0,191,0,40,0,8c006,7,23000000,0,20689,0,0,0,10c9,4|P1
                4|51c81c6|40000000,0,0,0,0,0,0,0,0,0|0|21440000,0,208fc,d,0,0,4191da,7,21000000,0,14a0f4,9,2000000,0,42509a,c|P2
                4|3182086|40000000,0,0,0,0,0,0,0,0,0|0|4c0000,0,1015fb,16,0,0,109c65e,13,24280000,0,24fbd,13,461000,0,31ef,16|P3
                4|b148185|40000000,0,0,0,0,0,0,0,0,0|0|880000,0,803395,e,21000,0,a10356,5,1b000000,0,1038e,a,4000000,0,4395c,c|P4
                4|7244040|40000000,0,0,0,0,0,0,0,0,0|0|23000000,0,4239cf,22,480000,0,214fdd,1b,1000000,0,224ef,13,0,0,19431cf,23|P4
                4|70450c6|40000000,0,0,0,0,0,0,0,0,0|0|11040000,0,10311cf,11,880000,0,2078ef,1e,0,0,490f6f,1a,116c0000,0,26bcf,17|P4
                4|7244143|40000000,0,0,0,0,0,0,0,0,0|0|8c0000,0,198c,b,136c0000,0,d89,9,1859000,0,206a,6,0,0,1042186,a|P3
                4|414b0c6|40000000,0,0,0,0,0,0,0,0,0|0|c891000,0,134b,8,11000000,0,308fa,c,3000000,0,9b4e,11,8c0000,0,11709c,f|P1
                4|3180103|40000000,0,0,0,0,0,0,0,0,0|0|8d1000,0,10137b,10,11000000,0,70dfe,1b,19000,0,1009bcf,1b,0,0,33719e,21|P3
                4|7145146|40000000,0,0,0,0,0,0,0,0,0|0|21462000,0,200cc,7,1a000000,0,11152,3,a000,0,1080d4,7,0,0,42500a,6|P1
                4|1431c5|40000000,0,0,0,0,0,0,0,0,0|0|11000,0,8233b7,16,0,0,a18b77,13,1b000000,0,943cf,18,0,0,1043d7e,18|P1
                4|81030ca|40000000,0,0,0,0,0,0,0,0,0|0|b000000,0,2446,5,19000000,0,25289,6,440000,0,1118c,a,24000000,0,a426,9|P3
                4|4146205|40000000,0,0,0,0,0,0,0,0,0|0|840000,0,801285,5,21000,0,a00246,0,1a000000,0,1028c,4,2000000,0,194c,a|P2
                4|6285186|40000000,0,0,0,0,0,0,0,0,0|0|0,0,c218d9,12,198c0000,0,c67a,3,12000000,0,831ce,15,0,0,521c4,d|P2
                4|3248146|40000000,0,0,0,0,0,0,0,0,0|0|0,0,102cce,12,19280000,0,25acd,10,11000000,0,1999c,12,24000000,0,4a46e,e|P2
                3|5102083|40000000,0,0,0,0,0,0,0|0|0,0,220f4e,e,2640000,0,24264e,6,9080000,0,88218b,8|P3
                """;

        for (var testVector : testVectors.split("\n")) {
            var gameState = parseGameState(testVector);
            var mutGameState = new MutableGameState(gameState);
            assertEqualsGameStateStrict(gameState, mutGameState);
        }
    }

    @Test
    void mutableGameStateFillFactoriesProducesAllPossibleValues() {
        var seenTileSets = new ArrayList<Set<String>>();
        for (var i = 0; i < 5; i += 1) seenTileSets.add(new HashSet<>());

        var game = new Game(List.of(
                new Game.PlayerDescription(PlayerId.P1, "P1", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P2, "P2", Game.PlayerDescription.PlayerKind.HUMAN)));
        var initialState = ImmutableGameState.initial(game);

        var rng = RandomGeneratorFactory.getDefault().create(2026);
        for (var i = 0; i < 8000; i += 1) {
            var state = new MutableGameState(initialState);
            state.fillFactories(rng);
            for (var j = 0; j < 5; j += 1) {
                var pkFactory = state.pkTileSources().get(1 + j);
                seenTileSets.get(j).add(PkTileSet.toString(pkFactory));
            }
        }

        for (var tileSet : seenTileSets) assertEquals(70, tileSet.size());
    }

    @Test
    void mutableGameStateFillFactoriesWorksWhenBagHasToBeRefilled() {
        var testVectors = """
                3|20c5045|40000000,0,0,0,0,0,0,0|0|1000000,0,30dd3,11,14000000,0,5297c,8,148c0040,0,10120e,9|P2
                4|60830c5|40000000,0,0,0,0,0,0,0,0,0|0|1b000000,0,10aede,1a,1b000000,0,a5aef,1f,11000000,0,5b9de,25,24000000,0,4e66f,1a|P1
                4|71811c6|40000000,0,0,0,0,0,0,0,0,0|0|9000000,0,b4ce,d,14000000,0,168cb,c,1c000000,0,219d9,d,440,0,210867,13|P3
                4|40c7083|40000000,0,0,0,0,0,0,0,0,0|0|0,0,10b4ee,17,0,0,2368db,11,0,0,4239fb,20,440,0,1210c6f,17|P2
                4|4205146|40000000,0,0,0,0,0,0,0,0,0|0|94c1000,0,191,0,40,0,8c006,7,23000000,0,20689,0,0,0,10c9,4|P1
                4|51c81c6|40000000,0,0,0,0,0,0,0,0,0|0|21440000,0,208fc,d,0,0,4191da,7,21000000,0,14a0f4,9,2000000,0,42509a,c|P2
                4|3182086|40000000,0,0,0,0,0,0,0,0,0|0|4c0000,0,1015fb,16,0,0,109c65e,13,24280000,0,24fbd,13,461000,0,31ef,16|P3
                4|b148185|40000000,0,0,0,0,0,0,0,0,0|0|880000,0,803395,e,21000,0,a10356,5,1b000000,0,1038e,a,4000000,0,4395c,c|P4
                4|7244040|40000000,0,0,0,0,0,0,0,0,0|0|23000000,0,4239cf,22,480000,0,214fdd,1b,1000000,0,224ef,13,0,0,19431cf,23|P4
                4|70450c6|40000000,0,0,0,0,0,0,0,0,0|0|11040000,0,10311cf,11,880000,0,2078ef,1e,0,0,490f6f,1a,116c0000,0,26bcf,17|P4
                4|7244143|40000000,0,0,0,0,0,0,0,0,0|0|8c0000,0,198c,b,136c0000,0,d89,9,1859000,0,206a,6,0,0,1042186,a|P3
                4|414b0c6|40000000,0,0,0,0,0,0,0,0,0|0|c891000,0,134b,8,11000000,0,308fa,c,3000000,0,9b4e,11,8c0000,0,11709c,f|P1
                4|3180103|40000000,0,0,0,0,0,0,0,0,0|0|8d1000,0,10137b,10,11000000,0,70dfe,1b,19000,0,1009bcf,1b,0,0,33719e,21|P3
                4|7145146|40000000,0,0,0,0,0,0,0,0,0|0|21462000,0,200cc,7,1a000000,0,11152,3,a000,0,1080d4,7,0,0,42500a,6|P1
                4|1431c5|40000000,0,0,0,0,0,0,0,0,0|0|11000,0,8233b7,16,0,0,a18b77,13,1b000000,0,943cf,18,0,0,1043d7e,18|P1
                4|81030ca|40000000,0,0,0,0,0,0,0,0,0|0|b000000,0,2446,5,19000000,0,25289,6,440000,0,1118c,a,24000000,0,a426,9|P3
                4|4146205|40000000,0,0,0,0,0,0,0,0,0|0|840000,0,801285,5,21000,0,a00246,0,1a000000,0,1028c,4,2000000,0,194c,a|P2
                4|6285186|40000000,0,0,0,0,0,0,0,0,0|0|0,0,c218d9,12,198c0000,0,c67a,3,12000000,0,831ce,15,0,0,521c4,d|P2
                4|3248146|40000000,0,0,0,0,0,0,0,0,0|0|0,0,102cce,12,19280000,0,25acd,10,11000000,0,1999c,12,24000000,0,4a46e,e|P2
                3|5102083|40000000,0,0,0,0,0,0,0|0|0,0,220f4e,e,2640000,0,24264e,6,9080000,0,88218b,8|P3
                """;

        for (var testVector : testVectors.split("\n")) {
            var gameState = parseGameState(testVector);

            var pkTileBag = gameState.pkTileBag();
            var pkDiscardedTiles = gameState.pkDiscardedTiles();

            var randomGenerator = RandomGeneratorFactory.getDefault().create(2026);
            for (int i = 0; i < 100; i += 1) {
                var mutGameState = new MutableGameState(gameState);
                mutGameState.fillFactories(randomGenerator);

                // Check that discarded tiles got transferred into the bag.
                assertTrue(PkTileSet.isEmpty(mutGameState.pkDiscardedTiles()));

                var pkFactoryTiles = PkTileSet.EMPTY;
                for (var factory : mutGameState.game().factories()) {
                    pkFactoryTiles = PkTileSet.union(pkFactoryTiles, mutGameState.pkTileSources().get(factory.index()));
                }

                // Check that all tiles from the bag (seem to) appear in the factories
                for (var color : TileKind.Colored.ALL)
                    assertTrue(PkTileSet.countOf(pkTileBag, color) <= PkTileSet.countOf(pkFactoryTiles, color));

                var pkExDiscardedTiles = PkTileSet.difference(pkFactoryTiles, pkTileBag);
                // Check that the remaining tiles were part of the discarded ones.
                for (var color : TileKind.Colored.ALL)
                    assertTrue(PkTileSet.countOf(pkExDiscardedTiles, color) <= PkTileSet.countOf(pkDiscardedTiles, color));
            }
        }
    }

    @Test
    void mutableGameStateFillFactoriesWorksWhenThereAreNotEnoughTiles() {
        var testVectors = """
                4|0|40000000,0,0,0,0,0,0,0,0,0|0|24000440,0,11c0477,0,23000000,0,4d864e,0,24021240,0,4de327,0,24000000,0,e53cf,1|P3
                4|6040041|40000000,0,0,0,0,0,0,0,0,0|0|144e2640,0,27487,1,b8da240,0,13a0f,0,16c2000,0,a072d,0,18c9000,0,c880cd,0|P2
                4|40041|40000000,0,0,0,0,0,0,0,0,0|0|120d2000,0,1d6f,0,144ca000,0,400677,0,22009000,0,1adbdd,8,1c8c0000,0,9830af,0|P4
                4|0|40000000,0,0,0,0,0,0,0,0,0|0|c442000,0,10b4f7e,0,a4ca240,0,1ca5a7d,0,4889000,0,a1559e,0,b0d9000,0,80f2fd,3|P4
                4|1043|40000000,0,0,0,0,0,0,0,0,0|0|24c0640,0,4278cf,0,14262000,0,70395,0,b480000,0,600fdb,0,2462000,0,1060f7,0|P1
                4|0|40000000,0,0,0,0,0,0,0,0,0|0|144c9000,0,500afb,0,224c0000,0,3ef7,0,4d1000,0,19212fb,0,138d2000,0,102fb,3|P4
                4|1002044|40000000,0,0,0,0,0,0,0,0,0|0|21000000,0,10cc7d7,2,24000240,0,1bf45e,0,238ca040,0,48001d,0,1c0d2440,0,2045d,0|P1
                4|c1040|40000000,0,0,0,0,0,0,0,0,0|0|2188a640,0,11050cf,0,28da840,0,41025e,0,232e2440,0,10002f6,0,8da000,0,1e0a0af,0|P3
                4|0|40000000,0,0,0,0,0,0,0,0,0|0|242a2000,0,3764f,0,882000,0,304cef,0,3000840,0,7235e,5,640,0,6a747b,6|P2
                4|1080040|40000000,0,0,0,0,0,0,0,0,0|0|122a2000,0,373bb,0,861000,0,2db07b,0,a480640,0,883c5d,0,682000,0,16246d7,3|P1
                4|41042|40000000,0,0,0,0,0,0,0,0,0|0|230e1040,0,50e737,2,21840000,0,10141fd,0,1b640000,0,8a06ef,0,236c0040,0,106897,0|P1
                4|1001081|40000000,0,0,0,0,0,0,0,0,0|0|1a69a000,0,8c021e,0,228c2000,0,8e38f,0,48d9000,0,4402db,0,190d2040,0,102182f,0|P3
                4|0|40000000,0,0,0,0,0,0,0,0,0|0|c889000,0,85df7d,d,280000,0,1a067b7,0,c8ca000,0,5477d,0,a6e1240,0,6567b,0|P3
                4|2002003|40000000,0,0,0,0,0,0,0,0,0|0|4c9000,0,14807c7,0,40ca000,0,a86fd,0,988a000,0,8033e,0,c840000,0,45fab,0|P4
                4|0|40000000,0,0,0,0,0,0,0,0,0|0|44c0000,0,c05bda,e,1249a440,0,5042fd,0,24a2440,0,80209e,0,2489240,0,d1b5d,0|P4
                4|0|40000000,0,0,0,0,0,0,0,0,0|0|881000,0,800f6d,0,ca240,0,21505b,0,c2c1840,0,8067dd,0,b2e2000,0,477e,0|P1
                4|1000|40000000,0,0,0,0,0,0,0,0,0|0|c0c0840,0,20fd7,0,a480000,0,40449d,0,236c1000,0,28877b,0,2200a000,0,2c67b,0|P2
                4|0|40000000,0,0,0,0,0,0,0,0,0|0|23882000,0,84bcf,0,21862840,0,28030f,0,c2a1040,0,44c3af,0,2488a000,0,913dd,0|P1
                4|20010c3|40000000,0,0,0,0,0,0,0,0,0|0|14442000,0,1812177,1,1c2a1840,0,11059b,0,214c0000,0,10216fc,0,1449a000,0,6019b,0|P4
                4|0|40000000,0,0,0,0,0,0,0,0,0|0|4ca240,0,8a403b,0,4440000,0,24754f,9,14022440,0,b26fa,0,14649440,0,1c8588b,0|P3
                """;

        for (var testVector : testVectors.split("\n")) {
            var gameState = parseGameState(testVector);
            var factoriesCount = gameState.game().factoriesCount();

            var pkAvailableTiles = PkTileSet.union(gameState.pkTileBag(), gameState.pkDiscardedTiles());
            var availableTilesCount = PkTileSet.size(pkAvailableTiles);
            assert 4 * factoriesCount > availableTilesCount;

            var randomGenerator = RandomGeneratorFactory.getDefault().create(2026);
            for (int i = 0; i < 10; i += 1) {
                var mutGameState = new MutableGameState(gameState);
                mutGameState.fillFactories(randomGenerator);

                var pkAllFactoryTiles = PkTileSet.EMPTY;
                var remainingTilesCount = availableTilesCount;
                for (var factory : gameState.game().factories()) {
                    var pkFactoryTiles = mutGameState.pkTileSources().get(factory.index());
                    assertEquals(Math.min(remainingTilesCount, 4), PkTileSet.size(pkFactoryTiles));
                    remainingTilesCount = Math.max(0, remainingTilesCount - 4);
                    pkAllFactoryTiles = PkTileSet.union(pkAllFactoryTiles, pkFactoryTiles);
                }
                assertEquals(pkAvailableTiles, pkAllFactoryTiles);
            }
        }
    }

    @Test
    void mutableGameStateRegisterMoveWorksWhenFirstPlayerMarkerIsInCenter() {
        var testVectors = """
                2|f2cf24a|40000000,1000081,80080,1080001,1002040,40081|3e|21000000,0,1044,0,0,0,12102,4|P2/3E4/2|f2cf24a|40080001,1000081,80080,0,1002040,40081|37|21000000,0,1044,0,840000,0,12102,4|P1
                4|f30d2cd|41000002,1081000,0,81040,2001040,80002,1001080,1081,41041,41041|1fb|240,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0|P2/8C1/4|f30d2cd|41040043,1081000,0,81040,2001040,80002,1001080,1081,0,41041|2fb|240,0,0,0,11,0,0,0,0,0,0,0,0,0,0,0|P3
                4|f30d2cd|41040043,1081000,0,81040,2001040,80002,1001080,1081,0,41041|2fb|240,0,0,0,11,0,0,0,0,0,0,0,0,0,0,0|P3/9C1/4|f30d2cd|41080084,1081000,0,81040,2001040,80002,1001080,1081,0,0|fb|240,0,0,0,11,0,0,0,11,0,0,0,0,0,0,0|P4
                2|60c1145|40000000,1042000,42001,2040040,1003000,41080|3e|c4c0000,0,71ab,a,11880000,0,10a6b,8|P1/1C1/2|60c1145|41040000,0,42001,2040040,1003000,41080|3d|c4c0011,11,71ab,a,11880000,0,10a6b,8|P2
                2|0|40000000,1003,2041000,82000,2001040,1080040|3e|24000000,0,435cf,1a,881000,0,212f7e,b|P2/3D5/2|0|40002000,1003,2041000,0,2001040,1080040|37|24000000,0,435cf,1a,1a881000,0,212f7e,b|P1
                2|20c51c3|40081041,0,1042000,0,1081000,1040041|35|85a000,0,20827c,0,218db000,0,18ab,0|P2/5E4/2|20c51c3|400c1082,0,1042000,0,1081000,0|15|85a000,0,20827c,0,2191b000,0,18ab,0|P1
                2|838e38a|41000041,3001000,2001001,1003,1000081,0|1f|1149a000,0,48,0,880019,0,882,1|P1/3C1/2|838e38a|41000044,3001000,2001001,0,1000081,0|17|1149a011,0,48,0,880019,0,882,1|P2
                2|62c9245|410020c2,1081,0,0,1002001,0|13|1101a680,0,806c,0,21899001,0,18aa,1|P1/4E1/2|62c9245|410040c3,1081,0,0,0,0|3|1101a6a1,0,806c,0,21899001,0,18aa,1|P2
                4|8189107|42001081,2041000,1000081,1000042,82000,1040002,0,1000042,1080040,0|13f|1c001240,0,22012,1,2000,0,210086,8,19,0,1011186,4,11,0,898a,6|P1/5A3/4|8189107|43041081,2041000,1000081,1000042,82000,0,0,1000042,1080040,0|11f|1c003240,0,22012,1,2000,0,210086,8,19,0,1011186,4,11,0,898a,6|P2
                2|61c30c1|42000001,82000,1001080,2040040,0,40081|2f|21880000,0,11c7,0,a480849,0,5a5c,5|P1/1D2/2|61c30c1|42002001,0,1001080,2040040,0,40081|2d|21880680,0,11c7,0,a480849,0,5a5c,5|P2
                2|11460c5|42000000,41041,42001,0,80080,1040080|37|3080640,0,8483c,0,1a000080,0,215b8,7|P1/5B1/2|11460c5|43040000,41041,42001,0,80080,0|17|3080649,9,8483c,0,1a000080,0,215b8,7|P2
                4|b38e28f|42080080,1040002,2002000,1000081,0,0,41080,1001080,40081,2041000|3cf|11,0,0,0,40,0,0,0,0,0,0,0,0,0,0,0|P3/7B3/4|b38e28f|43081080,1040002,2002000,1000081,0,0,41080,0,40081,2041000|34f|11,0,0,0,40,0,0,0,a000,0,0,0,0,0,0,0|P4
                2|9149248|40000000,1040041,1080001,1040080,1081,41041|3e|0,0,29298,a,230c9000,0,24c,0|P2/5A4/2|9149248|40041040,1040041,1080001,1040080,1081,0|1f|0,0,29298,a,23109000,0,24c,0|P1
                2|62c9245|40002040,1081,80002,1000081,1002001,0|1f|1101a000,0,806c,0,21899000,0,18aa,1|P1/2D2/2|62c9245|40002042,1081,0,1000081,1002001,0|1b|1101a680,0,806c,0,21899000,0,18aa,1|P2
                2|104103d1|41000001,0,1040041,1080040,3040,1041001|3d|280,0,0,0,0,0,0,0|P2/3D2/2|104103d1|42000041,0,1040041,0,3040,1041001|35|280,0,0,0,680,0,0,0|P1
                2|d34b30b|40000000,1001002,2041,1040041,81001,1001041|3e|1a88a000,0,81,0,11000000,0,4202,2|P1/5B1/2|d34b30b|41001001,1001002,2041,1040041,81001,0|1f|1a88a009,0,81,0,11000000,0,4202,2|P2
                2|d34b30b|41001001,1001002,2041,1040041,81001,0|1f|1a88a009,0,81,0,11000000,0,4202,2|P2/2A0/2|d34b30b|41003041,1001002,0,1040041,81001,0|1b|1a88a009,0,81,0,11000000,1,4202,2|P1
                2|0|40000000,41041,1040041,43000,c1,1001080|3e|880000,0,20877d,0,22000000,0,21caf,f|P1/2B1/2|0|41040001,41041,0,43000,c1,1001080|3b|880009,0,20877d,0,22000000,0,21caf,f|P2
                4|20800c4|40000000,1000081,82000,1000081,1001041,2041000,1080001,1041001,1042000,1081|3f6|19000,0,8c2fe,18,1000000,0,6dbcf,1b,12000000,0,4318db,12,0,0,1096b3e,1b|P3/2C5/4|20800c4|40080000,1000081,0,1000081,1001041,2041000,1080001,1041001,1042000,1081|3f3|19000,0,8c2fe,18,1000000,0,6dbcf,1b,14000000,0,4318db,12,0,0,1096b3e,1b|P4
                4|72041c6|41081040,40042,0,1041040,1001002,2001040,80002,1080001,1001002,0|fb|9,0,108cc,3,114c0480,0,10c3,5,280000,0,80206c,5,22240000,0,498c,9|P3/1B1/4|72041c6|410c1042,0,0,1041040,1001002,2001040,80002,1080001,1001002,0|f9|9,0,108cc,3,114c0480,0,10c3,5,280009,0,80206c,5,22240000,0,498c,9|P4
                """;

        for (var testVector : testVectors.split("\n")) {
            var moveTriplet = parseMoveTriplet(testVector);

            var actualGameState = new MutableGameState(moveTriplet.before);
            actualGameState.registerMove(moveTriplet.move.packed());

            var expectedGameState = moveTriplet.after;
            assertEqualsGameStateStrict(expectedGameState, actualGameState);
        }
    }

    @Test
    void mutableGameStateRegisterMoveWorksWhenTilesAreSortedDuringFilling() {
        var testVectors = """
                4|7148086|40000000,4,c1,100,100,4000,82000,100000,4000000,4000000|1ee|0,0,82100,4,1000000,0,8400,3,21000000,0,40200,1,0,0,40a00,0|P3/2B3/4|7148086|40000001,4,0,100,100,4000,82000,100000,4000000,4000000|1eb|0,0,82100,4,1000000,0,8400,3,2100b000,0,40200,1,0,0,40a00,0|P4
                4|f30d2cd|40040000,4,43,100,100,4000,0,100000,10c0000,4000000|3af|480,11,0,0,0,0,0,0,0,0,0,0,0,0,0,0|P2/5C3/4|f30d2cd|40040000,4,43,100,100,0,0,100000,10c0000,4000000|38f|480,11,0,0,13000,11,0,0,0,0,0,0,0,0,0,0|P3
                2|730e3cc|40000000,4,3040,10c0000,4000000,4000000|1e|0,0,1100,0,0,0,20208,0|P2/3E5/2|730e3cc|400c0000,4,3040,0,4000000,4000000|17|0,0,1100,0,21000000,0,20208,0|P1
                3|81cb28a|40001000,4,c1,3040,0,0,4000000,4000000|4f|1b000,19,9bb40,c,1901b000,0,8988c,8,0,0,7312a,11|P3/1A2/3|81cb28a|40001000,0,c1,3040,0,0,4000000,4000000|4d|1b000,19,9bb40,c,1901b000,0,8988c,8,80,2,7312a,11|P1
                3|41831c6|40000080,0,0,2080,0,100000,0,4000000|a9|1a000001,3,10cf3dc,23,25013000,11,2bf8f,18,480001,1,c63d9e,23|P3/7E2/3|41831c6|40000080,0,0,2080,0,100000,0,0|29|1a000001,3,10cf3dc,23,25013000,11,2bf8f,18,480881,903,c63d9e,23|P1
                3|30c5083|400000c0,4,0,4000,82000,100000,2080000,4000000|fb|1000000,0,c5208,6,21000000,0,2304,0,0,0,6280c,6|P2/5D1/3|30c5083|400000c0,4,0,4000,82000,0,2080000,4000000|db|1000000,0,c5208,6,21000019,6db,2304,0,0,0,6280c,6|P3
                3|f38f310|40000000,0,0,0,4000,c1000,10c0000,4000000|f0|300000,0,0,0,b000,9,0,0,100000,0,0,0|P1/5D2/3|f38f310|40001000,0,0,0,4000,0,10c0000,4000000|d1|300680,19,0,0,b000,9,0,0,100000,0,0,0|P2
                4|6142103|40003000,0,4,c1,100,0,4000,82000,2080000,4000000|3dd|0,0,719ac,17,0,0,4b9885,14,3000,1,103e390,a,1c000009,0,d78c4,15|P1/2A2/4|6142103|40003000,0,0,c1,100,0,4000,82000,2080000,4000000|3d9|80,2,719ac,17,0,0,4b9885,14,3000,1,103e390,a,1c000009,0,d78c4,15|P2
                4|9144106|400c2000,0,43,0,0,4000,0,0,0,4000000|225|b000,9,21100,2,21000480,11,4080,0,19700880,0,1080,0,9003280,1,10400,2|P1/5C4/4|9144106|400c2000,0,43,0,0,0,0,0,0,4000000|205|50b000,9,21100,2,21000480,11,4080,0,19700880,0,1080,0,9003280,1,10400,2|P2
                2|11390411|40000040,0,10c0,43000,0,3040000|2d|3000,0,0,0,1c000000,0,0,0|P1/2C0/2|11390411|40000100,0,0,43000,0,3040000|29|3000,11,0,0,1c000000,0,0,0|P2
                2|5083145|400c1000,43,100,4000,0,0|f|b000019,da,29596,e,21000000,0,465989,e|P1/1A2/2|5083145|400c1040,0,100,4000,0,0|d|b000099,db,29596,e,21000000,0,465989,e|P2
                4|60840c3|40080000,4,4,c1,10c0,0,c1000,0,0,4000000|25b|a000000,0,2bd62,15,21000880,0,6d3c4,12,1d000000,0,5b4ac,c,9013000,11,b3f84,10|P1/3B2/4|60840c3|40080001,4,4,0,10c0,0,c1000,0,0,4000000|253|a000280,9,2bd62,15,21000880,0,6d3c4,12,1d000000,0,5b4ac,c,9013000,11,b3f84,10|P2
                2|5086103|40000000,4,c1,100,43000,3040000|3e|1b000000,0,1cb8,5,0,0,449b8c,d|P1/4C2/2|5086103|40040000,4,c1,100,0,3040000|2f|1b000480,11,1cb8,5,0,0,449b8c,d|P2
                4|70c6204|42000000,82,100,4000,4000,82000,100000,100000,0,4000000|26f|19,19,44080,1,19000000,0,880,0,0,0,11010,0,0,0,1040,1|P2/1A2/4|70c6204|42000080,0,100,4000,4000,82000,100000,100000,0,4000000|26d|19,19,44080,1,19000080,0,880,0,0,0,11010,0,0,0,1040,1|P3
                4|2083083|40000000,4,82,0,3040,43000,0,0,0,4000000|236|680,da,7b9ec,27,1b000,19,4bda85,14,23000,21,10bf3b1,17,c000000,0,4f78c6,27|P2/1A4/4|2083083|40000000,0,82,0,3040,43000,0,0,0,4000000|234|680,da,7b9ec,27,11b000,19,4bda85,14,23000,21,10bf3b1,17,c000000,0,4f78c6,27|P3
                4|72c9188|40000000,0,43,100,100,3040,4000,10c0000,4000000,4000000|1ec|0,0,46188,a,19003000,1,cc8,3,0,0,19490,6,0,0,9860,7|P3/7D2/4|72c9188|41000000,0,43,100,100,3040,4000,0,4000000,4000000|16d|0,0,46188,a,19003000,1,cc8,3,680,19,19490,6,0,0,9860,7|P4
                4|d3d0386|43000000,4,4,4,82,100,0,100000,0,4000000|2b3|13000,11,0,0,19000000,0,0,0,0,0,0,0,0,0,0,0|P3/9E1/4|d3d0386|43000000,4,4,4,82,100,0,100000,0,0|b3|13000,11,0,0,19000000,0,0,0,21,923,0,0,0,0,0,0|P4
                3|30c5083|400000c0,4,0,4000,82000,0,2080000,4000000|db|1000000,0,c5208,6,21000019,6db,2304,0,0,0,6280c,6|P3/1A3/3|30c5083|400000c0,0,0,4000,82000,0,2080000,4000000|d9|1000000,0,c5208,6,21000019,6db,2304,0,3000,1,6280c,6|P1
                2|6208288|40000000,4,10c0,82000,10c0000,4000000|3e|b000000,0,29092,6,0,0,425101,6|P1/3C2/2|6208288|40080000,4,10c0,0,10c0000,4000000|37|b000480,0,29092,6,0,0,425101,6|P2
                4|7089145|40001000,0,0,82,0,0,0,c1000,0,2080000|289|3000,1,10088,2,700000,0,21004,3,150b000,0,14000,0,303000,1,880,0|P1/7C1/4|7089145|400c1000,0,0,82,0,0,0,0,0,2080000|209|3011,1,10088,2,700000,0,21004,3,150b000,0,14000,0,303000,1,880,0|P2
                """;

        for (var testVector : testVectors.split("\n")) {
            var moveTriplet = parseMoveTriplet(testVector);

            var actualGameState = new MutableGameState(moveTriplet.before);
            actualGameState.registerMove(moveTriplet.move.packed());

            var expectedGameState = moveTriplet.after;
            assertEqualsGameStateStrict(expectedGameState, actualGameState);
        }
    }

    @Test
    void mutableGameStateRegisterMoveCorrectlyMovesFirstPlayerMarker() {
        var testVectors = """
                4|5187107|41000041,1081,2000041,0,81040,1041001,3001000,2001040,3000001,0|1f7|b6c0019,da,d45,0,680000,0,20846,5,4000000,0,8414c,6,b000000,0,818a,6|P3/0B1/4|5187107|1000001,1081,2000041,0,81040,1041001,3001000,2001040,3000001,0|1f7|b6c0019,da,d45,0,680000,0,20846,5,4000009,29,8414c,6,b000000,0,818a,6|P4
                2|e28e20e|430c1100,0,0,0,0,42040|21|1280019,0,4041,0,1a8c1000,1,82,0|P1/0B2/2|e28e20e|30c1000,0,0,0,0,42040|21|1280299,a4b,4041,0,1a8c1000,1,82,0|P2
                2|820a10a|41042084,0,0,0,0,0|1|128a4a1,122,48c9,0,1a8c1899,0,186,4|P2/0E4/2|820a10a|42084,0,0,0,0,0|1|128a4a1,122,48c9,0,1a901899,29,186,4|P1
                3|60c0007|410c20c0,0,2041,2001040,41041,0,0,43000|9d|21049021,0,28c,3,1b000011,0,a0b2,3,9,0,40084c,0|P2/0D3/3|60c0007|10020c0,0,2041,2001040,41041,0,0,43000|9d|21049021,0,28c,3,1b01b011,29,a0b2,3,9,0,40084c,0|P3
                2|0|42081086,0,0,0,0,0|1|2281891,0,6bdb,4,1d261021,19,211af,b|P2/0A2/2|0|2081080,0,0,0,0,0|1|2281891,0,6bdb,4,1d2610a1,14001e,211af,b|P1
                3|f3cf3cc|42040082,0,41080,1001002,0,2040001,0,1042|ad|19,19,0,0,1,0,0,0,11,11,0,0|P1/0A0/3|f3cf3cc|2040080,0,41080,1001002,0,2040001,0,1042|ad|19,501c,0,0,1,0,0,0,11,11,0,0|P2
                3|928a1c8|42043081,0,1041001,1040041,10000c0,1080001,0,0|3d|4000011,0,208,0,9,0,20081,1,64b000,0,104,0|P1/0B2/3|928a1c8|2043001,0,1041001,1040041,10000c0,1080001,0,0|3d|4000291,29,208,0,9,0,20081,1,64b000,0,104,0|P2
                3|40c2085|45042040,0,0,81040,41080,0,0,41041|99|500080,0,100068c,2,21480,0,8200c3,8,680680,0,218c,9|P2/0E3/3|40c2085|42040,0,0,81040,41080,0,0,41041|99|500080,0,100068c,2,23480,5924,8200c3,8,680680,0,218c,9|P3
                4|30c20c2|42086102,0,0,0,0,0,1080040,1040041,0,0|c1|1b880,0,24dbcf,16,a4a1,0,b0aef,1a,21080081,0,39be,15,1840000,0,1139d,e|P4/0A2/4|30c20c2|2086100,0,0,0,0,0,1080040,1040041,0,0|c1|1b880,0,24dbcf,16,a4a1,0,b0aef,1a,21080081,0,39be,15,1840080,29,1139d,e|P1
                3|3041041|42082143,41041,0,0,0,82,0,0|23|1c240461,21,100a6ef,14,1000021,0,8299ef,1d,15023000,0,123cf,12|P2/0D2/3|3041041|2002143,41041,0,0,0,82,0,0|23|1c240461,21,100a6ef,14,10006a1,29,8299ef,1d,15023000,0,123cf,12|P3
                4|61c41c4|42040043,1000042,0,0,0,2000080,3001,81001,1041040,10c0000|3e3|640080,0,821,2,0,0,2102,2,21000011,0,4108,2,11,11,1088,0|P2/0A3/4|61c41c4|2040040,1000042,0,0,0,2000080,3001,81001,1041040,10c0000|3e3|640080,0,821,2,3000,29,2102,2,21000011,0,4108,2,11,11,1088,0|P3
                4|8105305|400c40c3,0,1001041,0,1041001,2041000,0,0,0,1041001|35|11022009,0,4414c,5,6c0881,0,80a4c,6,21080011,0,88a,0,682000,0,109,0|P3/0A3/4|8105305|c40c0,0,1001041,0,1041001,2041000,0,0,0,1041001|35|11022009,0,4414c,5,6c0881,0,80a4c,6,21083011,29,88a,0,682000,0,109,0|P4
                4|b145147|40003084,81001,0,0,0,41002,41080,0,1000081,1081|363|19680,19,10198,6,1b000280,0,24545,0,11000011,0,84032,3,8c1019,0,8c6,b|P2/0A3/4|b145147|3080,81001,0,0,0,41002,41080,0,1000081,1081|363|19680,19,10198,6,1b003280,142,24545,0,11000011,0,84032,3,8c1019,0,8c6,b|P3
                3|a20a1c9|44082046,0,1000042,0,0,0,2002000,0|45|21049011,0,88,0,1b4cb000,0,82,1,1d000019,0,44,0|P2/0C4/3|a20a1c9|4080046,0,1000042,0,0,0,2002000,0|45|21049011,0,88,0,1b50b000,152,82,1,1d000019,0,44,0|P3
                4|d38c30d|40003000,1040080,40042,80002,1040041,3040,0,0,1042000,2000002|33f|880,0,0,0,b000,0,0,0,0,0,0,0,0,0,0,0|P3/0C2/4|d38c30d|0,1040080,40042,80002,1040041,3040,0,0,1042000,2000002|33e|880,0,0,0,b000,0,0,0,480,152,0,0,0,0,0,0|P4
                4|940d2cf|40004005,2001040,3001000,0,400c0,2001040,0,10c0000,0,0|97|9,0,0,0,880,21,0,0,9,0,0,0,280,0,0,0|P1/0A4/4|940d2cf|4000,2001040,3001000,0,400c0,2001040,0,10c0000,0,0|97|100009,142,0,0,880,21,0,0,9,0,0,0,280,0,0,0|P2
                4|5101144|42001040,0,43000,41002,0,40003,1000081,1001041,2001001,1040041|3ed|b6c0000,0,1dcd,b,13680000,0,20867,c,880,0,108434e,12,1a000,0,1281ae,7|P1/0E2/4|5101144|1040,0,43000,41002,0,40003,1000081,1001041,2001001,1040041|3ed|b6c0880,29,1dcd,b,13680000,0,20867,c,880,0,108434e,12,1a000,0,1281ae,7|P2
                4|6149188|44082003,42040,1001002,0,1080040,1080040,0,1001080,0,0|97|702000,0,871,5,280,0,8314a,8,2100a000,0,412c,1,9,0,14ac,4|P2/0E3/4|6149188|82003,42040,1001002,0,1080040,1080040,0,1001080,0,0|97|702000,0,871,5,23280,162,8314a,8,2100a000,0,412c,1,9,0,14ac,4|P3
                4|41042|420420c3,1001080,0,0,c0001,2000080,0,0,2041000,0|133|14000680,0,119fb,17,13000001,0,939fe,19,220000a1,1,473af,1d,689680,0,1dfe,1a|P4/0A1/4|41042|20420c0,1001080,0,0,c0001,2000080,0,0,2041000,0|133|14000680,0,119fb,17,13000001,0,939fe,19,220000a1,1,473af,1d,689681,a03,1dfe,1a|P1
                3|2043081|410c10c3,0,0,1081,0,0,3040000,0|49|2a1,0,100ad6f,1d,680,0,101a9ef,20,224c9880,0,531bde,25|P2/0B5/3|2043081|10c1003,0,0,1081,0,0,3040000,0|49|2a1,0,100ad6f,1d,b000680,29,101a9ef,20,224c9880,0,531bde,25|P3
                """;

        for (var testVector : testVectors.split("\n")) {
            var moveTriplet = parseMoveTriplet(testVector);

            var actualGameState = new MutableGameState(moveTriplet.before);
            actualGameState.registerMove(moveTriplet.move.packed());

            var expectedGameState = moveTriplet.after;
            assertEqualsGameStateLax(expectedGameState, actualGameState);
        }
    }

    @Test
    void mutableGameStateEndRoundCorrectlyCallsObserverMethods() {
        var testVectors = """
                2|a1c8189|0,0,0,0,0,0|0|114a3281,84b,4552,0,1d002691,aa,1008d1,3|P2/floor(P1,4);floor(P2,2);newWallTile(P1,PATTERN_1,A,2);newWallTile(P1,PATTERN_2,B,3);newWallTile(P1,PATTERN_3,E,5);newWallTile(P2,PATTERN_1,C,2);newWallTile(P2,PATTERN_2,D,2);newWallTile(P2,PATTERN_5,D,1)
                4|82061c5|0,0,0,0,0,0,0,0,0,0|0|5900249,22,11145,2,15003011,29,60aa,3,1a000699,9,10d43,d,1168b489,490c,208cc,4|P1/floor(P1,2);floor(P2,1);floor(P3,1);floor(P4,6);newWallTile(P1,PATTERN_1,B,5);newWallTile(P1,PATTERN_4,E,4);newWallTile(P1,PATTERN_5,A,1);newWallTile(P2,PATTERN_1,C,5);newWallTile(P2,PATTERN_3,A,6);newWallTile(P2,PATTERN_5,C,1);newWallTile(P3,PATTERN_1,D,2);newWallTile(P3,PATTERN_2,D,2);newWallTile(P4,PATTERN_1,B,6);newWallTile(P4,PATTERN_2,C,5);newWallTile(P4,PATTERN_3,B,3)
                2|904b1cc|0,0,0,0,0,0|0|1505b691,491c,10878,7,12701281,6a,206a,0|P1/floor(P1,6);floor(P2,2);newWallTile(P1,PATTERN_1,C,3);newWallTile(P1,PATTERN_2,D,2);newWallTile(P1,PATTERN_3,D,4);newWallTile(P1,PATTERN_5,C,4);newWallTile(P2,PATTERN_1,A,4);newWallTile(P2,PATTERN_2,B,3);newWallTile(P2,PATTERN_4,D,1)
                2|50060c6|0,0,0,0,0,0|0|22083289,29,210e7c,e,158434a1,ca,120eb,6|P1/floor(P1,1);floor(P2,2);newWallTile(P1,PATTERN_1,B,9);newWallTile(P1,PATTERN_2,B,5);newWallTile(P1,PATTERN_3,A,6);newWallTile(P2,PATTERN_1,E,2);newWallTile(P2,PATTERN_2,C,7);newWallTile(P2,PATTERN_3,A,4);newWallTile(P2,PATTERN_5,C,2)
                2|6103085|0,0,0,0,0,0|0|1350b899,486c,4dd3,6,2003459,c2,500ad5,6|P2/floor(P1,6);floor(P2,2);newWallTile(P1,PATTERN_1,D,4);newWallTile(P1,PATTERN_2,E,7);newWallTile(P1,PATTERN_3,B,5);newWallTile(P1,PATTERN_4,C,4);newWallTile(P2,PATTERN_1,D,3);newWallTile(P2,PATTERN_3,A,5)
                4|3180103|0,0,0,0,0,0,0,0,0,0|0|d8d18a1,11,134b,8,1111b491,6d3,308fa,c,5019281,6a,9b4e,11,15900489,21,11709c,f|P2/floor(P1,1);floor(P2,4);floor(P3,2);floor(P4,1);newWallTile(P1,PATTERN_1,E,4);newWallTile(P1,PATTERN_2,E,4);newWallTile(P1,PATTERN_5,B,1);newWallTile(P2,PATTERN_1,C,6);newWallTile(P2,PATTERN_2,C,6);newWallTile(P2,PATTERN_3,D,4);newWallTile(P2,PATTERN_4,A,3);newWallTile(P3,PATTERN_1,A,4);newWallTile(P3,PATTERN_2,B,7);newWallTile(P3,PATTERN_5,A,1);newWallTile(P4,PATTERN_1,B,4);newWallTile(P4,PATTERN_2,C,5);newWallTile(P4,PATTERN_4,E,6);newWallTile(P4,PATTERN_5,C,4)
                3|714c20c|0,0,0,0,0,0,0,0|0|171b2a1,19,842,4,140e34a1,26b,10028,2,118c1009,0,100204,3|P2/floor(P1,1);floor(P2,4);newWallTile(P1,PATTERN_1,E,1);newWallTile(P1,PATTERN_2,B,2);newWallTile(P1,PATTERN_3,D,2);newWallTile(P1,PATTERN_4,D,4);newWallTile(P2,PATTERN_1,E,2);newWallTile(P2,PATTERN_2,C,2);newWallTile(P2,PATTERN_3,E,2);newWallTile(P3,PATTERN_1,B,2)
                2|73042c6|0,0,0,0,0,0|0|15243091,4db,2312,4,2410b009,2b,112c,0|P1/floor(P1,4);floor(P2,4);newWallTile(P1,PATTERN_1,C,2);newWallTile(P1,PATTERN_2,A,2);newWallTile(P1,PATTERN_3,A,2);newWallTile(P1,PATTERN_5,C,1);newWallTile(P2,PATTERN_1,B,3);newWallTile(P2,PATTERN_3,B,5);newWallTile(P2,PATTERN_4,A,4)
                4|6103207|0,0,0,0,0,0,0,0,0,0|0|c883699,da,41,0,116a20a1,29,20028,1,521099,0,1104,0,97132a1,5a,2008,1|P3/floor(P1,2);floor(P2,1);floor(P4,2);newWallTile(P1,PATTERN_1,D,1);newWallTile(P1,PATTERN_2,D,1);newWallTile(P1,PATTERN_3,A,1);newWallTile(P2,PATTERN_1,E,2);newWallTile(P2,PATTERN_2,A,2);newWallTile(P3,PATTERN_1,D,4);newWallTile(P3,PATTERN_2,A,1);newWallTile(P3,PATTERN_4,C,1);newWallTile(P4,PATTERN_1,E,2);newWallTile(P4,PATTERN_2,B,1);newWallTile(P4,PATTERN_3,C,2);newWallTile(P4,PATTERN_4,D,1)
                2|e38a28c|0,0,0,0,0,0|0|1149b4a1,953,4042,0,d000281,cb,850,3|P1/floor(P1,3);floor(P2,4);newWallTile(P1,PATTERN_1,E,1);newWallTile(P1,PATTERN_2,C,1);newWallTile(P1,PATTERN_3,D,1);newWallTile(P2,PATTERN_1,A,1);newWallTile(P2,PATTERN_2,B,2);newWallTile(P2,PATTERN_5,B,1)
                2|0|0,0,0,0,0,0|0|14903691,8eb,edfb,14,4859489,9,501add,c|P1/floor(P1,4);floor(P2,1);newWallTile(P1,PATTERN_1,C,7);newWallTile(P1,PATTERN_2,D,8);newWallTile(P1,PATTERN_3,A,8);newWallTile(P1,PATTERN_4,E,4);newWallTile(P2,PATTERN_1,B,8);newWallTile(P2,PATTERN_2,C,6)
                4|5148184|0,0,0,0,0,0,0,0,0,0|0|9483291,29,2048,1,11713099,91b,801,1,1b8a32a1,9,1101,3,13022081,a,10022,1|P4/floor(P1,1);floor(P2,4);floor(P3,1);floor(P4,2);newWallTile(P1,PATTERN_1,C,2);newWallTile(P1,PATTERN_2,B,4);newWallTile(P1,PATTERN_3,A,5);newWallTile(P2,PATTERN_1,D,1);newWallTile(P2,PATTERN_2,A,2);newWallTile(P2,PATTERN_3,C,1);newWallTile(P2,PATTERN_4,D,3);newWallTile(P3,PATTERN_1,E,1);newWallTile(P3,PATTERN_2,B,4);newWallTile(P3,PATTERN_3,E,2);newWallTile(P4,PATTERN_1,A,4);newWallTile(P4,PATTERN_2,A,4)
                4|71811c6|0,0,0,0,0,0,0,0,0,0|0|951b009,26c4,30cc,b,1400b289,a,14849,4,1c900099,b25,1991,9,15023451,0,10063,7|P3/floor(P1,6);floor(P2,2);floor(P3,8);newWallTile(P1,PATTERN_1,B,5);newWallTile(P1,PATTERN_3,D,1);newWallTile(P1,PATTERN_4,C,2);newWallTile(P2,PATTERN_1,B,6);newWallTile(P2,PATTERN_2,B,2);newWallTile(P2,PATTERN_3,B,2);newWallTile(P3,PATTERN_1,D,4);newWallTile(P3,PATTERN_2,A,5);newWallTile(P3,PATTERN_4,E,3);newWallTile(P4,PATTERN_1,C,3);newWallTile(P4,PATTERN_3,E,4);newWallTile(P4,PATTERN_5,C,5)
                4|414b0c6|0,0,0,0,0,0,0,0,0,0|0|c891489,29,1249,1,11723289,36cc,20078,4,3023689,21,914c,6,d8c3011,2,16098,5|P2/floor(P1,1);floor(P2,6);floor(P3,1);floor(P4,2);newWallTile(P1,PATTERN_1,B,4);newWallTile(P1,PATTERN_2,C,4);newWallTile(P2,PATTERN_1,B,2);newWallTile(P2,PATTERN_2,B,3);newWallTile(P2,PATTERN_3,E,3);newWallTile(P2,PATTERN_4,D,6);newWallTile(P3,PATTERN_1,B,5);newWallTile(P3,PATTERN_2,D,2);newWallTile(P3,PATTERN_3,E,5);newWallTile(P4,PATTERN_1,C,5);newWallTile(P4,PATTERN_3,A,6);newWallTile(P4,PATTERN_5,B,1)
                2|22430c3|0,0,0,0,0,0|0|1b263001,12,203356,7,25300281,434c,4312e,8|P2/floor(P1,2);floor(P2,6);newWallTile(P1,PATTERN_1,A,3);newWallTile(P1,PATTERN_3,E,6);newWallTile(P2,PATTERN_1,A,6);newWallTile(P2,PATTERN_2,B,5);newWallTile(P2,PATTERN_4,B,2);newWallTile(P2,PATTERN_5,E,5)
                4|c3ca34e|0,0,0,0,0,0,0,0,0,0|0|3491,893,0,0,b899,9a4d,0,0,723481,0,0,0,11880091,11,0,0|P3/floor(P1,3);floor(P2,3);floor(P4,1);newWallTile(P1,PATTERN_1,C,1);newWallTile(P1,PATTERN_2,C,1);newWallTile(P1,PATTERN_3,A,1);newWallTile(P2,PATTERN_1,D,1);newWallTile(P2,PATTERN_2,E,1);newWallTile(P2,PATTERN_3,B,1);newWallTile(P3,PATTERN_1,A,1);newWallTile(P3,PATTERN_2,C,1);newWallTile(P3,PATTERN_3,E,1);newWallTile(P3,PATTERN_4,D,2);newWallTile(P4,PATTERN_1,C,1);newWallTile(P4,PATTERN_2,A,1)
                4|8202206|0,0,0,0,0,0,0,0,0,0|0|199004a1,92,801a8c,b,15480299,1,11106,3,501b099,1,10886,5,11323689,12a,42145,3|P4/floor(P1,2);floor(P2,1);floor(P3,1);floor(P4,2);newWallTile(P1,PATTERN_1,E,5);newWallTile(P1,PATTERN_2,C,5);newWallTile(P1,PATTERN_4,E,4);newWallTile(P2,PATTERN_1,D,5);newWallTile(P2,PATTERN_2,B,5);newWallTile(P2,PATTERN_5,C,2);newWallTile(P3,PATTERN_1,D,3);newWallTile(P3,PATTERN_2,A,6);newWallTile(P3,PATTERN_3,D,2);newWallTile(P3,PATTERN_5,A,1);newWallTile(P4,PATTERN_1,B,5);newWallTile(P4,PATTERN_2,D,2);newWallTile(P4,PATTERN_3,E,3);newWallTile(P4,PATTERN_4,B,2)
                4|91c71c5|0,0,0,0,0,0,0,0,0,0|0|c4c3089,0,191,0,71b099,3004,8c006,7,24023491,11,20689,0,44b891,29,10c9,4|P4/floor(P2,6);floor(P3,1);floor(P4,1);newWallTile(P1,PATTERN_1,B,2);newWallTile(P1,PATTERN_2,A,5);newWallTile(P1,PATTERN_3,A,2);newWallTile(P2,PATTERN_1,D,3);newWallTile(P2,PATTERN_2,A,2);newWallTile(P2,PATTERN_3,D,2);newWallTile(P2,PATTERN_4,D,2);newWallTile(P3,PATTERN_1,C,4);newWallTile(P3,PATTERN_2,C,5);newWallTile(P3,PATTERN_3,E,2);newWallTile(P4,PATTERN_1,C,5);newWallTile(P4,PATTERN_2,E,5);newWallTile(P4,PATTERN_3,B,2)
                2|0|0,0,0,0,0,0|0|1d692499,11,aef7,e,a3030a1,482c,1044b8f,7|P1/floor(P1,1);floor(P2,6);newWallTile(P1,PATTERN_1,D,5);newWallTile(P1,PATTERN_2,C,8);newWallTile(P1,PATTERN_5,D,1);newWallTile(P2,PATTERN_1,E,8);newWallTile(P2,PATTERN_2,A,7);newWallTile(P2,PATTERN_3,A,5);newWallTile(P2,PATTERN_4,B,7)
                4|2001083|0,0,0,0,0,0,0,0,0,0|0|523011,21,1015fb,16,1d000001,36c4,109c65e,13,25283089,0,24fbd,13,1521680,249357,31ef,16|P1/floor(P1,1);floor(P2,6);floor(P4,14);newWallTile(P1,PATTERN_1,C,8);newWallTile(P1,PATTERN_3,E,6);newWallTile(P1,PATTERN_4,C,5);newWallTile(P2,PATTERN_1,A,5);newWallTile(P2,PATTERN_5,D,1);newWallTile(P3,PATTERN_1,B,5);newWallTile(P3,PATTERN_2,A,8);newWallTile(P3,PATTERN_3,A,7);newWallTile(P3,PATTERN_5,E,1);newWallTile(P4,PATTERN_2,D,5);newWallTile(P4,PATTERN_4,C,1)
                """;

        for (var testVector : testVectors.split("\n")) {
            var parts = testVector.split("/");
            var gameState = parseGameState(parts[0]);
            var calls = parsePointsObserverCalls(parts[1]);

            var pointsObserver = new RecordingPointsObserver();
            var actualGameState = new MutableGameState(gameState, pointsObserver);
            actualGameState.endRound();

            assertEquals(calls, pointsObserver.calls());
        }
    }

    @Test
    void mutableGameStateEndRoundCorrectlyUpdatesState() {
        var testVectors = """
                4|f30d2cd|0,0,0,0,0,0,0,0,0,0|0|88b261,963,0,0,700251,0,0,0,5680291,2,0,0,500299,9a,0,0|P2/4|f30d2cd|40000000,0,0,0,0,0,0,0,0,0|0|880240,0,2010,0,240,0,10004,2,680000,0,1000084,2,0,0,8088,1|P1
                4|9144106|0,0,0,0,0,0,0,0,0,0|0|1c901249,21,2010,0,15002289,0,10004,2,703489,135c,1000084,2,23489,1,8088,1|P4/4|9144106|40000000,0,0,0,0,0,0,0,0,0|0|1c001240,0,22012,1,2000,0,210086,8,0,0,1011186,4,0,0,898a,6|P3
                4|8189107|0,0,0,0,0,0,0,0,0,0|0|1c503259,1a,22012,1,3699,19,210086,8,25023099,29,1011186,4,d840091,4a,898a,6|P2/4|8189107|40000000,0,0,0,0,0,0,0,0,0|0|1c000240,0,2b01a,6,0,0,21128e,e,0,0,18119ce,15,840000,0,1089ce,11|P3
                2|0|0,0,0,0,0,0|0|d8db0a1,32b,f3af,11,120da2a1,9,32b6f,11|P1/2|0|40000000,0,0,0,0,0|0|8c0000,0,10f7ff,25,120da000,0,32bff,1e|P1
                4|3004003|0,0,0,0,0,0,0,0,0,0|0|1d2e1491,2494,2b09b,9,90b0a1,92b,211b8f,e,24b001,0,1851bde,21,1d043001,19,128dee,1b|P1/4|3004003|40000000,0,0,0,0,0,0,0,0,0|0|2e1000,0,42b19f,16,0,0,233bdf,26,240000,0,1853bdf,2e,40000,0,529def,2f|P2
                2|104103d1|0,0,0,0,0,0|0|1a88a281,2b,0,0,11013689,122,0,0|P2/2|104103d1|40000000,0,0,0,0,0|0|1a88a000,0,81,0,11000000,0,4202,2|P1
                2|1148f390|0,0,0,0,0,0|0|b003489,8a95,0,0,11680889,21,0,0|P1/2|1148f390|40000000,0,0,0,0,0|0|b000000,0,1102,0,11680000,0,22,1|P1
                2|a1c9206|0,0,0,0,0,0|0|c4cb281,6eb,512a,0,118a3699,3,10063,6|P2/2|a1c9206|40000000,0,0,0,0,0|0|c4c0000,0,71ab,a,11880000,0,10a6b,8|P1
                2|51820c4|0,0,0,0,0,0|0|1c302021,253,625cb,11,146c04a1,492c,cace,a|P2/2|51820c4|40000000,0,0,0,0,0|0|1c002000,0,e25db,12,146c0000,0,cbde,11|P2
                2|0|0,0,0,0,0,0|0|1d703891,21,e25db,12,1570b881,2b,cbde,11|P1/2|0|40000000,0,0,0,0,0|0|0,0,4f35ff,2e,0,0,21ebff,29|P2
                2|40c3106|0,0,0,0,0,0|0|1d023091,21,2b2ba,c,232cb4a1,2554,402cd,0|P1/2|40c3106|40000000,0,0,0,0,0|0|0,0,42bafe,23,232c0000,0,423dd,9|P2
                2|4289209|0,0,0,0,0,0|0|3080661,243,8482c,0,1a9034a1,4ab,4a8,0|P2/2|4289209|40000000,0,0,0,0,0|0|3080640,0,8483c,0,1a000000,0,215b8,7|P2
                4|72041c6|0,0,0,0,0,0,0,0,0,0|0|85b489,0,108cc,3,135224a1,0,10c3,5,5300689,a,80206c,5,2525b081,29,498c,9|P4/4|72041c6|40000000,0,0,0,0,0,0,0,0,0|0|840000,0,10dce,11,13022000,0,91d3,a,0,0,188226e,e,240000,0,804dcd,11|P4
                4|3144104|0,0,0,0,0,0,0,0,0,0|0|1a8436a1,29,10dce,11,13323691,21,91d3,a,11013281,0,188226e,e,243689,124906,804dcd,11|P1/4|3144104|40000000,0,0,0,0,0,0,0,0,0|0|1a840000,0,11fde,20,13000000,0,89bd7,1b,11000000,0,18862ef,1f,240000,0,805fcf,19|P1
                4|4040001|0,0,0,0,0,0,0,0,0,0|0|1d84b881,42,11fde,20,15853880,29,89bd7,1b,11083021,36dc,18862ef,1f,1124b021,a,805fcf,19|P3/4|4040001|40000000,0,0,0,0,0,0,0,0,0|0|840000,0,413fff,33,840000,0,28dbf7,26,11080000,0,18872ff,29,11240000,0,807fdf,27|P2
                4|d28e34e|0,0,0,0,0,0,0,0,0,0|0|89,4a,0,0,1922091,aa,0,0,1d700021,19,0,0,1313011,0,0,0|P3/4|d28e34e|40000000,0,0,0,0,0,0,0,0,0|0|0,0,42,1,1022000,0,20044,1,0,0,410010,2,1000000,0,84004,4|P2
                4|3147205|0,0,0,0,0,0,0,0,0,0|0|500891,0,42,1,1123299,2486d,20044,1,903009,36dc,410010,2,3000489,0,84004,4|P4/4|3147205|40000000,0,0,0,0,0,0,0,0,0|0|0,0,8066,6,1000000,0,608cc,3,0,0,431012,3,3000000,0,84106,7|P2
                2|e391452|0,0,0,0,0,0|0|23891,252e,0,0,1b299,2db,0,0|P2/2|e391452|40000000,0,0,0,0,0|0|0,0,824,0,0,0,488,0|P1
                4|40|0,0,0,0,0,0,0,0,0,0|0|319699,0,c0f6,d,1013681,42,699ce,13,12023081,162,43109a,8,7238a1,91c,108630e,14|P1/4|40|40000000,0,0,0,0,0,0,0,0,0|0|19000,0,8c2fe,18,1000000,0,6dbcf,1b,12000000,0,4318db,12,0,0,1096b3e,1b|P3
                2|d30c34a|0,0,0,0,0,0|0|c013899,dc,1102,0,11722081,743,22,1|P2/2|d30c34a|40000000,0,0,0,0,0|0|c000000,0,512a,0,11022000,0,10063,6|P2
                """;

        for (var testVector : testVectors.split("\n")) {
            var gameStatePair = parseGameStatePair(testVector);

            var actualGameState = new MutableGameState(gameStatePair.before);
            actualGameState.endRound();

            var expectedGameState = gameStatePair.after;
            assertEqualsGameStateStrict(expectedGameState, actualGameState);
        }
    }

    @Test
    void mutableGameStateEndGameCorrectlyCallsObserverMethods() {
        var testVectors = """
                2|0|40000000,0,0,0,0,0|0|24000000,0,253fff,37,3840000,0,2139ff,22|P2/fullColumn(P1,1,7);fullColumn(P2,1,7);fullRow(P1,PATTERN_1,2);fullRow(P1,PATTERN_2,2);fullRow(P2,PATTERN_1,2)
                4|3083000|40000000,0,0,0,0,0,0,0,0,0|0|80000,0,5973ff,31,1a000000,0,49bff,23,a000000,0,18c73ff,40,0,0,a34fff,35|P3/fullColor(P1,B,10);fullColor(P3,A,10);fullColor(P4,E,10);fullColumn(P3,3,7);fullColumn(P3,4,7);fullColumn(P4,1,7);fullRow(P1,PATTERN_1,2);fullRow(P1,PATTERN_2,2);fullRow(P2,PATTERN_1,2);fullRow(P2,PATTERN_2,2);fullRow(P3,PATTERN_1,2);fullRow(P3,PATTERN_2,2);fullRow(P4,PATTERN_1,2);fullRow(P4,PATTERN_2,2)
                4|4084102|40000000,0,0,0,0,0,0,0,0,0|0|1a0c0000,0,10b5ef,26,22040000,0,2378ff,24,240000,0,423fff,34,440000,0,1212d7f,1f|P3/fullColumn(P1,0,7);fullColumn(P2,1,7);fullColumn(P3,2,7);fullColumn(P4,1,7);fullRow(P2,PATTERN_1,2);fullRow(P3,PATTERN_1,2);fullRow(P3,PATTERN_2,2);fullRow(P4,PATTERN_1,2)
                4|2001083|40000000,0,0,0,0,0,0,0,0,0|0|0,0,109dff,28,0,0,149c65f,13,280000,0,825fff,28,1021000,0,b3ef,e|P4/fullColor(P2,D,10);fullColor(P3,E,10);fullColumn(P1,0,7);fullColumn(P2,4,7);fullRow(P1,PATTERN_1,2);fullRow(P2,PATTERN_1,2);fullRow(P3,PATTERN_1,2);fullRow(P3,PATTERN_2,2);fullRow(P4,PATTERN_2,2)
                4|2082080|40000000,0,0,0,0,0,0,0,0,0|0|1300a000,0,10711ff,1d,2880000,0,207dff,31,11000000,0,492fff,29,11002000,0,36bff,2b|P1/fullColor(P1,A,10);fullColor(P3,D,10);fullRow(P1,PATTERN_1,2);fullRow(P2,PATTERN_1,2);fullRow(P2,PATTERN_3,2);fullRow(P3,PATTERN_1,2);fullRow(P3,PATTERN_2,2);fullRow(P4,PATTERN_1,2);fullRow(P4,PATTERN_2,2)
                3|20820c1|40000000,0,0,0,0,0,0,0|0|21280000,0,217def,26,230ca000,0,2319fe,1b,224c0000,0,437ff,1a|P2/fullColumn(P1,1,7);fullColumn(P2,1,7);fullRow(P1,PATTERN_3,2);fullRow(P3,PATTERN_1,2);fullRow(P3,PATTERN_2,2)
                2|0|40000000,0,0,0,0,0|0|252000,0,603bdf,1d,3691000,0,8c33bf,1a|P2/fullColumn(P2,3,7);fullRow(P1,PATTERN_1,2);fullRow(P2,PATTERN_1,2)
                2|0|40000000,0,0,0,0,0|0|14000000,0,4979ff,29,3240000,0,847bef,22|P2/fullColumn(P2,3,7);fullRow(P1,PATTERN_1,2);fullRow(P2,PATTERN_2,2)
                2|0|40000000,0,0,0,0,0|0|6c0000,0,1184fff,21,12000000,0,63ffd,20|P1/fullColumn(P1,4,7);fullRow(P1,PATTERN_1,2);fullRow(P1,PATTERN_2,2);fullRow(P2,PATTERN_2,2)
                2|0|40000000,0,0,0,0,0|0|14000000,0,2ffff,2b,4859000,0,501bdf,19|P1/fullRow(P1,PATTERN_1,2);fullRow(P1,PATTERN_2,2);fullRow(P1,PATTERN_3,2);fullRow(P2,PATTERN_1,2)
                2|0|40000000,0,0,0,0,0|0|21640000,0,118e7ff,30,23080000,0,431bff,23|P2/fullColor(P1,B,10);fullColumn(P1,0,7);fullColumn(P1,4,7);fullColumn(P2,2,7);fullRow(P1,PATTERN_1,2);fullRow(P1,PATTERN_2,2);fullRow(P2,PATTERN_1,2);fullRow(P2,PATTERN_2,2)
                3|81104|40000000,0,0,0,0,0,0,0|0|26c1240,0,224f7e,19,2000000,0,276fdf,27,0,0,9c2bdf,26|P1/fullColor(P3,B,10);fullColumn(P2,1,7);fullColumn(P3,3,7);fullRow(P2,PATTERN_1,2);fullRow(P3,PATTERN_1,2)
                4|41040|40000000,0,0,0,0,0,0,0,0,0|0|1b000000,0,18aeff,20,0,0,4a5bff,30,0,0,25b9ff,27,24000000,0,4e77f,28|P1/fullColor(P1,B,10);fullColumn(P1,0,7);fullColumn(P2,2,7);fullColumn(P3,1,7);fullRow(P1,PATTERN_1,2);fullRow(P2,PATTERN_1,2);fullRow(P2,PATTERN_2,2);fullRow(P3,PATTERN_1,2);fullRow(P4,PATTERN_1,2)
                4|142000|40000000,0,0,0,0,0,0,0,0,0|0|0,0,a35bff,3b,0,0,14d377f,15,0,0,18afff,28,a000000,0,2d3ff,2e|P3/fullColor(P1,E,10);fullColor(P2,A,10);fullColor(P2,D,10);fullColor(P3,B,10);fullColumn(P1,1,7);fullColumn(P3,0,7);fullRow(P1,PATTERN_1,2);fullRow(P1,PATTERN_2,2);fullRow(P2,PATTERN_1,2);fullRow(P3,PATTERN_1,2);fullRow(P3,PATTERN_2,2);fullRow(P4,PATTERN_1,2);fullRow(P4,PATTERN_2,2)
                4|3043001|40000000,0,0,0,0,0,0,0,0,0|0|11000,0,1d2fdf,27,0,0,29fff,2c,0,0,1086fff,1e,1a000000,0,219dff,33|P1/fullColor(P1,B,10);fullColumn(P3,4,7);fullColumn(P4,1,7);fullRow(P1,PATTERN_1,2);fullRow(P2,PATTERN_1,2);fullRow(P2,PATTERN_2,2);fullRow(P3,PATTERN_1,2);fullRow(P3,PATTERN_2,2);fullRow(P4,PATTERN_1,2)
                2|830520a|40000000,0,0,0,0,0|0|8a000,0,7313ff,17,14000000,0,a7bf7,e|P2/fullColumn(P1,2,7);fullRow(P1,PATTERN_1,2);fullRow(P1,PATTERN_2,2);fullRow(P2,PATTERN_2,2)
                2|0|40000000,0,0,0,0,0|0|0,0,298fcf,18,3840000,0,885bff,11|P2/fullColumn(P1,1,7);fullRow(P2,PATTERN_1,2);fullRow(P2,PATTERN_2,2)
                4|30c2041|40000000,0,0,0,0,0,0,0,0,0|0|2440000,0,822bff,22,2861000,0,4197ff,21,21019000,0,1ca3f7,20,22000000,0,14ad39f,26|P4/fullColor(P1,E,10);fullColor(P2,D,10);fullColor(P3,B,10);fullColumn(P4,2,7);fullColumn(P4,4,7);fullRow(P1,PATTERN_1,2);fullRow(P1,PATTERN_2,2);fullRow(P2,PATTERN_1,2);fullRow(P2,PATTERN_2,2);fullRow(P3,PATTERN_2,2);fullRow(P4,PATTERN_1,2)
                4|10c1040|40000000,0,0,0,0,0,0,0,0,0|0|19880000,0,a47bdf,26,1000,0,1387ff,2b,b080000,0,1fff,29,1b0c0000,0,807bff,33|P3/fullColumn(P1,3,7);fullColumn(P2,0,7);fullRow(P1,PATTERN_1,2);fullRow(P2,PATTERN_1,2);fullRow(P2,PATTERN_2,2);fullRow(P3,PATTERN_1,2);fullRow(P3,PATTERN_2,2);fullRow(P4,PATTERN_1,2);fullRow(P4,PATTERN_2,2)
                4|3083081|40000000,0,0,0,0,0,0,0,0,0|0|d2000,0,c233f7,22,1040000,0,a1cb7f,1c,1c022000,0,943ff,28,9000000,0,10c3dff,27|P1/fullColor(P2,C,10);fullColor(P4,A,10);fullColumn(P1,2,7);fullColumn(P2,1,7);fullRow(P1,PATTERN_2,2);fullRow(P2,PATTERN_1,2);fullRow(P3,PATTERN_1,2);fullRow(P3,PATTERN_2,2);fullRow(P4,PATTERN_1,2)
                """;

        for (var testVector : testVectors.split("\n")) {
            var parts = testVector.split("/");
            var gameState = parseGameState(parts[0]);
            var calls = parsePointsObserverCalls(parts[1]);

            var pointsObserver = new RecordingPointsObserver();
            var actualGameState = new MutableGameState(gameState, pointsObserver);
            actualGameState.endGame();

            assertEquals(calls, pointsObserver.calls());
        }
    }

    @Test
    void mutableGameStateEndGameCorrectlyUpdatesState() {
        var testVectors = """
                2|0|40000000,0,0,0,0,0|0|24000000,0,253fff,37,3840000,0,2139ff,22|P2/2|0|40000000,0,0,0,0,0|0|24000000,0,253fff,42,3840000,0,2139ff,2b|P2
                4|3083000|40000000,0,0,0,0,0,0,0,0,0|0|80000,0,5973ff,31,1a000000,0,49bff,23,a000000,0,18c73ff,40,0,0,a34fff,35|P3/4|3083000|40000000,0,0,0,0,0,0,0,0,0|0|80000,0,5973ff,3f,1a000000,0,49bff,27,a000000,0,18c73ff,5c,0,0,a34fff,4a|P3
                4|4084102|40000000,0,0,0,0,0,0,0,0,0|0|1a0c0000,0,10b5ef,26,22040000,0,2378ff,24,240000,0,423fff,34,440000,0,1212d7f,1f|P3/4|4084102|40000000,0,0,0,0,0,0,0,0,0|0|1a0c0000,0,10b5ef,2d,22040000,0,2378ff,2d,240000,0,423fff,3f,440000,0,1212d7f,28|P3
                4|2001083|40000000,0,0,0,0,0,0,0,0,0|0|0,0,109dff,28,0,0,149c65f,13,280000,0,825fff,28,1021000,0,b3ef,e|P4/4|2001083|40000000,0,0,0,0,0,0,0,0,0|0|0,0,109dff,31,0,0,149c65f,26,280000,0,825fff,36,1021000,0,b3ef,10|P4
                4|2082080|40000000,0,0,0,0,0,0,0,0,0|0|1300a000,0,10711ff,1d,2880000,0,207dff,31,11000000,0,492fff,29,11002000,0,36bff,2b|P1/4|2082080|40000000,0,0,0,0,0,0,0,0,0|0|1300a000,0,10711ff,29,2880000,0,207dff,35,11000000,0,492fff,37,11002000,0,36bff,2f|P1
                3|20820c1|40000000,0,0,0,0,0,0,0|0|21280000,0,217def,26,230ca000,0,2319fe,1b,224c0000,0,437ff,1a|P2/3|20820c1|40000000,0,0,0,0,0,0,0|0|21280000,0,217def,2f,230ca000,0,2319fe,22,224c0000,0,437ff,1e|P2
                2|0|40000000,0,0,0,0,0|0|252000,0,603bdf,1d,3691000,0,8c33bf,1a|P2/2|0|40000000,0,0,0,0,0|0|252000,0,603bdf,1f,3691000,0,8c33bf,23|P2
                2|0|40000000,0,0,0,0,0|0|14000000,0,4979ff,29,3240000,0,847bef,22|P2/2|0|40000000,0,0,0,0,0|0|14000000,0,4979ff,2b,3240000,0,847bef,2b|P2
                2|0|40000000,0,0,0,0,0|0|6c0000,0,1184fff,21,12000000,0,63ffd,20|P1/2|0|40000000,0,0,0,0,0|0|6c0000,0,1184fff,2c,12000000,0,63ffd,22|P1
                2|0|40000000,0,0,0,0,0|0|14000000,0,2ffff,2b,4859000,0,501bdf,19|P1/2|0|40000000,0,0,0,0,0|0|14000000,0,2ffff,31,4859000,0,501bdf,1b|P1
                2|0|40000000,0,0,0,0,0|0|21640000,0,118e7ff,30,23080000,0,431bff,23|P2/2|0|40000000,0,0,0,0,0|0|21640000,0,118e7ff,4c,23080000,0,431bff,2e|P2
                3|81104|40000000,0,0,0,0,0,0,0|0|26c1240,0,224f7e,19,2000000,0,276fdf,27,0,0,9c2bdf,26|P1/3|81104|40000000,0,0,0,0,0,0,0|0|26c1240,0,224f7e,19,2000000,0,276fdf,30,0,0,9c2bdf,39|P1
                4|41040|40000000,0,0,0,0,0,0,0,0,0|0|1b000000,0,18aeff,20,0,0,4a5bff,30,0,0,25b9ff,27,24000000,0,4e77f,28|P1/4|41040|40000000,0,0,0,0,0,0,0,0,0|0|1b000000,0,18aeff,33,0,0,4a5bff,3b,0,0,25b9ff,30,24000000,0,4e77f,2a|P1
                4|142000|40000000,0,0,0,0,0,0,0,0,0|0|0,0,a35bff,3b,0,0,14d377f,15,0,0,18afff,28,a000000,0,2d3ff,2e|P3/4|142000|40000000,0,0,0,0,0,0,0,0,0|0|0,0,a35bff,50,0,0,14d377f,2b,0,0,18afff,3d,a000000,0,2d3ff,32|P3
                4|3043001|40000000,0,0,0,0,0,0,0,0,0|0|11000,0,1d2fdf,27,0,0,29fff,2c,0,0,1086fff,1e,1a000000,0,219dff,33|P1/4|3043001|40000000,0,0,0,0,0,0,0,0,0|0|11000,0,1d2fdf,33,0,0,29fff,30,0,0,1086fff,29,1a000000,0,219dff,3c|P1
                2|830520a|40000000,0,0,0,0,0|0|8a000,0,7313ff,17,14000000,0,a7bf7,e|P2/2|830520a|40000000,0,0,0,0,0|0|8a000,0,7313ff,22,14000000,0,a7bf7,10|P2
                2|0|40000000,0,0,0,0,0|0|0,0,298fcf,18,3840000,0,885bff,11|P2/2|0|40000000,0,0,0,0,0|0|0,0,298fcf,1f,3840000,0,885bff,15|P2
                4|30c2041|40000000,0,0,0,0,0,0,0,0,0|0|2440000,0,822bff,22,2861000,0,4197ff,21,21019000,0,1ca3f7,20,22000000,0,14ad39f,26|P4/4|30c2041|40000000,0,0,0,0,0,0,0,0,0|0|2440000,0,822bff,30,2861000,0,4197ff,2f,21019000,0,1ca3f7,2c,22000000,0,14ad39f,36|P4
                4|10c1040|40000000,0,0,0,0,0,0,0,0,0|0|19880000,0,a47bdf,26,1000,0,1387ff,2b,b080000,0,1fff,29,1b0c0000,0,807bff,33|P3/4|10c1040|40000000,0,0,0,0,0,0,0,0,0|0|19880000,0,a47bdf,2f,1000,0,1387ff,36,b080000,0,1fff,2d,1b0c0000,0,807bff,37|P3
                4|3083081|40000000,0,0,0,0,0,0,0,0,0|0|d2000,0,c233f7,22,1040000,0,a1cb7f,1c,1c022000,0,943ff,28,9000000,0,10c3dff,27|P1/4|3083081|40000000,0,0,0,0,0,0,0,0,0|0|d2000,0,c233f7,2b,1040000,0,a1cb7f,2f,1c022000,0,943ff,2c,9000000,0,10c3dff,33|P1
                """;

        for (var testVector : testVectors.split("\n")) {
            var gameStatePair = parseGameStatePair(testVector);

            var actualGameState = new MutableGameState(gameStatePair.before);
            actualGameState.endGame();

            var expectedGameState = gameStatePair.after;
            assertEqualsGameStateStrict(expectedGameState, actualGameState);
        }
    }

    void assertEqualsGameStateStrict(ReadOnlyGameState expected, ReadOnlyGameState actual) {
        assertEquals(expected.game().playerDescriptions(), actual.game().playerDescriptions());
        assertEquals(expected.pkTileBag(), actual.pkTileBag());
        assertIntArrayEquals(expected.pkTileSources(), actual.pkTileSources());
        assertEquals(expected.pkUniqueTileSources(), actual.pkUniqueTileSources());
        assertIntArrayEquals(expected.pkPlayerStates(), actual.pkPlayerStates());
        assertEquals(expected.currentPlayerId(), actual.currentPlayerId());
    }

    /// Assert that the two game states are equal, without caring about the order of the tiles on the floor line.
    void assertEqualsGameStateLax(ReadOnlyGameState expected, ReadOnlyGameState actual) {
        assertEquals(expected.game().playerDescriptions(), actual.game().playerDescriptions());
        assertEquals(expected.pkTileBag(), actual.pkTileBag());
        assertIntArrayEquals(expected.pkTileSources(), actual.pkTileSources());
        var expectedPS = expected.pkPlayerStates();
        var actualPS = actual.pkPlayerStates();
        for (var playerId : expected.game().playerIds()) {
            assertEquals(
                    PkPlayerStates.pkPatterns(expectedPS, playerId),
                    PkPlayerStates.pkPatterns(actualPS, playerId));
            assertEquals(
                    PkFloor.asPkTileSet(PkPlayerStates.pkFloor(expectedPS, playerId)),
                    PkFloor.asPkTileSet(PkPlayerStates.pkFloor(actualPS, playerId)));
            assertEquals(
                    PkPlayerStates.pkWall(expectedPS, playerId),
                    PkPlayerStates.pkWall(actualPS, playerId));
            assertEquals(
                    PkPlayerStates.points(expectedPS, playerId),
                    PkPlayerStates.points(actualPS, playerId));
        }
        assertEquals(expected.currentPlayerId(), actual.currentPlayerId());
    }

    void assertIntArrayEquals(ReadOnlyIntArray expected, ReadOnlyIntArray actual) {
        assertEquals(expected.size(), actual.size());
        for (var i = 0; i < expected.size(); i += 1)
            assertEquals(expected.get(i), actual.get(i));
    }

    static final class RecordingPointsObserver implements PointsObserver {
        private final Set<String> calls = new TreeSet<>();

        public Set<String> calls() {
            return calls;
        }

        @Override
        public void newWallTile(PlayerId playerId, TileDestination.Pattern line, TileKind.Colored color, int points) {
            calls.add("newWallTile(%s,%s,%s,%d)".formatted(playerId, line, color, points));
        }

        @Override
        public void floor(PlayerId playerId, int penalty) {
            calls.add("floor(%s,%d)".formatted(playerId, penalty));
        }

        @Override
        public void fullRow(PlayerId playerId, TileDestination.Pattern line, int points) {
            calls.add("fullRow(%s,%s,%d)".formatted(playerId, line, points));
        }

        @Override
        public void fullColumn(PlayerId playerId, int column, int points) {
            calls.add("fullColumn(%s,%d,%d)".formatted(playerId, column, points));
        }

        @Override
        public void fullColor(PlayerId playerId, TileKind.Colored color, int points) {
            calls.add("fullColor(%s,%s,%d)".formatted(playerId, color, points));
        }
    }

    static Set<String> parsePointsObserverCalls(String string) {
        return Arrays.stream(string.split(";"))
                .collect(Collectors.toSet());
    }

    record MoveTriplet(ReadOnlyGameState before, Move move, ReadOnlyGameState after) {}

    static MoveTriplet parseMoveTriplet(String string) {
        var parts = string.split("/");
        return new MoveTriplet(parseGameState(parts[0]), parseMove(parts[1]), parseGameState(parts[2]));
    }

    record GameStatePair(ReadOnlyGameState before, ReadOnlyGameState after) { }

    static GameStatePair parseGameStatePair(String string) {
        var parts = string.split("/");
        return new GameStatePair(parseGameState(parts[0]), parseGameState(parts[1]));
    }

    static ImmutableGameState parseGameState(String string) {
        var allPlayers = List.of(
                new Game.PlayerDescription(PlayerId.P1, "P1", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P2, "P2", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P3, "P3", Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P4, "P4", Game.PlayerDescription.PlayerKind.HUMAN));

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

    static int parseIntBase16(String string) {
        return Integer.parseInt(string, 16);
    }

    static ReadOnlyIntArray parseIntArray(String string) {
        return MutableIntArray.wrapping(
                Arrays.stream(string.split(Pattern.quote(",")))
                        .mapToInt(MutableGameStateTest::parseIntBase16)
                        .toArray());
    }

    static Move parseMove(String moveString) {
        var sourceI = Integer.parseInt(moveString.substring(0, 1));
        var color = TileKind.Colored.valueOf(moveString.substring(1, 2));
        var destinationI = Integer.parseInt(moveString.substring(2, 3));
        destinationI = (destinationI + TileDestination.COUNT - 1) % TileDestination.COUNT;
        return new Move(TileSource.ALL.get(sourceI), color, TileDestination.ALL.get(destinationI));
    }
}