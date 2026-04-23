package ch.epfl.ajul.mcts;

import ch.epfl.ajul.TileDestination;
import ch.epfl.ajul.TileKind;
import ch.epfl.ajul.TileSource;
import ch.epfl.ajul.gamestate.ReadOnlyGameState;
import ch.epfl.ajul.gamestate.packed.PkMove;
import ch.epfl.ajul.gamestate.packed.PkPatterns;
import ch.epfl.ajul.gamestate.packed.PkPlayerStates;
import ch.epfl.ajul.gamestate.packed.PkTileSet;

import java.util.random.RandomGenerator;

public final class HeuristicMoveSelector {


    public static int selectMove(RandomGenerator randomGenerator,
                                 ReadOnlyGameState gameState,
                                 short[] packedMoveArray,
                                 int validMoves) {

        int pattern = PkPlayerStates.pkPatterns(gameState.pkPlayerStates(), gameState.currentPlayerId());

        int res1, res2, res3;
        res1 = res2 = res3 = -1;

        int count1, count2, count3;
        count1 = count2 = count3 = 0;

        for (int i = 0; i < validMoves; i++){
            TileKind.Colored color = PkMove.color(packedMoveArray[i]);
            TileDestination destination = PkMove.destination(packedMoveArray[i]);
            TileSource source = PkMove.source(packedMoveArray[i]);

            if (destination instanceof TileDestination.Pattern line) {
                int remaining = line.capacity() - PkPatterns.size(pattern, line);
                int tileCount = PkTileSet.countOf(
                        gameState.pkTileSources().get(source.index()), color);

                //Cas 1 : les coups remplissent totalement la ligne de motif
                if (tileCount == remaining) {
                    int j = randomGenerator.nextInt(count1 + 1);
                    if (j == 0) res1 = i;
                    count1++;
                }

                //Cas 2 : les coups remplissent partiellement une ligne de motif
                else if (tileCount < remaining){
                    int j = randomGenerator.nextInt(count2 + 1);
                    if (j == 0) res2 = i;
                    count2++;
                }

                //Cas 3 : Autres coups/Les coups qui remplissent une ligne de motif avec des tuiles excédentaires
                else {
                    int j = randomGenerator.nextInt(count3 + 1);
                    if (j == 0) res3 = i;
                    count3++;
                }
            }
            //Cas 3 : Autres coups/Les coups qui remplissent la ligne plancher
            else {
                int j = randomGenerator.nextInt(count3 + 1);
                if (j == 0) res3 = i;
                count3++;
            }
        }

        if (res1 != -1) return res1;
        if (res2 != -1) return res2;
        return res3;

    }

    private static class ReservoirSampler {

        public static int sampler;

    }



}
